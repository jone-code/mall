<template>
  <div class="sku-editor">
    <div class="toolbar">
      <el-button v-if="!readonly" type="primary" size="small" @click="addRow">添加 SKU</el-button>
      <span class="hint">规格名/值各 1~32 字；保存后服务端生成 spec_text</span>
    </div>

    <el-table :data="rows" border size="small" class="sku-table">
      <el-table-column label="默认" width="56" align="center">
        <template #default="{ row }">
          <el-radio
            :model-value="defaultKey"
            :value="row._key"
            :disabled="readonly"
            @update:model-value="onDefaultChange"
          />
        </template>
      </el-table-column>

      <el-table-column label="规格维度" min-width="220">
        <template #default="{ row }">
          <div v-for="(dim, idx) in row.dims" :key="idx" class="dim-row">
            <el-input
              v-model="dim.name"
              placeholder="维度名"
              size="small"
              :disabled="readonly"
              style="width: 88px"
            />
            <el-input
              v-model="dim.value"
              placeholder="维度值"
              size="small"
              :disabled="readonly"
              style="width: 100px; margin-left: 6px"
            />
            <el-button
              v-if="!readonly && row.dims.length > 1"
              link
              type="danger"
              size="small"
              @click="row.dims.splice(idx, 1)"
            >
              删
            </el-button>
          </div>
          <el-button
            v-if="!readonly"
            link
            type="primary"
            size="small"
            @click="row.dims.push({ name: '', value: '' })"
          >
            + 维度
          </el-button>
          <div v-if="row.specText" class="spec-preview">{{ row.specText }}</div>
        </template>
      </el-table-column>

      <el-table-column label="售价" width="108">
        <template #default="{ row }">
          <el-input-number
            v-model="row.price"
            :min="0.01"
            :precision="2"
            :controls="false"
            size="small"
            :disabled="readonly"
            style="width: 90px"
          />
        </template>
      </el-table-column>

      <el-table-column label="划线价" width="108">
        <template #default="{ row }">
          <el-input-number
            v-model="row.marketPrice"
            :min="0"
            :precision="2"
            :controls="false"
            size="small"
            :disabled="readonly"
            style="width: 90px"
          />
        </template>
      </el-table-column>

      <el-table-column label="库存" width="96">
        <template #default="{ row }">
          <el-input-number
            v-model="row.available"
            :min="0"
            :controls="false"
            size="small"
            :disabled="readonly"
            style="width: 78px"
          />
        </template>
      </el-table-column>

      <el-table-column label="启用" width="72" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            :disabled="readonly"
          />
        </template>
      </el-table-column>

      <el-table-column v-if="!readonly" label="操作" width="72" fixed="right">
        <template #default="{ $index, row }">
          <el-button
            link
            type="danger"
            size="small"
            :disabled="rows.length <= 1"
            @click="removeRow($index, row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script lang="ts">
import type { AdminSku, SkuSavePayload, SpecDim } from '@/api/product'

export interface SkuRow {
  _key: string
  id?: number
  dims: SpecDim[]
  specText?: string
  price: number
  marketPrice?: number
  isDefault: number
  status: number
  available: number
}

let importKeySeq = 0
function nextImportKey() {
  importKeySeq += 1
  return `sku-${importKeySeq}`
}

export function skuFromApi(sku: AdminSku): SkuRow {
  const dims = sku.specJson?.dims?.length
    ? sku.specJson.dims.map((d) => ({ name: d.name, value: d.value }))
    : [{ name: '规格', value: '默认' }]
  return {
    _key: sku.id ? `id-${sku.id}` : nextImportKey(),
    id: sku.id,
    dims,
    specText: sku.specText,
    price: sku.price ?? 99,
    marketPrice: sku.marketPrice,
    isDefault: sku.isDefault ?? 0,
    status: sku.status ?? 1,
    available: sku.available ?? 0
  }
}

export function skuToPayload(row: SkuRow): SkuSavePayload {
  return {
    id: row.id,
    specJson: { dims: row.dims.map((d) => ({ name: d.name.trim(), value: d.value.trim() })) },
    price: row.price,
    marketPrice: row.marketPrice || undefined,
    isDefault: row.isDefault,
    status: row.status,
    available: row.available
  }
}

export function validateRows(list: SkuRow[]): string | null {
  if (!list.length) return '至少保留一个 SKU'
  let defaultCount = 0
  for (const row of list) {
    if (!row.dims.length) return '每个 SKU 至少一个规格维度'
    for (const d of row.dims) {
      if (!d.name?.trim() || !d.value?.trim()) return '规格名和规格值不能为空'
      if (d.name.trim().length > 32 || d.value.trim().length > 32) {
        return '规格名/值长度不能超过 32'
      }
    }
    if (!row.price || row.price <= 0) return '售价须大于 0'
    if (row.isDefault === 1) defaultCount++
  }
  if (defaultCount !== 1) return '须指定唯一默认 SKU'
  return null
}
</script>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  modelValue: SkuRow[]
  readonly?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [SkuRow[]]
  remove: [row: SkuRow]
}>()

const rows = ref<SkuRow[]>([])
const defaultKey = ref('')

let keySeq = 0
function nextKey() {
  keySeq += 1
  return `sku-${keySeq}`
}

function syncFromParent(val: SkuRow[]) {
  rows.value = val.map((r) => ({ ...r, dims: r.dims.map((d) => ({ ...d })) }))
  const def = rows.value.find((r) => r.isDefault === 1)
  defaultKey.value = def?._key || rows.value[0]?._key || ''
}

watch(
  () => props.modelValue,
  (val) => syncFromParent(val || []),
  { immediate: true, deep: true }
)

watch(
  rows,
  (val) => emit('update:modelValue', val),
  { deep: true }
)

function onDefaultChange(key: string | number | boolean | undefined) {
  const k = String(key)
  defaultKey.value = k
  rows.value.forEach((r) => {
    r.isDefault = r._key === k ? 1 : 0
  })
}

function addRow() {
  const row: SkuRow = {
    _key: nextKey(),
    dims: [{ name: '规格', value: '' }],
    price: 99,
    marketPrice: undefined,
    isDefault: rows.value.length === 0 ? 1 : 0,
    status: 1,
    available: 0
  }
  rows.value.push(row)
  if (rows.value.length === 1) {
    defaultKey.value = row._key
  }
}

function removeRow(_index: number, row: SkuRow) {
  if (rows.value.length <= 1) return
  emit('remove', row)
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.hint {
  font-size: 12px;
  color: #999;
}
.dim-row {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}
.spec-preview {
  font-size: 12px;
  color: #888;
  margin-top: 4px;
}
</style>
