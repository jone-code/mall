<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span>审计日志</span>
        <div class="filters">
          <el-select v-model="action" placeholder="操作类型" clearable filterable style="width: 150px" @change="reload">
            <el-option v-for="a in AUDIT_ACTIONS" :key="a.value" :label="a.label" :value="a.value" />
          </el-select>
          <el-select v-model="result" placeholder="结果" clearable style="width: 110px" @change="reload">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
          <el-input
            v-model="username"
            placeholder="操作人"
            clearable
            style="width: 130px"
            @keyup.enter="reload"
            @clear="reload"
          />
          <el-input
            v-model="targetId"
            placeholder="对象ID/订单号"
            clearable
            style="width: 150px"
            @keyup.enter="reload"
            @clear="reload"
          />
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="~"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 230px"
            @change="reload"
          />
          <el-button type="primary" @click="reload">查询</el-button>
          <el-button @click="exportCsv">导出 CSV</el-button>
        </div>
      </div>
    </template>

    <el-table v-loading="loading" :data="rows" stripe size="small">
      <template #empty>
        <TableEmpty description="暂无审计记录" />
      </template>
      <el-table-column label="时间" min-width="120">
        <template #default="{ row }">
          <RelativeTime :value="row.createdAt" />
        </template>
      </el-table-column>
      <el-table-column label="操作人" min-width="120">
        <template #default="{ row }">
          {{ row.username || '—' }}
          <span v-if="row.adminUserId" class="muted">#{{ row.adminUserId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="120">
        <template #default="{ row }">{{ auditActionLabel(row.action) }}</template>
      </el-table-column>
      <el-table-column label="对象" min-width="120">
        <template #default="{ row }">
          <span v-if="row.targetType">{{ row.targetType }}<span v-if="row.targetId">:{{ row.targetId }}</span></span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="requestIp" label="IP" min-width="120" />
      <el-table-column label="结果" width="80">
        <template #default="{ row }">
          <el-tag :type="row.result === 1 ? 'success' : 'danger'" size="small">
            {{ row.result === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="reload"
      />
    </div>
  </el-card>

  <el-dialog v-model="detailVisible" title="日志详情" width="600px">
    <template v-if="detail">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="时间">
          <RelativeTime :value="detail.createdAt" />
        </el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ detail.username || '—' }}
          <span v-if="detail.adminUserId" class="muted">#{{ detail.adminUserId }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作">{{ auditActionLabel(detail.action) }} ({{ detail.action }})</el-descriptions-item>
        <el-descriptions-item label="对象">
          {{ detail.targetType || '—' }}<span v-if="detail.targetId">:{{ detail.targetId }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="IP">{{ detail.requestIp || '—' }}</el-descriptions-item>
        <el-descriptions-item label="UA">{{ detail.userAgent || '—' }}</el-descriptions-item>
        <el-descriptions-item label="结果">
          <el-tag :type="detail.result === 1 ? 'success' : 'danger'" size="small">
            {{ detail.result === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.errorMsg" label="错误">{{ detail.errorMsg }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.requestBody" label="请求体">
          <pre class="body-pre">{{ detail.requestBody }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  listAuditLogs,
  exportAuditLogs,
  auditActionLabel,
  AUDIT_ACTIONS,
  type AuditLog
} from '@/api/audit'
import RelativeTime from '@/components/RelativeTime.vue'
import TableEmpty from '@/components/TableEmpty.vue'

const loading = ref(false)
const rows = ref<AuditLog[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const action = ref<string>('')
const result = ref<number | undefined>(undefined)
const username = ref<string>('')
const targetId = ref<string>('')
const dateRange = ref<[string, string] | null>(null)

const detailVisible = ref(false)
const detail = ref<AuditLog | null>(null)

async function load() {
  loading.value = true
  try {
    const res = await listAuditLogs({
      action: action.value || undefined,
      result: result.value,
      username: username.value || undefined,
      targetId: targetId.value || undefined,
      from: dateRange.value?.[0],
      to: dateRange.value?.[1],
      page: page.value,
      size: size.value
    })
    rows.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

async function exportCsv() {
  try {
    await exportAuditLogs({
      action: action.value || undefined,
      result: result.value,
      username: username.value || undefined,
      targetId: targetId.value || undefined,
      from: dateRange.value?.[0],
      to: dateRange.value?.[1]
    })
    ElMessage.success('导出成功')
  } catch (e: any) {
    ElMessage.error(e?.message || '导出失败')
  }
}

function openDetail(row: AuditLog) {
  detail.value = row
  detailVisible.value = true
}

onMounted(load)
</script>

<style scoped>
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.muted {
  color: #c0c4cc;
  margin-left: 4px;
}
.body-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 240px;
  overflow: auto;
}
</style>
