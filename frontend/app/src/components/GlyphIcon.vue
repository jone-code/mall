<template>
  <view class="glyph" :class="[`glyph-${type}`, `theme-${theme}`]" :style="sizeStyle">
    <view v-if="type === 'digital'" class="shape phone">
      <view class="phone-screen" />
    </view>
    <view v-else-if="type === 'fashion'" class="shape fashion">
      <view class="hanger-hook" />
      <view class="hanger-bar" />
    </view>
    <view v-else-if="type === 'food'" class="shape food">
      <view class="bowl" />
      <view class="steam s1" />
      <view class="steam s2" />
    </view>
    <view v-else-if="type === 'home'" class="shape home">
      <view class="roof" />
      <view class="house-body" />
    </view>
    <view v-else-if="type === 'service'" class="shape service">
      <view class="gear" />
    </view>
    <view v-else-if="type === 'hot'" class="shape hot">
      <view class="flame f1" />
      <view class="flame f2" />
    </view>
    <view v-else-if="type === 'category'" class="shape category">
      <view class="grid-dot" v-for="n in 4" :key="n" />
    </view>
    <view v-else-if="type === 'cart'" class="shape cart">
      <view class="cart-body" />
      <view class="cart-wheel w1" />
      <view class="cart-wheel w2" />
    </view>
    <view v-else-if="type === 'user'" class="shape user">
      <view class="user-head" />
      <view class="user-body" />
    </view>
    <view v-else class="shape default">
      <view class="default-bar b1" />
      <view class="default-bar b2" />
      <view class="default-bar b3" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    type?: string
    theme?: string
    size?: number
  }>(),
  {
    type: 'default',
    theme: 'red',
    size: 48,
  }
)

const sizeStyle = computed(() => ({
  width: `${props.size}rpx`,
  height: `${props.size}rpx`,
}))
</script>

<style scoped lang="scss">
.glyph {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.shape {
  position: relative;
  width: 100%;
  height: 100%;
}

/* ---- digital ---- */
.phone {
  width: 44%;
  height: 68%;
  margin: 0 auto;
  border: 4rpx solid currentColor;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.phone-screen {
  width: 70%;
  height: 78%;
  background: currentColor;
  border-radius: 4rpx;
  opacity: 0.85;
}

/* ---- fashion ---- */
.fashion {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 8%;
}

.hanger-hook {
  width: 16rpx;
  height: 16rpx;
  border: 4rpx solid currentColor;
  border-bottom: none;
  border-radius: 12rpx 12rpx 0 0;
}

.hanger-bar {
  width: 70%;
  height: 4rpx;
  background: currentColor;
  margin-top: 2rpx;
  position: relative;

  &::before,
  &::after {
    content: '';
    position: absolute;
    top: 0;
    width: 50%;
    height: 4rpx;
    background: currentColor;
  }

  &::before {
    left: 0;
    transform: rotate(28deg);
    transform-origin: left center;
  }

  &::after {
    right: 0;
    transform: rotate(-28deg);
    transform-origin: right center;
  }
}

/* ---- food ---- */
.food {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  padding-bottom: 8%;
}

.bowl {
  width: 60%;
  height: 36%;
  border: 4rpx solid currentColor;
  border-top: none;
  border-radius: 0 0 50% 50%;
}

.steam {
  position: absolute;
  width: 4rpx;
  height: 20%;
  background: currentColor;
  border-radius: 4rpx;
  opacity: 0.7;
  top: 18%;

  &.s1 { left: 38%; transform: rotate(-8deg); }
  &.s2 { right: 38%; transform: rotate(8deg); }
}

/* ---- home ---- */
.home {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  padding-bottom: 10%;
}

.roof {
  width: 0;
  height: 0;
  border-left: 36rpx solid transparent;
  border-right: 36rpx solid transparent;
  border-bottom: 28rpx solid currentColor;
}

.house-body {
  width: 52%;
  height: 32%;
  background: currentColor;
  border-radius: 2rpx 2rpx 4rpx 4rpx;
}

/* ---- service ---- */
.service {
  display: flex;
  align-items: center;
  justify-content: center;
}

.gear {
  width: 52%;
  height: 52%;
  border: 6rpx solid currentColor;
  border-radius: 50%;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    inset: 22%;
    border: 4rpx solid currentColor;
    border-radius: 50%;
  }
}

/* ---- hot ---- */
.hot {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  gap: 4rpx;
  padding-bottom: 12%;
}

.flame {
  background: currentColor;
  border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;

  &.f1 {
    width: 36%;
    height: 52%;
    opacity: 0.75;
  }

  &.f2 {
    width: 28%;
    height: 68%;
  }
}

/* ---- category grid ---- */
.category {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14%;
  width: 56%;
  height: 56%;
  margin: auto;
}

.grid-dot {
  background: currentColor;
  border-radius: 6rpx;
}

/* ---- cart ---- */
.cart {
  padding-bottom: 8%;
}

.cart-body {
  width: 62%;
  height: 40%;
  border: 4rpx solid currentColor;
  border-radius: 4rpx 4rpx 8rpx 8rpx;
  margin: 0 auto;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: -28%;
    left: -8%;
    width: 50%;
    height: 28%;
    border: 4rpx solid currentColor;
    border-right: none;
    border-bottom: none;
    border-radius: 8rpx 0 0 0;
    transform: rotate(-12deg);
  }
}

.cart-wheel {
  position: absolute;
  bottom: 6%;
  width: 12rpx;
  height: 12rpx;
  background: currentColor;
  border-radius: 50%;

  &.w1 { left: 28%; }
  &.w2 { right: 28%; }
}

/* ---- user ---- */
.user {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6%;
  padding-top: 6%;
}

.user-head {
  width: 34%;
  height: 34%;
  background: currentColor;
  border-radius: 50%;
}

.user-body {
  width: 56%;
  height: 30%;
  background: currentColor;
  border-radius: 50% 50% 20% 20%;
}

/* ---- default ---- */
.default {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 14%;
  width: 56%;
  margin: auto;
}

.default-bar {
  height: 6rpx;
  background: currentColor;
  border-radius: 4rpx;

  &.b1 { width: 100%; }
  &.b2 { width: 72%; }
  &.b3 { width: 86%; }
}

/* ---- themes ---- */
.theme-blue { color: #1677ff; }
.theme-orange { color: #fa8c16; }
.theme-green { color: #52c41a; }
.theme-purple { color: #722ed1; }
.theme-red { color: #ff4d4f; }
.theme-black { color: #1a1a1a; }
.theme-muted { color: #8e8e8e; }
.theme-cyan { color: #13c2c2; }
</style>
