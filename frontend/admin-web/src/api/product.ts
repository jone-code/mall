import http from '@/api/http'
import type { PageResult } from '@/api/types'

export interface CategoryNode {
  id: number
  parentId?: number
  name: string
  iconUrl?: string
  sortOrder?: number
  status?: number
  level?: number
  children?: CategoryNode[]
}

export interface SpecDim {
  name: string
  value: string
}

export interface AdminSku {
  id?: number
  spuId?: number
  skuCode?: string
  specJson?: { dims?: SpecDim[] }
  specText?: string
  price?: number
  marketPrice?: number
  isDefault?: number
  status?: number
  available?: number
}

export interface AdminSpu {
  id: number
  categoryId?: number
  title?: string
  subtitle?: string
  productType?: string
  mainImage?: string
  images?: string[]
  detailHtml?: string
  status?: number
  sortOrder?: number
  minPrice?: number
  maxPrice?: number
  skus?: AdminSku[]
}

export interface SkuSavePayload {
  id?: number
  skuCode?: string
  specJson: { dims: SpecDim[] }
  price: number
  marketPrice?: number
  isDefault?: number
  status?: number
  available?: number
}

export async function listCategories() {
  const { data } = await http.get<CategoryNode[]>('/admin/categories')
  return data
}

export async function createCategory(payload: {
  parentId?: number
  name: string
  iconUrl?: string
  sortOrder?: number
}) {
  const { data } = await http.post<{ id: number }>('/admin/categories', payload)
  return data
}

export function updateCategory(
  id: number,
  payload: { name?: string; iconUrl?: string; sortOrder?: number; status?: number }
) {
  return http.put(`/admin/categories/${id}`, payload)
}

export function deleteCategory(id: number) {
  return http.delete(`/admin/categories/${id}`)
}

export async function listProducts(params: {
  keyword?: string
  productType?: string
  status?: number
  categoryId?: number
  page?: number
  size?: number
}) {
  const { data } = await http.get<PageResult<AdminSpu>>('/admin/products', { params })
  return data
}

export async function getProduct(id: number) {
  const { data } = await http.get<AdminSpu>(`/admin/products/${id}`)
  return data
}

export function updateProduct(
  id: number,
  payload: {
    categoryId?: number
    title?: string
    subtitle?: string
    mainImage?: string
    images?: string[]
    detailHtml?: string
    sortOrder?: number
  }
) {
  return http.put(`/admin/products/${id}`, payload)
}

export function saveSkus(spuId: number, skus: SkuSavePayload[]) {
  return http.put(`/admin/products/${spuId}/skus`, { skus })
}

export function deleteSku(skuId: number) {
  return http.delete(`/admin/skus/${skuId}`)
}

/** 扁平化二级类目，供下拉选择 */
export function flattenLevel2Categories(tree: CategoryNode[]) {
  const result: { id: number; label: string }[] = []
  for (const root of tree) {
    for (const child of root.children || []) {
      result.push({ id: child.id, label: `${root.name} / ${child.name}` })
    }
  }
  return result
}

export function productTypeLabel(t?: string) {
  if (t === 'VIRTUAL') return '虚拟'
  if (t === 'SERVICE') return '服务'
  return '实物'
}

export function spuStatusLabel(s?: number) {
  if (s === 1) return '上架'
  if (s === 2) return '下架'
  return '草稿'
}

export interface StockFlowRow {
  id: number
  skuId: number
  orderNo?: string
  changeType: string
  deltaAvailable?: number
  deltaFrozen?: number
  availableAfter?: number
  frozenAfter?: number
  operatorType?: string
  operatorId?: string
  remark?: string
  createdAt?: string
}

export function listStockFlows(skuId: number, params?: { page?: number; size?: number }) {
  return http.get<PageResult<StockFlowRow>>(`/admin/skus/${skuId}/stock-flows`, { params })
}

export function copyProduct(id: number) {
  return http.post<{ spuId: number; skuId: number }>(`/admin/products/${id}/copy`)
}
