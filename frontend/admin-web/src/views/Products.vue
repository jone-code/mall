<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span>商品管理</span>
        <div class="actions">
          <el-input
            v-model="keyword"
            placeholder="搜索标题"
            clearable
            style="width: 200px"
            @keyup.enter="search"
            @clear="search"
          />
          <el-select
            v-model="statusFilter"
            placeholder="状态"
            clearable
            style="width: 120px"
            @change="search"
          >
            <el-option label="草稿" :value="0" />
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="2" />
          </el-select>
          <el-button v-perm="'product:write'" type="primary" @click="openCreate">新建商品</el-button>
          <el-button v-perm.disable="'product:write'" v-if="selectedIds.length" @click="batchOffline">批量下架</el-button>
          <el-button v-perm.disable="'product:write'" v-if="selectedIds.length" type="success" @click="batchPublish">批量上架</el-button>
        </div>
      </div>
    </template>

    <el-table v-loading="loading" :data="rows" stripe @selection-change="onSelect">
      <template #empty>
        <TableEmpty description="暂无商品" />
      </template>
      <el-table-column v-if="canWrite" type="selection" width="48" />
      <el-table-column prop="id" label="ID" width="72" />
      <el-table-column label="封面" width="72">
        <template #default="{ row }">
          <el-image
            v-if="row.mainImage"
            :src="row.mainImage"
            style="width: 48px; height: 48px"
            fit="cover"
          />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column label="类型" width="88">
        <template #default="{ row }">{{ productTypeLabel(row.productType) }}</template>
      </el-table-column>
      <el-table-column label="价格" width="120" align="right" class-name="col-money">
        <template #default="{ row }">
          <span v-if="row.minPrice">
            {{ row.minPrice
            }}{{ row.maxPrice && row.maxPrice > row.minPrice ? ` - ${row.maxPrice}` : '' }}
          </span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="88">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" size="small">
            {{ spuStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row.id)">编辑</el-button>
          <el-button v-if="canWrite" link type="info" @click="onCopy(row.id)">复制</el-button>
          <template v-if="canWrite">
            <el-button v-if="row.status !== 1" link type="success" @click="onPublish(row.id)">
              上架
            </el-button>
            <el-button v-if="row.status === 1" link type="warning" @click="onOffline(row.id)">
              下架
            </el-button>
          </template>
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
        @size-change="onPageSizeChange"
      />
    </div>
  </el-card>

  <!-- 新建 -->
  <el-dialog v-model="createVisible" title="新建商品" width="560px" destroy-on-close>
    <el-form :model="createForm" label-width="100px">
      <el-form-item label="类目" required>
        <el-select v-model="createForm.categoryId" placeholder="选择二级类目" style="width: 100%">
          <el-option
            v-for="c in categoryOptions"
            :key="c.id"
            :label="c.label"
            :value="c.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="类型" required>
        <el-select v-model="createForm.productType" style="width: 100%">
          <el-option label="实物" value="PHYSICAL" />
          <el-option label="虚拟" value="VIRTUAL" />
          <el-option label="服务" value="SERVICE" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题" required>
        <el-input v-model="createForm.title" />
      </el-form-item>
      <el-form-item label="副标题">
        <el-input v-model="createForm.subtitle" />
      </el-form-item>
      <el-form-item label="主图" required>
        <div class="image-upload-row">
          <el-image v-if="createForm.mainImage" :src="createForm.mainImage" style="width: 72px; height: 72px" fit="cover" />
          <el-upload
            v-if="canWrite"
            :show-file-list="false"
            accept="image/*"
            :http-request="handleCreateUpload"
          >
            <el-button size="small">上传主图</el-button>
          </el-upload>
          <el-input v-model="createForm.mainImage" placeholder="或粘贴图片 URL" style="flex: 1" />
        </div>
      </el-form-item>
      <el-form-item label="售价" required>
        <el-input-number v-model="createForm.price" :min="0.01" :precision="2" />
      </el-form-item>
      <el-form-item label="库存">
        <el-input-number v-model="createForm.available" :min="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitCreate">创建</el-button>
    </template>
  </el-dialog>

  <!-- 编辑 -->
  <el-drawer v-model="editVisible" title="编辑商品" size="720px" destroy-on-close>
    <div v-if="editForm.id" v-loading="editLoading">
      <div class="edit-head">
        <el-tag :type="statusTag(editForm.status)" size="small">
          {{ spuStatusLabel(editForm.status) }}
        </el-tag>
        <span class="type-tag">{{ productTypeLabel(editForm.productType) }}</span>
        <span class="muted">ID {{ editForm.id }}</span>
      </div>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="editForm" label-width="100px" class="edit-form">
            <el-form-item label="类目" required>
              <el-select
                v-model="editForm.categoryId"
                placeholder="二级类目"
                style="width: 100%"
                :disabled="!canWrite"
              >
                <el-option
                  v-for="c in categoryOptions"
                  :key="c.id"
                  :label="c.label"
                  :value="c.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="标题" required>
              <el-input v-model="editForm.title" :disabled="!canWrite" />
            </el-form-item>
            <el-form-item label="副标题">
              <el-input v-model="editForm.subtitle" :disabled="!canWrite" />
            </el-form-item>
            <el-form-item label="主图" required>
              <div class="image-upload-row">
                <el-image v-if="editForm.mainImage" :src="editForm.mainImage" style="width: 72px; height: 72px" fit="cover" />
                <el-upload
                  v-if="canWrite"
                  :show-file-list="false"
                  accept="image/*"
                  :http-request="handleEditUpload"
                >
                  <el-button size="small">上传主图</el-button>
                </el-upload>
                <el-input v-model="editForm.mainImage" placeholder="或粘贴图片 URL" :disabled="!canWrite" style="flex: 1" />
              </div>
            </el-form-item>
            <el-form-item label="轮播图">
              <div v-if="canWrite" style="margin-bottom: 8px">
                <el-upload
                  :show-file-list="false"
                  accept="image/*"
                  :http-request="handleGalleryUpload"
                >
                  <el-button size="small">上传轮播图</el-button>
                </el-upload>
              </div>
              <el-input
                v-model="editForm.imagesText"
                type="textarea"
                :rows="3"
                placeholder="每行一个图片 URL"
                :disabled="!canWrite"
              />
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="editForm.sortOrder" :min="0" :disabled="!canWrite" />
            </el-form-item>
            <el-form-item label="详情 HTML">
              <el-input
                v-model="editForm.detailHtml"
                type="textarea"
                :rows="6"
                :disabled="!canWrite"
              />
            </el-form-item>
            <el-form-item v-if="canWrite">
              <el-button type="primary" :loading="savingBasic" @click="saveBasic">保存基本信息</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="SKU 规格" name="skus">
          <SkuEditor
            v-model="skuRows"
            :readonly="!canWrite"
            @remove="onSkuRemove"
          />
          <div v-if="canWrite" class="sku-actions">
            <el-button type="primary" :loading="savingSkus" @click="saveSkus">保存 SKU</el-button>
          </div>
        </el-tab-pane>

        <el-tab-pane label="库存记录" name="stock-flows">
          <div class="stock-flow-toolbar">
            <el-select v-model="stockFlowSkuId" placeholder="选择 SKU" style="width: 280px" @change="loadStockFlows">
              <el-option
                v-for="sku in skuRows.filter((s) => s.id)"
                :key="sku.id"
                :label="`${sku.id} · ${sku.specText || '默认规格'}`"
                :value="sku.id!"
              />
            </el-select>
            <el-button @click="loadStockFlows">刷新</el-button>
          </div>
          <el-table v-loading="stockFlowLoading" :data="stockFlows" size="small" stripe>
            <el-table-column prop="changeType" label="类型" width="110" />
            <el-table-column label="可用变动" width="90" align="right">
              <template #default="{ row }">{{ row.deltaAvailable ?? 0 }}</template>
            </el-table-column>
            <el-table-column label="变更后可用" width="100" align="right">
              <template #default="{ row }">{{ row.availableAfter ?? 0 }}</template>
            </el-table-column>
            <el-table-column prop="orderNo" label="订单号" min-width="160" />
            <el-table-column prop="operatorId" label="操作人" width="100" />
            <el-table-column label="时间" min-width="120">
              <template #default="{ row }">
                <RelativeTime :value="row.createdAt" />
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'
import {
  copyProduct,
  deleteSku,
  flattenLevel2Categories,
  getProduct,
  listCategories,
  listProducts,
  listStockFlows,
  productTypeLabel,
  saveSkus as saveSkusApi,
  spuStatusLabel,
  updateProduct,
  type AdminSpu,
  type CategoryNode,
  type StockFlowRow
} from '@/api/product'
import SkuEditor, {
  skuFromApi,
  skuToPayload,
  validateRows,
  type SkuRow
} from '@/components/SkuEditor.vue'
import RelativeTime from '@/components/RelativeTime.vue'
import { useAdminStore } from '@/stores/admin'
import { uploadImage } from '@/api/upload'
import TableEmpty from '@/components/TableEmpty.vue'
import type { UploadRequestOptions } from 'element-plus'

const store = useAdminStore()
const canWrite = computed(() => store.hasPermission('product:write'))

const loading = ref(false)
const saving = ref(false)
const savingBasic = ref(false)
const savingSkus = ref(false)
const editLoading = ref(false)
const keyword = ref('')
const statusFilter = ref<number | undefined>()
const rows = ref<AdminSpu[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const selectedIds = ref<number[]>([])
const categoryTree = ref<CategoryNode[]>([])
const categoryOptions = computed(() => flattenLevel2Categories(categoryTree.value))

const createVisible = ref(false)
const editVisible = ref(false)
const activeTab = ref('basic')
const stockFlowSkuId = ref<number | undefined>()
const stockFlows = ref<StockFlowRow[]>([])
const stockFlowLoading = ref(false)
const skuRows = ref<SkuRow[]>([])

const createForm = reactive({
  categoryId: undefined as number | undefined,
  productType: 'PHYSICAL',
  title: '',
  subtitle: '',
  mainImage: '',
  price: 99,
  available: 10
})

async function handleCreateUpload(opt: UploadRequestOptions) {
  return handleUpload(opt, 'create')
}

async function handleEditUpload(opt: UploadRequestOptions) {
  return handleUpload(opt, 'edit')
}

async function handleUpload(opt: UploadRequestOptions, target: 'create' | 'edit') {
  try {
    const res = await uploadImage(opt.file as File)
    const url = res.data?.url || ''
    if (target === 'create') createForm.mainImage = url
    else editForm.mainImage = url
    ElMessage.success('上传成功')
    opt.onSuccess?.(res.data)
  } catch (e: any) {
    ElMessage.error(e?.message || '上传失败')
    opt.onError?.(e)
  }
}

async function handleGalleryUpload(opt: UploadRequestOptions) {
  try {
    const res = await uploadImage(opt.file as File)
    const url = res.data?.url || ''
    const lines = editForm.imagesText.trim() ? editForm.imagesText.split('\n') : []
    lines.push(url)
    editForm.imagesText = lines.join('\n')
    ElMessage.success('已追加轮播图')
    opt.onSuccess?.(res.data)
  } catch (e: any) {
    ElMessage.error(e?.message || '上传失败')
    opt.onError?.(e)
  }
}

const editForm = reactive({
  id: 0,
  categoryId: undefined as number | undefined,
  title: '',
  subtitle: '',
  productType: '',
  mainImage: '',
  imagesText: '',
  detailHtml: '',
  sortOrder: 0,
  status: 0
})

function statusTag(s?: number) {
  if (s === 1) return 'success'
  if (s === 2) return 'info'
  return 'warning'
}

function parseImages(text: string) {
  return text
    .split('\n')
    .map((s) => s.trim())
    .filter(Boolean)
}

function imagesToText(images?: string[]) {
  return (images || []).join('\n')
}

async function loadCategories() {
  try {
    categoryTree.value = await listCategories()
    if (!createForm.categoryId && categoryOptions.value.length) {
      createForm.categoryId = categoryOptions.value[0].id
    }
  } catch {
    /* ignore */
  }
}

async function load() {
  loading.value = true
  try {
    const pageData = await listProducts({
      keyword: keyword.value || undefined,
      status: statusFilter.value,
      page: page.value,
      size: pageSize.value
    })
    rows.value = pageData?.list || []
    total.value = pageData?.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function onPageSizeChange() {
  page.value = 1
  load()
}

function search() {
  page.value = 1
  load()
}

function openCreate() {
  createVisible.value = true
}

async function submitCreate() {
  if (!createForm.categoryId || !createForm.title.trim()) {
    ElMessage.warning('请填写类目和标题')
    return
  }
  saving.value = true
  try {
    await http.post('/admin/products', {
      categoryId: createForm.categoryId,
      productType: createForm.productType,
      title: createForm.title.trim(),
      subtitle: createForm.subtitle || undefined,
      mainImage: createForm.mainImage,
      defaultSku: {
        specJson: { dims: [{ name: '规格', value: '默认' }] },
        price: createForm.price,
        available: createForm.available
      }
    })
    ElMessage.success('已创建')
    createVisible.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    saving.value = false
  }
}

async function openEdit(id: number) {
  editVisible.value = true
  editLoading.value = true
  activeTab.value = 'basic'
  stockFlows.value = []
  stockFlowSkuId.value = undefined
  try {
    const spu = await getProduct(id)
    editForm.id = spu.id
    editForm.categoryId = spu.categoryId
    editForm.title = spu.title || ''
    editForm.subtitle = spu.subtitle || ''
    editForm.productType = spu.productType || ''
    editForm.mainImage = spu.mainImage || ''
    editForm.imagesText = imagesToText(spu.images)
    editForm.detailHtml = spu.detailHtml || ''
    editForm.sortOrder = spu.sortOrder ?? 0
    editForm.status = spu.status ?? 0
    skuRows.value = (spu.skus || []).map(skuFromApi)
    if (!skuRows.value.length) {
      skuRows.value = [skuFromApi({ specJson: { dims: [{ name: '规格', value: '默认' }] }, price: 99, isDefault: 1, status: 1, available: 0 })]
    }
    const firstSku = skuRows.value.find((s) => s.id)
    if (firstSku?.id) {
      stockFlowSkuId.value = firstSku.id
      await loadStockFlows()
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
    editVisible.value = false
  } finally {
    editLoading.value = false
  }
}

async function onCopy(id: number) {
  try {
    await ElMessageBox.confirm('将复制该商品为草稿（含 SKU 与库存），是否继续？', '复制商品', {
      type: 'info'
    })
  } catch {
    return
  }
  try {
    const result = await copyProduct(id)
    ElMessage.success(`已复制，新商品 ID ${result?.spuId}`)
    await load()
    if (result?.spuId) {
      openEdit(result.spuId)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '复制失败')
  }
}

async function loadStockFlows() {
  if (!stockFlowSkuId.value) {
    stockFlows.value = []
    return
  }
  stockFlowLoading.value = true
  try {
    const res = await listStockFlows(stockFlowSkuId.value, { page: 1, size: 50 })
    stockFlows.value = res?.list || []
  } catch (e: any) {
    ElMessage.error(e?.message || '加载库存记录失败')
  } finally {
    stockFlowLoading.value = false
  }
}

async function saveBasic() {
  if (!editForm.categoryId || !editForm.title.trim() || !editForm.mainImage.trim()) {
    ElMessage.warning('类目、标题、主图不能为空')
    return
  }
  savingBasic.value = true
  try {
    await updateProduct(editForm.id, {
      categoryId: editForm.categoryId,
      title: editForm.title.trim(),
      subtitle: editForm.subtitle || undefined,
      mainImage: editForm.mainImage.trim(),
      images: parseImages(editForm.imagesText),
      detailHtml: editForm.detailHtml || undefined,
      sortOrder: editForm.sortOrder
    })
    ElMessage.success('基本信息已保存')
    await load()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    savingBasic.value = false
  }
}

async function onSkuRemove(row: SkuRow) {
  if (row.id) {
    try {
      await deleteSku(row.id)
    } catch (e: any) {
      ElMessage.error(e.message || '删除失败')
      return
    }
  }
  skuRows.value = skuRows.value.filter((r) => r._key !== row._key)
  if (!skuRows.value.some((r) => r.isDefault === 1) && skuRows.value.length) {
    skuRows.value[0].isDefault = 1
  }
}

async function saveSkus() {
  const err = validateRows(skuRows.value)
  if (err) {
    ElMessage.warning(err)
    return
  }
  savingSkus.value = true
  try {
    await saveSkusApi(
      editForm.id,
      skuRows.value.map(skuToPayload)
    )
    ElMessage.success('SKU 已保存')
    await openEdit(editForm.id)
    await load()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    savingSkus.value = false
  }
}

async function onPublish(id: number) {
  try {
    await http.post(`/admin/products/${id}/publish`)
    ElMessage.success('已上架')
    await load()
    if (editVisible.value && editForm.id === id) await openEdit(id)
  } catch (e: any) {
    ElMessage.error(e.message || '上架失败')
  }
}

async function onOffline(id: number) {
  try {
    await http.post(`/admin/products/${id}/offline`)
    ElMessage.success('已下架')
    await load()
    if (editVisible.value && editForm.id === id) await openEdit(id)
  } catch (e: any) {
    ElMessage.error(e.message || '下架失败')
  }
}

function onSelect(list: AdminSpu[]) {
  selectedIds.value = list.map((r) => r.id)
}

async function batchPublish() {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm(`确认批量上架 ${selectedIds.value.length} 个商品？`, '批量上架', { type: 'warning' })
  } catch {
    return
  }
  try {
    const res = await http.post<{ count: number }>('/admin/products/batch-status', {
      ids: selectedIds.value,
      action: 'publish'
    })
    ElMessage.success(`已上架 ${res.data?.count ?? 0} 个`)
    selectedIds.value = []
    await load()
  } catch (e: any) {
    ElMessage.error(e.message || '批量上架失败')
  }
}

async function batchOffline() {
  if (!selectedIds.value.length) return
  try {
    await ElMessageBox.confirm(`确认批量下架 ${selectedIds.value.length} 个商品？`, '批量下架', { type: 'warning' })
  } catch {
    return
  }
  try {
    const res = await http.post<{ count: number }>('/admin/products/batch-status', {
      ids: selectedIds.value,
      action: 'offline'
    })
    ElMessage.success(`已下架 ${res.data?.count ?? 0} 个`)
    selectedIds.value = []
    await load()
  } catch (e: any) {
    ElMessage.error(e.message || '批量下架失败')
  }
}

onMounted(async () => {
  await loadCategories()
  await load()
})
</script>

<style scoped>
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.edit-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}
.type-tag {
  font-size: 13px;
  color: #666;
}
.muted {
  color: #999;
  font-size: 12px;
}
.edit-form {
  max-width: 640px;
}
.sku-actions {
  margin-top: 16px;
}
.stock-flow-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}
  margin-top: 16px;
}
.image-upload-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}
</style>
