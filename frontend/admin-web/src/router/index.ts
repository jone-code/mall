import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const layoutChildren: RouteRecordRaw[] = [
  {
    path: 'dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { title: '主控台' }
  },
  {
    path: 'users',
    name: 'AdminUsers',
    component: () => import('@/views/AdminUsers.vue'),
    meta: { title: '管理账号', perm: 'admin:user:read' }
  },
  {
    path: 'members',
    name: 'Members',
    component: () => import('@/views/Members.vue'),
    meta: { title: '会员用户', perm: 'member:read' }
  },
  {
    path: 'permissions',
    name: 'Permissions',
    component: () => import('@/views/Permissions.vue'),
    meta: { title: '权限管理', perm: 'admin:role:read' }
  },
  {
    path: 'categories',
    name: 'Categories',
    component: () => import('@/views/Categories.vue'),
    meta: { title: '类目管理', perm: 'product:read' }
  },
  {
    path: 'products',
    name: 'Products',
    component: () => import('@/views/Products.vue'),
    meta: { title: '商品管理', perm: 'product:read' }
  },
  {
    path: 'orders',
    name: 'Orders',
    component: () => import('@/views/Orders.vue'),
    meta: { title: '订单管理', perm: 'order:read' }
  },
  {
    path: 'cards',
    name: 'Cards',
    component: () => import('@/views/Cards.vue'),
    meta: { title: '虚拟卡', perm: 'card:read' }
  },
  {
    path: 'service-codes',
    name: 'ServiceCodes',
    component: () => import('@/views/ServiceCodes.vue'),
    meta: { title: '核销码池', perm: 'card:read' }
  },
  {
    path: 'service-verify',
    name: 'ServiceVerify',
    component: () => import('@/views/ServiceVerify.vue'),
    meta: { title: '服务核销', perm: 'order:write' }
  },
  {
    path: 'reviews',
    name: 'Reviews',
    component: () => import('@/views/Reviews.vue'),
    meta: { title: '用户评价', perm: 'order:read' }
  },
  {
    path: 'audit',
    name: 'Audit',
    component: () => import('@/views/Audit.vue'),
    meta: { title: '审计日志', perm: 'admin:audit:read' }
  }
]

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/force-pwd-reset',
    name: 'ForcePwdReset',
    component: () => import('@/views/ForcePwdReset.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: layoutChildren
  },
  {
    path: '/no-permission',
    name: 'NoPermission',
    component: () => import('@/views/NoPermission.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const store = useAdminStore()
  if (to.path === '/login') {
    if (store.isLoggedIn) return next('/dashboard')
    return next()
  }
  if (to.meta?.requiresAuth && !store.isLoggedIn) {
    const restored = await store.tryRestoreSession()
    if (!restored) {
      return next({ path: '/login', query: { redirect: to.fullPath } })
    }
  }
  const requiredPerm = (to.meta as { perm?: string }).perm
  if (requiredPerm && !store.hasPermission(requiredPerm)) {
    return next('/no-permission')
  }
  next()
})

export default router
