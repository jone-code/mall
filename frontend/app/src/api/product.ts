import { http } from "@/utils/request";

export interface SpuSummary {
  id: number;
  title: string;
  subtitle?: string;
  mainImage: string;
  productType: string;
  minPrice: number;
  maxPrice?: number;
}

export interface CategoryNode {
  id: number;
  name: string;
  iconUrl?: string;
  children?: CategoryNode[];
}

export interface HomeData {
  categories: CategoryNode[];
  recommendSpus: SpuSummary[];
}

export interface SkuDetail {
  id: number;
  specText: string;
  specJson?: { dims?: { name: string; value: string }[] };
  price: number;
  marketPrice?: number;
  isDefault?: number;
  available: number;
}

export interface SpuDetail {
  id: number;
  title: string;
  subtitle?: string;
  productType: string;
  mainImage: string;
  images: string[];
  detailHtml?: string;
  skus: SkuDetail[];
  poolAvailable?: number;
}

export interface PageResult<T> {
  list: T[];
  page: number;
  size: number;
  total: number;
}

export function getHome() {
  return http.get<HomeData>("/home", undefined, { noAuth: true });
}

export function getCategories() {
  return http.get<CategoryNode[]>("/categories", undefined, { noAuth: true });
}

export function getProducts(params?: {
  categoryId?: number;
  keyword?: string;
  page?: number;
  size?: number;
}) {
  return http.get<PageResult<SpuSummary>>("/products", params, { noAuth: true });
}

export function getProductDetail(id: number) {
  return http.get<SpuDetail>(`/products/${id}`, undefined, { noAuth: true });
}

export function productTypeLabel(type: string) {
  if (type === "VIRTUAL") return "虚拟";
  if (type === "SERVICE") return "服务";
  return "实物";
}

export function formatPrice(min?: number, max?: number) {
  if (min == null) return "—";
  if (max != null && max > min) return `¥${min} - ¥${max}`;
  return `¥${min}起`;
}
