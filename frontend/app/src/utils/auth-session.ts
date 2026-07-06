/** 与 user-service LoginVO / TokenVO 对齐 */
export interface AuthTokenPayload {
  accessToken: string;
  refreshToken: string;
  sid?: string;
  /** Unix 秒，JWT 自然过期时间 */
  accessExp?: number;
}

export interface AuthSessionStore {
  setTokens(payload: AuthTokenPayload & { expiresIn?: number }): void;
}

/** 登录或 refresh 成功后写入 store */
export function applyAuthTokens(
  store: AuthSessionStore,
  payload: AuthTokenPayload
): void {
  store.setTokens({
    accessToken: payload.accessToken,
    refreshToken: payload.refreshToken,
    sid: payload.sid,
    accessExp: payload.accessExp,
  });
}
