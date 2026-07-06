<template>
  <div class="reset-page">
    <div class="card">
      <h2>请更新您的登录密码</h2>
      <p class="tip">您的密码已超过 90 天未更新，或为首次登录</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="原密码" prop="oldPassword">
          <PasswordInput v-model="form.oldPassword" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <PasswordInput v-model="form.newPassword" :show-strength="true" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <PasswordInput v-model="form.confirmPassword" />
        </el-form-item>
      </el-form>

      <div class="rules">
        规则：
        <ul>
          <li>至少 10 位</li>
          <li>包含大小写字母 + 数字 + 符号</li>
          <li>不可与最近 5 次相同</li>
        </ul>
      </div>

      <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">
        提 交
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import http from '@/api/http'
import { useAdminStore } from '@/stores/admin'
import PasswordInput from '@/components/PasswordInput.vue'

const router = useRouter()
const store = useAdminStore()
const formRef = ref<FormInstance | null>(null)
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const strongPwd = (_: any, value: string, cb: (e?: Error) => void) => {
  if (!value) return cb(new Error('请输入新密码'))
  if (value.length < 10) return cb(new Error('密码至少 10 位'))
  if (!/[a-z]/.test(value) || !/[A-Z]/.test(value)) return cb(new Error('需包含大小写字母'))
  if (!/\d/.test(value)) return cb(new Error('需包含数字'))
  if (!/[^A-Za-z0-9]/.test(value)) return cb(new Error('需包含符号'))
  cb()
}
const sameAsNew = (_: any, value: string, cb: (e?: Error) => void) => {
  if (value !== form.newPassword) return cb(new Error('两次密码不一致'))
  cb()
}

const rules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, validator: strongPwd, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: sameAsNew, trigger: 'blur' }]
}

async function onSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await http.post('/admin/me/password', {
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })
    ElMessage.success('密码已更新，请重新登录')
    store.clearAuth()
    router.replace('/login')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.message || '修改失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.reset-page {
  min-height: 100vh;
  background: var(--admin-color-bg);
  display: flex;
  align-items: center;
  justify-content: center;
}
.card {
  width: 460px;
  background: #fff;
  padding: 32px 36px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}
h2 {
  margin: 0 0 8px;
}
.tip {
  color: #888;
  margin: 0 0 16px;
  font-size: 13px;
}
.rules {
  background: #fafafa;
  padding: 12px 16px;
  border-radius: 6px;
  font-size: 12px;
  color: #666;
  margin-bottom: 16px;
}
.rules ul {
  margin: 8px 0 0 16px;
  padding: 0;
}
.submit {
  width: 100%;
}
</style>
