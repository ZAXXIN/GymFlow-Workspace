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
      <el-form 
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="member-form"
      >
        <!-- 基本信息 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
            </div>
          </template>
          
          <div class="form-row">
            <el-form-item label="手机号" prop="phone" class="form-item">
              <el-input 
                v-model="formData.basicDTO.phone"
                placeholder="请输入手机号"
                maxlength="11"
                clearable
                @blur="checkPhoneAvailability"
                :disabled="isEditMode"
              />
              <div v-if="phoneChecking" class="checking-text">检查中...</div>
              <div v-if="phoneAvailable !== null && !isEditMode" 
                   :class="['check-result', phoneAvailable ? 'success' : 'error']">
                {{ phoneAvailable ? '手机号可用' : '手机号已存在' }}
              </div>
            </el-form-item>
            
            <el-form-item label="密码" prop="password" class="form-item" v-if="!isEditMode">
              <el-input 
                v-model="formData.basicDTO.password"
                type="password"
                placeholder="请输入密码（默认手机号后6位）"
                maxlength="255"
                clearable
                show-password
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="真实姓名" prop="realName" class="form-item">
              <el-input 
                v-model="formData.basicDTO.realName"
                placeholder="请输入真实姓名"
                maxlength="50"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="性别" prop="gender" class="form-item">
              <el-radio-group v-model="formData.basicDTO.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="出生日期" prop="birthday" class="form-item">
              <el-date-picker
                v-model="formData.basicDTO.birthday"
                type="date"
                placeholder="选择出生日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="身份证号" prop="idCard" class="form-item">
              <el-input 
                v-model="formData.basicDTO.idCard"
                placeholder="请输入身份证号"
                maxlength="20"
                clearable
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="身高(cm)" prop="height" class="form-item">
              <el-input-number
                v-model="formData.basicDTO.height"
                :min="100"
                :max="250"
                :step="1"
                controls-position="right"
                style="width: 100%"
                placeholder="请输入身高"
              />
            </el-form-item>
            
            <el-form-item label="体重(kg)" prop="weight" class="form-item">
              <el-input-number
                v-model="formData.basicDTO.weight"
                :min="30"
                :max="200"
                :step="0.1"
                controls-position="right"
                style="width: 100%"
                placeholder="请输入体重"
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="专属教练" prop="personalCoachId" class="form-item">
              <el-select 
                v-model="formData.basicDTO.personalCoachId"
                placeholder="请选择专属教练"
                style="width: 100%"
                clearable
                filterable
              >
                <el-option
                  v-for="coach in coachList"
                  :key="coach.id"
                  :label="coach.realName + ' - ' + coach.phone"
                  :value="coach.id"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="地址" prop="address" class="form-item">
              <el-input 
                v-model="formData.basicDTO.address"
                placeholder="请输入地址"
                maxlength="200"
                clearable
              />
            </el-form-item>
          </div>
        </el-card>

        <!-- 健康档案 -->
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
              <el-form-item label="记录日期" prop="recordDate" class="form-item">
                <el-date-picker
                  v-model="formData.healthRecordDTO.recordDate"
                  type="date"
                  placeholder="选择记录日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
              
              <el-form-item label="体重(kg)" prop="weight" class="form-item">
                <el-input-number
                  v-model="formData.healthRecordDTO.weight"
                  :min="30"
                  :max="200"
                  :step="0.1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="体脂率(%)" prop="bodyFatPercentage" class="form-item">
                <el-input-number
                  v-model="formData.healthRecordDTO.bodyFatPercentage"
                  :min="5"
                  :max="50"
                  :step="0.1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
              
              <el-form-item label="肌肉量(kg)" prop="muscleMass" class="form-item">
                <el-input-number
                  v-model="formData.healthRecordDTO.muscleMass"
                  :min="20"
                  :max="100"
                  :step="0.1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="BMI指数" prop="bmi" class="form-item">
                <el-input 
                  v-model="bmiValue"
                  :disabled="true"
                  placeholder="自动计算"
                >
                  <template #append>
                    <span v-if="bmiValue" class="bmi-status" :class="getBmiStatus(bmiValue).className">
                      {{ getBmiStatus(bmiValue).text }}
                    </span>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="血压" prop="bloodPressure" class="form-item">
                <el-input 
                  v-model="formData.healthRecordDTO.bloodPressure"
                  placeholder="如：120/80"
                  clearable
                />
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="胸围(cm)" prop="chestCircumference" class="form-item">
                <el-input-number
                  v-model="formData.healthRecordDTO.chestCircumference"
                  :min="50"
                  :max="150"
                  :step="0.1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
              
              <el-form-item label="腰围(cm)" prop="waistCircumference" class="form-item">
                <el-input-number
                  v-model="formData.healthRecordDTO.waistCircumference"
                  :min="50"
                  :max="150"
                  :step="0.1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="臀围(cm)" prop="hipCircumference" class="form-item">
                <el-input-number
                  v-model="formData.healthRecordDTO.hipCircumference"
                  :min="50"
                  :max="150"
                  :step="0.1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
              
              <el-form-item label="心率(bpm)" prop="heartRate" class="form-item">
                <el-input-number
                  v-model="formData.healthRecordDTO.heartRate"
                  :min="40"
                  :max="200"
                  :step="1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </div>
            
            <el-form-item label="备注" prop="notes" class="full-width">
              <el-input
                v-model="formData.healthRecordDTO.notes"
                type="textarea"
                :rows="3"
                placeholder="请输入健康状况备注"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            
            <div class="health-actions">
              <el-button type="danger" text @click="handleRemoveHealthRecord">
                移除健康记录
              </el-button>
            </div>
          </div>
          
          <div v-else class="no-health-record">
            <el-empty description="暂无健康档案信息" :image-size="80">
              <el-button type="primary" @click="handleAddHealthRecord">添加健康记录</el-button>
            </el-empty>
          </div>
        </el-card>

        <!-- 会员卡信息 -->
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
            <div class="form-row">
              <el-form-item label="卡类型" prop="cardType" class="form-item">
                <el-select 
                  v-model="formData.cardDTO.cardType"
                  placeholder="请选择卡类型"
                  style="width: 100%"
                  @change="handleCardTypeChange"
                >
                  <el-option label="私教课" :value="0" />
                  <el-option label="团课" :value="1" />
                  <el-option label="月卡" :value="2" />
                  <el-option label="年卡" :value="3" />
                  <el-option label="周卡" :value="4" />
                  <el-option label="其他" :value="5" />
                </el-select>
              </el-form-item>
              
              <!-- <el-form-item label="卡名称" prop="cardName" class="form-item">
                <el-input 
                  v-model="formData.cardDTO.cardName"
                  placeholder="请输入卡名称"
                  clearable
                />
              </el-form-item> -->
            </div>
            
            <!-- 会籍卡（月卡、年卡、周卡）显示时间 -->
            <div v-if="isMembershipCard(formData.cardDTO.cardType)" class="card-fields">
              <div class="form-row">
                <el-form-item label="开始日期" prop="startDate" class="form-item">
                  <el-date-picker
                    v-model="formData.cardDTO.startDate"
                    type="date"
                    placeholder="选择开始日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
                
                <el-form-item label="结束日期" prop="endDate" class="form-item">
                  <el-date-picker
                    v-model="formData.cardDTO.endDate"
                    type="date"
                    placeholder="选择结束日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </div>
              
              <el-form-item label="天数" class="form-item full-width">
                <el-input 
                  :value="calculateDays"
                  :disabled="true"
                  placeholder="自动计算"
                >
                  <template #append>天</template>
                </el-input>
              </el-form-item>
            </div>
            
            <!-- 课程卡（私教课、团课）显示课时数 -->
            <div v-else-if="isCourseCard(formData.cardDTO.cardType)" class="card-fields">
              <div class="form-row">
                <el-form-item label="总课时数" prop="totalSessions" class="form-item">
                  <el-input-number
                    v-model="formData.cardDTO.totalSessions"
                    :min="1"
                    :max="999"
                    :step="1"
                    controls-position="right"
                    style="width: 100%"
                    placeholder="请输入总课时数"
                  />
                </el-form-item>
                
                <el-form-item label="剩余课时" prop="remainingSessions" class="form-item">
                  <el-input-number
                    v-model="formData.cardDTO.remainingSessions"
                    :min="0"
                    :max="formData.cardDTO.totalSessions || 999"
                    :step="1"
                    controls-position="right"
                    style="width: 100%"
                    placeholder="请输入剩余课时"
                  />
                </el-form-item>
              </div>
              
              <div class="form-row">
                <el-form-item label="已用课时" class="form-item">
                  <el-input 
                    :value="calculateUsedSessions"
                    :disabled="true"
                    placeholder="自动计算"
                  />
                </el-form-item>
                
                <el-form-item label="商品ID" prop="productId" class="form-item" v-if="isEditMode">
                  <el-input 
                    v-model="formData.cardDTO.productId"
                    :disabled="true"
                    placeholder="系统分配"
                  />
                </el-form-item>
              </div>
            </div>
            
            <!-- 其他卡类型 -->
            <div v-else class="card-fields">
              <div class="form-row">
                <el-form-item label="开始日期" prop="startDate" class="form-item">
                  <el-date-picker
                    v-model="formData.cardDTO.startDate"
                    type="date"
                    placeholder="选择开始日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
                
                <el-form-item label="结束日期" prop="endDate" class="form-item">
                  <el-date-picker
                    v-model="formData.cardDTO.endDate"
                    type="date"
                    placeholder="选择结束日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </div>
            </div>
            
            <div class="form-row">
              <el-form-item label="金额" prop="amount" class="form-item">
                <el-input-number
                  v-model="formData.cardDTO.amount"
                  :min="0"
                  :step="100"
                  :precision="2"
                  controls-position="right"
                  style="width: 100%"
                >
                  <template #prepend>¥</template>
                </el-input-number>
              </el-form-item>
              
              <el-form-item label="商品ID" prop="productId" class="form-item" v-if="isEditMode">
                <el-input 
                  v-model="formData.cardDTO.productId"
                  :disabled="true"
                  placeholder="系统分配"
                />
              </el-form-item>
            </div>
            
            <div class="card-actions">
              <el-button type="danger" text @click="handleRemoveCard">
                移除会员卡
              </el-button>
            </div>
          </div>
          
          <div v-else class="no-card">
            <el-empty description="暂无会员卡信息" :image-size="80">
              <el-button type="primary" @click="handleAddCard">添加会员卡</el-button>
            </el-empty>
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
import type { 
  MemberAddRequest, 
  MemberUpdateRequest,
  MemberBasicDTO,
  HealthRecordDTO,
  MemberCardDTO
} from '@/types/member'
import type { Coach } from '@/types/coach'

