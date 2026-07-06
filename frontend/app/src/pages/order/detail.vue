<template>
  <view class="page">
    <OrderDetailSkeleton v-if="loading" />

    <template v-else-if="order">
      <view class="status-bar">
        <text class="status-text">{{ orderStatusLabel(order.status) }}</text>
        <text v-if="order.status === 'PENDING_PAY' && countdown" class="countdown">
          剩余支付 {{ countdown }}
        </text>
        <text v-else-if="order.status === 'PENDING_PAY' && payExpired" class="countdown expired">
          支付已超时
        </text>
      </view>

      <view v-if="showAddress" class="card">
        <view class="card-title">收货信息</view>
        <text class="receiver">{{ order.receiver }} {{ order.receiverPhone }}</text>
        <text class="addr">{{ order.addressDetail }}</text>
      </view>

      <view v-if="showLogistics" class="card">
        <view class="card-title">物流信息</view>
        <view class="meta-row">
          <text>承运商</text>
          <text>{{ order.trackingCompany || "快递" }}</text>
        </view>
        <view class="meta-row">
          <text>运单号</text>
          <CopyText :text="order.trackingNo || ''" />
        </view>
        <view v-if="order.shipAt" class="meta-row">
          <text>发货时间</text>
          <RelativeTime :value="order.shipAt" mode="absolute" />
        </view>
        <view v-for="(ev, i) in logisticsEvents" :key="i" class="logistics-ev">
          <text class="ev-time"><RelativeTime :value="ev.time" mode="absolute" /></text>
          <text class="ev-desc">{{ ev.status }} — {{ ev.desc }}</text>
        </view>
      </view>

      <view v-if="showVirtualCard" class="card">
        <view class="card-title">卡密信息</view>
        <view class="meta-row">
          <text>卡号</text>
          <CopyText :text="order.fulfillment?.cardNo || ''" />
        </view>
        <view class="meta-row">
          <text>卡密</text>
          <view class="secret-row">
            <text class="secret">{{ secretVisible ? order.fulfillment?.cardSecret : maskedSecret }}</text>
            <text class="toggle" @click="secretVisible = !secretVisible">{{ secretVisible ? "隐藏" : "显示" }}</text>
            <text class="toggle copy" @click="copySecret">复制</text>
          </view>
        </view>
      </view>

      <view v-if="showVerifyCode" class="card">
        <view class="card-title">服务核销</view>
        <VerifyQrCode :text="order.fulfillment?.verifyCode || ''" />
        <view class="verify-box">
          <text class="verify-code">{{ order.fulfillment?.verifyCode }}</text>
          <CopyText :text="order.fulfillment?.verifyCode || ''" success-msg="核销码已复制" />
          <text class="verify-hint">到店出示二维码或核销码即可使用</text>
        </view>
      </view>

      <view class="card">
        <view class="card-title">商品</view>
        <view v-for="item in order.items" :key="item.skuId" class="row">
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
      </view>

      <view class="card">
        <view class="fee-row"><text>商品金额</text><text>¥{{ Number(order.totalAmount).toFixed(2) }}</text></view>
        <view class="fee-row"><text>运费</text><text>¥{{ Number(order.freightAmount).toFixed(2) }}</text></view>
        <view class="fee-row total"><text>实付</text><text>¥{{ Number(order.payAmount).toFixed(2) }}</text></view>
      </view>

      <view class="card meta">
        <view class="meta-row">
          <text>订单号</text>
          <CopyText :text="order.orderNo" />
        </view>
        <view class="meta-row">
          <text>下单时间</text>
          <RelativeTime :value="order.createdAt" mode="absolute" />
        </view>
        <view v-if="order.payAt" class="meta-row">
          <text>支付时间</text>
          <RelativeTime :value="order.payAt" mode="absolute" />
        </view>
        <view v-if="order.completeAt" class="meta-row">
          <text>完成时间</text>
          <RelativeTime :value="order.completeAt" mode="absolute" />
        </view>
        <view v-if="order.remark" class="meta-row"><text>备注</text><text>{{ order.remark }}</text></view>
      </view>

      <FixedActionBar v-if="order.status === 'PENDING_PAY' && !payExpired">
        <button class="act outline" @click="onCancel">取消订单</button>
        <button class="act" @click="goPay">去支付</button>
      </FixedActionBar>

      <FixedActionBar v-else-if="order.status === 'SHIPPED' && order.productType !== 'SERVICE'">
        <button class="act outline" v-if="canRefund" @click="onRefund">申请退款</button>
        <button class="act" @click="onConfirm">确认收货</button>
      </FixedActionBar>

      <FixedActionBar v-else-if="order.status === 'SHIPPED' && order.productType === 'SERVICE'">
        <button class="act outline" v-if="canRefund" @click="onRefund">申请退款</button>
      </FixedActionBar>

      <FixedActionBar v-else-if="canRefund">
        <button class="act outline" @click="onRefund">申请退款</button>
      </FixedActionBar>

      <FixedActionBar v-else-if="showReviewAction">
        <button class="act" @click="goReview">{{ reviewExists ? "查看评价" : "去评价" }}</button>
      </FixedActionBar>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import {
  getOrder,
  cancelOrder,
  confirmReceive,
  applyRefund,
  getOrderLogistics,
  orderStatusLabel,
  type OrderDetail,
} from "@/api/order";
import { getReviewByOrder, type Review } from "@/api/review";
import { useCountdown } from "@/composables/useCountdown";
import CopyText from "@/components/CopyText.vue";
import RelativeTime from "@/components/RelativeTime.vue";
import VerifyQrCode from "@/components/VerifyQrCode.vue";
import OrderDetailSkeleton from "@/components/OrderDetailSkeleton.vue";
import FixedActionBar from "@/components/FixedActionBar.vue";
import { copyToClipboard, maskSecret } from "@/utils/format";
import { resolveMediaUrl } from "@/utils/media";
import { showError } from "@/utils/error-message";

