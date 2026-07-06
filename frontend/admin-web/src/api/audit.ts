import axios from 'axios'
import http from '@/api/http'
import { getToken } from '@/utils/auth-storage'

export interface AuditLog {
  id: number
  adminUserId?: number
  username?: string
  action: string
  targetType?: string
  targetId?: string
  requestIp?: string
  userAgent?: string
  requestBody?: string
  result: number
  errorMsg?: string
  createdAt?: string
}

export interface AuditPage {
  list: AuditLog[]
  total: number
  page: number
  size: number
}

export interface AuditQuery {
  action?: string
  adminUserId?: number
  username?: string
  targetId?: string
  result?: number
  from?: string
  to?: string
  page?: number
  size?: number
}

export function listAuditLogs(params: AuditQuery) {
  return http.get<AuditPage>('/admin/audit-logs', { params })
}

export async function exportAuditLogs(params: Omit<AuditQuery, 'page' | 'size'>) {
  const baseURL = import.meta.env.VITE_API_BASE || ''
  const resp = await axios.get(`${baseURL}/admin/audit-logs/export`, {
    params,
    responseType: 'blob',
    headers: { Authorization: `Bearer ${getToken()}` }
  })
  const blob = new Blob([resp.data], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `audit-logs-${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(a.href)
}

export const AUDIT_ACTIONS: { value: string; label: string }[] = [
  { value: 'LOGIN', label: '登录成功' },
  { value: 'LOGIN_FAIL', label: '登录失败' },
  { value: 'LOGOUT', label: '登出' },
  { value: 'TOKEN_REFRESH', label: 'Token 续期' },
  { value: 'CHANGE_PASSWORD', label: '修改密码' },
  { value: 'RESET_ADMIN_PASSWORD', label: '重置管理员密码' },
  { value: 'CREATE_SPU', label: '创建商品' },
  { value: 'UPDATE_SPU', label: '编辑商品' },
  { value: 'SAVE_SKUS', label: '保存SKU' },
  { value: 'DELETE_SKU', label: '删除SKU' },
  { value: 'PUBLISH_SPU', label: '上架商品' },
  { value: 'OFFLINE_SPU', label: '下架商品' },
  { value: 'BATCH_PRODUCT_STATUS', label: '批量上下架' },
  { value: 'ADJUST_STOCK', label: '调整库存' },
  { value: 'CREATE_CATEGORY', label: '创建类目' },
  { value: 'UPDATE_CATEGORY', label: '编辑类目' },
  { value: 'DELETE_CATEGORY', label: '删除类目' },
  { value: 'CLOSE_ORDER', label: '关闭订单' },
  { value: 'SHIP_ORDER', label: '订单发货' },
  { value: 'BATCH_SHIP', label: '批量发货' },
  { value: 'APPROVE_REFUND', label: '同意退款' },
  { value: 'REJECT_REFUND', label: '拒绝退款' },
  { value: 'IMPORT_CARDS', label: '导入卡密' },
  { value: 'UPDATE_CARD', label: '编辑卡密' },
  { value: 'IMPORT_SERVICE_CODES', label: '导入核销码' },
  { value: 'UPDATE_SERVICE_CODE', label: '编辑核销码' },
  { value: 'VERIFY_SERVICE', label: '服务核销' },
  { value: 'UPDATE_MEMBER_STATUS', label: '会员状态变更' },
  { value: 'CREATE_ADMIN_USER', label: '创建管理员' },
  { value: 'ASSIGN_ROLES', label: '分配角色' },
  { value: 'ASSIGN_ROLE_PERMISSIONS', label: '分配权限' },
  { value: 'CREATE_PERMISSION', label: '创建权限点' },
  { value: 'UPDATE_PERMISSION', label: '编辑权限点' },
  { value: 'DELETE_PERMISSION', label: '删除权限点' },
  { value: 'CREATE_ROLE', label: '创建角色' },
  { value: 'UPDATE_ROLE', label: '编辑角色' },
  { value: 'DELETE_ROLE', label: '删除角色' }
]

export function auditActionLabel(action?: string) {
  return AUDIT_ACTIONS.find((a) => a.value === action)?.label || action || ''
}
