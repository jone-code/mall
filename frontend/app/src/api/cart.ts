import { http } from "@/utils/request";

export interface CartItem {
  skuId: number;
  spuId?: number;
  title?: string;
  specText?: string;
  mainImage?: string;
  price?: number;
  quantity: number;
  selected?: boolean;
  available?: number;
  sellable?: boolean;
  invalid?: boolean;
  invalidReason?: string;
  stockInsufficient?: boolean;
  productType?: string;
}

export interface CartSummary {
  totalQuantity: number;
  selectedQuantity: number;
  selectedAmount: number;
  invalidCount: number;
}

export interface CartData {
  items: CartItem[];
  summary: CartSummary;
}

export function getCart() {
  return http.get<CartData>("/cart");
}

export function addCartItem(skuId: number, quantity = 1) {
  return http.post<{ cartItemCount: number; skuQuantity: number }>("/cart/items", {
    skuId,
    quantity,
  });
}

export function updateCartItem(
  skuId: number,
  payload: { quantity?: number; selected?: boolean }
) {
  return http.put<void>(`/cart/items/${skuId}`, payload);
}

export function selectAllCart(selected: boolean) {
  return http.put<void>("/cart/select-all", { selected });
}

export function deleteCartItem(skuId: number) {
  return http.del<void>(`/cart/items/${skuId}`);
}

export function deleteInvalidCartItems() {
  return http.del<{ removed: number }>("/cart/invalid");
}

export function checkoutPreview() {
  return http.get<{
    items: CartItem[];
    totalAmount: number;
    itemCount: number;
    skuLineCount: number;
  }>("/cart/checkout-preview");
}

export function invalidReasonLabel(reason?: string) {
  if (reason === "OFF_SALE") return "商品已下架";
  if (reason === "SKU_DISABLED") return "规格已禁用";
  if (reason === "OUT_OF_STOCK") return "无货";
  if (reason === "SKU_NOT_FOUND") return "商品不存在";
  return "已失效";
}