const orderNo = ref("");
const order = ref<OrderDetail | null>(null);
const loading = ref(true);
const logisticsEvents = ref<{ time: string; status: string; desc: string }[]>([]);
const existingReview = ref<Review | null>(null);
const secretVisible = ref(false);

const expireAt = computed(() => order.value?.expireAt);
const { countdown, expired: payExpired } = useCountdown(expireAt);

const reviewExists = computed(() => !!existingReview.value);
const showReviewAction = computed(() => order.value?.status === "COMPLETED");

const maskedSecret = computed(() =>
  maskSecret(order.value?.fulfillment?.cardSecret)
);

const canRefund = computed(() => {
  const s = order.value?.status;
  return s === "PAID" || s === "SHIPPED";
});

const showAddress = computed(() => {
  const t = order.value?.productType;
  return !t || t === "PHYSICAL";
});

const showLogistics = computed(
  () =>
    order.value?.productType === "PHYSICAL" &&
    !!order.value?.trackingNo &&
    ["SHIPPED", "COMPLETED"].includes(order.value.status)
);

const showVirtualCard = computed(
  () =>
    order.value?.productType === "VIRTUAL" &&
    order.value.status === "COMPLETED" &&
    !!order.value.fulfillment?.cardNo
);

const showVerifyCode = computed(
  () =>
    order.value?.productType === "SERVICE" &&
    ["SHIPPED", "COMPLETED"].includes(order.value?.status || "") &&
    !!order.value?.fulfillment?.verifyCode
);

function mediaUrl(url?: string) {
  return resolveMediaUrl(url);
}

onLoad((q: any) => {
  orderNo.value = q?.orderNo || "";
});

async function load() {
  if (!orderNo.value) return;
  loading.value = true;
  try {
    const res = await getOrder(orderNo.value);
    order.value = res.data || null;
    logisticsEvents.value = [];
    if (order.value?.trackingNo) {
      try {
        const lg = await getOrderLogistics(orderNo.value);
        logisticsEvents.value = lg.data?.events || [];
      } catch {
        /* ignore */
      }
    }
    if (order.value?.status === "COMPLETED") {
      try {
        const rv = await getReviewByOrder(orderNo.value);
        existingReview.value = rv.data || null;
      } catch {
        existingReview.value = null;
      }
    } else {
      existingReview.value = null;
    }
  } catch (e) {
    showError(e, "加载失败");
  } finally {
    loading.value = false;
  }
}

function goPay() {
  uni.navigateTo({
    url: `/pages/order/pay?orderNo=${orderNo.value}&amount=${order.value?.payAmount}`,
  });
}

async function copySecret() {
  const secret = order.value?.fulfillment?.cardSecret;
  if (!secret) return;
  const ok = await copyToClipboard(secret);
  uni.showToast({ title: ok ? "卡密已复制" : "复制失败", icon: ok ? "success" : "none" });
}

