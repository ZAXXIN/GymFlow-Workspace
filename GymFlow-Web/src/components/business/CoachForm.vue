<template>
  <div class="coach-form">
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
            <el-form-item label="教练姓名" prop="name">
              <el-input
                v-model="formData.name"
                placeholder="请输入教练姓名"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio label="MALE">男</el-radio>
                <el-radio label="FEMALE">女</el-radio>
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
            
            <el-form-item label="入职日期" prop="hireDate">
              <el-date-picker
                v-model="formData.hireDate"
                type="date"
                placeholder="选择入职日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="教练头像" prop="avatar">
              <app-upload
                v-model="formData.avatar"
                :limit="1"
                :max-size="2"
                :action="uploadUrl"
                accept="image/*"
                list-type="picture-card"
                @success="handleAvatarSuccess"
              >
                <el-icon><Plus /></el-icon>
              </app-upload>
            </el-form-item>
          </div>
        </el-tab-pane>
        
        <!-- 专业信息 -->
        <el-tab-pane label="专业信息" name="professional">
          <div class="form-section">
            <el-form-item label="教练等级" prop="coachLevel">
              <el-select
                v-model="formData.coachLevel"
                placeholder="请选择教练等级"
                clearable
                style="width: 100%"
              >
                <el-option label="初级教练" value="JUNIOR" />
                <el-option label="中级教练" value="INTERMEDIATE" />
                <el-option label="高级教练" value="SENIOR" />
                <el-option label="明星教练" value="STAR" />
                <el-option label="培训师" value="TRAINER" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="教练状态" prop="status">
              <el-select
                v-model="formData.status"
                placeholder="请选择教练状态"
                clearable
                style="width: 100%"
              >
                <el-option label="在职" value="ACTIVE" />
                <el-option label="休假" value="ON_LEAVE" />
                <el-option label{"value"="RESIGNED"} label="离职" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="专业技能" prop="skills">
              <el-select
                v-model="formData.skills"
                multiple
                placeholder="请选择专业技能"
                style="width: 100%"
              >
                <el-option label="私人教练" value="PERSONAL_TRAINING" />
                <el-option label="团体课程" value="GROUP_CLASS" />
                <el-option label="瑜伽" value="YOGA" />
                <el-option label="普拉提" value="PILATES" />
                <el-option label="动感单车" value="SPINNING" />
                <el-option label{"value"="FUNCTIONAL_TRAINING"} label="功能性训练" />
                <el-option label{"value"="WEIGHT_LOSS"} label="减脂塑形" />
                <el-option label{"value"="STRENGTH_TRAINING"} label="力量训练" />
                <el-option label{"value"="REHABILITATION"} label="康复训练" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="证书资质" prop="certifications">
              <el-input
                v-model="formData.certifications"
                type="textarea"
                placeholder="请输入证书资质（每行一个）"
                :rows="4"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="教学经验" prop="experience">
              <el-input-number
                v-model="formData.experience"
                :min="0"
                :max="50"
                :step="1"
                controls-position="right"
                placeholder="请输入教学经验（年）"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="教学风格" prop="teachingStyle">
              <el-input
                v-model="formData.teachingStyle"
                type="textarea"
                placeholder="请输入教学风格描述"
                :rows="3"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="个人介绍" prop="introduction">
              <el-input
                v-model="formData.introduction"
                type="textarea"
                placeholder="请输入个人介绍"
                :rows="4"
                maxlength="1000"
                show-word-limit
              />
            </el-form-item>
          </div>
        </el-tab-pane>
        
        <!-- 排班信息 -->
        <el-tab-pane label="排班信息" name="schedule">
          <div class="form-section">
            <el-form-item label="工作类型" prop="workType">
              <el-select
                v-model="formData.workType"
                placeholder="请选择工作类型"
                clearable
                style="width: 100%"
              >
                <el-option label="全职" value="FULL_TIME" />
                <el-option label="兼职" value="PART_TIME" />
                <el-option label="自由教练" value="FREELANCE" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="工作时段" prop="workShifts">
              <el-checkbox-group v-model="formData.workShifts">
                <el-checkbox label="MORNING">上午 (8:00-12:00)</el-checkbox>
                <el-checkbox label="AFTERNOON">下午 (14:00-18:00)</el-checkbox>
                <el-checkbox label="EVENING">晚上 (19:00-22:00)</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            
            <el-form-item label="每周工作日" prop="workDays">
              <el-checkbox-group v-model="formData.workDays">
                <el-checkbox label="1">周一</el-checkbox>
                <el-checkbox label="2">周二</el-checkbox>
                <el-checkbox label="3">周三</el-checkbox>
                <el-checkbox label="4">周四</el-checkbox>
                <el-checkbox label="5">周五</el-checkbox>
                <el-checkbox label="6">周六</el-checkbox>
                <el-checkbox label="0">周日</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            
            <el-form-item label="最大带课数" prop="maxCoursesPerDay">
              <el-input-number
                v-model="formData.maxCoursesPerDay"
                :min="1"
                :max="8"
                :step="1"
                controls-position="right"
                placeholder="请输入每天最大带课数"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="基础课时费" prop="baseHourlyRate">
              <el-input-number
                v-model="formData.baseHourlyRate"
                :min="0"
                :step="10"
                controls-position="right"
                placeholder="请输入基础课时费"
                style="width: 100%"
              >
                <template #prefix>¥</template>
              </el-input-number>
            </el-form-item>
            
            <el-form-item label="绩效提成比例" prop="commissionRate">
              <el-input-number
                v-model="formData.commissionRate"
                :min="0"
                :max="100"
                :step="0.5"
                controls-position="right"
                placeholder="请输入绩效提成比例"
                style="width: 100%"
              >
                <template #suffix>%</template>
              </el-input-number>
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
import { ref, computed } from 'vue'
import AppUpload from '../common/AppUpload.vue'
import type { CoachFormData } from '@/types'

interface Props {
  formData: CoachFormData
  isEdit?: boolean
}

interface Emits {
  (e: 'submit', data: CoachFormData): void
  (e: 'cancel'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 表单引用
const formRef = ref<FormInstance>()

// 响应式数据
const loading = ref(false)
const activeTab = ref('basic')
const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL}/upload`)

// 表单验证规则
const formRules = ref<FormRules>({
  name: [
    { required: true, message: '请输入教练姓名', trigger: 'blur' },
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
  hireDate: [
    { required: true, message: '请选择入职日期', trigger: 'change' }
  ],
  coachLevel: [
    { required: true, message: '请选择教练等级', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择教练状态', trigger: 'change' }
  ],
  workType: [
    { required: true, message: '请选择工作类型', trigger: 'change' }
  ],
  baseHourlyRate: [
    { required: true, message: '请输入基础课时费', trigger: 'blur' },
    { type: 'number', min: 0, message: '课时费不能为负数', trigger: 'blur' }
  ]
})

// 处理头像上传成功
const handleAvatarSuccess = (response: any, file: any) => {
  if (response.code === 200 && response.data?.url) {
    props.formData.avatar = response.data.url
    ElMessage.success('头像上传成功')
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
    
    ElMessage.success(props.isEdit ? '教练信息更新成功' : '教练创建成功')
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
</script>

<style scoped lang="scss">
.coach-form {
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

:deep(.el-tabs) {
  .el-tabs__header {
    margin-bottom: 20px;
  }
}

:deep(.el-checkbox-group) {
  display: flex;
  flex-direction: column;
  gap: 8px;
  
  .el-checkbox {
    margin-right: 20px;
  }
}
</style>