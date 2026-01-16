<template>
  <div class="order-form-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/order' }">订单管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ formData.id ? '编辑订单' : '新增订单' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">{{ formData.id ? '编辑订单信息' : '新增订单' }}</h1>
      </div>
      <div class="header-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="loading">
          {{ formData.id ? '更新' : '创建' }}
        </el-button>
      </div>
    </div>

    <!-- 主表单区域 -->
    <div class="form-content">
      <el-form 
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="order-form"
      >
        <!-- 订单基本信息 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">订单基本信息</span>
            </div>
          </template>
          
          <div class="form-row">
            <el-form-item label="订单编号" prop="orderNo" class="form-item">
              <el-input 
                v-model="formData.orderNo"
                placeholder="自动生成"
                :disabled="true"
              />
            </el-form-item>
            
            <el-form-item label="订单类型" prop="type" class="form-item">
              <el-select v-model="formData.type" placeholder="请选择订单类型" style="width: 100%">
                <el-option 
                  v-for="item in orderTypeOptions" 
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="会员" prop="memberId" class="form-item">
              <el-select 
                v-model="formData.memberId"
                filterable
                placeholder="请选择会员"
                style="width: 100%"
                @change="handleMemberChange"
              >
                <el-option 
                  v-for="member in memberList"
                  :key="member.id"
                  :label="`${member.realName} (${member.memberNo})`"
                  :value="member.id"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="会员姓名" class="form-item">
              <el-input 
                v-model="formData.memberName"
                placeholder="选择会员后自动填充"
                :disabled="true"
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="订单状态" prop="orderStatus" class="form-item">
              <el-select v-model="formData.orderStatus" placeholder="请选择订单状态" style="width: 100%">
                <el-option 
                  v-for="item in orderStatusOptions" 
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="支付状态" prop="paymentStatus" class="form-item">
              <el-select v-model="formData.paymentStatus" placeholder="请选择支付状态" style="width: 100%">
                <el-option 
                  v-for="item in paymentStatusOptions" 
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
        </el-card>

        <!-- 订单商品 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">订单商品</span>
              <el-button type="primary" text @click="handleAddItem">
                <el-icon><Plus /></el-icon>
                添加商品
              </el-button>
            </div>
          </template>
          
          <div v-if="formData.items.length > 0" class="order-items-table">
            <div class="table-header">
              <div class="header-cell" style="width: 30%">商品名称</div>
              <div class="header-cell" style="width: 15%">类型</div>
              <div class="header-cell" style="width: 10%">数量</div>
              <div class="header-cell" style="width: 15%">单价(元)</div>
              <div class="header-cell" style="width: 15%">总价(元)</div>
              <div class="header-cell" style="width: 15%">操作</div>
            </div>
            
            <div class="table-body">
              <div v-for="(item, index) in formData.items" :key="index" class="table-row">
                <div class="table-cell" style="width: 30%">
                  <el-select
                    v-model="item.itemName"
                    filterable
                    allow-create
                    placeholder="选择或输入商品名称"
                    style="width: 100%"
                    @change="handleItemChange(index, $event)"
                  >
                    <el-option
                      v-for="product in availableProducts"
                      :key="product.id"
                      :label="product.name"
                      :value="product.name"
                    />
                  </el-select>
                </div>
                
                <div class="table-cell" style="width: 15%">
                  <el-select
                    v-model="item.itemType"
                    placeholder="选择类型"
                    style="width: 100%"
                  >
                    <el-option 
                      v-for="itemType in itemTypeOptions" 
                      :key="itemType.value"
                      :label="itemType.label"
                      :value="itemType.value"
                    />
                  </el-select>
                </div>
                
                <div class="table-cell" style="width: 10%">
                  <el-input-number
                    v-model="item.quantity"
                    :min="1"
                    :max="100"
                    :step="1"
                    controls-position="right"
                    style="width: 100%"
                    @change="calculateItemTotal(index)"
                  />
                </div>
                
                <div class="table-cell" style="width: 15%">
                  <el-input-number
                    v-model="item.unitPrice"
                    :min="0"
                    :max="10000"
                    :step="10"
                    controls-position="right"
                    style="width: 100%"
                    @change="calculateItemTotal(index)"
                  />
                </div>
                
                <div class="table-cell" style="width: 15%">
                  <el-input
                    v-model="item.totalPrice"
                    :disabled="true"
                    placeholder="自动计算"
                  />
                </div>
                
                <div class="table-cell" style="width: 15%">
                  <el-button type="danger" text @click="handleRemoveItem(index)">
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          
          <div v-else class="no-items">
            <el-empty description="暂无商品" :image-size="80">
              <el-button type="primary" @click="handleAddItem">添加商品</el-button>
            </el-empty>
          </div>
        </el-card>

        <!-- 价格与支付 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">价格与支付</span>
            </div>
          </template>
          
          <div class="price-summary">
            <div class="price-row">
              <span class="price-label">商品总价：</span>
              <span class="price-value">{{ formatCurrency(totalAmount) }}</span>
            </div>
            
            <div class="price-row">
              <span class="price-label">优惠金额：</span>
              <div class="discount-input">
                <el-input-number
                  v-model="formData.discountAmount"
                  :min="0"
                  :max="totalAmount"
                  :step="10"
                  controls-position="right"
                  style="width: 200px"
                  @change="calculatePaymentAmount"
                />
                <span class="form-tip">最大可优惠: {{ formatCurrency(totalAmount) }}</span>
              </div>
            </div>
            
            <div class="price-row">
              <span class="price-label">应付金额：</span>
              <span class="price-value total-amount">{{ formatCurrency(formData.paymentAmount) }}</span>
            </div>
          </div>
          
          <div class="form-row">
            <el-form-item label="支付方式" prop="paymentMethod" class="form-item">
              <el-select v-model="formData.paymentMethod" placeholder="请选择支付方式" style="width: 100%">
                <el-option 
                  v-for="item in paymentMethodOptions" 
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="支付时间" prop="paidAt" class="form-item">
              <el-date-picker
                v-model="formData.paidAt"
                type="datetime"
                placeholder="选择支付时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="操作员" prop="operator" class="form-item">
              <el-input 
                v-model="formData.operator"
                placeholder="请输入操作员姓名"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="完成时间" prop="completedAt" class="form-item">
              <el-date-picker
                v-model="formData.completedAt"
                type="datetime"
                placeholder="选择完成时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          
          <el-form-item label="订单备注" prop="notes" class="full-width">
            <el-input 
              v-model="formData.notes"
              type="textarea"
              :rows="3"
              placeholder="请输入订单备注"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-card>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { 
  Order, 
  OrderItem, 
  OrderType, 
  OrderStatus, 
  PaymentMethod, 
  PaymentStatus,
  OrderItemType,
  Member
} from '@/types'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const loading = ref(false)

