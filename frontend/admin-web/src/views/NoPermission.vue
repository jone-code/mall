<template>
  <div class="np">
    <el-result icon="warning" title="403" sub-title="您没有访问该资源的权限">
      <template #extra>
        <el-button type="primary" @click="goBack">返回主控台</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
function goBack() {
  router.replace('/dashboard')
}
</script>

<style scoped>
.np {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--admin-color-bg);
}
</style>
