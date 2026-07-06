<template>
  <view class="page">
    <StepIndicator :current="1" />

    <view class="pay-header">
      <text class="pay-title">收银台</text>
      <text v-if="countdown" class="pay-sub countdown">请在 {{ countdown }} 内完成支付</text>
      <text v-else-if="expired" class="pay-sub expired">订单已超时，请重新下单</text>
      <text v-else class="pay-sub">请在 30 分钟内完成支付</text>
    </view>

    <view class="amount-card">
      <text class="label">应付金额</text>
      <text class="amount">¥{{ Number(amount).toFixed(2) }}</text>
      <view class="order-no-row">
        <text class="order-no">订单号 {{ orderNo }}</text>
        <CopyText :text="orderNo" />
      </view>
    </view>

    <view class="channel">
      <text class="channel-title">选择支付方式</text>
      <view class="channel-item active">
        <view class="channel-left">
          <text class="channel-icon">💳</text>
          <text>模拟支付（MOCK）</text>
        </view>
        <text class="dot">●</text>
      </view>
      <text class="tip">本地联调用 Mock 渠道，点击下方按钮模拟支付成功回调。</text>
    </view>

    <view class="secure">
      <text class="secure-icon">🔒</text>
      <text>支付环境安全加密</text>
    </view>

    <button class="pay-btn" :disabled="paying || expired" @click="onPay">
      {{ paying ? "支付中…" : expired ? "已超时" : "确认支付" }}
    </button>
    <button class="later-btn" @click="onLater">稍后支付</button>
  </view>
</template>

<script setup lang="ts">
import { onLoad } from "@dcloudio/uni-app";
import { computed, ref } from "vue";
import { createPay, confirmPay, getOrder } from "@/api/order";
import { useCartStore } from "@/stores/cart";
import { useCountdown } from "@/composables/useCountdown";
import StepIndicator from "@/components/StepIndicator.vue";
import CopyText from "@/components/CopyText.vue";
import { showError } from "@/utils/error-message";

const orderNo = ref("");
const amount = ref(0);
const paying = ref(false);
const expireAt = ref<string | undefined>();
const cartStore = useCartStore();

const expireRef = computed(() => expireAt.value);
const { countdown, expired } = useCountdown(expireRef);

onLoad(async (q: any) => {
  orderNo.value = q?.orderNo || "";
  amount.value = Number(q?.amount || 0);
  if (orderNo.value) {
    try {
      const res = await getOrder(orderNo.value);
      if (res.data?.expireAt) expireAt.value = res.data.expireAt;
      if (res.data?.payAmount) amount.value = res.data.payAmount;
    } catch {
      /* ignore */
    }
  }
});

async function onPay() {
  if (!orderNo.value || expired.value) return;
  paying.value = true;
  try {
    const created = await createPay(orderNo.value);
    const payNo = created.data?.payNo;
    if (!payNo) throw new Error("create pay failed");
    const confirmed = await confirmPay(payNo);
    const ok = confirmed.data?.status === "SUCCESS";
    cartStore.refreshBadge();
    uni.redirectTo({
      url: `/pages/order/pay-result?orderNo=${orderNo.value}&amount=${amount.value}&success=${ok ? 1 : 0}`,
    });
  } catch (e) {
    showError(e, "支付失败");
  } finally {
    paying.value = false;
  }
}

function onLater() {
  uni.redirectTo({ url: "/pages/order/list?status=PENDING_PAY" });
}
</script>

<style scoped lang="scss">
.page {
  @include page-shell;
  padding: 0 24rpx 48rpx;
}

.pay-header {
  padding: 24rpx 8rpx 16rpx;
}

.pay-title {
  display: block;
  font-size: 36rpx;
  font-weight: 700;
  color: $color-text-primary;
}

.pay-sub {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: $color-text-secondary;

  &.countdown {
    color: $color-primary;
    font-weight: 500;
  }
  &.expired {
    color: #ff4d4f;
  }
}

.amount-card {
  @include card;
  padding: 48rpx 24rpx;
  text-align: center;
  background: linear-gradient(180deg, #fff 0%, #fffafa 100%);
}

.channel {
  @include card;
  padding: 24rpx;
  margin-top: 24rpx;
}

.channel-title {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 20rpx;
  color: $color-text-primary;
}

.pay-btn {
  @include btn-primary;
  margin-top: 40rpx;
  font-size: 30rpx;
  height: 88rpx;
  line-height: 88rpx;

  &[disabled] {
    opacity: 0.55;
  }
}

.later-btn {
  margin-top: 24rpx;
  background: #fff;
  color: $color-text-secondary;
  border-radius: 44rpx;
  font-size: 28rpx;
  &::after { border: none; }
}

.label {
  color: $color-text-secondary;
  font-size: 26rpx;
  display: block;
}

.amount {
  @include price-text(72rpx);
  font-weight: 700;
  display: block;
  margin: 16rpx 0;
}

.order-no-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}

.order-no {
  color: $color-text-disabled;
  font-size: 22rpx;
}

.channel-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 28rpx;
  padding: 20rpx;
  border-radius: 12rpx;
  background: $color-primary-light;
  border: 2rpx solid rgba(255, 77, 79, 0.25);
}

.channel-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.channel-icon {
  font-size: 36rpx;
}

.channel-item.active .dot {
  color: $color-primary;
  font-size: 28rpx;
}

.tip {
  display: block;
  margin-top: 16rpx;
  color: $color-text-secondary;
  font-size: 22rpx;
  line-height: 1.5;
}

.secure {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  margin-top: 32rpx;
  font-size: 22rpx;
  color: $color-text-disabled;
}

.secure-icon {
  font-size: 24rpx;
}
</style>