const router = useRouter()
const route = useRoute()
const memberStore = useMemberStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEditMode = computed(() => !!route.params.id)
const coachList = ref<Coach[]>([])

// 表单数据
const formData = reactive<MemberAddRequest>({
  basicDTO: {
    phone: '',
    password: '',
    realName: '',
    gender: 1,
    birthday: '',
    idCard: '',
    height: undefined,
    weight: undefined,
    personalCoachId: undefined,
    address: ''
  },
  healthRecordDTO: undefined,
  cardDTO: undefined
})

// 手机号检查
const phoneChecking = ref(false)
const phoneAvailable = ref<boolean | null>(null)

// 表单验证规则
const formRules: FormRules = {
  basicDTO: {
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ],
    password: [
      { required: !isEditMode.value, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 255, message: '密码长度至少6个字符', trigger: 'blur' }
    ],
    realName: [
      { required: true, message: '请输入真实姓名', trigger: 'blur' },
      { min: 2, max: 50, message: '姓名长度在2-50个字符', trigger: 'blur' }
    ],
    gender: [
      { required: true, message: '请选择性别', trigger: 'change' }
    ]
  }
}

// 计算BMI
const bmiValue = computed(() => {
  if (formData.basicDTO.height && formData.basicDTO.weight) {
    const heightInM = formData.basicDTO.height / 100
    const bmi = formData.basicDTO.weight / (heightInM * heightInM)
    return bmi.toFixed(1)
  }
  return ''
})

