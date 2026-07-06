<template>
  <view class="sk-page">
    <view class="sk-banner" />
    <view class="sk-panel">
      <view class="sk-line sk-line-price" />
      <view class="sk-line sk-line-title" />
      <view class="sk-line sk-line-sub" />
    </view>
    <view class="sk-panel sk-panel-tall" />
    <view class="sk-panel" />
  </view>
</template>

<style scoped lang="scss">
.sk-page {
  @include page-shell;
  min-height: 100vh;
}

.sk-banner {
  height: 750rpx;
  background: linear-gradient(90deg, #eee 25%, #f5f5f5 37%, #eee 63%);
  background-size: 400% 100%;
  animation: sk-shimmer 1.4s ease infinite;
}

.sk-panel {
  @include card;
  margin: 16rpx;
  padding: 24rpx;
  min-height: 120rpx;
}

.sk-panel-tall {
  min-height: 200rpx;
}

.sk-line {
  height: 28rpx;
  margin-bottom: 16rpx;
  border-radius: 8rpx;
  background: linear-gradient(90deg, #eee 25%, #f5f5f5 37%, #eee 63%);
  background-size: 400% 100%;
  animation: sk-shimmer 1.4s ease infinite;
}

.sk-line-price {
  width: 40%;
  height: 40rpx;
}
.sk-line-title {
  width: 80%;
}
.sk-line-sub {
  width: 55%;
  margin-bottom: 0;
}

.sk-panel {
  background: linear-gradient(90deg, #f8f8f8 25%, #fafafa 37%, #f8f8f8 63%);
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
