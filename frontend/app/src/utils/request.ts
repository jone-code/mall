import { useUserStore } from "@/stores/user";
import { applyAuthTokens } from "@/utils/auth-session";

// 开发环境走 Vite 代理 /api；生产或真机调试可设 VITE_API_BASE=http://<host>:8001/api
export const BASE_URL = import.meta.env.VITE_API_BASE || "/api";

export interface ApiResult<T = unknown> {
  code: number;
  message?: string;
  data: T;
}

export interface RequestOptions {
  url: string;
  method?:
    | "GET"
    | "POST"
    | "PUT"
    | "DELETE"
    | "PATCH"
    | "HEAD"
    | "OPTIONS";
  data?: any;
  header?: Record<string, string>;
  noAuth?: boolean;
  _retried?: boolean;
}

let refreshPromise: Promise<boolean> | null = null;

function rawRequest<T = any>(opts: RequestOptions): Promise<{
  statusCode: number;
  data: T;
  header?: any;
}> {
  return new Promise((resolve, reject) => {
    uni.request({
      url: opts.url.startsWith("http") ? opts.url : `${BASE_URL}${opts.url}`,
      method: (opts.method || "GET") as any,
      data: opts.data,
      header: opts.header,
      success: (res: any) => resolve(res),
      fail: (err: any) => reject(err),
    });
  });
}

async function doRefresh(): Promise<boolean> {
  const userStore = useUserStore();
  if (!userStore.refreshToken || !userStore.sid) return false;
  try {
    const res = await rawRequest<ApiResult<any>>({
      url: "/token/refresh",
      method: "POST",
      data: {
        sid: userStore.sid,
        refreshToken: userStore.refreshToken,
      },
      header: { "Content-Type": "application/json" },
    });
    const body = res.data as ApiResult<any>;
    if (
      res.statusCode === 200 &&
      body?.data?.accessToken &&
      body?.data?.refreshToken
    ) {
      applyAuthTokens(userStore, {
        accessToken: body.data.accessToken,
        refreshToken: body.data.refreshToken,
        accessExp: body.data.accessExp,
      });
      return true;
    }
    return false;
  } catch {
    return false;
  }
}

function ensureRefresh(): Promise<boolean> {
  if (!refreshPromise) {
    refreshPromise = doRefresh().finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}

function gotoLogin() {
  const pages = getCurrentPages();
  const cur = pages[pages.length - 1];
  const route = cur ? (cur as any).route : "";
  if (route && route.indexOf("pages/login/") === 0) return;
  uni.reLaunch({ url: "/pages/login/index" });
}

function isKickedPayload(payload: any): boolean {
  if (!payload) return false;
  return (
    payload.reason === "KICKED" ||
    payload.data?.reason === "KICKED" ||
    payload.message?.includes?.("KICKED")
  );
}

export async function request<T = any>(
  opts: RequestOptions
): Promise<ApiResult<T>> {
  const userStore = useUserStore();

  if (
    !opts.noAuth &&
    userStore.isLogin &&
    userStore.accessExpiringSoon &&
    !opts._retried &&
    opts.url !== "/token/refresh"
  ) {
    await ensureRefresh();
  }

  const header: Record<string, string> = {
    "Content-Type": "application/json",
    ...(opts.header || {}),
  };
  if (!opts.noAuth && userStore.accessToken) {
    header["Authorization"] = `Bearer ${userStore.accessToken}`;
  }

  const res = await rawRequest<ApiResult<T>>({ ...opts, header });

  if (res.statusCode === 401 && !opts._retried && opts.url !== "/token/refresh") {
    const ok = await ensureRefresh();
    if (ok) {
      return request<T>({ ...opts, _retried: true });
    }
    userStore.clear();
    if (isKickedPayload(res.data)) {
      uni.showToast({ title: "您的账号已在其他设备登录", icon: "none" });
    }
    gotoLogin();
    throw new Error("UNAUTHORIZED");
  }

  if (res.statusCode < 200 || res.statusCode >= 300) {
    const body = res.data as ApiResult<T>;
    const msg =
      (body && typeof body === "object" && body.message) ||
      `请求失败 (${res.statusCode})`;
    throw Object.assign(new Error(msg), {
      statusCode: res.statusCode,
      payload: body,
    });
  }

  // 后端对部分业务错误（如 404xx/409xx）映射为 HTTP 200 + 非 0 code，
  // 这里统一在请求层抛出，避免业务错误被页面静默忽略。
  const body = res.data as ApiResult<T>;
  if (body && typeof body.code === "number" && body.code !== 0) {
    throw Object.assign(new Error(body.message || "BIZ_ERROR"), {
      statusCode: res.statusCode,
      payload: body,
    });
  }

  return body;
}

export const http = {
  get: <T = any>(url: string, data?: any, options: Partial<RequestOptions> = {}) =>
    request<T>({ url, method: "GET", data, ...options }),
  post: <T = any>(url: string, data?: any, options: Partial<RequestOptions> = {}) =>
    request<T>({ url, method: "POST", data, ...options }),
  put: <T = any>(url: string, data?: any, options: Partial<RequestOptions> = {}) =>
    request<T>({ url, method: "PUT", data, ...options }),
  del: <T = any>(url: string, data?: any, options: Partial<RequestOptions> = {}) =>
    request<T>({ url, method: "DELETE", data, ...options }),
};
