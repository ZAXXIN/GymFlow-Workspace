<template>
  <div class="coach-form-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/coach' }">教练管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ isEditMode ? '编辑教练' : '新增教练' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">{{ isEditMode ? '编辑教练信息' : '新增教练' }}</h1>
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
        class="coach-form"
      >
        <!-- 基本信息 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
            </div>
          </template>
          
          <div class="form-row">
            <el-form-item label="姓名" prop="realName" class="form-item">
              <el-input 
                v-model="formData.realName"
                placeholder="请输入教练姓名"
                maxlength="50"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="手机号" prop="phone" class="form-item">
              <el-input 
                v-model="formData.phone"
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
          </div>
          
          <div class="form-row" v-if="!isEditMode">
            <el-form-item label="密码" prop="password" class="form-item">
              <el-input 
                v-model="formData.password"
                type="password"
                placeholder="请输入密码（默认手机号后6位）"
                maxlength="255"
                clearable
                show-password
              />
            </el-form-item>
            
            <el-form-item label="确认密码" prop="confirmPassword" class="form-item" v-if="!isEditMode">
              <el-input 
                v-model="confirmPassword"
                type="password"
                placeholder="请确认密码"
                maxlength="255"
                clearable
                show-password
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="专长领域" prop="specialty" class="form-item">
              <el-input 
                v-model="formData.specialty"
                placeholder="请输入教练专长，如：增肌、减脂、瑜伽等"
                maxlength="100"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="经验年限" prop="yearsOfExperience" class="form-item">
              <el-input-number
                v-model="formData.yearsOfExperience"
                :min="0"
                :max="50"
                :step="1"
                controls-position="right"
                style="width: 100%"
                placeholder="请输入经验年限"
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="时薪" prop="hourlyRate" class="form-item">
              <el-input-number
                v-model="formData.hourlyRate"
                :min="0"
                :step="10"
                :precision="2"
                controls-position="right"
                style="width: 100%"
              >
                <template #prepend>¥</template>
              </el-input-number>
            </el-form-item>
            
            <el-form-item label="提成比例" prop="commissionRate" class="form-item">
              <el-input-number
                v-model="formData.commissionRate"
                :min="0"
                :max="100"
                :step="0.5"
                controls-position="right"
                style="width: 100%"
                placeholder="请输入提成比例"
              >
                <template #append>%</template>
              </el-input-number>
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="资格证书" prop="certificationList" class="full-width">
              <el-select
                v-model="formData.certificationList"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="请选择或输入资格证书"
                style="width: 100%"
              >
                <el-option
                  v-for="cert in certificationOptions"
                  :key="cert"
                  :label="cert"
                  :value="cert"
                />
              </el-select>
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="个人简介" prop="introduction" class="full-width">
              <el-input
                v-model="formData.introduction"
                type="textarea"
                :rows="4"
                placeholder="请输入教练个人简介"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
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
import { useCoachStore } from '@/stores/coach'
import type { CoachBasicDTO } from '@/types/coach'

const router = useRouter()
const route = useRoute()
const coachStore = useCoachStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEditMode = computed(() => !!route.params.id)

// 表单数据
const formData = reactive<CoachBasicDTO>({
  realName: '',
  phone: '',
  password: '',
  specialty: '',
  certificationList: [],
  yearsOfExperience: 0,
  hourlyRate: 0,
  commissionRate: 0,
  introduction: ''
})

const confirmPassword = ref('')

// 手机号检查
const phoneChecking = ref(false)
const phoneAvailable = ref<boolean | null>(null)

// 资格证书选项
const certificationOptions = ref([
  '国家健身教练资格证',
  'NASM-CPT',
  'ACE-CPT',
  'NSCA-CPT',
  'AFAA',
  '瑜伽教练资格证',
  '普拉提教练证',
  '营养师资格证'
])

// 表单验证规则
const formRules: FormRules = {
  realName: [
    { required: true, message: '请输入教练姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在2-50个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: !isEditMode.value, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 255, message: '密码长度至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: !isEditMode.value, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!isEditMode.value && value !== formData.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  yearsOfExperience: [
    { required: true, message: '请输入经验年限', trigger: 'blur' }
  ],
  hourlyRate: [
    { required: true, message: '请输入时薪', trigger: 'blur' },
    { type: 'number', min: 0, message: '时薪不能为负数', trigger: 'blur' }
  ],
  commissionRate: [
    { required: true, message: '请输入提成比例', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '提成比例在0-100之间', trigger: 'blur' }
  ]
}

// 检查手机号是否可用（模拟）
const checkPhoneAvailability = async () => {
  const phone = formData.phone
  if (!phone || !/^1[3-9]\d{9}$/.test(phone)) return
  
  phoneChecking.value = true
  try {
    // 模拟检查手机号是否已存在
    await new Promise(resolve => setTimeout(resolve, 500))
    // 这里应该调用API检查手机号是否存在
    phoneAvailable.value = Math.random() > 0.5 // 模拟50%概率可用
  } catch (error) {
    console.error('检查手机号失败:', error)
    phoneAvailable.value = null
  } finally {
    phoneChecking.value = false
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
      // 更新教练
      await coachStore.updateCoach(Number(route.params.id), formData)
      ElMessage.success('教练信息更新成功')
    } else {
      // 新增教练
      await coachStore.addCoach(formData)
      ElMessage.success('教练创建成功')
    }
    
    router.push('/coach/list')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请检查表单')
  } finally {
    loading.value = false
  }
}

// 取消
const handleCancel = () => {
  router.push('/coach/list')
}

// 初始化表单数据（编辑模式）
const initFormData = async () => {
  if (!isEditMode.value) return
  
  try {
    loading.value = true
    const coachId = Number(route.params.id)
    await coachStore.fetchCoachDetail(coachId)
    
    if (coachStore.currentCoach) {
      const coach = coachStore.currentCoach
      formData.realName = coach.realName
      formData.phone = coach.phone
      formData.password = '' // 编辑时不显示密码
      formData.specialty = coach.specialty || ''
      formData.certificationList = coach.certificationList || []
      formData.yearsOfExperience = coach.yearsOfExperience
      formData.hourlyRate = coach.hourlyRate
      formData.commissionRate = coach.commissionRate
      formData.introduction = coach.introduction || ''
    }
  } catch (error) {
    console.error('加载教练详情失败:', error)
    ElMessage.error('加载教练信息失败')
  } finally {
    loading.value = false
  }
}

// 监听手机号变化
watch(() => formData.phone, (newVal) => {
  if (!isEditMode.value && newVal && newVal.length === 11) {
    // 如果是新增模式，设置默认密码为手机号后6位
    formData.password = newVal.slice(-6)
    confirmPassword.value = newVal.slice(-6)
  }
})

onMounted(() => {
  initFormData()
})
</script>

<style scoped>
.coach-form-container {
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

:deep(.el-card__header) {
  padding: 16px 20px;
  background-color: #fafafa;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-select) {
  width: 100%;
}
</style>