<template>
  <view class="product-card" @click="$emit('click')">
    <view class="cover-wrap">
      <image class="cover" :src="coverSrc" mode="aspectFill" />
    </view>
    <view class="body">
      <TypeBadge v-if="item.productType" class="badge" :type="item.productType" />
      <text class="name">{{ item.title }}</text>
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
.product-card {
  width: 48%;
  margin-bottom: 48rpx;
  overflow: hidden;

  &:active {
    opacity: 0.85;
  }
}

.cover {
  width: 100%;
  height: 400rpx;
  @include image-cover;
  display: block;
}

.body {
  padding: 20rpx 4rpx 0;
}

.badge {
  margin-bottom: 12rpx;
}

.name {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  font-size: 24rpx;
  color: $color-text-primary;
  line-height: 1.5;
  letter-spacing: 0.5rpx;
  min-height: 72rpx;
}

.sub {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
  color: $color-text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  display: block;
  margin-top: 12rpx;
  @include price-text(28rpx);
}
</style>
