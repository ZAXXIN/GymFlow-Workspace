<template>
  <div class="member-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      label-position="left"
    >
      <el-tabs v-model="activeTab">
        <!-- 基础信息 -->
        <el-tab-pane label="基础信息" name="basic">
          <div class="form-section">
            <el-form-item label="会员姓名" prop="name">
              <el-input
                v-model="formData.name"
                placeholder="请输入会员姓名"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio label="MALE">男</el-radio>
                <el-radio label="FEMALE">女</el-radio>
                <el-radio label="UNKNOWN">保密</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="手机号码" prop="phone">
              <el-input
                v-model="formData.phone"
                placeholder="请输入手机号码"
                clearable
                maxlength="11"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="formData.email"
                placeholder="请输入邮箱地址"
                clearable
                type="email"
              />
            </el-form-item>
            
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker
                v-model="formData.birthDate"
                type="date"
                placeholder="选择出生日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="身份证号" prop="idCard">
              <el-input
                v-model="formData.idCard"
                placeholder="请输入身份证号"
                clearable
                maxlength="18"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="联系地址" prop="address">
              <el-input
                v-model="formData.address"
                type="textarea"
                placeholder="请输入联系地址"
                :rows="3"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </div>
        </el-tab-pane>
        
        <!-- 健康信息 -->
        <el-tab-pane label="健康信息" name="health">
          <div class="form-section">
            <el-form-item label="身高(cm)" prop="height">
              <el-input-number
                v-model="formData.height"
                :min="100"
                :max="250"
                :step="0.1"
                controls-position="right"
                placeholder="请输入身高"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="体重(kg)" prop="weight">
              <el-input-number
                v-model="formData.weight"
                :min="30"
                :max="200"
                :step="0.1"
                controls-position="right"
                placeholder="请输入体重"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="BMI指数">
              <el-tag :type="getBMIType(formData.bmi)">
                {{ formData.bmi || '--' }}
              </el-tag>
              <span class="bmi-tips">{{ getBMITips(formData.bmi) }}</span>
            </el-form-item>
            
            <el-form-item label="体脂率(%)" prop="bodyFatPercentage">
              <el-input-number
                v-model="formData.bodyFatPercentage"
                :min="5"
                :max="50"
                :step="0.1"
                controls-position="right"
                placeholder="请输入体脂率"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="健康状况" prop="healthStatus">
              <el-select
                v-model="formData.healthStatus"
                placeholder="请选择健康状况"
                clearable
                style="width: 100%"
              >
                <el-option label="优秀" value="EXCELLENT" />
                <el-option label="良好" value="GOOD" />
                <el-option label="一般" value="NORMAL" />
                <el-option label="需关注" value="NEED_ATTENTION" />
                <el-option label="有病史" value="HAS_HISTORY" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="运动目标" prop="fitnessGoal">
              <el-input
                v-model="formData.fitnessGoal"
                type="textarea"
                placeholder="请输入运动目标"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="健康状况备注" prop="healthNotes">
              <el-input
                v-model="formData.healthNotes"
                type="textarea"
                placeholder="请输入健康状况备注"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </div>
        </el-tab-pane>
        
        <!-- 会员卡信息 -->
        <el-tab-pane label="会员卡信息" name="card">
          <div class="form-section">
            <el-form-item label="会员等级" prop="memberLevel">
              <el-select
                v-model="formData.memberLevel"
                placeholder="请选择会员等级"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="level in memberLevels"
                  :key="level.value"
                  :label="level.label"
                  :value="level.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="会员状态" prop="status">
              <el-select
                v-model="formData.status"
                placeholder="请选择会员状态"
                clearable
                style="width: 100%"
              >
                <el-option label="正常" value="ACTIVE" />
                <el-option label="冻结" value="FROZEN" />
                <el-option label="已过期" value="EXPIRED" />
                <el-option label="已注销" value="CANCELLED" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="会籍顾问" prop="consultantId">
              <el-select
                v-model="formData.consultantId"
                placeholder="请选择会籍顾问"
                clearable
                filterable
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
            
            <el-form-item label="入会日期" prop="joinDate">
              <el-date-picker
                v-model="formData.joinDate"
                type="date"
                placeholder="选择入会日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="到期日期" prop="expireDate">
              <el-date-picker
                v-model="formData.expireDate"
                type="date"
                placeholder="选择到期日期"
                value-format="YYYY-MM-DD"
                :disabled-date="disabledExpireDate"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="剩余课时" prop="remainingSessions">
              <el-input-number
                v-model="formData.remainingSessions"
                :min="0"
                :max="1000"
                :step="1"
                controls-position="right"
                placeholder="请输入剩余课时"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="备注" prop="notes">
              <el-input
                v-model="formData.notes"
                type="textarea"
                placeholder="请输入备注信息"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </div>
        </el-tab-pane>
        
        <!-- 紧急联系人 -->
        <el-tab-pane label="紧急联系人" name="emergency">
          <div class="form-section">
            <el-form-item label="联系人姓名" prop="emergencyContact.name">
              <el-input
                v-model="formData.emergencyContact.name"
                placeholder="请输入紧急联系人姓名"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="联系人关系" prop="emergencyContact.relationship">
              <el-select
                v-model="formData.emergencyContact.relationship"
                placeholder="请选择关系"
                clearable
                style="width: 100%"
              >
                <el-option label="配偶" value="SPOUSE" />
                <el-option label="父母" value="PARENT" />
                <el-option label="子女" value="CHILD" />
                <el-option label="兄弟姐妹" value="SIBLING" />
                <el-option label="其他亲属" value="OTHER_RELATIVE" />
                <el-option label="朋友" value="FRIEND" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="联系人电话" prop="emergencyContact.phone">
              <el-input
                v-model="formData.emergencyContact.phone"
                placeholder="请输入紧急联系人电话"
                clearable
                maxlength="11"
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
import { ref, computed, watch, onMounted } from 'vue'
import { useDict } from '@/composables/useDict'
import type { MemberFormData } from '@/types'

