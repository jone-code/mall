<template>
  <view class="phone-input">
    <view class="prefix">+86 ▾</view>
    <input
      class="input"
      type="number"
      maxlength="11"
      :value="modelValue"
      placeholder="138 0000 0000"
      @input="onInput"
    />
  </view>
  <view v-if="errorMsg" class="error-tip">{{ errorMsg }}</view>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{ modelValue: string }>();
const emit = defineEmits<{
  (e: "update:modelValue", v: string): void;
  (e: "valid", valid: boolean): void;
}>();

const phoneReg = /^1[3-9]\d{9}$/;

const errorMsg = computed(() => {
  if (!props.modelValue) return "";
  if (props.modelValue.length < 11) return "";
  return phoneReg.test(props.modelValue) ? "" : "手机号格式不正确";
});

function onInput(e: any) {
  const v: string = (e.detail.value || "").replace(/\D/g, "").slice(0, 11);
  emit("update:modelValue", v);
  emit("valid", phoneReg.test(v));
}
</script>

<style scoped lang="scss">
@import "@/styles/auth.scss";

.phone-input {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid $auth-border;
}
.prefix {
  font-size: 32rpx;
  font-weight: 500;
  color: $auth-text;
  flex-shrink: 0;
}
.input {
  flex: 1;
  font-size: 36rpx;
  color: $auth-text;
}
.error-tip {
  margin-top: 12rpx;
  color: $auth-primary;
  font-size: 24rpx;
}
</style>