// 表单数据
const formData = reactive<Partial<Order> & {
  items: Partial<OrderItem>[]
  operator: string
}>({
  orderNo: '',
  memberId: undefined,
  memberName: '',
  type: OrderType.MEMBERSHIP,
  amount: 0,
  paymentAmount: 0,
  discountAmount: 0,
  paymentMethod: undefined,
  paymentStatus: PaymentStatus.PENDING,
  orderStatus: OrderStatus.PENDING,
  items: [],
  notes: '',
  paidAt: '',
  completedAt: '',
  operator: ''
})

// 会员列表
const memberList = ref<Member[]>([
  { id: 1, userId: 1, memberNo: 'M001', realName: '张三', gender: 1, status: 'ACTIVE', remainingSessions: 10, totalSessions: 30, membershipStartDate: '2023-01-01', membershipEndDate: '2024-01-01', createdAt: '', updatedAt: '' },
  { id: 2, userId: 2, memberNo: 'M002', realName: '李四', gender: 2, status: 'ACTIVE', remainingSessions: 5, totalSessions: 20, membershipStartDate: '2023-02-01', membershipEndDate: '2024-02-01', createdAt: '', updatedAt: '' },
  { id: 3, userId: 3, memberNo: 'M003', realName: '王五', gender: 1, status: 'ACTIVE', remainingSessions: 15, totalSessions: 50, membershipStartDate: '2023-03-01', membershipEndDate: '2024-03-01', createdAt: '', updatedAt: '' }
])

