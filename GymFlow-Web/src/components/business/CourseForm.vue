<template>
  <div class="course-form">
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
            <el-form-item label="课程名称" prop="name">
              <el-input
                v-model="formData.name"
                placeholder="请输入课程名称"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="课程类型" prop="type">
              <el-select
                v-model="formData.type"
                placeholder="请选择课程类型"
                clearable
                style="width: 100%"
              >
                <el-option label="私人训练" value="PRIVATE_TRAINING" />
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
            
            <el-form-item label="课程难度" prop="level">
              <el-select
                v-model="formData.level"
                placeholder="请选择课程难度"
                clearable
                style="width: 100%"
              >
                <el-option label="初级" value="BEGINNER" />
                <el-option label="中级" value="INTERMEDIATE" />
                <el-option label="高级" value="ADVANCED" />
                <el-option label{"value"="ALL_LEVELS"} label="所有级别" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="课程状态" prop="status">
              <el-select
                v-model="formData.status"
                placeholder="请选择课程状态"
                clearable
                style="width: 100%"
              >
                <el-option label="未开始" value="UPCOMING" />
                <el-option label="进行中" value="ONGOING" />
                <el-option label="已结束" value="COMPLETED" />
                <el-option label="已取消" value="CANCELLED" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="授课教练" prop="coachId">
              <el-select
                v-model="formData.coachId"
                placeholder="请选择授课教练"
                clearable
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="coach in availableCoaches"
                  :key="coach.id"
                  :label="coach.name"
                  :value="coach.id"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="助教" prop="assistantCoachId">
              <el-select
                v-model="formData.assistantCoachId"
                placeholder="请选择助教（可选）"
                clearable
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="coach in availableCoaches"
                  :key="coach.id"
                  :label="coach.name"
                  :value="coach.id"
                  :disabled="coach.id === formData.coachId"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="课程封面" prop="coverImage">
              <app-upload
                v-model="formData.coverImage"
                :limit="1"
                :max-size="2"
                :action="uploadUrl"
                accept="image/*"
                list-type="picture-card"
                @success="handleCoverImageSuccess"
              >
                <el-icon><Plus /></el-icon>
              </app-upload>
            </el-form-item>
            
            <el-form-item label="课程描述" prop="description">
              <el-input
                v-model="formData.description"
                type="textarea"
                placeholder="请输入课程描述"
                :rows="4"
                maxlength="1000"
                show-word-limit
              />
            </el-form-item>
          </div>
        </el-tab-pane>
        
        <!-- 时间设置 -->
        <el-tab-pane label="时间设置" name="schedule">
          <div class="form-section">
            <el-form-item label="课程日期" prop="courseDate">
              <el-date-picker
                v-model="formData.courseDate"
                type="date"
                placeholder="选择课程日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="开始时间" prop="startTime">
              <el-time-picker
                v-model="formData.startTime"
                placeholder="选择开始时间"
                value-format="HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="结束时间" prop="endTime">
              <el-time-picker
                v-model="formData.endTime"
                placeholder="选择结束时间"
                value-format="HH:mm:ss"
                :disabled-hours="disabledEndHours"
                :disabled-minutes="disabledEndMinutes"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="课程时长" prop="duration">
              <el-input-number
                v-model="formData.duration"
                :min="30"
                :max="180"
                :step="15"
                controls-position="right"
                placeholder="请输入课程时长"
                style="width: 100%"
              >
                <template #suffix>分钟</template>
              </el-input-number>
            </el-form-item>
            
            <el-form-item label="重复设置" prop="repeatType">
              <el-select
                v-model="formData.repeatType"
                placeholder="请选择重复类型"
                clearable
                style="width: 100%"
                @change="handleRepeatTypeChange"
              >
                <el-option label="不重复" value="NONE" />
                <el-option label="每天" value="DAILY" />
                <el-option label="每周" value="WEEKLY" />
                <el-option label{"value"="MONTHLY"} label="每月" />
              </el-select>
            </el-form-item>
            
            <el-form-item 
              label="重复次数" 
              prop="repeatCount"
              v-if="formData.repeatType !== 'NONE'"
            >
              <el-input-number
                v-model="formData.repeatCount"
                :min="1"
                :max="52"
                :step="1"
                controls-position="right"
                placeholder="请输入重复次数"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item 
              label="每周重复日" 
              prop="repeatDays"
              v-if="formData.repeatType === 'WEEKLY'"
            >
              <el-checkbox-group v-model="formData.repeatDays">
                <el-checkbox label="1">周一</el-checkbox>
                <el-checkbox label="2">周二</el-checkbox>
                <el-checkbox label="3">周三</el-checkbox>
                <el-checkbox label="4">周四</el-checkbox>
                <el-checkbox label="5">周五</el-checkbox>
                <el-checkbox label="6">周六</el-checkbox>
                <el-checkbox label="0">周日</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            
            <el-form-item label="总课次数" prop="totalSessions">
              <el-input-number
                v-model="formData.totalSessions"
                :min="1"
                :max="100"
                :step="1"
                controls-position="right"
                placeholder="请输入总课次数"
                style="width: 100%"
              />
            </el-form-item>
          </div>
        </el-tab-pane>
        
        <!-- 价格设置 -->
        <el-tab-pane label="价格设置" name="price">
          <div class="form-section">
            <el-form-item label="单次价格" prop="singlePrice">
              <el-input-number
                v-model="formData.singlePrice"
                :min="0"
                :step="10"
                controls-position="right"
                placeholder="请输入单次价格"
                style="width: 100%"
              >
                <template #prefix>¥</template>
              </el-input-number>
            </el-form-item>
            
            <el-form-item label="套餐价格" prop="packagePrice">
              <el-input-number
                v-model="formData.packagePrice"
                :min="0"
                :step="100"
                controls-position="right"
                placeholder="请输入套餐价格"
                style="width: 100%"
              >
                <template #prefix>¥</template>
              </el-input-number>
              <div class="price-tips">（购买多次课程享受优惠）</div>
            </el-form-item>
            
            <el-form-item label="套餐次数" prop="packageSessions">
              <el-input-number
                v-model="formData.packageSessions"
                :min="2"
                :max="50"
                :step="1"
                controls-position="right"
                placeholder="请输入套餐包含次数"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="会员价" prop="memberPrice">
              <el-input-number
                v-model="formData.memberPrice"
                :min="0"
                :step="10"
                controls-position="right"
                placeholder="请输入会员价"
                style="width: 100%"
              >
                <template #prefix>¥</template>
              </el-input-number>
            </el-form-item>
            
            <el-form-item label="最大人数" prop="maxCapacity">
              <el-input-number
                v-model="formData.maxCapacity"
                :min="1"
                :max="50"
                :step="1"
                controls-position="right"
                placeholder="请输入最大人数"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="最少开课人数" prop="minCapacity">
              <el-input-number
                v-model="formData.minCapacity"
                :min="1"
                :max="formData.maxCapacity || 10"
                :step="1"
                controls-position="right"
                placeholder="请输入最少开课人数"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="取消政策" prop="cancellationPolicy">
              <el-input
                v-model="formData.cancellationPolicy"
                type="textarea"
                placeholder="请输入取消政策说明"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="退款政策" prop="refundPolicy">
              <el-input
                v-model="formData.refundPolicy"
                type="textarea"
                placeholder="请输入退款政策说明"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </div>
        </el-tab-pane>
        
        <!-- 课程内容 -->
        <el-tab-pane label="课程内容" name="content">
          <div class="form-section">
            <el-form-item label="课程目标" prop="objectives">
              <el-input
                v-model="formData.objectives"
                type="textarea"
                placeholder="请输入课程目标"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="适用人群" prop="suitableFor">
              <el-input
                v-model="formData.suitableFor"
                type="textarea"
                placeholder="请输入适用人群描述"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="注意事项" prop="precautions">
              <el-input
                v-model="formData.precautions"
                type="textarea"
                placeholder="请输入注意事项"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="所需装备" prop="equipmentRequired">
              <el-input
                v-model="formData.equipmentRequired"
                type="textarea"
                placeholder="请输入所需装备"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="课前准备" prop="preparation">
              <el-input
                v-model="formData.preparation"
                type="textarea"
                placeholder="请输入课前准备要求"
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
import AppUpload from '../common/AppUpload.vue'
import type { CourseFormData } from '@/types'

