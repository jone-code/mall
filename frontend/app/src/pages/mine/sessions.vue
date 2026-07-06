<template>
  <view class="page">
    <ListSkeleton v-if="loading && !sessions.length" />

    <template v-else>
      <view v-for="s in sessions" :key="s.sid" class="card">
        <view class="row">
          <text class="device">{{ deviceLabel(s) }}</text>
          <text v-if="s.current" class="tag">当前设备</text>
        </view>
        <text class="meta">最近活跃：<RelativeTime :value="s.lastActiveAt" /></text>
        <text class="meta">登录时间：<RelativeTime :value="s.createdAt" mode="absolute" /></text>
        <button
          v-if="!s.current"
          class="kick"
          :loading="kicking === s.sid"
          @click="onKick(s.sid)"
        >
          退出此设备
        </button>
      </view>

      <view v-if="!sessions.length" class="center">
        <EmptyState text="暂无登录记录" icon="📱" />
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { onShow } from "@dcloudio/uni-app";
import { ref } from "vue";
import { killSession, listSessions, type SessionItem } from "@/api/auth";
import EmptyState from "@/components/EmptyState.vue";
import ListSkeleton from "@/components/ListSkeleton.vue";
import RelativeTime from "@/components/RelativeTime.vue";
import { showError } from "@/utils/error-message";

const loading = ref(false);
const kicking = ref("");
const sessions = ref<SessionItem[]>([]);

function deviceLabel(s: SessionItem) {
  const type = s.deviceType || "UNKNOWN";
  const map: Record<string, string> = {
    H5: "浏览器",
    MP_WEIXIN: "微信小程序",
    APP: "手机 App",
    IOS: "iOS",
    ANDROID: "Android",
  };
  return map[type] || type;
}

async function load() {
  loading.value = true;
  try {
    const res = await listSessions();
    sessions.value = (res.data || []).sort(
      (a, b) => Number(b.current) - Number(a.current)
    );
  } catch (e) {
    showError(e, "加载失败");
  } finally {
    loading.value = false;
  }
}

async function onKick(sid: string) {
  uni.showModal({
    title: "退出设备",
    content: "确认让该设备退出登录？",
    success: async (r) => {
      if (!r.confirm) return;
      kicking.value = sid;
      try {
        await killSession(sid);
        uni.showToast({ title: "已退出", icon: "success" });
        load();
      } catch (e) {
        showError(e, "操作失败");
      } finally {
        kicking.value = "";
      }
    },
  });
}

onShow(load);
</script>

<style scoped lang="scss">
.page {
  @include page-shell;
  padding: 16rpx;
  min-height: 100vh;
}

.hint {
  text-align: center;
  padding: 120rpx;
  color: $color-text-secondary;
}

.center {
  padding-top: 80rpx;
}

.card {
  @include card;
  padding: 24rpx;
  margin-bottom: 16rpx;
}

.row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 12rpx;
}

.device {
  font-size: 30rpx;
  font-weight: 600;
}

.tag {
  font-size: 22rpx;
  color: $color-primary;
  background: $color-primary-light;
  padding: 4rpx 12rpx;
  border-radius: $radius-pill;
}

.meta {
  display: block;
  font-size: 24rpx;
  color: $color-text-secondary;
  margin-top: 6rpx;
}

.kick {
  margin-top: 20rpx;
  @include btn-outline;
  font-size: 26rpx;
  height: 64rpx;
  line-height: 64rpx;
  background: #fff;
}
</style>
