<template>
  <view class="page">
    <view v-if="userStore.isLogin" class="profile">
      <view class="hero" @click="goProfile">
        <view class="hero-deco hero-deco-1" />
        <view class="hero-deco hero-deco-2" />
        <view class="avatar-wrap">
          <image
            v-if="userStore.user?.avatarUrl"
            class="avatar"
            :src="avatarSrc"
            mode="aspectFill"
          />
          <view v-else class="avatar avatar-placeholder">{{ avatarLetter }}</view>
        </view>
        <text class="nickname">{{ displayName }}</text>
        <text v-if="maskedPhone" class="phone">{{ maskedPhone }}</text>
        <text class="edit-hint">点击编辑资料 ›</text>
      </view>

      <view class="stats">
        <view class="stat" @click="goCart">
          <text class="stat-num">{{ cartStore.totalQuantity }}</text>
          <text class="stat-label">购物车</text>
        </view>
        <view class="stat-divider" />
        <view class="stat" @click="goOrders('PENDING_PAY')">
          <text class="stat-num">{{ pendingPayCount }}</text>
          <text class="stat-label">待支付</text>
        </view>
        <view class="stat-divider" />
        <view class="stat" @click="goOrders('')">
          <text class="stat-num">{{ orderTotal }}</text>
          <text class="stat-label">全部订单</text>
        </view>
      </view>

      <view class="order-panel">
        <view class="order-head" @click="goOrders('')">
          <text class="order-title">我的订单</text>
          <text class="order-all">全部 ›</text>
        </view>
        <view class="order-grid">
          <view class="order-item" @click="goOrders('PENDING_PAY')">
            <view class="order-icon-wrap"><text class="order-glyph">付</text></view>
            <text>待支付</text>
          </view>
          <view class="order-item" @click="goOrders('PAID')">
            <view class="order-icon-wrap"><text class="order-glyph">发</text></view>
            <text>待发货</text>
          </view>
          <view class="order-item" @click="goOrders('SHIPPED')">
            <view class="order-icon-wrap"><text class="order-glyph">收</text></view>
            <text>待收货</text>
          </view>
          <view class="order-item" @click="goOrders('COMPLETED')">
            <view class="order-icon-wrap"><text class="order-glyph">完</text></view>
            <text>已完成</text>
          </view>
        </view>
      </view>

      <view class="menu">
        <MenuCell icon="👤" label="编辑资料" icon-class="blue" @click="goProfile" />
        <MenuCell icon="📍" label="收货地址" icon-class="blue" @click="goAddress" />
        <MenuCell icon="📱" label="登录设备" icon-class="blue" @click="goSessions" />
        <MenuCell icon="💬" label="我的评价" icon-class="orange" @click="goMyReviews" />
        <MenuCell icon="🔍" label="搜索商品" icon-class="orange" @click="goSearch" />
        <MenuCell icon="ℹ️" label="关于 ComonOn" icon-class="green" @click="showAbout" />
      </view>

      <button class="btn outline logout" @click="onLogout">退出登录</button>
    </view>

    <view v-else class="guest">
      <view class="guest-card">
        <view class="guest-icon">C</view>
        <text class="guest-title">登录 ComonOn</text>
        <text class="guest-desc">登录后可查看订单、购物车与个人资料</text>
        <button class="btn" @click="goLogin">登录 / 注册</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import MenuCell from '@/components/MenuCell.vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { logout as logoutApi, getMe } from '@/api/auth'
import { listOrdersPage } from '@/api/order'
import { resolveMediaUrl } from '@/utils/media'

const userStore = useUserStore()
const cartStore = useCartStore()
const pendingPayCount = ref(0)
const orderTotal = ref(0)

const displayName = computed(
  () => userStore.user?.nickname || `用户_${userStore.user?.id ?? ''}`
)

const avatarLetter = computed(() => {
  const name = displayName.value
  return name ? name.slice(0, 1).toUpperCase() : 'U'
})

const maskedPhone = computed(() => {
  const phone = userStore.user?.phone
  if (!phone || phone.length < 7) return ''
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
})

const avatarSrc = computed(() => resolveMediaUrl(userStore.user?.avatarUrl))

function goLogin() {
  uni.navigateTo({ url: '/pages/login/index' })
}

function goSearch() {
  uni.navigateTo({ url: '/pages/search/index' })
}

function goCart() {
  uni.switchTab({ url: '/pages/cart/index' })
}

function goProfile() {
  if (!userStore.isLogin) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/mine/profile' })
}

function goAddress() {
  if (!userStore.isLogin) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/address/list' })
}

function goMyReviews() {
  if (!userStore.isLogin) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/order/my-reviews' })
}

function goSessions() {
  if (!userStore.isLogin) {
    goLogin()
    return
  }
  uni.navigateTo({ url: '/pages/mine/sessions' })
}

function goOrders(status = '') {
  if (!userStore.isLogin) {
    goLogin()
    return
  }
  const q = status ? `?status=${status}` : ''
  uni.navigateTo({ url: `/pages/order/list${q}` })
}

