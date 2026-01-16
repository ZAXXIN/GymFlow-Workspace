<template>
  <div class="order-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      label-position="left"
    >
      <el-tabs v-model="activeTab">
        <!-- 订单信息 -->
        <el-tab-pane label="订单信息" name="order">
          <div class="form-section">
            <el-form-item label="订单编号" prop="orderNumber">
              <el-input
                v-model="formData.orderNumber"
                placeholder="系统自动生成"
                disabled
              />
            </el-form-item>
            
            <el-form-item label="订单类型" prop="orderType">
              <el-select
                v-model="formData.orderType"
                placeholder="请选择订单类型"
                clearable
                style="width: 100%"
              >
                <el-option label="会员卡购买" value="MEMBERSHIP" />
                <el-option label="课程购买" value="COURSE" />
                <el-option label="私教课购买" value="PRIVATE_TRAINING" />
                <el-option label{"value"="MERCHANDISE"} label="商品购买" />
                <el-option label{"value"="RENEWAL"} label="续费" />
                <el-option label{"value"="UPGRADE"} label="升级" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="订单状态" prop="status">
              <el-select
                v-model="formData.status"
                placeholder="请选择订单状态"
                clearable
                style="width: 100%"
              >
                <el-option label="待支付" value="PENDING" />
                <el-option label="已支付" value="PAID" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label{"value"="CANCELLED"} label="已取消" />
                <el-option label{"value"="REFUNDED"} label="已退款" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="下单时间" prop="orderTime">
              <el-date-picker
                v-model="formData.orderTime"
                type="datetime"
                placeholder="选择下单时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="支付时间" prop="paymentTime">
              <el-date-picker
                v-model="formData.paymentTime"
                type="datetime"
                placeholder="选择支付时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="完成时间" prop="completionTime">
              <el-date-picker
                v-model="formData.completionTime"
                type="datetime"
                placeholder="选择完成时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="下单会员" prop="memberId">
              <el-select
                v-model="formData.memberId"
                placeholder="请选择下单会员"
                clearable
                filterable
                remote
                :remote-method="searchMembers"
                :loading="searchingMembers"
                style="width: 100%"
              >
                <el-option
                  v-for="member in memberOptions"
                  :key="member.id"
                  :label="member.name"
                  :value="member.id"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="销售顾问" prop="salesConsultantId">
              <el-select
                v-model="formData.salesConsultantId"
                placeholder="请选择销售顾问"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="consultant in consultants"
                  :key="consultant.id"
                  :label="consultant.name"
                  :value="consultant.id"
                />
              </el-select>
            </el-form-item>
          </div>
        </el-tab-pane>
        
        <!-- 订单项 -->
        <el-tab-pane label="订单项" name="items">
          <div class="form-section">
            <div class="items-header">
              <h3>订单商品/服务</h3>
              <el-button type="primary" size="small" @click="addOrderItem">
                <el-icon><Plus /></el-icon>添加项目
              </el-button>
            </div>
            
            <div class="order-items">
              <el-table
                :data="formData.items"
                style="width: 100%"
                border
                stripe
              >
                <el-table-column label="序号" width="60" align="center">
                  <template #default="{ $index }">
                    {{ $index + 1 }}
                  </template>
                </el-table-column>
                
                <el-table-column label="商品/服务" prop="productName" min-width="200">
                  <template #default="{ row, $index }">
                    <el-select
                      v-model="row.productId"
                      placeholder="选择商品/服务"
                      clearable
                      filterable
                      style="width: 100%"
                      @change="handleProductChange(row, $index)"
                    >
                      <el-option-group
                        v-for="group in productOptions"
                        :key="group.label"
                        :label="group.label"
                      >
                        <el-option
                          v-for="product in group.options"
                          :key="product.id"
                          :label="product.name"
                          :value="product.id"
                          :disabled="isProductSelected(product.id)"
                        />
                      </el-option-group>
                    </el-select>
                  </template>
                </el-table-column>
                
                <el-table-column label="单价" prop="unitPrice" width="120" align="right">
                  <template #default="{ row }">
                    <el-input-number
                      v-model="row.unitPrice"
                      :min="0"
                      :step="10"
                      controls-position="right"
                      style="width: 100px"
                      @change="calculateItemTotal(row)"
                    >
                      <template #prefix>¥</template>
                    </el-input-number>
                  </template>
                </el-table-column>
                
                <el-table-column label="数量" prop="quantity" width="120" align="center">
                  <template #default="{ row }">
                    <el-input-number
                      v-model="row.quantity"
                      :min="1"
                      :max="999"
                      :step="1"
                      controls-position="right"
                      style="width: 80px"
                      @change="calculateItemTotal(row)"
                    />
                  </template>
                </el-table-column>
                
                <el-table-column label="折扣(%)" prop="discount" width="120" align="right">
                  <template #default="{ row }">
                    <el-input-number
                      v-model="row.discount"
                      :min="0"
                      :max="100"
                      :step="1"
                      controls-position="right"
                      style="width: 80px"
                      @change="calculateItemTotal(row)"
                    >
                      <template #suffix>%</template>
                    </el-input-number>
                  </template>
                </el-table-column>
                
                <el-table-column label="小计" prop="subtotal" width="120" align="right">
                  <template #default="{ row }">
                    <span class="subtotal">¥{{ row.subtotal.toFixed(2) }}</span>
                  </template>
                </el-table-column>
                
                <el-table-column label="操作" width="80" align="center" fixed="right">
                  <template #default="{ $index }">
                    <el-button
                      type="danger"
                      size="small"
                      link
                      @click="removeOrderItem($index)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <div class="order-summary">
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="summary-item">
                    <span class="label">商品总金额：</span>
                    <span class="value">¥{{ totalAmount.toFixed(2) }}</span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="summary-item">
                    <span class="label">订单折扣：</span>
                    <span class="value">
                      <el-input-number
                        v-model="formData.orderDiscount"
                        :min="0"
                        :max="100"
                        :step="1"
                        controls-position="right"
                        style="width: 120px"
                      >
                        <template #suffix>%</template>
                      </el-input-number>
                    </span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="summary-item">
                    <span class="label">运费：</span>
                    <span class="value">
                      <el-input-number
                        v-model="formData.shippingFee"
                        :min="0"
                        :step="10"
                        controls-position="right"
                        style="width: 120px"
                      >
                        <template #prefix>¥</template>
                      </el-input-number>
                    </span>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="summary-item">
                    <span class="label">实付金额：</span>
                    <span class="value total">¥{{ actualAmount.toFixed(2) }}</span>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 支付信息 -->
        <el-tab-pane label="支付信息" name="payment">
          <div class="form-section">
            <el-form-item label="支付方式" prop="paymentMethod">
              <el-select
                v-model="formData.paymentMethod"
                placeholder="请选择支付方式"
                clearable
                style="width: 100%"
              >
                <el-option label="微信支付" value="WECHAT" />
                <el-option label="支付宝" value="ALIPAY" />
                <el-option label="现金" value="CASH" />
                <el-option label="银行卡" value="BANK_CARD" />
                <el-option label="POS机" value="POS" />
                <el-option label{"value"="TRANSFER"} label="转账" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="支付状态" prop="paymentStatus">
              <el-select
                v-model="formData.paymentStatus"
                placeholder="请选择支付状态"
                clearable
                style="width: 100%"
              >
                <el-option label="未支付" value="UNPAID" />
                <el-option label="已支付" value="PAID" />
                <el-option label="支付中" value="PROCESSING" />
                <el-option label{"value"="FAILED"} label="支付失败" />
                <el-option label{"value"="REFUNDED"} label="已退款" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="支付金额" prop="paymentAmount">
              <el-input-number
                v-model="formData.paymentAmount"
                :min="0"
                :step="0.01"
                :precision="2"
                controls-position="right"
                placeholder="请输入支付金额"
                style="width: 100%"
              >
                <template #prefix>¥</template>
              </el-input-number>
            </el-form-item>
            
            <el-form-item label="交易流水号" prop="transactionNumber">
              <el-input
                v-model="formData.transactionNumber"
                placeholder="请输入交易流水号"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="收款账户" prop="paymentAccount">
              <el-input
                v-model="formData.paymentAccount"
                placeholder="请输入收款账户"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="支付备注" prop="paymentNotes">
              <el-input
                v-model="formData.paymentNotes"
                type="textarea"
                placeholder="请输入支付备注"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </div>
        </el-tab-pane>
        
        <!-- 配送信息 -->
        <el-tab-pane label="配送信息" name="delivery" v-if="showDeliveryTab">
          <div class="form-section">
            <el-form-item label="配送方式" prop="deliveryMethod">
              <el-select
                v-model="formData.deliveryMethod"
                placeholder="请选择配送方式"
                clearable
                style="width: 100%"
              >
                <el-option label="门店自提" value="STORE_PICKUP" />
                <el-option label="快递配送" value="EXPRESS" />
                <el-option label{"value"="COURIER"} label="跑腿配送" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="配送状态" prop="deliveryStatus">
              <el-select
                v-model="formData.deliveryStatus"
                placeholder="请选择配送状态"
                clearable
                style="width: 100%"
              >
                <el-option label="待发货" value="PENDING" />
                <el-option label="已发货" value="SHIPPED" />
                <el-option label{"value"="DELIVERED"} label="已送达" />
                <el-option label{"value"="CANCELLED"} label="已取消" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="收货人" prop="recipientName">
              <el-input
                v-model="formData.recipientName"
                placeholder="请输入收货人姓名"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="联系电话" prop="recipientPhone">
              <el-input
                v-model="formData.recipientPhone"
                placeholder="请输入联系电话"
                clearable
                maxlength="11"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="收货地址" prop="deliveryAddress">
              <el-input
                v-model="formData.deliveryAddress"
                type="textarea"
                placeholder="请输入收货地址"
                :rows="3"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="发货时间" prop="shippingTime">
              <el-date-picker
                v-model="formData.shippingTime"
                type="datetime"
                placeholder="选择发货时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="预计送达" prop="estimatedDelivery">
              <el-date-picker
                v-model="formData.estimatedDelivery"
                type="datetime"
                placeholder="选择预计送达时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="物流公司" prop="logisticsCompany">
              <el-input
                v-model="formData.logisticsCompany"
                placeholder="请输入物流公司"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="运单号" prop="trackingNumber">
              <el-input
                v-model="formData.trackingNumber"
                placeholder="请输入运单号"
                clearable
              />
            </el-form-item>
          </div>
        </el-tab-pane>
        
        <!-- 备注信息 -->
        <el-tab-pane label="备注信息" name="notes">
          <div class="form-section">
            <el-form-item label="客户备注" prop="customerNotes">
              <el-input
                v-model="formData.customerNotes"
                type="textarea"
                placeholder="请输入客户备注"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="内部备注" prop="internalNotes">
              <el-input
                v-model="formData.internalNotes"
                type="textarea"
                placeholder="请输入内部备注"
                :rows="4"
                maxlength="1000"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="发票信息" prop="invoiceInfo">
              <el-input
                v-model="formData.invoiceInfo"
                type="textarea"
                placeholder="请输入发票信息"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 表单操作 -->
      <div class="form-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { OrderFormData, OrderItem } from '@/types'

