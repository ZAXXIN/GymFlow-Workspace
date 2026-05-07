<template>
  <div class="order-detail-container">
    <!-- 页面头部 -->
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <div class="header-content">
          <span class="page-title">订单详情</span>
        </div>
      </template>
    </el-page-header>

    <!-- 基本信息卡片 -->
    <el-card class="info-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">基本信息</span>
          <div class="card-actions">
            <el-tag :type="getStatusTagType(currentOrder?.orderStatus || '')" size="large">
              {{ currentOrder?.orderStatusDesc }}
            </el-tag>
            <span class="order-no">{{ currentOrder?.orderNo }}</span>
          </div>
        </div>
      </template>

      <el-descriptions :column="3" border>
        <el-descriptions-item label="订单编号">{{ currentOrder?.orderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">{{ currentOrder?.orderTypeDesc || '-' }}</el-descriptions-item>

        <el-descriptions-item label="会员姓名">
          <div class="member-info">
            <div class="member-name">{{ currentOrder?.memberName || '-' }}</div>
            <div class="member-phone">{{ currentOrder?.memberPhone || '-' }}</div>
          </div>
        </el-descriptions-item>
        <!-- <el-descriptions-item label="会员ID">{{ currentOrder?.memberId || '-' }}</el-descriptions-item> -->
        <el-descriptions-item label="支付方式">{{ currentOrder?.paymentMethod || '现金' }}</el-descriptions-item>

        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusTagType(currentOrder?.status || '')" size="small">
            {{ currentOrder?.statusDesc || '-' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentOrder?.createTime) }}</el-descriptions-item>
        <!-- <el-descriptions-item label="最后更新">{{ formatDateTime(currentOrder?.updateTime) }}</el-descriptions-item> -->

        <el-descriptions-item label="订单金额">
          <div class="amount-info">
            <div class="amount-item">
              <span class="amount-label">商品总价：</span>
              <span class="amount-value">{{ formatAmount(currentOrder?.totalAmount) }}</span>
            </div>
            <div class="amount-item">
              <span class="amount-label">实付金额：</span>
              <span class="amount-value actual">{{ formatAmount(currentOrder?.actualAmount) }}</span>
            </div>
            <div class="amount-item" v-if="currentOrder?.totalAmount !== currentOrder?.actualAmount">
              <span class="amount-label">优惠金额：</span>
              <span class="amount-value discount">-{{ formatAmount((currentOrder?.totalAmount || 0) - (currentOrder?.actualAmount || 0)) }}</span>
            </div>
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="备注">
          {{ currentOrder?.remark || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 商品信息 -->
    <el-card class="products-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">商品信息</span>
          <span class="card-subtitle">共 {{ currentOrder?.orderItems?.length || 0 }} 件商品</span>
        </div>
      </template>

      <div v-if="currentOrder?.orderItems && currentOrder.orderItems.length > 0">
        <el-table :data="currentOrder.orderItems" style="width: 100%" border stripe>
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="商品信息" min-width="250">
            <template #default="{ row }">
              <div class="product-info">
                <el-image v-if="row.productImage" :src="row.productImage" :preview-src-list="[row.productImage]" fit="cover" style="width: 50px; height: 50px; margin-right: 10px;" />
                <div class="product-text">
                  <div class="product-name">{{ row.productName }}</div>
                  <div class="product-type text-gray">{{ getProductTypeDesc(row.productType) }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120" align="right">
            <template #default="{ row }">
              {{ formatAmount(row.unitPrice) }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100" align="center" />
          <el-table-column label="总价" width="120" align="right">
            <template #default="{ row }">
              <span class="text-primary">{{ formatAmount(row.totalPrice) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="课时信息" width="200" v-if="isCourseOrder">
            <template #default="{ row }">
              <div v-if="row.totalSessions">
                <div>总课时：{{ row.totalSessions }}</div>
                <div>剩余课时：{{ row.remainingSessions }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="有效期" width="200" v-if="hasValidity">
            <template #default="{ row }">
              <div v-if="row.validityStartDate && row.validityEndDate">
                <div>开始：{{ formatDate(row.validityStartDate) }}</div>
                <div>结束：{{ formatDate(row.validityEndDate) }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="getItemStatusType(row.status)">
                {{ getItemStatusDesc(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 金额汇总 -->
        <div class="amount-summary">
          <div class="summary-row">
            <span>商品总价：</span>
            <span>{{ formatAmount(currentOrder.totalAmount) }}</span>
          </div>
          <div class="summary-row" v-if="currentOrder.totalAmount !== currentOrder.actualAmount">
            <span>优惠金额：</span>
            <span class="text-success">-{{ formatAmount(currentOrder.totalAmount - currentOrder.actualAmount) }}</span>
          </div>
          <div class="summary-row total">
            <span>实付金额：</span>
            <span class="total-amount">{{ formatAmount(currentOrder.actualAmount) }}</span>
          </div>
        </div>
      </div>

      <div v-else class="empty-data">
        <el-empty description="暂无商品信息" />
      </div>
    </el-card>

    <!-- 支付记录 -->
    <el-card class="payment-card" v-if="paymentRecords && paymentRecords.length > 0">
      <template #header>
        <div class="card-header">
          <span class="card-title">支付记录</span>
        </div>
      </template>

      <el-table :data="paymentRecords" style="width: 100%">
        <el-table-column prop="paymentNo" label="支付流水号" width="180" />
        <el-table-column prop="paymentMethod" label="支付方式" width="100" />
        <el-table-column prop="paymentAmount" label="支付金额" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.paymentAmount < 0 ? 'text-danger' : 'text-success'">
              {{ formatAmount(row.paymentAmount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentTime" label="支付时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.paymentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="transactionId" label="交易号" min-width="150" />
        <el-table-column prop="remark" label="备注" min-width="150" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useOrderStore } from '@/stores/order'

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()

const loading = ref(false)

const orderId = computed(() => Number(route.params.id))
const currentOrder = computed(() => orderStore.currentOrder)

// 支付记录（后端 payment_record 表已删，此处保留为空数组）
const paymentRecords = computed(() => [])

const isCourseOrder = computed(() => {
  return currentOrder.value?.orderType === 1 || currentOrder.value?.orderType === 2
})
const hasValidity = computed(() => {
  return currentOrder.value?.orderType === 0 || isCourseOrder.value
})

const formatDate = (date: string | null | undefined) => {
  if (!date) return '-'
  return date.split('T')[0]
}
const formatDateTime = (datetime: string | null | undefined) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ')
}
const formatAmount = (amount: number | null | undefined) => {
  if (!amount) return '0.00'
  return `¥${amount.toFixed(2)}`
}

const getProductTypeDesc = (productType: number | undefined) => {
  if (productType === undefined) return '未知'
  const map: Record<number, string> = { 0: '会籍卡', 1: '私教课', 2: '团课', 3: '相关产品' }
  return map[productType] || '未知'
}

const getItemStatusType = (status: string | undefined) => {
  if (!status) return 'info'
  const map: Record<string, string> = {
    UNPAID: 'warning',
    PAID: 'primary',
    ACTIVE: 'success',
    EXPIRED: 'danger',
    USED_UP: 'info',
  }
  return map[status] || 'info'
}
const getItemStatusDesc = (status: string | undefined) => {
  if (!status) return '未知'
  const map: Record<string, string> = {
    UNPAID: '未支付',
    PAID: '已支付',
    ACTIVE: '生效中',
    EXPIRED: '已过期',
    USED_UP: '已用完',
  }
  return map[status] || '未知'
}

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'WAIT_PAY':
      return 'warning'
    case 'PAID':
      return 'primary'
    case 'COMPLETED':
      return 'success'
    case 'CANCELLED':
      return 'info'
    case 'REFUNDED':
      return 'danger'
    default:
      return 'info'
  }
}

const loadOrderDetail = async () => {
  try {
    loading.value = true
    await orderStore.fetchOrderDetail(orderId.value)
  } catch (error) {
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => router.push('/order/list')

onMounted(() => {
  loadOrderDetail()
})
</script>
<style scoped lang="scss">
.order-detail-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.info-card,
.products-card,
.payment-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-subtitle {
  font-size: 14px;
  color: #909399;
}

.order-no {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
  padding: 4px 12px;
  background-color: #ecf5ff;
  border-radius: 4px;
  margin-left: 10px;
}

.member-info {
  .member-name {
    font-weight: 500;
    margin-bottom: 4px;
  }

  .member-phone {
    font-size: 12px;
    color: #909399;
  }
}

.sub-info {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.amount-info {
  .amount-item {
    margin-bottom: 8px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .amount-label {
    color: #606266;
    margin-right: 8px;
  }

  .amount-value {
    font-weight: 600;

    &.actual {
      color: #67c23a;
    }

    &.discount {
      color: #e6a23c;
    }
  }
}

.product-info {
  display: flex;
  align-items: center;

  .product-text {
    flex: 1;

    .product-name {
      font-weight: 500;
      margin-bottom: 4px;
    }

    .product-type {
      font-size: 12px;
      color: #909399;
    }
  }
}

.text-gray {
  color: #909399;
}

.text-primary {
  color: #409eff;
  font-weight: 600;
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}

.amount-summary {
  margin-top: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
  max-width: 400px;
  margin-left: auto;

  .summary-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
    font-size: 14px;
    color: #606266;

    &:last-child {
      margin-bottom: 0;
    }

    &.total {
      padding-top: 12px;
      border-top: 1px solid #e0e0e0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;

      .total-amount {
        color: #67c23a;
        font-size: 18px;
      }
    }
  }
}

.empty-data {
  padding: 40px 0;
  text-align: center;
}

:deep(.el-card__header) {
  padding: 16px 20px;
}

:deep(.el-descriptions__body) {
  background-color: white;
}

:deep(.el-descriptions__cell) {
  padding: 12px 16px;
}

:deep(.el-table__header) {
  background-color: #f8f9fa;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>