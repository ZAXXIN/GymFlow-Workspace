<template>
  <div class="member-form-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/member' }">会员管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ formData.id ? '编辑会员' : '新增会员' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">{{ formData.id ? '编辑会员信息' : '新增会员' }}</h1>
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
        label-width="100px"
        class="member-form"
      >
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
            </div>
          </template>
          
          <div class="form-row">
            <el-form-item label="会员姓名" prop="name" class="form-item">
              <el-input 
                v-model="formData.name"
                placeholder="请输入会员姓名"
                maxlength="20"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="手机号" prop="phone" class="form-item">
              <el-input 
                v-model="formData.phone"
                placeholder="请输入手机号"
                maxlength="11"
                clearable
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="性别" prop="gender" class="form-item">
              <el-radio-group v-model="formData.gender">
                <el-radio label="MALE">男</el-radio>
                <el-radio label="FEMALE">女</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="出生日期" prop="birthday" class="form-item">
              <el-date-picker
                v-model="formData.birthday"
                type="date"
                placeholder="选择出生日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="邮箱" prop="email" class="form-item">
              <el-input 
                v-model="formData.email"
                placeholder="请输入邮箱"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="会员状态" prop="status" class="form-item">
              <el-select v-model="formData.status" placeholder="请选择会员状态" style="width: 100%">
                <el-option 
                  v-for="item in memberStatusOptions" 
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
          
          <el-form-item label="联系地址" prop="address" class="full-width">
            <el-input 
              v-model="formData.address"
              type="textarea"
              :rows="2"
              placeholder="请输入联系地址"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-card>

        <!-- 健康信息 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">健康信息</span>
            </div>
          </template>
          
          <div class="form-row">
            <el-form-item label="身高(cm)" prop="height" class="form-item">
              <el-input-number
                v-model="formData.height"
                :min="100"
                :max="250"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="体重(kg)" prop="weight" class="form-item">
              <el-input-number
                v-model="formData.weight"
                :min="30"
                :max="200"
                :step="0.1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="BMI指数" prop="bmi" class="form-item">
              <el-input 
                v-model="formData.bmi"
                :disabled="true"
                placeholder="自动计算"
              >
                <template #append>
                  <span v-if="formData.bmi" class="bmi-status" :class="getBmiStatus(formData.bmi).className">
                    {{ getBmiStatus(formData.bmi).text }}
                  </span>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item label="体脂率(%)" prop="bodyFatRate" class="form-item">
              <el-input-number
                v-model="formData.bodyFatRate"
                :min="5"
                :max="50"
                :step="0.1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          
          <el-form-item label="健康状况备注" prop="healthNote" class="full-width">
            <el-input
              v-model="formData.healthNote"
              type="textarea"
              :rows="3"
              placeholder="请输入健康状况备注"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-card>

        <!-- 会员卡信息 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">会员卡信息</span>
              <el-button type="primary" text @click="handleAddCard" v-if="!formData.memberCard">
                添加会员卡
              </el-button>
            </div>
          </template>
          
          <div v-if="formData.memberCard" class="card-info">
            <div class="form-row">
              <el-form-item label="卡类型" prop="memberCard.cardType" class="form-item">
                <el-select 
                  v-model="formData.memberCard.cardType"
                  placeholder="请选择卡类型"
                  style="width: 100%"
                >
                  <el-option label="年卡" value="YEAR" />
                  <el-option label="季卡" value="QUARTER" />
                  <el-option label="月卡" value="MONTH" />
                  <el-option label="次卡" value="TIMES" />
                  <el-option label="私教卡" value="PERSONAL" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="卡号" prop="memberCard.cardNumber" class="form-item">
                <el-input 
                  v-model="formData.memberCard.cardNumber"
                  placeholder="请输入卡号"
                  :disabled="!!formData.id"
                />
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="开卡日期" prop="memberCard.startDate" class="form-item">
                <el-date-picker
                  v-model="formData.memberCard.startDate"
                  type="date"
                  placeholder="选择开卡日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
              
              <el-form-item label="到期日期" prop="memberCard.endDate" class="form-item">
                <el-date-picker
                  v-model="formData.memberCard.endDate"
                  type="date"
                  placeholder="选择到期日期"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                />
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="剩余次数" prop="memberCard.remainingTimes" class="form-item">
                <el-input-number
                  v-model="formData.memberCard.remainingTimes"
                  :min="0"
                  :max="999"
                  :step="1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
              
              <el-form-item label="总次数" prop="memberCard.totalTimes" class="form-item">
                <el-input-number
                  v-model="formData.memberCard.totalTimes"
                  :min="1"
                  :max="999"
                  :step="1"
                  controls-position="right"
                  style="width: 100%"
                />
              </el-form-item>
            </div>
            
            <el-form-item label="备注" prop="memberCard.note" class="full-width">
              <el-input
                v-model="formData.memberCard.note"
                type="textarea"
                :rows="2"
                placeholder="请输入会员卡备注"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { MemberFormData, MemberCard } from '@/types'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const loading = ref(false)

