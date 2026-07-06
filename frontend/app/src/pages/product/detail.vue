<template>
  <view class="page" v-if="detail">
    <scroll-view scroll-y class="scroll-body" :show-scrollbar="false">
      <swiper class="banner" circular indicator-dots indicator-color="rgba(255,255,255,.5)" indicator-active-color="#1A1A1A">
        <swiper-item v-for="(img, i) in gallery" :key="i">
          <image class="banner-img" :src="img" mode="aspectFill" />
        </swiper-item>
      </swiper>

      <view class="panel price-panel">
        <view class="price-row">
          <text class="price">¥{{ selectedSku?.price ?? '—' }}</text>
          <text v-if="selectedSku?.marketPrice" class="market">¥{{ selectedSku.marketPrice }}</text>
        </view>
        <text class="title">{{ detail.title }}</text>
        <text v-if="detail.subtitle" class="subtitle">{{ detail.subtitle }}</text>
        <view class="tags">
          <TypeBadge :type="detail.productType" />
          <text class="stock">{{ stockLabel }}</text>
        </view>
      </view>

      <view v-if="specNames.length" class="panel sku-panel" @click="showSkuHint">
        <view class="sku-row">
          <text class="sku-label">已选</text>
          <text class="sku-value">{{ selectedSpecText }}</text>
          <text class="chev">›</text>
        </view>
        <view v-for="name in specNames" :key="name" class="spec-row">
          <text class="spec-name">{{ name }}</text>
          <view class="spec-values">
            <view
              v-for="val in specValues(name)"
              :key="val"
              :class="['chip', chipClass(name, val)]"
              @click.stop="pickSpec(name, val)"
            >
              {{ val }}
            </view>
          </view>
        </view>
      </view>

      <ServiceGuarantee />

      <view class="panel review-panel">
        <view class="review-head">
          <text class="panel-title">用户评价</text>
          <text v-if="reviewSummary.count" class="review-score">
            {{ Number(reviewSummary.avgRating).toFixed(1) }} 分 · {{ reviewSummary.count }} 条
          </text>
          <text v-else class="review-score muted">暂无评价</text>
        </view>
        <view v-for="rv in reviews" :key="rv.id" class="review-item">
          <view class="review-meta">
            <text class="review-user">{{ rv.userNickname || '用户' }}</text>
            <text class="review-stars">{{ '★'.repeat(rv.rating) }}</text>
          </view>
          <text class="review-content">{{ rv.content }}</text>
          <text class="review-time"><RelativeTime :value="rv.createdAt" /></text>
        </view>
        <view
          v-if="reviewSummary.count > reviews.length"
          class="review-more"
          @click="loadMoreReviews"
        >
          {{ loadingReviews ? '加载中…' : `查看更多评价（${reviewSummary.count}）` }}
        </view>
      </view>

      <view class="panel detail-panel">
        <text class="panel-title">商品详情</text>
        <rich-text v-if="detailHtml" class="detail-rich" :nodes="detailHtml" />
        <text v-else class="detail-fallback">{{ detail.subtitle || '暂无更多详情' }}</text>
      </view>

      <view class="scroll-tail" />
    </scroll-view>

    <FixedActionBar full>
      <view class="product-bar">
        <view class="cart-entry" @click="goCart">
          <GlyphIcon type="cart" theme="black" :size="40" />
          <text class="cart-label">购物车</text>
        </view>
        <view class="bar-actions">
          <button class="btn cart-btn" :disabled="!canAddCart" @click="onAddCart">加入购物车</button>
          <button class="btn buy-btn" :disabled="!canAddCart" @click="onBuyNow">立即购买</button>
        </view>
      </view>
    </FixedActionBar>
  </view>
  <ProductDetailSkeleton v-else />
</template>

<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import FixedActionBar from '@/components/FixedActionBar.vue'
import GlyphIcon from '@/components/GlyphIcon.vue'
import ServiceGuarantee from '@/components/ServiceGuarantee.vue'
import TypeBadge from '@/components/TypeBadge.vue'
import {
  getProductDetail,
  type SkuDetail,
  type SpuDetail
} from '@/api/product'
import {
  getProductReviewSummary,
  getProductReviews,
  type Review,
  type ReviewSummary
} from '@/api/review'
import RelativeTime from '@/components/RelativeTime.vue'
import ProductDetailSkeleton from '@/components/ProductDetailSkeleton.vue'
import { resolveMediaUrl, resolveRichHtml } from '@/utils/media'
import { showError } from '@/utils/error-message'
import { addCartItem, selectAllCart, updateCartItem } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const cartStore = useCartStore()

const detail = ref<SpuDetail | null>(null)
const spuId = ref(0)
const selectedSkuId = ref<number | null>(null)
const reviews = ref<Review[]>([])
const reviewSummary = ref<ReviewSummary>({ count: 0, avgRating: 0 })
const reviewPage = ref(1)
const loadingReviews = ref(false)

