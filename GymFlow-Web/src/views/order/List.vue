<template>
  <div class="order-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">订单管理</h1>
      </div>
      <!-- <div class="header-right">
        <el-button type="primary" @click="handleCreateOrder">
          <el-icon><Plus /></el-icon>
          新建订单
        </el-button>
      </div> -->
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <!-- <el-form-item label="订单编号">
          <el-input
            v-model="filterForm.orderNo"
            placeholder="请输入订单编号"
            clearable
            style="width: 180px;"
          />
        </el-form-item> -->
        <el-form-item label="会员姓名">
          <el-input v-model="filterForm.memberName" placeholder="请输入会员姓名" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="filterForm.memberPhone" placeholder="请输入手机号" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="订单类型">
          <el-select v-model="filterForm.orderType" placeholder="请选择订单类型" clearable style="width: 180px;">
            <el-option label="会籍卡" :value="0" />
            <el-option label="私教课" :value="1" />
            <el-option label="团课" :value="2" />
            <el-option label="相关产品" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="filterForm.paymentStatus" placeholder="请选择支付状态" clearable style="width: 180px;">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="filterForm.orderStatus" placeholder="请选择订单状态" clearable style="width: 180px;">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker v-model="filterForm.dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon>
              <Search />
            </el-icon>
            查询
          </el-button>
          <el-button @click="handleReset" :disabled="loading">
            <el-icon>
              <Refresh />
            </el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="table-title">订单列表</span>
          <div class="table-actions">
            <el-button text @click="refreshTable" :loading="loading">
              <el-icon>
                <Refresh />
              </el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="formattedOrders" style="width: 100%" row-key="id" v-loading="loading" stripe border>
        <el-table-column prop="orderNo" label="订单编号" width="180" fixed="left" />
        <el-table-column label="会员信息" width="200">
          <template #default="{ row }">
            <div class="member-info">
              <div class="member-name">{{ row.memberName || '-' }}</div>
              <div class="member-phone">{{ row.memberPhone || '-' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="orderTypeDesc" label="订单类型" width="100" />
        <el-table-column prop="itemCount" label="商品数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.itemCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmountFormatted" label="总金额" width="120" align="right" />
        <el-table-column prop="actualAmountFormatted" label="实付金额" width="120" align="right">
          <template #default="{ row }">
            <span class="actual-amount">{{ row.actualAmountFormatted }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentStatusDesc" label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.paymentStatus === 1 ? 'success' : 'warning'" size="small">
              {{ row.paymentStatusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="{ row }">
            {{ row.paymentMethod || '现金' }}
          </template>
        </el-table-column>
        <el-table-column prop="orderStatusDesc" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.orderStatus)" size="small">
              {{ row.orderStatusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTimeFormatted" label="创建时间" width="180" />
        <el-table-column prop="paymentTimeFormatted" label="支付时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row.id)">
              详情
            </el-button>
            <el-button v-permission="'order:edit'" type="warning" link size="small" v-if="row.orderStatus === 'PENDING'" @click="handleEdit(row.id)">
              编辑
            </el-button>
            <!-- <el-button 
              type="success" 
              link 
              size="small" 
              v-if="row.paymentStatus === 0 && row.orderStatus === 'PENDING'" v-permission="'order:pay'"
              @click="handlePay(row.id)"
            >
              支付
            </el-button> -->
            <el-popconfirm title="确定要删除这个订单吗？" @confirm="handleDelete(row.id)" confirm-button-text="确定" cancel-button-text="取消" v-if="row.orderStatus === 'CANCELLED' || row.orderStatus === 'COMPLETED'">
              <template #reference>
                <el-button v-permission="'order:delete'" type="danger" link size="small">
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div class="batch-actions" v-if="selectedRows.length > 0">
        <el-button type="danger" size="small" @click="handleBatchDelete">
          <el-icon>
            <Delete />
          </el-icon>
          批量删除
        </el-button>
        <span class="selected-count">已选择 {{ selectedRows.length }} 项</span>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pageInfo.pageNum" v-model:page-size="pageInfo.pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" :disabled="loading" />
      </div>
    </el-card>

    <!-- 支付对话框 -->
    <!-- <PaymentDialog
      v-model="paymentDialogVisible"
      :order-id="currentOrderId"
      @success="handlePaymentSuccess"
    /> -->
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useOrderStore } from '@/stores/order'
// import PaymentDialog from './components/PaymentDialog.vue'
import type { OrderQueryParams } from '@/types/order'
import { usePermission } from '@/composables/usePermission'

const { hasPermission } = usePermission()

const router = useRouter()
const orderStore = useOrderStore()

// 筛选表单
const filterForm = reactive({
  orderNo: '',
  memberName: '',
  memberPhone: '',
  orderType: undefined as number | undefined,
  paymentStatus: undefined as number | undefined,
  orderStatus: '' as string,
  dateRange: [] as string[],
})

// 选择的行
const selectedRows = ref<any[]>([])

// 对话框
const paymentDialogVisible = ref(false)
const currentOrderId = ref<number | null>(null)

// 获取store状态
const { orderList, total, loading, pageInfo, formattedOrderList } = orderStore

// 格式化后的订单列表
const formattedOrders = computed(() => formattedOrderList())

// 获取状态标签类型
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

// 加载数据
const loadData = async () => {
  const params: OrderQueryParams = {
    pageNum: pageInfo.pageNum,
    pageSize: pageInfo.pageSize,
    orderNo: filterForm.orderNo,
    orderType: filterForm.orderType,
    paymentStatus: filterForm.paymentStatus,
    orderStatus: filterForm.orderStatus,
  }

  // 处理日期范围
  if (filterForm.dateRange?.length === 2) {
    params.startDate = filterForm.dateRange[0]
    params.endDate = filterForm.dateRange[1]
  }

  // 处理会员信息查询（这里需要根据实际情况调整）
  // 如果API不支持按会员姓名和手机号直接查询，需要先查询会员ID
  if (filterForm.memberName || filterForm.memberPhone) {
    // 这里可以调用会员API查询符合条件的会员ID
    // 暂时先不处理，实际项目中需要根据业务需求实现
  }

  await orderStore.fetchOrderList(params)
}

// 搜索
const handleSearch = () => {
  pageInfo.pageNum = 1
  loadData()
}

// 重置
const handleReset = () => {
  filterForm.orderNo = ''
  filterForm.memberName = ''
  filterForm.memberPhone = ''
  filterForm.orderType = undefined
  filterForm.paymentStatus = undefined
  filterForm.orderStatus = ''
  filterForm.dateRange = []
  pageInfo.pageNum = 1
  loadData()
}

// 创建订单
const handleCreateOrder = () => {
  router.push('/order/create')
}

// 查看详情
const handleViewDetail = (id: number) => {
  router.push(`/order/detail/${id}`)
}

// 编辑订单
const handleEdit = (id: number) => {
  router.push(`/order/edit/${id}`)
}

// 支付订单
const handlePay = (id: number) => {
  currentOrderId.value = id
  paymentDialogVisible.value = true
}

// 删除订单
const handleDelete = async (id: number) => {
  try {
    await orderStore.deleteOrder(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的订单')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 个订单吗？`, '警告', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })

    const ids = selectedRows.value.map((row) => row.id)
    await orderStore.batchDeleteOrder(ids)
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadData()
  } catch (error) {
    // 用户取消
  }
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pageInfo.pageSize = size
  pageInfo.pageNum = 1
  loadData()
}

// 页码变化
const handleCurrentChange = (current: number) => {
  pageInfo.pageNum = current
  loadData()
}

// 刷新表格
const refreshTable = async () => {
  await loadData()
  ElMessage.success('刷新成功')
}

// 支付成功回调
const handlePaymentSuccess = () => {
  paymentDialogVisible.value = false
  loadData()
}

// 初始化加载
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.order-list-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
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

.actual-amount {
  font-weight: 600;
  color: #67c23a;
}

.batch-actions {
  margin: 16px 0;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 12px;

  .selected-count {
    color: #606266;
    font-size: 14px;
  }
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-card__header) {
  padding: 16px 20px;
}

:deep(.el-table__header) {
  background-color: #f8f9fa;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>