interface Props {
  formData: OrderFormData
  isEdit?: boolean
}

interface Emits {
  (e: 'submit', data: OrderFormData): void
  (e: 'cancel'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 表单引用
const formRef = ref<FormInstance>()

// 响应式数据
const loading = ref(false)
const activeTab = ref('order')
const searchingMembers = ref(false)
const memberOptions = ref<any[]>([])
const consultants = ref<any[]>([])
const productOptions = ref<any[]>([])

// 计算属性
const showDeliveryTab = computed(() => {
  const orderType = props.formData.orderType
  return orderType === 'MERCHANDISE'
})

const totalAmount = computed(() => {
  return props.formData.items.reduce((sum, item) => sum + item.subtotal, 0)
})

const actualAmount = computed(() => {
  const discountAmount = totalAmount.value * (props.formData.orderDiscount / 100)
  return totalAmount.value - discountAmount + props.formData.shippingFee
})

// 表单验证规则
const formRules = ref<FormRules>({
  orderType: [
    { required: true, message: '请选择订单类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择订单状态', trigger: 'change' }
  ],
  orderTime: [
    { required: true, message: '请选择下单时间', trigger: 'change' }
  ],
  memberId: [
    { required: true, message: '请选择下单会员', trigger: 'change' }
  ],
  paymentMethod: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ],
  paymentStatus: [
    { required: true, message: '请选择支付状态', trigger: 'change' }
  ],
  paymentAmount: [
    { required: true, message: '请输入支付金额', trigger: 'blur' },
    { type: 'number', min: 0, message: '支付金额不能为负数', trigger: 'blur' }
  ]
})

// 搜索会员
const searchMembers = async (query: string) => {
  if (!query) {
    memberOptions.value = []
    return
  }
  
  searchingMembers.value = true
  try {
    // 这里应该调用API搜索会员
    // 暂时使用模拟数据
    memberOptions.value = [
      { id: '1', name: '张三' },
      { id: '2', name: '李四' },
      { id: '3', name: '王五' }
    ].filter(member => member.name.includes(query))
  } catch (error) {
    console.error('搜索会员失败:', error)
  } finally {
    searchingMembers.value = false
  }
}

// 添加订单项
const addOrderItem = () => {
  const newItem: OrderItem = {
    id: Date.now().toString(),
    productId: '',
    productName: '',
    unitPrice: 0,
    quantity: 1,
    discount: 0,
    subtotal: 0
  }
  props.formData.items.push(newItem)
}

// 移除订单项
const removeOrderItem = (index: number) => {
  if (props.formData.items.length > 1) {
    props.formData.items.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一个订单项')
  }
}

// 检查商品是否已选择
const isProductSelected = (productId: string) => {
  return props.formData.items.some(item => item.productId === productId)
}

// 处理商品选择变化
const handleProductChange = (row: OrderItem, index: number) => {
  if (row.productId) {
    // 这里应该根据商品ID获取商品信息
    const product = getProductById(row.productId)
    if (product) {
      row.productName = product.name
      row.unitPrice = product.price
      calculateItemTotal(row)
    }
  } else {
    row.productName = ''
    row.unitPrice = 0
    row.subtotal = 0
  }
}

// 计算订单项小计
const calculateItemTotal = (row: OrderItem) => {
  const discountMultiplier = (100 - row.discount) / 100
  row.subtotal = row.unitPrice * row.quantity * discountMultiplier
}

// 根据ID获取商品信息
const getProductById = (productId: string) => {
  // 这里应该调用API获取商品信息
  // 暂时使用模拟数据
  const allProducts = [
    { id: '101', name: '月度会员卡', price: 299, type: 'MEMBERSHIP' },
    { id: '102', name: '季度会员卡', price: 799, type: 'MEMBERSHIP' },
    { id: '103', name: '年度会员卡', price: 2599, type: 'MEMBERSHIP' },
    { id: '201', name: '团体瑜伽课', price: 80, type: 'COURSE' },
    { id: '202', name: '动感单车课', price: 60, type: 'COURSE' },
    { id: '301', name: '私教课（10节）', price: 3000, type: 'PRIVATE_TRAINING' },
    { id: '401', name: '运动水杯', price: 89, type: 'MERCHANDISE' },
    { id: '402', name: '运动毛巾', price: 39, type: 'MERCHANDISE' }
  ]
  return allProducts.find(product => product.id === productId)
}

// 加载商品选项
const loadProductOptions = () => {
  // 分组显示商品选项
  productOptions.value = [
    {
      label: '会员卡',
      options: [
        { id: '101', name: '月度会员卡', price: 299 },
        { id: '102', name: '季度会员卡', price: 799 },
        { id: '103', name: '年度会员卡', price: 2599 }
      ]
    },
    {
      label: '课程',
      options: [
        { id: '201', name: '团体瑜伽课', price: 80 },
        { id: '202', name: '动感单车课', price: 60 }
      ]
    },
    {
      label: '私教课',
      options: [
        { id: '301', name: '私教课（10节）', price: 3000 }
      ]
    },
    {
      label: '商品',
      options: [
        { id: '401', name: '运动水杯', price: 89 },
        { id: '402', name: '运动毛巾', price: 39 }
      ]
    }
  ]
}

// 加载顾问列表
const loadConsultants = async () => {
  try {
    // 这里应该调用API获取顾问列表
    // 暂时使用模拟数据
    consultants.value = [
      { id: '1', name: '销售顾问A' },
      { id: '2', name: '销售顾问B' },
      { id: '3', name: '销售顾问C' }
    ]
  } catch (error) {
    console.error('加载顾问列表失败:', error)
  }
}

// 生成订单编号
const generateOrderNumber = () => {
  const timestamp = Date.now()
  const random = Math.floor(Math.random() * 10000)
  return `ORD${timestamp}${random.toString().padStart(4, '0')}`
}

// 监听订单类型变化
watch(() => props.formData.orderType, (newType) => {
  // 如果是商品购买，自动添加配送信息
  if (newType === 'MERCHANDISE') {
    if (!props.formData.recipientName && props.formData.memberId) {
      // 自动填充会员信息
      // 这里应该根据会员ID获取会员信息
      props.formData.recipientName = '张三'
      props.formData.recipientPhone = '13800138000'
    }
  }
})

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 验证订单项
    if (props.formData.items.length === 0) {
      ElMessage.error('请至少添加一个订单项')
      return
    }
    
