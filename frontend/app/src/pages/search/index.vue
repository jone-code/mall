<template>
  <view class="page">
    <view class="search-bar">
      <view class="search-inner">
        <text class="search-icon">⌕</text>
        <input
          v-model="keyword"
          class="search-input"
          type="text"
          placeholder="搜索商品名称"
          confirm-type="search"
          :focus="true"
          @confirm="onSearch"
        />
        <text v-if="keyword" class="clear" @click="clearKeyword">✕</text>
      </view>
      <text class="search-btn" @click="onSearch">搜索</text>
    </view>

    <view v-if="!searched" class="hot-wrap">
      <SectionHeader title="热门搜索" />
      <view class="hot-tags">
        <text
          v-for="tag in hotTags"
          :key="tag"
          class="hot-tag"
          @click="searchTag(tag)"
        >
          {{ tag }}
        </text>
      </view>
      <view v-if="history.length" class="history">
        <view class="history-head">
          <text class="history-title">搜索历史</text>
          <text class="history-clear" @click="clearHistory">清空</text>
        </view>
        <view class="hot-tags">
          <text
            v-for="h in history"
            :key="h"
            class="hot-tag muted"
            @click="searchTag(h)"
          >
            {{ h }}
          </text>
        </view>
      </view>
    </view>

    <scroll-view scroll-y class="result" @scrolltolower="loadMore">
      <ProductSkeleton v-if="loading && !products.length" :count="6" />

      <template v-else-if="searched">
        <view v-if="!products.length" class="empty-wrap">
          <EmptyState text="没有找到相关商品" desc="换个关键词试试" icon="🔍" />
        </view>

        <view v-else>
          <view class="result-head">
            <text>找到 {{ total }} 件相关商品</text>
          </view>
          <view class="grid">
            <ProductCard
              v-for="item in products"
              :key="item.id"
              :item="item"
              @click="goDetail(item.id)"
            />
          </view>
        </view>

        <view v-if="loadingMore" class="hint">加载更多…</view>
        <view v-else-if="products.length && products.length >= total" class="hint">没有更多了</view>
      </template>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import EmptyState from '@/components/EmptyState.vue'
import ProductCard from '@/components/ProductCard.vue'
import ProductSkeleton from '@/components/ProductSkeleton.vue'
import SectionHeader from '@/components/SectionHeader.vue'
import { searchProducts } from '@/api/search'
import { type SpuSummary } from '@/api/product'
import { showError } from '@/utils/error-message'

const HISTORY_KEY = 'search_history'
const hotTags = ['衬衫', '大衣', '跑鞋', '耳机', '香薰', '会员']

const keyword = ref('')
const products = ref<SpuSummary[]>([])
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const searched = ref(false)
const history = ref<string[]>([])

function loadHistory() {
  try {
    history.value = JSON.parse(uni.getStorageSync(HISTORY_KEY) || '[]')
  } catch {
    history.value = []
  }
}

function saveHistory(kw: string) {
  const list = [kw, ...history.value.filter((h) => h !== kw)].slice(0, 8)
  history.value = list
  uni.setStorageSync(HISTORY_KEY, JSON.stringify(list))
}

function clearHistory() {
  history.value = []
  uni.removeStorageSync(HISTORY_KEY)
}

async function fetchList(reset = false) {
  const kw = keyword.value.trim()
  if (!kw) return
  if (reset) {
    page.value = 1
    products.value = []
  }
  const isFirst = page.value === 1
  if (isFirst) loading.value = true
  else loadingMore.value = true
  try {
    const res = await searchProducts({ keyword: kw, page: page.value, size: 20 })
    const data = res.data
    total.value = data?.total || 0
    const list = data?.list || []
    products.value = reset ? list : products.value.concat(list)
    searched.value = true
    saveHistory(kw)
  } catch (e) {
    showError(e, '搜索失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function onSearch() {
  if (!keyword.value.trim()) {
    searched.value = false
    products.value = []
    return
  }
  fetchList(true)
}

function searchTag(tag: string) {
  keyword.value = tag
  fetchList(true)
}

function clearKeyword() {
  keyword.value = ''
  searched.value = false
  products.value = []
}

function loadMore() {
  if (loadingMore.value || loading.value) return
  if (products.value.length >= total.value) return
  page.value += 1
  fetchList(false)
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/product/detail?id=${id}` })
}

onLoad((query) => {
  loadHistory()
  if (query?.keyword) {
    keyword.value = String(query.keyword)
    fetchList(true)
  }
})
</script>

<style lang="scss">
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  @include page-shell;
}

.search-bar {
  display: flex;
  align-items: center;
  background: #fff;
  padding: 16rpx 24rpx;
  gap: 16rpx;
  box-shadow: 0 2rpx 12rpx $color-shadow;
}

.search-inner {
  flex: 1;
  display: flex;
  align-items: center;
  background: $color-bg-page;
  border-radius: $radius-pill;
  padding: 0 24rpx;
  gap: 12rpx;
}

.search-icon {
  color: $color-primary;
  font-size: 30rpx;
}

.search-input {
  flex: 1;
  height: 72rpx;
  font-size: 28rpx;
}

.clear {
  color: #bbb;
  font-size: 28rpx;
  padding: 8rpx;
}

.search-btn {
  color: $color-primary;
  font-size: 30rpx;
  font-weight: 600;
}

.hot-wrap {
  padding-top: 8rpx;
}

.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  padding: 0 $spacing-page 24rpx;
}

.hot-tag {
  @include chip-tag;
  padding: 12rpx 28rpx;

  &.muted {
    background: #fafafa;
  }
}

.history {
  margin-top: 8rpx;
}

.history-head {
  display: flex;
  justify-content: space-between;
  padding: 0 $spacing-page 16rpx;
}

.history-title {
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text-primary;
}

.history-clear {
  font-size: 24rpx;
  color: $color-text-secondary;
}

.result {
  flex: 1;
  padding: $spacing-page;
}

.result-head {
  font-size: 24rpx;
  color: $color-text-secondary;
  margin-bottom: 16rpx;
}

.empty-wrap {
  padding-top: 80rpx;
}

.grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.hint {
  color: $color-text-secondary;
  text-align: center;
  padding: 32rpx;
  font-size: 26rpx;
}
</style>
