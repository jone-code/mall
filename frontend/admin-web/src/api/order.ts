import axios from 'axios'
import http from '@/api/http'
import { getToken } from '@/utils/auth-storage'
import type { PageResult } from '@/api/types'

export interface OrderItem {
  skuId: number
  spuId?: number
  title: string
  specText?: string
  mainImage?: string
  price: number
  quantity: number
  subtotal: number
  productType?: string
}

export interface FulfillmentInfo {
  cardNo?: string
  cardSecret?: string
  verifyCode?: string
}

export interface PaymentInfo {
  payNo?: string
  channel?: string
  status?: string
  amount?: number
  channelTxn?: string
  paidAt?: string
}

export interface OrderLog {
  id: number
  fromStatus?: string
  toStatus: string
  operatorType?: string
  operatorId?: string
  remark?: string
  createdAt: string
}

export interface OrderRow {
  orderNo: string
  userId?: number
  userNickname?: string
  status: string
  totalAmount: number
  freightAmount: number
  payAmount: number
  itemCount: number
  productType?: string
  receiver?: string
  receiverPhone?: string
  addressDetail?: string
  expireAt?: string
  payAt?: string
  shipAt?: string
  trackingNo?: string
  trackingCompany?: string
  completeAt?: string
  fulfillment?: FulfillmentInfo
  payment?: PaymentInfo
  adminRemark?: string
  refundAt?: string
  refundReason?: string
  verifiedAt?: string
  cancelAt?: string
  cancelReason?: string
  remark?: string
  createdAt?: string
  items?: OrderItem[]
  logs?: OrderLog[]
}

export interface BatchShipItem {
  orderNo: string
  trackingNo: string
  trackingCompany?: string
}

export interface BatchShipResult {
  success: number
  failed: number
  errors: string[]
}

export function formatUserLabel(userId?: number, nickname?: string): string {
  if (nickname) return nickname
  if (userId == null) return '—'
  const hex = userId.toString(16)
  const suffix = hex.length >= 6 ? hex.slice(-6) : ('000000' + hex).slice(hex.length)
  return `用户_${suffix}`
}

export function listOrders(params: {
  status?: string
  userId?: number
  orderNo?: string
  phone?: string
  productType?: string
  from?: string
  to?: string
  page?: number
  size?: number
}) {
  return http.get<PageResult<OrderRow>>('/admin/orders', { params })
}

export function getOrder(orderNo: string) {
  return http.get<OrderRow>(`/admin/orders/${orderNo}`)
}

export function closeOrder(orderNo: string) {
  return http.post(`/admin/orders/${orderNo}/close`)
}

export function shipOrder(orderNo: string, data: { trackingNo: string; trackingCompany?: string }) {
  return http.post(`/admin/orders/${orderNo}/ship`, data)
}

export function batchShipOrders(items: BatchShipItem[]) {
  return http.post<BatchShipResult>('/admin/orders/batch-ship', { items })
}

export function updateOrderRemark(orderNo: string, remark: string) {
  return http.put(`/admin/orders/${orderNo}/remark`, { remark })
}

export function approveRefund(orderNo: string) {
  return http.post(`/admin/orders/${orderNo}/refund/approve`)
}

export function rejectRefund(orderNo: string) {
  return http.post(`/admin/orders/${orderNo}/refund/reject`)
}

export function getOrderLogistics(orderNo: string) {
  return http.get<{ trackingNo: string; trackingCompany: string; status: string; events: { time: string; status: string; desc: string }[] }>(
    `/admin/orders/${orderNo}/logistics`
  )
}

export async function exportOrders(params: {
  status?: string
  userId?: number
  orderNo?: string
  phone?: string
  productType?: string
  from?: string
  to?: string
}) {
  const baseURL = import.meta.env.VITE_API_BASE || ''
  const resp = await axios.get(`${baseURL}/admin/orders/export`, {
    params,
    responseType: 'blob',
    headers: { Authorization: `Bearer ${getToken()}` }
  })
  const blob = new Blob([resp.data], { type: 'text/csv;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `orders-${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(a.href)
}

export function orderLogLabel(status?: string) {
  return orderStatusLabel(status)
}

export function orderStatusLabel(status?: string) {
  switch (status) {
    case 'PENDING_PAY':
      return '待支付'
    case 'PAID':
      return '待发货'
    case 'SHIPPED':
      return '待收货'
    case 'COMPLETED':
      return '已完成'
    case 'CANCELLED':
      return '已取消'
    case 'REFUNDING':
      return '退款中'
    case 'REFUNDED':
      return '已退款'
    default:
      return status || ''
  }
}

export function orderStatusTag(status?: string): '' | 'success' | 'warning' | 'info' | 'danger' {
  switch (status) {
    case 'PENDING_PAY':
      return 'warning'
    case 'PAID':
      return 'success'
    case 'SHIPPED':
      return ''
    case 'COMPLETED':
      return 'info'
    case 'CANCELLED':
      return 'danger'
    case 'REFUNDING':
      return 'warning'
    case 'REFUNDED':
      return 'info'
    default:
      return ''
  }
}
