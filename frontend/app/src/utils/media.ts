/** 将后端返回的相对路径转为可访问 URL */
export function resolveMediaUrl(url?: string | null): string {
  if (!url) return "";
  const trimmed = url.trim();
  if (!trimmed) return "";
  if (/^https?:\/\//i.test(trimmed)) return trimmed;

  const path = trimmed.startsWith("/") ? trimmed : `/${trimmed}`;

  const fileBase = import.meta.env.VITE_FILE_BASE as string | undefined;
  if (fileBase) {
    const base = fileBase.replace(/\/$/, "");
    return `${base}${path}`;
  }

  const apiBase = import.meta.env.VITE_API_BASE as string | undefined;
  if (apiBase?.startsWith("http")) {
    const origin = apiBase.replace(/\/api\/?$/, "");
    return `${origin}${path}`;
  }

  // H5 开发：Vite 代理 /files → BFF
  return path;
}

/** 将 rich-text HTML 内相对图片路径转为可访问 URL */
export function resolveRichHtml(html?: string | null): string {
  if (!html) return "";
  return html.replace(/src=(["'])([^"']+)\1/gi, (_match, quote, url) => {
    return `src=${quote}${resolveMediaUrl(url)}${quote}`;
  });
}
