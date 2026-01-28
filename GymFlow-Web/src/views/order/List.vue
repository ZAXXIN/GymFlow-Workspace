<template>
  <div class="order-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">订单管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>订单管理</el-breadcrumb-item>
          <el-breadcrumb-item>订单列表</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreateOrder">
          <el-icon><Plus /></el-icon>
          创建订单
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>
    
    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline label-width="80px">
        <el-form-item label="订单编号">
          <el-input
            v-model="filterForm.orderNo"
            placeholder="请输入订单编号"
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="会员姓名">
          <el-input
            v-model="filterForm.memberName"
            placeholder="请输入会员姓名"
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="订单类型">
          <el-select
            v-model="filterForm.type"
            placeholder="请选择类型"
            clearable
            style="width: 180px;"
          >
            <el-option
              v-for="item in orderTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select
            v-model="filterForm.paymentStatus"
            placeholder="请选择状态"
            clearable
            style="width: 180px;"
          >
            <el-option
              v-for="item in paymentStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select
            v-model="filterForm.orderStatus"
            placeholder="请选择状态"
            clearable
            style="width: 180px;"
          >
            <el-option
              v-for="item in orderStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card class="stats-card">
        <div class="stats-content">
          <div class="stats-icon total">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-number">{{ stats?.totalOrders || 0 }}</div>
            <div class="stats-label">总订单数</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stats-card">
        <div class="stats-content">
          <div class="stats-icon revenue">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-number">¥{{ formatAmount(stats?.totalRevenue || 0) }}</div>
            <div class="stats-label">总营业额</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stats-card">
        <div class="stats-content">
          <div class="stats-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-number">{{ stats?.pendingOrders || 0 }}</div>
            <div class="stats-label">待处理订单</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stats-card">
        <div class="stats-content">
          <div class="stats-icon today">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-number">¥{{ formatAmount(stats?.todayRevenue || 0) }}</div>
            <div class="stats-label">今日营业额</div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 数据表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="table-title">订单列表</span>
          <div class="table-actions">
            <el-button text @click="refreshTable">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <app-table
        :data="orderList"
        :total="pagination.total"
        :show-add="false"
        :show-export="false"
        :page="pagination.current"
        :page-size="pagination.size"
        :show-selection="false"
        @refresh="refreshTable"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="orderNo" label="订单编号" width="160" sortable />
        <el-table-column prop="memberName" label="会员" width="120">
          <template #default="{ row }">
            <div class="member-cell">
              <el-avatar :size="32" :src="row.member?.avatar" class="member-avatar">
                {{ row.memberName?.charAt(0) || 'M' }}
              </el-avatar>
              <span class="member-name">{{ row.memberName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="订单类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOrderTypeType(row.type)" size="small">
              {{ getOrderTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" width="150">
          <template #default="{ row }">
            <div class="amount-cell">
              <div class="total-amount">¥{{ row.amount?.toFixed(2) }}</div>
              <div v-if="row.discountAmount > 0" class="discount-amount">
                优惠: -¥{{ row.discountAmount?.toFixed(2) }}
              </div>
              <div class="payment-amount">
                实付: <span class="pay-amount">¥{{ row.paymentAmount?.toFixed(2) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="items" label="商品明细" min-width="200">
          <template #default="{ row }">
            <div class="items-cell">
              <div
                v-for="item in row.items.slice(0, 2)"
                :key="item.id"
                class="item-tag"
              >
                {{ item.itemName }} ×{{ item.quantity }}
              </div>
              <div v-if="row.items.length > 2" class="more-items">
                等{{ row.items.length }}件商品
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120">
          <template #default="{ row }">
            <div class="payment-method">
              <el-icon :class="getPaymentMethodIcon(row.paymentMethod)"></el-icon>
              <span>{{ getPaymentMethodText(row.paymentMethod) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getPaymentStatusType(row.paymentStatus)" size="small">
              {{ getPaymentStatusText(row.paymentStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="订单状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.orderStatus)" size="small">
              {{ getOrderStatusText(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" width="160" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <template #actions="{ row }">
          <el-button type="text" size="small" @click="handleViewDetail(row.id)">
            <el-icon><View /></el-icon>
            详情
          </el-button>
          <el-dropdown @command="handleMoreAction($event, row)" trigger="click">
            <el-button type="text" size="small">
              <el-icon><MoreFilled /></el-icon>
              更多
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  command="confirm"
                  v-if="row.orderStatus === 'PENDING'"
                >
                  <el-icon><Check /></el-icon>
                  确认订单
                </el-dropdown-item>
                <el-dropdown-item
                  command="complete"
                  v-if="row.orderStatus === 'PROCESSING' && row.paymentStatus === 'PAID'"
                >
                  <el-icon><Finished /></el-icon>
                  完成订单
                </el-dropdown-item>
                <el-dropdown-item
                  command="cancel"
                  v-if="['PENDING', 'PROCESSING'].includes(row.orderStatus)"
                >
                  <el-icon><Close /></el-icon>
                  取消订单
                </el-dropdown-item>
                <el-dropdown-item
                  command="refund"
                  v-if="row.paymentStatus === 'PAID' && row.orderStatus === 'PROCESSING'"
                >
                  <el-icon><RefreshLeft /></el-icon>
                  退款
                </el-dropdown-item>
                <el-dropdown-divider />
                <el-dropdown-item command="delete" class="delete-item">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </app-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import AppTable from '@/components/common/AppTable.vue'
import { useOrderStore } from '@/stores/order'
import {
  orderTypeOptions,
  paymentStatusOptions,
  OrderStatus,
  PaymentStatus,
  OrderType,
  PaymentMethod
} from '@/utils/constants'
import { formatDateTime, formatAmount } from '@/utils'
import type { Order, QueryParams } from '@/types'

// Store
const orderStore = useOrderStore()

// Router
const router = useRouter()

// 状态
const loading = ref(false)

const filterForm = reactive({
  orderNo: '',
  memberName: '',
  type: '' as OrderType | '',
  paymentStatus: '' as PaymentStatus | '',
  orderStatus: '' as OrderStatus | '',
  dateRange: [] as string[]
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const sortParams = reactive({
  sortBy: '',
  sortOrder: '' as 'asc' | 'desc' | ''
})

const stats = ref({
  totalOrders: 0,
  totalRevenue: 0,
  pendingOrders: 0,
  todayRevenue: 0
})

// Computed
const orderList = computed(() => orderStore.orders)

const orderStatusOptions = [
  { label: '待处理', value: 'PENDING' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '已退款', value: 'REFUNDED' }
]

// Methods
const loadOrders = async () => {
  try {
    loading.value = true
    
    const params: QueryParams = {
      page: pagination.current,
      pageSize: pagination.size,
      ...buildFilterParams()
    }
    
    if (sortParams.sortBy) {
      params.sortBy = sortParams.sortBy
      params.sortOrder = sortParams.sortOrder
    }
    
    await orderStore.fetchOrders(params)
    pagination.total = orderStore.total
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const loadOrderStats = async () => {
  try {
    const data = await orderStore.getOrderStatistics()
    stats.value = data
  } catch (error) {
    console.error('加载订单统计失败:', error)
  }
}

const buildFilterParams = () => {
  const params: Record<string, any> = {}
  
  if (filterForm.orderNo) {
    params.orderNo = filterForm.orderNo
  }
  
  if (filterForm.memberName) {
    params.memberName = filterForm.memberName
  }
  
  if (filterForm.type) {
    params.type = filterForm.type
  }
  
  if (filterForm.paymentStatus) {
    params.paymentStatus = filterForm.paymentStatus
  }
  
  if (filterForm.orderStatus) {
    params.orderStatus = filterForm.orderStatus
  }
  
  if (filterForm.dateRange?.length === 2) {
    params.startDate = filterForm.dateRange[0]
    params.endDate = filterForm.dateRange[1]
  }
  
  return params
}

const handleSearch = () => {
  pagination.current = 1
  loadOrders()
}

const handleReset = () => {
  filterForm.orderNo = ''
  filterForm.memberName = ''
  filterForm.type = ''
  filterForm.paymentStatus = ''
  filterForm.orderStatus = ''
  filterForm.dateRange = []
  
  pagination.current = 1
  loadOrders()
}

const handleSortChange = ({ prop, order }: { prop: string; order: 'ascending' | 'descending' | null }) => {
  sortParams.sortBy = prop
  sortParams.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  loadOrders()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadOrders()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadOrders()
}

const refreshTable = () => {
  loadOrders()
  loadOrderStats()
}

const handleCreateOrder = () => {
  router.push('/orders/create')
}

const handleExport = async () => {
  try {
    loading.value = true
    const params = buildFilterParams()
    await orderStore.exportOrders(params)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

const handleViewDetail = (id: number) => {
  router.push(`/orders/${id}`)
}

const handleMoreAction = async (command: string, row: Order) => {
  switch (command) {
    case 'confirm':
      handleConfirmOrder(row.id)
      break
    case 'complete':
      handleCompleteOrder(row.id)
      break
    case 'cancel':
      handleCancelOrder(row.id)
      break
    case 'refund':
      handleRefundOrder(row.id)
      break
    case 'delete':
      handleDeleteOrder(row.id)
      break
  }
}

const handleConfirmOrder = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认处理该订单吗？', '确认订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await orderStore.confirmOrder(id)
    ElMessage.success('订单已确认')
    loadOrders()
  } catch (error) {
    console.error('确认订单失败:', error)
    ElMessage.error('确认订单失败')
  }
}

const handleCompleteOrder = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认完成该订单吗？', '完成订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await orderStore.completeOrder(id)
    ElMessage.success('订单已完成')
    loadOrders()
  } catch (error) {
    console.error('完成订单失败:', error)
    ElMessage.error('完成订单失败')
  }
}

const handleCancelOrder = async (id: number) => {
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
    
    await orderStore.cancelOrder(id, value)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    console.error('取消订单失败:', error)
  }
}

const handleRefundOrder = async (id: number) => {
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
    
    await orderStore.refundOrder(id, value)
    ElMessage.success('退款申请已提交')
    loadOrders()
  } catch (error) {
    console.error('退款失败:', error)
  }
}

const handleDeleteOrder = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该订单吗？此操作不可恢复。', '删除订单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    
    await orderStore.deleteOrder(id)
    ElMessage.success('删除成功')
    loadOrders()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

const getOrderTypeType = (type: OrderType) => {
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

const getOrderTypeText = (type: OrderType) => {
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

const getPaymentStatusType = (status: PaymentStatus) => {
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

const getPaymentStatusText = (status: PaymentStatus) => {
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

const getOrderStatusType = (status: OrderStatus) => {
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

const getOrderStatusText = (status: OrderStatus) => {
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

const getPaymentMethodIcon = (method: PaymentMethod) => {
  const icons: Record<string, string> = {
    WECHAT: 'i-ep-wechat',
    ALIPAY: 'i-ep-alipay',
    CASH: 'i-ep-money',
    CARD: 'i-ep-credit-card',
    BANK_TRANSFER: 'i-ep-bank-card'
  }
  return icons[method] || 'i-ep-money'
}

const getPaymentMethodText = (method: PaymentMethod) => {
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
  loadOrders()
  loadOrderStats()
})
</script>

<style lang="scss" scoped>
.order-list-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .header-left {
      .page-title {
        font-size: 24px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
        margin: 0 0 8px;
      }
      
      .el-breadcrumb {
        font-size: 14px;
        color: var(--gymflow-text-secondary);
      }
    }
  }
  
  .filter-card {
    margin-bottom: 20px;
    border-radius: 12px;
    
    :deep(.el-card__body) {
      padding: 20px;
    }
    
    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }
  
  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 16px;
    margin-bottom: 24px;
    
    .stats-card {
      border-radius: 12px;
      border: 1px solid var(--gymflow-border);
      
      :deep(.el-card__body) {
        padding: 20px;
      }
      
      .stats-content {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .stats-icon {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 48px;
          height: 48px;
          border-radius: 12px;
          font-size: 24px;
          
          &.total {
            background: rgba(64, 158, 255, 0.1);
            color: #409eff;
          }
          
          &.revenue {
            background: rgba(103, 194, 58, 0.1);
            color: #67c23a;
          }
          
          &.pending {
            background: rgba(230, 162, 60, 0.1);
            color: #e6a23c;
          }
          
          &.today {
            background: rgba(155, 89, 182, 0.1);
            color: #9b59b6;
          }
        }
        
        .stats-info {
          .stats-number {
            font-size: 24px;
            font-weight: 700;
            color: var(--gymflow-text-primary);
            margin-bottom: 4px;
            line-height: 1;
          }
          
          .stats-label {
            font-size: 12px;
            color: var(--gymflow-text-secondary);
          }
        }
      }
    }
  }
  
  .table-card {
    border-radius: 12px;
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid var(--gymflow-border);
    }
    
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .table-title {
        font-size: 16px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
      }
    }
    
    .member-cell {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .member-avatar {
        background: var(--gymflow-primary);
        color: white;
      }
      
      .member-name {
        font-weight: 500;
      }
    }
    
    .amount-cell {
      .total-amount {
        font-size: 14px;
        color: var(--gymflow-text-secondary);
        text-decoration: line-through;
        margin-bottom: 2px;
      }
      
      .discount-amount {
        font-size: 12px;
        color: var(--el-color-danger);
        margin-bottom: 2px;
      }
      
      .payment-amount {
        font-size: 16px;
        font-weight: 600;
        
        .pay-amount {
          color: var(--gymflow-primary);
        }
      }
    }
    
    .items-cell {
      .item-tag {
        display: inline-block;
        font-size: 12px;
        background: var(--gymflow-bg);
        color: var(--gymflow-text-secondary);
        padding: 2px 8px;
        border-radius: 4px;
        margin: 2px 4px 2px 0;
      }
      
      .more-items {
        font-size: 12px;
        color: var(--gymflow-text-secondary);
        margin-top: 4px;
      }
    }
    
    .payment-method {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .el-icon {
        font-size: 16px;
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
  .order-list-container {
    padding: 16px;
    
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    
    .filter-card {
      :deep(.el-form) {
        display: flex;
        flex-direction: column;
        gap: 16px;
      }
      
      :deep(.el-form-item) {
        width: 100%;
        margin-right: 0;
        
        .el-input,
        .el-select,
        .el-date-editor {
          width: 100%;
        }
      }
    }
    
    .stats-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}
</style>