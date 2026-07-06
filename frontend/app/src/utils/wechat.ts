import { loginByWechat } from "@/api/auth";
import { getDeviceId, getDeviceType } from "@/utils/device";

/**
 * Cross-platform WeChat OAuth entry. Returns LoginResult on success.
 * - MP-WEIXIN: wx.login -> code
 * - APP-PLUS:  uni.login provider=weixin -> code
 * - H5:        redirect to public-account OAuth url; resolves never (page unloaded)
 */
export async function wechatLogin(): Promise<any> {
  // #ifdef MP-WEIXIN
  return new Promise((resolve, reject) => {
    // @ts-ignore wx is global in MP
    wx.login({
      success: async (res: any) => {
        if (!res.code) return reject(new Error("WX_LOGIN_NO_CODE"));
        try {
          const r = await loginByWechat({
            code: res.code,
            deviceId: getDeviceId(),
            deviceType: getDeviceType(),
          });
          resolve(r);
        } catch (e) {
          reject(e);
        }
      },
      fail: (err: any) => reject(err),
    });
  });
  // #endif

  // #ifdef APP-PLUS
  return new Promise((resolve, reject) => {
    uni.login({
      provider: "weixin",
      success: async (res: any) => {
        const code = res.code || res.authResult?.code;
        if (!code) return reject(new Error("WX_LOGIN_NO_CODE"));
        try {
          const r = await loginByWechat({
            code,
            deviceId: getDeviceId(),
            deviceType: getDeviceType(),
          });
          resolve(r);
        } catch (e) {
          reject(e);
        }
      },
      fail: (err: any) => reject(err),
    });
  });
  // #endif

  // #ifdef H5
  const APPID = import.meta.env.VITE_WECHAT_APPID || "";
  if (!APPID || APPID.startsWith("wxXXXX")) {
    return Promise.reject(new Error("请配置 VITE_WECHAT_APPID 后使用微信登录"));
  }
  const redirect = encodeURIComponent(
    `${location.origin}/oauth/wechat/callback`
  );
  const url =
    `https://open.weixin.qq.com/connect/oauth2/authorize` +
    `?appid=${APPID}&redirect_uri=${redirect}` +
    `&response_type=code&scope=snsapi_userinfo&state=login#wechat_redirect`;
  location.href = url;
  return new Promise(() => {
    /* never resolves: page unloads */
  });
  // #endif

  // eslint-disable-next-line no-unreachable
  throw new Error("WECHAT_LOGIN_UNSUPPORTED");
}
