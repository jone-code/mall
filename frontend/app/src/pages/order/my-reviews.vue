<template>
  <view class="page">
    <ListSkeleton v-if="loading && !reviews.length" />
    <view v-else-if="!reviews.length" class="center">
      <EmptyState text="暂无评价" icon="💬">
        <button class="btn outline" @click="goOrders">去看看订单</button>
      </EmptyState>
    </view>
    <view v-else class="list">
      <view v-for="rv in reviews" :key="rv.id" class="card">
        <view class="head">
          <text class="stars">{{ '★'.repeat(rv.rating) }}</text>
          <RelativeTime :value="rv.createdAt" />
        </view>
        <text class="content">{{ rv.content }}</text>
        <view class="foot">
          <text class="order">订单 {{ rv.orderNo }}</text>
          <text class="link" @click="goOrder(rv.orderNo)">查看订单 ›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onShow, onPullDownRefresh } from "@dcloudio/uni-app";
import { ref } from "vue";
import { getMyReviews, type Review } from "@/api/review";
import EmptyState from "@/components/EmptyState.vue";
import ListSkeleton from "@/components/ListSkeleton.vue";
import RelativeTime from "@/components/RelativeTime.vue";
import { useUserStore } from "@/stores/user";
import { showError } from "@/utils/error-message";

const userStore = useUserStore();
const loading = ref(false);
const reviews = ref<Review[]>([]);

async function load() {
  if (!userStore.isLogin) {
    uni.navigateTo({ url: "/pages/login/index" });
    return;
  }
  loading.value = true;
  try {
    const res = await getMyReviews();
    reviews.value = res.data || [];
  } catch (e) {
    showError(e, "加载失败");
  } finally {
    loading.value = false;
    uni.stopPullDownRefresh();
  }
}

function goOrders() {
  uni.navigateTo({ url: "/pages/order/list" });
}

function goOrder(orderNo: string) {
  uni.navigateTo({ url: `/pages/order/detail?orderNo=${orderNo}` });
}

onShow(load);
onPullDownRefresh(load);
</script>

<style scoped lang="scss">
.page {
  @include page-shell;
  min-height: 100vh;
  padding: 24rpx;
}

.center {
  padding: 80rpx 0;
  text-align: center;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.card {
  @include card;
  padding: 28rpx;
}

.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.stars {
  color: #ffb400;
  font-size: 28rpx;
}

.content {
  font-size: 28rpx;
  color: $color-text-primary;
  line-height: 1.6;
}

.foot {
  margin-top: 20rpx;
  display: flex;
  justify-content: space-between;
  font-size: 24rpx;
  color: $color-text-secondary;
}

.link {
  color: $color-text-primary;
}

.btn {
  margin-top: 24rpx;
  @include btn-primary;
  font-size: 28rpx;
  height: 72rpx;
  line-height: 72rpx;
  width: 280rpx;

  &.outline {
    @include btn-outline;
    background: #fff;
  }
}
</style>
