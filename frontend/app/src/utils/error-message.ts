import type { ApiResult } from "@/utils/request";

const ERROR_MESSAGES: Record<number, string> = {
  40000: "请求参数有误",
  40100: "请先登录",
  40101: "验证码错误",
  40102: "验证码已过期",
  40103: "发送过于频繁，请稍后再试",
  40104: "今日短信发送已达上限",
  40110: "登录已过期，请重新登录",
  40111: "登录状态无效，请重新登录",
  40301: "账号已被禁用",
  40302: "账号已注销",
  40401: "商品不存在",
  40402: "规格不存在",
  40411: "地址不存在",
  40424: "购物车商品不存在",
  40431: "订单不存在",
  40432: "支付单不存在",
  40440: "评价不存在",
  40904: "库存不足",
  40921: "商品已下架或不可购买",
  40922: "请先勾选要结算的商品",
  40923: "购物车商品库存不足",
  40924: "不同类型商品请分开下单",
  40925: "虚拟/服务类商品每单限购 1 件",
  40930: "当前订单状态不允许此操作",
  40931: "购物车为空，无法下单",
  40932: "支付状态异常",
  40933: "虚拟商品库存不足",
  40940: "当前订单不可评价",
  40941: "该订单已评价",
};

export function resolveErrorMessage(err: unknown, fallback = "操作失败"): string {
  const payload = (err as { payload?: ApiResult })?.payload;
  if (payload?.message) return payload.message;
  if (payload?.code && ERROR_MESSAGES[payload.code]) {
    return ERROR_MESSAGES[payload.code];
  }
  const msg = (err as Error)?.message;
  if (msg && msg !== "BIZ_ERROR" && !msg.startsWith("请求失败")) return msg;
  return fallback;
}

export function showError(err: unknown, fallback?: string) {
  uni.showToast({
    title: resolveErrorMessage(err, fallback),
    icon: "none",
  });
}
