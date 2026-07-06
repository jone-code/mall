<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span>服务核销码池</span>
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
              v-for="p in serviceProducts"
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
            <el-button type="primary" @click="openAdd">新增核销码</el-button>
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
      title="核销码须绑定服务商品；用户支付后从池中分配，到店出示后在「服务核销」页扫码/输入完成核销。"
      style="margin-bottom: 16px"
    />

    <el-table v-loading="loading" :data="rows" stripe>
      <template #empty>
        <TableEmpty description="暂无核销码" />
      </template>
      <el-table-column prop="id" label="ID" width="72" />
      <el-table-column label="服务商品" min-width="180">
        <template #default="{ row }">{{ spuLabel(row.spuId) }}</template>
      </el-table-column>
      <el-table-column prop="verifyCode" label="核销码" min-width="160" />
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
          <el-button v-if="row.status === 'AVAILABLE'" link type="primary" @click="openEdit(row)">
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

  <el-dialog v-model="formVisible" :title="editingId ? '编辑核销码' : '新增核销码'" width="480px" @closed="resetForm">
    <el-form label-width="96px">
      <el-form-item label="服务商品" required>
        <el-select v-model="form.spuId" placeholder="请选择服务商品" filterable style="width: 100%">
          <el-option
            v-for="p in serviceProducts"
            :key="p.id"
            :label="`${p.id} · ${p.title}`"
            :value="p.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="核销码" required>
        <el-input
          v-model="form.verifyCode"
          :placeholder="editingId ? '必填' : '留空则自动生成 SVC 开头码'"
          clearable
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="importVisible" title="批量导入核销码" width="520px">
    <el-form label-width="96px" style="margin-bottom: 12px">
      <el-form-item label="服务商品" required>
        <el-select v-model="importSpuId" placeholder="请选择服务商品" filterable style="width: 100%">
          <el-option
            v-for="p in serviceProducts"
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
      placeholder="每行一个核销码；留空行会自动跳过。也可留空由系统生成（每行留空则自动生成一条）"
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
  listServiceVerifyCodes,
  getServiceVerifyCodeStats,
  importServiceVerifyCodes,
  updateServiceVerifyCode,
  type ServiceVerifyCode
} from '@/api/serviceCode'

const store = useAdminStore()
const canImport = computed(() => store.hasPermission('card:import'))

const loading = ref(false)
const rows = ref<ServiceVerifyCode[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const status = ref('')
const filterSpuId = ref<number | undefined>()
const stats = ref({ available: 0 })
const serviceProducts = ref<AdminSpu[]>([])

const formVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = ref({ spuId: undefined as number | undefined, verifyCode: '' })

const importVisible = ref(false)
const importText = ref('')
const importSpuId = ref<number | undefined>()
const importing = ref(false)

function spuLabel(spuId?: number) {
  if (!spuId) return '—'
  const p = serviceProducts.value.find((x) => x.id === spuId)
  return p ? `${p.id} · ${p.title}` : String(spuId)
}

async function loadServiceProducts() {
  const data = await listProducts({ productType: 'SERVICE', page: 1, size: 200 })
  serviceProducts.value = data?.list || []
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
    const [listRes, statsRes] = await Promise.all([
      listServiceVerifyCodes(params),
      getServiceVerifyCodeStats(filterSpuId.value)
    ])
    rows.value = listRes.data?.list || []
    total.value = listRes.data?.total || 0
    stats.value = statsRes.data || { available: 0 }
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
  form.value = { spuId: undefined, verifyCode: '' }
}

function openAdd() {
  resetForm()
  form.value.spuId = filterSpuId.value
  formVisible.value = true
}

function openEdit(row: ServiceVerifyCode) {
  editingId.value = row.id
  form.value = { spuId: row.spuId, verifyCode: row.verifyCode }
  formVisible.value = true
}

async function submitForm() {
  if (!form.value.spuId) {
    ElMessage.warning('请选择服务商品')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      const code = form.value.verifyCode.trim()
      if (!code) {
        ElMessage.warning('请填写核销码')
        return
      }
      await updateServiceVerifyCode(editingId.value, {
        spuId: form.value.spuId,
        verifyCode: code
      })
      ElMessage.success('已保存')
    } else {
      const res = await importServiceVerifyCodes({
        spuId: form.value.spuId,
        codes: [{ verifyCode: form.value.verifyCode.trim() || undefined }]
      })
      const n = res.data?.imported ?? 0
      if (n === 0) {
        ElMessage.warning('未导入（核销码可能已存在）')
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
    ElMessage.warning('请选择服务商品')
    return
  }
  const lines = importText.value.split('\n').map((l) => l.trim())
  const codes = lines.map((line) => ({ verifyCode: line || undefined }))
  if (!codes.length) {
    ElMessage.warning('请填写核销码')
    return
  }
  try {
    await ElMessageBox.confirm(`确认导入 ${codes.length} 条核销码？`, '批量导入', { type: 'warning' })
  } catch {
    return
  }
  importing.value = true
  try {
    const res = await importServiceVerifyCodes({ spuId: importSpuId.value, codes })
    ElMessage.success(`成功导入 ${res.data?.imported ?? 0} 条`)
    importVisible.value = false
    importText.value = ''
    load()
  } catch (e: any) {
    ElMessage.error(e?.message || '导入失败')
  } finally {
    importing.value = false
  }
}

onMounted(async () => {
  await loadServiceProducts()
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
