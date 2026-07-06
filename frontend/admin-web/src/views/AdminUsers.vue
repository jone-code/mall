<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span>管理账号</span>
        <div class="actions">
          <el-input
            v-model="keyword"
            placeholder="搜索用户名 / 姓名"
            clearable
            style="width: 240px"
            @keyup.enter="load"
            @clear="load"
          />
          <el-button v-if="canCreate" type="primary" @click="openCreate">新增账号</el-button>
        </div>
      </div>
    </template>

    <el-table v-loading="loading" :data="rows" stripe>
      <el-table-column prop="id" label="ID" width="72" />
      <el-table-column prop="username" label="账号" min-width="120" />
      <el-table-column prop="realName" label="姓名" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
      <el-table-column label="状态" width="88">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
            {{ row.status === 0 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginAt" label="最近登录" min-width="168" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button v-if="canAssignRole" link type="primary" size="small" @click="openAssignRoles(row)">
            分配角色
          </el-button>
          <el-button
            v-if="canResetPassword && row.id !== store.profile?.id"
            link
            type="warning"
            size="small"
            @click="openResetPassword(row)"
          >
            重置密码
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="createVisible" title="新增管理账号" width="520px" destroy-on-close>
    <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="88px">
      <el-form-item label="账号" prop="username">
        <el-input v-model="createForm.username" autocomplete="off" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="createForm.password" type="password" show-password autocomplete="new-password" />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model="createForm.realName" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="createForm.phone" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="createForm.email" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createVisible = false">取消</el-button>
      <el-button type="primary" :loading="creating" @click="submitCreate">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="roleVisible" :title="`分配角色：${assignUser?.username || ''}`" width="480px" destroy-on-close>
    <el-checkbox-group v-model="checkedRoleIds">
      <el-checkbox v-for="role in allRoles" :key="role.id" :value="role.id" class="role-item">
        {{ role.name }}
        <span class="role-code">{{ role.code }}</span>
      </el-checkbox>
    </el-checkbox-group>
    <el-empty v-if="!allRoles.length" description="暂无角色，请先在权限管理创建" />
    <template #footer>
      <el-button @click="roleVisible = false">取消</el-button>
      <el-button type="primary" :loading="assigning" @click="submitAssignRoles">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="pwdVisible"
    :title="`重置密码：${resetUser?.username || ''}`"
    width="480px"
    destroy-on-close
  >
    <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="96px">
      <el-form-item label="新密码" prop="newPassword">
        <PasswordInput v-model="pwdForm.newPassword" :show-strength="true" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <PasswordInput v-model="pwdForm.confirmPassword" />
      </el-form-item>
    </el-form>
    <p class="pwd-tip">重置后该账号所有会话将失效，需重新登录。</p>
    <template #footer>
      <el-button @click="pwdVisible = false">取消</el-button>
      <el-button type="primary" :loading="resetting" @click="submitResetPassword">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import PasswordInput from '@/components/PasswordInput.vue'
import {
  assignAdminUserRoles,
  createAdminUser,
  getAdminUserRoles,
  listAdminUsers,
  resetAdminUserPassword,
  type AdminUserRow
} from '@/api/adminUser'
import { listRoles, type RoleRow } from '@/api/rbac'

const store = useAdminStore()
const canCreate = computed(() => store.hasPermission('admin:user:create'))
const canAssignRole = computed(() => store.hasPermission('admin:role:assign'))
const canResetPassword = computed(() => store.isSuperAdmin)

const loading = ref(false)
const keyword = ref('')
const rows = ref<AdminUserRow[]>([])

async function load() {
  loading.value = true
  try {
    const { data } = await listAdminUsers({
      keyword: keyword.value || undefined,
      page: 1,
      size: 50
    })
    rows.value = data || []
  } finally {
    loading.value = false
  }
}

const createVisible = ref(false)
const creating = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: ''
})
const createRules: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, min: 8, message: '密码至少 8 位', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

function openCreate() {
  createForm.username = ''
  createForm.password = ''
  createForm.realName = ''
  createForm.phone = ''
  createForm.email = ''
  createVisible.value = true
}

async function submitCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  creating.value = true
  try {
    await createAdminUser({
      username: createForm.username.trim(),
      password: createForm.password,
      realName: createForm.realName.trim() || undefined,
      phone: createForm.phone.trim(),
      email: createForm.email.trim() || undefined
    })
    ElMessage.success('账号已创建')
    createVisible.value = false
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message || '创建失败')
  } finally {
    creating.value = false
  }
}

const roleVisible = ref(false)
const assigning = ref(false)
const assignUser = ref<AdminUserRow | null>(null)
const allRoles = ref<RoleRow[]>([])
const checkedRoleIds = ref<number[]>([])

async function openAssignRoles(row: AdminUserRow) {
  assignUser.value = row
  roleVisible.value = true
  try {
    const [rolesRes, userRolesRes] = await Promise.all([listRoles(), getAdminUserRoles(row.id)])
    allRoles.value = rolesRes.data || []
    checkedRoleIds.value = (userRolesRes.data || []).map((id) => Number(id))
  } catch (e: any) {
    ElMessage.error(e?.message || '加载角色失败')
  }
}

async function submitAssignRoles() {
  if (!assignUser.value) return
  if (!checkedRoleIds.value.length) {
    ElMessage.warning('请至少选择一个角色')
    return
  }
  assigning.value = true
  try {
    await assignAdminUserRoles(assignUser.value.id, checkedRoleIds.value)
    ElMessage.success('角色已分配，该账号需重新登录后生效')
    roleVisible.value = false
  } catch (e: any) {
    ElMessage.error(e?.message || '分配失败')
  } finally {
    assigning.value = false
  }
}

const pwdVisible = ref(false)
const resetting = ref(false)
const resetUser = ref<AdminUserRow | null>(null)
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const strongPwd = (_: unknown, value: string, cb: (e?: Error) => void) => {
  if (!value) return cb(new Error('请输入新密码'))
  if (value.length < 10) return cb(new Error('密码至少 10 位'))
  if (!/[a-z]/.test(value) || !/[A-Z]/.test(value)) return cb(new Error('需包含大小写字母'))
  if (!/\d/.test(value)) return cb(new Error('需包含数字'))
  if (!/[^A-Za-z0-9]/.test(value)) return cb(new Error('需包含符号'))
  cb()
}
const sameAsNew = (_: unknown, value: string, cb: (e?: Error) => void) => {
  if (value !== pwdForm.newPassword) return cb(new Error('两次密码不一致'))
  cb()
}
const pwdRules: FormRules = {
  newPassword: [{ required: true, validator: strongPwd, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: sameAsNew, trigger: 'blur' }]
}

function openResetPassword(row: AdminUserRow) {
  resetUser.value = row
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdVisible.value = true
}

async function submitResetPassword() {
  if (!resetUser.value) return
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  resetting.value = true
  try {
    await resetAdminUserPassword(resetUser.value.id, pwdForm.newPassword)
    ElMessage.success('密码已重置，该账号需重新登录')
    pwdVisible.value = false
  } catch (e: unknown) {
    ElMessage.error((e as Error)?.message || '重置失败')
  } finally {
    resetting.value = false
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
}
.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.role-item {
  display: flex;
  margin-right: 16px;
  margin-bottom: 8px;
}
.role-code {
  margin-left: 6px;
  color: #909399;
  font-size: 12px;
}
.pwd-tip {
  margin: 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}
</style>
