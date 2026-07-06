<template>
  <text class="type-badge" :style="{ borderColor: border, color: textColor }">{{ label }}</text>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { productTypeLabel } from '@/api/product'

const props = defineProps<{ type: string; small?: boolean }>()

const label = computed(() => productTypeLabel(props.type))

const border = computed(() => {
  if (props.type === 'VIRTUAL') return '#7e57c2'
  if (props.type === 'SERVICE') return '#d4a056'
  return '#5c6bc0'
})

const textColor = computed(() => border.value)
</script>

<style scoped lang="scss">
.type-badge {
  display: inline-block;
  font-size: 18rpx;
  padding: 4rpx 12rpx;
  border: 1rpx solid;
  letter-spacing: 2rpx;
  text-transform: uppercase;
  line-height: 1.2;
}
</style>
