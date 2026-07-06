<template>
  <view class="home-nav">
    <view v-if="categories.length" class="cat-section">
      <text class="nav-label">分类</text>
      <scroll-view scroll-x class="cat-scroll" show-scrollbar="false">
        <view class="cat-list">
          <view
            v-for="c in categories"
            :key="c.id"
            class="cat-item"
            @click="$emit('category', c.id)"
          >
            <view class="cat-icon-wrap">
              <image v-if="c.iconUrl" class="cat-img" :src="c.iconUrl" mode="aspectFill" />
              <GlyphIcon v-else :type="iconType(c.name)" theme="black" :size="40" />
            </view>
            <text class="cat-name">{{ c.name }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="quick-section">
      <view
        v-for="item in quickItems"
        :key="item.key"
        class="quick-item"
        @click="$emit('quick', item.key)"
      >
        <GlyphIcon :type="item.icon" theme="black" :size="36" />
        <text class="quick-label">{{ item.label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import GlyphIcon from '@/components/GlyphIcon.vue'
import type { CategoryNode } from '@/api/product'

defineProps<{ categories: CategoryNode[] }>()
defineEmits<{ category: [id: number]; quick: [key: string] }>()

const quickItems = [
  { key: 'hot', icon: 'hot', label: '热卖' },
  { key: 'category', icon: 'category', label: '分类' },
  { key: 'cart', icon: 'cart', label: '购物袋' },
  { key: 'mine', icon: 'user', label: '我的' },
]

function iconType(name: string) {
  if (/数码|电子|手机|电脑|3c/i.test(name)) return 'digital'
  if (/服|装|衣|鞋|饰/.test(name)) return 'fashion'
  if (/食|品|吃|饮|鲜/.test(name)) return 'food'
  if (/家|居|日用/.test(name)) return 'home'
  if (/服务|虚拟/.test(name)) return 'service'
  return 'default'
}
</script>

<style scoped lang="scss">
.home-nav {
  margin-bottom: 40rpx;
  border-top: 1rpx solid $color-border;
  border-bottom: 1rpx solid $color-border;
}

.cat-section {
  padding: 32rpx 0 24rpx;
}

.nav-label {
  display: block;
  padding: 0 $spacing-page 24rpx;
  font-size: 22rpx;
  letter-spacing: 4rpx;
  color: $color-text-secondary;
  font-weight: 500;
}

.cat-scroll {
  white-space: nowrap;
}

.cat-list {
  display: inline-flex;
  padding: 0 $spacing-page;
  gap: 32rpx;
}

.cat-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  width: 120rpx;
  flex-shrink: 0;

  &:active {
    opacity: 0.6;
  }
}

.cat-icon-wrap {
  width: 96rpx;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
  border: 1rpx solid $color-border;
  background: $color-bg-section;
}

.cat-img {
  width: 56rpx;
  height: 56rpx;
}

.cat-name {
  font-size: 22rpx;
  color: $color-text-primary;
  letter-spacing: 1rpx;
  text-align: center;
  max-width: 120rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.quick-section {
  display: flex;
  border-top: 1rpx solid $color-border;
  padding: 28rpx $spacing-page;
  gap: 0;
}

.quick-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;

  &:active {
    opacity: 0.6;
  }
}

.quick-label {
  font-size: 22rpx;
  color: $color-text-secondary;
  letter-spacing: 1rpx;
}
</style>
