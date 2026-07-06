import http from '@/api/http'

export interface OrderStats {
  totalOrders: number
  pendingPay: number
  paid: number
  shipped: number
  completed: number
  cancelled: number
  refunding?: number
  refunded?: number
  todayOrders: number
  todayGmv: number
  totalGmv: number
}

export interface OrderTrendPoint {
  date: string
  orderCount: number
  gmv: number
}

export interface OrderTrend {
  days: number
  points: OrderTrendPoint[]
}

export interface MemberStats {
  todayNewUsers: number
}

export interface ProductStats {
  total: number
  onSale: number
  offSale: number
  draft: number
  lowStock?: number
}

export interface OpsTodo {
  pendingPay: number
  pendingShip: number
  refunding: number
  virtualCardAvailable: number
  virtualPoolEmpty: number
  servicePoolEmpty: number
}

export function getOrderStats() {
  return http.get<OrderStats>('/admin/orders/stats')
}

export function getOrderTrends(days = 7) {
  return http.get<OrderTrend>('/admin/orders/trends', { params: { days } })
}

export function getProductStats() {
  return http.get<ProductStats>('/admin/products/stats')
}

export function getMemberStats() {
  return http.get<MemberStats>('/admin/members/stats')
}

export function getOpsTodos() {
  return http.get<OpsTodo>('/admin/ops/todos')
}
