<template>
  <view class="fixed-action-bar" :class="{ 'above-tab-bar': aboveTabBar, full: full }">
    <view v-if="full" class="full-inner">
      <slot />
    </view>
    <template v-else>
      <view class="left">
        <slot name="left" />
      </view>
      <view class="actions">
        <slot />
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    aboveTabBar?: boolean
    full?: boolean
  }>(),
  {
    aboveTabBar: false,
    full: false,
  }
)
</script>

<style scoped lang="scss">
.fixed-action-bar {
  @include fixed-footer(false);
  width: 100%;
  box-sizing: border-box;
  justify-content: space-between;

  &.above-tab-bar {
    bottom: calc(#{$tab-bar-height} + env(safe-area-inset-bottom));
  }

  &.full {
    display: block;
    padding-top: 16rpx;
    padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
  }
}

.full-inner {
  width: 100%;
}

.left {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
}

.actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16rpx;
  flex-shrink: 0;
  margin-left: auto;
}

.actions :deep(button),
.actions :deep(uni-button) {
  width: auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0;
}
</style>
