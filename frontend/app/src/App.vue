<script setup lang="ts">
import { onLaunch, onShow } from "@dcloudio/uni-app";
import { useUserStore } from "@/stores/user";
import { useCartStore } from "@/stores/cart";

onLaunch(() => {
  console.log("App Launch");
});

onShow(() => {
  const userStore = useUserStore();
  if (userStore.isLogin) {
    useCartStore().refreshBadge();
  }
});
</script>
<style></style>
