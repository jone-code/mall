<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span>类目管理</span>
        <el-button v-if="canWrite" type="primary" @click="openCreate(0)">新建一级类目</el-button>
      </div>
    </template>

    <el-table
      v-loading="loading"
      :data="tree"
      row-key="id"
      default-expand-all
      :tree-props="{ children: 'children' }"
      stripe
    >
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="id" label="ID" width="72" />
      <el-table-column label="层级" width="88">
        <template #default="{ row }">{{ row.level }} 级</template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="72" />
      <el-table-column label="状态" width="88">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="canWrite" label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="(row.level ?? 1) < 2"
            link
            type="primary"
            @click="openCreate(row.id)"
          >
            添加子类目
          </el-button>
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="440px" destroy-on-close>
    <el-form :model="form" label-width="88px">
      <el-form-item v-if="form.parentId > 0" label="父类目">
        <span>{{ parentName }}</span>
      </el-form-item>
      <el-form-item label="名称" required>
        <el-input v-model="form.name" maxlength="64" show-word-limit />
      </el-form-item>
      <el-form-item label="图标 URL">
        <el-input v-model="form.iconUrl" placeholder="可选" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sortOrder" :min="0" />
      </el-form-item>
      <el-form-item v-if="editingId" label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createCategory,
  deleteCategory,
  listCategories,
  updateCategory,
  type CategoryNode
} from '@/api/product'
import { useAdminStore } from '@/stores/admin'

const store = useAdminStore()
const canWrite = computed(() => store.hasPermission('product:write'))

const loading = ref(false)
const saving = ref(false)
const tree = ref<CategoryNode[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const parentName = ref('')

const form = reactive({
  parentId: 0,
  name: '',
  iconUrl: '',
  sortOrder: 0,
  status: 1
})

const dialogTitle = computed(() => {
  if (editingId.value) return '编辑类目'
  return form.parentId > 0 ? '新建子类目' : '新建一级类目'
})

function findNode(nodes: CategoryNode[], id: number): CategoryNode | null {
  for (const n of nodes) {
    if (n.id === id) return n
    if (n.children?.length) {
      const found = findNode(n.children, id)
      if (found) return found
    }
  }
  return null
}

async function load() {
  loading.value = true
  try {
    tree.value = await listCategories()
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate(parentId: number) {
  editingId.value = null
  form.parentId = parentId
  form.name = ''
  form.iconUrl = ''
  form.sortOrder = 0
  form.status = 1
  parentName.value = parentId > 0 ? findNode(tree.value, parentId)?.name || '' : ''
  dialogVisible.value = true
}

function openEdit(row: CategoryNode) {
  editingId.value = row.id
  form.parentId = row.parentId ?? 0
  form.name = row.name
  form.iconUrl = row.iconUrl || ''
  form.sortOrder = row.sortOrder ?? 0
  form.status = row.status ?? 1
  parentName.value = ''
  dialogVisible.value = true
}

async function submit() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入类目名称')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateCategory(editingId.value, {
        name: form.name.trim(),
        iconUrl: form.iconUrl || undefined,
        sortOrder: form.sortOrder,
        status: form.status
      })
      ElMessage.success('已更新')
    } else {
      await createCategory({
        parentId: form.parentId || undefined,
        name: form.name.trim(),
        iconUrl: form.iconUrl || undefined,
        sortOrder: form.sortOrder
      })
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function onDelete(row: CategoryNode) {
  try {
    await ElMessageBox.confirm(`确定删除类目「${row.name}」？`, '删除确认', { type: 'warning' })
    await deleteCategory(row.id)
    ElMessage.success('已删除')
    await load()
  } catch {
    /* cancel or error shown by http */
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
</style>
