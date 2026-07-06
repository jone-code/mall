const PERM_LABELS: Record<string, string> = {
  'product:read': '查看商品',
  'product:write': '维护商品',
  'product:offline': '商品下架',
  'order:read': '查看订单',
  'order:write': '处理订单',
  'card:read': '查看卡密',
  'card:import': '导入卡密',
  'member:read': '查看会员',
  'member:write': '维护会员',
  'admin:user:read': '查看管理员',
  'admin:user:create': '创建管理员',
  'admin:role:read': '查看角色',
  'admin:role:write': '维护角色',
  'admin:role:assign': '分配角色',
  'admin:permission:read': '查看权限点',
  'admin:permission:write': '维护权限点',
  'admin:permission:assign': '分配权限',
  'admin:audit:read': '查看审计日志'
}

export function permissionLabel(code?: string): string {
  if (!code) return '相应'
  return PERM_LABELS[code] || code
}

export function permissionTip(codes: string | string[]): string {
  const list = Array.isArray(codes) ? codes : [codes]
  const labels = list.map(permissionLabel).join(' 或 ')
  return `需要 ${labels} 权限`
}
