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
        <el-form-item label="订单状态">
          <el-select v-model="filterForm.status" placeholder="请选择订单状态" clearable style="width: 180px;">
            <el-option label="待支付" value="WAIT_PAY" />
            <el-option label="已完成" value="COMPLETED" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已退款" value="REFUNDED" />
          </el-select>
        </el-form-item>
        <!-- <el-form-item label="创建时间">
          <el-date-picker v-model="filterForm.dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px;" />
        </el-form-item> -->
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
        <!-- <el-table-column prop="itemCount" label="商品数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.itemCount }}</el-tag>
          </template>
        </el-table-column> -->
        <el-table-column prop="totalAmountFormatted" label="总金额" width="120" align="right" />
        <el-table-column prop="actualAmountFormatted" label="实付金额" width="120" align="right">
          <template #default="{ row }">
            <span class="actual-amount">{{ row.actualAmountFormatted }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="100">
          <template #default="{ row }">
            {{ row.paymentMethod || '现金' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTimeFormatted" label="创建时间" width="180" />
        <el-table-column prop="paymentTimeFormatted" label="支付时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-permission="'order:detail'" type="primary" link size="small" @click="handleViewDetail(row.id)">
              详情
            </el-button>
            <!-- <el-button v-permission="'order:edit'" type="warning" link size="small" v-if="row.orderStatus === 'PENDING'" @click="handleEdit(row.id)">
              编辑
            </el-button> -->
            <!-- v-permission="'order:pay'" -->
            <el-popconfirm
              title="确认支付该订单吗？支付后将自动激活权益并完成订单。"
              @confirm="handlePay(row.id)"
              confirm-button-text="确定"
              cancel-button-text="取消"
            >
              <template #reference>
                <el-button v-if="row.status === 'WAIT_PAY'" type="danger" link size="small">
                  确认支付
                </el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm title="确定要删除这个订单吗？" @confirm="handleDelete(row.id)" confirm-button-text="确定" cancel-button-text="取消" v-if="row.status === 'CANCELLED'">
              <template #reference>
                <el-button v-permission="'order:delete'" type="danger" link size="small">
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pageInfo.pageNum" v-model:page-size="pageInfo.pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" :disabled="loading" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useOrderStore } from '@/stores/order'
import type { OrderQueryParams } from '@/types/order'
import { usePermission } from '@/directives/usePermission'

const { hasPermission } = usePermission()
const router = useRouter()
const orderStore = useOrderStore()

const filterForm = reactive({
  orderNo: '',
  memberName: '',
  memberPhone: '',
  orderType: undefined as number | undefined,
  status: '' as string,
  dateRange: [] as string[],
})

const { orderList, total, loading, pageInfo, formattedOrderList } = orderStore
const formattedOrders = computed(() => formattedOrderList())

const getStatusTagType = (status: string) => {
  switch (status) {
    case 'WAIT_PAY': return 'warning'
    case 'PAID': return 'primary'
    case 'COMPLETED': return 'success'
    case 'CANCELLED': return 'info'
    case 'REFUNDED': return 'danger'
    default: return 'info'
  }
}

const loadData = async () => {
  const params: OrderQueryParams = {
    pageNum: pageInfo.pageNum,
    pageSize: pageInfo.pageSize,
    orderNo: filterForm.orderNo,
    orderType: filterForm.orderType,
    status: filterForm.status || undefined,
  }
  if (filterForm.dateRange?.length === 2) {
    params.startDate = filterForm.dateRange[0]
    params.endDate = filterForm.dateRange[1]
  }
  await orderStore.fetchOrderList(params)
}

const handleSearch = () => {
  pageInfo.pageNum = 1
  loadData()
}

const handleReset = () => {
  filterForm.orderNo = ''
  filterForm.memberName = ''
  filterForm.memberPhone = ''
  filterForm.orderType = undefined
  filterForm.status = ''
  filterForm.dateRange = []
  pageInfo.pageNum = 1
  loadData()
}

const handleViewDetail = (id: number) => {
  router.push(`/order/detail/${id}`)
}

const handlePay = async (orderId: number) => {
  try {
    const activated = await orderStore.payOrder(orderId, '后台支付')
    if (activated) {
      ElMessage.success('支付成功，订单已完成')
      await loadData()
    } else {
      ElMessage.warning('支付成功，但权益激活失败，请稍后重试激活')
      await loadData()
    }
  } catch (error: any) {
    ElMessage.error(error.message || '支付失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await orderStore.deleteOrder(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleSizeChange = (size: number) => {
  pageInfo.pageSize = size
  pageInfo.pageNum = 1
  loadData()
}

const handleCurrentChange = (current: number) => {
  pageInfo.pageNum = current
  loadData()
}

const refreshTable = async () => {
  await loadData()
  ElMessage.success('刷新成功')
}

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