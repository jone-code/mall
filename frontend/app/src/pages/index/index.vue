<template>
  <view class="page">
    <view class="header">
      <view class="status-bar" :style="{ height: statusBarHeight + 'px' }" />
      <view class="top-bar">
        <view class="brand-wrap">
          <text class="brand">COMONON</text>
          <text class="slogan">精选好物 · 品质生活</text>
        </view>
        <view class="search-entry" @click="goSearch">
          <text class="search-text">搜索商品</text>
          <text class="search-icon">⌕</text>
        </view>
      </view>
    </view>

    <view v-if="promoItem" class="promo" @click="goDetail(promoItem.id)">
      <image class="promo-img" :src="promoItem.mainImage" mode="aspectFill" />
      <view class="promo-mask">
        <text class="promo-tag">精选</text>
        <text class="promo-title">{{ promoItem.title }}</text>
        <text class="promo-price">{{ formatPrice(promoItem.minPrice, promoItem.maxPrice) }}</text>
      </view>
    </view>

    <HomeNavZone
      v-if="categories.length"
      :categories="categories"
      @category="goCategoryWithId"
      @quick="onQuickEntry"
    />

    <SectionHeader title="新品推荐" subtitle="New Arrivals" action="查看全部" @action="goCategory" />

    <ProductSkeleton v-if="loading" :count="4" />
    <EmptyState v-else-if="!recommend.length" text="暂无商品" desc="稍后再来看看吧" icon="🛍️" />
    <view v-else class="grid">
      <ProductCard
        v-for="item in recommend"
        :key="item.id"
        :item="item"
        @click="goDetail(item.id)"
      />
    </view>

    <view class="footer-space" />
  </view>
</template>

<script setup lang="ts">
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import EmptyState from '@/components/EmptyState.vue'
import ProductCard from '@/components/ProductCard.vue'
import ProductSkeleton from '@/components/ProductSkeleton.vue'
import HomeNavZone from '@/components/HomeNavZone.vue'
import SectionHeader from '@/components/SectionHeader.vue'
import { formatPrice, getHome, type CategoryNode, type SpuSummary } from '@/api/product'

const loading = ref(false)
const categories = ref<CategoryNode[]>([])
const recommend = ref<SpuSummary[]>([])
const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 44

const promoItem = computed(() => recommend.value[0] || null)

async function load() {
  loading.value = true
  try {
    const res = await getHome()
    categories.value = res.data?.categories || []
    recommend.value = res.data?.recommendSpus || []
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goSearch() {
  uni.navigateTo({ url: '/pages/search/index' })
}

function goCategory() {
  uni.switchTab({ url: '/pages/category/index' })
}

function goCart() {
  uni.switchTab({ url: '/pages/cart/index' })
}

function goMine() {
  uni.switchTab({ url: '/pages/mine/index' })
}

function goCategoryWithId(id: number) {
  uni.setStorageSync('pendingCategoryId', String(id))
  uni.switchTab({ url: '/pages/category/index' })
}

function onQuickEntry(key: string) {
  if (key === 'hot') goSearch()
  else if (key === 'category') goCategory()
  else if (key === 'cart') goCart()
  else if (key === 'mine') goMine()
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

onShow(load)

onPullDownRefresh(async () => {
  await load()
  uni.stopPullDownRefresh()
})
</script>

<style lang="scss">
.page {
  @include page-shell;
  @include page-header-bg;
}

.header {
  padding-bottom: 8rpx;
}

.top-bar {
  padding: 16rpx $spacing-page 28rpx;
}

.brand-wrap {
  margin-bottom: 24rpx;
}

.brand {
  font-size: 36rpx;
  font-weight: 500;
  color: $color-text-primary;
  letter-spacing: 8rpx;
}

.slogan {
  display: block;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: $color-text-secondary;
  letter-spacing: 2rpx;
}

.search-entry {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1rpx solid $color-border;
  padding: 20rpx 0;
}

.search-icon {
  color: $color-text-primary;
  font-size: 32rpx;
}

.search-text {
  color: $color-text-disabled;
  font-size: 26rpx;
  letter-spacing: 1rpx;
}

.promo {
  @include promo-banner;
  position: relative;
  height: 520rpx;
}

.promo-img {
  width: 100%;
  height: 100%;
  @include image-cover;
}

.promo-mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 80rpx $spacing-page 40rpx;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.45));
}

.promo-tag {
  display: inline-block;
  font-size: 20rpx;
  color: #fff;
  letter-spacing: 4rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.6);
  padding: 6rpx 16rpx;
  margin-bottom: 16rpx;
}

.promo-title {
  display: block;
  color: #fff;
  font-size: 32rpx;
  font-weight: 400;
  letter-spacing: 1rpx;
  line-height: 1.5;
}

.promo-price {
  display: block;
  margin-top: 12rpx;
  color: #fff;
  font-size: 28rpx;
  font-weight: 500;
  letter-spacing: 2rpx;
}

.grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  padding: 0 $spacing-page;
}

.footer-space {
  height: 40rpx;
}
</style>
