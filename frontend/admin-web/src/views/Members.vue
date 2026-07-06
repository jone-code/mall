<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span>会员用户</span>
        <div class="filters">
          <el-input
            v-model="keyword"
            placeholder="搜索昵称 / 手机号"
            clearable
            style="width: 220px"
            @keyup.enter="reload"
            @clear="reload"
          />
          <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="reload">
            <el-option label="正常" :value="0" />
            <el-option label="禁用" :value="1" />
            <el-option label="注销" :value="2" />
          </el-select>
          <el-button type="primary" @click="reload">查询</el-button>
        </div>
      </div>
    </template>

    <el-table v-loading="loading" :data="rows" stripe>
      <template #empty>
        <TableEmpty description="暂无会员" />
      </template>
      <el-table-column prop="id" label="ID" width="72" />
      <el-table-column prop="nickname" label="昵称" min-width="140" />
      <el-table-column prop="phone" label="手机号" min-width="130">
        <template #default="{ row }">{{ row.phone || '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)" size="small">
            {{ memberStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" min-width="120">
        <template #default="{ row }">
          <RelativeTime :value="row.createdAt" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openDetail(row.id)">详情</el-button>
          <el-select
            v-if="canWrite"
            :model-value="row.status"
            size="small"
            style="width: 110px"
            @change="(v: number) => onStatusChange(row, v)"
          >
            <el-option label="正常" :value="0" />
            <el-option label="禁用" :value="1" />
            <el-option label="注销" :value="2" />
          </el-select>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="reload"
      />
    </div>
  </el-card>

  <el-drawer v-model="detailVisible" title="会员详情" size="480px">
    <template v-if="detail">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detail.nickname || '—' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.phone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.status)" size="small">
            {{ memberStatusLabel(detail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          <RelativeTime :value="detail.createdAt" />
        </el-descriptions-item>
      </el-descriptions>

      <div class="section-title">收货地址</div>
      <el-empty v-if="!detail.addresses?.length" description="暂无地址" :image-size="48" />
      <div v-else class="addr-list">
        <div v-for="addr in detail.addresses" :key="addr.id" class="addr-item">
          <el-tag v-if="addr.isDefault === 1" type="success" size="small">默认</el-tag>
          <div>{{ addr.receiver }} {{ addr.phone }}</div>
          <div class="muted">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail }}</div>
        </div>
      </div>

      <div class="section-title">近期订单</div>
      <el-empty v-if="!detail.recentOrders?.length" description="暂无订单" :image-size="48" />
      <el-table v-else :data="detail.recentOrders" size="small" stripe>
        <el-table-column label="订单号" min-width="170">
          <template #default="{ row }">
            <CopyText :text="row.orderNo" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">{{ formatOrderStatus(row.status) }}</template>
        </el-table-column>
        <el-table-column label="应付" width="90" align="right" class-name="col-money">
          <template #default="{ row }">¥{{ formatMoney(row.payAmount) }}</template>
        </el-table-column>
        <el-table-column label="时间" min-width="110">
          <template #default="{ row }">
            <RelativeTime :value="row.createdAt" />
          </template>
        </el-table-column>
      </el-table>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import CopyText from '@/components/CopyText.vue'
import RelativeTime from '@/components/RelativeTime.vue'
import TableEmpty from '@/components/TableEmpty.vue'
import { formatMoney } from '@/utils/format'
import {
  listMembers,
  getMemberDetail,
  updateMemberStatus,
  memberStatusLabel,
  formatOrderStatus,
  type MemberRow,
  type MemberDetail
} from '@/api/member'

const store = useAdminStore()
const canWrite = computed(() => store.hasPermission('member:write'))

const loading = ref(false)
const keyword = ref('')
const statusFilter = ref<number | undefined>()
const rows = ref<MemberRow[]>([])
const page = ref(1)
const size = ref(20)
const total = ref(0)

const detailVisible = ref(false)
const detail = ref<MemberDetail | null>(null)

function statusTagType(status?: number) {
  if (status === 0) return 'success'
  if (status === 1) return 'warning'
  return 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await listMembers({
      keyword: keyword.value || undefined,
      status: statusFilter.value,
      page: page.value,
      size: size.value
    })
    rows.value = res.data?.list || []
    total.value = res.data?.total || 0
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

async function openDetail(id: number) {
  try {
    const res = await getMemberDetail(id)
    detail.value = res.data
    detailVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '加载详情失败')
  }
}

async function onStatusChange(row: MemberRow, status: number) {
  if (row.status === status) return
  let reason = ''
  try {
    const { value } = await ElMessageBox.prompt(
      `确认将用户 ${row.nickname || row.id} 状态改为「${memberStatusLabel(status)}」？可填写变更原因（可选）`,
      '确认操作',
      {
        type: 'warning',
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputPlaceholder: '变更原因（可选）',
        inputValidator: () => true
      }
    )
    reason = value?.trim() || ''
  } catch {
    return
  }
  try {
    await updateMemberStatus(row.id, status, reason || undefined)
    row.status = status
    ElMessage.success('状态已更新')
  } catch {
    ElMessage.error('更新失败')
  }
}

onMounted(load)
</script>

<style scoped>
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
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
.section-title {
  margin: 20px 0 10px;
  font-weight: 600;
  font-size: 14px;
}
.addr-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.addr-item {
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 13px;
}
.muted {
  color: #909399;
  margin-top: 4px;
}
</style>
