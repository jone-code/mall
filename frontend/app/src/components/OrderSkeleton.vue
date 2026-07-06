<template>
  <view class="sk-list">
    <view v-for="n in count" :key="n" class="sk-order">
      <view class="sk-head">
        <view class="sk-line sk-line-md" />
        <view class="sk-line sk-line-sm" />
      </view>
      <view class="sk-row">
        <view class="sk-thumb" />
        <view class="sk-body">
          <view class="sk-line sk-line-lg" />
          <view class="sk-line sk-line-md" />
        </view>
      </view>
      <view class="sk-foot">
        <view class="sk-line sk-line-sm" />
        <view class="sk-line sk-line-xs" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ count?: number }>(), { count: 3 });
</script>

<style scoped lang="scss">
.sk-list {
  padding: 16rpx $spacing-page;
}

.sk-order {
  @include card;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.sk-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.sk-row {
  display: flex;
  gap: 16rpx;
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

.sk-foot {
  margin-top: 20rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #f5f5f5;
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
.sk-line-sm {
  width: 35%;
}
.sk-line-xs {
  width: 25%;
  margin-left: auto;
}

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