function showAbout() {
  uni.showModal({
    title: 'ComonOn',
    content: '品质生活，一站购齐\nDemo 商城应用',
    showCancel: false,
  })
}

onShow(() => {
  if (userStore.isLogin) {
    cartStore.refreshBadge()
    loadOrderStats()
    syncProfile()
  } else {
    pendingPayCount.value = 0
    orderTotal.value = 0
  }
})

onPullDownRefresh(async () => {
  if (userStore.isLogin) {
    await Promise.all([cartStore.refreshBadge(), loadOrderStats(), syncProfile()])
  }
  uni.stopPullDownRefresh()
})

async function syncProfile() {
  try {
    const res = await getMe()
    if (res.data) {
      userStore.setUser({
        ...userStore.user,
        ...res.data,
        phone: res.data.phone || userStore.user?.phone,
      })
    }
  } catch {
    /* ignore */
  }
}

async function loadOrderStats() {
  try {
    const res = await listOrdersPage(undefined, 1, 50)
    const list = res.data?.list || []
    orderTotal.value = res.data?.total ?? list.length
    pendingPayCount.value = list.filter((o) => o.status === 'PENDING_PAY').length
  } catch {
    pendingPayCount.value = 0
    orderTotal.value = 0
  }
}

async function onLogout() {
  try {
    await logoutApi()
  } catch {
    /* ignore */
  }
  userStore.clear()
  cartStore.clear()
  uni.showToast({ title: '已退出', icon: 'none' })
}
</script>

<style scoped lang="scss">
.page {
  @include page-shell;
  min-height: 100vh;
}

.profile {
  padding-bottom: 48rpx;
}

.hero {
  position: relative;
  overflow: hidden;
  background: $color-primary;
  padding: 88rpx 32rpx 64rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero-deco {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.hero-deco-1 {
  width: 240rpx;
  height: 240rpx;
  top: -60rpx;
  right: -40rpx;
}

.hero-deco-2 {
  width: 160rpx;
  height: 160rpx;
  bottom: 20rpx;
  left: -40rpx;
}

.avatar-wrap {
  margin-bottom: 24rpx;
  position: relative;
  z-index: 1;
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255, 255, 255, 0.85);
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
}

.avatar-placeholder {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  font-size: 56rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nickname {
  font-size: 36rpx;
  font-weight: 600;
  color: #fff;
  position: relative;
  z-index: 1;
}

.phone {
  margin-top: 12rpx;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
  position: relative;
  z-index: 1;
}

.edit-hint {
  margin-top: 16rpx;
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.65);
  position: relative;
  z-index: 1;
  letter-spacing: 1rpx;
}

.stats {
  margin: -28rpx 24rpx 0;
  @include card;
  display: flex;
  align-items: center;
  padding: 28rpx 0;
  position: relative;
  z-index: 2;
}

.stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.stat-num {
  font-size: 36rpx;
  font-weight: 500;
  color: $color-text-primary;
  letter-spacing: 2rpx;
}

.stat-label {
  font-size: 24rpx;
  color: $color-text-secondary;
}

.stat-divider {
  width: 1rpx;
  height: 48rpx;
  background: #f0f0f0;
}

.order-panel {
  margin: 20rpx 24rpx 0;
  @include card;
  padding: 24rpx 28rpx 32rpx;
}

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.order-title {
  font-size: 30rpx;
  font-weight: 600;
  color: $color-text-primary;
}

.order-all {
  font-size: 26rpx;
  color: $color-text-secondary;
}

.order-grid {
  display: flex;
  justify-content: space-around;
}

.order-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  font-size: 24rpx;
  color: #595959;
}

.order-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1rpx solid $color-border;
  background: $color-bg-section;
}

.order-glyph {
  font-size: 28rpx;
  color: $color-text-primary;
  font-weight: 500;
  letter-spacing: 2rpx;
}

.menu {
  margin: 20rpx 24rpx 0;
  @include card;
  overflow: hidden;
  padding: 0;
}

.btn {
  width: calc(100% - 48rpx);
  margin: 32rpx 24rpx 0;
  height: 88rpx;
  line-height: 88rpx;
  @include btn-primary;
  font-size: 30rpx;
}

.btn.outline {
  @include btn-outline;
  background: #fff;
}

.logout {
  margin-top: 48rpx;
}

.guest {
  padding: 120rpx 32rpx 0;
}

.guest-card {
  @include card;
  padding: 64rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.guest-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 0;
  background: $color-primary;
  color: #fff;
  font-size: 56rpx;
  font-weight: 500;
  letter-spacing: 4rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28rpx;
  border: 1rpx solid $color-border;
}

.guest-title {
  font-size: 36rpx;
  font-weight: 600;
  color: $color-text-primary;
}

.guest-desc {
  margin-top: 16rpx;
  font-size: 26rpx;
  color: $color-text-secondary;
  line-height: 1.5;
}

.guest-card .btn {
  width: 100%;
  margin: 40rpx 0 0;
}
</style>
