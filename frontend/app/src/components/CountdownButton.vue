<template>
  <button
    class="cd-btn"
    :class="{ disabled: !canClick }"
    :disabled="!canClick"
    @click="onClick"
  >
    {{ display }}
  </button>
</template>

<script setup lang="ts">
import { computed, onUnmounted, ref } from "vue";

const props = withDefaults(
  defineProps<{
    seconds?: number;
    text?: string;
    disabled?: boolean;
  }>(),
  { seconds: 60, text: "获取验证码", disabled: false }
);
const emit = defineEmits<{
  (e: "click"): void;
}>();

const remaining = ref(0);
let timer: ReturnType<typeof setInterval> | null = null;

const canClick = computed(() => remaining.value === 0 && !props.disabled);
const display = computed(() => {
  if (remaining.value > 0) return `获取验证码  ${remaining.value}s`;
  return props.text;
});

function start() {
  remaining.value = props.seconds;
  if (timer) clearInterval(timer);
  timer = setInterval(() => {
    remaining.value -= 1;
    if (remaining.value <= 0 && timer) {
      clearInterval(timer);
      timer = null;
    }
  }, 1000);
}
function reset() {
  if (timer) clearInterval(timer);
  remaining.value = 0;
}
function onClick() {
  if (!canClick.value) return;
  emit("click");
}

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

defineExpose({ start, reset });
</script>

<style scoped lang="scss">
@import "@/styles/auth.scss";

.cd-btn {
  background: transparent;
  color: $auth-primary;
  border: none;
  font-size: 28rpx;
  padding: 0;
  line-height: 1.6;
  &::after {
    border: none;
  }
  &.disabled {
    color: $auth-text-secondary;
  }
}
</style>
