<template>
  <span v-if="visible" class="env-badge" :class="envClass">{{ label }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const raw = (import.meta.env.VITE_ENV || '').toLowerCase()
const isDev = import.meta.env.DEV

const visible = computed(() => {
  if (isDev && raw !== 'prod' && raw !== 'production') return true
  return raw === 'test' || raw === 'staging' || raw === 'pre' || raw === 'dev' || raw === 'development'
})

const label = computed(() => {
  if (raw === 'staging' || raw === 'pre') return 'PRE'
  if (raw === 'test') return 'TEST'
  if (raw === 'dev' || raw === 'development') return 'DEV'
  if (isDev) return 'DEV'
  return raw.toUpperCase() || 'DEV'
})

const envClass = computed(() => {
  const l = label.value
  if (l === 'DEV') return 'is-dev'
  if (l === 'PRE') return 'is-pre'
  return 'is-test'
})
</script>

<style scoped>
.env-badge {
  display: inline-block;
  padding: 2px 10px;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  border-radius: 4px;
  letter-spacing: 1px;
}
.env-badge.is-test {
  background: var(--admin-color-sale);
}
.env-badge.is-pre {
  background: var(--admin-color-accent);
  color: var(--admin-color-primary);
}
.env-badge.is-dev {
  background: var(--admin-color-primary);
}
</style>
