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
            <el-descriptions-item label="销量">{{ productDetail?.salesVolume || 0 }}</el-descriptions-item>
            <el-descriptions-item label="原价">
              <span class="amount">¥{{ formatAmount(productDetail?.originalPrice) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="现价">
              <span class="amount current">¥{{ formatAmount(productDetail?.currentPrice) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="库存数量">{{ productDetail?.stockQuantity || 0 }}</el-descriptions-item>
            <el-descriptions-item label="最大购买数量">{{ productDetail?.maxPurchaseQuantity || 0 }}</el-descriptions-item>
            <el-descriptions-item v-if="productDetail?.productType == 1 || productDetail?.productType == 2" label="总节数">{{ productDetail?.totalSessions }}节</el-descriptions-item>
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

      <!-- 会籍权益 -->
      <div class="spec-section" v-if="productDetail?.membershipBenefits && productDetail?.membershipBenefits.length > 0">
        <h3 class="section-title">会籍权益</h3>
        <div class="spec-content" v-for="(benefit, index) in productDetail.membershipBenefits" :key="index">
          {{ benefit }}
        </div>
      </div>

      <div class="spec-section" v-if="productDetail?.usageRules">
        <h3 class="section-title">使用规则</h3>
        <div class="spec-content">
          {{ productDetail.usageRules }}
        </div>
      </div>

      <!-- 销售记录 -->
      <div class="spec-section">
        <h3 class="section-title">销售记录</h3>
        <div v-if="paginatedSalesRecords.length > 0" v-loading="loading">
          <el-table :data="paginatedSalesRecords" style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="memberName" label="购买会员" width="120" />
            <el-table-column prop="memberPhone" label="会员手机号" width="120" />
            <el-table-column prop="quantity" label="购买数量" width="100" align="center" />
            <el-table-column prop="unitPrice" label="单价" width="100" align="right">
              <template #default="{ row }">
                ¥{{ row.unitPrice?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="totalPrice" label="总金额" width="100" align="right">
              <template #default="{ row }">
                ¥{{ row.totalPrice?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="paymentMethod" label="支付方式" width="120">
              <template #default="{ row }">
                {{ getPaymentMethodDesc(row.paymentMethod) }}
              </template>
            </el-table-column>
            <el-table-column label="订单状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getOrderStatusTagType(row.orderStatus)" size="small">
                  {{ getOrderStatusDesc(row.orderStatus) }}
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
            <el-pagination 
              v-model:current-page="salesPageNum" 
              v-model:page-size="salesPageSize" 
              :total="salesTotal" 
              :page-sizes="[5, 10, 20, 50]" 
              layout="total, sizes, prev, pager, next, jumper" 
              @size-change="handleSalesSizeChange" 
              @current-change="handleSalesPageChange" 
            />
          </div>
        </div>
        <div v-else class="empty-data">
          <el-empty description="暂无销售记录" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { useProductStore } from '@/stores/product'

const router = useRouter()
const route = useRoute()
const productStore = useProductStore()

const loading = ref(false)

// 销售记录分页
const salesPageNum = ref(1)
const salesPageSize = ref(10)

const productId = computed(() => Number(route.params.id))
const productDetail = computed(() => productStore.currentProduct)

// 获取销售记录数据
const allSalesRecords = computed(() => productDetail.value?.salesRecords || [])

// 销售记录总数
const salesTotal = computed(() => allSalesRecords.value.length)

// 分页后的销售记录
const paginatedSalesRecords = computed(() => {
  const start = (salesPageNum.value - 1) * salesPageSize.value
  const end = start + salesPageSize.value
  return allSalesRecords.value.slice(start, end)
})

const formatDate = (date: string | null | undefined) => date || '-'
const formatDateTime = (datetime: string | null | undefined) => datetime?.replace('T', ' ') || '-'
const formatAmount = (amount: number | null | undefined) => amount ? amount.toFixed(2) : '0.00'

// 支付方式描述
const getPaymentMethodDesc = (method?: string | null) => {
  if (!method) return '-'
  const map: Record<string, string> = {
    '微信支付': '微信支付',
    '支付宝': '支付宝',
    '银行卡': '银行卡',
    '现金': '现金',
    '前台支付': '前台支付'
  }
  return map[method] || method
}

// 订单状态描述
const getOrderStatusDesc = (status?: string) => {
  if (!status) return '-'
  const map: Record<string, string> = {
    WAIT_PAY: '待支付',
    PAID: '已支付',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REFUNDED: '已退款'
  }
  return map[status] || status
}

// 订单状态标签类型
const getOrderStatusTagType = (status?: string) => {
  if (!status) return 'info'
  switch (status) {
    case 'WAIT_PAY': return 'warning'
    case 'PAID': return 'primary'
    case 'COMPLETED': return 'success'
    case 'CANCELLED': return 'info'
    case 'REFUNDED': return 'danger'
    default: return 'info'
  }
}

const loadProductDetail = async () => {
  try {
    loading.value = true
    await productStore.fetchProductDetail(productId.value)
  } catch (error) {
    ElMessage.error('加载商品详情失败')
  } finally {
    loading.value = false
  }
}

const handleViewOrderDetail = (orderId: number) => {
  router.push(`/order/detail/${orderId}`)
}

// 分页事件
const handleSalesSizeChange = (size: number) => {
  salesPageSize.value = size
  salesPageNum.value = 1
}

const handleSalesPageChange = (page: number) => {
  salesPageNum.value = page
}

const goBack = () => router.push('/product/list')
const handleEdit = () => router.push(`/product/edit/${productId.value}`)

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