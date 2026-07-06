<template>
  <span :title="absolute">{{ relativeTime(value) }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatDateTime, relativeTime } from '@/utils/format'

const props = defineProps<{
  value?: string | null
}>()

const absolute = computed(() => formatDateTime(props.value))
</script>
