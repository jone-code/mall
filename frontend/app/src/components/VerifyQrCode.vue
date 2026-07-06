<template>
  <view class="qr-wrap">
    <canvas
      :canvas-id="canvasId"
      :id="canvasId"
      class="qr-canvas"
      :style="{ width: size + 'px', height: size + 'px' }"
    />
    <text v-if="hint" class="qr-hint">{{ hint }}</text>
  </view>
</template>

<script setup lang="ts">
import { getCurrentInstance, onMounted, watch } from "vue";
// @ts-ignore no types
import UQRCode from "uqrcodejs";

const props = withDefaults(
  defineProps<{
    text: string;
    size?: number;
    hint?: string;
  }>(),
  { size: 180, hint: "扫码核销" }
);

const canvasId = `qr-${Math.random().toString(36).slice(2, 9)}`;
const instance = getCurrentInstance();

function draw() {
  const value = props.text?.trim();
  if (!value) return;

  const qr = new UQRCode();
  qr.data = value;
  qr.size = props.size;
  qr.margin = 8;
  qr.make();

  const ctx = uni.createCanvasContext(canvasId, instance?.proxy);
  qr.canvasContext = ctx;
  qr.drawCanvas();
}

onMounted(draw);
watch(() => props.text, draw);
</script>

<style scoped lang="scss">
.qr-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.qr-canvas {
  background: #fff;
  border-radius: 12rpx;
  border: 1rpx solid #eee;
}

.qr-hint {
  margin-top: 16rpx;
  font-size: 22rpx;
  color: $color-text-secondary;
}
</style>
