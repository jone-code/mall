<template>
  <view class="page">
    <view class="card">
      <view class="card-title">订单评价</view>
      <text class="order-no">订单号 {{ orderNo }}</text>

      <view class="stars">
        <text
          v-for="n in 5"
          :key="n"
          :class="['star', n <= rating ? 'on' : '']"
          @click="rating = n"
        >
          ★
        </text>
        <text class="rating-text">{{ rating }} 分</text>
      </view>

      <textarea
        v-model="content"
        class="textarea"
        placeholder="说说商品怎么样吧（必填）"
        maxlength="1000"
      />
      <text class="count">{{ content.length }}/1000</text>
    </view>

    <FixedActionBar>
      <button class="act" :disabled="submitting" @click="onSubmit">提交评价</button>
    </FixedActionBar>
  </view>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
import { createReview } from "@/api/review";
import FixedActionBar from "@/components/FixedActionBar.vue";
import { showError } from "@/utils/error-message";

const orderNo = ref("");
const rating = ref(5);
const content = ref("");
const submitting = ref(false);

onLoad((q: any) => {
  orderNo.value = q?.orderNo || "";
});

async function onSubmit() {
  const text = content.value.trim();
  if (!text) {
    uni.showToast({ title: "请填写评价内容", icon: "none" });
    return;
  }
  if (!orderNo.value) return;
  submitting.value = true;
  try {
    await createReview({
      orderNo: orderNo.value,
      rating: rating.value,
      content: text,
    });
    uni.showToast({ title: "评价成功", icon: "success" });
    setTimeout(() => uni.navigateBack(), 600);
  } catch (e) {
    showError(e, "提交失败");
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped lang="scss">
.page {
  @include page-shell;
  padding-bottom: 140rpx;
}

.card {
  @include card;
  margin: 16rpx;
  padding: 24rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  margin-bottom: 12rpx;
}

.order-no {
  display: block;
  font-size: 24rpx;
  color: $color-text-secondary;
  margin-bottom: 24rpx;
}

.stars {
  display: flex;
  align-items: center;
  gap: 8rpx;
  margin-bottom: 24rpx;
}

.star {
  font-size: 48rpx;
  color: #ddd;

  &.on {
    color: #ffb400;
  }
}

.rating-text {
  margin-left: 12rpx;
  font-size: 26rpx;
  color: $color-text-secondary;
}

.textarea {
  width: 100%;
  min-height: 240rpx;
  background: $color-bg-page;
  border-radius: 12rpx;
  padding: 20rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.count {
  display: block;
  text-align: right;
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}

.act {
  @include btn-primary;
  border-radius: 40rpx;
  font-size: 28rpx;
  margin: 0;
  height: 80rpx;
  line-height: 80rpx;
}
</style>
