<template>
  <view class="page">
    <view class="title">完善资料（可跳过）</view>

    <view class="avatar-wrap" aria-label="选择头像" @click="chooseAvatar">
      <image v-if="avatarUrl" class="avatar" :src="avatarUrl" mode="aspectFill" />
      <view v-else class="avatar avatar-placeholder">+</view>
    </view>

    <view class="field">
      <text class="label">昵称</text>
      <input
        class="input"
        :value="nickname"
        maxlength="16"
        placeholder="请输入昵称"
        @input="onNick"
      />
    </view>

    <view class="actions">
      <button class="primary" aria-label="开始逛逛" @click="onSubmit">开始逛逛</button>
      <view class="skip" aria-label="跳过" @click="onSkip">跳过 →</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { updateMe } from "@/api/auth";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const nickname = ref(userStore.user?.nickname || "");
const avatarUrl = ref(userStore.user?.avatarUrl || "");

function onNick(e: any) {
  nickname.value = e.detail.value;
}

function chooseAvatar() {
  uni.chooseImage({
    count: 1,
    success: (res: any) => {
      avatarUrl.value = res.tempFilePaths[0];
    },
  });
}

async function onSubmit() {
  if (!nickname.value || nickname.value.length < 2) {
    uni.showToast({ title: "昵称至少 2 个字符", icon: "none" });
    return;
  }
  try {
    await updateMe({ nickname: nickname.value, avatarUrl: avatarUrl.value });
    userStore.setUser({
      ...(userStore.user || {}),
      nickname: nickname.value,
      avatarUrl: avatarUrl.value,
    });
  } catch {
    uni.showToast({ title: "资料保存失败，已进入首页", icon: "none" });
  }
  uni.switchTab({ url: "/pages/mine/index" });
}

function onSkip() {
  uni.switchTab({ url: "/pages/mine/index" });
}
</script>

<style scoped lang="scss">
@import "@/styles/auth.scss";

.page {
  @include auth-page;
  padding: 64rpx $auth-page-padding;
}

.title {
  font-size: 40rpx;
  font-weight: 700;
  color: $auth-text;
  text-align: center;
}

.avatar-wrap {
  margin: 64rpx auto 48rpx;
  width: 176rpx;
  height: 176rpx;
}

.avatar {
  width: 176rpx;
  height: 176rpx;
  border-radius: 50%;
  background: #f0f0f0;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64rpx;
  font-weight: 300;
  color: #bfbfbf;
  border: 2rpx solid $auth-border;
}

.field {
  width: 100%;
}

.label {
  display: block;
  color: $auth-text-secondary;
  font-size: 28rpx;
  margin-bottom: 16rpx;
}

.input {
  width: 100%;
  height: 96rpx;
  padding: 0 32rpx;
  box-sizing: border-box;
  font-size: 32rpx;
  color: $auth-text;
  background: #fff;
  border: 1rpx solid $auth-border;
  border-radius: $auth-btn-radius;
}

.actions {
  margin-top: 48rpx;
  width: 100%;
}

.primary {
  @include auth-primary-btn;
}

.skip {
  text-align: center;
  margin-top: 32rpx;
  color: $auth-text-secondary;
  font-size: 28rpx;
}
</style>
