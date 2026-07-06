<template>
  <el-tabs v-model="activeTab">
    <el-tab-pane label="角色授权" name="roles">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-head">
                <span>角色列表</span>
                <el-button v-if="canWriteRole" type="primary" size="small" @click="openCreateRole">
                  新增角色
                </el-button>
              </div>
            </template>
            <el-table
              v-loading="rolesLoading"
              :data="roles"
              highlight-current-row
              @current-change="onSelectRole"
            >
              <el-table-column prop="code" label="编码" min-width="100" />
              <el-table-column prop="name" label="名称" min-width="100" />
              <el-table-column prop="permissionCount" label="权限数" width="72" />
              <el-table-column v-if="canWriteRole" label="操作" width="100" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" size="small" @click.stop="openEditRole(row)">编辑</el-button>
                  <el-button
                    v-if="row.code !== 'SUPER_ADMIN'"
                    link
                    type="danger"
                    size="small"
                    @click.stop="onDeleteRole(row)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>

        <el-col :span="16">
          <el-card v-loading="detailLoading">
            <template #header>
              <div class="card-head">
                <span>{{ selectedRole ? `权限配置：${selectedRole.name}` : '请选择角色' }}</span>
                <el-button
                  v-if="selectedRole"
                  type="primary"
                  :disabled="!canAssign"
                  :loading="saving"
                  @click="saveRolePermissions"
                >
                  保存
                </el-button>
              </div>
            </template>

            <el-empty v-if="!selectedRole" description="从左侧选择一个角色" />

            <div v-else class="perm-groups">
              <div v-for="group in permGroups" :key="group.module" class="perm-group">
                <div class="perm-module">{{ group.module || '其他' }}</div>
                <el-checkbox-group v-model="checkedPermIds" :disabled="!canAssign">
                  <el-checkbox
                    v-for="p in group.items"
                    :key="p.id"
                    :value="p.id"
                    class="perm-item"
                  >
                    {{ p.name }}
                    <span class="perm-code">{{ p.code }}</span>
                  </el-checkbox>
                </el-checkbox-group>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-tab-pane>

    <el-tab-pane label="权限点" name="permissions">
      <el-card>
        <template #header>
          <div class="card-head">
            <span>权限点列表</span>
            <el-button v-if="canWritePermission" type="primary" size="small" @click="openCreatePermission">
              新增权限
            </el-button>
          </div>
        </template>
        <el-table v-loading="permissionsLoading" :data="permissions" stripe>
          <el-table-column prop="code" label="编码" min-width="180" />
          <el-table-column prop="name" label="名称" min-width="140" />
          <el-table-column prop="module" label="模块" width="120" />
          <el-table-column v-if="canWritePermission" label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="openEditPermission(row)">编辑</el-button>
              <el-button link type="danger" size="small" @click="onDeletePermission(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-tab-pane>
  </el-tabs>

  <el-dialog v-model="roleDialogVisible" :title="roleDialogTitle" width="480px" destroy-on-close>
    <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="88px">
      <el-form-item v-if="roleDialogMode === 'create'" label="编码" prop="code">
        <el-input v-model="roleForm.code" placeholder="如 OPS_ADMIN" />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="roleForm.name" placeholder="角色名称" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="roleForm.remark" type="textarea" :rows="3" placeholder="可选" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="roleDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="roleSaving" @click="submitRole">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="permDialogVisible" :title="permDialogTitle" width="480px" destroy-on-close>
    <el-form ref="permFormRef" :model="permForm" :rules="permRules" label-width="88px">
      <el-form-item v-if="permDialogMode === 'create'" label="编码" prop="code">
        <el-input v-model="permForm.code" placeholder="如 admin:role:write" />
      </el-form-item>
      <el-form-item label="名称" prop="name">
        <el-input v-model="permForm.name" placeholder="权限名称" />
      </el-form-item>
      <el-form-item label="模块">
        <el-input v-model="permForm.module" placeholder="如 admin" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="permDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="permSaving" @click="submitPermission">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import {
  assignRolePermissions,
  createPermission,
  createRole,
  deletePermission,
  deleteRole,
  getRole,
  listPermissions,
  listRoles,
  updatePermission,
  updateRole,
  type PermissionRow,
  type RoleRow
} from '@/api/rbac'

const store = useAdminStore()
const canAssign = computed(() => store.hasPermission('admin:permission:assign'))
const canWriteRole = computed(() => store.hasPermission('admin:role:write'))
const canWritePermission = computed(() => store.hasPermission('admin:permission:write'))

const activeTab = ref('roles')
const rolesLoading = ref(false)
const permissionsLoading = ref(false)
const detailLoading = ref(false)
const saving = ref(false)
const roles = ref<RoleRow[]>([])
const permissions = ref<PermissionRow[]>([])
const selectedRole = ref<RoleRow | null>(null)
const checkedPermIds = ref<number[]>([])

const permGroups = computed(() => {
  const map = new Map<string, PermissionRow[]>()
  for (const p of permissions.value) {
    const mod = p.module || '其他'
    if (!map.has(mod)) map.set(mod, [])
    map.get(mod)!.push(p)
  }
  return Array.from(map.entries()).map(([module, items]) => ({ module, items }))
})

async function loadRoles() {
  rolesLoading.value = true
  try {
    const { data } = await listRoles()
    roles.value = data || []
  } finally {
    rolesLoading.value = false
  }
}

async function loadPermissions() {
  permissionsLoading.value = true
  try {
    const { data } = await listPermissions()
    permissions.value = data || []
  } finally {
    permissionsLoading.value = false
  }
}

