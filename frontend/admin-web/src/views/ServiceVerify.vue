<template>
  <div class="service-verify">
    <el-card class="verify-card">
      <template #header>
        <div class="verify-head">
          <span>服务核销</span>
          <span class="hint">支持扫码枪输入，回车确认</span>
        </div>
      </template>
      <el-form label-position="top" class="verify-form" @submit.prevent="onVerify">
        <el-form-item label="核销码" required>
          <el-input
            ref="codeInputRef"
            v-model="code"
            class="code-input"
            placeholder="扫描或输入 SVC 核销码"
            clearable
            autofocus
            @keyup.enter="onVerify"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="onVerify">
          确认核销
        </el-button>
      </el-form>

      <el-result
        v-if="result"
        icon="success"
        title="核销成功"
        class="verify-result"
      >
        <template #sub-title>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="订单号">
              <CopyText :text="result.orderNo" success-msg="订单号已复制" />
            </el-descriptions-item>
            <el-descriptions-item label="状态">{{ orderStatusLabel(result.status) }}</el-descriptions-item>
            <el-descriptions-item label="核销码">{{ result.fulfillment?.verifyCode }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">
              <RelativeTime :value="result.completeAt || result.verifiedAt" />
            </el-descriptions-item>
          </el-descriptions>
        </template>
        <template #extra>
          <el-button @click="resetForm">继续核销</el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { InputInstance } from 'element-plus'
import { verifyServiceCode } from '@/api/serviceCode'
import { orderStatusLabel, type OrderRow } from '@/api/order'
import CopyText from '@/components/CopyText.vue'
import RelativeTime from '@/components/RelativeTime.vue'

const code = ref('')
const loading = ref(false)
const result = ref<OrderRow | null>(null)
const codeInputRef = ref<InputInstance>()

async function onVerify() {
  const v = code.value.trim()
  if (!v) {
    ElMessage.warning('请输入核销码')
    return
  }
  loading.value = true
  try {
    const res = await verifyServiceCode(v)
    result.value = res.data as OrderRow
    ElMessage.success('核销成功')
  } catch (e: any) {
    ElMessage.error(e?.message || '核销失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  code.value = ''
  result.value = null
  nextTick(() => codeInputRef.value?.focus())
}

onMounted(() => {
  nextTick(() => codeInputRef.value?.focus())
})
</script>

<style scoped>
.service-verify {
  max-width: 560px;
  margin: 0 auto;
}
.verify-head {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.hint {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}
.verify-form {
  margin-top: 8px;
}
.code-input :deep(.el-input__inner) {
  font-size: 18px;
  letter-spacing: 0.5px;
}
.submit-btn {
  width: 100%;
  margin-top: 8px;
}
.verify-result {
  margin-top: 24px;
}
@media (max-width: 768px) {
  .service-verify {
    max-width: none;
    padding: 0 4px;
  }
  .verify-card {
    border: none;
    box-shadow: none;
  }
  .code-input :deep(.el-input__inner) {
    font-size: 20px;
    min-height: 48px;
  }
  .submit-btn {
    min-height: 48px;
    font-size: 16px;
  }
}
</style>
