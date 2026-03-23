<template>
  <div class="member-form-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/member' }">会员管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ isEditMode ? '编辑会员' : '新增会员' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">{{ isEditMode ? '编辑会员信息' : '新增会员' }}</h1>
      </div>
      <div class="header-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEditMode ? '更新' : '创建' }}
        </el-button>
      </div>
    </div>

    <!-- 主表单区域 -->
    <div class="form-content">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" class="member-form">
        <!-- 基本信息 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
            </div>
          </template>

          <div class="form-row">
            <el-form-item label="真实姓名" prop="basicDTO.realName" class="form-item">
              <el-input v-model="formData.basicDTO.realName" placeholder="请输入真实姓名" maxlength="50" clearable />
            </el-form-item>

            <el-form-item label="性别" prop="basicDTO.gender" class="form-item">
              <el-radio-group v-model="formData.basicDTO.gender">
                <el-radio :label="0">女</el-radio>
                <el-radio :label="1">男</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="手机号" prop="basicDTO.phone" class="form-item">
              <el-input v-model="formData.basicDTO.phone" placeholder="请输入手机号" maxlength="11" clearable @blur="checkPhoneAvailability" :disabled="isEditMode" />
              <div v-if="phoneChecking" class="checking-text">检查中...</div>
              <div v-if="phoneAvailable !== null && !isEditMode" :class="['check-result', phoneAvailable ? 'success' : 'error']">
                <!-- 提示信息可保留或隐藏 -->
              </div>
            </el-form-item>

            <el-form-item label="出生日期" prop="basicDTO.birthday" class="form-item">
              <el-date-picker v-model="formData.basicDTO.birthday" type="date" placeholder="选择出生日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </div>
        </el-card>

        <!-- 健康档案 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">健康档案</span>
              <el-button type="primary" text @click="handleAddHealthRecord">
                添加健康记录
              </el-button>
            </div>
          </template>

          <div v-if="healthRecords.length > 0" class="health-records-container">
            <el-collapse v-model="activeHealthRecordIndex" accordion>
              <el-collapse-item v-for="(record, index) in healthRecords" :key="index" :name="index">
                <template #title>
                  <div class="collapse-title">
                    <span class="record-date">{{ record.recordDate }}</span>
                    <el-tag size="small" :type="record.id ? 'primary' : 'warning'" style="margin-left: 12px;">
                      {{ record.id ? '已有记录' : '新增记录' }}
                    </el-tag>
                    <el-button type="danger" text size="small" style="margin-left: auto;" @click.stop="handleDeleteHealthRecord(index)">
                      删除
                    </el-button>
                  </div>
                </template>

                <div class="health-record-form">
                  <div class="form-row">
                    <el-form-item label="记录日期" :prop="`healthRecords.${index}.recordDate`" class="form-item">
                      <el-date-picker v-model="record.recordDate" type="date" placeholder="选择记录日期" value-format="YYYY-MM-DD" style="width: 100%" :disabled="!!record.id" />
                    </el-form-item>

                    <el-form-item label="身高(cm)" :prop="`healthRecords.${index}.height`" class="form-item">
                      <el-input-number v-model="record.height" :min="100" :max="250" :step="1" controls-position="right" style="width: 100%" />
                    </el-form-item>
                  </div>

                  <div class="form-row">
                    <el-form-item label="体重(kg)" :prop="`healthRecords.${index}.weight`" class="form-item">
                      <el-input-number v-model="record.weight" :min="30" :max="200" :step="1" controls-position="right" style="width: 100%" />
                    </el-form-item>

                    <el-form-item label="BMI指数" class="form-item">
                      <el-tag :type="getBmiType(record)" size="large">
                        {{ calculateBmi(record) || '--' }}
                      </el-tag>
                      <span class="bmi-tip">{{ getBmiTip(record) }}</span>
                    </el-form-item>
                  </div>

                  <div class="form-row">
                    <el-form-item label="体脂率(%)" :prop="`healthRecords.${index}.bodyFatPercentage`" class="form-item">
                      <el-input-number v-model="record.bodyFatPercentage" :min="5" :max="50" :step="1" controls-position="right" style="width: 100%" />
                    </el-form-item>

                    <el-form-item label="肌肉量(kg)" :prop="`healthRecords.${index}.muscleMass`" class="form-item">
                      <el-input-number v-model="record.muscleMass" :min="20" :max="100" :step="1" controls-position="right" style="width: 100%" />
                    </el-form-item>
                  </div>

                  <div class="form-row">
                    <el-form-item label="胸围(cm)" :prop="`healthRecords.${index}.chestCircumference`" class="form-item">
                      <el-input-number v-model="record.chestCircumference" :min="50" :max="150" :step="1" controls-position="right" style="width: 100%" />
                    </el-form-item>

                    <el-form-item label="腰围(cm)" :prop="`healthRecords.${index}.waistCircumference`" class="form-item">
                      <el-input-number v-model="record.waistCircumference" :min="50" :max="150" :step="1" controls-position="right" style="width: 100%" />
                    </el-form-item>
                  </div>

                  <div class="form-row">
                    <el-form-item label="臀围(cm)" :prop="`healthRecords.${index}.hipCircumference`" class="form-item">
                      <el-input-number v-model="record.hipCircumference" :min="50" :max="150" :step="1" controls-position="right" style="width: 100%" />
                    </el-form-item>

                    <el-form-item label="血压" :prop="`healthRecords.${index}.bloodPressure`" class="form-item">
                      <el-input v-model="record.bloodPressure" placeholder="如：120/80" clearable />
                    </el-form-item>
                  </div>

                  <div class="form-row">
                    <el-form-item label="心率(bpm)" :prop="`healthRecords.${index}.heartRate`" class="form-item">
                      <el-input-number v-model="record.heartRate" :min="40" :max="200" :step="1" controls-position="right" style="width: 100%" />
                    </el-form-item>
                  </div>

                  <el-form-item label="备注" :prop="`healthRecords.${index}.notes`" class="full-width">
                    <el-input v-model="record.notes" type="textarea" :rows="3" placeholder="请输入健康状况备注" maxlength="500" show-word-limit />
                  </el-form-item>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>

          <div v-else class="no-health-record">
            <el-empty description="暂无健康档案信息" :image-size="80" />
          </div>
        </el-card>

        <!-- 会员卡信息 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">会员卡信息</span>
              <el-button type="primary" text @click="handleAddCard">添加会员卡</el-button>
            </div>
          </template>

          <!-- 新增模式：只能选择一张会员卡 -->
          <div v-if="!isEditMode">
            <div v-if="formData.cardDTO" class="card-info">
              <el-form-item label="卡类型" prop="cardDTO.productId" required>
                <el-cascader v-model="selectedProduct" :options="productOptions" :props="cascaderProps" placeholder="请选择卡类型" style="width: 100%" clearable filterable :show-all-levels="false" @change="handleProductChange" />
              </el-form-item>

              <el-row :gutter="20" v-if="isCourseCard">
                <el-col :span="24">
                  <el-form-item label="总课时" prop="cardDTO.totalSessions">
                    <el-input-number v-model="formData.cardDTO.totalSessions" :min="1" :max="999" :step="1" controls-position="right" style="width: 100%" placeholder="请输入总课时" :disabled="true" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="20">
                <el-col :span="24">
                  <el-form-item label="金额" prop="cardDTO.amount" required>
                    <el-input-number v-model="formData.cardDTO.amount" :min="0" :step="100" :precision="2" controls-position="right" style="width: 100%" placeholder="请先选择商品" :disabled="true">
                      <template #prepend>¥</template>
                    </el-input-number>
                  </el-form-item>
                </el-col>
              </el-row>

              <el-alert v-if="selectedProductInfo" :title="selectedProductInfo.productName" :description="productDescription" type="info" :closable="false" show-icon />

              <div class="card-actions">
                <el-button type="danger" text @click="handleRemoveCard">移除会员卡</el-button>
              </div>
            </div>

            <div v-else class="no-card">
              <el-empty description="暂无会员卡" :image-size="80" />
              <div class="empty-actions">
              </div>
            </div>
          </div>

          <!-- 编辑模式：显示已有会员卡 + 添加新卡区域 -->
          <div v-else>
            <!-- 已有会员卡列表（只读） -->
            <div v-if="existingCards.length > 0" class="existing-cards">
              <div class="section-subtitle">已有会员卡</div>
              <div v-for="card in existingCards" :key="card.productId" class="existing-card-item">
                <div class="card-header-info">
                  <span class="card-name">{{ card.productName }}</span>
                  <el-tag :type="getCardStatusType(card.status)" size="small">
                    {{ card.statusDesc || '有效' }}
                  </el-tag>
                </div>
                <div class="card-detail-info">
                  <template v-if="card.cardType == 0">
                    <span>有效期：{{ formatDate(card.startDate) }} 至 {{ formatDate(card.endDate) }}</span>
                  </template>
                  <template v-else>
                    <span>总课时：{{ card.totalSessions || 0 }}，剩余：{{ card.remainingSessions || 0 }}</span>
                  </template>
                </div>
              </div>
            </div>
            <div v-else class="no-card">
              <el-empty description="暂无会员卡" :image-size="60" />
            </div>

            <!-- 添加新卡区域 -->
            <div class="add-card-section">
              <div class="section-subtitle">添加新卡</div>
              <div v-if="!showAddCardForm" class="add-card-trigger">
                <el-button type="primary" text @click="showAddCardForm = true">
                  <el-icon>
                    <Plus />
                  </el-icon>
                  添加新卡
                </el-button>
              </div>
              <div v-else class="add-card-form">
                <el-form-item label="卡类型" required>
                  <el-cascader v-model="newCardSelected" :options="productOptions" :props="cascaderProps" placeholder="请选择卡类型" style="width: 100%" clearable filterable :show-all-levels="false" @change="handleNewCardChange" />
                </el-form-item>

                <el-row :gutter="20" v-if="isNewCardCourse">
                  <el-col :span="24">
                    <el-form-item label="总课时">
                      <el-input-number v-model="newCardForm.totalSessions" :min="1" :max="999" :step="1" controls-position="right" style="width: 100%" :disabled="true" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="24">
                    <el-form-item label="金额">
                      <el-input-number v-model="newCardForm.amount" :min="0" :precision="2" controls-position="right" style="width: 100%" :disabled="true">
                        <template #prepend>¥</template>
                      </el-input-number>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-alert v-if="newCardProductInfo" :title="newCardProductInfo.productName" type="info" :closable="false" show-icon />

                <div class="add-card-actions">
                  <el-button size="small" @click="cancelAddCard">取消</el-button>
                  <el-button type="primary" size="small" @click="confirmAddNewCard" :loading="addCardLoading">
                    确认添加
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useMemberStore } from '@/stores/member'
import { productApi } from '@/api/product'
import type {
  MemberAddRequest,
  MemberUpdateRequest,
  HealthRecordDTO,
  MemberCardDTO,
  MiniMemberCardDTO,
} from '@/types/member'

