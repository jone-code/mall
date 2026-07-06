<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span>登录设备</span>
        <el-button @click="load">刷新</el-button>
      </div>
    </template>

    <el-table v-loading="loading" :data="rows" stripe>
      <template #empty>
        <TableEmpty description="暂无活跃会话" />
      </template>
      <el-table-column label="会话 ID" min-width="220">
        <template #default="{ row }">
          <span class="mono">{{ row.sid }}</span>
          <el-tag v-if="row.current" type="success" size="small" style="margin-left: 8px">当前</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="140">
        <template #default="{ row }">
          <RelativeTime :value="row.createdAt ? new Date(row.createdAt).toISOString() : undefined" />
        </template>
      </el-table-column>
      <el-table-column label="最近活跃" min-width="140">
        <template #default="{ row }">
          <RelativeTime :value="row.lastActiveAt ? new Date(row.lastActiveAt).toISOString() : undefined" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="!row.current"
            link
            type="danger"
            size="small"
            @click="onKick(row.sid)"
          >
            踢下线
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import TableEmpty from '@/components/TableEmpty.vue'
import RelativeTime from '@/components/RelativeTime.vue'
import { kickAdminSession, listAdminSessions, type AdminSession } from '@/api/adminSession'

const loading = ref(false)
const rows = ref<AdminSession[]>([])

async function load() {
  loading.value = true
  try {
    rows.value = (await listAdminSessions()) || []
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function onKick(sid: string) {
  try {
    await ElMessageBox.confirm('确认踢下该设备？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await kickAdminSession(sid)
    ElMessage.success('已踢下线')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(load)
</script>

<style scoped>
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.mono {
  font-family: ui-monospace, monospace;
  font-size: 12px;
}
</style>
