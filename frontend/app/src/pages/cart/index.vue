<template>
  <view class="page">
    <view v-if="!userStore.isLogin" class="center">
      <EmptyState text="登录后查看购物车" desc="登录后可同步购物车商品" icon="🛒">
        <button class="btn" @click="goLogin">去登录</button>
      </EmptyState>
    </view>

    <view v-else-if="loading" class="body">
      <CartSkeleton />
    </view>

    <view v-else-if="!items.length" class="center">
      <EmptyState text="购物车空空如也" desc="在商品详情页可加入购物车或立即购买" icon="🛍️">
        <button class="btn outline" @click="goHome">去逛逛</button>
      </EmptyState>
    </view>

    <view v-else class="body">
      <view class="shop-bar">
        <text class="shop-name">ComonOn 自营</text>
        <text class="shop-count">共 {{ items.length }} 件商品</text>
      </view>

      <view class="toolbar">
        <label class="check-all" @click="toggleAll">
          <checkbox :checked="allSelected" color="#1A1A1A" />
          <text>全选</text>
        </label>
        <text v-if="invalidCount > 0" class="clear-invalid" @click="onClearInvalid">
          清除失效商品
        </text>
      </view>

      <view
        v-for="item in items"
        :key="item.skuId"
        class="row"
        :class="{ invalid: item.invalid }"
      >
        <checkbox
          :checked="!!item.selected"
          :disabled="!!item.invalid"
          color="#1A1A1A"
          @click="toggleItem(item)"
        />
        <image class="thumb" :src="resolveMediaUrl(item.mainImage)" mode="aspectFill" />
        <view class="info">
          <text class="title">{{ item.title }}</text>
          <text class="spec">{{ item.specText }}</text>
          <text v-if="item.invalid" class="bad">{{ invalidReasonLabel(item.invalidReason) }}</text>
          <text v-else-if="item.stockInsufficient" class="bad">库存不足</text>
          <view class="bottom">
            <text class="price">¥{{ item.price }}</text>
            <view v-if="!item.invalid" class="stepper">
              <text class="step" @click="changeQty(item, -1)">-</text>
              <text class="num">{{ item.quantity }}</text>
              <text
                class="step"
                :class="{ disabled: isQtyLocked(item) }"
                @click="changeQty(item, 1)"
              >+</text>
            </view>
            <text v-else class="del" @click="remove(item)">删除</text>
          </view>
        </view>
      </view>

      <FixedActionBar above-tab-bar>
        <template #left>
          <view class="footer-left">
            <text class="total-label">合计</text>
            <text class="total-amount">¥{{ displayAmount.toFixed(2) }}</text>
            <text class="total-qty">已选 {{ selectedQty }} 件</text>
          </view>
        </template>
        <button class="checkout" @click="onCheckout">去结算({{ selectedQty }})</button>
      </FixedActionBar>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onShow, onPullDownRefresh } from "@dcloudio/uni-app";
import { computed, ref } from "vue";
import {
  deleteCartItem,
  deleteInvalidCartItems,
  getCart,
  invalidReasonLabel,
  selectAllCart,
  updateCartItem,
  type CartItem,
} from "@/api/cart";
import { useCartStore } from "@/stores/cart";
import { useUserStore } from "@/stores/user";
import EmptyState from "@/components/EmptyState.vue";
import CartSkeleton from "@/components/CartSkeleton.vue";
import FixedActionBar from "@/components/FixedActionBar.vue";
import { resolveMediaUrl } from "@/utils/media";
import { showError } from "@/utils/error-message";

const userStore = useUserStore();
const cartStore = useCartStore();

const loading = ref(false);
const items = ref<CartItem[]>([]);
const selectedAmount = ref(0);
const selectedQty = ref(0);
const invalidCount = ref(0);

const allSelected = computed(() => {
  const valid = items.value.filter((i) => !i.invalid);
  return valid.length > 0 && valid.every((i) => i.selected);
});

const displayAmount = computed(() => {
  const fromApi = selectedAmount.value;
  if (fromApi > 0) return fromApi;
  return items.value
    .filter((i) => i.selected && !i.invalid)
    .reduce((sum, i) => sum + Number(i.price || 0) * i.quantity, 0);
});

