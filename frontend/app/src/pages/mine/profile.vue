<template>
  <view class="page">
    <view class="avatar-wrap" @click="chooseAvatar">
      <image v-if="avatarUrl" class="avatar" :src="avatarUrl" mode="aspectFill" />
      <view v-else class="avatar avatar-placeholder">{{ avatarLetter }}</view>
      <text class="avatar-tip">点击更换头像</text>
    </view>

    <view class="field">
      <text class="label">昵称</text>
      <input
        class="input"
        :value="nickname"
        maxlength="16"
        placeholder="2-16 个字符"
        @input="onNickInput"
      />
    </view>

    <view class="field">
      <text class="label">手机号</text>
      <input class="input" :value="phone" maxlength="11" placeholder="11 位手机号" @input="onPhoneInput" />
      <text class="hint">换绑需先获取短信验证码</text>
    </view>

    <view class="field">
      <text class="label">验证码</text>
      <view class="code-row">
        <input class="input code-input" :value="smsCode" maxlength="6" placeholder="6 位验证码" @input="onCodeInput" />
        <CountdownButton ref="smsBtnRef" :disabled="!canSendSms" @click="onSendSms" />
      </view>
    </view>

    <button class="btn save" :loading="saving" @click="onSave">保存资料</button>
    <button v-if="phoneChanged" class="btn phone" :loading="phoneSaving" @click="onChangePhone">保存手机号</button>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getMe, updateMe, changePhone, uploadImage, sendSms } from '@/api/auth'
import { resolveMediaUrl } from '@/utils/media'
import { showError } from '@/utils/error-message'
import CountdownButton from '@/components/CountdownButton.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const nickname = ref('')
const avatarUrl = ref('')
const phone = ref('')
const originalPhone = ref('')
const smsCode = ref('')
const saving = ref(false)
const phoneSaving = ref(false)
const smsBtnRef = ref<{ start: () => void } | null>(null)

const avatarLetter = computed(() => {
  const name = nickname.value || userStore.user?.nickname || 'U'
  return name.slice(0, 1).toUpperCase()
})

const phoneChanged = computed(() => phone.value.trim() !== originalPhone.value)
const canSendSms = computed(() => /^1\d{10}$/.test(phone.value.trim()))

function onNickInput(e: any) {
  nickname.value = e.detail.value
}
function onPhoneInput(e: any) {
  phone.value = e.detail.value
}
function onCodeInput(e: any) {
  smsCode.value = e.detail.value
}

function chooseAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      const local = res.tempFilePaths[0]
      try {
        uni.showLoading({ title: '上传中' })
        const url = await uploadImage(local)
        avatarUrl.value = url
        uni.showToast({ title: '头像已更新', icon: 'none' })
      } catch (e) {
        showError(e, '上传失败')
      } finally {
        uni.hideLoading()
      }
    },
  })
}

async function onSendSms() {
  const p = phone.value.trim()
  if (!/^1\d{10}$/.test(p)) {
    uni.showToast({ title: '请先填写正确手机号', icon: 'none' })
    return
  }
  try {
    await sendSms(p)
    smsBtnRef.value?.start()
    uni.showToast({ title: '验证码已发送', icon: 'none' })
  } catch (e) {
    showError(e, '发送失败')
  }
}

async function loadProfile() {
  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  try {
    const res = await getMe()
    const u = res.data
    if (u) {
      nickname.value = u.nickname || ''
      avatarUrl.value = resolveMediaUrl(u.avatarUrl || '')
      phone.value = u.phone || ''
      originalPhone.value = u.phone || ''
      userStore.setUser({ ...userStore.user, ...u })
    }
  } catch {
    nickname.value = userStore.user?.nickname || ''
    avatarUrl.value = resolveMediaUrl(userStore.user?.avatarUrl || '')
    phone.value = userStore.user?.phone || ''
    originalPhone.value = phone.value
  }
}

async function onSave() {
  const name = nickname.value.trim()
  if (name.length < 2) {
    uni.showToast({ title: '昵称至少 2 个字符', icon: 'none' })
    return
  }
  saving.value = true
  try {
    const res = await updateMe({ nickname: name, avatarUrl: avatarUrl.value || '' })
    userStore.setUser({ ...userStore.user, ...res.data })
    uni.showToast({ title: '已保存', icon: 'success' })
  } catch (e: unknown) {
    const msg = (e as { payload?: { message?: string } })?.payload?.message || '保存失败'
    uni.showToast({ title: msg, icon: 'none' })
  } finally {
    saving.value = false
  }
}

async function onChangePhone() {
  const p = phone.value.trim()
  if (!/^1\d{10}$/.test(p)) {
    uni.showToast({ title: '手机号格式不正确', icon: 'none' })
    return
  }
  phoneSaving.value = true
  try {
    const res = await changePhone(p, smsCode.value.trim())
    userStore.setUser({ ...userStore.user, ...res.data })
    originalPhone.value = res.data?.phone || p
    uni.showToast({ title: '手机号已更新', icon: 'success' })
  } catch (e: unknown) {
    const msg = (e as { payload?: { message?: string } })?.payload?.message || '换绑失败'
    uni.showToast({ title: msg, icon: 'none' })
  } finally {
    phoneSaving.value = false
  }
}

onShow(loadProfile)
</script>

<style scoped lang="scss">
.page {
  @include page-shell;
  padding: 48rpx $spacing-page 80rpx;
}
.avatar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 56rpx;
}
.avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  border: 2rpx solid $color-border;
  background: $color-bg-section;
}
.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56rpx;
  font-weight: 500;
  color: $color-text-secondary;
}
.avatar-tip {
  margin-top: 16rpx;
  font-size: 24rpx;
  color: $color-text-secondary;
}
.field {
  margin-bottom: 32rpx;
}
.label {
  display: block;
  font-size: 26rpx;
  color: $color-text-secondary;
  margin-bottom: 12rpx;
}
.input {
  width: 100%;
  height: 88rpx;
  padding: 0 24rpx;
  box-sizing: border-box;
  font-size: 30rpx;
  background: $color-bg-page;
  border: 1rpx solid $color-border;
  border-radius: $radius-card;
}
.hint {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $color-text-disabled;
}
.btn.save,
.btn.phone {
  margin-top: 24rpx;
  @include btn-primary;
  height: 88rpx;
  line-height: 88rpx;
  font-size: 30rpx;
}
.code-row {
  display: flex;
  gap: 16rpx;
  align-items: center;
}
.code-input {
  flex: 1;
}
.btn.phone {
  @include btn-outline;
}
</style>
