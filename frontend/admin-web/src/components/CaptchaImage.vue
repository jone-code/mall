<template>
  <div class="captcha-image" @click="refresh" :title="'点击刷新'">
    <img v-if="src" :src="src" alt="captcha" />
    <span v-else class="placeholder">加载中…</span>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import http from '@/api/http'

interface CaptchaResp {
  captchaId: string
  image: string // base64 dataURL or url
}

const emit = defineEmits<{ (e: 'update:captchaId', id: string): void }>()

const src = ref('')

async function refresh() {
  src.value = ''
  try {
    const { data } = await http.get<CaptchaResp>('/admin/captcha')
    const image = data.image
    src.value =
      image && !image.startsWith('data:') ? `data:image/png;base64,${image}` : image
    emit('update:captchaId', data.captchaId)
  } catch (_) {
    src.value =
      'data:image/svg+xml;utf8,' +
      encodeURIComponent(
        `<svg xmlns="http://www.w3.org/2000/svg" width="100" height="36"><rect width="100%" height="100%" fill="#fef0f0"/><text x="50%" y="55%" font-size="12" text-anchor="middle" fill="#f56c6c">加载失败，点击重试</text></svg>`
      )
    emit('update:captchaId', '')
  }
}

onMounted(refresh)

defineExpose({ refresh })
</script>

<style scoped>
.captcha-image {
  width: 100px;
  height: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  background: #fafafa;
}
.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.placeholder {
  font-size: 12px;
  color: #999;
}
</style>
