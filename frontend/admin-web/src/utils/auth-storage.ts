const TOKEN_KEY = 'admin_access_token'
const REFRESH_KEY = 'admin_refresh_token'
const REMEMBER_KEY = 'admin_remember_me'

export function isRememberMe(): boolean {
  return localStorage.getItem(REMEMBER_KEY) === '1'
}

export function setRememberMeFlag(v: boolean) {
  if (v) localStorage.setItem(REMEMBER_KEY, '1')
  else localStorage.removeItem(REMEMBER_KEY)
}

function activeStorage(): Storage {
  return isRememberMe() ? localStorage : sessionStorage
}

export function getToken(): string {
  return activeStorage().getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY) || localStorage.getItem(TOKEN_KEY) || ''
}

export function getRefreshToken(): string {
  return activeStorage().getItem(REFRESH_KEY) || sessionStorage.getItem(REFRESH_KEY) || localStorage.getItem(REFRESH_KEY) || ''
}

export function setToken(token: string) {
  const store = activeStorage()
  store.setItem(TOKEN_KEY, token)
  const other = store === localStorage ? sessionStorage : localStorage
  other.removeItem(TOKEN_KEY)
}

export function setRefreshToken(token: string) {
  const store = activeStorage()
  if (token) store.setItem(REFRESH_KEY, token)
  else store.removeItem(REFRESH_KEY)
  const other = store === localStorage ? sessionStorage : localStorage
  other.removeItem(REFRESH_KEY)
}

export function clearAuthStorage() {
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(REFRESH_KEY)
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
}

export function migrateAuthStorage(remember: boolean) {
  setRememberMeFlag(remember)
  const access = sessionStorage.getItem(TOKEN_KEY) || localStorage.getItem(TOKEN_KEY) || ''
  const refresh = sessionStorage.getItem(REFRESH_KEY) || localStorage.getItem(REFRESH_KEY) || ''
  sessionStorage.removeItem(TOKEN_KEY)
  sessionStorage.removeItem(REFRESH_KEY)
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
  const target = remember ? localStorage : sessionStorage
  if (access) target.setItem(TOKEN_KEY, access)
  if (refresh) target.setItem(REFRESH_KEY, refresh)
}
