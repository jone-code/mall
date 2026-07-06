<template>
  <view class="section-header">
    <view class="title-wrap">
      <text class="title">{{ title }}</text>
      <text v-if="subtitle" class="subtitle">{{ subtitle }}</text>
    </view>
    <text v-if="action" class="action" @click="$emit('action')">{{ action }}</text>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
  action?: string
}>()
defineEmits<{ action: [] }>()
</script>

<style scoped lang="scss">
.section-header {
  @include section-header-row;
  padding: 0 $spacing-page;
  margin: 16rpx 0 28rpx;
}

.title-wrap {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  min-width: 0;
}

.title {
  @include section-header-title;
}

.subtitle {
  font-size: 22rpx;
  color: $color-text-secondary;
  letter-spacing: 2rpx;
  text-transform: uppercase;
}

.action {
  font-size: 22rpx;
  color: $color-text-secondary;
  letter-spacing: 2rpx;
  text-transform: uppercase;
  flex-shrink: 0;
}
</style>