function onCancel() {
  uni.showModal({
    title: "提示",
    content: "确认取消该订单？",
    success: async (r) => {
      if (!r.confirm) return;
      try {
        await cancelOrder(orderNo.value);
        uni.showToast({ title: "已取消", icon: "none" });
        load();
      } catch (e) {
        showError(e, "取消失败");
      }
    },
  });
}

function onConfirm() {
  uni.showModal({
    title: "确认收货",
    content: "请确认已收到商品",
    success: async (r) => {
      if (!r.confirm) return;
      try {
        await confirmReceive(orderNo.value);
        uni.showToast({ title: "已完成", icon: "success" });
        load();
      } catch (e) {
        showError(e, "操作失败");
      }
    },
  });
}

function onRefund() {
  uni.showModal({
    title: "申请退款",
    editable: true,
    placeholderText: "请填写退款原因",
    success: async (r) => {
      if (!r.confirm) return;
      const reason = (r.content || "").trim() || "用户申请退款";
      try {
        await applyRefund(orderNo.value, reason);
        uni.showToast({ title: "已提交", icon: "none" });
        load();
      } catch (e) {
        showError(e, "提交失败");
      }
    },
  });
}

function goReview() {
  if (existingReview.value) {
    uni.showModal({
      title: "我的评价",
      content: `${"★".repeat(existingReview.value.rating)}\n${existingReview.value.content}`,
      showCancel: false,
    });
    return;
  }
  uni.navigateTo({ url: `/pages/order/review?orderNo=${orderNo.value}` });
}

onShow(() => {
  load();
});
</script>

<style scoped lang="scss">
.page {
  @include page-shell;
  padding-bottom: 140rpx;
}

.status-bar {
  background: $color-primary;
  color: #fff;
  padding: 48rpx $spacing-page;
}

.status-text {
  font-size: 36rpx;
  font-weight: 600;
  display: block;
}

.countdown {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  opacity: 0.9;

  &.expired {
    opacity: 0.75;
  }
}

.card {
  @include card;
  margin: 16rpx;
  padding: 24rpx;
}
.card-title {
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 16rpx;
}
.receiver {
  display: block;
  font-size: 28rpx;
}
.addr {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-top: 8rpx;
}
.row {
  display: flex;
  gap: 16rpx;
  padding: 12rpx 0;
  align-items: center;
}
.thumb {
  width: 110rpx;
  height: 110rpx;
  border-radius: 8rpx;
  background: #eee;
}
.info {
  flex: 1;
  min-width: 0;
}
.title {
  font-size: 26rpx;
  display: block;
}
.spec {
  font-size: 22rpx;
  color: #999;
  display: block;
  margin-top: 6rpx;
}
.right {
  text-align: right;
}
.price {
  display: block;
}
.qty {
  font-size: 22rpx;
  color: #999;
}
.fee-row {
  display: flex;
  justify-content: space-between;
  font-size: 26rpx;
  padding: 8rpx 0;
  color: #666;
}
.fee-row.total {
  @include price-text(30rpx);
  font-weight: 600;
}
.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16rpx;
  font-size: 24rpx;
  color: $color-text-secondary;
  padding: 6rpx 0;
}
.secret-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
  justify-content: flex-end;
  max-width: 65%;
}
.secret {
  font-family: monospace;
  letter-spacing: 2rpx;
  word-break: break-all;
}
.toggle {
  font-size: 22rpx;
  color: $color-primary;
  flex-shrink: 0;

  &.copy {
    margin-left: 4rpx;
  }
}
.verify-box {
  text-align: center;
  padding: 16rpx 0 0;
}
.verify-code {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
  color: $color-primary;
  margin-bottom: 8rpx;
}
.verify-hint {
  display: block;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #999;
}
.logistics-ev {
  padding: 12rpx 0;
  border-top: 1rpx solid #f0f0f0;
}
.ev-time {
  display: block;
  font-size: 22rpx;
  color: #999;
}
.ev-desc {
  display: block;
  font-size: 24rpx;
  margin-top: 4rpx;
}

.act {
  @include btn-primary;
  border-radius: 40rpx;
  font-size: 28rpx;
  padding: 0 48rpx;
  margin: 0;
  height: 80rpx;
  line-height: 80rpx;

  &.outline {
    @include btn-outline;
    color: $color-text-secondary;
    border-color: #ddd;
    background: #fff;
  }
}
</style>
