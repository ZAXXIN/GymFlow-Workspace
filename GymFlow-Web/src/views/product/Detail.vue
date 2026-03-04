<!-- src/views/product/Detail.vue -->
<template>
  <div class="product-detail-container">
    <!-- 页面头部 -->
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <div class="header-content">
          <span class="page-title">商品详情</span>
        </div>
      </template>
    </el-page-header>

    <!-- 基本信息卡片 -->
    <el-card class="info-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">基本信息</span>
          <div class="card-actions">
            <el-tag :type="productDetail?.status == 1 ? 'success' : 'danger'" size="large">
              {{ productDetail?.status == 1 ? '在售' : '下架' }}
            </el-tag>
          </div>
        </div>
      </template>

      <div class="basic-info">
        <div class="info-details">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="商品名称">{{ productDetail?.productName }}</el-descriptions-item>
            <el-descriptions-item label="商品类型">{{ productDetail?.productTypeDesc }}</el-descriptions-item>
            <!-- <el-descriptions-item label="原价">
              <span class="amount">¥{{ formatAmount(productDetail?.originalPrice) }}</span>
            </el-descriptions-item> -->
            <el-descriptions-item label="现价">
              <span class="amount current">¥{{ formatAmount(productDetail?.currentPrice) }}</span>
            </el-descriptions-item>
            <!-- <el-descriptions-item label="成本价">
              <span>¥{{ formatAmount(productDetail?.costPrice) }}</span>
            </el-descriptions-item> -->
            <el-descriptions-item label="库存数量">{{ productDetail?.stockQuantity || 0 }}</el-descriptions-item>
            <el-descriptions-item label="销量">{{ productDetail?.salesVolume || 0 }}</el-descriptions-item>
            <!-- <el-descriptions-item label="单位">{{ productDetail?.unit || '-' }}</el-descriptions-item> -->
            <el-descriptions-item label="商品分类">{{ productDetail?.categoryName || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="productDetail.productType == 0" label="有效期">{{ productDetail?.validityDays || '-' }}天</el-descriptions-item>
            <!-- <el-descriptions-item label="创建时间">{{ formatDateTime(productDetail?.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(productDetail?.updateTime) }}</el-descriptions-item> -->
          </el-descriptions>
        </div>
      </div>

      <!-- 商品图片 -->
      <div class="image-section" v-if="productDetail?.images && productDetail.images.length > 0">
        <h3 class="section-title">商品图片</h3>
        <div class="image-list">
          <div v-for="(image, index) in productDetail.images" :key="index" class="image-item">
            <el-image :src="image" fit="cover" class="image-content" :preview-src-list="productDetail.images" :initial-index="index" hide-on-click-modal>
              <template #error>
                <div class="image-error">
                  <el-icon>
                    <Picture />
                  </el-icon>
                  <span>加载失败</span>
                </div>
              </template>
            </el-image>
          </div>
        </div>
      </div>

      <!-- 商品描述 -->
      <div class="description-section" v-if="productDetail?.description">
        <h3 class="section-title">商品描述</h3>
        <div class="description-content">
          {{ productDetail.description }}
        </div>
      </div>

      <!-- 规格信息 -->
      <div class="spec-section" v-if="productDetail?.specifications">
        <h3 class="section-title">规格信息</h3>
        <div class="spec-content">
          {{ productDetail.specifications }}
        </div>
      </div>
    </el-card>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <!-- 商品详情 -->
      <el-tab-pane label="商品详情" name="details">
        <el-card shadow="never" class="tab-content">
          <template #header>
            <div class="tab-header">
              <span class="tab-title">商品详情</span>
            </div>
          </template>

          <div v-if="productDetail?.detailDTO" class="detail-content">
            <!-- 会籍卡详情 -->
            <template v-if="productDetail.productType === 0">
              <div class="info-section">
                <div class="info-title">会籍类型</div>
                <div class="info-value">{{ getMembershipTypeDesc(productDetail.detailDTO.membershipType) }}</div>
              </div>

              <div v-if="productDetail.detailDTO.validityDays" class="info-section">
                <div class="info-title">有效期</div>
                <div class="info-value">{{ productDetail.detailDTO.validityDays }}天</div>
              </div>

              <div v-if="productDetail.detailDTO.maxPurchaseQuantity" class="info-section">
                <div class="info-title">最大购买数量</div>
                <div class="info-value">{{ productDetail.detailDTO.maxPurchaseQuantity }}</div>
              </div>

              <!-- 会籍权益 -->
              <div v-if="productDetail.detailDTO.membershipBenefits && productDetail.detailDTO.membershipBenefits.length > 0" class="info-section full-width">
                <div class="info-title">会籍权益</div>
                <div class="benefits-list">
                  <div v-for="(benefit, index) in productDetail.detailDTO.membershipBenefits" :key="index" class="benefit-item">
                    <el-icon>
                      <Check />
                    </el-icon>
                    <span>{{ benefit }}</span>
                  </div>
                </div>
              </div>
            </template>

            <!-- 私教课详情 -->
            <template v-if="productDetail.productType === 1">
              <div v-if="productDetail.detailDTO.courseDuration" class="info-section">
                <div class="info-title">课时长</div>
                <div class="info-value">{{ productDetail.detailDTO.courseDuration }}分钟</div>
              </div>

              <div v-if="productDetail.detailDTO.totalSessions" class="info-section">
                <div class="info-title">总节数</div>
                <div class="info-value">{{ productDetail.detailDTO.totalSessions }}节</div>
              </div>

              <div v-if="productDetail.detailDTO.availableSessions" class="info-section">
                <div class="info-title">可用节数</div>
                <div class="info-value">{{ productDetail.detailDTO.availableSessions }}节</div>
              </div>

              <div v-if="productDetail.detailDTO.courseLevel" class="info-section">
                <div class="info-title">课程级别</div>
                <div class="info-value">{{ productDetail.detailDTO.courseLevel }}</div>
              </div>

              <div v-if="productDetail.detailDTO.validityDays" class="info-section">
                <div class="info-title">有效期</div>
                <div class="info-value">{{ productDetail.detailDTO.validityDays }}天</div>
              </div>

              <!-- 适用教练 -->
              <div v-if="productDetail.detailDTO.coachIds && productDetail.detailDTO.coachIds.length > 0" class="info-section full-width">
                <div class="info-title">适用教练</div>
                <div class="coach-tags">
                  <el-tag v-for="coachId in productDetail.detailDTO.coachIds" :key="coachId" type="info" size="small">
                    教练 {{ coachId }}
                  </el-tag>
                </div>
              </div>
            </template>

            <!-- 团课详情 -->
            <template v-if="productDetail.productType === 2">
              <div v-if="productDetail.detailDTO.courseDuration" class="info-section">
                <div class="info-title">课时长</div>
                <div class="info-value">{{ productDetail.detailDTO.courseDuration }}分钟</div>
              </div>

              <div v-if="productDetail.detailDTO.totalSessions" class="info-section">
                <div class="info-title">总节数</div>
                <div class="info-value">{{ productDetail.detailDTO.totalSessions }}节</div>
              </div>

              <div v-if="productDetail.detailDTO.courseLevel" class="info-section">
                <div class="info-title">课程级别</div>
                <div class="info-value">{{ productDetail.detailDTO.courseLevel }}</div>
              </div>

              <div v-if="productDetail.detailDTO.validityDays" class="info-section">
                <div class="info-title">有效期</div>
                <div class="info-value">{{ productDetail.detailDTO.validityDays }}天</div>
              </div>
            </template>

            <!-- 所有类型通用 -->
            <div v-if="productDetail.detailDTO.maxPurchaseQuantity" class="info-section">
              <div class="info-title">最大购买数量</div>
              <div class="info-value">{{ productDetail.detailDTO.maxPurchaseQuantity }}</div>
            </div>

            <!-- 使用规则 -->
            <div v-if="productDetail.detailDTO.usageRules" class="info-section full-width">
              <div class="info-title">使用规则</div>
              <div class="rules-content">{{ productDetail.detailDTO.usageRules }}</div>
            </div>

            <!-- 退款政策 -->
            <div v-if="productDetail.detailDTO.refundPolicy" class="info-section full-width">
              <div class="info-title">退款政策</div>
              <div class="refund-content">{{ productDetail.detailDTO.refundPolicy }}</div>
            </div>
          </div>

          <div v-else class="empty-data">
            <el-empty description="暂无商品详情信息" />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 销售记录 -->
      <el-tab-pane label="销售记录" name="sales">
        <el-card shadow="never" class="tab-content">
          <template #header>
            <div class="tab-header">
              <span class="tab-title">销售记录</span>
              <div class="tab-actions">
                <el-date-picker v-model="salesFilter.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" size="small" style="width: 240px;" @change="handleDateRangeChange" />
              </div>
            </div>
          </template>

          <div v-if="salesRecords.length > 0">
            <el-table :data="salesRecords" style="width: 100%">
              <el-table-column prop="orderNo" label="订单号" width="180" />
              <el-table-column prop="memberName" label="购买会员" width="120" />
              <el-table-column prop="quantity" label="购买数量" width="100" align="center" />
              <el-table-column prop="unitPrice" label="单价" width="100" align="right">
                <template #default="{ row }">
                  ¥{{ row.unitPrice?.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="totalAmount" label="总金额" width="100" align="right">
                <template #default="{ row }">
                  ¥{{ row.totalAmount?.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="paymentMethod" label="支付方式" width="120">
                <template #default="{ row }">
                  {{ getPaymentMethodDesc(row.paymentMethod) }}
                </template>
              </el-table-column>
              <el-table-column prop="paymentStatus" label="支付状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getPaymentStatusType(row.paymentStatus)" size="small">
                    {{ getPaymentStatusDesc(row.paymentStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="购买时间" width="160">
                <template #default="{ row }">
                  {{ formatDateTime(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row }">
                  <el-button type="text" size="small" @click="handleViewOrderDetail(row.orderId)">
                    查看订单
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-container">
              <el-pagination v-model:current-page="salesPagination.pageNum" v-model:page-size="salesPagination.pageSize" :total="salesPagination.total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSalesSizeChange" @current-change="handleSalesPageChange" />
            </div>
          </div>

          <div v-else class="empty-data">
            <el-empty description="暂无销售记录" />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useProductStore } from '@/stores/product'
import type { ProductFullDTO } from '@/types/product'

const router = useRouter()
const route = useRoute()
const productStore = useProductStore()

const loading = ref(false)
const activeTab = ref('details')

// 销售记录相关
const salesRecords = ref<any[]>([])
const salesFilter = ref({
  dateRange: [] as string[],
  startDate: '',
  endDate: '',
})

const salesPagination = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0,
})

const productId = computed(() => Number(route.params.id))
const productDetail = computed(() => productStore.currentProduct)

// 格式化函数
const formatDate = (date: string | null | undefined) => {
  if (!date) return '-'
  return date
}

const formatDateTime = (datetime: string | null | undefined) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ')
}

const formatAmount = (amount: number | null | undefined) => {
  if (!amount) return '0.00'
  return amount.toFixed(2)
}

// 获取会籍类型描述
const getMembershipTypeDesc = (type?: number) => {
  if (type === undefined) return '-'
  const types = ['私教课', '团课', '月卡', '年卡', '其他']
  return types[type] || '-'
}

// 获取支付方式描述
const getPaymentMethodDesc = (method?: number) => {
  if (method === undefined) return '-'
  const methods = ['微信支付', '支付宝', '银行卡', '现金', '会员卡']
  return methods[method] || '-'
}

// 获取支付状态描述
const getPaymentStatusDesc = (status?: number) => {
  if (status === undefined) return '-'
  const statuses = ['待支付', '支付成功', '支付失败', '已退款']
  return statuses[status] || '-'
}

const getPaymentStatusType = (status?: number) => {
  if (status === 0) return 'warning' // 待支付
  if (status === 1) return 'success' // 支付成功
  if (status === 2) return 'danger' // 支付失败
  if (status === 3) return 'info' // 已退款
  return 'info'
}

// 加载商品详情
const loadProductDetail = async () => {
  try {
    loading.value = true
    await productStore.fetchProductDetail(productId.value)

    // 加载销售记录
    await loadSalesRecords()
  } catch (error) {
    console.error('加载商品详情失败:', error)
    ElMessage.error('加载商品详情失败')
  } finally {
    loading.value = false
  }
}

// 加载销售记录
const loadSalesRecords = async () => {
  try {
    // TODO: 这里需要调用API加载销售记录
    // 暂时使用模拟数据
    salesRecords.value = [
      {
        orderId: 10001,
        orderNo: 'DD202312150001',
        memberName: '张三',
        quantity: 1,
        unitPrice: 299.0,
        totalAmount: 299.0,
        paymentMethod: 0,
        paymentStatus: 1,
        createTime: '2023-12-15 14:30:00',
      },
      {
        orderId: 10002,
        orderNo: 'DD202312150002',
        memberName: '李四',
        quantity: 2,
        unitPrice: 299.0,
        totalAmount: 598.0,
        paymentMethod: 1,
        paymentStatus: 1,
        createTime: '2023-12-15 16:45:00',
      },
    ]
    salesPagination.value.total = 2
  } catch (error) {
    console.error('加载销售记录失败:', error)
  }
}

// 日期范围变化
const handleDateRangeChange = (dates: [string, string]) => {
  if (dates && dates.length === 2) {
    salesFilter.value.startDate = dates[0]
    salesFilter.value.endDate = dates[1]
  } else {
    salesFilter.value.startDate = ''
    salesFilter.value.endDate = ''
  }
  salesPagination.value.pageNum = 1
  loadSalesRecords()
}

// 分页大小变化
const handleSalesSizeChange = (size: number) => {
  salesPagination.value.pageSize = size
  salesPagination.value.pageNum = 1
  loadSalesRecords()
}

// 页码变化
const handleSalesPageChange = (page: number) => {
  salesPagination.value.pageNum = page
  loadSalesRecords()
}

// 查看订单详情
const handleViewOrderDetail = (orderId: number) => {
  router.push(`/order/detail/${orderId}`)
}

// 返回
const goBack = () => {
  router.push('/product/list')
}

// 编辑商品
const handleEdit = () => {
  router.push(`/product/edit/${productId.value}`)
}

onMounted(() => {
  loadProductDetail()
})
</script>

<style scoped>
.product-detail-container {
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

.info-card {
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

.basic-info {
  display: flex;
  gap: 40px;
  margin-bottom: 30px;
}

.info-details {
  flex: 1;
}

.image-section,
.description-section,
.spec-section {
  margin-top: 30px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-item {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.image-item:hover {
  transform: scale(1.05);
}

.image-content {
  width: 100%;
  height: 100%;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #909399;
}

.image-error .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.description-content,
.spec-content {
  line-height: 1.6;
  color: #606266;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.detail-tabs {
  margin-top: 20px;
}

.tab-content {
  margin-top: 0;
  border: none;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.tab-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-content {
  padding: 20px;
}

.info-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.info-section.full-width {
  display: block;
}

.info-title {
  width: 120px;
  font-weight: 500;
  color: #606266;
  flex-shrink: 0;
}

.info-value {
  color: #303133;
  flex: 1;
}

.amount {
  font-weight: 600;
  color: #67c23a;
}

.amount.current {
  color: #e6a23c;
}

.benefits-list {
  margin-top: 10px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  color: #606266;
}

.benefit-item .el-icon {
  color: #67c23a;
}

.coach-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.rules-content,
.refund-content {
  line-height: 1.6;
  color: #606266;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  margin-top: 10px;
}

.empty-data {
  padding: 40px 0;
  text-align: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

:deep(.el-card__header) {
  padding: 16px 20px;
}

:deep(.el-tabs__header) {
  background-color: white;
  padding: 0 20px;
  margin: 0;
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