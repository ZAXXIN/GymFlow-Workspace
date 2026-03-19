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

            <el-form-item label="密码" prop="basicDTO.password" class="form-item" v-if="!isEditMode">
              <el-input v-model="formData.basicDTO.password" type="password" placeholder="请输入密码" maxlength="255" clearable show-password />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="出生日期" prop="basicDTO.birthday" class="form-item">
              <el-date-picker v-model="formData.basicDTO.birthday" type="date" placeholder="选择出生日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>

            <!-- 会员编号：编辑模式下显示，新增模式下不显示（后端自动生成） -->
            <el-form-item v-if="isEditMode" label="会员编号" prop="basicDTO.memberNo" class="form-item">
              <el-input v-model="formData.basicDTO.memberNo" disabled placeholder="系统自动生成" />
            </el-form-item>
          </div>

          <!-- 会籍时间：只在编辑模式且已有会籍卡时显示，新增时不显示 -->
          <div v-if="isEditMode && hasMembershipCard" class="form-row">
            <el-form-item label="会籍开始" class="form-item">
              <el-input :value="formData.basicDTO.membershipStartDate || '未开通'" disabled />
            </el-form-item>

            <el-form-item label="会籍结束" class="form-item">
              <el-input :value="formData.basicDTO.membershipEndDate || '未开通'" disabled />
            </el-form-item>
          </div>
        </el-card>

        <!-- 健康档案（完整保留） -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">健康档案</span>
              <el-button type="primary" text @click="handleAddHealthRecord" v-if="!formData.healthRecordDTO">
                添加健康记录
              </el-button>
            </div>
          </template>

          <div v-if="formData.healthRecordDTO" class="health-info">
            <div class="form-row">
              <el-form-item label="记录日期" prop="healthRecordDTO.recordDate" class="form-item">
                <el-date-picker v-model="formData.healthRecordDTO.recordDate" type="date" placeholder="选择记录日期" value-format="YYYY-MM-DD" style="width: 100%" />
              </el-form-item>

              <el-form-item label="身高(cm)" prop="healthRecordDTO.height" class="form-item">
                <el-input-number v-model="formData.healthRecordDTO.height" :min="100" :max="250" :step="0.1" controls-position="right" style="width: 100%" />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="体重(kg)" prop="healthRecordDTO.weight" class="form-item">
                <el-input-number v-model="formData.healthRecordDTO.weight" :min="30" :max="200" :step="0.1" controls-position="right" style="width: 100%" />
              </el-form-item>

              <el-form-item label="BMI指数">
                <el-tag :type="bmiType" size="large">
                  {{ bmiValue || '--' }}
                </el-tag>
                <span class="bmi-tip">{{ bmiTip }}</span>
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="体脂率(%)" prop="healthRecordDTO.bodyFatPercentage" class="form-item">
                <el-input-number v-model="formData.healthRecordDTO.bodyFatPercentage" :min="5" :max="50" :step="0.1" controls-position="right" style="width: 100%" />
              </el-form-item>

              <el-form-item label="肌肉量(kg)" prop="healthRecordDTO.muscleMass" class="form-item">
                <el-input-number v-model="formData.healthRecordDTO.muscleMass" :min="20" :max="100" :step="0.1" controls-position="right" style="width: 100%" />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="胸围(cm)" prop="healthRecordDTO.chestCircumference" class="form-item">
                <el-input-number v-model="formData.healthRecordDTO.chestCircumference" :min="50" :max="150" :step="0.1" controls-position="right" style="width: 100%" />
              </el-form-item>

              <el-form-item label="腰围(cm)" prop="healthRecordDTO.waistCircumference" class="form-item">
                <el-input-number v-model="formData.healthRecordDTO.waistCircumference" :min="50" :max="150" :step="0.1" controls-position="right" style="width: 100%" />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="臀围(cm)" prop="healthRecordDTO.hipCircumference" class="form-item">
                <el-input-number v-model="formData.healthRecordDTO.hipCircumference" :min="50" :max="150" :step="0.1" controls-position="right" style="width: 100%" />
              </el-form-item>

              <el-form-item label="血压" prop="healthRecordDTO.bloodPressure" class="form-item">
                <el-input v-model="formData.healthRecordDTO.bloodPressure" placeholder="如：120/80" clearable />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item label="心率(bpm)" prop="healthRecordDTO.heartRate" class="form-item">
                <el-input-number v-model="formData.healthRecordDTO.heartRate" :min="40" :max="200" :step="1" controls-position="right" style="width: 100%" />
              </el-form-item>
            </div>

            <el-form-item label="备注" prop="healthRecordDTO.notes" class="full-width">
              <el-input v-model="formData.healthRecordDTO.notes" type="textarea" :rows="3" placeholder="请输入健康状况备注" maxlength="500" show-word-limit />
            </el-form-item>

            <div class="health-actions">
              <el-button type="danger" text @click="handleRemoveHealthRecord">
                移除健康记录
              </el-button>
            </div>
          </div>

          <div v-else class="no-health-record">
            <el-empty description="暂无健康档案信息" :image-size="80" />
          </div>
        </el-card>

        <!-- 会员卡信息（重构后，不再使用 MemberCardSelector） -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">会员卡信息</span>
              <el-button type="primary" text @click="handleAddCard" v-if="!formData.cardDTO">
                添加会员卡
              </el-button>
            </div>
          </template>

          <div v-if="formData.cardDTO" class="card-info">
            <!-- 商品级联选择（只显示 productType 0,1,2） -->
            <el-form-item label="卡类型" prop="cardDTO.productId" required>
              <el-cascader v-model="selectedProduct" :options="productOptions" :props="cascaderProps" placeholder="请选择卡类型" style="width: 100%" clearable filterable :show-all-levels="false" @change="handleProductChange" />
            </el-form-item>

            <!-- 课程卡：显示总课时数（私教课、团课） -->
            <el-row :gutter="20" v-if="isCourseCard">
              <el-col :span="24">
                <el-form-item label="总课时" prop="cardDTO.totalSessions">
                  <el-input-number v-model="formData.cardDTO.totalSessions" :min="1" :max="999" :step="1" controls-position="right" style="width: 100%" placeholder="请输入总课时" disabled />
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 金额（自动填充，不可编辑） -->
            <el-row :gutter="20">
              <el-col :span="24">
                <el-form-item label="金额" prop="cardDTO.amount" required>
                  <el-input-number v-model="formData.cardDTO.amount" :min="0" :step="100" :precision="2" controls-position="right" style="width: 100%" placeholder="请先选择商品" :disabled="true">
                    <template #prepend>¥</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>

            <!-- 商品信息提示 -->
            <el-alert v-if="selectedProductInfo" :title="selectedProductInfo.productName" :description="productDescription" type="info" :closable="false" show-icon />

            <div class="card-actions">
              <el-button type="danger" text @click="handleRemoveCard">
                移除会员卡
              </el-button>
            </div>
          </div>

          <div v-else class="no-card">
            <el-empty description="暂无会员卡信息" :image-size="80" />
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
  MemberBasicDTO,
  HealthRecordDTO,
  MemberCardDTO,
} from '@/types/member'