const router = useRouter()
const route = useRoute()
const memberStore = useMemberStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEditMode = computed(() => !!route.params.id)

// 表单数据
const formData = reactive<MemberAddRequest>({
  basicDTO: {
    phone: '',
    realName: '',
    gender: 1,
    birthday: '',
    memberNo: '',
    membershipStartDate: '',
    membershipEndDate: '',
  },
  healthRecordDTO: undefined,
  cardDTO: undefined,
})

// 健康记录列表
const healthRecords = ref<HealthRecordDTO[]>([])
const activeHealthRecordIndex = ref<number | string | null>(null)

// 手机号检查
const phoneChecking = ref(false)
const phoneAvailable = ref<boolean | null>(null)

// 表单验证规则
const formRules: FormRules = {
  'basicDTO.phone': [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  'basicDTO.realName': [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在2-50个字符', trigger: 'blur' },
  ],
  'basicDTO.gender': [{ required: true, message: '请选择性别', trigger: 'change' }],
  'basicDTO.birthday': [{ required: true, message: '请选择出生日期', trigger: 'change' }],
}

// ---------- 会员卡相关逻辑 ----------
const productOptions = ref<any[]>([])
const productMap = ref<Map<number, any>>(new Map())
const selectedProductInfo = ref<any>(null)
const selectedProduct = ref<(string | number)[]>([])

// 编辑模式：已有会员卡
const existingCards = ref<MiniMemberCardDTO[]>([])

// 添加新卡相关
const showAddCardForm = ref(false)
const addCardLoading = ref(false)
const newCardSelected = ref<(string | number)[]>([])
const newCardProductInfo = ref<any>(null)
const newCardForm = ref({
  productId: undefined as number | undefined,
  productName: undefined as string | undefined,
  cardType: undefined as number | undefined,
  totalSessions: undefined as number | undefined,
  amount: 0,
})

const cascaderProps = {
  expandTrigger: 'hover' as const,
  value: 'id',
  label: 'productName',
  children: 'children',
  checkStrictly: false,
  emitPath: false,
}

// 判断是否为课程卡
const isCourseCard = computed(() => {
  const type = formData.cardDTO?.cardType
  return type === 1 || type === 2
})

// 判断新卡是否为课程卡
const isNewCardCourse = computed(() => {
  const type = newCardForm.value.cardType
  return type === 1 || type === 2
})

// 商品描述
const productDescription = computed(() => {
  if (!selectedProductInfo.value) return ''
  const info = selectedProductInfo.value
  const desc = []
  if (info.totalSessions) desc.push(`总课时：${info.totalSessions}节`)
  if (info.description) desc.push(info.description)
  return desc.join(' | ')
})

// 获取卡状态标签类型
const getCardStatusType = (status: string | undefined) => {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'EXPIRED':
      return 'danger'
    case 'USED_UP':
      return 'warning'
    default:
      return 'info'
  }
}

