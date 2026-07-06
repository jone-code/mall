<template>
  <div class="dashboard">
    <el-card class="welcome" shadow="never">
      <h3 style="margin: 0">
        欢迎回来{{ store.profile?.realName ? '，' + store.profile.realName : '' }}
      </h3>
      <p class="welcome-sub">
        当前拥有 {{ store.permissions.length }} 项权限点
      </p>
    </el-card>

    <el-row v-if="canOrder || canCard || canProduct" :gutter="16">
      <el-col v-if="canOrder" :xs="24" :md="16">
        <el-card shadow="never" v-loading="todoLoading">
          <template #header>运营待办</template>
          <div class="todo-grid">
            <router-link class="todo-item warning" :to="{ path: '/orders', query: { status: 'PENDING_PAY' } }">
              <span class="todo-label">待支付</span>
              <span class="todo-value">{{ todos?.pendingPay ?? 0 }}</span>
            </router-link>
            <router-link class="todo-item success" :to="{ path: '/orders', query: { status: 'PAID' } }">
              <span class="todo-label">待发货</span>
              <span class="todo-value">{{ todos?.pendingShip ?? 0 }}</span>
            </router-link>
            <router-link class="todo-item danger" :to="{ path: '/orders', query: { status: 'REFUNDING' } }">
              <span class="todo-label">退款中</span>
              <span class="todo-value">{{ todos?.refunding ?? 0 }}</span>
            </router-link>
            <router-link v-if="canCard" class="todo-item info" to="/cards">
              <span class="todo-label">可用卡密</span>
              <span class="todo-value">{{ todos?.virtualCardAvailable ?? 0 }}</span>
            </router-link>
          </div>
          <el-alert
            v-if="canCard && (todos?.virtualPoolEmpty ?? 0) > 0"
            type="warning"
            :closable="false"
            show-icon
            style="margin-top: 12px"
            :title="`${todos?.virtualPoolEmpty} 个虚拟商品卡密池已耗尽，请及时补货`"
          >
            <router-link to="/cards">前往卡密池</router-link>
          </el-alert>
          <el-alert
            v-if="canCard && (todos?.servicePoolEmpty ?? 0) > 0"
            type="warning"
            :closable="false"
            show-icon
            style="margin-top: 8px"
            :title="`${todos?.servicePoolEmpty} 个服务商品核销码池已耗尽，请及时补货`"
          >
            <router-link to="/service-codes">前往核销码池</router-link>
          </el-alert>
          <el-alert
            v-if="canProduct && (productStats?.lowStock ?? 0) > 0"
            type="warning"
            :closable="false"
            show-icon
            style="margin-top: 8px"
            :title="`${productStats?.lowStock} 个在售 SKU 库存低于 10，请关注补货`"
          >
            <router-link to="/products">前往商品管理</router-link>
          </el-alert>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never" v-loading="orderLoading">
          <template #header>今日概览</template>
          <div class="mini-stats">
            <div v-if="canOrder" class="mini-item">
              <span>今日订单</span>
              <strong>{{ orderStats?.todayOrders ?? 0 }}</strong>
            </div>
            <div v-if="canOrder" class="mini-item">
              <span>今日成交额</span>
              <strong class="money">¥{{ money(orderStats?.todayGmv) }}</strong>
            </div>
            <div v-if="canMember" class="mini-item">
              <span>今日新用户</span>
              <strong>{{ memberStats?.todayNewUsers ?? 0 }}</strong>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row v-if="canOrder" :gutter="16" class="stat-row" v-loading="orderLoading">
      <el-col :xs="12" :sm="6">
        <div class="stat-tile">
          <div class="stat-label">今日订单</div>
          <div class="stat-value">{{ orderStats?.todayOrders ?? 0 }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-tile">
          <div class="stat-label">今日成交额</div>
          <div class="stat-value money">¥{{ money(orderStats?.todayGmv) }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-tile">
          <div class="stat-label">累计成交额</div>
          <div class="stat-value money">¥{{ money(orderStats?.totalGmv) }}</div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-tile">
          <div class="stat-label">今日新用户</div>
          <div class="stat-value">{{ memberStats?.todayNewUsers ?? 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row v-if="canOrder" :gutter="16">
      <el-col :span="24">
        <el-card shadow="never" v-loading="trendLoading">
          <template #header>
            <div class="trend-head">
              <span>订单趋势</span>
              <el-radio-group v-model="trendDays" size="small" @change="loadTrends">
                <el-radio-button :value="7">近 7 天</el-radio-button>
                <el-radio-button :value="30">近 30 天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div v-if="trendPoints.length" class="trend-chart">
            <div v-for="p in trendPoints" :key="p.date" class="trend-bar-group">
              <div class="trend-bars">
                <div
                  class="trend-bar orders"
                  :style="{ height: barHeight(p.orderCount, maxOrders) }"
                  :title="`${p.date}: ${p.orderCount} 单`"
                />
                <div
                  class="trend-bar gmv"
                  :style="{ height: barHeight(p.gmv, maxGmv) }"
                  :title="`${p.date}: ¥${Number(p.gmv).toFixed(2)}`"
                />
              </div>
              <span class="trend-label">{{ p.date.slice(5) }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无趋势数据" :image-size="60" />
          <div class="trend-legend">
            <span><i class="dot orders" />订单量</span>
            <span><i class="dot gmv" />成交额</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col v-if="canOrder" :xs="24" :md="14">
        <el-card shadow="never" v-loading="orderLoading">
          <template #header>订单状态分布</template>
          <div class="breakdown">
            <router-link
              v-for="s in orderBreakdown"
              :key="s.key"
              class="bd-item link"
              :to="{ path: '/orders', query: { status: s.status } }"
            >
              <el-tag :type="s.tag" size="small">{{ s.label }}</el-tag>
              <span class="bd-count">{{ s.count }}</span>
            </router-link>
            <el-empty
              v-if="!orderStats || orderStats.totalOrders === 0"
              description="暂无订单"
              :image-size="60"
            />
          </div>
        </el-card>
      </el-col>

      <el-col v-if="canProduct" :xs="24" :md="10">
        <el-card shadow="never" v-loading="productLoading">
          <template #header>商品概况</template>
          <div class="breakdown">
            <router-link class="bd-item link" to="/products">
              <el-tag type="success" size="small">在售</el-tag>
              <span class="bd-count">{{ productStats?.onSale ?? 0 }}</span>
            </router-link>
            <div class="bd-item">
              <el-tag type="info" size="small">已下架</el-tag>
              <span class="bd-count">{{ productStats?.offSale ?? 0 }}</span>
            </div>
            <div class="bd-item">
              <el-tag size="small">草稿</el-tag>
              <span class="bd-count">{{ productStats?.draft ?? 0 }}</span>
            </div>
            <div class="bd-item">
              <el-tag type="warning" size="small">商品总数</el-tag>
              <span class="bd-count">{{ productStats?.total ?? 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="!canOrder && !canProduct" shadow="never">
      <el-empty description="暂无可展示的统计数据（缺少订单/商品查看权限）" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import {
  getOrderStats,
  getOrderTrends,
  getProductStats,
  getMemberStats,
  getOpsTodos,
  type OrderStats,
  type OrderTrendPoint,
  type ProductStats,
  type MemberStats,
  type OpsTodo
} from '@/api/dashboard'

const store = useAdminStore()
const canOrder = computed(() => store.hasPermission('order:read'))
const canProduct = computed(() => store.hasPermission('product:read'))
const canMember = computed(() => store.hasPermission('member:read'))
const canCard = computed(() => store.hasPermission('card:read'))

const orderLoading = ref(false)
const productLoading = ref(false)
const todoLoading = ref(false)
const orderStats = ref<OrderStats | null>(null)
const productStats = ref<ProductStats | null>(null)
const memberStats = ref<MemberStats | null>(null)
const todos = ref<OpsTodo | null>(null)
const trendLoading = ref(false)
const trendDays = ref(7)
const trendPoints = ref<OrderTrendPoint[]>([])

const maxOrders = computed(() => Math.max(1, ...trendPoints.value.map((p) => p.orderCount)))
const maxGmv = computed(() => Math.max(1, ...trendPoints.value.map((p) => Number(p.gmv))))

function barHeight(value: number, max: number) {
  const pct = Math.max(4, Math.round((value / max) * 100))
  return `${pct}%`
}

const orderBreakdown = computed(() => {
  const s = orderStats.value
  return [
    { key: 'pendingPay', status: 'PENDING_PAY', label: '待支付', count: s?.pendingPay ?? 0, tag: 'warning' as const },
    { key: 'paid', status: 'PAID', label: '待发货', count: s?.paid ?? 0, tag: 'success' as const },
    { key: 'shipped', status: 'SHIPPED', label: '待收货', count: s?.shipped ?? 0, tag: '' as const },
    { key: 'completed', status: 'COMPLETED', label: '已完成', count: s?.completed ?? 0, tag: 'info' as const },
    { key: 'cancelled', status: 'CANCELLED', label: '已取消', count: s?.cancelled ?? 0, tag: 'danger' as const },
    { key: 'refunding', status: 'REFUNDING', label: '退款中', count: s?.refunding ?? 0, tag: 'warning' as const },
    { key: 'refunded', status: 'REFUNDED', label: '已退款', count: s?.refunded ?? 0, tag: 'info' as const }
  ]
})

function money(v?: number) {
  return Number(v ?? 0).toFixed(2)
}

async function loadOrders() {
  if (!canOrder.value) return
  orderLoading.value = true
  try {
    const res = await getOrderStats()
    orderStats.value = res.data
  } catch (e: any) {
    ElMessage.error(e?.message || '订单统计加载失败')
  } finally {
    orderLoading.value = false
  }
}

async function loadTodos() {
  if (!canOrder.value) return
  todoLoading.value = true
  try {
    const res = await getOpsTodos()
    todos.value = res.data
  } catch {
    /* ignore */
  } finally {
    todoLoading.value = false
  }
}

async function loadProducts() {
  if (!canProduct.value) return
  productLoading.value = true
  try {
    const res = await getProductStats()
    productStats.value = res.data
  } catch (e: any) {
    ElMessage.error(e?.message || '商品统计加载失败')
  } finally {
    productLoading.value = false
  }
}

async function loadTrends() {
  if (!canOrder.value) return
  trendLoading.value = true
  try {
    const res = await getOrderTrends(trendDays.value)
    trendPoints.value = res.data?.points || []
  } catch {
    trendPoints.value = []
  } finally {
    trendLoading.value = false
  }
}

async function loadMembers() {
  if (!canMember.value) return
  try {
    const res = await getMemberStats()
    memberStats.value = res.data
  } catch {
    /* ignore */
  }
}

onMounted(() => {
  loadOrders()
  loadTrends()
  loadTodos()
  loadProducts()
  loadMembers()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.welcome-sub {
  margin: 8px 0 0;
  color: var(--admin-color-text-secondary);
}
.stat-row {
  margin: 0;
}
.stat-tile {
  background: var(--admin-color-bg-card);
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}
.stat-label {
  color: var(--admin-color-text-secondary);
  font-size: 13px;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  margin-top: 8px;
  color: var(--admin-color-text);
}
.stat-value.money {
  color: var(--admin-color-accent);
}
.todo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}
.todo-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px;
  border-radius: 4px;
  text-decoration: none;
  color: inherit;
  background: var(--admin-color-bg);
  transition: transform 0.15s, box-shadow 0.15s;
}
.todo-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.todo-label {
  font-size: 13px;
  color: #606266;
}
.todo-value {
  font-size: 24px;
  font-weight: 700;
}
.todo-item.warning .todo-value { color: #e6a23c; }
.todo-item.success .todo-value { color: #67c23a; }
.todo-item.danger .todo-value { color: #f56c6c; }
.todo-item.info .todo-value { color: #409eff; }
.mini-stats {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.mini-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #606266;
}
.mini-item strong {
  font-size: 18px;
  color: #303133;
}
.mini-item strong.money {
  color: #f56c6c;
}
.breakdown {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}
.bd-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.bd-item.link {
  text-decoration: none;
  color: inherit;
  cursor: pointer;
}
.bd-item.link:hover .bd-count {
  color: #409eff;
}
.bd-count {
  font-size: 20px;
  font-weight: 600;
}
.trend-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.trend-chart {
  display: flex;
  align-items: flex-end;
  gap: 6px;
  height: 160px;
  overflow-x: auto;
  padding-bottom: 4px;
}
.trend-bar-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 36px;
  flex: 1;
}
.trend-bars {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  height: 130px;
  width: 100%;
  justify-content: center;
}
.trend-bar {
  width: 10px;
  border-radius: 3px 3px 0 0;
  min-height: 4px;
}
.trend-bar.orders { background: var(--admin-color-primary); }
.trend-bar.gmv { background: var(--admin-color-accent); }
.trend-label {
  font-size: 11px;
  color: #909399;
  margin-top: 6px;
}
.trend-legend {
  display: flex;
  gap: 16px;
  margin-top: 12px;
  font-size: 12px;
  color: #606266;
}
.trend-legend .dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 2px;
  margin-right: 4px;
  vertical-align: middle;
}
.trend-legend .dot.orders { background: var(--admin-color-primary); }
.trend-legend .dot.gmv { background: var(--admin-color-accent); }
</style>
