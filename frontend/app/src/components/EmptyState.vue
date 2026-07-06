<template>
  <view class="empty-state">
    <view class="icon-ring">
      <text class="empty-icon">{{ icon }}</text>
    </view>
    <text class="empty-text">{{ text }}</text>
    <text v-if="desc" class="empty-desc">{{ desc }}</text>
    <slot />
  </view>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    text?: string
    desc?: string
    icon?: string
  }>(),
  {
    text: '暂无数据',
    icon: '🗒️',
  }
)
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100rpx 48rpx;
}

.icon-ring {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: $color-bg-section;
  border: 1rpx solid $color-border;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
}

.empty-icon {
  font-size: 64rpx;
  line-height: 1;
}

.empty-text {
  font-size: 28rpx;
  color: $color-text-primary;
  font-weight: 500;
  letter-spacing: 1rpx;
}

.empty-desc {
  margin-top: 12rpx;
  font-size: 24rpx;
  color: $color-text-secondary;
  text-align: center;
  line-height: 1.5;
}
</style>
