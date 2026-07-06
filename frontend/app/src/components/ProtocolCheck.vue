<template>
  <view class="proto">
    <view class="checkbox" :class="{ checked: modelValue }" @click="toggle">
      <text v-if="modelValue" class="tick">✓</text>
    </view>
    <view class="txt">
      我已阅读
      <text class="link" @click.stop="open('user')">《用户协议》</text>
      <text class="link" @click.stop="open('privacy')">《隐私政策》</text>
    </view>
  </view>
</template>

<script setup lang="ts">
const props = defineProps<{ modelValue: boolean }>();
const emit = defineEmits<{
  (e: "update:modelValue", v: boolean): void;
}>();

function toggle() {
  emit("update:modelValue", !props.modelValue);
}
function open(type: "user" | "privacy") {
  uni.navigateTo({ url: `/pages/mine/protocol?type=${type}` });
}
</script>

<style scoped lang="scss">
@import "@/styles/auth.scss";

.proto {
  display: flex;
  align-items: flex-start;
  font-size: 24rpx;
  color: $auth-text-secondary;
  line-height: 1.5;
}
.checkbox {
  width: 32rpx;
  height: 32rpx;
  border-radius: 6rpx;
  border: 1rpx solid #d9d9d9;
  margin-right: 12rpx;
  margin-top: 4rpx;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}
.checkbox.checked {
  background: $auth-primary;
  border-color: $auth-primary;
}
.tick {
  color: #fff;
  font-size: 22rpx;
}
.txt {
  flex: 1;
}
.link {
  color: $auth-primary;
}
</style>