// 格式化日期
const formatDate = (date: string | undefined) => {
  if (!date) return '-'
  return date
}

// 加载商品列表
const loadProducts = async () => {
  try {
    const productTypes = [0, 1, 2]
    const typeLabels = ['会籍卡', '私教课', '团课']

    const options = []

    for (let i = 0; i < productTypes.length; i++) {
      const type = productTypes[i]
      const response = await productApi.getProductList({
        productType: type,
        status: 1,
        pageSize: 100,
      })

      if (response.code === 200 && response.data.list.length > 0) {
        const children = response.data.list.map((product: any) => {
          productMap.value.set(product.id, {
            id: product.id,
            productName: product.productName,
            productType: type,
            productTypeDesc: product.productTypeDesc,
            currentPrice: product.currentPrice,
            originalPrice: product.originalPrice,
            totalSessions: product.totalSessions,
            description: product.description,
          })
          return {
            id: product.id,
            productName: product.productName,
            productType: type,
            productTypeDesc: product.productTypeDesc,
            currentPrice: product.currentPrice,
          }
        })

        options.push({
          id: `type-${type}`,
          productName: typeLabels[i],
          children,
        })
      }
    }

    productOptions.value = options
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  }
}

// 新增模式：商品选择变化
const handleProductChange = (value: number) => {
  if (!value) {
    selectedProductInfo.value = null
    if (formData.cardDTO) {
      formData.cardDTO.productId = undefined
      formData.cardDTO.productName = undefined
      formData.cardDTO.cardType = undefined
      formData.cardDTO.totalSessions = undefined
      formData.cardDTO.amount = 0
    }
    return
  }

  const product = productMap.value.get(value)
  if (product && formData.cardDTO) {
    selectedProductInfo.value = product
    formData.cardDTO.productId = product.id
    formData.cardDTO.productName = product.productName
    formData.cardDTO.cardType = product.productType
    formData.cardDTO.amount = product.currentPrice

    if (product.productType === 1 || product.productType === 2) {
      formData.cardDTO.totalSessions = product.totalSessions || 10
    } else {
      formData.cardDTO.totalSessions = undefined
    }
  }
}

