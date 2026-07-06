<template>
  <view class="sk-wrap">
    <view class="sk-card sk-addr" />
    <view class="sk-card">
      <view v-for="n in 2" :key="n" class="sk-row">
        <view class="sk-thumb" />
        <view class="sk-body">
          <view class="sk-line sk-line-lg" />
          <view class="sk-line sk-line-md" />
        </view>
      </view>
    </view>
    <view class="sk-card sk-fee" />
  </view>
</template>

<style scoped lang="scss">
.sk-wrap {
  padding: 16rpx $spacing-page;
}

.sk-card {
  @include card;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.sk-addr {
  height: 160rpx;
}

.sk-fee {
  height: 100rpx;
}

.sk-row {
  display: flex;
  gap: 16rpx;
  padding: 12rpx 0;
}

.sk-thumb {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.sk-body {
  flex: 1;
  padding-top: 8rpx;
}

.sk-line {
  height: 24rpx;
  margin-bottom: 12rpx;
  border-radius: 8rpx;
}

.sk-line-lg {
  width: 85%;
}
.sk-line-md {
  width: 55%;
}

.sk-addr,
.sk-fee,
.sk-thumb,
.sk-line {
  background: linear-gradient(90deg, #eee 25%, #f5f5f5 37%, #eee 63%);
  background-size: 400% 100%;
  animation: sk-shimmer 1.4s ease infinite;
}

@keyframes sk-shimmer {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}
</style>
