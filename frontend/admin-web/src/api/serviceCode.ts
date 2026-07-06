import http from '@/api/http'
import type { PageResult } from '@/api/types'

export interface ServiceVerifyCode {
  id: number
  spuId: number
  verifyCode: string
  status: string
  orderNo?: string
  issuedAt?: string
  createdAt?: string
}

export function listServiceVerifyCodes(params?: {
  status?: string
  spuId?: number
  page?: number
  size?: number
}) {
  return http.get<PageResult<ServiceVerifyCode>>('/admin/service-verify-codes', { params })
}

export function getServiceVerifyCodeStats(spuId?: number) {
  return http.get<{ available: number }>('/admin/service-verify-codes/stats', {
    params: spuId ? { spuId } : undefined
  })
}

export interface ServiceVerifyPool {
  spuId: number
  available: number
  issued: number
  total: number
}

export interface ImportCodesResult {
  imported: number
  duplicate: number
  skipped: number
}

export function getServiceVerifyCodePoolSummary() {
  return http.get<ServiceVerifyPool[]>('/admin/service-verify-codes/pool-summary')
}

export function importServiceVerifyCodes(data: {
  spuId: number
  codes: { verifyCode?: string }[]
}) {
  return http.post<ImportCodesResult>('/admin/service-verify-codes/import', data)
}

export function updateServiceVerifyCode(
  id: number,
  data: { spuId: number; verifyCode: string }
) {
  return http.put(`/admin/service-verify-codes/${id}`, data)
}

export function verifyServiceCode(verifyCode: string) {
  return http.post('/admin/service/verify', { verifyCode })
}