// 获取BMI状态
const getBmiStatus = (bmi: string) => {
  const bmiNum = parseFloat(bmi)
  if (bmiNum < 18.5) return { text: '偏瘦', className: 'bmi-underweight' }
  if (bmiNum < 24) return { text: '正常', className: 'bmi-normal' }
  if (bmiNum < 28) return { text: '超重', className: 'bmi-overweight' }
  return { text: '肥胖', className: 'bmi-obese' }
}

// 判断是否为会籍卡
const isMembershipCard = (cardType: number | undefined) => {
  return cardType !== undefined && (cardType === 2 || cardType === 3 || cardType === 4)
}

// 判断是否为课程卡
const isCourseCard = (cardType: number | undefined) => {
  return cardType !== undefined && (cardType === 0 || cardType === 1)
}

// 计算有效期天数
const calculateDays = computed(() => {
  if (!formData.cardDTO?.startDate || !formData.cardDTO?.endDate) return 0
  
  const start = new Date(formData.cardDTO.startDate)
  const end = new Date(formData.cardDTO.endDate)
  const diffTime = end.getTime() - start.getTime()
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
})

// 计算已用课时
const calculateUsedSessions = computed(() => {
  if (!formData.cardDTO?.totalSessions || !formData.cardDTO?.remainingSessions) return 0
  return formData.cardDTO.totalSessions - formData.cardDTO.remainingSessions
})