interface Props {
  formData: MemberFormData
  isEdit?: boolean
}

interface Emits {
  (e: 'submit', data: MemberFormData): void
  (e: 'cancel'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 表单引用
const formRef = ref<FormInstance>()

// 响应式数据
const loading = ref(false)
const activeTab = ref('basic')
const consultants = ref<any[]>([])

// 字典数据
const { dictData: memberLevels } = useDict('member_level')

// 表单验证规则
const formRules = ref<FormRules>({
  name: [
    { required: true, message: '请输入会员姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2到20个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  idCard: [
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  height: [
    { type: 'number', min: 100, max: 250, message: '身高应在100-250cm之间', trigger: 'blur' }
  ],
  weight: [
    { type: 'number', min: 30, max: 200, message: '体重应在30-200kg之间', trigger: 'blur' }
  ],
  joinDate: [
    { required: true, message: '请选择入会日期', trigger: 'change' }
  ],
  expireDate: [
    { required: true, message: '请选择到期日期', trigger: 'change' }
  ]
})

// 计算BMI
const bmi = computed(() => {
  const { height, weight } = props.formData
  if (height && weight && height > 0) {
    const heightInMeter = height / 100
    return (weight / (heightInMeter * heightInMeter)).toFixed(1)
  }
  return null
})

// 监听表单数据变化，更新BMI
watch(() => [props.formData.height, props.formData.weight], () => {
  if (props.formData.height && props.formData.weight) {
    props.formData.bmi = bmi.value
  }
}, { immediate: true })

// 获取BMI类型
const getBMIType = (bmiValue: string | null) => {
  if (!bmiValue) return 'info'
  const value = parseFloat(bmiValue)
  if (value < 18.5) return 'warning' // 偏瘦
  if (value >= 18.5 && value < 24) return 'success' // 正常
  if (value >= 24 && value < 28) return 'warning' // 超重
  return 'danger' // 肥胖
}

// 获取BMI提示
const getBMITips = (bmiValue: string | null) => {
  if (!bmiValue) return ''
  const value = parseFloat(bmiValue)
  if (value < 18.5) return '偏瘦'
  if (value >= 18.5 && value < 24) return '正常'
  if (value >= 24 && value < 28) return '超重'
  return '肥胖'
}

// 禁用到期日期（不能早于入会日期）
const disabledExpireDate = (time: Date) => {
  if (!props.formData.joinDate) return false
  const joinDate = new Date(props.formData.joinDate)
  return time.getTime() < joinDate.getTime()
}

// 加载会籍顾问列表
const loadConsultants = async () => {
  try {
    // 这里应该调用API获取会籍顾问列表
    // 暂时使用模拟数据
    consultants.value = [
      { id: '1', name: '张三' },
      { id: '2', name: '李四' },
      { id: '3', name: '王五' }
    ]
  } catch (error) {
    console.error('加载会籍顾问失败:', error)
  }
}

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    // 提交表单数据
    emit('submit', props.formData)
    
    ElMessage.success(props.isEdit ? '会员信息更新成功' : '会员创建成功')
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
onMounted(() => {
  loadConsultants()
})
</script>

<style scoped lang="scss">
.member-form {
  .form-section {
    padding: 20px 0;
    
    .el-form-item {
      margin-bottom: 20px;
      
      .bmi-tips {
        margin-left: 10px;
        color: #909399;
        font-size: 12px;
      }
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

:deep(.el-tabs) {
  .el-tabs__header {
    margin-bottom: 20px;
  }
}
</style>