// 编辑模式：新卡商品选择变化
const handleNewCardChange = (value: number) => {
  if (!value) {
    newCardProductInfo.value = null
    newCardForm.value = {
      productId: undefined,
      productName: undefined,
      cardType: undefined,
      totalSessions: undefined,
      amount: 0,
    }
    return
  }

  const product = productMap.value.get(value)
  if (product) {
    newCardProductInfo.value = product
    newCardForm.value = {
      productId: product.id,
      productName: product.productName,
      cardType: product.productType,
      totalSessions: product.totalSessions || undefined,
      amount: product.currentPrice,
    }
  }
}

// 取消添加新卡
const cancelAddCard = () => {
  showAddCardForm.value = false
  newCardSelected.value = []
  newCardProductInfo.value = null
  newCardForm.value = {
    productId: undefined,
    productName: undefined,
    cardType: undefined,
    totalSessions: undefined,
    amount: 0,
  }
}

// 确认添加新卡
const confirmAddNewCard = async () => {
  if (!newCardForm.value.productId) {
    ElMessage.warning('请选择卡类型')
    return
  }

  try {
    addCardLoading.value = true
    const memberId = Number(route.params.id)
    await memberStore.addMemberCard(memberId, newCardForm.value)
    ElMessage.success('添加成功')

    // 刷新会员详情，获取最新的会员卡列表
    await initFormData()

    // 重置表单
    cancelAddCard()
  } catch (error: any) {
    console.error('添加失败:', error)
    ElMessage.error(error.message || '添加失败')
  } finally {
    addCardLoading.value = false
  }
}

