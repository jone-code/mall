/** 兼容 ISO 字符串与 Jackson 数组形式的 LocalDateTime */
export function formatDateTime(value: unknown): string {
  if (value == null || value === "") return "—";

  if (Array.isArray(value)) {
    const [y, m, d, h = 0, min = 0, s = 0] = value as number[];
    if (!y || !m || !d) return "—";
    return `${y}-${pad(m)}-${pad(d)} ${pad(h)}:${pad(min)}:${pad(s)}`;
  }

  const text = String(value).trim();
  if (!text) return "—";

  const normalized = text.replace("T", " ").replace(/\.\d+$/, "");
  if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}(:\d{2})?$/.test(normalized)) {
    return normalized.length === 16 ? `${normalized}:00` : normalized;
  }

  const ts = Date.parse(text);
  if (Number.isNaN(ts)) return text;

  const dt = new Date(ts);
  return `${dt.getFullYear()}-${pad(dt.getMonth() + 1)}-${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}:${pad(dt.getSeconds())}`;
}

function pad(n: number) {
  return n < 10 ? `0${n}` : String(n);
}

/** 解析后端时间值为毫秒时间戳 */
export function parseDateMs(value: unknown): number | null {
  if (value == null || value === "") return null;

  if (Array.isArray(value)) {
    const [y, m, d, h = 0, min = 0, s = 0] = value as number[];
    if (!y || !m || !d) return null;
    return new Date(y, m - 1, d, h, min, s).getTime();
  }

  const text = String(value).trim();
  if (!text) return null;

  const normalized = text.includes("T") ? text : text.replace(" ", "T");
  const ts = Date.parse(normalized);
  return Number.isNaN(ts) ? null : ts;
}

export function relativeTime(value: unknown): string {
  const ts = parseDateMs(value);
  if (ts == null) return "—";
  const diff = Date.now() - ts;
  if (diff < 0) return formatDateTime(value);
  const sec = Math.floor(diff / 1000);
  if (sec < 60) return "刚刚";
  const min = Math.floor(sec / 60);
  if (min < 60) return `${min} 分钟前`;
  const hr = Math.floor(min / 60);
  if (hr < 24) return `${hr} 小时前`;
  const day = Math.floor(hr / 24);
  if (day < 7) return `${day} 天前`;
  return formatDateTime(value);
}

/** 剩余支付时间，格式 mm:ss；已过期返回 null */
export function formatCountdown(value: unknown): string | null {
  const ts = parseDateMs(value);
  if (ts == null) return null;
  const ms = ts - Date.now();
  if (ms <= 0) return null;
  const totalSec = Math.floor(ms / 1000);
  const min = Math.floor(totalSec / 60);
  const sec = totalSec % 60;
  return `${pad(min)}:${pad(sec)}`;
}

export function isExpired(value: unknown): boolean {
  const ts = parseDateMs(value);
  if (ts == null) return false;
  return Date.now() >= ts;
}