// 表单数据
const formData = reactive<MemberFormData>({
  id: undefined,
  name: '',
  phone: '',
  gender: 'MALE',
  birthday: '',
  email: '',
  status: 'ACTIVE',
  address: '',
  height: 170,
  weight: 65,
  bmi: '',
  bodyFatRate: 20,
  healthNote: '',
  memberCard: null
})

// 会员状态选项
const memberStatusOptions = [
  { label: '活跃', value: 'ACTIVE' },
  { label: '冻结', value: 'FROZEN' },
  { label: '过期', value: 'EXPIRED' },
  { label: '注销', value: 'CANCELLED' }
]

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入会员姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入正确的邮箱', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择会员状态', trigger: 'change' }
  ],
  height: [
    { required: true, message: '请输入身高', trigger: 'blur' }
  ],
  weight: [
    { required: true, message: '请输入体重', trigger: 'blur' }
  ]
}

// 计算BMI
const calculateBMI = () => {
  if (formData.height && formData.weight) {
    const heightInM = formData.height / 100
    const bmiValue = formData.weight / (heightInM * heightInM)
    formData.bmi = bmiValue.toFixed(1)
  }
}

// 获取BMI状态
const getBmiStatus = (bmi: string) => {
  const bmiNum = parseFloat(bmi)
  if (bmiNum < 18.5) return { text: '偏瘦', className: 'bmi-underweight' }
  if (bmiNum < 24) return { text: '正常', className: 'bmi-normal' }
  if (bmiNum < 28) return { text: '超重', className: 'bmi-overweight' }
  return { text: '肥胖', className: 'bmi-obese' }
}

// 监听身高体重变化
watch([() => formData.height, () => formData.weight], () => {
  calculateBMI()
})

// 添加会员卡
const handleAddCard = () => {
  formData.memberCard = {
    cardType: 'MONTH',
    cardNumber: `MC${Date.now().toString().slice(-8)}`,
    startDate: new Date().toISOString().split('T')[0],
    endDate: '',
    remainingTimes: 0,
    totalTimes: 12,
    note: ''
  }
}

// 移除会员卡
const handleRemoveCard = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要移除会员卡吗？移除后相关数据将无法恢复。',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    formData.memberCard = null
    ElMessage.success('会员卡已移除')
  } catch {
    // 用户取消操作
  }
}

// 保存表单
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    // 这里调用API保存数据
    // await saveMember(formData)
    
    ElMessage.success(formData.id ? '会员信息更新成功' : '会员创建成功')
    router.push('/member')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请检查表单')
  } finally {
    loading.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  router.push('/member/list')
}

// 初始化数据（编辑模式）
const initFormData = () => {
  const id = route.params.id
  if (id) {
    // 编辑模式：从API获取数据
    // const data = await getMemberDetail(id)
    // Object.assign(formData, data)
    
    // 模拟数据
    Object.assign(formData, {
      id: id,
      name: '张三',
      phone: '13800138000',
      gender: 'MALE',
      birthday: '1990-01-01',
      email: 'zhangsan@example.com',
      status: 'ACTIVE',
      address: '北京市朝阳区',
      height: 175,
      weight: 70,
      bodyFatRate: 18.5,
      healthNote: '无特殊病史',
      memberCard: {
        cardType: 'YEAR',
        cardNumber: 'MC20230001',
        startDate: '2023-01-01',
        endDate: '2024-01-01',
        remainingTimes: 50,
        totalTimes: 100,
        note: '年度会员卡'
      }
    })
    calculateBMI()
  }
}

onMounted(() => {
  initFormData()
})
</script>

<style scoped lang="scss">
.member-form-container {
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

.card-info {
  .card-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
  }
}

.no-card {
  padding: 40px 0;
  text-align: center;
}

.bmi-status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;

  &.bmi-underweight {
    background: #f0f9ff;
    color: #409eff;
  }

  &.bmi-normal {
    background: #f0f9eb;
    color: #67c23a;
  }

  &.bmi-overweight {
    background: #fef0f0;
    color: #f56c6c;
  }

  &.bmi-obese {
    background: #f56c6c;
    color: #fff;
  }
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-input-group__append) {
  padding: 0 10px;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 20px;
}
</style>