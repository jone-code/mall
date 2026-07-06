/**
 * Get a stable client device id (best-effort).
 * For multi-end uni-app: use uni-id from system info, fallback to random+storage.
 */
const KEY = "comonon_device_id";

export function getDeviceId(): string {
  try {
    const cached = uni.getStorageSync(KEY);
    if (cached) return cached;
  } catch (e) {
    // ignore
  }
  let id = "";
  try {
    const sys: any = uni.getSystemInfoSync();
    id = sys.deviceId || sys.uniPlatform || "";
  } catch (e) {
    // ignore
  }
  if (!id) {
    id = "d_" + Math.random().toString(36).slice(2) + Date.now().toString(36);
  }
  try {
    uni.setStorageSync(KEY, id);
  } catch (e) {
    // ignore
  }
  return id;
}

export function getDeviceType(): string {
  // #ifdef APP-PLUS
  // @ts-ignore
  const sys: any = uni.getSystemInfoSync();
  return sys.osName === "ios" || sys.platform === "ios"
    ? "APP_IOS"
    : "APP_ANDROID";
  // #endif
  // #ifdef MP-WEIXIN
  return "MP_WX";
  // #endif
  // #ifdef H5
  return "H5";
  // #endif
  // fallback
  // eslint-disable-next-line no-unreachable
  return "H5";
}
