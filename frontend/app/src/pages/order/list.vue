<template>
  <view class="page">
    <view class="tabs">
      <text
        v-for="t in tabs"
        :key="t.value"
        class="tab"
        :class="{ active: status === t.value }"
        @click="switchTab(t.value)"
      >
        {{ t.label }}
      </text>
    </view>

    <OrderSkeleton v-if="loading && !orders.length" />

    <view v-else-if="!orders.length" class="center">
      <EmptyState text="暂无订单" icon="📋">
        <button class="btn outline" @click="goHome">去逛逛</button>
      </EmptyState>
    </view>

    <view v-else class="list">
      <view v-for="o in orders" :key="o.orderNo" class="order" @click="goDetail(o.orderNo)">
        <view class="order-head">
          <view class="order-shop">
            <text class="shop-icon">🏪</text>
            <text class="shop-name">ComonOn 自营</text>
          </view>
          <view class="head-right">
            <text v-if="o.status === 'PENDING_PAY' && orderCountdown(o)" class="cd">
              {{ orderCountdown(o) }}
            </text>
            <text class="status" :class="o.status">{{ orderStatusLabel(o.status) }}</text>
          </view>
        </view>
        <view v-for="item in o.items" :key="item.skuId" class="row">
          <image class="thumb" :src="mediaUrl(item.mainImage)" mode="aspectFill" />
          <view class="info">
            <text class="title">{{ item.title }}</text>
            <text class="spec">{{ item.specText }}</text>
          </view>
          <view class="right">
            <text class="price">¥{{ item.price }}</text>
            <text class="qty">x{{ item.quantity }}</text>
          </view>
        </view>
        <view class="order-foot">
          <view class="foot-left">
            <view class="no-row">
              <text class="no">{{ o.orderNo }}</text>
              <text class="copy-btn" @click.stop="copyOrderNo(o.orderNo)">复制</text>
            </view>
            <text class="pay">共{{ o.itemCount }}件 应付 ¥{{ Number(o.payAmount).toFixed(2) }}</text>
            <text class="time"><RelativeTime :value="o.createdAt" /></text>
          </view>
          <view class="actions" @click.stop>
            <button
              v-if="o.status === 'PENDING_PAY'"
              class="act outline"
              @click="onCancel(o.orderNo)"
            >
              取消
            </button>
            <button
              v-if="o.status === 'PENDING_PAY'"
              class="act"
              @click="goPay(o.orderNo, o.payAmount)"
            >
              去支付
            </button>
            <button
              v-if="o.status === 'SHIPPED'"
              class="act"
              @click="onConfirm(o.orderNo)"
            >
              确认收货
            </button>
            <button
              v-if="o.status === 'COMPLETED'"
              class="act outline"
              @click="onReviewAction(o.orderNo)"
            >
              {{ isReviewed(o.orderNo) ? '查看评价' : '去评价' }}
            </button>
          </view>
        </view>
      </view>
      <view v-if="loadingMore" class="more-hint">加载中…</view>
      <view v-else-if="!hasMore && orders.length" class="more-hint muted">没有更多了</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onLoad, onPullDownRefresh, onReachBottom, onShow } from "@dcloudio/uni-app";
import { ref } from "vue";
import {
  listOrdersPage,
  cancelOrder,
  confirmReceive,
  orderStatusLabel,
  type OrderDetail,
} from "@/api/order";
import { getMyReviews } from "@/api/review";
import EmptyState from "@/components/EmptyState.vue";
import OrderSkeleton from "@/components/OrderSkeleton.vue";
import RelativeTime from "@/components/RelativeTime.vue";
import { formatCountdown } from "@/utils/datetime";
import { copyToClipboard } from "@/utils/format";
import { resolveMediaUrl } from "@/utils/media";
import { showError } from "@/utils/error-message";

const tabs = [
  { label: "全部", value: "" },
  { label: "待支付", value: "PENDING_PAY" },
  { label: "待发货", value: "PAID" },
  { label: "待收货", value: "SHIPPED" },
  { label: "已完成", value: "COMPLETED" },
  { label: "退款中", value: "REFUNDING" },
];

const PAGE_SIZE = 10;

const status = ref("");
const orders = ref<OrderDetail[]>([]);
const loading = ref(false);
const loadingMore = ref(false);
const page = ref(1);
const hasMore = ref(true);
const reviewedOrderNos = ref<Set<string>>(new Set());
let countdownTimer: ReturnType<typeof setInterval> | null = null;
const tick = ref(0);

function mediaUrl(url?: string) {
  return resolveMediaUrl(url);
}

function orderCountdown(o: OrderDetail) {
  tick.value;
  if (o.status !== "PENDING_PAY" || !o.expireAt) return "";
  return formatCountdown(o.expireAt) || "";
}

function startCountdownTicker() {
  stopCountdownTicker();
  countdownTimer = setInterval(() => {
    tick.value++;
  }, 1000);
}

function stopCountdownTicker() {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
}

onLoad((query: any) => {
  const s = query?.status;
  if (typeof s === "string") status.value = s;
});

function isReviewed(orderNo: string) {
  return reviewedOrderNos.value.has(orderNo);
}

async function loadReviewedOrders() {
  try {
    const res = await getMyReviews();
    reviewedOrderNos.value = new Set((res.data || []).map((r) => r.orderNo));
  } catch {
    reviewedOrderNos.value = new Set();
  }
}