const gallery = computed(() => {
  if (!detail.value) return []
  const imgs = detail.value.images?.length ? detail.value.images : [detail.value.mainImage]
  return imgs.filter(Boolean).map((u) => resolveMediaUrl(u))
})

const detailHtml = computed(() => resolveRichHtml(detail.value?.detailHtml))

const selectedSku = computed(() =>
  detail.value?.skus.find((s) => s.id === selectedSkuId.value)
)

const stockLabel = computed(() => {
  const sku = selectedSku.value
  const avail = sku?.available ?? 0
  const pt = detail.value?.productType
  if (pt === 'VIRTUAL') {
    const pool = detail.value?.poolAvailable ?? avail
    return `码池剩余 ${pool} 份`
  }
  if (pt === 'SERVICE') {
    const pool = detail.value?.poolAvailable ?? avail
    return `核销码剩余 ${pool} 份`
  }
  return `库存 ${avail} 件`
})

const selectedSpecText = computed(() => selectedSku.value?.specText || '请选择规格')

const canAddCart = computed(() => {
  const sku = selectedSku.value
  return sku && sku.available > 0
})

const specNames = computed(() => {
  const names = new Set<string>()
  detail.value?.skus.forEach((sku) => {
    sku.specJson?.dims?.forEach((d) => names.add(d.name))
  })
  return Array.from(names)
})

const picked = ref<Record<string, string>>({})

function specValues(name: string) {
  const vals = new Set<string>()
  detail.value?.skus.forEach((sku) => {
    sku.specJson?.dims?.forEach((d) => {
      if (d.name === name) vals.add(d.value)
    })
  })
  return Array.from(vals)
}

function pickSpec(name: string, value: string) {
  picked.value = { ...picked.value, [name]: value }
  matchSku()
}

function chipClass(name: string, value: string) {
  const active = picked.value[name] === value
  const sku = findSkuFor({ ...picked.value, [name]: value })
  if (!sku || sku.available <= 0) return 'disabled'
  return active ? 'on' : ''
}

function findSkuFor(sel: Record<string, string>): SkuDetail | undefined {
  return detail.value?.skus.find((sku) => {
    const dims = sku.specJson?.dims || []
    return dims.every((d) => !sel[d.name] || sel[d.name] === d.value)
  })
}

function matchSku() {
  const sku = findSkuFor(picked.value)
  if (sku && sku.available > 0) {
    selectedSkuId.value = sku.id
  }
}

function initDefault() {
  if (!detail.value?.skus.length) return
  const def =
    detail.value.skus.find((s) => s.isDefault === 1 && s.available > 0) ||
    detail.value.skus.find((s) => s.available > 0) ||
    detail.value.skus[0]
  def?.specJson?.dims?.forEach((d) => {
    picked.value[d.name] = d.value
  })
  selectedSkuId.value = def?.id ?? null
}

function showSkuHint() {
  /* 规格已在页面展示，点击行仅作视觉反馈 */
}

async function onAddCart() {
  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  const sku = selectedSku.value
  if (!sku || sku.available <= 0) {
    uni.showToast({ title: '请选择有货规格', icon: 'none' })
    return
  }
  try {
    await addCartItem(sku.id, 1)
    await cartStore.refreshBadge()
    uni.showToast({ title: '已加入购物车', icon: 'success' })
  } catch (e) {
    showError(e, '加购失败')
  }
}

function goCart() {
  uni.switchTab({ url: '/pages/cart/index' })
}

async function onBuyNow() {
  if (!userStore.isLogin) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  const sku = selectedSku.value
  if (!sku || sku.available <= 0) {
    uni.showToast({ title: '请选择有货规格', icon: 'none' })
    return
  }
  try {
    await addCartItem(sku.id, 1)
    await selectAllCart(false)
    await updateCartItem(sku.id, { selected: true })
    await cartStore.refreshBadge()
    uni.navigateTo({ url: '/pages/order/confirm' })
  } catch (e) {
    showError(e, '下单失败')
  }
}

async function loadMoreReviews() {
  if (!spuId.value || loadingReviews.value) return
  if (reviews.value.length >= reviewSummary.value.count) return
  loadingReviews.value = true
  try {
    reviewPage.value += 1
    const res = await getProductReviews(spuId.value, reviewPage.value, 5)
    reviews.value = [...reviews.value, ...(res.data?.list || [])]
  } catch (e) {
    reviewPage.value -= 1
    showError(e, '加载失败')
  } finally {
    loadingReviews.value = false
  }
}

