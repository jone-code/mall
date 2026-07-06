<template>
  <view class="page">
    <view class="layout">
      <scroll-view scroll-y class="left">
        <view
          v-for="c in categories"
          :key="c.id"
          :class="['left-item', activeRootId === c.id ? 'active' : '']"
          @click="selectRoot(c)"
        >
          <view v-if="activeRootId === c.id" class="active-bar" />
          <text class="left-name">{{ c.name }}</text>
        </view>
      </scroll-view>
      <scroll-view scroll-y class="right" @scrolltolower="loadMore">
        <view v-if="activeRootName" class="right-head">
          <text class="right-title">{{ activeRootName }}</text>
          <text class="right-count">{{ total }} 件商品</text>
        </view>

        <view v-if="subCategories.length" class="sub-cats">
          <view
            v-for="s in subCategories"
            :key="s.id"
            :class="['sub-chip', activeCategoryId === s.id ? 'on' : '']"
            @click="selectSub(s.id)"
          >
            {{ s.name }}
          </view>
        </view>

        <view v-if="loading && !products.length" class="sk-wrap">
          <ProductRowSkeleton :count="6" />
        </view>
        <EmptyState v-else-if="!products.length" text="暂无商品" desc="换个分类看看吧" icon="🛍️" />
        <ProductRow
          v-for="item in products"
          :key="item.id"
          :item="item"
          @click="goDetail(item.id)"
        />
        <view v-if="loadingMore" class="hint">加载更多…</view>
        <view v-else-if="products.length && products.length >= total" class="hint end">— 已经到底了 —</view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onLoad, onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { computed, ref } from 'vue'
import EmptyState from '@/components/EmptyState.vue'
import ProductRow from '@/components/ProductRow.vue'
import ProductRowSkeleton from '@/components/ProductRowSkeleton.vue'
import {
  getCategories,
  getProducts,
  type CategoryNode,
  type SpuSummary
} from '@/api/product'
import { showError } from '@/utils/error-message'

const categories = ref<CategoryNode[]>([])
const activeRootId = ref<number | null>(null)
const activeCategoryId = ref<number | null>(null)
const products = ref<SpuSummary[]>([])
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)

const subCategories = computed(() => {
  const root = categories.value.find((c) => c.id === activeRootId.value)
  return root?.children || []
})

const activeRootName = computed(() =>
  categories.value.find((c) => c.id === activeRootId.value)?.name || ''
)

async function loadCategories(initialId?: number) {
  const res = await getCategories()
  categories.value = res.data || []
  if (!categories.value.length) return
  if (initialId) {
    for (const c of categories.value) {
      if (c.id === initialId) {
        activeRootId.value = c.id
        activeCategoryId.value = c.children?.[0]?.id ?? c.id
        return
      }
      const child = c.children?.find((ch) => ch.id === initialId)
      if (child) {
        activeRootId.value = c.id
        activeCategoryId.value = child.id
        return
      }
    }
  }
  activeRootId.value = categories.value[0].id
  activeCategoryId.value =
    categories.value[0].children?.[0]?.id ?? categories.value[0].id
}

async function fetchList(reset = false) {
  if (!activeCategoryId.value) return
  if (reset) {
    page.value = 1
    products.value = []
  }
  const isFirst = page.value === 1
  if (isFirst) loading.value = true
  else loadingMore.value = true
  try {
    const res = await getProducts({
      categoryId: activeCategoryId.value,
      page: page.value,
      size: 20
    })
    const data = res.data
    total.value = data?.total || 0
    const list = data?.list || []
    products.value = reset ? list : products.value.concat(list)
  } catch (e) {
    showError(e, '加载失败')
  } finally {
    loading.value = false
    loadingMore.value = false
    uni.stopPullDownRefresh()
  }
}

function selectRoot(c: CategoryNode) {
  activeRootId.value = c.id
  activeCategoryId.value = c.children?.[0]?.id ?? c.id
  fetchList(true)
}

function selectSub(id: number) {
  activeCategoryId.value = id
  fetchList(true)
}

function loadMore() {
  if (loadingMore.value || products.value.length >= total.value) return
  page.value += 1
  fetchList(false)
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

onLoad(async (query) => {
  const cid = query?.categoryId ? Number(query.categoryId) : undefined
  await initCategories(cid)
})

onShow(async () => {
  const pending = uni.getStorageSync('pendingCategoryId')
  if (pending) {
    uni.removeStorageSync('pendingCategoryId')
    await initCategories(Number(pending))
  }
})

async function initCategories(initialId?: number) {
  try {
    await loadCategories(initialId)
    await fetchList(true)
  } catch (e) {
    showError(e, '加载失败')
  }
}

onPullDownRefresh(() => {
  fetchList(true)
})
</script>

<style lang="scss">
.page {
  height: 100vh;
  @include page-shell;
}

.layout {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.left {
  width: 200rpx;
  min-width: 200rpx;
  max-width: 200rpx;
  flex: 0 0 200rpx;
  flex-shrink: 0;
  box-sizing: border-box;
  background: $color-bg-section;
  height: 100%;
  border-right: 1rpx solid $color-border;
}

.left-item {
  position: relative;
  box-sizing: border-box;
  width: 200rpx;
  padding: 40rpx 12rpx;
  font-size: 24rpx;
  color: $color-text-secondary;
  text-align: center;
  letter-spacing: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.left-name {
  display: block;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.left-item.active {
  background: $color-bg-page;
  color: $color-text-primary;
  font-weight: 600;
}

.active-bar {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4rpx;
  height: 32rpx;
  background: $color-primary;
}

.right {
  flex: 1;
  min-width: 0;
  width: 0;
  height: 100%;
  padding: 20rpx 16rpx;
  background: $color-bg-page;
  box-sizing: border-box;
}

.right-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16rpx;
  padding: 0 8rpx;
}

.right-title {
  font-size: 32rpx;
  font-weight: 700;
  color: $color-text-primary;
}

.right-count {
  font-size: 22rpx;
  color: $color-text-secondary;
}

.sub-cats {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
  margin-bottom: 20rpx;
}

.sub-chip {
  @include chip-tag;
}

.sub-chip.on {
  background: $color-primary-light;
  color: $color-primary;
  border-color: rgba(255, 77, 79, 0.3);
}

.hint {
  text-align: center;
  color: $color-text-secondary;
  padding: 32rpx;
  font-size: 26rpx;

  &.end {
    color: $color-text-disabled;
    font-size: 24rpx;
  }
}
</style>
