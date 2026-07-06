import http from '@/api/http'

export interface AdminUserRow {
  id: number
  username: string
  realName?: string
  phone?: string
  email?: string
  status?: number
  lastLoginAt?: string
}

export function listAdminUsers(params: { keyword?: string; page?: number; size?: number }) {
  return http.get<AdminUserRow[]>('/admin/users', { params })
}

export function createAdminUser(data: {
  username: string
  password: string
  realName?: string
  phone: string
  email?: string
}) {
  return http.post<number>('/admin/users', data)
}

export function getAdminUserRoles(id: number) {
  return http.get<number[]>(`/admin/users/${id}/roles`)
}

export function assignAdminUserRoles(id: number, roleIds: number[]) {
  return http.put(`/admin/users/${id}/roles`, { roleIds })
}

export function resetAdminUserPassword(id: number, newPassword: string) {
  return http.put(`/admin/users/${id}/password`, { newPassword })
}
