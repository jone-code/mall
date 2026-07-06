import { http, BASE_URL } from "@/utils/request";
import { useUserStore } from "@/stores/user";
import type { UserInfo } from "@/stores/user";

/** 与后端 RedisKeys.SCENE_LOGIN 一致 */
export const SMS_SCENE_LOGIN = "LOGIN";

export interface LoginResult {
  accessToken: string;
  refreshToken: string;
  sid: string;
  accessExp: number;
  isNewUser?: boolean;
  user: UserInfo;
}

export interface TokenRefreshResult {
  accessToken: string;
  refreshToken: string;
  accessExp: number;
}

export function sendSms(phone: string) {
  return http.post<{ ttl: number }>(
    "/sms/send",
    { phone, scene: SMS_SCENE_LOGIN },
    { noAuth: true }
  );
}

export function loginBySms(payload: {
  phone: string;
  code: string;
  deviceId: string;
  deviceType: string;
}) {
  return http.post<LoginResult>("/login/sms", payload, { noAuth: true });
}

export function loginByWechat(payload: {
  code: string;
  deviceId: string;
  deviceType: string;
}) {
  return http.post<LoginResult>("/oauth/wechat", payload, { noAuth: true });
}

export function logout() {
  return http.post<void>("/logout");
}

export function updateMe(payload: { nickname?: string; avatarUrl?: string }) {
  return http.put<UserInfo>("/me", payload);
}

export function getMe() {
  return http.get<UserInfo>("/me");
}

export function changePhone(phone: string, code: string) {
  return http.put<UserInfo>("/me/phone", { phone, code });
}

export function uploadAvatarMock(localPath?: string) {
  return http.post<{ avatarUrl: string }>("/me/avatar", { localPath });
}

export interface SessionItem {
  sid: string;
  deviceId?: string;
  deviceType?: string;
  createdAt?: number;
  lastActiveAt?: number;
  current: boolean;
}

export function listSessions() {
  return http.get<SessionItem[]>("/me/sessions");
}

export function killSession(sid: string) {
  return http.del<void>(`/me/sessions/${sid}`);
}

function resolveUploadUrl(): string {
  if (BASE_URL.startsWith("http")) return `${BASE_URL}/upload`;
  // #ifdef H5
  if (typeof location !== "undefined") {
    return `${location.origin}${BASE_URL}/upload`;
  }
  // #endif
  return `${import.meta.env.VITE_API_BASE || "http://localhost:8001/api"}/upload`;
}

/** 上传图片到 BFF，返回可访问的相对路径如 /files/xxx.jpg */
export function uploadImage(filePath: string): Promise<string> {
  const userStore = useUserStore();
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: resolveUploadUrl(),
      filePath,
      name: "file",
      header: userStore.accessToken
        ? { Authorization: `Bearer ${userStore.accessToken}` }
        : {},
      success: (res) => {
        try {
          const body = JSON.parse(res.data || "{}");
          if (body.code === 0 && body.data?.url) {
            resolve(body.data.url);
            return;
          }
          reject(new Error(body.message || "上传失败"));
        } catch {
          reject(new Error("上传响应解析失败"));
        }
      },
      fail: (err) => reject(err),
    });
  });
}
