import { http } from "@/utils/request";
import type { PageResult } from "@/api/product";

export interface Review {
  id: number;
  orderNo: string;
  userId: number;
  spuId: number;
  skuId?: number;
  rating: number;
  content: string;
  userNickname?: string;
  createdAt: string;
}

export interface ReviewSummary {
  count: number;
  avgRating: number;
}

export function createReview(data: {
  orderNo: string;
  rating: number;
  content: string;
}) {
  return http.post<Review>("/reviews", data);
}

export function getMyReviews() {
  return http.get<Review[]>("/reviews/me");
}

export function getReviewByOrder(orderNo: string) {
  return http.get<Review | null>(`/reviews/order/${orderNo}`);
}

export function getProductReviews(spuId: number, page = 1, size = 10) {
  return http.get<PageResult<Review>>(`/reviews/spu/${spuId}`, { page, size }, {
    noAuth: true,
  });
}

export function getProductReviewSummary(spuId: number) {
  return http.get<ReviewSummary>(`/reviews/spu/${spuId}/summary`, undefined, {
    noAuth: true,
  });
}
