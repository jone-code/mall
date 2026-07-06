<template>
  <view class="page">
    <view v-if="!userStore.isLogin" class="center">
      <EmptyState text="请先登录" icon="🔐">
        <button class="btn" @click="goLogin">去登录</button>
      </EmptyState>
    </view>

    <template v-else>
      <view class="head">
        <button class="add-btn" @click="goEdit()">+ 新增地址</button>
      </view>

      <ListSkeleton v-if="loading" />

      <view v-else-if="!rows.length" class="center">
        <EmptyState text="暂无收货地址" icon="📍">
          <button class="btn" @click="goEdit()">新增地址</button>
        </EmptyState>
      </view>

      <template v-else>
        <view v-for="item in rows" :key="item.id" class="card">
          <view class="pin">📍</view>
          <view class="card-body">
            <view class="top">
              <text class="name">{{ item.receiver }}</text>
              <text class="phone">{{ item.phone }}</text>
              <text v-if="item.isDefault === 1" class="tag">默认</text>
            </view>
            <text class="addr">{{ item.fullAddress || formatAddr(item) }}</text>
            <view class="actions">
              <text v-if="item.isDefault !== 1" class="action" @click="onDefault(item.id)">设为默认</text>
              <text class="action" @click="goEdit(item.id)">编辑</text>
              <text class="action danger" @click="onDelete(item.id)">删除</text>
            </view>
          </view>
        </view>
      </template>
    </template>
  </view>
</template>

<script setup lang="ts">
import { onShow, onPullDownRefresh } from "@dcloudio/uni-app";
import { ref } from "vue";
import {
  deleteAddress,
  listAddresses,
  setDefaultAddress,
  type AddressItem,
} from "@/api/address";
import { useUserStore } from "@/stores/user";
import EmptyState from "@/components/EmptyState.vue";
import ListSkeleton from "@/components/ListSkeleton.vue";
import { showError } from "@/utils/error-message";

const userStore = useUserStore();
const loading = ref(false);
const rows = ref<AddressItem[]>([]);

function formatAddr(item: AddressItem) {
  return `${item.province}${item.city}${item.district}${item.detail}`;
}

async function load() {
  if (!userStore.isLogin) return;
  loading.value = true;
  try {
    const res = await listAddresses();
    rows.value = res.data || [];
  } catch (e) {
    showError(e, "加载失败");
  } finally {
    loading.value = false;
    uni.stopPullDownRefresh();
  }
}

function goLogin() {
  uni.navigateTo({ url: "/pages/login/index" });
}

function goEdit(id?: number) {
  const q = id ? `?id=${id}` : "";
  uni.navigateTo({ url: `/pages/address/edit${q}` });
}

async function onDefault(id: number) {
  await setDefaultAddress(id);
  await load();
}

async function onDelete(id: number) {
  uni.showModal({
    title: "确认删除该地址？",
    success: async (res) => {
      if (res.confirm) {
        await deleteAddress(id);
        await load();
      }
    },
  });
}

onShow(load);
onPullDownRefresh(load);
</script>

<style scoped lang="scss">

.page {
  @include page-shell;
  padding: $spacing-page;
}

.center {
  padding-top: 80rpx;
}

.hint {
  text-align: center;
  padding: 80rpx 0;
  color: $color-text-secondary;
}

.btn {
  margin-top: 32rpx;
  width: 320rpx;
  height: 80rpx;
  line-height: 80rpx;
  @include btn-primary;
  font-size: 28rpx;
}

.head {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16rpx;
}

.add-btn {
  font-size: 28rpx;
  background: $color-bg-card;
  color: $color-primary;
  border-radius: $radius-pill;
  padding: 0 28rpx;
  height: 64rpx;
  line-height: 64rpx;
  margin: 0;
  box-shadow: 0 2rpx 12rpx $color-shadow;

  &::after {
    border: none;
  }
}

.card {
  @include card;
  padding: 0;
  margin-bottom: 16rpx;
  display: flex;
  overflow: hidden;
}

.pin {
  width: 72rpx;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 32rpx;
  font-size: 32rpx;
  background: linear-gradient(180deg, #fff1f0 0%, #fff 100%);
}

.card-body {
  flex: 1;
  padding: 28rpx 28rpx 28rpx 0;
  min-width: 0;
}

.top {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.name {
  font-weight: 600;
  font-size: 30rpx;
  color: $color-text-primary;
}

.phone {
  color: $color-text-secondary;
  font-size: 28rpx;
}

.tag {
  @include tag-default;
}

.addr {
  display: block;
  margin-top: 16rpx;
  color: $color-text-secondary;
  font-size: 26rpx;
  line-height: 1.6;
}

.actions {
  display: flex;
  gap: 32rpx;
  margin-top: 24rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f5f5f5;
}

.action {
  font-size: 26rpx;
  color: $color-text-secondary;
}

.danger {
  color: $color-primary;
}
</style>
