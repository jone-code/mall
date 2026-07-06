import { http } from "@/utils/request";
import type { PageResult, SpuSummary } from "@/api/product";

export function searchProducts(params?: {
  keyword?: string;
  categoryId?: number;
  page?: number;
  size?: number;
}) {
  return http.get<PageResult<SpuSummary>>("/search/products", params, {
    noAuth: true,
  });
}
