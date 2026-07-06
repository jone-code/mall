import http from '@/api/http'
import type { PageResult } from '@/api/types'
import { orderStatusLabel } from '@/api/order'

export interface MemberRow {
  id: number
  nickname?: string
  phone?: string
  status?: number
  createdAt?: string
}

export interface MemberAddress {
  id: number
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault?: number
}

export interface MemberOrderBrief {
  orderNo: string
  status: string
  payAmount?: number
  createdAt?: string
}

export interface MemberDetail extends MemberRow {
  avatarUrl?: string
  addresses?: MemberAddress[]
  recentOrders?: MemberOrderBrief[]
}

export function listMembers(params: {
  keyword?: string
  status?: number
  page?: number
  size?: number
}) {
  return http.get<PageResult<MemberRow>>('/admin/members', { params })
}

export function getMemberDetail(id: number) {
  return http.get<MemberDetail>(`/admin/members/${id}`)
}

export function updateMemberStatus(id: number, status: number) {
  return http.put(`/admin/members/${id}/status`, { status })
}

export function memberStatusLabel(status?: number) {
  if (status === 1) return '禁用'
  if (status === 2) return '注销'
  return '正常'
}

export function formatOrderStatus(status?: string) {
  return orderStatusLabel(status)
}
