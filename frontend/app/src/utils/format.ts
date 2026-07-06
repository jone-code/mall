export function copyToClipboard(text: string): Promise<boolean> {
  const value = text?.trim();
  if (!value) return Promise.resolve(false);
  return new Promise((resolve) => {
    uni.setClipboardData({
      data: value,
      success: () => resolve(true),
      fail: () => resolve(false),
    });
  });
}

export function maskSecret(value?: string, visibleTail = 4): string {
  if (!value) return "—";
  if (value.length <= visibleTail) return value;
  return "•".repeat(Math.min(value.length - visibleTail, 12)) + value.slice(-visibleTail);
}
