<template>
  <view class="page">
    <view class="icon-wrap" :class="success ? 'ok' : 'fail'">
      <text class="icon">{{ success ? "✓" : "!" }}</text>
    </view>
    <text class="title">{{ success ? "支付成功" : "支付未完成" }}</text>
    <text class="sub">订单号 {{ orderNo }}</text>
    <text v-if="amount > 0" class="amount">¥{{ Number(amount).toFixed(2) }}</text>

    <view class="actions">
      <button class="btn primary" @click="goDetail">查看订单</button>
      <button class="btn outline" @click="goHome">继续购物</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onLoad } from "@dcloudio/uni-app";
import { ref } from "vue";

const orderNo = ref("");
const amount = ref(0);
const success = ref(true);

onLoad((q: any) => {
  orderNo.value = q?.orderNo || "";
  amount.value = Number(q?.amount || 0);
  success.value = q?.success !== "0";
});

function goDetail() {
  if (!orderNo.value) {
    uni.navigateBack();
    return;
  }
  uni.redirectTo({ url: `/pages/order/detail?orderNo=${orderNo.value}` });
}

function goHome() {
  uni.switchTab({ url: "/pages/index/index" });
}
</script>

<style scoped lang="scss">
.page {
  @include page-shell;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 48rpx 48rpx;
}

.icon-wrap {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32rpx;

  &.ok {
    background: #e8f8ef;
    color: #52c41a;
  }
  &.fail {
    background: #fff2f0;
    color: #ff4d4f;
  }
}

.icon {
  font-size: 56rpx;
  font-weight: 700;
}

.title {
  font-size: 40rpx;
  font-weight: 600;
  color: $color-text-primary;
}

.sub {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: $color-text-secondary;
}

.amount {
  margin-top: 24rpx;
  @include price-text(48rpx);
  font-weight: 700;
}

.actions {
  width: 100%;
  margin-top: 80rpx;
}

.btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 30rpx;
  margin-bottom: 24rpx;
  border-radius: 44rpx;

  &.primary {
    @include btn-primary;
  }
  &.outline {
    @include btn-outline;
    background: #fff;
  }
}
</style>
