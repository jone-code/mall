import http from '@/api/http'

export interface Review {
  id: number
  orderNo: string
  userId: number
  spuId: number
  skuId?: number
  rating: number
  content: string
  images?: string[]
  userNickname?: string
  status?: string
  createdAt: string
}

export interface ReviewPage {
  list: Review[]
  page: number
  size: number
  total: number
}

export function listReviews(params?: {
  page?: number
  size?: number
  spuId?: number
  rating?: number
  status?: string
  from?: string
  to?: string
}) {
  return http.get<ReviewPage>('/admin/reviews', { params })
}

export function hideReview(id: number) {
  return http.post(`/admin/reviews/${id}/hide`)
}

export function unhideReview(id: number) {
  return http.post(`/admin/reviews/${id}/unhide`)
}

export function getReviewStats() {
  return http.get<{ badReviewCount: number; visibleCount: number }>('/admin/reviews/stats')
}