// ---------- 健康档案相关 ----------
const calculateBmi = (record: HealthRecordDTO) => {
  if (record.height && record.weight) {
    const heightInM = record.height / 100
    const bmi = record.weight / (heightInM * heightInM)
    return bmi.toFixed(1)
  }
  return null
}

const getBmiType = (record: HealthRecordDTO) => {
  const bmi = calculateBmi(record)
  if (!bmi) return 'info'
  const bmiNum = parseFloat(bmi)
  if (bmiNum < 18.5) return 'warning'
  if (bmiNum < 24) return 'success'
  if (bmiNum < 28) return 'warning'
  return 'danger'
}

const getBmiTip = (record: HealthRecordDTO) => {
  const bmi = calculateBmi(record)
  if (!bmi) return ''
  const bmiNum = parseFloat(bmi)
  if (bmiNum < 18.5) return '偏瘦'
  if (bmiNum < 24) return '正常'
  if (bmiNum < 28) return '超重'
  return '肥胖'
}

const handleAddHealthRecord = () => {
  const today = new Date().toISOString().split('T')[0]
  const hasTodayRecord = healthRecords.value.some((r) => r.recordDate === today)
  if (hasTodayRecord) {
    ElMessage.warning('今日已有健康记录，请编辑已有记录')
    return
  }

  healthRecords.value.push({
    id: undefined,
    recordDate: today,
    height: 170,
    weight: 65,
    bodyFatPercentage: 20,
    muscleMass: 45,
    chestCircumference: 95,
    waistCircumference: 85,
    hipCircumference: 95,
    bloodPressure: '120/80',
    heartRate: 75,
    notes: '',
  })
  activeHealthRecordIndex.value = healthRecords.value.length - 1
}

const handleDeleteHealthRecord = async (index: number) => {
  const record = healthRecords.value[index]
  if (record.id) {
    try {
      await ElMessageBox.confirm('确定要删除这条健康记录吗？', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
      await memberStore.deleteHealthRecord(record.id)
      healthRecords.value.splice(index, 1)
      if (activeHealthRecordIndex.value === index) {
        activeHealthRecordIndex.value = healthRecords.value.length > 0 ? 0 : null
      }
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除失败:', error)
        ElMessage.error('删除失败')
      }
    }
  } else {
    healthRecords.value.splice(index, 1)
    if (activeHealthRecordIndex.value === index) {
      activeHealthRecordIndex.value = healthRecords.value.length > 0 ? 0 : null
    }
  }
}

// 手机号检查
const checkPhoneAvailability = async () => {
  const phone = formData.basicDTO.phone
  if (!phone || !/^1[3-9]\d{9}$/.test(phone) || isEditMode.value) return

  phoneChecking.value = true
  try {
    await new Promise((resolve) => setTimeout(resolve, 500))
    phoneAvailable.value = true
  } catch (error) {
    console.error('检查手机号失败:', error)
    phoneAvailable.value = null
  } finally {
    phoneChecking.value = false
  }
}

// 新增模式：添加会员卡
const handleAddCard = () => {
  formData.cardDTO = {
    productId: undefined,
    productName: undefined,
    cardType: undefined,
    amount: 0,
  }
  selectedProduct.value = []
  selectedProductInfo.value = null
}