interface Props {
  formData: CourseFormData
  isEdit?: boolean
}

interface Emits {
  (e: 'submit', data: CourseFormData): void
  (e: 'cancel'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 表单引用
const formRef = ref<FormInstance>()

// 响应式数据
const loading = ref(false)
const activeTab = ref('basic')
const availableCoaches = ref<any[]>([])
const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL}/upload`)

// 表单验证规则
const formRules = ref<FormRules>({
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 50, message: '课程名称长度在2到50个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择课程类型', trigger: 'change' }
  ],
  level: [
    { required: true, message: '请选择课程难度', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择课程状态', trigger: 'change' }
  ],
  coachId: [
    { required: true, message: '请选择授课教练', trigger: 'change' }
  ],
  courseDate: [
    { required: true, message: '请选择课程日期', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入课程时长', trigger: 'blur' },
    { type: 'number', min: 30, message: '课程时长不能少于30分钟', trigger: 'blur' }
  ],
  maxCapacity: [
    { required: true, message: '请输入最大人数', trigger: 'blur' },
    { type: 'number', min: 1, message: '最大人数不能少于1人', trigger: 'blur' }
  ],
  minCapacity: [
    { type: 'number', min: 1, message: '最少开课人数不能少于1人', trigger: 'blur' }
  ],
  singlePrice: [
    { required: true, message: '请输入单次价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' }
  ]
})

// 禁用结束时间的小时
const disabledEndHours = () => {
  if (!props.formData.startTime) return []
  const startHour = parseInt(props.formData.startTime.split(':')[0])
  const hours = []
  for (let i = 0; i < startHour; i++) {
    hours.push(i)
  }
  return hours
}

// 禁用结束时间的分钟
const disabledEndMinutes = (hour: number) => {
  if (!props.formData.startTime) return []
  const [startHour, startMinute] = props.formData.startTime.split(':').map(Number)
  
  if (hour === startHour) {
    const minutes = []
    for (let i = 0; i <= startMinute; i++) {
      minutes.push(i)
    }
    return minutes
  }
  return []
}

// 处理重复类型变化
const handleRepeatTypeChange = (value: string) => {
  if (value === 'NONE') {
    props.formData.repeatCount = 1
    props.formData.repeatDays = []
  } else if (value === 'WEEKLY') {
    // 默认选择周一
    props.formData.repeatDays = ['1']
  }
}

// 处理封面图片上传成功
const handleCoverImageSuccess = (response: any, file: any) => {
  if (response.code === 200 && response.data?.url) {
    props.formData.coverImage = response.data.url
    ElMessage.success('封面图片上传成功')
  }
}

// 加载可用的教练列表
const loadAvailableCoaches = async () => {
  try {
    // 这里应该调用API获取教练列表
    // 暂时使用模拟数据
    availableCoaches.value = [
      { id: '1', name: '张教练' },
      { id: '2', name: '李教练' },
      { id: '3', name: '王教练' },
      { id: '4', name: '刘教练' }
    ]
  } catch (error) {
    console.error('加载教练列表失败:', error)
  }
}

// 监听最大人数变化，调整最少人数最大值
watch(() => props.formData.maxCapacity, (newMax) => {
  if (newMax && props.formData.minCapacity > newMax) {
    props.formData.minCapacity = newMax
  }
})

// 处理提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    // 提交表单数据
    emit('submit', props.formData)
    
    ElMessage.success(props.isEdit ? '课程信息更新成功' : '课程创建成功')
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
loadAvailableCoaches()
</script>

<style scoped lang="scss">
.course-form {
  .form-section {
    padding: 20px 0;
    
    .el-form-item {
      margin-bottom: 20px;
      
      .price-tips {
        margin-top: 4px;
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

:deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  
  .el-checkbox {
    margin-right: 0;
  }
}
</style>