const router = useRouter()
const route = useRoute()
const memberStore = useMemberStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEditMode = computed(() => !!route.params.id)

// 判断是否有会籍卡（月卡/年卡/周卡）
const hasMembershipCard = computed(() => {
  return formData.cardDTO && [2, 3, 4].includes(formData.cardDTO.cardType)
})

// 表单数据
const formData = reactive<MemberAddRequest>({
  basicDTO: {
    phone: '',
    password: '',
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

// 手机号检查
const phoneChecking = ref(false)
const phoneAvailable = ref<boolean | null>(null)

// 表单验证规则
const formRules: FormRules = {
  'basicDTO.phone': [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  'basicDTO.password': [
    { required: !isEditMode.value, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 255, message: '密码长度至少6个字符', trigger: 'blur' },
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

const cascaderProps = {
  expandTrigger: 'hover' as const,
  value: 'id',
  label: 'productName',
  children: 'children',
  checkStrictly: false,
  emitPath: false,
}

// 判断是否为课程卡（私教课、团课）
const isCourseCard = computed(() => {
  const type = formData.cardDTO?.cardType
  return type == 1 || type == 2
})

// 商品描述
const productDescription = computed(() => {
  if (!selectedProductInfo.value) return ''
  const info = selectedProductInfo.value
  const desc = []
  if (info.validityDays) desc.push(`有效期：${info.validityDays}天`)
  if (info.totalSessions) desc.push(`总课时：${info.totalSessions}节`)
  if (info.description) desc.push(info.description)
  return desc.join(' | ')
})

// 加载商品列表（只加载 productType = 0,1,2）
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
            validityDays: product.validityDays,
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

// 商品选择变化
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

    if (product.productType === 0 || product.productType === 1) {
      formData.cardDTO.totalSessions = product.totalSessions || 10
    } else {
      formData.cardDTO.totalSessions = undefined
    }
  }
}

// ---------- 健康档案相关 ----------
const bmiValue = computed(() => {
  if (formData.healthRecordDTO?.height && formData.healthRecordDTO?.weight) {
    const heightInM = formData.healthRecordDTO.height / 100
    const bmi = formData.healthRecordDTO.weight / (heightInM * heightInM)
    return bmi.toFixed(1)
  }
  return null
})

const bmiType = computed(() => {
  if (!bmiValue.value) return 'info'
  const bmi = parseFloat(bmiValue.value)
  if (bmi < 18.5) return 'warning'
  if (bmi < 24) return 'success'
  if (bmi < 28) return 'warning'
  return 'danger'
})

const bmiTip = computed(() => {
  if (!bmiValue.value) return ''
  const bmi = parseFloat(bmiValue.value)
  if (bmi < 18.5) return '偏瘦'
  if (bmi < 24) return '正常'
  if (bmi < 28) return '超重'
  return '肥胖'
})

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

const handleAddHealthRecord = () => {
  const today = new Date().toISOString().split('T')[0]
  formData.healthRecordDTO = {
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
  }
}

const handleRemoveHealthRecord = async () => {
  try {
    await ElMessageBox.confirm('确定要移除健康记录吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    formData.healthRecordDTO = undefined
  } catch {}
}

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

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    if (!isEditMode.value && phoneAvailable.value === false) {
      ElMessage.warning('手机号已存在，请更换手机号')
      return
    }

    if (formData.cardDTO && !formData.cardDTO.productId) {
      ElMessage.warning('请选择会员卡商品')
      return
    }

    loading.value = true

    if (isEditMode.value) {
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
        healthRecordDTO: formData.healthRecordDTO,
        cardDTO: formData.cardDTO,
      }
      delete updateData.basicDTO.password
      await memberStore.updateMember(Number(route.params.id), updateData)
      ElMessage.success('会员信息更新成功')
    } else {
      const addData: MemberAddRequest = {
        basicDTO: {
          phone: formData.basicDTO.phone,
          password: formData.basicDTO.password,
          realName: formData.basicDTO.realName,
          gender: formData.basicDTO.gender,
          birthday: formData.basicDTO.birthday,
          memberNo: '',
          membershipStartDate: '',
          membershipEndDate: '',
        },
        healthRecordDTO: formData.healthRecordDTO,
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

const handleCancel = () => {
  router.push('/member/list')
}

const initFormData = async () => {
  if (!isEditMode.value) return

  try {
    loading.value = true
    const memberId = Number(route.params.id)
    const memberDetail = await memberStore.fetchMemberDetail(memberId)

    if (memberDetail) {
      formData.basicDTO = {
        phone: memberDetail.phone,
        password: '',
        realName: memberDetail.realName,
        gender: memberDetail.gender,
        birthday: memberDetail.birthday || '',
        memberNo: memberDetail.memberNo,
        membershipStartDate: memberDetail.membershipStartDate || '',
        membershipEndDate: memberDetail.membershipEndDate || '',
      }

      if (memberDetail.healthRecords && memberDetail.healthRecords.length > 0) {
        formData.healthRecordDTO = { ...memberDetail.healthRecords[0] }
      }

      if (memberDetail.memberCards && memberDetail.memberCards.length > 0) {
        const card = memberDetail.memberCards[0]
        formData.cardDTO = {
          productId: card.productId,
          productName: card.productName,
          cardType: card.cardType,
          startDate: card.startDate,
          endDate: card.endDate,
          totalSessions: card.totalSessions,
          remainingSessions: card.remainingSessions,
          amount: card.amount,
          status: card.status,
        }
        if (card.productId) {
          selectedProduct.value = [card.productId]
          selectedProductInfo.value = productMap.value.get(card.productId) || null
        }
      }
    }
  } catch (error) {
    console.error('加载会员详情失败:', error)
    ElMessage.error('加载会员信息失败')
  } finally {
    loading.value = false
  }
}

watch(
  () => [formData.healthRecordDTO?.height, formData.healthRecordDTO?.weight],
  () => {}
)

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

.health-actions,
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

:deep(.el-card__header) {
  padding: 16px 20px;
  background-color: #fafafa;
}
</style>