<template>
  <div class="login-page">
    <EnvBadge class="env-badge-pos" />
    <div class="left-pane">
      <div class="brand">
        <div class="illu">
          <svg viewBox="0 0 200 200" width="180" height="180">
            <defs>
              <linearGradient id="g1" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="#2d2d2d" />
                <stop offset="100%" stop-color="#1a1a1a" />
              </linearGradient>
            </defs>
            <rect x="40" y="40" width="120" height="120" fill="url(#g1)" />
            <rect x="72" y="72" width="56" height="56" fill="none" stroke="#c9a96e" stroke-width="2" />
            <text x="100" y="108" text-anchor="middle" fill="#c9a96e" font-size="36" font-weight="600">C</text>
          </svg>
        </div>
        <div class="title">ComonOn 后台</div>
        <div class="slogan">经营，从这里开始</div>
      </div>
    </div>

    <div class="right-pane">
      <div class="card">
        <div class="card-title">ComonOn 后台</div>
        <div class="divider" />

        <!-- Step 1 -->
        <template v-if="step === 1">
          <el-form
            :model="form1"
            ref="form1Ref"
            :rules="rules1"
            label-position="top"
            @submit.prevent="onStep1"
          >
            <el-form-item label="账号" prop="username">
              <el-input v-model="form1.username" autocomplete="username" placeholder="请输入账号" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <PasswordInput v-model="form1.password" />
            </el-form-item>
            <el-form-item label="验证码" prop="captcha">
              <div class="captcha-row">
                <el-input
                  v-model="form1.captcha"
                  maxlength="4"
                  placeholder="验证码"
                  style="flex: 1"
                />
                <CaptchaImage ref="captchaRef" @update:captchaId="captchaId = $event" />
              </div>
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="form1.rememberMe">7 天免登录</el-checkbox>
            </el-form-item>
            <el-button
              type="primary"
              class="primary-btn"
              :loading="loading"
              @click="onStep1"
            >
              下一步
            </el-button>
            <div class="helper">忘记密码？请联系超管 admin@comonon.com</div>
          </el-form>
        </template>

        <!-- Step 2 -->
        <template v-else>
          <div class="step2-title">验证手机号</div>
          <div class="step2-sub">验证码已发送至 {{ store.phoneMasked || '***' }}</div>
          <SmsCodeInput
            v-model="smsCode"
            :error="smsError"
            @complete="onStep2"
          />
          <div class="countdown">
            <span v-if="counting > 0">{{ counting }}s 后可重发</span>
            <el-button v-else link type="primary" @click="resend">重新发送</el-button>
          </div>
          <el-button
            type="primary"
            class="primary-btn"
            :loading="loading"
            :disabled="smsCode.length !== 6"
            @click="onStep2"
          >
            登 录
          </el-button>
          <el-button link class="back" @click="goBack">← 返回上一步</el-button>
        </template>
      </div>
      <div class="footer">ICP备案号 · v0.1.0</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import EnvBadge from '@/components/EnvBadge.vue'
import CaptchaImage from '@/components/CaptchaImage.vue'
import SmsCodeInput from '@/components/SmsCodeInput.vue'
import PasswordInput from '@/components/PasswordInput.vue'

const router = useRouter()
const route = useRoute()
const store = useAdminStore()

const step = ref<1 | 2>(1)
const loading = ref(false)
const captchaRef = ref<InstanceType<typeof CaptchaImage> | null>(null)
const captchaId = ref('')

const form1Ref = ref<FormInstance | null>(null)
const form1 = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false
})

const rules1: FormRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 10, message: '密码至少 10 位', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为 4 位', trigger: 'blur' }
  ]
}

const smsCode = ref('')
const smsError = ref(false)
const counting = ref(0)
let timer: number | undefined

function startCountdown(sec = 60) {
  counting.value = sec
  if (timer) clearInterval(timer)
  timer = window.setInterval(() => {
    counting.value--
    if (counting.value <= 0 && timer) {
      clearInterval(timer)
      timer = undefined
    }
  }, 1000)
}

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})

onMounted(() => {
  // if loggedin already, jump
  if (store.isLoggedIn) router.replace('/dashboard')
})

async function onStep1() {
  const valid = await form1Ref.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await store.loginStep1({
      username: form1.username,
      password: form1.password,
      captcha: form1.captcha,
      captchaId: captchaId.value,
      rememberMe: form1.rememberMe
    })
    step.value = 2
    smsCode.value = ''
    startCountdown(60)
  } catch (e: any) {
    captchaRef.value?.refresh()
    ElMessage.error(e?.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function onStep2() {
  if (smsCode.value.length !== 6) return
  loading.value = true
  smsError.value = false
  try {
    await store.loginStep2(smsCode.value)
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.replace(redirect)
  } catch (e: any) {
    smsError.value = true
    smsCode.value = ''
    ElMessage.error(e?.response?.data?.message || '验证码错误')
  } finally {
    loading.value = false
  }
}

async function resend() {
  // resend re-runs step1 with same credentials
  await onStep1()
}

function goBack() {
  step.value = 1
  smsCode.value = ''
  store.challengeToken = ''
  if (timer) clearInterval(timer)
  counting.value = 0
}
</script>

<style scoped>
.login-page {
  position: relative;
  display: flex;
  height: 100vh;
  min-height: 600px;
  background: var(--admin-color-bg);
}
.env-badge-pos {
  position: absolute;
  top: 16px;
  right: 24px;
  z-index: 10;
}
.left-pane {
  flex: 0 0 50%;
  max-width: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2d2d2d 0%, #1a1a1a 100%);
}
.brand {
  text-align: center;
  padding: 0 48px;
}
.brand .title {
  font-size: 36px;
  font-weight: 600;
  color: #fff;
  margin-top: 28px;
  letter-spacing: 4px;
  text-transform: uppercase;
}
.brand .slogan {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.65);
  margin-top: 14px;
  letter-spacing: 1px;
}
.right-pane {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--admin-color-bg);
}
.card {
  width: 400px;
  max-width: calc(100% - 48px);
  background: var(--admin-color-bg-card);
  padding: 40px;
  border-radius: 4px;
  border: 1px solid var(--admin-color-border);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.04);
}
.card-title {
  font-size: 20px;
  font-weight: 600;
  text-align: center;
  letter-spacing: 2px;
}
.divider {
  height: 1px;
  background: var(--admin-color-border);
  margin: 16px 0 24px;
}
.captcha-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.primary-btn {
  width: 100%;
  height: 44px;
  margin-top: 8px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
}
.helper {
  margin-top: 16px;
  text-align: center;
  font-size: 12px;
  color: var(--admin-color-text-secondary);
}
.step2-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}
.step2-sub {
  font-size: 13px;
  color: var(--admin-color-text-secondary);
  margin-bottom: 16px;
}
.countdown {
  margin: 12px 0 16px;
  font-size: 13px;
  color: var(--admin-color-text-secondary);
}
.back {
  display: block;
  margin: 16px auto 0;
}
.footer {
  margin-top: 16px;
  font-size: 12px;
  color: var(--admin-color-text-secondary);
}
</style>