async function load(reset = false) {
  if (reset) {
    page.value = 1;
    hasMore.value = true;
    if (!orders.value.length) loading.value = true;
  } else {
    if (!hasMore.value || loadingMore.value) return;
    loadingMore.value = true;
  }

  try {
    if (reset) {
      await loadReviewedOrders();
    }
    const res = await listOrdersPage(status.value || undefined, page.value, PAGE_SIZE);
    const list = res.data?.list || [];
    const total = res.data?.total ?? list.length;
    if (reset) {
      orders.value = list;
    } else {
      orders.value = [...orders.value, ...list];
    }
    hasMore.value = orders.value.length < total;
    if (hasMore.value) page.value += 1;
  } catch (e) {
    showError(e, "加载失败");
  } finally {
    loading.value = false;
    loadingMore.value = false;
    uni.stopPullDownRefresh();
  }
}

function switchTab(v: string) {
  status.value = v;
  orders.value = [];
  load(true);
}

function goDetail(orderNo: string) {
  uni.navigateTo({ url: `/pages/order/detail?orderNo=${orderNo}` });
}

function goPay(orderNo: string, amount: number) {
  uni.navigateTo({ url: `/pages/order/pay?orderNo=${orderNo}&amount=${amount}` });
}

function goReview(orderNo: string) {
  uni.navigateTo({ url: `/pages/order/review?orderNo=${orderNo}` });
}

function onReviewAction(orderNo: string) {
  if (isReviewed(orderNo)) {
    goDetail(orderNo);
  } else {
    goReview(orderNo);
  }
}

function goHome() {
  uni.switchTab({ url: "/pages/index/index" });
}

async function copyOrderNo(orderNo: string) {
  const ok = await copyToClipboard(orderNo);
  uni.showToast({ title: ok ? "已复制" : "复制失败", icon: ok ? "success" : "none" });
}

async function onCancel(orderNo: string) {
  uni.showModal({
    title: "提示",
    content: "确认取消该订单？",
    success: async (r) => {
      if (!r.confirm) return;
      try {
        await cancelOrder(orderNo);
        uni.showToast({ title: "已取消", icon: "none" });
        load(true);
      } catch (e) {
        showError(e, "取消失败");
      }
    },
  });
}

function onConfirm(orderNo: string) {
  uni.showModal({
    title: "确认收货",
    content: "请确认已收到商品",
    success: async (r) => {
      if (!r.confirm) return;
      try {
        await confirmReceive(orderNo);
        uni.showToast({ title: "已完成", icon: "success" });
        load(true);
      } catch (e) {
        showError(e, "操作失败");
      }
    },
  });
}

onShow(() => {
  load(true);
  startCountdownTicker();
});

onPullDownRefresh(() => load(true));
onReachBottom(() => load(false));
</script>

<style scoped lang="scss">
.page {
  @include page-shell;
  padding-bottom: 32rpx;
}

.tabs {
  @include tab-bar;
}

.tab {
  @include tab-item;
}

.center {
  padding-top: 80rpx;
}

.btn {
  margin-top: 32rpx;
  width: 280rpx;
  height: 72rpx;
  line-height: 72rpx;
  font-size: 28rpx;

  &.outline {
    @include btn-outline;
    background: #fff;
  }
}

.order {
  @include card;
  margin: 16rpx $spacing-page;
  padding: 24rpx;
}

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.head-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.cd {
  font-size: 22rpx;
  color: $color-primary;
  font-variant-numeric: tabular-nums;
}

.order-shop {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.shop-icon {
  font-size: 28rpx;
}

.shop-name {
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text-primary;
}

.no-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 6rpx;
}

.no {
  font-size: 22rpx;
  color: $color-text-disabled;
}

.copy-btn {
  font-size: 22rpx;
  color: $color-primary;
}

.status {
  color: $color-primary;
  font-weight: 500;
  font-size: 26rpx;
  flex-shrink: 0;
  padding: 4rpx 16rpx;
  background: $color-primary-light;
  border-radius: $radius-pill;

  &.COMPLETED,
  &.CANCELLED {
    color: $color-text-secondary;
    background: #f5f5f5;
  }
}

.row {
  display: flex;
  gap: 16rpx;
  padding: 12rpx 0;
  align-items: center;
}

.thumb {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  background: #eee;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
}

.title {
  font-size: 26rpx;
  display: block;
  color: $color-text-primary;
  line-height: 1.4;
}

.spec {
  font-size: 22rpx;
  color: $color-text-secondary;
  display: block;
  margin-top: 6rpx;
}

.right {
  text-align: right;
  flex-shrink: 0;
}

.price {
  display: block;
  color: $color-text-primary;
  font-size: 26rpx;
}

.qty {
  font-size: 22rpx;
  color: $color-text-secondary;
}

.order-foot {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-top: 16rpx;
  border-top: 1rpx solid #f5f5f5;
  padding-top: 20rpx;
}

.foot-left {
  flex: 1;
  min-width: 0;
  margin-right: 16rpx;
}

.pay {
  font-size: 26rpx;
  color: $color-text-primary;
  font-weight: 500;
  display: block;
}

.time {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: $color-text-disabled;
}

.actions {
  display: flex;
  gap: 16rpx;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.act {
  @include btn-primary;
  border-radius: 32rpx;
  font-size: 24rpx;
  padding: 0 28rpx;
  margin: 0;
  line-height: 56rpx;
  height: 56rpx;

  &.outline {
    @include btn-outline;
    color: $color-text-secondary;
    border-color: #ddd;
    background: #fff;
  }
}

.more-hint {
  text-align: center;
  padding: 24rpx;
  font-size: 24rpx;
  color: $color-text-secondary;

  &.muted {
    color: $color-text-disabled;
  }
}
</style>
