import { onMounted, onUnmounted } from 'vue'

/** 按 Esc 关闭最顶层 Element Plus 弹窗/抽屉 */
export function useEscapeClose() {
  function onKeydown(e: KeyboardEvent) {
    if (e.key !== 'Escape') return
    const overlays = document.querySelectorAll('.el-overlay')
    if (!overlays.length) return
    const top = overlays[overlays.length - 1]
    const closeBtn =
      top.querySelector('.el-dialog__headerbtn') ||
      top.querySelector('.el-drawer__close-btn')
    if (closeBtn instanceof HTMLElement) {
      closeBtn.click()
      e.preventDefault()
    }
  }

  onMounted(() => window.addEventListener('keydown', onKeydown))
  onUnmounted(() => window.removeEventListener('keydown', onKeydown))
}
