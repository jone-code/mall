<template>
  <view class="sk-wrap">
    <view class="sk-status" />
    <view class="sk-card" />
    <view class="sk-card" />
    <view class="sk-card" />
  </view>
</template>

<style scoped lang="scss">
.sk-wrap {
  padding: 16rpx;
}

.sk-status {
  height: 120rpx;
  background: #f0f0f0;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
}

.sk-card {
  height: 180rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  margin-bottom: 16rpx;
  animation: sk-pulse 1.4s ease infinite;
}

.sk-status {
  animation: sk-pulse 1.4s ease infinite;
}

@keyframes sk-pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.55;
  }
}
</style>
