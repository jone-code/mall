<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <span class="logo-mark">C</span>
        <span class="logo-text">ComonOn</span>
      </div>
      <el-menu
        :default-active="active"
        v-model:openeds="openeds"
        router
        class="side-menu"
      >
        <template v-for="entry in visibleMenus" :key="entry.key">
          <el-menu-item
            v-if="entry.type === 'item'"
            :index="entry.path"
            v-perm="entry.perm || ''"
          >
            <el-icon><component :is="entry.icon" /></el-icon>
            <span class="menu-title">{{ entry.title }}</span>
          </el-menu-item>

          <el-sub-menu v-else :index="entry.key">
            <template #title>
              <el-icon><component :is="entry.icon" /></el-icon>
              <span class="menu-title">{{ entry.title }}</span>
              <span v-if="groupBadge(entry) > 0" class="menu-badge">
                {{ groupBadge(entry) > 99 ? '99+' : groupBadge(entry) }}
              </span>
            </template>
            <el-menu-item
              v-for="child in entry.children"
              :key="child.path"
              :index="child.path"
              v-perm="child.perm || ''"
            >
              <span class="menu-title">{{ child.title }}</span>
              <span v-if="child.badge && child.badge > 0" class="menu-badge">
                {{ child.badge > 99 ? '99+' : child.badge }}
              </span>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="title">{{ pageTitle }}</div>
        <div class="actions">
          <EnvBadge style="margin-right: 12px" />
          <router-link to="/sessions" class="sessions-link">登录设备</router-link>
          <span class="username">{{ store.profile?.realName || store.profile?.username || '管理员' }}</span>
          <el-button link @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch, type Component } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { getOpsTodos } from '@/api/dashboard'
import EnvBadge from '@/components/EnvBadge.vue'
import {
  HomeFilled,
  Goods,
  List,
  User,
  Document,
  CreditCard,
  Avatar,
  Key,
  Setting
} from '@element-plus/icons-vue'

type MenuLeaf = {
  path: string
  title: string
  perm?: string
  badgeKey?: 'orders'
  badge?: number
}

type MenuItemEntry = {
  type: 'item'
  key: string
  path: string
  title: string
  icon: Component
  perm?: string
}

type MenuGroupEntry = {
  type: 'group'
  key: string
  title: string
  icon: Component
  children: MenuLeaf[]
}

type MenuEntry = MenuItemEntry | MenuGroupEntry

const store = useAdminStore()
const router = useRouter()
const route = useRoute()
const orderTodoBadge = ref(0)
const openeds = ref<string[]>([])

const menuStructure: MenuEntry[] = [
  { type: 'item', key: 'dashboard', path: '/dashboard', title: '主控台', icon: HomeFilled },
  {
    type: 'group',
    key: 'product',
    title: '商品中心',
    icon: Goods,
    children: [
      { path: '/categories', title: '类目管理', perm: 'product:read' },
      { path: '/products', title: '商品管理', perm: 'product:read' }
    ]
  },
  {
    type: 'group',
    key: 'order',
    title: '订单中心',
    icon: List,
    children: [
      { path: '/orders', title: '订单管理', perm: 'order:read', badgeKey: 'orders' },
      { path: '/reviews', title: '用户评价', perm: 'order:read' }
    ]
  },
  {
    type: 'group',
    key: 'card',
    title: '卡密核销',
    icon: CreditCard,
    children: [
      { path: '/cards', title: '虚拟卡', perm: 'card:read' },
      { path: '/service-codes', title: '核销码池', perm: 'card:read' },
      { path: '/service-verify', title: '服务核销', perm: 'order:write' }
    ]
  },
  {
    type: 'group',
    key: 'member',
    title: '会员运营',
    icon: Avatar,
    children: [{ path: '/members', title: '会员用户', perm: 'member:read' }]
  },
  {
    type: 'group',
    key: 'system',
    title: '系统设置',
    icon: Setting,
    children: [
      { path: '/users', title: '管理账号', perm: 'admin:user:read' },
      { path: '/permissions', title: '权限管理', perm: 'admin:role:read' },
      { path: '/audit', title: '审计日志', perm: 'admin:audit:read' }
    ]
  }
]

