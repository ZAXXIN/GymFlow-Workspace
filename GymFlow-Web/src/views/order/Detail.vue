<template>
  <div class="order-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button icon="i-ep-arrow-left" @click="goBack">
          返回
        </el-button>
        <h1 class="page-title">订单详情</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handlePrint">
          <el-icon><Printer /></el-icon>
          打印订单
        </el-button>
        <el-dropdown @command="handleMoreAction">
          <el-button>
            <el-icon><MoreFilled /></el-icon>
            更多操作
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                command="confirm"
                v-if="orderData?.orderStatus === 'PENDING'"
              >
                <el-icon><Check /></el-icon>
                确认订单
              </el-dropdown-item>
              <el-dropdown-item
                command="complete"
                v-if="orderData?.orderStatus === 'PROCESSING' && orderData?.paymentStatus === 'PAID'"
              >
                <el-icon><Finished /></el-icon>
                完成订单
              </el-dropdown-item>
              <el-dropdown-item
                command="cancel"
                v-if="['PENDING', 'PROCESSING'].includes(orderData?.orderStatus)"
              >
                <el-icon><Close /></el-icon>
                取消订单
              </el-dropdown-item>
              <el-dropdown-item
                command="refund"
                v-if="orderData?.paymentStatus === 'PAID' && orderData?.orderStatus === 'PROCESSING'"
              >
                <el-icon><RefreshLeft /></el-icon>
                退款
              </el-dropdown-item>
              <el-dropdown-divider />
              <el-dropdown-item command="edit" v-if="canEdit">
                <el-icon><Edit /></el-icon>
                编辑订单
              </el-dropdown-item>
              <el-dropdown-item command="delete" class="delete-item">
                <el-icon><Delete /></el-icon>
                删除订单
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 订单信息 -->
    <el-row :gutter="20">
      <!-- 订单基本信息 -->
      <el-col :xs="24" :md="16">
        <el-card class="order-info-card">
          <template #header>
            <div class="card-header">
              <h3>订单信息</h3>
              <div class="order-status">
                <el-tag :type="getOrderStatusType(orderData?.orderStatus)" size="large">
                  {{ getOrderStatusText(orderData?.orderStatus) }}
                </el-tag>
                <el-tag :type="getPaymentStatusType(orderData?.paymentStatus)" size="large">
                  {{ getPaymentStatusText(orderData?.paymentStatus) }}
                </el-tag>
              </div>
            </div>
          </template>
          
          <div class="order-basic-info">
            <div class="info-row">
              <div class="info-item">
                <span class="label">订单编号:</span>
                <span class="value order-no">{{ orderData?.orderNo }}</span>
              </div>
              <div class="info-item">
                <span class="label">订单类型:</span>
                <el-tag :type="getOrderTypeType(orderData?.type)" size="small">
                  {{ getOrderTypeText(orderData?.type) }}
                </el-tag>
              </div>
            </div>
            
            <div class="info-row">
              <div class="info-item">
                <span class="label">下单时间:</span>
                <span class="value">{{ formatDateTime(orderData?.createdAt) }}</span>
              </div>
              <div class="info-item" v-if="orderData?.paidAt">
                <span class="label">支付时间:</span>
                <span class="value">{{ formatDateTime(orderData.paidAt) }}</span>
              </div>
              <div class="info-item" v-if="orderData?.completedAt">
                <span class="label">完成时间:</span>
                <span class="value">{{ formatDateTime(orderData.completedAt) }}</span>
              </div>
            </div>
          </div>
          
          <!-- 订单备注 -->
          <div class="order-notes" v-if="orderData?.notes">
            <h4>订单备注</h4>
            <p>{{ orderData.notes }}</p>
          </div>
        </el-card>
        
        <!-- 商品明细 -->
        <el-card class="items-card">
          <template #header>
            <h3>商品明细</h3>
          </template>
          
          <el-table :data="orderData?.items" border>
            <el-table-column prop="itemName" label="商品名称" width="200" />
            <el-table-column prop="itemType" label="商品类型" width="120">
              <template #default="{ row }">
                {{ getItemTypeText(row.itemType) }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" align="center" />
            <el-table-column prop="unitPrice" label="单价" width="120" align="right">
              <template #default="{ row }">
                ¥{{ row.unitPrice?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="totalPrice" label="小计" width="120" align="right">
              <template #default="{ row }">
                <span class="item-total">¥{{ row.totalPrice?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="佣金" width="120" align="right" v-if="hasCommission">
              <template #default="{ row }">
                <span v-if="row.commissionRate" class="commission">
                  {{ row.commissionRate }}% (¥{{ row.commissionAmount?.toFixed(2) || '0.00' }})
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <!-- 右侧信息面板 -->
      <el-col :xs="24" :md="8">
        <!-- 会员信息 -->
        <el-card class="member-card">
          <template #header>
            <h3>会员信息</h3>
          </template>
          
          <div class="member-info" v-if="orderData?.member">
            <div class="member-avatar-section">
              <el-avatar :size="60" :src="orderData.member.avatar" class="member-avatar">
                {{ orderData.memberName?.charAt(0) || 'M' }}
              </el-avatar>
              <div class="member-details">
                <h4 class="member-name">{{ orderData.memberName }}</h4>
                <p class="member-no">{{ orderData.member.memberNo }}</p>
              </div>
            </div>
            
            <el-divider />
            
            <div class="member-contact">
              <div class="contact-item">
                <el-icon><Phone /></el-icon>
                <span>{{ orderData.member.user?.phone || '未知' }}</span><template>
  <div class="order-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button icon="i-ep-arrow-left" @click="goBack">
          返回
        </el-button>
        <h1 class="page-title">订单详情</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handlePrint">
          <el-icon><Printer /></el-icon>
          打印订单
        </el-button>
        <el-dropdown @command="handleMoreAction">
          <el-button>
            <el-icon><MoreFilled /></el-icon>
            更多操作
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                command="confirm"
                v-if="orderData?.orderStatus === 'PENDING'"
              >
                <el-icon><Check /></el-icon>
                确认订单
              </el-dropdown-item>
              <el-dropdown-item
                command="complete"
                v-if="orderData?.orderStatus === 'PROCESSING' && orderData?.paymentStatus === 'PAID'"
              >
                <el-icon><Finished /></el-icon>
                完成订单
              </el-dropdown-item>
              <el-dropdown-item
                command="cancel"
                v-if="['PENDING', 'PROCESSING'].includes(orderData?.orderStatus)"
              >
                <el-icon><Close /></el-icon>
                取消订单
              </el-dropdown-item>
              <el-dropdown-item
                command="refund"
                v-if="orderData?.paymentStatus === 'PAID' && orderData?.orderStatus === 'PROCESSING'"
              >
                <el-icon><RefreshLeft /></el-icon>
                退款
              </el-dropdown-item>
              <el-dropdown-divider />
              <el-dropdown-item command="edit" v-if="canEdit">
                <el-icon><Edit /></el-icon>
                编辑订单
              </el-dropdown-item>
              <el-dropdown-item command="delete" class="delete-item">
                <el-icon><Delete /></el-icon>
                删除订单
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 订单信息 -->
    <el-row :gutter="20">
      <!-- 订单基本信息 -->
      <el-col :xs="24" :md="16">
        <el-card class="order-info-card">
          <template #header>
            <div class="card-header">
              <h3>订单信息</h3>
              <div class="order-status">
                <el-tag :type="getOrderStatusType(orderData?.orderStatus)" size="large">
                  {{ getOrderStatusText(orderData?.orderStatus) }}
                </el-tag>
                <el-tag :type="getPaymentStatusType(orderData?.paymentStatus)" size="large">
                  {{ getPaymentStatusText(orderData?.paymentStatus) }}
                </el-tag>
              </div>
            </div>
          </template>
          
          <div class="order-basic-info">
            <div class="info-row">
              <div class="info-item">
                <span class="label">订单编号:</span>
                <span class="value order-no">{{ orderData?.orderNo }}</span>
              </div>
              <div class="info-item">
                <span class="label">订单类型:</span>
                <el-tag :type="getOrderTypeType(orderData?.type)" size="small">
                  {{ getOrderTypeText(orderData?.type) }}
                </el-tag>
              </div>
            </div>
            
            <div class="info-row">
              <div class="info-item">
                <span class="label">下单时间:</span>
                <span class="value">{{ formatDateTime(orderData?.createdAt) }}</span>
              </div>
              <div class="info-item" v-if="orderData?.paidAt">
                <span class="label">支付时间:</span>
                <span class="value">{{ formatDateTime(orderData.paidAt) }}</span>
              </div>
              <div class="info-item" v-if="orderData?.completedAt">
                <span class="label">完成时间:</span>
                <span class="value">{{ formatDateTime(orderData.completedAt) }}</span>
              </div>
            </div>
          </div>
          
          <!-- 订单备注 -->
          <div class="order-notes" v-if="orderData?.notes">
            <h4>订单备注</h4>
            <p>{{ orderData.notes }}</p>
          </div>
        </el-card>
        
        <!-- 商品明细 -->
        <el-card class="items-card">
          <template #header>
            <h3>商品明细</h3>
          </template>
          
          <el-table :data="orderData?.items" border>
            <el-table-column prop="itemName" label="商品名称" width="200" />
            <el-table-column prop="itemType" label="商品类型" width="120">
              <template #default="{ row }">
                {{ getItemTypeText(row.itemType) }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" align="center" />
            <el-table-column prop="unitPrice" label="单价" width="120" align="right">
              <template #default="{ row }">
                ¥{{ row.unitPrice?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="totalPrice" label="小计" width="120" align="right">
              <template #default="{ row }">
                <span class="item-total">¥{{ row.totalPrice?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="佣金" width="120" align="right" v-if="hasCommission">
              <template #default="{ row }">
                <span v-if="row.commissionRate" class="commission">
                  {{ row.commissionRate }}% (¥{{ row.commissionAmount?.toFixed(2) || '0.00' }})
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <!-- 右侧信息面板 -->
      <el-col :xs="24" :md="8">
        <!-- 会员信息 -->
        <el-card class="member-card">
          <template #header>
            <h3>会员信息</h3>
          </template>
          
          <div class="member-info" v-if="orderData?.member">
            <div class="member-avatar-section">
              <el-avatar :size="60" :src="orderData.member.avatar" class="member-avatar">
                {{ orderData.memberName?.charAt(0) || 'M' }}
              </el-avatar>
              <div class="member-details">
                <h4 class="member-name">{{ orderData.memberName }}</h4>
                <p class="member-no">{{ orderData.member.memberNo }}</p>
              </div>
            </div>
            
            <el-divider />
            
            <div class="member-contact">
              <div class="contact-item">
                <el-icon><Phone /></el-icon>
                <span>{{ orderData.member.user?.phone || '未知' }}</span>
              </div>
              <div class="contact-item">
                <el-icon><Message /></el-icon>
                <span>{{ orderData.member.user?.email || '未知' }}</span>
              </div>
            </div>
          </div>
          <div v-else class="no-member">
            会员信息未找到
          </div>
        </el-card>
        
        <!-- 支付信息 -->
        <el-card class="payment-card">
          <template #header>
            <h3>支付信息</h3>
          </template>
          
          <div class="payment-info">
            <div class="payment-item">
              <span class="label">支付方式:</span>
              <div class="value">
                <el-icon :class="getPaymentMethodIcon(orderData?.paymentMethod)"></el-icon>
                <span>{{ getPaymentMethodText(orderData?.paymentMethod) }}</span>
              </div>
            </div>
            
            <el-divider />
            
            <div class="amount-summary">
              <div class="amount-item">
                <span class="label">订单总额:</span>
                <span class="value">¥{{ orderData?.amount?.toFixed(2) || '0.00' }}</span>
              </div>
              <div class="amount-item" v-if="orderData?.discountAmount > 0">
                <span class="label">优惠金额:</span>
                <span class="value discount">-¥{{ orderData?.discountAmount?.toFixed(2) || '0.00' }}</span>
              </div>
              <div class="amount-item total">
                <span class="label">实付金额:</span>
                <span class="value total-amount">¥{{ orderData?.paymentAmount?.toFixed(2) || '0.00' }}</span>
              </div>
            </div>
            
            <el-divider />
            
            <div class="commission-summary" v-if="hasCommission">
              <h4>佣金信息</h4>
              <div class="commission-item">
                <span class="label">总佣金:</span>
                <span class="value commission-amount">
                  ¥{{ totalCommission?.toFixed(2) || '0.00' }}
                </span>
              </div>
            </div>
          </div>
        </el-card>
        
        <!-- 操作记录 -->
        <el-card class="logs-card">
          <template #header>
            <h3>操作记录</h3>
          </template>
          
          <div class="logs-list">
            <div class="log-item" v-if="orderData?.createdAt">
              <div class="log-time">{{ formatDateTime(orderData.createdAt) }}</div>
              <div class="log-action">订单创建</div>
            </div>
            <div class="log-item" v-if="orderData?.paidAt">
              <div class="log-time">{{ formatDateTime(orderData.paidAt) }}</div>
              <div class="log-action">订单支付</div>
            </div>
            <div class="log-item" v-if="orderData?.completedAt">
              <div class="log-time">{{ formatDateTime(orderData.completedAt) }}</div>
              <div class="log-action">订单完成</div>
            </div>
            <!-- 可以添加更多操作记录 -->
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Printer,
  MoreFilled,
  Check,
  Finished,
  Close,
  RefreshLeft,
  Edit,
  Delete,
  Phone,
  Message
} from '@element-plus/icons-vue'
import { useOrderStore } from '@/stores/order'
import { useAuthStore } from '@/stores/auth'
import { formatDateTime } from '@/utils'
import type { Order, OrderStatus, PaymentStatus, OrderType, PaymentMethod } from '@/types'

// Store
const orderStore = useOrderStore()
const authStore = useAuthStore()

// Router
const route = useRoute()
const router = useRouter()

// 状态
const loading = ref(false)

// Computed
const orderId = computed(() => parseInt(route.params.id as string))
const orderData = computed(() => orderStore.currentOrder)

const canEdit = computed(() => {
  if (!orderData.value) return false
  return authStore.isAdmin && ['PENDING', 'PROCESSING'].includes(orderData.value.orderStatus)
})

const hasCommission = computed(() => {
  return orderData.value?.items?.some(item => item.commissionRate)
})

const totalCommission = computed(() => {
  if (!orderData.value?.items) return 0
  return orderData.value.items.reduce((sum, item) => {
    return sum + (item.commissionAmount || 0)
  }, 0)
})

// Methods
const goBack = () => {
  router.push('/orders')
}

const loadOrderData = async () => {
  try {
    loading.value = true
    await orderStore.fetchOrderById(orderId.value)
  } catch (error) {
    console.error('加载订单数据失败:', error)
    ElMessage.error('加载订单数据失败')
    goBack()
  } finally {
    loading.value = false
  }
}

const handlePrint = () => {
  window.print()
}

const handleMoreAction = async (command: string) => {
  switch (command) {
    case 'confirm':
      handleConfirmOrder()
      break
    case 'complete':
      handleCompleteOrder()
      break
    case 'cancel':
      handleCancelOrder()
      break
    case 'refund':
      handleRefundOrder()
      break
    case 'edit':
      handleEditOrder()
      break
    case 'delete':
      handleDeleteOrder()
      break
  }
}

const handleConfirmOrder = async () => {
  try {
    await ElMessageBox.confirm('确认处理该订单吗？', '确认订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await orderStore.confirmOrder(orderId.value)
    ElMessage.success('订单已确认')
    loadOrderData()
  } catch (error) {
    console.error('确认订单失败:', error)
    ElMessage.error('确认订单失败')
  }
}

const handleCompleteOrder = async () => {
  try {
    await ElMessageBox.confirm('确认完成该订单吗？', '完成订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await orderStore.completeOrder(orderId.value)
    ElMessage.success('订单已完成')
    loadOrderData()
  } catch (error) {
    console.error('完成订单失败:', error)
    ElMessage.error('完成订单失败')
  }
}

const handleCancelOrder = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入取消原因',
      inputValidator: (value) => {
        if (!value || value.trim().length === 0) {
          return '请输入取消原因'
        }
        return true
      }
    })
    
    await orderStore.cancelOrder(orderId.value, value)
    ElMessage.success('订单已取消')
    loadOrderData()
  } catch (error) {
    console.error('取消订单失败:', error)
  }
}

const handleRefundOrder = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入退款原因', '退款', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入退款原因',
      inputValidator: (value) => {
        if (!value || value.trim().length === 0) {
          return '请输入退款原因'
        }
        return true
      }
    })
    
    await orderStore.refundOrder(orderId.value, value)
    ElMessage.success('退款申请已提交')
    loadOrderData()
  } catch (error) {
    console.error('退款失败:', error)
  }
}

const handleEditOrder = () => {
  router.push(`/orders/${orderId.value}/edit`)
}

const handleDeleteOrder = async () => {
  try {
    await ElMessageBox.confirm('确认删除该订单吗？此操作不可恢复。', '删除订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    
    await orderStore.deleteOrder(orderId.value)
    ElMessage.success('删除成功')
    goBack()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

const getOrderStatusType = (status?: OrderStatus) => {
  if (!status) return 'info'
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'PROCESSING':
      return 'primary'
    case 'COMPLETED':
      return 'success'
    case 'CANCELLED':
      return 'danger'
    case 'REFUNDED':
      return 'info'
    default:
      return 'info'
  }
}

const getOrderStatusText = (status?: OrderStatus) => {
  if (!status) return '未知'
  switch (status) {
    case 'PENDING':
      return '待处理'
    case 'PROCESSING':
      return '处理中'
    case 'COMPLETED':
      return '已完成'
    case 'CANCELLED':
      return '已取消'
    case 'REFUNDED':
      return '已退款'
    default:
      return '未知'
  }
}

const getPaymentStatusType = (status?: PaymentStatus) => {
  if (!status) return 'info'
  switch (status) {
    case 'PAID':
      return 'success'
    case 'PENDING':
      return 'warning'
    case 'FAILED':
      return 'danger'
    case 'REFUNDED':
      return 'info'
    default:
      return 'info'
  }
}

const getPaymentStatusText = (status?: PaymentStatus) => {
  if (!status) return '未知'
  switch (status) {
    case 'PAID':
      return '已支付'
    case 'PENDING':
      return '待支付'
    case 'FAILED':
      return '支付失败'
    case 'REFUNDED':
      return '已退款'
    default:
      return '未知'
  }
}

const getOrderTypeType = (type?: OrderType) => {
  if (!type) return 'info'
  switch (type) {
    case 'MEMBERSHIP':
      return 'success'
    case 'COURSE_PACKAGE':
      return 'primary'
    case 'PERSONAL_TRAINING':
      return 'warning'
    case 'PRODUCT':
      return 'info'
    default:
      return 'info'
  }
}

const getOrderTypeText = (type?: OrderType) => {
  if (!type) return ''
  switch (type) {
    case 'MEMBERSHIP':
      return '会籍卡'
    case 'COURSE_PACKAGE':
      return '课程套餐'
    case 'PERSONAL_TRAINING':
      return '私教课'
    case 'PRODUCT':
      return '商品'
    default:
      return type
  }
}

const getItemTypeText = (type?: string) => {
  if (!type) return ''
  switch (type) {
    case 'MEMBERSHIP_CARD':
      return '会籍卡'
    case 'COURSE_PACKAGE':
      return '课程套餐'
    case 'PERSONAL_TRAINING_SESSION':
      return '私教课程'
    case 'PRODUCT_ITEM':
      return '商品'
    default:
      return type
  }
}

const getPaymentMethodIcon = (method?: PaymentMethod) => {
  if (!method) return ''
  const icons: Record<string, string> = {
    WECHAT: 'i-ep-wechat',
    ALIPAY: 'i-ep-alipay',
    CASH: 'i-ep-money',
    CARD: 'i-ep-credit-card',
    BANK_TRANSFER: 'i-ep-bank-card'
  }
  return icons[method] || 'i-ep-money'
}

const getPaymentMethodText = (method?: PaymentMethod) => {
  if (!method) return ''
  switch (method) {
    case 'WECHAT':
      return '微信支付'
    case 'ALIPAY':
      return '支付宝'
    case 'CASH':
      return '现金'
    case 'CARD':
      return '刷卡'
    case 'BANK_TRANSFER':
      return '银行转账'
    default:
      return method
  }
}

// 生命周期
onMounted(() => {
  loadOrderData()
})
</script>

<style lang="scss" scoped>
.order-detail-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .page-title {
        font-size: 24px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
        margin: 0;
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }
  
  .order-info-card {
    border-radius: 12px;
    margin-bottom: 20px;
    
    :deep(.el-card__header) {
      padding: 20px;
      border-bottom: 1px solid var(--gymflow-border);
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
          color: var(--gymflow-text-primary);
        }
        
        .order-status {
          display: flex;
          gap: 8px;
        }
      }
    }
    
    .order-basic-info {
      .info-row {
        display: flex;
        margin-bottom: 16px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .info-item {
          flex: 1;
          display: flex;
          align-items: center;
          gap: 8px;
          
          .label {
            font-size: 14px;
            color: var(--gymflow-text-secondary);
            min-width: 80px;
          }
          
          .value {
            font-size: 14px;
            font-weight: 500;
            color: var(--gymflow-text-primary);
            
            &.order-no {
              font-family: monospace;
              color: var(--gymflow-primary);
              font-weight: 600;
            }
          }
        }
      }
    }
    
    .order-notes {
      margin-top: 20px;
      padding: 16px;
      background: var(--gymflow-bg);
      border-radius: 8px;
      
      h4 {
        font-size: 14px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
        margin: 0 0 8px;
      }
      
      p {
        font-size: 14px;
        color: var(--gymflow-text-secondary);
        margin: 0;
        line-height: 1.6;
      }
    }
  }
  
  .items-card {
    border-radius: 12px;
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid var(--gymflow-border);
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
      }
    }
    
    :deep(.el-table) {
      th {
        background: var(--gymflow-bg);
        font-weight: 600;
      }
      
      .item-total {
        font-weight: 600;
        color: var(--gymflow-primary);
      }
      
      .commission {
        font-size: 12px;
        color: var(--gymflow-text-secondary);
      }
    }
  }
  
  .member-card {
    border-radius: 12px;
    margin-bottom: 20px;
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid var(--gymflow-border);
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
      }
    }
    
    .member-info {
      .member-avatar-section {
        display: flex;
        align-items: center;
        gap: 16px;
        margin-bottom: 16px;
        
        .member-avatar {
          background: var(--gymflow-primary);
          color: white;
          font-size: 20px;
          font-weight: bold;
        }
        
        .member-details {
          .member-name {
            font-size: 18px;
            font-weight: 600;
            color: var(--gymflow-text-primary);
            margin: 0 0 4px;
          }
          
          .member-no {
            font-size: 14px;
            color: var(--gymflow-text-secondary);
            margin: 0;
          }
        }
      }
      
      .member-contact {
        .contact-item {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 8px;
          
          &:last-child {
            margin-bottom: 0;
          }
          
          .el-icon {
            font-size: 16px;
            color: var(--gymflow-primary);
          }
          
          span {
            font-size: 14px;
            color: var(--gymflow-text-primary);
          }
        }
      }
    }
    
    .no-member {
      text-align: center;
      padding: 20px;
      color: var(--gymflow-text-secondary);
      font-size: 14px;
    }
  }
  
  .payment-card {
    border-radius: 12px;
    margin-bottom: 20px;
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid var(--gymflow-border);
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
      }
    }
    
    .payment-info {
      .payment-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        
        .label {
          font-size: 14px;
          color: var(--gymflow-text-secondary);
        }
        
        .value {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 14px;
          font-weight: 500;
          color: var(--gymflow-text-primary);
          
          .el-icon {
            font-size: 18px;
          }
        }
      }
      
      .amount-summary {
        .amount-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;
          
          &:last-child {
            margin-bottom: 0;
          }
          
          &.total {
            padding-top: 12px;
            border-top: 1px solid var(--gymflow-border);
            margin-top: 12px;
          }
          
          .label {
            font-size: 14px;
            color: var(--gymflow-text-secondary);
          }
          
          .value {
            font-size: 14px;
            font-weight: 500;
            color: var(--gymflow-text-primary);
            
            &.discount {
              color: var(--el-color-danger);
            }
            
            &.total-amount {
              font-size: 18px;
              font-weight: 700;
              color: var(--gymflow-primary);
            }
          }
        }
      }
      
      .commission-summary {
        h4 {
          font-size: 14px;
          font-weight: 600;
          color: var(--gymflow-text-primary);
          margin: 0 0 12px;
        }
        
        .commission-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .label {
            font-size: 14px;
            color: var(--gymflow-text-secondary);
          }
          
          .commission-amount {
            font-size: 16px;
            font-weight: 600;
            color: var(--gymflow-secondary);
          }
        }
      }
    }
  }
  
  .logs-card {
    border-radius: 12px;
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid var(--gymflow-border);
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
      }
    }
    
    .logs-list {
      .log-item {
        padding: 12px 0;
        border-bottom: 1px solid var(--gymflow-border);
        
        &:last-child {
          border-bottom: none;
        }
        
        .log-time {
          font-size: 12px;
          color: var(--gymflow-text-secondary);
          margin-bottom: 4px;
        }
        
        .log-action {
          font-size: 14px;
          color: var(--gymflow-text-primary);
          font-weight: 500;
        }
      }
    }
  }
}

:deep(.delete-item) {
  color: var(--el-color-danger);
  
  &:hover {
    background: rgba(245, 108, 108, 0.1);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .order-detail-container {
    padding: 16px;
    
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    
    .order-info-card {
      .order-basic-info {
        .info-row {
          flex-direction: column;
          gap: 12px;
        }
      }
    }
  }
}

// 打印样式
@media print {
  .order-detail-container {
    padding: 0;
    
    .page-header,
    .member-card,
    .payment-card .payment-item:first-child,
    .logs-card {
      display: none;
    }
    
    .order-info-card,
    .items-card,
    .payment-card {
      box-shadow: none;
      border: 1px solid #ddd;
      margin-bottom: 10px;
    }
  }
}
</style>