<template>
  <div class="member-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      label-position="right"
      v-loading="loading"
    >
      <el-tabs v-model="activeTab">
        <!-- 基础信息 -->
        <el-tab-pane label="基础信息" name="basic">
          <div class="form-section">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="会员姓名" prop="basicDTO.realName">
                  <el-input
                    v-model="formData.basicDTO.realName"
                    placeholder="请输入会员姓名"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="性别" prop="basicDTO.gender">
                  <el-radio-group v-model="formData.basicDTO.gender">
                    <el-radio :label="1">男</el-radio>
                    <el-radio :label="0">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="手机号码" prop="basicDTO.phone">
                  <el-input
                    v-model="formData.basicDTO.phone"
                    placeholder="请输入手机号码"
                    clearable
                    maxlength="11"
                    show-word-limit
                    :disabled="isEdit"
                    @blur="checkPhone"
                  />
                  <div v-if="phoneChecking" class="checking-text">检查中...</div>
                  <div v-if="phoneAvailable !== null && !isEdit" 
                       :class="['check-result', phoneAvailable ? 'success' : 'error']">
                    {{ phoneAvailable ? '手机号可用' : '手机号已存在' }}
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="出生日期" prop="basicDTO.birthday">
                  <el-date-picker
                    v-model="formData.basicDTO.birthday"
                    type="date"
                    placeholder="选择出生日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="会员编号" prop="basicDTO.memberNo">
                  <el-input
                    v-model="formData.basicDTO.memberNo"
                    placeholder="请输入会员编号"
                    clearable
                    :disabled="isEdit"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12" v-if="!isEdit">
                <el-form-item label="密码" prop="basicDTO.password">
                  <el-input
                    v-model="formData.basicDTO.password"
                    type="password"
                    placeholder="请输入密码（默认手机号后6位）"
                    show-password
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="会籍开始" prop="basicDTO.membershipStartDate">
                  <el-date-picker
                    v-model="formData.basicDTO.membershipStartDate"
                    type="date"
                    placeholder="选择开始日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="会籍结束" prop="basicDTO.membershipEndDate">
                  <el-date-picker
                    v-model="formData.basicDTO.membershipEndDate"
                    type="date"
                    placeholder="选择结束日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                    :disabled-date="disabledEndDate"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 健康信息 -->
        <el-tab-pane label="健康信息" name="health">
          <div class="form-section">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="身高(cm)" prop="healthRecordDTO.height">
                  <el-input-number
                    v-model="healthRecord.height"
                    :min="100"
                    :max="250"
                    :step="0.1"
                    controls-position="right"
                    placeholder="请输入身高"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="体重(kg)" prop="healthRecordDTO.weight">
                  <el-input-number
                    v-model="healthRecord.weight"
                    :min="30"
                    :max="200"
                    :step="0.1"
                    controls-position="right"
                    placeholder="请输入体重"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="记录日期" prop="healthRecordDTO.recordDate">
                  <el-date-picker
                    v-model="healthRecord.recordDate"
                    type="date"
                    placeholder="选择记录日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="BMI指数">
                  <el-tag :type="bmiType" size="large">
                    {{ bmiValue || '--' }}
                  </el-tag>
                  <span class="bmi-tip">{{ bmiTip }}</span>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="体脂率(%)" prop="healthRecordDTO.bodyFatPercentage">
                  <el-input-number
                    v-model="healthRecord.bodyFatPercentage"
                    :min="5"
                    :max="50"
                    :step="0.1"
                    controls-position="right"
                    placeholder="请输入体脂率"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="肌肉量(kg)" prop="healthRecordDTO.muscleMass">
                  <el-input-number
                    v-model="healthRecord.muscleMass"
                    :min="20"
                    :max="100"
                    :step="0.1"
                    controls-position="right"
                    placeholder="请输入肌肉量"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="胸围(cm)" prop="healthRecordDTO.chestCircumference">
                  <el-input-number
                    v-model="healthRecord.chestCircumference"
                    :min="50"
                    :max="150"
                    :step="0.1"
                    controls-position="right"
                    placeholder="请输入胸围"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="腰围(cm)" prop="healthRecordDTO.waistCircumference">
                  <el-input-number
                    v-model="healthRecord.waistCircumference"
                    :min="50"
                    :max="150"
                    :step="0.1"
                    controls-position="right"
                    placeholder="请输入腰围"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="臀围(cm)" prop="healthRecordDTO.hipCircumference">
                  <el-input-number
                    v-model="healthRecord.hipCircumference"
                    :min="50"
                    :max="150"
                    :step="0.1"
                    controls-position="right"
                    placeholder="请输入臀围"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="血压" prop="healthRecordDTO.bloodPressure">
                  <el-input
                    v-model="healthRecord.bloodPressure"
                    placeholder="如：120/80"
                    clearable
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="心率(bpm)" prop="healthRecordDTO.heartRate">
                  <el-input-number
                    v-model="healthRecord.heartRate"
                    :min="40"
                    :max="200"
                    :step="1"
                    controls-position="right"
                    placeholder="请输入心率"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="备注" prop="healthRecordDTO.notes">
              <el-input
                v-model="healthRecord.notes"
                type="textarea"
                :rows="3"
                placeholder="请输入健康状况备注"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </div>
        </el-tab-pane>

        <!-- 会员卡信息 -->
        <el-tab-pane label="会员卡信息" name="card">
          <div class="form-section">
            <!-- 添加会员卡按钮 -->
            <div v-if="!hasCard" class="add-card-placeholder">
              <el-empty description="暂无会员卡信息">
                <el-button type="primary" @click="handleAddCard">
                  <el-icon><Plus /></el-icon>
                  添加会员卡
                </el-button>
              </el-empty>
            </div>

            <!-- 会员卡表单 -->
            <div v-else class="card-form">
              <MemberCardSelector
                v-model="formData.cardDTO"
                :is-edit-mode="isEdit"
                @change="handleCardChange"
              />

              <!-- 移除会员卡按钮 -->
              <div class="card-actions">
                <el-button type="danger" text @click="handleRemoveCard">
                  <el-icon><Delete /></el-icon>
                  移除会员卡
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 表单操作 -->
      <div class="form-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import MemberCardSelector from './MemberCardSelector.vue'
