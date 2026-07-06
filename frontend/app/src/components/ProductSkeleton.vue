<template>
  <view class="sk-grid">
    <view v-for="n in count" :key="n" class="sk-card">
      <view class="sk-cover" />
      <view class="sk-line sk-line-lg" />
      <view class="sk-line sk-line-sm" />
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ count?: number }>(), { count: 6 })
</script>

<style scoped lang="scss">
.sk-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  padding: 0 $spacing-page;
}

.sk-card {
  width: 48%;
  margin-bottom: 48rpx;
}

.sk-cover {
  width: 100%;
  height: 400rpx;
}

.sk-line {
  height: 22rpx;
  margin-top: 20rpx;
  border-radius: 2rpx;
}

.sk-line-lg {
  width: 85%;
}

.sk-line-sm {
  width: 45%;
  margin-top: 14rpx;
}

.sk-cover,
.sk-line {
  background: linear-gradient(90deg, $color-bg-section 25%, #eee 37%, $color-bg-section 63%);
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
