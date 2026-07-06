<template>
  <el-card>
    <el-empty :description="`${title}功能开发中`" />
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '该模块')
</script>
