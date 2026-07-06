import http from '@/api/http'
import type { PageResult } from '@/api/types'

export interface VirtualCard {
  id: number
  spuId?: number
  cardNo: string
  cardSecret: string
  status: string
  orderNo?: string
  issuedAt?: string
  createdAt?: string
}

export interface VirtualCardPool {
  spuId: number
  available: number
  issued: number
  total: number
}

export interface ImportCardsResult {
  imported: number
  duplicate: number
  skipped: number
  errors: string[]
}

export function listVirtualCards(params?: {
  status?: string
  spuId?: number
  page?: number
  size?: number
}) {
  return http.get<PageResult<VirtualCard>>('/admin/virtual-cards', { params })
}

export function getVirtualCardStats(spuId?: number) {
  return http.get<{ available: number }>('/admin/virtual-cards/stats', {
    params: spuId ? { spuId } : undefined
  })
}

export function getVirtualCardPoolSummary() {
  return http.get<VirtualCardPool[]>('/admin/virtual-cards/pool-summary')
}

export function importVirtualCards(data: {
  spuId: number
  cards: { cardNo: string; cardSecret?: string }[]
}) {
  return http.post<ImportCardsResult>('/admin/virtual-cards/import', data)
}

export function updateVirtualCard(
  id: number,
  data: { spuId: number; cardNo: string; cardSecret?: string }
) {
  return http.put(`/admin/virtual-cards/${id}`, data)
}

export function maskSecret(secret?: string) {
  if (!secret) return '—'
  if (secret.length <= 4) return '****'
  return secret.slice(0, 2) + '****' + secret.slice(-2)
}
