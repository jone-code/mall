import { defineStore } from "pinia";

export interface UserInfo {
  id?: number | string;
  nickname?: string;
  avatarUrl?: string;
  phone?: string;
}

interface UserState {
  accessToken: string;
  refreshToken: string;
  /** 会话 id，refresh 时必传 */
  sid: string;
  /** access token 过期时间戳（毫秒） */
  accessExpireAt: number;
  user: UserInfo | null;
}

const STORAGE_KEY = "comonon_auth";
const DEFAULT_ACCESS_TTL_SEC = 2 * 60 * 60;

function loadFromStorage(): Partial<UserState> {
  try {
    const raw = uni.getStorageSync(STORAGE_KEY);
    if (raw) {
      return typeof raw === "string" ? JSON.parse(raw) : raw;
    }
  } catch (e) {
    // ignore
  }
  return {};
}

function resolveAccessExpireAt(payload: {
  accessExp?: number;
  expiresIn?: number;
}): number {
  if (payload.accessExp != null && payload.accessExp > 0) {
    return payload.accessExp * 1000;
  }
  const ttl = payload.expiresIn ?? DEFAULT_ACCESS_TTL_SEC;
  return Date.now() + ttl * 1000;
}

export const useUserStore = defineStore("user", {
  state: (): UserState => {
    const persisted = loadFromStorage();
    return {
      accessToken: persisted.accessToken || "",
      refreshToken: persisted.refreshToken || "",
      sid: persisted.sid || "",
      accessExpireAt: persisted.accessExpireAt || 0,
      user: persisted.user || null,
    };
  },
  getters: {
    isLogin(state): boolean {
      return !!state.accessToken;
    },
    accessExpiringSoon(state): boolean {
      if (!state.accessExpireAt) return false;
      return state.accessExpireAt - Date.now() < 5 * 60 * 1000;
    },
  },
  actions: {
    setTokens(payload: {
      accessToken: string;
      refreshToken: string;
      sid?: string;
      accessExp?: number;
      expiresIn?: number;
    }) {
      this.accessToken = payload.accessToken;
      this.refreshToken = payload.refreshToken;
      if (payload.sid) {
        this.sid = payload.sid;
      }
      this.accessExpireAt = resolveAccessExpireAt(payload);
      this.persist();
    },
    setUser(user: UserInfo | null) {
      this.user = user;
      this.persist();
    },
    clear() {
      this.accessToken = "";
      this.refreshToken = "";
      this.sid = "";
      this.accessExpireAt = 0;
      this.user = null;
      try {
        uni.removeStorageSync(STORAGE_KEY);
      } catch (e) {
        // ignore
      }
    },
    persist() {
      try {
        uni.setStorageSync(
          STORAGE_KEY,
          JSON.stringify({
            accessToken: this.accessToken,
            refreshToken: this.refreshToken,
            sid: this.sid,
            accessExpireAt: this.accessExpireAt,
            user: this.user,
          })
        );
      } catch (e) {
        // ignore
      }
    },
  },
});
