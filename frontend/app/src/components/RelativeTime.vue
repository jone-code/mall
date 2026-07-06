<template>
  <text class="relative-time" @longpress="showAbsolute">{{ display }}</text>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { formatDateTime, relativeTime } from "@/utils/datetime";

const props = defineProps<{
  value: unknown;
  mode?: "relative" | "absolute";
}>();

const display = computed(() =>
  props.mode === "absolute"
    ? formatDateTime(props.value)
    : relativeTime(props.value)
);

function showAbsolute() {
  uni.showToast({
    title: formatDateTime(props.value),
    icon: "none",
  });
}
</script>

<style scoped lang="scss">
.relative-time {
  color: inherit;
}
</style>