// 检查手机号是否可用
const checkPhoneAvailability = async () => {
  const phone = formData.basicDTO.phone
  if (!phone || !/^1[3-9]\d{9}$/.test(phone)) return
  
  phoneChecking.value = true
  try {
    const exists = await memberStore.checkPhoneExists(phone)
    phoneAvailable.value = !exists
  } catch (error) {
    console.error('检查手机号失败:', error)
    phoneAvailable.value = null
  } finally {
    phoneChecking.value = false
  }
}

// 添加健康记录
const handleAddHealthRecord = () => {
  formData.healthRecordDTO = {
    recordDate: new Date().toISOString().split('T')[0],
    weight: formData.basicDTO.weight || 65,
    bodyFatPercentage: 20,
    muscleMass: 45,
    bmi: 22.5,
    chestCircumference: 95,
    waistCircumference: 85,
    hipCircumference: 95,
    bloodPressure: '120/80',
    heartRate: 75,
    notes: ''
  }
}

// 移除健康记录
const handleRemoveHealthRecord = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要移除健康记录吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    formData.healthRecordDTO = undefined
  } catch {
    // 用户取消
  }
}

// 添加会员卡
const handleAddCard = () => {
  const today = new Date().toISOString().split('T')[0]
  const nextMonth = new Date()
  nextMonth.setMonth(nextMonth.getMonth() + 1)
  const nextMonthDate = nextMonth.toISOString().split('T')[0]
  
  formData.cardDTO = {
    // cardName: '健身会员卡',
    cardType: 2, // 默认为月卡
    startDate: today,
    endDate: nextMonthDate,
    amount: 500.00
  }
}

// 移除会员卡
const handleRemoveCard = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要移除会员卡吗？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    formData.cardDTO = undefined
  } catch {
    // 用户取消
  }
}

