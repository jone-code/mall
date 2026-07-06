<template>
  <view class="sms-wrap" :class="{ shake: shake }">
    <view
      v-for="(_, i) in length"
      :key="i"
      class="cell"
      :class="{ active: i === activeIndex, filled: !!chars[i], error: shake }"
      @click="focusInput"
    >
      {{ chars[i] || "" }}
    </view>
    <input
      ref="hiddenInputRef"
      class="hidden-input"
      type="number"
      :maxlength="length"
      :value="modelValue"
      :textContentType="'oneTimeCode'"
      :focus="focused"
      @input="onInput"
      @blur="onBlur"
    />
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";

const props = withDefaults(
  defineProps<{ modelValue: string; length?: number; shake?: boolean }>(),
  { length: 6, shake: false }
);
const emit = defineEmits<{
  (e: "update:modelValue", v: string): void;
  (e: "complete", v: string): void;
}>();

const focused = ref(false);

const chars = computed(() => props.modelValue.split(""));
const activeIndex = computed(() =>
  Math.min(props.modelValue.length, props.length - 1)
);

function focusInput() {
  focused.value = false;
  setTimeout(() => {
    focused.value = true;
  }, 0);
}
function onBlur() {
  focused.value = false;
}
function onInput(e: any) {
  let v: string = (e.detail.value || "").replace(/\D/g, "");
  if (v.length > props.length) v = v.slice(0, props.length);
  emit("update:modelValue", v);
  if (v.length === props.length) {
    emit("complete", v);
  }
}

watch(
  () => props.shake,
  (s) => {
    if (s) focusInput();
  }
);
</script>

<style scoped lang="scss">
@import "@/styles/auth.scss";

.sms-wrap {
  position: relative;
  display: flex;
  justify-content: space-between;
  gap: 12rpx;
}
.cell {
  width: 96rpx;
  height: 96rpx;
  border: 1rpx solid #d9d9d9;
  border-radius: $auth-btn-radius;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  font-weight: 600;
  color: $auth-text;
  background: #fff;
}
.cell.filled,
.cell.active {
  border-color: $auth-primary;
}
.cell.error {
  border-color: $auth-primary;
}
.hidden-input {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
}
.shake {
  animation: shake 0.4s;
}
@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }
  20% {
    transform: translateX(-12rpx);
  }
  40% {
    transform: translateX(12rpx);
  }
  60% {
    transform: translateX(-8rpx);
  }
  80% {
    transform: translateX(8rpx);
  }
}
</style>