// 可用商品列表
const availableProducts = ref([
  { id: 1, name: '年卡会员', type: OrderItemType.MEMBERSHIP_CARD, price: 4999 },
  { id: 2, name: '季卡会员', type: OrderItemType.MEMBERSHIP_CARD, price: 1999 },
  { id: 3, name: '月卡会员', type: OrderItemType.MEMBERSHIP_CARD, price: 699 },
  { id: 4, name: '私教课10节', type: OrderItemType.PERSONAL_TRAINING_SESSION, price: 5000 },
  { id: 5, name: '私教课20节', type: OrderItemType.PERSONAL_TRAINING_SESSION, price: 9000 },
  { id: 6, name: '团体课10节', type: OrderItemType.COURSE_PACKAGE, price: 1500 },
  { id: 7, name: '蛋白粉1kg', type: OrderItemType.PRODUCT_ITEM, price: 299 },
  { id: 8, name: '运动毛巾', type: OrderItemType.PRODUCT_ITEM, price: 39 },
  { id: 9, name: '瑜伽垫', type: OrderItemType.PRODUCT_ITEM, price: 129 }
])

// 选项数据
const orderTypeOptions = [
  { label: '会员卡', value: OrderType.MEMBERSHIP },
  { label: '课程套餐', value: OrderType.COURSE_PACKAGE },
  { label: '私教课程', value: OrderType.PERSONAL_TRAINING },
  { label: '商品购买', value: OrderType.PRODUCT }
]

const orderStatusOptions = [
  { label: '待处理', value: OrderStatus.PENDING },
  { label: '处理中', value: OrderStatus.PROCESSING },
  { label: '已完成', value: OrderStatus.COMPLETED },
  { label: '已取消', value: OrderStatus.CANCELLED },
  { label: '已退款', value: OrderStatus.REFUNDED }
]

const paymentStatusOptions = [
  { label: '待支付', value: PaymentStatus.PENDING },
  { label: '已支付', value: PaymentStatus.PAID },
  { label: '支付失败', value: PaymentStatus.FAILED },
  { label: '已退款', value: PaymentStatus.REFUNDED }
]

const paymentMethodOptions = [
  { label: '微信支付', value: PaymentMethod.WECHAT },
  { label: '支付宝', value: PaymentMethod.ALIPAY },
  { label: '现金', value: PaymentMethod.CASH },
  { label: '刷卡', value: PaymentMethod.CARD },
  { label: '银行转账', value: PaymentMethod.BANK_TRANSFER }
]

const itemTypeOptions = [
  { label: '会员卡', value: OrderItemType.MEMBERSHIP_CARD },
  { label: '课程套餐', value: OrderItemType.COURSE_PACKAGE },
  { label: '私教课程', value: OrderItemType.PERSONAL_TRAINING_SESSION },
  { label: '商品', value: OrderItemType.PRODUCT_ITEM }
]

// 计算商品总价
const totalAmount = computed(() => {
  return formData.items.reduce((sum, item) => {
    return sum + (item.totalPrice || 0)
  }, 0)
})

// 计算应付金额
const calculatePaymentAmount = () => {
  formData.amount = totalAmount.value
  formData.paymentAmount = Math.max(0, totalAmount.value - (formData.discountAmount || 0))
}

// 计算单个商品总价
const calculateItemTotal = (index: number) => {
  const item = formData.items[index]
  if (item.quantity && item.unitPrice) {
    item.totalPrice = item.quantity * item.unitPrice
    calculatePaymentAmount()
  }
}

// 格式化货币
const formatCurrency = (value: number) => {
  return `¥${value.toFixed(2)}`
}

// 表单验证规则
const formRules = {
  memberId: [
    { required: true, message: '请选择会员', trigger: 'change' }
  ],
  type: [
    { required: true, message: '请选择订单类型', trigger: 'change' }
  ],
  orderStatus: [
    { required: true, message: '请选择订单状态', trigger: 'change' }
  ],
  paymentStatus: [
    { required: true, message: '请选择支付状态', trigger: 'change' }
  ]
}

// 会员选择变化
const handleMemberChange = (memberId: number) => {
  const selectedMember = memberList.value.find(member => member.id === memberId)
  if (selectedMember) {
    formData.memberName = selectedMember.realName
  }
}

// 商品选择变化
const handleItemChange = (index: number, itemName: string) => {
  const product = availableProducts.value.find(p => p.name === itemName)
  if (product) {
    const item = formData.items[index]
    item.itemName = product.name
    item.itemType = product.type
    item.unitPrice = product.price
    item.quantity = 1
    calculateItemTotal(index)
  }
}

