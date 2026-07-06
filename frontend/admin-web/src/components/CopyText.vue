<template>
  <span class="copy-text" :title="title || '点击复制'" @click.stop="onCopy">
    <span class="copy-text__value">{{ text }}</span>
    <el-icon class="copy-text__icon"><DocumentCopy /></el-icon>
  </span>
</template>

<script setup lang="ts">
import { DocumentCopy } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { copyToClipboard } from '@/utils/format'

const props = defineProps<{
  text: string
  title?: string
  successMsg?: string
}>()

async function onCopy() {
  const ok = await copyToClipboard(props.text)
  if (ok) {
    ElMessage.success(props.successMsg || '已复制')
  } else {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.copy-text {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: inherit;
}
.copy-text:hover .copy-text__icon {
  color: var(--admin-color-accent);
}
.copy-text__icon {
  font-size: 14px;
  color: #c0c4cc;
  flex-shrink: 0;
}
</style>
