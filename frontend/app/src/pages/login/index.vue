<template>
  <view class="page">
    <view class="close" aria-label="关闭" @click="onClose">×</view>

    <view class="brand">
      <view class="logo">C</view>
      <view class="welcome">欢迎来到 ComonOn</view>
    </view>

    <view class="actions">
      <button
        class="btn primary"
        :class="{ disabled: !agreed }"
        aria-label="手机号一键登录"
        @click="onPhone"
      >
        手机号一键登录
      </button>
      <button
        class="btn wx"
        :class="{ disabled: !agreed }"
        aria-label="微信登录"
        @click="onWechat"
      >
        <view class="wx-ico">微</view>
        <text>微信登录</text>
      </button>
    </view>

    <view class="proto-row">
      <ProtocolCheck v-model="agreed" />
    </view>

    <view v-if="showAgreementSheet" class="sheet-mask" @click="showAgreementSheet = false">
      <view class="sheet" @click.stop>
        <view class="sheet-title">请先阅读并同意协议</view>
        <view class="sheet-body">
          <ProtocolCheck v-model="agreed" />
        </view>
        <button class="sheet-btn" :disabled="!agreed" @click="onSheetConfirm">同意并继续</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import ProtocolCheck from "@/components/ProtocolCheck.vue";
import { wechatLogin } from "@/utils/wechat";
import { useUserStore } from "@/stores/user";
import { applyAuthTokens } from "@/utils/auth-session";

const agreed = ref(false);
const showAgreementSheet = ref(false);
const pendingAction = ref<"phone" | "wechat" | null>(null);
const userStore = useUserStore();

function promptAgreement(action: "phone" | "wechat") {
  pendingAction.value = action;
  showAgreementSheet.value = true;
}

function onSheetConfirm() {
  if (!agreed.value) return;
  showAgreementSheet.value = false;
  if (pendingAction.value === "phone") {
    uni.navigateTo({ url: "/pages/login/phone" });
  } else if (pendingAction.value === "wechat") {
    doWechatLogin();
  }
  pendingAction.value = null;
}

function onPhone() {
  if (!agreed.value) {
    promptAgreement("phone");
    return;
  }
  uni.navigateTo({ url: "/pages/login/phone" });
}

function onWechat() {
  if (!agreed.value) {
    promptAgreement("wechat");
    return;
  }
  doWechatLogin();
}

async function doWechatLogin() {
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
  } catch (e: any) {
    uni.hideLoading();
    uni.showToast({
      title: e?.message === "WX_LOGIN_NO_CODE" ? "微信授权失败" : "微信登录失败",
      icon: "none",
    });
  }
}

function onClose() {
  uni.switchTab({ url: "/pages/index/index" });
}
</script>

<style scoped lang="scss">
@import "@/styles/auth.scss";

.page {
  @include auth-page;
  padding: 80rpx $auth-page-padding;
  display: flex;
  flex-direction: column;
}

.close {
  font-size: 56rpx;
  color: #595959;
  line-height: 1;
  width: 64rpx;
}

.brand {
  margin-top: 80rpx;
  text-align: center;
}

.logo {
  width: 144rpx;
  height: 144rpx;
  border-radius: 32rpx;
  background: $auth-primary;
  color: #fff;
  font-size: 72rpx;
  font-weight: 700;
  margin: 0 auto 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome {
  font-size: 44rpx;
  font-weight: 600;
  color: $auth-text;
}

.actions {
  margin-top: 96rpx;
  width: 100%;
}

.btn {
  width: 100%;
  height: $auth-btn-height;
  border-radius: $auth-btn-radius;
  font-size: 34rpx;
  margin-bottom: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  &::after {
    border: none;
  }
  &.disabled {
    opacity: 0.5;
  }
}

.btn.primary {
  background: $auth-primary;
  color: #fff;
  font-weight: 600;
}

.btn.wx {
  background: #fff;
  color: $auth-text;
  border: 1rpx solid $auth-border;
  font-weight: 500;
  gap: 16rpx;
}

.wx-ico {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: $auth-wechat;
  color: #fff;
  font-size: 22rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.proto-row {
  margin-top: 48rpx;
  width: 100%;
}

.sheet-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 100;
  display: flex;
  align-items: flex-end;
}

.sheet {
  width: 100%;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  padding: 48rpx 48rpx calc(48rpx + env(safe-area-inset-bottom));
}

.sheet-title {
  font-size: 32rpx;
  font-weight: 600;
  color: $auth-text;
  margin-bottom: 32rpx;
}

.sheet-btn {
  @include auth-primary-btn;
  margin-top: 32rpx;
}
</style>
