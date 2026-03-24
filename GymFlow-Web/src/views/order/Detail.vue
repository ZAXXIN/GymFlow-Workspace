<template>
  <div class="order-detail-container">
    <!-- 页面头部 -->
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <div class="header-content">
          <span class="page-title">订单详情</span>
          <div class="header-actions">
          </div>
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

        <el-descriptions-item label="支付状态">
          <el-tag :type="currentOrder?.paymentStatus === 1 ? 'success' : 'warning'" size="small">
            {{ currentOrder?.paymentStatusDesc || '-' }}
          </el-tag>
          <div class="sub-info" v-if="currentOrder?.paymentTime">
            支付时间：{{ formatDateTime(currentOrder.paymentTime) }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusTagType(currentOrder?.orderStatus || '')" size="small">
            {{ currentOrder?.orderStatusDesc || '-' }}
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
        <el-table-column prop="paymentStatus" label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatus === 1 ? 'success' : 'warning'" size="small">
              {{ row.paymentStatus === 1 ? '成功' : '失败' }}
            </el-tag>
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

    <!-- 支付对话框 -->
    <!-- <PaymentDialog
      v-model="paymentDialogVisible"
      :order-id="orderId"
      @success="handlePaymentSuccess"
    /> -->

    <!-- 退款对话框 -->
    <!-- <RefundDialog
      v-model="refundDialogVisible"
      :order-id="orderId"
      @success="handleRefundSuccess"
    /> -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useOrderStore } from '@/stores/order'
// import PaymentDialog from './components/PaymentDialog.vue'
// import RefundDialog from './components/RefundDialog.vue'
// import type { OrderDetailVO } from '@/types/order'

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()

const loading = ref(false)
const paymentDialogVisible = ref(false)
const refundDialogVisible = ref(false)

const orderId = computed(() => Number(route.params.id))
const currentOrder = computed(() => orderStore.currentOrder)

// 支付记录（模拟数据，实际应该从API获取）
const paymentRecords = computed(() => {
  if (!currentOrder.value) return []

  // 这里应该从API获取支付记录
  // 暂时模拟数据
  return []
})

// 是否为课程订单
const isCourseOrder = computed(() => {
  return currentOrder.value?.orderType === 1 || currentOrder.value?.orderType === 2
})

// 是否有有效期
const hasValidity = computed(() => {
  return currentOrder.value?.orderType === 0 || isCourseOrder.value
})

// 格式化函数
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

// 商品类型描述
const getProductTypeDesc = (productType: number | undefined) => {
  if (productType === undefined) return '未知'
  switch (productType) {
    case 0:
      return '会籍卡'
    case 1:
      return '私教课'
    case 2:
      return '团课'
    case 3:
      return '相关产品'
    default:
      return '未知'
  }
}

// 商品项状态
const getItemStatusType = (status: string | undefined) => {
  if (!status) return 'info'
  switch (status) {
    case 'UNPAID':
      return 'warning'
    case 'PAID':
      return 'primary'
    case 'ACTIVE':
      return 'success'
    case 'EXPIRED':
      return 'danger'
    case 'USED_UP':
      return 'info'
    default:
      return 'info'
  }
}

const getItemStatusDesc = (status: string | undefined) => {
  if (!status) return '未知'
  switch (status) {
    case 'UNPAID':
      return '未支付'
    case 'PAID':
      return '已支付'
    case 'ACTIVE':
      return '生效中'
    case 'EXPIRED':
      return '已过期'
    case 'USED_UP':
      return '已用完'
    default:
      return '未知'
  }
}

// 状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'PROCESSING':
      return 'primary'
    case 'COMPLETED':
      return 'success'
    case 'CANCELLED':
      return 'info'
    case 'REFUNDED':
      return 'danger'
    case 'DELETED':
      return 'info'
    default:
      return 'info'
  }
}

// 加载订单详情
const loadOrderDetail = async () => {
  try {
    loading.value = true
    await orderStore.fetchOrderDetail(orderId.value)
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

// 编辑订单
const goEdit = () => {
  router.push(`/order/edit/${orderId.value}`)
}

// 支付订单
const handlePay = () => {
  paymentDialogVisible.value = true
}

// 完成订单
const handleComplete = async () => {
  try {
    await ElMessageBox.confirm('确定要完成此订单吗？', '提示', {
      type: 'warning',
    })

    await orderStore.completeOrder(orderId.value)
    ElMessage.success('订单已完成')
    await loadOrderDetail()
  } catch (error) {
    // 用户取消
  }
}

// 取消订单
const handleCancel = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
    })

    await orderStore.cancelOrder(orderId.value, reason)
    ElMessage.success('订单已取消')
    await loadOrderDetail()
  } catch (error) {
    // 用户取消
  }
}

// 更多操作
const handleMoreAction = async (command: string) => {
  switch (command) {
    case 'refund':
      refundDialogVisible.value = true
      break
    case 'print':
      handlePrint()
      break
    case 'delete':
      handleDelete()
      break
  }
}

// 打印订单
const handlePrint = () => {
  ElMessage.info('打印功能开发中...')
}

// 删除订单
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除此订单吗？', '警告', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })

    await orderStore.deleteOrder(orderId.value)
    ElMessage.success('订单已删除')
    goBack()
  } catch (error) {
    // 用户取消
  }
}

// 支付成功回调
const handlePaymentSuccess = async () => {
  paymentDialogVisible.value = false
  await loadOrderDetail()
  ElMessage.success('支付成功')
}

// 退款成功回调
const handleRefundSuccess = async () => {
  refundDialogVisible.value = false
  await loadOrderDetail()
  ElMessage.success('退款成功')
}

// 返回
const goBack = () => {
  router.push('/order/list')
}

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