import { useMemberStore } from '@/stores/member'
import type { 
  MemberAddRequest, 
  MemberUpdateRequest,
  MemberBasicDTO,
  HealthRecordDTO,
  MemberCardDTO 
} from '@/types/member'

interface Props {
  formData?: MemberAddRequest
  isEdit?: boolean
  memberId?: number
}

interface Emits {
  (e: 'submit', data: MemberAddRequest | MemberUpdateRequest): void
  (e: 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  isEdit: false
})

const emit = defineEmits<Emits>()

// Stores
const memberStore = useMemberStore()

// 表单引用
const formRef = ref<FormInstance>()

// 状态
const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('basic')
const phoneChecking = ref(false)
const phoneAvailable = ref<boolean | null>(null)

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
    membershipEndDate: ''
  },
  healthRecordDTO: undefined,
  cardDTO: undefined
})

// 健康记录（用于表单绑定）
const healthRecord = reactive<HealthRecordDTO>({
  recordDate: new Date().toISOString().split('T')[0],
  height: 170,
  weight: 65,
  bodyFatPercentage: 20,
  muscleMass: 45,
  chestCircumference: 95,
  waistCircumference: 85,
  hipCircumference: 95,
  bloodPressure: '120/80',
  heartRate: 75,
  notes: ''
})

// 是否有会员卡
const hasCard = computed(() => !!formData.cardDTO)

// 计算BMI
const bmiValue = computed(() => {
  if (healthRecord.height && healthRecord.weight) {
    const heightInM = healthRecord.height / 100
    const bmi = healthRecord.weight / (heightInM * heightInM)
    return bmi.toFixed(1)
  }
  return null
})

// BMI类型
const bmiType = computed(() => {
  if (!bmiValue.value) return 'info'
  const bmi = parseFloat(bmiValue.value)
  if (bmi < 18.5) return 'warning'
  if (bmi < 24) return 'success'
  if (bmi < 28) return 'warning'
  return 'danger'
})

// BMI提示
const bmiTip = computed(() => {
  if (!bmiValue.value) return ''
  const bmi = parseFloat(bmiValue.value)
  if (bmi < 18.5) return '偏瘦'
  if (bmi < 24) return '正常'
  if (bmi < 28) return '超重'
  return '肥胖'
})

