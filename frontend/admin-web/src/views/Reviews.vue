<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span>用户评价</span>
        <div class="filters">
          <el-input
            v-model.number="filterSpuId"
            placeholder="商品ID"
            clearable
            style="width: 110px"
            @keyup.enter="reload"
            @clear="reload"
          />
          <el-select v-model="filterRating" placeholder="评分" clearable style="width: 100px" @change="reload">
            <el-option v-for="n in 5" :key="n" :label="`${n} 星`" :value="n" />
          </el-select>
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 110px" @change="reload">
            <el-option label="展示中" value="VISIBLE" />
            <el-option label="已隐藏" value="HIDDEN" />
          </el-select>
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
        </div>
      </div>
    </template>

    <el-table v-loading="loading" :data="rows" stripe size="small">
      <template #empty>
        <TableEmpty description="暂无评价" />
      </template>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderNo" label="订单号" min-width="160" />
      <el-table-column prop="spuId" label="商品ID" width="88" />
      <el-table-column prop="userNickname" label="用户" min-width="100" />
      <el-table-column label="评分" width="100">
        <template #default="{ row }">
          <span class="stars">{{ '★'.repeat(row.rating) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" min-width="180" show-overflow-tooltip />
      <el-table-column label="晒图" width="120">
        <template #default="{ row }">
          <div v-if="row.images?.length" class="review-images">
            <el-image
              v-for="(url, i) in row.images.slice(0, 3)"
              :key="i"
              :src="url"
              :preview-src-list="row.images"
              fit="cover"
              class="review-thumb"
            />
          </div>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'HIDDEN' ? 'info' : 'success'" size="small">
            {{ row.status === 'HIDDEN' ? '已隐藏' : '展示中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" min-width="120">
        <template #default="{ row }">
          <RelativeTime :value="row.createdAt" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="canWrite && row.status !== 'HIDDEN'"
            link
            type="warning"
            size="small"
            @click="onHide(row.id)"
          >
            隐藏
          </el-button>
          <el-button
            v-if="canWrite && row.status === 'HIDDEN'"
            link
            type="success"
            size="small"
            @click="onUnhide(row.id)"
          >
            恢复
          </el-button>
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
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hideReview, unhideReview, listReviews, type Review } from '@/api/review'
import RelativeTime from '@/components/RelativeTime.vue'
import TableEmpty from '@/components/TableEmpty.vue'
import { useAdminStore } from '@/stores/admin'

const store = useAdminStore()
const route = useRoute()
const canWrite = computed(() => store.hasPermission('order:write'))

const loading = ref(false)
const rows = ref<Review[]>([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const filterSpuId = ref<number | undefined>()
const filterRating = ref<number | undefined>()
const filterStatus = ref('')
const dateRange = ref<[string, string] | null>(null)

async function load() {
  loading.value = true
  try {
    const res = await listReviews({
      page: page.value,
      size: size.value,
      spuId: filterSpuId.value || undefined,
      rating: filterRating.value || undefined,
      status: filterStatus.value || undefined,
      from: dateRange.value?.[0],
      to: dateRange.value?.[1]
    })
    rows.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

async function onHide(id: number) {
  try {
    await ElMessageBox.confirm('隐藏后 C 端商品详情将不再展示该评价', '确认隐藏', { type: 'warning' })
    await hideReview(id)
    ElMessage.success('已隐藏')
    load()
  } catch (_) {
    /* cancel */
  }
}

async function onUnhide(id: number) {
  try {
    await unhideReview(id)
    ElMessage.success('已恢复展示')
    load()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(() => {
  const q = route.query
  if (q.rating) {
    const r = Number(q.rating)
    if (!Number.isNaN(r)) filterRating.value = r
  }
  load()
})
</script>

<style scoped>
.review-images {
  display: flex;
  gap: 4px;
}
.review-thumb {
  width: 36px;
  height: 36px;
  border-radius: 4px;
}
.muted {
  color: #909399;
}
</style>

<style scoped>
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}
.filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.stars {
  color: #e6a23c;
}
</style>
