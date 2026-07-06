import http from '@/api/http'

export interface PermissionRow {
  id: number
  code: string
  name: string
  module?: string
}

export interface RoleRow {
  id: number
  code: string
  name: string
  remark?: string
  permissionCount?: number
  permissionIds?: number[]
}

export function listPermissions() {
  return http.get<PermissionRow[]>('/admin/rbac/permissions')
}

export function listRoles() {
  return http.get<RoleRow[]>('/admin/rbac/roles')
}

export function getRole(id: number) {
  return http.get<RoleRow>(`/admin/rbac/roles/${id}`)
}

export function createRole(data: { code: string; name: string; remark?: string }) {
  return http.post<RoleRow>('/admin/rbac/roles', data)
}

export function updateRole(id: number, data: { name: string; remark?: string }) {
  return http.put<RoleRow>(`/admin/rbac/roles/${id}`, data)
}

export function deleteRole(id: number) {
  return http.delete(`/admin/rbac/roles/${id}`)
}

export function assignRolePermissions(id: number, permissionIds: number[]) {
  return http.put(`/admin/rbac/roles/${id}/permissions`, { permissionIds })
}

export function createPermission(data: { code: string; name: string; module?: string }) {
  return http.post<PermissionRow>('/admin/rbac/permissions', data)
}

export function updatePermission(id: number, data: { name: string; module?: string }) {
  return http.put<PermissionRow>(`/admin/rbac/permissions/${id}`, data)
}

export function deletePermission(id: number) {
  return http.delete(`/admin/rbac/permissions/${id}`)
}
