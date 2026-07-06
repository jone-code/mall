import { defineStore } from "pinia";
import { getCart } from "@/api/cart";

export const useCartStore = defineStore("cart", {
  state: () => ({
    totalQuantity: 0,
  }),
  actions: {
    async refreshBadge() {
      try {
        const res = await getCart();
        this.totalQuantity = res.data?.summary?.totalQuantity ?? 0;
      } catch {
        this.totalQuantity = 0;
      }
      this.syncTabBadge();
    },
    syncTabBadge() {
      const tabIndex = 2; // pages.json 购物车 Tab
      if (this.totalQuantity > 0) {
        uni.setTabBarBadge({
          index: tabIndex,
          text: this.totalQuantity > 99 ? "99+" : String(this.totalQuantity),
        });
      } else {
        uni.removeTabBarBadge({ index: tabIndex });
      }
    },
    clear() {
      this.totalQuantity = 0;
      this.syncTabBadge();
    },
  },
});
