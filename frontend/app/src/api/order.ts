import { http } from "@/utils/request";
import type { PageResult } from "@/api/product";

export interface OrderItem {
  skuId: number;
  spuId?: number;
  title: string;
  specText?: string;
  mainImage?: string;
  price: number;
  quantity: number;
  subtotal: number;
  productType?: string;
}

export interface FulfillmentInfo {
  cardNo?: string;
  cardSecret?: string;
  verifyCode?: string;
}

export interface OrderDetail {
  orderNo: string;
  status: string;
  totalAmount: number;
  freightAmount: number;
  payAmount: number;
  itemCount: number;
  productType?: string;
  receiver?: string;
  receiverPhone?: string;
  addressDetail?: string;
  expireAt?: string;
  payAt?: string;
  shipAt?: string;
  trackingNo?: string;
  trackingCompany?: string;
  completeAt?: string;
  fulfillment?: FulfillmentInfo;
  cancelAt?: string;
  cancelReason?: string;
  remark?: string;
  createdAt?: string;
  items: OrderItem[];
}

export interface CreateOrderResult {
  orderNo: string;
  payAmount: number;
  expireAt: string;
}

export interface CreatePayResult {
  payNo: string;
  orderNo: string;
  channel: string;
  amount: number;
  status: string;
  mockConfirmUrl: string;
}

export function createOrder(addressId: number, remark?: string) {
  return http.post<CreateOrderResult>("/orders", { addressId, remark });
}

export function listOrders(status?: string) {
  return http.get<OrderDetail[]>("/orders", status ? { status } : undefined);
}

export function listOrdersPage(status?: string, page = 1, size = 10) {
  const params: Record<string, string | number> = { page, size };
  if (status) params.status = status;
  return http.get<PageResult<OrderDetail>>("/orders", params);
}

export function getOrder(orderNo: string) {
  return http.get<OrderDetail>(`/orders/${orderNo}`);
}

export function cancelOrder(orderNo: string) {
  return http.post<void>(`/orders/${orderNo}/cancel`);
}

export function confirmReceive(orderNo: string) {
  return http.post<void>(`/orders/${orderNo}/confirm`);
}

export function applyRefund(orderNo: string, reason: string) {
  return http.post<void>(`/orders/${orderNo}/refund`, { reason });
}

export interface LogisticsInfo {
  trackingNo: string;
  trackingCompany: string;
  status: string;
  events: { time: string; status: string; desc: string }[];
}

export function getOrderLogistics(orderNo: string) {
  return http.get<LogisticsInfo>(`/orders/${orderNo}/logistics`);
}

export function createPay(orderNo: string) {
  return http.post<CreatePayResult>(`/orders/${orderNo}/pay`);
}

export function confirmPay(payNo: string) {
  return http.post<{ status: string }>(`/pay/${payNo}/confirm`);
}

export function getPay(payNo: string) {
  return http.get<{ payNo: string; orderNo: string; status: string; amount: number }>(
    `/pay/${payNo}`
  );
}

export function orderStatusLabel(status?: string) {
  switch (status) {
    case "PENDING_PAY":
      return "待支付";
    case "PAID":
      return "待发货";
    case "SHIPPED":
      return "待收货";
    case "COMPLETED":
      return "已完成";
    case "CANCELLED":
      return "已取消";
    case "REFUNDING":
      return "退款中";
    case "REFUNDED":
      return "已退款";
    default:
      return status || "";
  }
}
