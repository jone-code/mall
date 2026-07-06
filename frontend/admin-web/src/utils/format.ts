export function formatMoney(v?: number | string | null): string {
  const n = Number(v ?? 0)
  if (Number.isNaN(n)) return '0.00'
  return n.toFixed(2)
}

export function formatDateTime(iso?: string | null): string {
  if (!iso) return '—'
  return iso.length > 19 ? iso.slice(0, 19) : iso
}

export function relativeTime(iso?: string | null): string {
  if (!iso) return '—'
  const normalized = iso.includes('T') ? iso : iso.replace(' ', 'T')
  const d = new Date(normalized)
  if (Number.isNaN(d.getTime())) return iso
  const diff = Date.now() - d.getTime()
  if (diff < 0) return formatDateTime(iso)
  const sec = Math.floor(diff / 1000)
  if (sec < 60) return '刚刚'
  const min = Math.floor(sec / 60)
  if (min < 60) return `${min} 分钟前`
  const hr = Math.floor(min / 60)
  if (hr < 24) return `${hr} 小时前`
  const day = Math.floor(hr / 24)
  if (day < 7) return `${day} 天前`
  return formatDateTime(iso)
}

export async function copyToClipboard(text: string): Promise<boolean> {
  const value = text?.trim()
  if (!value) return false
  try {
    await navigator.clipboard.writeText(value)
    return true
  } catch {
    const ta = document.createElement('textarea')
    ta.value = value
    ta.style.position = 'fixed'
    ta.style.left = '-9999px'
    document.body.appendChild(ta)
    ta.select()
    const ok = document.execCommand('copy')
    document.body.removeChild(ta)
    return ok
  }
}
