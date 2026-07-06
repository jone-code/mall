<template>
  <router-view />
</template>

<script setup lang="ts">
import { useEscapeClose } from '@/composables/useEscapeClose'

useEscapeClose()
</script>

<style>
.col-money {
  text-align: right;
  font-variant-numeric: tabular-nums;
}
</style>