async function load() {
  if (!userStore.isLogin) return;
  loading.value = true;
  try {
    const res = await getCart();
    items.value = res.data?.items || [];
    selectedAmount.value = Number(res.data?.summary?.selectedAmount || 0);
    selectedQty.value = res.data?.summary?.selectedQuantity || 0;
    invalidCount.value = res.data?.summary?.invalidCount || 0;
    cartStore.totalQuantity = res.data?.summary?.totalQuantity || 0;
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

function goHome() {
  uni.switchTab({ url: "/pages/index/index" });
}

async function toggleAll() {
  await selectAllCart(!allSelected.value);
  await load();
}

async function toggleItem(item: CartItem) {
  if (item.invalid) return;
  await updateCartItem(item.skuId, { selected: !item.selected });
  await load();
}

async function changeQty(item: CartItem, delta: number) {
  if (delta > 0 && isQtyLocked(item) && item.quantity >= 1) {
    uni.showToast({ title: "虚拟/服务类商品限购 1 件", icon: "none" });
    return;
  }
  const next = item.quantity + delta;
  if (next < 1) return;
  try {
    await updateCartItem(item.skuId, { quantity: next });
    await load();
  } catch (e) {
    showError(e, "库存不足");
  }
}

function isQtyLocked(item: CartItem) {
  return item.productType === "VIRTUAL" || item.productType === "SERVICE";
}

async function remove(item: CartItem) {
  await deleteCartItem(item.skuId);
  await load();
}

async function onClearInvalid() {
  await deleteInvalidCartItems();
  await load();
}

function onCheckout() {
  if (selectedQty.value <= 0) {
    uni.showToast({ title: "请先勾选商品", icon: "none" });
    return;
  }
  uni.navigateTo({ url: "/pages/order/confirm" });
}

onShow(() => {
  load();
});

onPullDownRefresh(() => {
  load();
});
</script>

<style scoped lang="scss">

.page {
  @include page-shell;
  padding-bottom: calc(#{$tab-bar-height} + 140rpx + env(safe-area-inset-bottom));
}

.center {
  padding-top: 80rpx;
}

.hint-sub {
  display: block;
  text-align: center;
  margin-top: 12rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  line-height: 1.5;
}

.btn {
  margin-top: 32rpx;
  width: 320rpx;
  height: 80rpx;
  line-height: 80rpx;
  @include btn-primary;
  font-size: 28rpx;

  &.outline {
    @include btn-outline;
    background: #fff;
  }
}

.shop-bar {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 20rpx $spacing-page 8rpx;
}

.shop-name {
  font-size: 30rpx;
  font-weight: 700;
  color: $color-text-primary;
}

.shop-count {
  font-size: 24rpx;
  color: $color-text-secondary;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx $spacing-page 20rpx;
  background: $color-bg-card;
  margin: 0 $spacing-page 16rpx;
  border-radius: $radius-card;
  box-shadow: 0 2rpx 12rpx $color-shadow;
}

.check-all {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 28rpx;
  color: $color-text-primary;
}

.clear-invalid {
  color: $color-primary;
  font-size: 26rpx;
}

.row {
  display: flex;
  gap: 16rpx;
  @include card;
  padding: 20rpx 24rpx;
  margin: 0 $spacing-page 16rpx;
  align-items: flex-start;

  &.invalid {
    opacity: 0.65;
  }
}

.thumb {
  width: 160rpx;
  height: 160rpx;
  border-radius: 8rpx;
  background: #eee;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
}

.title {
  font-size: 28rpx;
  display: block;
  color: $color-text-primary;
  line-height: 1.4;
}

.spec {
  font-size: 24rpx;
  color: $color-text-secondary;
  margin-top: 8rpx;
  display: block;
}

.bad {
  color: $color-primary;
  font-size: 24rpx;
  margin-top: 8rpx;
  display: block;
}

.bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}

.price {
  @include price-text;
}

.stepper {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.step {
  @include stepper-btn;

  &.disabled {
    opacity: 0.35;
  }
}

.num {
  min-width: 40rpx;
  text-align: center;
  font-size: 28rpx;
}

.del {
  color: $color-primary;
  font-size: 26rpx;
}

.footer-left {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 8rpx;
  min-width: 0;
}

.total-label {
  font-size: 26rpx;
  color: $color-text-primary;
}

.total-amount {
  @include price-text(36rpx);
  font-weight: 700;
  line-height: 1.2;
}

.total-qty {
  font-size: 22rpx;
  color: $color-text-secondary;
}

.checkout {
  @include btn-primary;
  border-radius: 40rpx;
  font-size: 28rpx;
  padding: 0 40rpx;
  height: 80rpx;
  line-height: 80rpx;
  margin: 0;
  flex-shrink: 0;
}
</style>
