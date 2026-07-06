<template>
  <view class="copy-text" @click.stop="onCopy">
    <text class="copy-text__value">{{ text }}</text>
    <text class="copy-text__icon">⎘</text>
  </view>
</template>

<script setup lang="ts">
import { copyToClipboard } from "@/utils/format";

const props = withDefaults(
  defineProps<{
    text: string;
    successMsg?: string;
  }>(),
  { successMsg: "已复制" }
);

async function onCopy() {
  const ok = await copyToClipboard(props.text);
  uni.showToast({
    title: ok ? props.successMsg : "复制失败",
    icon: ok ? "success" : "none",
  });
}
</script>

<style scoped lang="scss">
.copy-text {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  max-width: 100%;
}

.copy-text__value {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.copy-text__icon {
  flex-shrink: 0;
  font-size: 28rpx;
  color: $color-primary;
  opacity: 0.85;
}
</style>
