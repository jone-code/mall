<template>
  <view class="steps">
    <view
      v-for="(step, i) in steps"
      :key="step"
      class="step"
      :class="{ active: i <= current, done: i < current }"
    >
      <view class="dot">{{ i + 1 }}</view>
      <text class="label">{{ step }}</text>
      <view v-if="i < steps.length - 1" class="line" />
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    current?: number
    steps?: string[]
  }>(),
  {
    current: 0,
    steps: () => ['确认订单', '支付', '完成'],
  }
)
</script>

<style scoped lang="scss">
.steps {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 28rpx $spacing-page;
  background: $color-bg-card;
  margin-bottom: 16rpx;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  flex: 1;
  max-width: 200rpx;
}

.dot {
  width: 44rpx;
  height: 44rpx;
  line-height: 44rpx;
  text-align: center;
  border-radius: 50%;
  font-size: 22rpx;
  background: #f0f0f0;
  color: $color-text-secondary;
  position: relative;
  z-index: 1;
}

.step.active .dot {
  background: $color-primary;
  color: #fff;
}

.step.done .dot {
  background: $color-primary-light;
  color: $color-primary;
}

.label {
  margin-top: 10rpx;
  font-size: 22rpx;
  color: $color-text-secondary;
  white-space: nowrap;
}

.step.active .label {
  color: $color-primary;
  font-weight: 600;
}

.line {
  position: absolute;
  top: 22rpx;
  left: calc(50% + 22rpx);
  width: calc(100% - 44rpx);
  height: 2rpx;
  background: #eee;
  z-index: 0;
}

.step.done .line {
  background: rgba(255, 77, 79, 0.35);
}
</style>
