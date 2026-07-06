import axios, { AxiosError, AxiosRequestConfig, InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  clearAuthStorage,
  getRefreshToken,
  getToken,
  setRefreshToken,
  setToken
} from '@/utils/auth-storage'

const baseURL = import.meta.env.VITE_API_BASE || ''

interface ApiResult<T = unknown> {
  code: number
  message?: string
  data: T
}

function unwrapResult<T>(body: unknown): T {
  if (body && typeof body === 'object' && 'code' in body && 'data' in body) {
    const result = body as ApiResult<T>
    if (result.code !== 0) {
      throw new Error(result.message || '请求失败')
    }
    return result.data
  }
  return body as T
}

const http = axios.create({
  baseURL,
  timeout: 15000,
  withCredentials: true
})

function gotoLogin() {
  clearAuthStorage()
  if (location.pathname !== '/login') {
    location.href = '/login'
  }
}

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const t = getToken()
  if (t) {
    config.headers = config.headers ?? ({} as any)
    ;(config.headers as any).Authorization = `Bearer ${t}`
  }
  return config
})

interface RetryConfig extends AxiosRequestConfig {
  _retried?: boolean
}

let refreshPromise: Promise<string> | null = null

async function doRefresh(): Promise<string> {
  if (refreshPromise) return refreshPromise
  refreshPromise = http
    .post<{ accessToken?: string; refreshToken?: string }>('/admin/token/refresh', {
      accessToken: getToken(),
      refreshToken: getRefreshToken()
    })
    .then((res) => {
      const newToken = res.data?.accessToken || ''
      if (!newToken) throw new Error('no token')
      setToken(newToken)
      if (res.data?.refreshToken) setRefreshToken(res.data.refreshToken)
      return newToken
    })
    .finally(() => {
      setTimeout(() => {
        refreshPromise = null
      }, 0)
    })
  return refreshPromise
}

export async function trySilentRefresh(): Promise<boolean> {
  if (!getRefreshToken()) return false
  try {
    await doRefresh()
    return !!getToken()
  } catch {
    return false
  }
}

http.interceptors.response.use(
  (resp) => {
    resp.data = unwrapResult(resp.data)
    return resp
  },
  async (error: AxiosError<any>) => {
    const status = error.response?.status
    const original = (error.config || {}) as RetryConfig
    const data: any = error.response?.data || {}

    if (status === 401 && !original._retried) {
      original._retried = true
      try {
        const newToken = await doRefresh()
        original.headers = original.headers ?? ({} as any)
        ;(original.headers as any).Authorization = `Bearer ${newToken}`
        return http.request(original)
      } catch (_) {
        gotoLogin()
        return Promise.reject(error)
      }
    }

    if (status === 403) {
      if (data?.reason === 'KICKED' || data?.data?.reason === 'KICKED') {
        try {
          await ElMessageBox.alert('您的账号已在其他设备登录', '提示', {
            type: 'warning',
            confirmButtonText: '重新登录'
          })
        } catch (_) {
          /* ignore */
        }
        gotoLogin()
        return Promise.reject(error)
      }
      const code = data?.code
      if (code === 40330) {
        ElMessage.error(data?.message || '无权限执行此操作')
      } else if (code === 40301) {
        ElMessage.error(data?.message || '账号已禁用')
      } else {
        ElMessage.error(data?.message || '无权限')
      }
      return Promise.reject(error)
    }

    const message = data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

export default http
