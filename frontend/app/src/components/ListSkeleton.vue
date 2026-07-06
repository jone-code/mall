<template>
  <view class="sk-list">
    <view v-for="n in count" :key="n" class="sk-card">
      <view class="sk-line sk-line-lg" />
      <view class="sk-line sk-line-md" />
      <view class="sk-line sk-line-sm" />
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ count?: number }>(), { count: 4 });
</script>

<style scoped lang="scss">
.sk-list {
  padding: 0 $spacing-page;
}

.sk-card {
  @include card;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.sk-line {
  height: 24rpx;
  margin-bottom: 14rpx;
  border-radius: 8rpx;
  background: linear-gradient(90deg, #eee 25%, #f5f5f5 37%, #eee 63%);
  background-size: 400% 100%;
  animation: sk-shimmer 1.4s ease infinite;
}

.sk-line-lg {
  width: 70%;
}
.sk-line-md {
  width: 90%;
}
.sk-line-sm {
  width: 45%;
  margin-bottom: 0;
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
