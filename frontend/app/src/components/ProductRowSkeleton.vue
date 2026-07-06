<template>
  <view class="sk-list">
    <view v-for="n in count" :key="n" class="sk-row">
      <view class="sk-thumb" />
      <view class="sk-body">
        <view class="sk-line sk-line-lg" />
        <view class="sk-line sk-line-md" />
        <view class="sk-line sk-line-sm" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ count?: number }>(), { count: 5 });
</script>

<style scoped lang="scss">
.sk-list {
  padding: 0 8rpx;
}

.sk-row {
  display: flex;
  gap: 24rpx;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}

.sk-thumb {
  width: 160rpx;
  height: 160rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.sk-body {
  flex: 1;
  padding-top: 8rpx;
}

.sk-line {
  height: 24rpx;
  margin-bottom: 14rpx;
  border-radius: 8rpx;
}

.sk-line-lg {
  width: 85%;
}
.sk-line-md {
  width: 60%;
}
.sk-line-sm {
  width: 35%;
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