async function onSelectRole(role: RoleRow | null) {
  if (!role) return
  selectedRole.value = role
  detailLoading.value = true
  try {
    const { data } = await getRole(role.id)
    checkedPermIds.value = (data.permissionIds || []).map((id) => Number(id))
  } catch (e: any) {
    checkedPermIds.value = []
    ElMessage.error(e?.message || '加载角色权限失败')
  } finally {
    detailLoading.value = false
  }
}

async function saveRolePermissions() {
  if (!selectedRole.value) return
  saving.value = true
  try {
    await assignRolePermissions(selectedRole.value.id, checkedPermIds.value)
    ElMessage.success('权限已保存，相关管理员需重新登录后生效')
    await loadRoles()
    const updated = roles.value.find((r) => r.id === selectedRole.value!.id)
    if (updated) selectedRole.value = updated
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const roleDialogVisible = ref(false)
const roleDialogMode = ref<'create' | 'edit'>('create')
const roleDialogTitle = computed(() => (roleDialogMode.value === 'create' ? '新增角色' : '编辑角色'))
const roleSaving = ref(false)
const editingRoleId = ref<number | null>(null)
const roleFormRef = ref<FormInstance>()
const roleForm = reactive({ code: '', name: '', remark: '' })
const roleRules: FormRules = {
  code: [
    { required: true, message: '请输入编码', trigger: 'blur' },
    { pattern: /^[A-Z][A-Z0-9_]{1,62}$/, message: '大写字母开头，仅含大写字母/数字/下划线', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

function openCreateRole() {
  roleDialogMode.value = 'create'
  editingRoleId.value = null
  roleForm.code = ''
  roleForm.name = ''
  roleForm.remark = ''
  roleDialogVisible.value = true
}

function openEditRole(row: RoleRow) {
  roleDialogMode.value = 'edit'
  editingRoleId.value = row.id
  roleForm.code = row.code
  roleForm.name = row.name
  roleForm.remark = row.remark || ''
  roleDialogVisible.value = true
}

async function submitRole() {
  const valid = await roleFormRef.value?.validate().catch(() => false)
  if (!valid) return
  roleSaving.value = true
  try {
    if (roleDialogMode.value === 'create') {
      const { data } = await createRole({
        code: roleForm.code.trim(),
        name: roleForm.name.trim(),
        remark: roleForm.remark.trim() || undefined
      })
      ElMessage.success('角色已创建')
      await loadRoles()
      selectedRole.value = data
      checkedPermIds.value = []
    } else if (editingRoleId.value != null) {
      await updateRole(editingRoleId.value, {
        name: roleForm.name.trim(),
        remark: roleForm.remark.trim() || undefined
      })
      ElMessage.success('角色已更新')
      await loadRoles()
    }
    roleDialogVisible.value = false
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    roleSaving.value = false
  }
}

async function onDeleteRole(row: RoleRow) {
  try {
    await ElMessageBox.confirm(`确认删除角色「${row.name}」？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteRole(row.id)
    ElMessage.success('角色已删除')
    if (selectedRole.value?.id === row.id) {
      selectedRole.value = null
      checkedPermIds.value = []
    }
    await loadRoles()
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

const permDialogVisible = ref(false)
const permDialogMode = ref<'create' | 'edit'>('create')
const permDialogTitle = computed(() => (permDialogMode.value === 'create' ? '新增权限' : '编辑权限'))
const permSaving = ref(false)
const editingPermId = ref<number | null>(null)
const permFormRef = ref<FormInstance>()
const permForm = reactive({ code: '', name: '', module: '' })
const permRules: FormRules = {
  code: [
    { required: true, message: '请输入编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_:]{2,127}$/, message: '格式如 admin:role:write', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
}

function openCreatePermission() {
  permDialogMode.value = 'create'
  editingPermId.value = null
  permForm.code = ''
  permForm.name = ''
  permForm.module = ''
  permDialogVisible.value = true
}

function openEditPermission(row: PermissionRow) {
  permDialogMode.value = 'edit'
  editingPermId.value = row.id
  permForm.code = row.code
  permForm.name = row.name
  permForm.module = row.module || ''
  permDialogVisible.value = true
}

async function submitPermission() {
  const valid = await permFormRef.value?.validate().catch(() => false)
  if (!valid) return
  permSaving.value = true
  try {
    if (permDialogMode.value === 'create') {
      await createPermission({
        code: permForm.code.trim(),
        name: permForm.name.trim(),
        module: permForm.module.trim() || undefined
      })
      ElMessage.success('权限已创建')
    } else if (editingPermId.value != null) {
      await updatePermission(editingPermId.value, {
        name: permForm.name.trim(),
        module: permForm.module.trim() || undefined
      })
      ElMessage.success('权限已更新')
    }
    permDialogVisible.value = false
    await loadPermissions()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    permSaving.value = false
  }
}

async function onDeletePermission(row: PermissionRow) {
  try {
    await ElMessageBox.confirm(`确认删除权限「${row.name}」？关联角色的该权限将被移除。`, '提示', {
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deletePermission(row.id)
    ElMessage.success('权限已删除')
    checkedPermIds.value = checkedPermIds.value.filter((id) => id !== row.id)
    await Promise.all([loadPermissions(), loadRoles()])
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

onMounted(async () => {
  await Promise.all([loadRoles(), loadPermissions()])
})
</script>

<style scoped>
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.perm-groups {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.perm-module {
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}
.perm-item {
  display: flex;
  margin-right: 16px;
  margin-bottom: 8px;
}
.perm-code {
  margin-left: 6px;
  color: #909399;
  font-size: 12px;
}
</style>