// 添加商品项
const handleAddItem = () => {
  formData.items.push({
    itemName: '',
    itemType: OrderItemType.PRODUCT_ITEM,
    quantity: 1,
    unitPrice: 0,
    totalPrice: 0
  })
}

// 删除商品项
const handleRemoveItem = (index: number) => {
  formData.items.splice(index, 1)
  calculatePaymentAmount()
}

// 保存表单
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 验证商品列表
    if (formData.items.length === 0) {
      ElMessage.warning('请至少添加一个商品')
      return
    }
    
    const hasInvalidItem = formData.items.some(item => 
      !item.itemName || !item.itemType || !item.quantity || !item.unitPrice
    )
    
    if (hasInvalidItem) {
      ElMessage.warning('请完善所有商品信息')
      return
    }
    
    loading.value = true
    
    // 生成订单编号（如果是新增）
    if (!formData.id && !formData.orderNo) {
      formData.orderNo = `ORD${Date.now().toString().slice(-8)}`
    }
    
    // 这里调用API保存数据
    // await saveOrder(formData)
    
    ElMessage.success(formData.id ? '订单信息更新成功' : '订单创建成功')
    router.push('/order')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请检查表单')
  } finally {
    loading.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  router.push('/order')
}

// 初始化数据（编辑模式）
const initFormData = () => {
  const id = route.params.id
  if (id) {
    // 编辑模式：从API获取数据
    // const data = await getOrderDetail(id)
    // Object.assign(formData, data)
    
    // 模拟数据
    Object.assign(formData, {
      id: id,
      orderNo: 'ORD202300001',
      memberId: 1,
      memberName: '张三',
      type: OrderType.MEMBERSHIP,
      amount: 5998,
      paymentAmount: 5998,
      discountAmount: 0,
      paymentMethod: PaymentMethod.WECHAT,
      paymentStatus: PaymentStatus.PAID,
      orderStatus: OrderStatus.COMPLETED,
      items: [
        {
          itemName: '年卡会员',
          itemType: OrderItemType.MEMBERSHIP_CARD,
          quantity: 1,
          unitPrice: 4999,
          totalPrice: 4999
        },
        {
          itemName: '私教课10节',
          itemType: OrderItemType.PERSONAL_TRAINING_SESSION,
          quantity: 1,
          unitPrice: 5000,
          totalPrice: 5000
        }
      ],
      notes: '客户购买年卡会员+私教课程',
      paidAt: '2023-06-15 14:30:00',
      completedAt: '2023-06-15 14:35:00',
      operator: '张前台'
    })
    calculatePaymentAmount()
  } else {
    // 新增模式：生成订单编号
    formData.orderNo = `ORD${Date.now().toString().slice(-8)}`
    formData.operator = '系统管理员'
  }
}

onMounted(() => {
  initFormData()
})
</script>

<style scoped lang="scss">
.order-form-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  .header-left {
    .page-title {
      margin: 10px 0 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
  }

  .header-actions {
    display: flex;
    gap: 10px;
  }
}

.form-content {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.form-section {
  margin-bottom: 20px;
  border: 1px solid #ebeef5;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;

  .form-item {
    flex: 1;
    margin-bottom: 0;
  }
}

.full-width {
  width: 100%;
  margin-bottom: 20px;
}

.order-items-table {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.table-header {
  display: flex;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  
  .header-cell {
    padding: 12px;
    text-align: center;
    font-weight: 500;
    color: #606266;
  }
}

.table-body {
  .table-row {
    display: flex;
    align-items: center;
    border-bottom: 1px solid #ebeef5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .table-cell {
      padding: 10px;
      display: flex;
      align-items: center;
    }
  }
}

.no-items {
  padding: 40px 0;
  text-align: center;
}

.price-summary {
  background: #f9fafc;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
}

.price-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  .price-label {
    width: 120px;
    font-weight: 500;
    color: #606266;
  }
  
  .price-value {
    flex: 1;
    font-size: 16px;
    color: #303133;
    
    &.total-amount {
      font-size: 20px;
      font-weight: 600;
      color: #409eff;
    }
  }
  
  .discount-input {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 10px;
  }
}

.form-tip {
  font-size: 12px;
  color: #909399;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 20px;
}

:deep(.el-input-number),
:deep(.el-date-editor) {
  width: 100%;
}
</style>