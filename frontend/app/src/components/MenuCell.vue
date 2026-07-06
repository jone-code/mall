<template>
  <view class="menu-cell" @click="$emit('click')">
    <view class="icon-wrap" :class="iconClass">
      <text class="icon">{{ icon }}</text>
    </view>
    <text class="label">{{ label }}</text>
    <text v-if="extra" class="extra">{{ extra }}</text>
    <text class="arrow">›</text>
  </view>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    icon: string
    label: string
    extra?: string
    iconClass?: string
  }>(),
  { iconClass: 'default' }
)
defineEmits<{ click: [] }>()
</script>

<style scoped lang="scss">
.menu-cell {
  @include menu-cell;
}

.icon-wrap {
  width: 64rpx;
  height: 64rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
  flex-shrink: 0;

  &.default { background: $color-primary-light; }
  &.blue { background: #e6f4ff; }
  &.green { background: #f6ffed; }
  &.orange { background: #fff7e6; }
}

.icon {
  font-size: 32rpx;
}

.label {
  flex: 1;
  font-size: 30rpx;
  color: $color-text-primary;
}

.extra {
  font-size: 24rpx;
  color: $color-text-secondary;
  margin-right: 8rpx;
}

.arrow {
  color: #ccc;
  font-size: 36rpx;
}
</style>