// 卡类型改变处理
const handleCardTypeChange = (cardType: number) => {
  if (!formData.cardDTO) return
  
  // 重置相关字段
  if (isCourseCard(cardType)) {
    formData.cardDTO.startDate = undefined
    formData.cardDTO.endDate = undefined
    formData.cardDTO.totalSessions = 10
    formData.cardDTO.remainingSessions = 10
  } else if (isMembershipCard(cardType)) {
    const today = new Date().toISOString().split('T')[0]
    const endDate = new Date()
    
    switch (cardType) {
      case 2: // 月卡
        endDate.setMonth(endDate.getMonth() + 1)
        break
      case 3: // 年卡
        endDate.setFullYear(endDate.getFullYear() + 1)
        break
      case 4: // 周卡
        endDate.setDate(endDate.getDate() + 7)
        break
    }
    
    formData.cardDTO.startDate = today
    formData.cardDTO.endDate = endDate.toISOString().split('T')[0]
    formData.cardDTO.totalSessions = undefined
    formData.cardDTO.remainingSessions = undefined
  }
}

// 加载教练列表
const loadCoaches = async () => {
  try {
    // 这里需要调用教练API获取教练列表
    // 暂时使用模拟数据
    coachList.value = [
      { id: 1, realName: '张教练', phone: '13800138001', status: 1 },
      { id: 2, realName: '李教练', phone: '13800138002', status: 1 },
      { id: 3, realName: '王教练', phone: '13800138003', status: 1 }
    ]
  } catch (error) {
    console.error('加载教练列表失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    // 表单验证
    await formRef.value.validate()
    
    // 检查手机号（新增时）
    if (!isEditMode.value && phoneAvailable.value === false) {
      ElMessage.warning('手机号已存在，请更换手机号')
      return
    }
    
    loading.value = true
    
    if (isEditMode.value) {
      // 更新会员
      const updateData: MemberUpdateRequest = {
        basicDTO: { ...formData.basicDTO },
        healthRecordDTO: formData.healthRecordDTO,
        cardDTO: formData.cardDTO
      }
      // 编辑模式下不需要密码
      delete updateData.basicDTO.password
      
      await memberStore.updateMember(Number(route.params.id), updateData)
      ElMessage.success('会员信息更新成功')
    } else {
      // 新增会员
      await memberStore.addMember(formData)
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
      // 填充基本信息
      formData.basicDTO = {
        phone: memberDetail.phone,
        password: '', // 编辑时不显示密码
        realName: memberDetail.realName,
        gender: memberDetail.gender,
        birthday: memberDetail.birthday,
        idCard: memberDetail.idCard,
        height: memberDetail.height,
        weight: memberDetail.weight,
        personalCoachId: memberDetail.personalCoachId,
        address: memberDetail.address
      }
      
      // 如果有健康记录，取最新一条
      if (memberDetail.healthRecords && memberDetail.healthRecords.length > 0) {
        formData.healthRecordDTO = memberDetail.healthRecords[0]
      }
      
      // 如果有会员卡，取最新一张
      if (memberDetail.memberCards && memberDetail.memberCards.length > 0) {
        const card = memberDetail.memberCards[0]
        formData.cardDTO = {
          productId: card.productId,
          // cardName: card.cardName,
          cardType: card.cardType,
          startDate: card.startDate,
          endDate: card.endDate,
          totalSessions: card.totalSessions,
          remainingSessions: card.remainingSessions,
          amount: card.amount
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

// 监听身高体重变化计算BMI
watch(
  () => [formData.basicDTO.height, formData.basicDTO.weight],
  () => {
    // BMI计算已在computed属性中自动处理
  }
)

onMounted(() => {
  initFormData()
  loadCoaches()
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
  color: #67C23A;
}

.check-result.error {
  color: #F56C6C;
}

.bmi-status {
  font-size: 12px;
  font-weight: 500;
}

.bmi-underweight {
  color: #E6A23C;
}

.bmi-normal {
  color: #67C23A;
}

.bmi-overweight {
  color: #E6A23C;
}

.bmi-obese {
  color: #F56C6C;
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

.card-fields {
  margin-bottom: 20px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  background-color: #fafafa;
}

:deep(.el-input-number) {
  width: 100%;
}
</style>