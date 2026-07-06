<template>
  <view class="page">
    <StepIndicator :current="0" />
    <ConfirmOrderSkeleton v-if="loading" />

    <template v-else>
      <view class="card address">
        <view class="card-head">
          <text class="card-icon">📍</text>
          <text class="card-title">收货地址</text>
        </view>
        <view v-if="!addresses.length" class="empty-addr" @click="goAddAddress">
          <text class="empty-addr-text">+ 新增收货地址</text>
        </view>
        <view v-else class="addr-list">
          <view
            v-for="a in addresses"
            :key="a.id"
            class="addr-item"
            :class="{ active: a.id === selectedAddressId }"
            @click="selectedAddressId = a.id"
          >
            <view class="addr-main">
              <text class="receiver">{{ a.receiver }} {{ a.phone }}</text>
              <text v-if="a.isDefault === 1" class="tag">默认</text>
            </view>
            <text class="addr-detail">{{ a.fullAddress }}</text>
          </view>
        </view>
      </view>

      <view class="card">
        <view class="card-head">
          <text class="card-icon">🛍️</text>
          <text class="card-title">商品清单</text>
          <text class="card-extra">{{ items.length }} 件</text>
        </view>
        <view v-for="item in items" :key="item.skuId" class="row">
          <image class="thumb" :src="resolveMediaUrl(item.mainImage)" mode="aspectFill" />
          <view class="info">
            <text class="title">{{ item.title }}</text>
            <text class="spec">{{ item.specText }}</text>
            <view class="line">
              <text class="price">¥{{ item.price }}</text>
              <text class="qty">x{{ item.quantity }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="card">
        <view class="fee-row"><text>商品金额</text><text>¥{{ totalAmount.toFixed(2) }}</text></view>
        <view class="fee-row"><text>运费</text><text>¥0.00</text></view>
      </view>

      <view class="card">
        <view class="remark-row">
          <text>备注</text>
          <input v-model="remark" class="remark-input" placeholder="选填" maxlength="100" />
        </view>
      </view>
    </template>

    <FixedActionBar>
      <template #left>
        <view class="footer-left">
          <text class="total-label">应付</text>
          <text class="total-amount">¥{{ totalAmount.toFixed(2) }}</text>
        </view>
      </template>
      <button class="submit" :disabled="submitting" @click="onSubmit">提交订单</button>
    </FixedActionBar>
  </view>
</template>

<script setup lang="ts">
import { onShow } from "@dcloudio/uni-app";
import { ref } from "vue";
import { checkoutPreview, type CartItem } from "@/api/cart";
import { listAddresses, type AddressItem } from "@/api/address";
import { createOrder } from "@/api/order";
import FixedActionBar from "@/components/FixedActionBar.vue";
import StepIndicator from "@/components/StepIndicator.vue";
import ConfirmOrderSkeleton from "@/components/ConfirmOrderSkeleton.vue";
import { resolveMediaUrl } from "@/utils/media";
import { showError } from "@/utils/error-message";

const loading = ref(true);
const submitting = ref(false);
const items = ref<CartItem[]>([]);
const totalAmount = ref(0);
const addresses = ref<AddressItem[]>([]);
const selectedAddressId = ref<number | null>(null);
const remark = ref("");

async function load() {
  loading.value = true;
  try {
    const [preview, addrRes] = await Promise.all([checkoutPreview(), listAddresses()]);
    items.value = preview.data?.items || [];
    totalAmount.value = Number(preview.data?.totalAmount || 0);
    addresses.value = addrRes.data || [];
    const def = addresses.value.find((a) => a.isDefault === 1) || addresses.value[0];
    selectedAddressId.value = def ? def.id : null;
  } catch (e) {
    showError(e, "加载失败");
    setTimeout(() => uni.navigateBack(), 800);
  } finally {
    loading.value = false;
  }
}

function goAddAddress() {
  uni.navigateTo({ url: "/pages/address/edit" });
}

async function onSubmit() {
  if (!selectedAddressId.value) {
    uni.showToast({ title: "请选择收货地址", icon: "none" });
    return;
  }
  submitting.value = true;
  try {
    const res = await createOrder(selectedAddressId.value, remark.value || undefined);
    const orderNo = res.data?.orderNo;
    const payAmount = res.data?.payAmount;
    uni.redirectTo({
      url: `/pages/order/pay?orderNo=${orderNo}&amount=${payAmount}`,
    });
  } catch (e) {
    showError(e, "下单失败");
  } finally {
    submitting.value = false;
  }
}

onShow(() => {
  load();
});
</script>

<style scoped lang="scss">

.page {
  @include page-shell;
  padding-bottom: 140rpx;
}

.hint {
  text-align: center;
  padding: 120rpx;
  color: $color-text-secondary;
}

.card {
  @include card;
  margin: 16rpx $spacing-page;
  padding: 24rpx;
}

.card-head {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.card-icon {
  font-size: 32rpx;
  margin-right: 10rpx;
}

.card-title {
  font-size: 28rpx;
  font-weight: 600;
  flex: 1;
}

.card-extra {
  font-size: 24rpx;
  color: $color-text-secondary;
}

.empty-addr {
  padding: 32rpx;
  text-align: center;
  border: 2rpx dashed rgba(255, 77, 79, 0.35);
  border-radius: 12rpx;
  background: $color-primary-light;
}

.empty-addr-text {
  color: $color-primary;
  font-size: 28rpx;
  font-weight: 500;
}
.addr-item {
  padding: 16rpx;
  border: 2rpx solid #eee;
  border-radius: 12rpx;
  margin-bottom: 12rpx;
}
.addr-item.active {
  border-color: $color-primary;
  background: $color-primary-light;
}
.addr-main {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.receiver {
  font-size: 28rpx;
  font-weight: 600;
}
.tag {
  @include tag-default;
  color: #fff;
  background: $color-primary;
}
.addr-detail {
  display: block;
  font-size: 24rpx;
  color: #666;
  margin-top: 8rpx;
}
.row {
  display: flex;
  gap: 16rpx;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
}
.thumb {
  width: 120rpx;
  height: 120rpx;
  border-radius: 8rpx;
  background: #eee;
}
.info {
  flex: 1;
  min-width: 0;
}
.title {
  font-size: 26rpx;
  display: block;
}
.spec {
  font-size: 22rpx;
  color: #999;
  display: block;
  margin-top: 6rpx;
}
.line {
  display: flex;
  justify-content: space-between;
  margin-top: 12rpx;
}
.price {
  @include price-text;
}
.qty {
  color: #999;
}
.fee-row {
  display: flex;
  justify-content: space-between;
  font-size: 26rpx;
  padding: 8rpx 0;
  color: #666;
}
.remark-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-size: 26rpx;
}
.remark-input {
  flex: 1;
  height: 56rpx;
}

.footer-left {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  min-width: 0;
}

.total-label {
  font-size: 22rpx;
  color: $color-text-secondary;
}

.total-amount {
  @include price-text(34rpx);
  font-weight: 700;
}

.submit {
  @include btn-primary;
  border-radius: 40rpx;
  padding: 0 56rpx;
  font-size: 28rpx;
  height: 80rpx;
  line-height: 80rpx;
  margin: 0;
  flex-shrink: 0;
  width: auto;
}
</style>
