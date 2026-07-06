import { defineStore } from 'pinia'
import http, { trySilentRefresh } from '@/api/http'
import {
  clearAuthStorage,
  getRefreshToken,
  getToken,
  migrateAuthStorage,
  setRefreshToken,
  setToken
} from '@/utils/auth-storage'

export interface AdminProfile {
  id: number
  username: string
  realName?: string
  phone?: string
  email?: string
}

interface LoginStep1Resp {
  challengeToken: string
  phoneMasked: string
}

interface LoginStep2Resp {
  accessToken: string
  refreshToken?: string
  adminUserId?: number
  username?: string
  permissions?: string[]
  roles?: string[]
  profile?: AdminProfile
  permsVersion?: number
}

interface MeResp {
  id: number
  username: string
  realName?: string
  phone?: string
  email?: string
  permissions?: string[]
  roles?: string[]
  profile?: AdminProfile
  permsVersion?: number
}

function toProfile(data: {
  id?: number
  adminUserId?: number
  username?: string
  realName?: string
  phone?: string
  email?: string
}): AdminProfile | null {
  const id = data.id ?? data.adminUserId
  if (id == null || !data.username) return null
  return {
    id,
    username: data.username,
    realName: data.realName,
    phone: data.phone,
    email: data.email
  }
}

export const useAdminStore = defineStore('admin', {
  state: () => ({
    accessToken: getToken(),
    refreshToken: getRefreshToken(),
    profile: null as AdminProfile | null,
    permissions: [] as string[],
    roles: [] as string[],
    permsVersion: 0,
    challengeToken: '' as string,
    phoneMasked: '' as string,
    rememberMe: false
  }),
  getters: {
    isLoggedIn: (s) => !!s.accessToken,
    hasPermission: (s) => (code: string) => s.permissions.includes(code),
    isSuperAdmin: (s) => s.roles.includes('SUPER_ADMIN')
  },
  actions: {
    syncTokensFromStorage() {
      this.accessToken = getToken()
      this.refreshToken = getRefreshToken()
    },
    setAccessToken(token: string) {
      this.accessToken = token
      setToken(token)
    },
    setRefreshToken(token: string) {
      this.refreshToken = token
      setRefreshToken(token)
    },
    clearAuth() {
      this.accessToken = ''
      this.refreshToken = ''
      clearAuthStorage()
      this.profile = null
      this.permissions = []
      this.roles = []
      this.permsVersion = 0
      this.challengeToken = ''
      this.phoneMasked = ''
    },
    async tryRestoreSession() {
      if (this.isLoggedIn) return true
      const ok = await trySilentRefresh()
      if (!ok) return false
      this.syncTokensFromStorage()
      try {
        await this.fetchMe()
        return true
      } catch {
        this.clearAuth()
        return false
      }
    },
    async loginStep1(payload: {
      username: string
      password: string
      captcha: string
      captchaId: string
      rememberMe?: boolean
    }) {
      const { data } = await http.post<LoginStep1Resp>('/admin/login/password', {
        username: payload.username,
        password: payload.password,
        captcha: payload.captcha,
        captchaId: payload.captchaId
      })
      this.challengeToken = data.challengeToken
      this.phoneMasked = data.phoneMasked
      this.rememberMe = !!payload.rememberMe
      return data
    },
    async loginStep2(smsCode: string) {
      const { data } = await http.post<LoginStep2Resp>('/admin/login/verify', {
        challengeToken: this.challengeToken,
        smsCode,
        rememberMe: this.rememberMe
      })
      migrateAuthStorage(this.rememberMe)
      this.setAccessToken(data.accessToken)
      if (data.refreshToken) this.setRefreshToken(data.refreshToken)
      this.profile = data.profile ?? toProfile(data)
      this.permissions = data.permissions || []
      this.roles = data.roles || []
      this.permsVersion = data.permsVersion || 0
      this.challengeToken = ''
      return data
    },
    async fetchMe() {
      const { data } = await http.get<MeResp>('/admin/me')
      this.profile = data.profile ?? toProfile(data)
      this.permissions = data.permissions || []
      this.roles = data.roles || []
      this.permsVersion = data.permsVersion || 0
      return data
    },
    async logout() {
      try {
        await http.post('/admin/logout')
      } catch (_) {
        /* ignore */
      } finally {
        this.clearAuth()
      }
    }
  }
})
