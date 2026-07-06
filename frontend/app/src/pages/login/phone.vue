<template>
  <view class="page">
    <view class="nav">
      <view class="back" aria-label="返回" @click="onBack">←</view>
    </view>

    <view class="header">
      <view class="title">手机号登录 / 注册</view>
      <view class="subtitle">未注册手机号将自动创建账号</view>
    </view>

    <view class="form">
      <PhoneInput v-model="phone" @valid="onPhoneValid" />

      <view v-if="phoneOK" class="sms-block">
        <SmsCodeInput
          v-model="code"
          :length="6"
          :shake="shake"
          @complete="onComplete"
        />
        <view class="sms-resend-row">
          <CountdownButton
            ref="cdRef"
            :seconds="60"
            :disabled="!phoneOK || sending"
            @click="onSend"
          />
        </view>
      </view>

      <view class="proto-row">
        <ProtocolCheck v-model="agreed" />
      </view>

      <button
        class="submit"
        :class="{ disabled: !canSubmit, active: canSubmit }"
        :disabled="!canSubmit"
        aria-label="登录或注册"
        @click="onSubmit"
      >
        登录 / 注册
      </button>

      <view class="wx-link" @click="onWechat">
        <view class="wx-circle">微</view>
        <text class="wx-label">微信登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import PhoneInput from "@/components/PhoneInput.vue";
import SmsCodeInput from "@/components/SmsCodeInput.vue";
import CountdownButton from "@/components/CountdownButton.vue";
import ProtocolCheck from "@/components/ProtocolCheck.vue";
import { sendSms, loginBySms } from "@/api/auth";
import { useUserStore } from "@/stores/user";
import { applyAuthTokens } from "@/utils/auth-session";
import { getDeviceId, getDeviceType } from "@/utils/device";
import { wechatLogin } from "@/utils/wechat";

const phone = ref("");
const phoneOK = ref(false);
const code = ref("");
const agreed = ref(false);
const sending = ref(false);
const shake = ref(false);
const cdRef = ref<any>(null);

const userStore = useUserStore();

const canSubmit = computed(
  () => phoneOK.value && code.value.length === 6 && agreed.value
);

function onPhoneValid(v: boolean) {
  phoneOK.value = v;
  if (!v) code.value = "";
}

function onBack() {
  uni.navigateBack({ fail: () => uni.switchTab({ url: "/pages/mine/index" }) });
}

async function onSend() {
  if (!phoneOK.value) {
    uni.showToast({ title: "手机号格式不正确", icon: "none" });
    return;
  }
  if (!agreed.value) {
    uni.showToast({ title: "请先勾选协议", icon: "none" });
    return;
  }
  try {
    sending.value = true;
    await sendSms(phone.value);
    cdRef.value?.start();
    uni.showToast({ title: "验证码已发送", icon: "success" });
  } catch (e: any) {
    const errCode = e?.payload?.code;
    if (errCode === 40103) {
      uni.showToast({ title: "发送过于频繁", icon: "none" });
    } else if (errCode === 40104) {
      uni.showToast({ title: "今日发送已达上限", icon: "none" });
    } else {
      uni.showToast({ title: "发送失败", icon: "none" });
    }
  } finally {
    sending.value = false;
  }
}

function onComplete() {
  if (canSubmit.value) onSubmit();
}

async function onSubmit() {
  if (!canSubmit.value) return;
  try {
    uni.showLoading({ title: "登录中" });
    const r = await loginBySms({
      phone: phone.value,
      code: code.value,
      deviceId: getDeviceId(),
      deviceType: getDeviceType(),
    });
    const data: any = r.data;
    applyAuthTokens(userStore, {
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      sid: data.sid,
      accessExp: data.accessExp,
    });
    userStore.setUser({ ...data.user, phone: phone.value });
    uni.hideLoading();
    if (data.isNewUser) {
      uni.redirectTo({ url: "/pages/login/profile" });
    } else {
      uni.switchTab({ url: "/pages/mine/index" });
    }
  } catch (e: any) {
    uni.hideLoading();
    const c = e?.payload?.code;
    if (c === 40101 || c === 40102) {
      shake.value = true;
      code.value = "";
      setTimeout(() => (shake.value = false), 500);
      uni.showToast({ title: "验证码错误或已过期", icon: "none" });
    } else {
      uni.showToast({ title: "登录失败", icon: "none" });
    }
  }
}

async function onWechat() {
  if (!agreed.value) {
    uni.showToast({ title: "请先勾选协议", icon: "none" });
    return;
  }
  try {
    uni.showLoading({ title: "登录中" });
    const r: any = await wechatLogin();
    const data = r.data;
    applyAuthTokens(userStore, {
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      sid: data.sid,
      accessExp: data.accessExp,
    });
    userStore.setUser(data.user);
    uni.hideLoading();
    if (data.isNewUser) {
      uni.redirectTo({ url: "/pages/login/profile" });
    } else {
      uni.switchTab({ url: "/pages/mine/index" });
    }
  } catch {
    uni.hideLoading();
    uni.showToast({ title: "微信登录失败", icon: "none" });
  }
}
</script>

<style scoped lang="scss">
@import "@/styles/auth.scss";

.page {
  @include auth-page;
  padding: 24rpx $auth-page-padding 48rpx;
}

.nav {
  height: 64rpx;
  display: flex;
  align-items: center;
}

.back {
  font-size: 44rpx;
  color: #595959;
  line-height: 1;
}

.header {
  margin-top: 16rpx;
}

.title {
  font-size: 44rpx;
  font-weight: 700;
  color: $auth-text;
}

.subtitle {
  margin-top: 16rpx;
  font-size: 28rpx;
  color: $auth-text-secondary;
}

.form {
  margin-top: 64rpx;
  width: 100%;
}

.sms-block {
  margin-top: 48rpx;
}

.sms-resend-row {
  margin-top: 24rpx;
  display: flex;
  justify-content: flex-end;
}

.proto-row {
  margin-top: 48rpx;
}

.submit {
  @include auth-primary-btn;
  margin-top: 48rpx;
}

.wx-link {
  margin-top: 48rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16rpx;
}

.wx-circle {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: $auth-wechat;
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wx-label {
  font-size: 26rpx;
  color: $auth-text-secondary;
}
</style>
