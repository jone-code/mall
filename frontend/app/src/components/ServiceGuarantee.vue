<template>
  <view class="guarantee">
    <view v-for="item in items" :key="item.label" class="item">
      <text class="icon">{{ item.icon }}</text>
      <text class="label">{{ item.label }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
const items = [
  { icon: '✓', label: '正品保障' },
  { icon: '↗', label: '极速发货' },
  { icon: '↺', label: '售后无忧' },
]
</script>

<style scoped lang="scss">
.guarantee {
  display: flex;
  justify-content: space-around;
  padding: 28rpx $spacing-page;
  margin: 0;
  border-top: 1rpx solid $color-border;
  border-bottom: 1rpx solid $color-border;
  background: $color-bg-page;
}

.item {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.icon {
  width: 28rpx;
  height: 28rpx;
  line-height: 28rpx;
  text-align: center;
  font-size: 18rpx;
  color: $color-text-primary;
  border: 1rpx solid $color-border;
  border-radius: 50%;
}

.label {
  font-size: 22rpx;
  color: $color-text-secondary;
  letter-spacing: 1rpx;
}
</style>
