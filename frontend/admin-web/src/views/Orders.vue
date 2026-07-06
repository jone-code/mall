<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span>订单管理</span>
        <div class="filters">
          <el-select v-model="status" placeholder="全部状态" clearable style="width: 130px" @change="reload">
            <el-option label="待支付" value="PENDING_PAY" />
            <el-option label="待发货" value="PAID" />
            <el-option label="待收货" value="SHIPPED" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="退款中" value="REFUNDING" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
          <el-select v-model="productType" placeholder="商品类型" clearable style="width: 110px" @change="reload">
            <el-option label="实物" value="PHYSICAL" />
            <el-option label="虚拟" value="VIRTUAL" />
            <el-option label="服务" value="SERVICE" />
          </el-select>
          <el-input
            v-model="orderNo"
            placeholder="订单号"
            clearable
            style="width: 170px"
            @keyup.enter="reload"
            @clear="reload"
          />
          <el-input
            v-model="phone"
            placeholder="手机号"
            clearable
            style="width: 130px"
            @keyup.enter="reload"
            @clear="reload"
          />
          <el-input
            v-model.number="userId"
            placeholder="用户ID"
            clearable
            style="width: 110px"
            @keyup.enter="reload"
            @clear="reload"
          />
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="~"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 230px"
            @change="reload"
          />
          <el-button type="primary" @click="reload">查询</el-button>
          <el-button @click="exportCsv">导出 CSV</el-button>
          <el-button
            v-perm.disable="'order:write'"
            v-if="selectedShippable.length"
            type="success"
            @click="openBatchShip"
          >
            批量发货 ({{ selectedShippable.length }})
          </el-button>
        </div>
      </div>
    </template>

    <el-table v-loading="loading" :data="rows" stripe @selection-change="onSelect">
      <template #empty>
        <TableEmpty description="暂无订单，可调整筛选条件" />
      </template>
      <el-table-column v-if="canWrite" type="selection" width="48" :selectable="isShippable" />
      <el-table-column label="订单号" min-width="210">
        <template #default="{ row }">
          <CopyText :text="row.orderNo" success-msg="订单号已复制" />
        </template>
      </el-table-column>
      <el-table-column label="用户" min-width="120">
        <template #default="{ row }">
          {{ formatUserLabel(row.userId, row.userNickname) }}
        </template>
      </el-table-column>
      <el-table-column label="类型" width="72">
        <template #default="{ row }">{{ productTypeLabel(row.productType) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="orderStatusTag(row.status)" size="small">
            {{ orderStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="应付" width="110" align="right" class-name="col-money">
        <template #default="{ row }">¥{{ formatMoney(row.payAmount) }}</template>
      </el-table-column>
      <el-table-column prop="itemCount" label="件数" width="72" align="right" />
      <el-table-column label="下单时间" min-width="120">
        <template #default="{ row }">
          <RelativeTime :value="row.createdAt" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="openDetail(row.orderNo)">详情</el-button>
          <el-button
            v-if="canWrite && row.status === 'PAID' && row.productType === 'PHYSICAL'"
            link
            type="success"
            size="small"
            @click="openShip(row.orderNo)"
          >
            发货
          </el-button>
          <el-button
            v-if="canWrite && row.status === 'REFUNDING'"
            link
            type="warning"
            size="small"
            @click="onApproveRefund(row.orderNo)"
          >
            同意退款
          </el-button>
          <el-button
            v-if="canWrite && row.status === 'REFUNDING'"
            link
            size="small"
            @click="onRejectRefund(row.orderNo)"
          >
            拒绝退款
          </el-button>
          <el-button
            v-if="canWrite && row.status === 'PENDING_PAY'"
            link
            type="danger"
            size="small"
            @click="onClose(row.orderNo)"
          >
            关单
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="load"
        @size-change="reload"
      />
    </div>
  </el-card>

  <el-dialog v-model="detailVisible" title="订单详情" width="640px">
    <template v-if="detail">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="订单号">
          <CopyText v-if="detail.orderNo" :text="detail.orderNo" success-msg="订单号已复制" />
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="orderStatusTag(detail.status)" size="small">
            {{ orderStatusLabel(detail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户">
          {{ formatUserLabel(detail.userId, detail.userNickname) }}
        </el-descriptions-item>
        <el-descriptions-item label="收货人">{{ detail.receiver || '—' }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ detail.receiverPhone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="地址" :span="2">{{ detail.addressDetail || '—' }}</el-descriptions-item>
        <el-descriptions-item label="商品金额">¥{{ Number(detail.totalAmount).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="运费">¥{{ Number(detail.freightAmount).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="应付">¥{{ formatMoney(detail.payAmount) }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">
          <RelativeTime :value="detail.createdAt" />
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.payAt" label="支付时间">{{ detail.payAt }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.shipAt" label="发货时间">{{ detail.shipAt }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.trackingNo" label="物流单号">
          {{ detail.trackingCompany || '快递' }} {{ detail.trackingNo }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.completeAt" label="完成时间">{{ detail.completeAt }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.fulfillment?.cardNo" label="卡号">{{ detail.fulfillment.cardNo }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.fulfillment?.verifyCode" label="核销码">{{ detail.fulfillment.verifyCode }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.payment?.payNo" label="支付单">{{ detail.payment.payNo }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.payment?.channel" label="支付渠道">{{ detail.payment.channel }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.refundReason" label="退款原因">{{ detail.refundReason }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.refundAt" label="退款时间">{{ detail.refundAt }}</el-descriptions-item>
        <el-descriptions-item label="运营备注" :span="2">
          <el-input v-if="canWrite" v-model="adminRemark" type="textarea" :rows="2" placeholder="运营备注" />
          <span v-else>{{ detail.adminRemark || '—' }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.remark" label="用户备注" :span="2">{{ detail.remark }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="detail.logs?.length" style="margin-top: 16px">
        <div style="font-weight: 600; margin-bottom: 8px">操作时间线</div>
        <el-timeline>
          <el-timeline-item
            v-for="log in detail.logs"
            :key="log.id"
            :timestamp="log.createdAt"
          >
            <span v-if="log.fromStatus">{{ orderStatusLabel(log.fromStatus) }} → </span>
            <strong>{{ orderStatusLabel(log.toStatus) }}</strong>
            <span v-if="log.remark" class="muted"> — {{ log.remark }}</span>
            <span v-if="log.operatorType" class="muted"> ({{ log.operatorType }}{{ log.operatorId ? ':' + log.operatorId : '' }})</span>
          </el-timeline-item>
        </el-timeline>
      </div>

      <div v-if="logisticsEvents.length" style="margin-top: 16px">
        <div style="font-weight: 600; margin-bottom: 8px">物流轨迹（Mock）</div>
        <el-timeline>
          <el-timeline-item v-for="(ev, i) in logisticsEvents" :key="i" :timestamp="ev.time">
            {{ ev.status }} — {{ ev.desc }}
          </el-timeline-item>
        </el-timeline>
      </div>

      <el-table :data="detail.items || []" size="small" style="margin-top: 16px" border>
        <el-table-column prop="title" label="商品" min-width="160" />
        <el-table-column prop="specText" label="规格" min-width="120" />
        <el-table-column label="单价" width="90">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="70" />
        <el-table-column label="小计" width="100">
          <template #default="{ row }">¥{{ Number(row.subtotal).toFixed(2) }}</template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 16px; text-align: right; display: flex; gap: 8px; justify-content: flex-end; flex-wrap: wrap">
        <el-button v-if="canWrite" @click="saveRemark">保存备注</el-button>
        <el-button v-if="canWrite && detail.status === 'REFUNDING'" type="warning" @click="onApproveRefund(detail.orderNo)">同意退款</el-button>
        <el-button v-if="canWrite && detail.status === 'REFUNDING'" @click="onRejectRefund(detail.orderNo)">拒绝退款</el-button>
        <el-button v-if="canWrite && detail.status === 'PAID' && detail.productType === 'PHYSICAL'" type="primary" @click="openShip(detail.orderNo)">发货</el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog v-model="shipVisible" title="订单发货" width="420px" @closed="resetShipForm">
    <el-form label-width="88px">
      <el-form-item label="物流公司">
        <el-input v-model="shipForm.trackingCompany" placeholder="如：顺丰、中通" clearable />
      </el-form-item>
      <el-form-item label="运单号" required>
        <el-input v-model="shipForm.trackingNo" placeholder="请输入物流单号" clearable />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="shipVisible = false">取消</el-button>
      <el-button type="primary" :loading="shipLoading" @click="submitShip">确认发货</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="batchShipVisible" title="批量发货" width="640px">
    <el-alert type="info" :closable="false" show-icon title="为每个订单填写物流单号，留空则跳过" style="margin-bottom: 12px" />
    <el-table :data="batchShipRows" size="small" border>
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column label="物流公司" min-width="120">
        <template #default="{ row }">
          <el-input v-model="row.trackingCompany" placeholder="顺丰" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="运单号" min-width="160">
        <template #default="{ row }">
          <el-input v-model="row.trackingNo" placeholder="必填" size="small" />
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button @click="batchShipVisible = false">取消</el-button>
      <el-button type="primary" :loading="batchShipLoading" @click="submitBatchShip">确认发货</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import { productTypeLabel } from '@/api/product'
import CopyText from '@/components/CopyText.vue'
import RelativeTime from '@/components/RelativeTime.vue'
import TableEmpty from '@/components/TableEmpty.vue'
import { formatMoney } from '@/utils/format'
import {
  listOrders,
  getOrder,
  closeOrder,
  shipOrder,
  batchShipOrders,
  updateOrderRemark,
  approveRefund,
  rejectRefund,
  getOrderLogistics,
  exportOrders,
  orderStatusLabel,
  orderStatusTag,
  formatUserLabel,
  type OrderRow,
  type BatchShipItem
} from '@/api/order'

const store = useAdminStore()
const route = useRoute()
const canWrite = computed(() => store.hasPermission('order:write'))

const loading = ref(false)
const rows = ref<OrderRow[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const status = ref<string>('')
const productType = ref<string>('')
const orderNo = ref('')
const phone = ref('')
const userId = ref<number | undefined>(undefined)
const dateRange = ref<[string, string] | null>(null)

const detailVisible = ref(false)
const detail = ref<OrderRow | null>(null)
const adminRemark = ref('')
const logisticsEvents = ref<{ time: string; status: string; desc: string }[]>([])

const shipVisible = ref(false)
const shipLoading = ref(false)
const shipOrderNo = ref('')
const shipForm = ref({ trackingNo: '', trackingCompany: '' })

const selectedRows = ref<OrderRow[]>([])
const selectedShippable = computed(() =>
  selectedRows.value.filter((r) => r.status === 'PAID' && r.productType === 'PHYSICAL')
)
const batchShipVisible = ref(false)
const batchShipLoading = ref(false)
const batchShipRows = ref<BatchShipItem[]>([])

function isShippable(row: OrderRow) {
  return row.status === 'PAID' && row.productType === 'PHYSICAL'
}

function onSelect(rows: OrderRow[]) {
  selectedRows.value = rows
}

function applyRouteQuery() {
  const q = route.query
  status.value = (q.status as string) || ''
  productType.value = (q.productType as string) || ''
  orderNo.value = (q.orderNo as string) || ''
  const uid = q.userId ? Number(q.userId) : undefined
  userId.value = uid && !Number.isNaN(uid) ? uid : undefined
  if (q.today === '1') {
    const today = new Date().toISOString().slice(0, 10)
    dateRange.value = [today, today]
  } else if (q.from && q.to) {
    dateRange.value = [String(q.from), String(q.to)]
  }
}

async function load() {
  loading.value = true
  try {
    const res = await listOrders({
      status: status.value || undefined,
      productType: productType.value || undefined,
      orderNo: orderNo.value || undefined,
      phone: phone.value || undefined,
      userId: userId.value || undefined,
      from: dateRange.value?.[0],
      to: dateRange.value?.[1],
      page: page.value,
      size: size.value
    })
    rows.value = res.data?.list || []
    total.value = res.data?.total || 0
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

async function openDetail(orderNoVal: string) {
  try {
    const res = await getOrder(orderNoVal)
    detail.value = res.data
    adminRemark.value = res.data?.adminRemark || ''
    detailVisible.value = true
    logisticsEvents.value = []
    if (res.data?.trackingNo) {
      try {
        const lg = await getOrderLogistics(orderNoVal)
        logisticsEvents.value = lg.data?.events || []
      } catch {
        /* ignore */
      }
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  }
}

async function exportCsv() {
  try {
    await exportOrders({
      status: status.value || undefined,
      productType: productType.value || undefined,
      orderNo: orderNo.value || undefined,
      phone: phone.value || undefined,
      userId: userId.value || undefined,
      from: dateRange.value?.[0],
      to: dateRange.value?.[1]
    })
    ElMessage.success('导出成功')
  } catch (e: any) {
    ElMessage.error(e?.message || '导出失败')
  }
}

function openBatchShip() {
  batchShipRows.value = selectedShippable.value.map((r) => ({
    orderNo: r.orderNo,
    trackingNo: '',
    trackingCompany: ''
  }))
  batchShipVisible.value = true
}

async function submitBatchShip() {
  const items = batchShipRows.value
    .filter((r) => r.trackingNo.trim())
    .map((r) => ({
      orderNo: r.orderNo,
      trackingNo: r.trackingNo.trim(),
      trackingCompany: r.trackingCompany?.trim() || undefined
    }))
  if (!items.length) {
    ElMessage.warning('请至少填写一个运单号')
    return
  }
  try {
    await ElMessageBox.confirm(`确认批量发货 ${items.length} 个订单？`, '批量发货', { type: 'warning' })
  } catch {
    return
  }
  batchShipLoading.value = true
  try {
    const res = await batchShipOrders(items)
    const data = res.data
    if (data?.failed) {
      ElMessage.warning(`成功 ${data.success}，失败 ${data.failed}`)
    } else {
      ElMessage.success(`成功发货 ${data?.success ?? items.length} 单`)
    }
    batchShipVisible.value = false
    load()
  } catch (e: any) {
    ElMessage.error(e?.message || '批量发货失败')
  } finally {
    batchShipLoading.value = false
  }
}

async function saveRemark() {
  if (!detail.value) return
  try {
    await updateOrderRemark(detail.value.orderNo, adminRemark.value)
    ElMessage.success('备注已保存')
    detail.value.adminRemark = adminRemark.value
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  }
}

async function onApproveRefund(orderNoVal: string) {
  try {
    await ElMessageBox.confirm('确认 Mock 原路退款？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await approveRefund(orderNoVal)
    ElMessage.success('已退款')
    load()
    if (detailVisible.value && detail.value?.orderNo === orderNoVal) {
      detail.value = (await getOrder(orderNoVal)).data
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function onRejectRefund(orderNoVal: string) {
  try {
    await rejectRefund(orderNoVal)
    ElMessage.success('已拒绝退款')
    load()
    if (detailVisible.value && detail.value?.orderNo === orderNoVal) {
      detail.value = (await getOrder(orderNoVal)).data
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

async function onClose(orderNoVal: string) {
  try {
    await ElMessageBox.confirm('确认强制关闭该订单并释放库存？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await closeOrder(orderNoVal)
    ElMessage.success('已关单')
    load()
  } catch (e: any) {
    ElMessage.error(e?.message || '关单失败')
  }
}

function openShip(orderNoVal: string) {
  shipOrderNo.value = orderNoVal
  shipForm.value = { trackingNo: '', trackingCompany: '' }
  shipVisible.value = true
}

function resetShipForm() {
  shipOrderNo.value = ''
  shipForm.value = { trackingNo: '', trackingCompany: '' }
}

async function submitShip() {
  const trackingNo = shipForm.value.trackingNo.trim()
  if (!trackingNo) {
    ElMessage.warning('请填写运单号')
    return
  }
  shipLoading.value = true
  try {
    await shipOrder(shipOrderNo.value, {
      trackingNo,
      trackingCompany: shipForm.value.trackingCompany.trim() || undefined
    })
    ElMessage.success('发货成功')
    shipVisible.value = false
    load()
    if (detailVisible.value && detail.value?.orderNo === shipOrderNo.value) {
      const res = await getOrder(shipOrderNo.value)
      detail.value = res.data
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '发货失败')
  } finally {
    shipLoading.value = false
  }
}

watch(
  () => route.query,
  () => {
    applyRouteQuery()
    reload()
  }
)

onMounted(() => {
  applyRouteQuery()
  load()
})
</script>

<style scoped>
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.muted {
  color: #909399;
  font-size: 12px;
}
</style>