onLoad(async (query) => {
  const id = Number(query?.id)
  if (!id) return
  spuId.value = id
  reviewPage.value = 1
  try {
    const res = await getProductDetail(id)
    detail.value = res.data
    initDefault()
    const [sumRes, listRes] = await Promise.all([
      getProductReviewSummary(id),
      getProductReviews(id, 1, 5)
    ])
    reviewSummary.value = sumRes.data || { count: 0, avgRating: 0 }
    reviews.value = listRes.data?.list || []
  } catch (e) {
    showError(e, '加载失败')
    setTimeout(() => uni.navigateBack(), 800)
  }
})
</script>

<style lang="scss">
.page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: $color-bg-page;
  box-sizing: border-box;
  overflow: hidden;
}

.scroll-body {
  flex: 1;
  height: 0;
  width: 100%;
}

.scroll-tail {
  height: 32rpx;
}

.loading {
  text-align: center;
  padding: 80rpx;
  color: $color-text-secondary;
}

.banner {
  height: 750rpx;
  background: #eee;
}

.banner-img {
  width: 100%;
  height: 100%;
}

.panel {
  margin: 0;
  padding: $spacing-card $spacing-page;
  border: none;
  border-bottom: 1rpx solid $color-border;
  border-radius: 0;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 16rpx;
}

.price {
  @include price-text(44rpx);
  font-weight: 700;
}

.market {
  color: $color-text-disabled;
  text-decoration: line-through;
  font-size: 26rpx;
}

.title {
  display: block;
  font-size: 32rpx;
  font-weight: 600;
  margin-top: 16rpx;
  line-height: 1.45;
  color: $color-text-primary;
}

.subtitle {
  display: block;
  color: $color-text-secondary;
  font-size: 26rpx;
  margin-top: 8rpx;
}

.tags {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-top: 16rpx;
}

.stock {
  font-size: 24rpx;
  color: $color-text-secondary;
}

.sku-row {
  display: flex;
  align-items: center;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid $color-border;
  margin-bottom: 16rpx;
}

.sku-label {
  color: $color-text-secondary;
  font-size: 26rpx;
  margin-right: 16rpx;
}

.sku-value {
  flex: 1;
  font-size: 26rpx;
  color: $color-text-primary;
}

.chev {
  color: #bfbfbf;
  font-size: 32rpx;
}

.panel-title {
  font-size: 28rpx;
  font-weight: 600;
  margin-bottom: 16rpx;
  display: block;
}

.detail-panel {
  padding-bottom: 48rpx;
}

.detail-rich {
  display: block;
  font-size: 28rpx;
  line-height: 1.7;
  color: $color-text-primary;
}

.detail-fallback {
  display: block;
  font-size: 26rpx;
  color: $color-text-secondary;
  line-height: 1.6;
}

.spec-row {
  margin-top: 16rpx;
}

.spec-name {
  font-size: 24rpx;
  color: $color-text-secondary;
}

.spec-values {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-top: 8rpx;
}

.chip {
  padding: 12rpx 28rpx;
  background: $color-bg-section;
  border-radius: $radius-chip;
  font-size: 24rpx;
  border: 1rpx solid $color-border;
}

.chip.on {
  background: $color-primary;
  color: #fff;
  border-color: $color-primary;
}

.chip.disabled {
  color: $color-text-disabled;
  background: $color-bg-section;
}

.detail-fallback {
  font-size: 26rpx;
  color: $color-text-secondary;
  line-height: 1.6;
}

.review-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.review-score {
  font-size: 24rpx;
  color: $color-primary;

  &.muted {
    color: $color-text-secondary;
  }
}

.review-item {
  padding: 16rpx 0;
  border-top: 1rpx solid $color-border;
}

.review-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-user {
  font-size: 26rpx;
  font-weight: 500;
}

.review-stars {
  color: #ffb400;
  font-size: 24rpx;
}

.review-content {
  display: block;
  margin-top: 8rpx;
  font-size: 26rpx;
  line-height: 1.5;
  color: $color-text-primary;
}

.review-time {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $color-text-secondary;
}

.review-more {
  text-align: center;
  padding: 20rpx 0 8rpx;
  font-size: 26rpx;
  color: $color-primary;
}

.product-bar {
  display: flex;
  align-items: center;
  gap: 20rpx;
  width: 100%;
}

.cart-entry {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 88rpx;
  flex-shrink: 0;
  gap: 6rpx;
  background: transparent;

  &:active {
    opacity: 0.6;
  }
}

.cart-label {
  font-size: 20rpx;
  color: $color-text-primary;
  line-height: 1.2;
}

.bar-actions {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.btn {
  flex: 1;
  min-width: 0;
  margin: 0;
  height: 80rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;

  &::after {
    border: none;
  }
}

.btn[disabled] {
  background: #e8e8e8 !important;
  color: #bbb !important;
  border-color: #e8e8e8 !important;
}

.cart-btn {
  @include btn-outline;
  background: #fff;
  color: $color-primary;
  border: 2rpx solid $color-primary;
}

.buy-btn {
  @include btn-primary;
  color: #fff;
  background: $color-primary;
  border: none;
}
</style>
