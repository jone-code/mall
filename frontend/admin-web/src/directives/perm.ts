import type { Directive, DirectiveBinding } from 'vue'
import { useAdminStore } from '@/stores/admin'
import { permissionTip } from '@/utils/permission-label'

function apply(el: HTMLElement, binding: DirectiveBinding) {
  const store = useAdminStore()
  const required = binding.value
  if (!required) return

  const list = Array.isArray(required) ? required : [required as string]
  const ok = list.some((c) => store.permissions.includes(c))
  const disableMode = binding.modifiers.disable

  if (ok) {
    if (el.getAttribute('data-perm-hidden') === 'true') {
      el.style.display = ''
      el.removeAttribute('data-perm-hidden')
    }
    if (el.getAttribute('data-perm-disabled') === 'true') {
      el.removeAttribute('data-perm-disabled')
      el.removeAttribute('title')
      if (el instanceof HTMLButtonElement) {
        el.disabled = false
      }
      el.classList.remove('is-disabled')
      el.style.pointerEvents = ''
      el.style.opacity = ''
    }
    return
  }

  if (disableMode) {
    el.setAttribute('data-perm-disabled', 'true')
    el.setAttribute('title', permissionTip(list))
    if (el instanceof HTMLButtonElement) {
      el.disabled = true
    }
    el.classList.add('is-disabled')
    el.style.pointerEvents = 'none'
    el.style.opacity = '0.55'
  } else {
    el.style.display = 'none'
    el.setAttribute('data-perm-hidden', 'true')
  }
}

const permDirective: Directive = {
  mounted(el, binding) {
    apply(el as HTMLElement, binding)
  },
  updated(el, binding) {
    apply(el as HTMLElement, binding)
  }
}

export default permDirective
