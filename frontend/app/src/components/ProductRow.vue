<template>
  <view class="product-row" @click="$emit('click')">
    <image class="thumb" :src="coverSrc" mode="aspectFill" />
    <view class="info">
      <TypeBadge v-if="item.productType" :type="item.productType" />
      <text class="title">{{ item.title }}</text>
      <text v-if="item.subtitle" class="sub">{{ item.subtitle }}</text>
      <text class="price">{{ formatPrice(item.minPrice, item.maxPrice) }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import TypeBadge from '@/components/TypeBadge.vue'
import { formatPrice, type SpuSummary } from '@/api/product'
import { resolveMediaUrl } from '@/utils/media'
import { computed } from 'vue'

const props = defineProps<{ item: SpuSummary }>()
defineEmits<{ click: [] }>()

const coverSrc = computed(() => resolveMediaUrl(props.item.mainImage))
</script>

<style scoped lang="scss">
.product-row {
  display: flex;
  gap: 24rpx;
  padding: 24rpx 0;
  border-bottom: 1rpx solid $color-border;
  align-items: flex-start;

  &:active {
    opacity: 0.7;
  }
}

.thumb {
  width: 200rpx;
  height: 240rpx;
  @include image-cover;
  flex-shrink: 0;
}

.info {
  flex: 1;
  min-width: 0;
  padding-top: 8rpx;
}

.title {
  display: block;
  margin-top: 12rpx;
  font-size: 26rpx;
  line-height: 1.5;
  color: $color-text-primary;
  letter-spacing: 0.5rpx;
}

.sub {
  display: block;
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $color-text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  display: block;
  margin-top: 20rpx;
  @include price-text(28rpx);
}
</style>