// 新增模式：移除会员卡
const handleRemoveCard = async () => {
  try {
    await ElMessageBox.confirm('确定要移除会员卡吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    formData.cardDTO = undefined
    selectedProduct.value = []
    selectedProductInfo.value = null
  } catch {}
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    if (!isEditMode.value && phoneAvailable.value === false) {
      ElMessage.warning('手机号已存在，请更换手机号')
      return
    }

    // 新增模式必须选择会员卡
    if (!isEditMode.value && (!formData.cardDTO || !formData.cardDTO.productId)) {
      ElMessage.warning('请选择会员卡')
      return
    }

    // 验证健康记录的日期
    for (let i = 0; i < healthRecords.value.length; i++) {
      const record = healthRecords.value[i]
      if (!record.recordDate) {
        ElMessage.warning(`第${i + 1}条健康记录的日期不能为空`)
        activeHealthRecordIndex.value = i
        return
      }
    }

    loading.value = true

    if (isEditMode.value) {
      // 编辑模式：只更新基本信息和健康档案
      const updateData: MemberUpdateRequest = {
        basicDTO: {
          phone: formData.basicDTO.phone,
          realName: formData.basicDTO.realName,
          gender: formData.basicDTO.gender,
          birthday: formData.basicDTO.birthday,
          memberNo: formData.basicDTO.memberNo,
          membershipStartDate: '',
          membershipEndDate: '',
        },
        healthRecordDTO: undefined,
        cardDTO: undefined,
      }
      await memberStore.updateMember(Number(route.params.id), updateData)

      // 处理健康记录
      for (const record of healthRecords.value) {
        if (record.id) {
          await memberStore.updateHealthRecord(record.id, record)
        } else {
          await memberStore.addHealthRecord(Number(route.params.id), record)
        }
      }

      ElMessage.success('会员信息更新成功')
    } else {
      // 新增模式
      const addData: MemberAddRequest = {
        basicDTO: {
          phone: formData.basicDTO.phone,
          realName: formData.basicDTO.realName,
          gender: formData.basicDTO.gender,
          birthday: formData.basicDTO.birthday,
          memberNo: '',
          membershipStartDate: '',
          membershipEndDate: '',
        },
        healthRecordDTO: healthRecords.value.length > 0 ? healthRecords.value[0] : undefined,
        cardDTO: formData.cardDTO,
      }
      await memberStore.addMember(addData)
      ElMessage.success('会员创建成功')
    }

    router.push('/member/list')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请检查表单')
  } finally {
    loading.value = false
  }
}

// 取消
const handleCancel = () => {
  router.push('/member/list')
}

// 初始化表单数据（编辑模式）
const initFormData = async () => {
  if (!isEditMode.value) return

  try {
    loading.value = true
    const memberId = Number(route.params.id)
    const memberDetail = await memberStore.fetchMemberDetail(memberId)

    if (memberDetail) {
      // 基本信息回显
      formData.basicDTO = {
        phone: memberDetail.phone || '',
        realName: memberDetail.realName || '',
        gender: memberDetail.gender ?? 1,
        birthday: memberDetail.birthday || '',
        memberNo: memberDetail.memberNo || '',
        membershipStartDate: '',
        membershipEndDate: '',
      }

      // 健康档案回显
      if (memberDetail.healthRecords && memberDetail.healthRecords.length > 0) {
        healthRecords.value = [...memberDetail.healthRecords]
        activeHealthRecordIndex.value = null
      }

      // 会员卡回显（只读）
      if (memberDetail.memberCards && memberDetail.memberCards.length > 0) {
        existingCards.value = memberDetail.memberCards
      }

      // 编辑模式不设置 cardDTO（新增卡通过单独接口添加）
      formData.cardDTO = undefined
    }
  } catch (error) {
    console.error('加载会员详情失败:', error)
    ElMessage.error('加载会员信息失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initFormData()
  loadProducts()
})
</script>

<style scoped>
.member-form-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left .page-title {
  margin: 10px 0 0 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  margin-top: 10px;
}

.form-content {
  background-color: white;
  border-radius: 4px;
  padding: 20px;
}

.form-section {
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
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

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.form-item {
  flex: 1;
  margin-bottom: 0;
}

.full-width {
  width: 100%;
}

.checking-text {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.check-result {
  font-size: 12px;
  margin-top: 5px;
}

.check-result.success {
  color: #67c23a;
}

.check-result.error {
  color: #f56c6c;
}

.bmi-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.card-actions {
  text-align: right;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.no-health-record,
.no-card {
  padding: 40px 0;
}

.empty-actions {
  margin-top: 12px;
  text-align: center;
}

.existing-cards {
  margin-bottom: 24px;
}

.section-subtitle {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 12px;
  padding-left: 4px;
  border-left: 3px solid #67c23a;
}

.existing-card-item {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fafafa;
  margin-bottom: 10px;
}

.card-header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.card-detail-info {
  font-size: 12px;
  color: #606266;
}

.add-card-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px dashed #e4e7ed;
}

.add-card-trigger {
  text-align: center;
  padding: 16px;
  background-color: #fafafa;
  border-radius: 8px;
  border: 1px dashed #dcdfe6;
}

.add-card-form {
  padding: 16px;
  background-color: #fafafa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.add-card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  background-color: #fafafa;
}
</style>