import http from '@/api/http'

export interface AdminSession {
  sid: string
  createdAt?: number
  lastActiveAt?: number
  current: boolean
}

export function listAdminSessions() {
  return http.get<AdminSession[]>('/admin/me/sessions')
}

export function kickAdminSession(sid: string) {
  return http.delete(`/admin/me/sessions/${sid}`)
}
