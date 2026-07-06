<template>
  <view class="page">
    <scroll-view scroll-y class="scroll">
      <rich-text class="content" :nodes="nodes" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";

const type = ref<"user" | "privacy">("user");

const USER_HTML = `
<h2>ComonOn 用户协议</h2>
<p>欢迎使用 ComonOn 商城。使用本服务即表示您同意以下条款：</p>
<ol>
<li>您应提供真实、准确的注册信息，并妥善保管账号。</li>
<li>禁止利用本平台从事违法违规活动。</li>
<li>订单提交后，请按页面提示完成支付；超时未支付订单将自动关闭。</li>
<li>虚拟商品、服务类商品的履约方式以商品详情及订单说明为准。</li>
<li>我们有权根据业务需要更新本协议，更新后将通过应用内公告等方式通知。</li>
</ol>
<p>如有疑问，请联系客服。</p>
`;

const PRIVACY_HTML = `
<h2>ComonOn 隐私政策</h2>
<p>我们重视您的个人信息保护：</p>
<ol>
<li><b>收集范围</b>：手机号、昵称、头像、收货地址、设备标识等，用于账号、交易与安全保障。</li>
<li><b>使用目的</b>：完成登录注册、订单履约、客服联系及风险控制。</li>
<li><b>存储与安全</b>：数据加密传输，访问权限最小化，定期审计。</li>
<li><b>第三方共享</b>：除法律法规要求或履约必要（如物流、支付）外，不向第三方出售您的个人信息。</li>
<li><b>您的权利</b>：可查询、更正资料，管理登录设备，或申请注销账号。</li>
</ol>
<p>继续使用即表示您已阅读并理解本政策。</p>
`;

const nodes = computed(() => (type.value === "user" ? USER_HTML : PRIVACY_HTML));

onLoad((q: any) => {
  const t = q?.type;
  if (t === "privacy") type.value = "privacy";
  uni.setNavigationBarTitle({
    title: type.value === "user" ? "用户协议" : "隐私政策",
  });
});
</script>

<style scoped lang="scss">
.page {
  @include page-shell;
  height: 100vh;
}

.scroll {
  height: 100%;
  padding: 24rpx $spacing-page 48rpx;
  box-sizing: border-box;
}

.content {
  font-size: 28rpx;
  line-height: 1.7;
  color: $color-text-primary;
}
</style>
