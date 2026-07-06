<template>
  <div class="password-input">
    <el-input
      :model-value="modelValue"
      :type="visible ? 'text' : 'password'"
      :placeholder="placeholder"
      autocomplete="current-password"
      @update:model-value="onInput"
    >
      <template #suffix>
        <el-icon class="eye" @click="visible = !visible">
          <View v-if="visible" />
          <Hide v-else />
        </el-icon>
      </template>
    </el-input>
    <div v-if="showStrength" class="strength">
      <div class="bar">
        <div
          class="fill"
          :style="{ width: strength.percent + '%', background: strength.color }"
        />
      </div>
      <span class="label" :style="{ color: strength.color }">密码强度：{{ strength.label }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { View, Hide } from '@element-plus/icons-vue'

const props = withDefaults(
  defineProps<{
    modelValue: string
    placeholder?: string
    showStrength?: boolean
  }>(),
  { placeholder: '请输入密码', showStrength: false }
)
const emit = defineEmits<{ (e: 'update:modelValue', v: string): void }>()

const visible = ref(false)

function onInput(v: string | number) {
  emit('update:modelValue', String(v))
}

function score(p: string): number {
  let s = 0
  if (!p) return 0
  if (p.length >= 10) s++
  if (/[a-z]/.test(p) && /[A-Z]/.test(p)) s++
  if (/\d/.test(p)) s++
  if (/[^A-Za-z0-9]/.test(p)) s++
  if (p.length >= 14) s++
  return Math.min(s, 4)
}

const strength = computed(() => {
  const s = score(props.modelValue)
  const labels = ['极弱', '弱', '一般', '强', '极强']
  const colors = ['#f56c6c', '#f56c6c', '#e6a23c', '#67c23a', '#67c23a']
  const percent = (s / 4) * 100
  return { label: labels[s], color: colors[s], percent }
})
</script>

<style scoped>
.eye {
  cursor: pointer;
  color: #999;
}
.strength {
  margin-top: 6px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.bar {
  width: 100%;
  height: 4px;
  background: #ebeef5;
  border-radius: 2px;
  overflow: hidden;
}
.fill {
  height: 100%;
  transition: width 0.3s, background 0.3s;
}
.label {
  font-size: 12px;
}
</style>
