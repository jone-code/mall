<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span>虚拟卡密池</span>
        <div class="actions">
          <el-tag type="success">可用 {{ stats.available ?? 0 }}</el-tag>
          <el-select
            v-model="filterSpuId"
            placeholder="按商品筛选"
            clearable
            filterable
            style="width: 220px"
            @change="reload"
          >
            <el-option
              v-for="p in virtualProducts"
              :key="p.id"
              :label="`${p.id} · ${p.title}`"
              :value="p.id"
            />
          </el-select>
          <el-select v-model="status" placeholder="状态" clearable style="width: 120px" @change="reload">
            <el-option label="可用" value="AVAILABLE" />
            <el-option label="已发放" value="ISSUED" />
          </el-select>
          <el-button @click="load">刷新</el-button>
          <template v-if="canImport">
            <el-button type="primary" @click="openAdd">新增卡密</el-button>
            <el-button @click="importVisible = true">批量导入</el-button>
          </template>
          <el-tooltip v-else content="需要 card:import 权限">
            <el-tag type="info">只读</el-tag>
          </el-tooltip>
        </div>
      </div>
    </template>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="卡密必须绑定虚拟商品；用户支付成功后从池中分配，无可用卡密时无法完成虚拟商品履约。"
      style="margin-bottom: 16px"
    />

    <el-table
      v-if="poolSummary.length"
      :data="poolSummary"
      size="small"
      border
      style="margin-bottom: 16px"
    >
      <el-table-column label="虚拟商品" min-width="200">
        <template #default="{ row }">{{ spuLabel(row.spuId) }}</template>
      </el-table-column>
      <el-table-column prop="available" label="可用" width="80" />
      <el-table-column prop="issued" label="已发放" width="88" />
      <el-table-column prop="total" label="合计" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.available > 0 ? 'success' : 'danger'" size="small">
            {{ row.available > 0 ? '正常' : '耗尽' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-table v-loading="loading" :data="rows" stripe>
      <template #empty>
        <TableEmpty description="暂无卡密" />
      </template>
      <el-table-column prop="id" label="ID" width="72" />
      <el-table-column label="虚拟商品" min-width="180">
        <template #default="{ row }">
          {{ spuLabel(row.spuId) }}
        </template>
      </el-table-column>
      <el-table-column prop="cardNo" label="卡号" min-width="160" />
      <el-table-column label="卡密" min-width="180">
        <template #default="{ row }">
          <span>{{ revealedIds.has(row.id) ? row.cardSecret : maskSecret(row.cardSecret) }}</span>
          <el-button
            v-if="canImport && row.cardSecret"
            link
            type="primary"
            size="small"
            style="margin-left: 6px"
            @click="toggleReveal(row.id)"
          >
            {{ revealedIds.has(row.id) ? '隐藏' : '查看' }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="88">
        <template #default="{ row }">
          <el-tag :type="row.status === 'AVAILABLE' ? 'success' : 'info'" size="small">
            {{ row.status === 'AVAILABLE' ? '可用' : '已发放' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column prop="issuedAt" label="发放时间" min-width="168" />
      <el-table-column v-if="canImport" label="操作" width="88" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 'AVAILABLE'"
            link
            type="primary"
            @click="openEdit(row)"
          >
            编辑
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="reload"
      />
    </div>
  </el-card>

  <el-dialog v-model="formVisible" :title="editingId ? '编辑卡密' : '新增卡密'" width="480px" @closed="resetForm">
    <el-form label-width="96px">
      <el-form-item label="虚拟商品" required>
        <el-select v-model="form.spuId" placeholder="请选择虚拟商品" filterable style="width: 100%">
          <el-option
            v-for="p in virtualProducts"
            :key="p.id"
            :label="`${p.id} · ${p.title}`"
            :value="p.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="卡号" required>
        <el-input v-model="form.cardNo" placeholder="必填，全局唯一" clearable />
      </el-form-item>
      <el-form-item label="卡密" :required="!editingId">
        <el-input
          v-model="form.cardSecret"
          :placeholder="editingId ? '留空则不修改' : '留空则自动生成 16 位数字'"
          clearable
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="importVisible" title="批量导入卡密" width="520px" @closed="importResult = null">
    <el-form label-width="96px" style="margin-bottom: 12px">
      <el-form-item label="虚拟商品" required>
        <el-select v-model="importSpuId" placeholder="请选择虚拟商品" filterable style="width: 100%">
          <el-option
            v-for="p in virtualProducts"
            :key="p.id"
            :label="`${p.id} · ${p.title}`"
            :value="p.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <el-input
      v-model="importText"
      type="textarea"
      :rows="8"
      placeholder="每行一条，格式：卡号,卡密（卡密可省略自动生成）"
    />
    <el-alert
      v-if="importResult"
      :type="importResult.imported > 0 ? 'success' : 'warning'"
      :closable="false"
      show-icon
      style="margin-top: 12px"
      :title="`成功 ${importResult.imported}，重复 ${importResult.duplicate}，跳过 ${importResult.skipped}`"
    />
    <template #footer>
      <el-button @click="importVisible = false">取消</el-button>
      <el-button type="primary" :loading="importing" @click="submitImport">导入</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import TableEmpty from '@/components/TableEmpty.vue'
import { listProducts, type AdminSpu } from '@/api/product'
import {
  listVirtualCards,
  getVirtualCardStats,
  getVirtualCardPoolSummary,
  importVirtualCards,
  updateVirtualCard,
  maskSecret,
  type VirtualCard,
  type VirtualCardPool,
  type ImportCardsResult
} from '@/api/card'

const store = useAdminStore()
const canImport = computed(() => store.hasPermission('card:import'))

const loading = ref(false)
const rows = ref<VirtualCard[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const status = ref('')
const filterSpuId = ref<number | undefined>()
const stats = ref({ available: 0 })
const poolSummary = ref<VirtualCardPool[]>([])
const virtualProducts = ref<AdminSpu[]>([])
const revealedIds = ref(new Set<number>())

const formVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = ref({ spuId: undefined as number | undefined, cardNo: '', cardSecret: '' })

const importVisible = ref(false)
const importText = ref('')
const importSpuId = ref<number | undefined>()
const importing = ref(false)
const importResult = ref<ImportCardsResult | null>(null)

function toggleReveal(id: number) {
  const next = new Set(revealedIds.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  revealedIds.value = next
}

function spuLabel(spuId?: number) {
  if (!spuId) return '—'
  const p = virtualProducts.value.find((x) => x.id === spuId)
  return p ? `${p.id} · ${p.title}` : String(spuId)
}

async function loadVirtualProducts() {
  const data = await listProducts({ productType: 'VIRTUAL', page: 1, size: 200 })
  virtualProducts.value = data?.list || []
}

async function load() {
  loading.value = true
  try {
    const params: { status?: string; spuId?: number; page?: number; size?: number } = {
      page: page.value,
      size: pageSize.value
    }
    if (status.value) params.status = status.value
    if (filterSpuId.value) params.spuId = filterSpuId.value
    const [listRes, statsRes, poolRes] = await Promise.all([
      listVirtualCards(params),
      getVirtualCardStats(filterSpuId.value),
      getVirtualCardPoolSummary()
    ])
    rows.value = listRes.data?.list || []
    total.value = listRes.data?.total || 0
    stats.value = statsRes.data || { available: 0 }
    poolSummary.value = poolRes.data || []
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

function resetForm() {
  editingId.value = null
  form.value = { spuId: undefined, cardNo: '', cardSecret: '' }
}

function openAdd() {
  resetForm()
  form.value.spuId = filterSpuId.value
  formVisible.value = true
}

function openEdit(row: VirtualCard) {
  editingId.value = row.id
  form.value = {
    spuId: row.spuId,
    cardNo: row.cardNo,
    cardSecret: ''
  }
  formVisible.value = true
}

async function submitForm() {
  if (!form.value.spuId) {
    ElMessage.warning('请选择虚拟商品')
    return
  }
  const cardNo = form.value.cardNo.trim()
  if (!cardNo) {
    ElMessage.warning('请填写卡号')
    return
  }
  if (!editingId.value && !form.value.cardSecret.trim()) {
    // allow auto-generate on create
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateVirtualCard(editingId.value, {
        spuId: form.value.spuId,
        cardNo,
        cardSecret: form.value.cardSecret.trim() || undefined
      })
      ElMessage.success('已保存')
    } else {
      const res = await importVirtualCards({
        spuId: form.value.spuId,
        cards: [{ cardNo, cardSecret: form.value.cardSecret.trim() || undefined }]
      })
      const n = res.data?.imported ?? 0
      if (n === 0) {
        ElMessage.warning('未导入（卡号可能已存在）')
        return
      }
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    load()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function submitImport() {
  if (!importSpuId.value) {
    ElMessage.warning('请选择虚拟商品')
    return
  }
  const lines = importText.value.split('\n').map((l) => l.trim()).filter(Boolean)
  const cards = lines.map((line) => {
    const [cardNo, cardSecret] = line.split(/[,，\t]/).map((s) => s.trim())
    return { cardNo, cardSecret: cardSecret || undefined }
  })
  if (!cards.length) {
    ElMessage.warning('请填写卡密')
    return
  }
  try {
    await ElMessageBox.confirm(`确认导入 ${cards.length} 条卡密？`, '批量导入', { type: 'warning' })
  } catch {
    return
  }
  importing.value = true
  importResult.value = null
  try {
    const res = await importVirtualCards({ spuId: importSpuId.value, cards })
    importResult.value = res.data || null
    const n = res.data?.imported ?? 0
    if (n > 0) {
      ElMessage.success(`成功导入 ${n} 条`)
      importVisible.value = false
      importText.value = ''
      load()
    } else {
      ElMessage.warning(`未导入新卡密（重复 ${res.data?.duplicate ?? 0}）`)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '导入失败')
  } finally {
    importing.value = false
  }
}

onMounted(async () => {
  await loadVirtualProducts()
  await load()
})
</script>

<style scoped>
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
