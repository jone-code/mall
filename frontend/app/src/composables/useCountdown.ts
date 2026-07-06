import { onUnmounted, ref, watch, type Ref } from "vue";
import { formatCountdown, isExpired } from "@/utils/datetime";

export function useCountdown(expireAt: Ref<string | undefined>) {
  const countdown = ref<string | null>(null);
  const expired = ref(false);
  let timer: ReturnType<typeof setInterval> | null = null;

  function tick() {
    if (!expireAt.value) {
      countdown.value = null;
      expired.value = false;
      return;
    }
    if (isExpired(expireAt.value)) {
      countdown.value = null;
      expired.value = true;
      stop();
      return;
    }
    expired.value = false;
    countdown.value = formatCountdown(expireAt.value);
  }

  function start() {
    stop();
    tick();
    timer = setInterval(tick, 1000);
  }

  function stop() {
    if (timer) {
      clearInterval(timer);
      timer = null;
    }
  }

  watch(expireAt, start, { immediate: true });
  onUnmounted(stop);

  return { countdown, expired, restart: start };
}