// 表单验证规则
const formRules: FormRules = {
  'basicDTO.realName': [
    { required: true, message: '请输入会员姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在2-50个字符', trigger: 'blur' }
  ],
  'basicDTO.gender': [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  'basicDTO.phone': [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  'basicDTO.birthday': [
    { required: true, message: '请选择出生日期', trigger: 'change' }
  ],
  'basicDTO.memberNo': [
    { required: true, message: '请输入会员编号', trigger: 'blur' }
  ],
  'basicDTO.membershipStartDate': [
    { required: true, message: '请选择会籍开始日期', trigger: 'change' }
  ],
  'basicDTO.membershipEndDate': [
    { required: true, message: '请选择会籍结束日期', trigger: 'change' }
  ]
}

// 如果未编辑模式，添加密码验证
if (!props.isEdit) {
  formRules['basicDTO.password'] = [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ]
}

// 禁用结束日期（不能早于开始日期）
const disabledEndDate = (time: Date) => {
  if (!formData.basicDTO.membershipStartDate) return false
  const start = new Date(formData.basicDTO.membershipStartDate)
  return time.getTime() < start.getTime()
}

// 检查手机号
const checkPhone = async () => {
  const phone = formData.basicDTO.phone
  if (!phone || !/^1[3-9]\d{9}$/.test(phone) || props.isEdit) return
  
  phoneChecking.value = true
  try {
    // 这里需要调用API检查手机号是否存在
    // 暂时模拟
    await new Promise(resolve => setTimeout(resolve, 500))
    phoneAvailable.value = true
  } catch (error) {
    console.error('检查手机号失败:', error)
    phoneAvailable.value = null
  } finally {
    phoneChecking.value = false
  }
}

// 添加会员卡
const handleAddCard = () => {
  const today = new Date().toISOString().split('T')[0]
  const nextMonth = new Date()
  nextMonth.setMonth(nextMonth.getMonth() + 1)
  
  formData.cardDTO = {
    cardType: 2, // 默认月卡
    startDate: today,
    endDate: nextMonth.toISOString().split('T')[0],
    amount: 0
  }
}

// 移除会员卡
const handleRemoveCard = () => {
  formData.cardDTO = undefined
}

// 会员卡变化
const handleCardChange = (card: MemberCardDTO) => {
  console.log('会员卡变化:', card)
}

// 加载会员数据（编辑模式）
const loadMemberData = async () => {
  if (!props.isEdit || !props.memberId) return
  
  try {
    loading.value = true
    const member = await memberStore.fetchMemberDetail(props.memberId)
    
    if (member) {
      // 填充基本信息
      Object.assign(formData.basicDTO, {
        phone: member.phone,
        realName: member.realName,
        gender: member.gender,
        birthday: member.birthday || '',
        memberNo: member.memberNo,
        membershipStartDate: member.membershipStartDate || '',
        membershipEndDate: member.membershipEndDate || ''
      })
      
      // 填充健康记录（取最新一条）
      if (member.healthRecords && member.healthRecords.length > 0) {
        const latest = member.healthRecords[0]
        Object.assign(healthRecord, latest)
      }
      
      // 填充会员卡（取最新一张）
      if (member.memberCards && member.memberCards.length > 0) {
        const card = member.memberCards[0]
        formData.cardDTO = {
          productId: card.productId,
          productName: card.productName,
          cardType: card.cardType,
          startDate: card.startDate,
          endDate: card.endDate,
          totalSessions: card.totalSessions,
          remainingSessions: card.remainingSessions,
          amount: card.amount,
          status: card.status
        }
      }
    }
  } catch (error) {
    console.error('加载会员数据失败:', error)
    ElMessage.error('加载会员数据失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    // 检查手机号（新增时）
    if (!props.isEdit && phoneAvailable.value === false) {
      ElMessage.warning('手机号已存在，请更换手机号')
      return
    }
    
    submitting.value = true
    
    // 如果有健康记录数据，添加到表单
    if (healthRecord.height && healthRecord.weight) {
      formData.healthRecordDTO = { ...healthRecord }
    }
    
    // 提交数据
    emit('submit', formData)
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  emit('cancel')
}

// 监听身高体重变化，自动计算BMI
watch(
  () => [healthRecord.height, healthRecord.weight],
  () => {
    // BMI已在computed中自动计算
  }
)

// 初始化
onMounted(() => {
  if (props.isEdit && props.memberId) {
    loadMemberData()
  }
})

// 暴露方法
defineExpose({
  validate: () => formRef.value?.validate(),
  getFormData: () => formData
})
</script>

<style scoped lang="scss">
.member-form {
  .form-section {
    padding: 20px 0;
    
    .el-row {
      margin-bottom: 18px;
    }
  }
  
  .checking-text {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
  
  .check-result {
    font-size: 12px;
    margin-top: 4px;
    
    &.success {
      color: #67C23A;
    }
    
    &.error {
      color: #F56C6C;
    }
  }
  
  .bmi-tip {
    margin-left: 10px;
    color: #909399;
    font-size: 12px;
  }
  
  .add-card-placeholder {
    padding: 40px 0;
  }
  
  .card-form {
    .card-actions {
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid #e4e7ed;
      text-align: center;
    }
  }
  
  .form-actions {
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #e4e7ed;
    text-align: right;
    
    .el-button {
      min-width: 100px;
      margin-left: 10px;
    }
  }
}

:deep(.el-tabs__header) {
  margin-bottom: 20px;
}
</style>