function canAccess(perm?: string) {
  return !perm || store.hasPermission(perm)
}

function withBadge(leaf: MenuLeaf): MenuLeaf {
  return {
    ...leaf,
    badge: leaf.badgeKey === 'orders' ? orderTodoBadge.value : 0
  }
}

const visibleMenus = computed(() =>
  menuStructure
    .map((entry) => {
      if (entry.type === 'item') {
        return canAccess(entry.perm) ? entry : null
      }
      const children = entry.children.filter((c) => canAccess(c.perm)).map(withBadge)
      if (!children.length) return null
      return { ...entry, children }
    })
    .filter((entry): entry is MenuItemEntry | (MenuGroupEntry & { children: MenuLeaf[] }) => !!entry)
)

function groupBadge(group: MenuGroupEntry & { children: MenuLeaf[] }) {
  return group.children.reduce((sum, c) => sum + (c.badge ?? 0), 0)
}

function syncOpeneds(path: string) {
  for (const entry of visibleMenus.value) {
    if (entry.type === 'group' && entry.children.some((c) => c.path === path)) {
      if (!openeds.value.includes(entry.key)) {
        openeds.value = [...openeds.value, entry.key]
      }
      return
    }
  }
}

const active = computed(() => route.path)
const pageTitle = computed(() => (route.meta.title as string) || '主控台')

watch(
  () => route.path,
  (path) => syncOpeneds(path),
  { immediate: true }
)

watch(visibleMenus, () => syncOpeneds(route.path), { deep: true })

onMounted(async () => {
  if (!store.profile) {
    try {
      await store.fetchMe()
    } catch (_) {
      /* http interceptor handles 401 */
    }
  }
  if (store.hasPermission('order:read')) {
    try {
      const res = await getOpsTodos()
      const t = res.data
      orderTodoBadge.value = (t?.pendingShip ?? 0) + (t?.refunding ?? 0)
    } catch {
      /* ignore */
    }
  }
})

async function logout() {
  await store.logout()
  router.replace('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background: var(--admin-color-primary);
  color: #fff;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  letter-spacing: 2px;
}

.logo-mark {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.35);
  font-size: 16px;
  font-weight: 600;
}

.logo-text {
  font-size: 15px;
  font-weight: 600;
  text-transform: uppercase;
}

:deep(.side-menu.el-menu) {
  background: transparent;
  border-right: none;
  flex: 1;
  padding: 8px 0;
  overflow-y: auto;
}

:deep(.side-menu .el-menu-item),
:deep(.side-menu .el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.72);
  margin: 2px 10px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 44px;
}

:deep(.side-menu .el-menu-item:hover),
:deep(.side-menu .el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

:deep(.side-menu .el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  position: relative;
}

:deep(.side-menu .el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  background: var(--admin-color-accent);
  border-radius: 0 2px 2px 0;
}

:deep(.side-menu .el-sub-menu.is-opened > .el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.9);
}

:deep(.side-menu .el-sub-menu__icon-arrow) {
  color: rgba(255, 255, 255, 0.45);
}

:deep(.side-menu .el-menu--inline) {
  background: transparent;
}

:deep(.side-menu .el-sub-menu .el-menu-item) {
  height: 40px;
  padding-left: 44px !important;
  font-size: 13px;
}

.header {
  background: var(--admin-color-bg-card);
  border-bottom: 1px solid var(--admin-color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-weight: 600;
  font-size: 16px;
  letter-spacing: 0.5px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  margin-right: 12px;
  color: var(--admin-color-text-secondary);
}
.sessions-link {
  margin-right: 12px;
  font-size: 13px;
  color: var(--admin-color-primary);
  text-decoration: none;
}
.sessions-link:hover {
  text-decoration: underline;
}
  color: var(--admin-color-text-secondary);
  font-size: 14px;
}

.menu-title {
  flex: 1;
}

.menu-badge {
  flex-shrink: 0;
  margin-left: auto;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  line-height: 18px;
  font-size: 11px;
  font-weight: 600;
  text-align: center;
  background: var(--admin-color-accent);
  color: var(--admin-color-primary);
  border-radius: 9px;
}
</style>