    // 验证订单项数据
    for (const item of props.formData.items) {
      if (!item.productId) {
        ElMessage.error('请选择商品/服务')
        return
      }
      if (item.quantity <= 0) {
        ElMessage.error('商品数量必须大于0')
        return
      }
    }
    
    loading.value = true
    
    // 自动生成订单编号（如果是新订单）
    if (!props.isEdit && !props.formData.orderNumber) {
      props.formData.orderNumber = generateOrderNumber()
    }
    
    // 更新实付金额
    props.formData.actualAmount = actualAmount.value
    
    // 提交表单数据
    emit('submit', props.formData)
    
    ElMessage.success(props.isEdit ? '订单信息更新成功' : '订单创建成功')
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理取消
const handleCancel = () => {
  emit('cancel')
}

// 初始化
loadProductOptions()
loadConsultants()
</script>

<style scoped lang="scss">
.order-form {
  .form-section {
    padding: 20px 0;
    
    .el-form-item {
      margin-bottom: 20px;
    }
  }
  
  .form-actions {
    margin-top: 20px;
    text-align: right;
    padding-top: 20px;
    border-top: 1px solid #e4e7ed;
    
    .el-button {
      min-width: 100px;
    }
  }
}

.items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h3 {
    margin: 0;
    font-size: 16px;
    color: #303133;
  }
}

.order-items {
  margin-bottom: 20px;
  
  .subtotal {
    font-weight: bold;
    color: #e6a23c;
  }
}

.order-summary {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  
  .summary-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    font-size: 14px;
    
    .label {
      color: #606266;
    }
    
    .value {
      color: #303133;
      font-weight: 500;
      
      &.total {
        font-size: 18px;
        color: #f56c6c;
        font-weight: bold;
      }
    }
  }
}

:deep(.el-tabs) {
  .el-tabs__header {
    margin-bottom: 20px;
  }
}

:deep(.el-table) {
  margin-bottom: 20px;
}
</style>