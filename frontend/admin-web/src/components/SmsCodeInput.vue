<template>
  <div class="sms-code-input" :class="{ shake: shaking }">
    <input
      v-for="(_, i) in length"
      :key="i"
      ref="inputs"
      class="cell"
      type="text"
      inputmode="numeric"
      autocomplete="one-time-code"
      maxlength="1"
      :value="digits[i] || ''"
      @input="onInput(i, $event)"
      @keydown="onKeyDown(i, $event)"
      @paste="onPaste"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'

const props = defineProps<{
  modelValue: string
  length?: number
  error?: boolean
}>()
const emit = defineEmits<{
  (e: 'update:modelValue', v: string): void
  (e: 'complete', v: string): void
}>()

const length = props.length ?? 6
const inputs = ref<HTMLInputElement[]>([])
const shaking = ref(false)

const digits = computed(() => props.modelValue.split(''))

watch(
  () => props.error,
  (v) => {
    if (v) {
      shaking.value = true
      setTimeout(() => (shaking.value = false), 400)
    }
  }
)

function commit(next: string) {
  const trimmed = next.replace(/\D/g, '').slice(0, length)
  emit('update:modelValue', trimmed)
  if (trimmed.length === length) emit('complete', trimmed)
}

function onInput(idx: number, ev: Event) {
  const el = ev.target as HTMLInputElement
  const ch = el.value.replace(/\D/g, '').slice(-1)
  const arr = props.modelValue.split('')
  arr[idx] = ch
  // fill empty slots before idx with ''
  for (let i = 0; i < idx; i++) if (arr[i] === undefined) arr[i] = ''
  const next = arr.join('').slice(0, length)
  commit(next)
  if (ch && idx < length - 1) {
    nextTick(() => inputs.value[idx + 1]?.focus())
  }
}

function onKeyDown(idx: number, ev: KeyboardEvent) {
  if (ev.key === 'Backspace' && !(ev.target as HTMLInputElement).value && idx > 0) {
    inputs.value[idx - 1]?.focus()
  }
}

function onPaste(ev: ClipboardEvent) {
  const text = ev.clipboardData?.getData('text') || ''
  if (text) {
    ev.preventDefault()
    commit(text)
    nextTick(() => {
      const last = Math.min(text.replace(/\D/g, '').length, length) - 1
      if (last >= 0) inputs.value[last]?.focus()
    })
  }
}
</script>

<style scoped>
.sms-code-input {
  display: flex;
  gap: 8px;
}
.cell {
  width: 40px;
  height: 48px;
  text-align: center;
  font-size: 22px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  outline: none;
  background: #fff;
  transition: border-color 0.2s;
}
.cell:focus {
  border-color: var(--admin-color-primary);
}
.shake {
  animation: shake 0.3s;
}
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-6px); }
  75% { transform: translateX(6px); }
}
</style>
