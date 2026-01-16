<template>
  <div class="checkin-form">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      label-position="left"
    >
      <el-tabs v-model="activeTab">
        <!-- 签到信息 -->
        <el-tab-pane label="签到信息" name="checkin">
          <div class="form-section">
            <el-form-item label="签到类型" prop="checkInType">
              <el-select
                v-model="formData.checkInType"
                placeholder="请选择签到类型"
                clearable
                style="width: 100%"
              >
                <el-option label="课程签到" value="COURSE" />
                <el-option label="场馆签到" value="GYM" />
                <el-option label="私教课签到" value="PRIVATE_TRAINING" />
                <el-option label="活动签到" value="EVENT" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="签到会员" prop="memberId">
              <el-select
                v-model="formData.memberId"
                placeholder="请选择签到会员"
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
            
            <el-form-item label="会员信息" v-if="selectedMember">
              <div class="member-info">
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="会员等级">
                    {{ selectedMember.memberLevel }}
                  </el-descriptions-item>
                  <el-descriptions-item label="剩余课时">
                    {{ selectedMember.remainingSessions }} 节
                  </el-descriptions-item>
                  <el-descriptions-item label="会员状态">
                    <el-tag :type="getMemberStatusType(selectedMember.status)">
                      {{ selectedMember.status }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="到期日期">
                    {{ selectedMember.expireDate }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-form-item>
            
            <el-form-item label="相关课程" prop="courseId" v-if="showCourseField">
              <el-select
                v-model="formData.courseId"
                placeholder="请选择相关课程"
                clearable
                filterable
                style="width: 100%"
                @change="handleCourseChange"
              >
                <el-option
                  v-for="course in availableCourses"
                  :key="course.id"
                  :label="course.name"
                  :value="course.id"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="课程信息" v-if="selectedCourse">
              <div class="course-info">
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="课程时间">
                    {{ selectedCourse.courseDate }} {{ selectedCourse.startTime }}
                  </el-descriptions-item>
                  <el-descriptions-item label="授课教练">
                    {{ selectedCourse.coachName }}
                  </el-descriptions-item>
                  <el-descriptions-item label="课程类型">
                    {{ selectedCourse.type }}
                  </el-descriptions-item>
                  <el-descriptions-item label="课程状态">
                    <el-tag :type="getCourseStatusType(selectedCourse.status)">
                      {{ selectedCourse.status }}
                    </el-tag>
                  </el-descriptions-item>
                </el-descriptions>
              </div>
            </el-form-item>
            
            <el-form-item label="签到时间" prop="checkInTime">
              <el-date-picker
                v-model="formData.checkInTime"
                type="datetime"
                placeholder="选择签到时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="签到方式" prop="checkInMethod">
              <el-select
                v-model="formData.checkInMethod"
                placeholder="请选择签到方式"
                clearable
                style="width: 100%"
              >
                <el-option label="扫码签到" value="QR_CODE" />
                <el-option label="手动签到" value="MANUAL" />
                <el-option label="刷脸签到" value="FACE_RECOGNITION" />
                <el-option label="刷卡签到" value="CARD" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="签到状态" prop="status">
              <el-select
                v-model="formData.status"
                placeholder="请选择签到状态"
                clearable
                style="width: 100%"
              >
                <el-option label="已签到" value="CHECKED_IN" />
                <el-option label="已签出" value="CHECKED_OUT" />
                <el-option label="迟到" value="LATE" />
                <el-option label="缺席" value="ABSENT" />
                <el-option label{"value"="CANCELLED"} label="已取消" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="签出时间" prop="checkOutTime" v-if="showCheckOutField">
              <el-date-picker
                v-model="formData.checkOutTime"
                type="datetime"
                placeholder="选择签出时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </div>
        </el-tab-pane>
        
        <!-- 签到详情 -->
        <el-tab-pane label="签到详情" name="details">
          <div class="form-section">
            <el-form-item label="消耗课时" prop="sessionsConsumed">
              <el-input-number
                v-model="formData.sessionsConsumed"
                :min="0"
                :max="10"
                :step="0.5"
                controls-position="right"
                placeholder="请输入消耗课时"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="签到设备" prop="checkInDevice">
              <el-input
                v-model="formData.checkInDevice"
                placeholder="请输入签到设备"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="设备IP" prop="deviceIp">
              <el-input
                v-model="formData.deviceIp"
                placeholder="请输入设备IP地址"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="签到位置" prop="location">
              <el-input
                v-model="formData.location"
                placeholder="请输入签到位置"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="体温检测" prop="bodyTemperature">
              <el-input-number
                v-model="formData.bodyTemperature"
                :min="35"
                :max="42"
                :step="0.1"
                controls-position="right"
                placeholder="请输入体温"
                style="width: 100%"
              >
                <template #suffix>°C</template>
              </el-input-number>
            </el-form-item>
            
            <el-form-item label="健康状况" prop="healthStatus">
              <el-select
                v-model="formData.healthStatus"
                placeholder="请选择健康状况"
                clearable
                style="width: 100%"
              >
                <el-option label="良好" value="GOOD" />
                <el-option label="一般" value="NORMAL" />
                <el-option label="不适" value="UNWELL" />
                <el-option label{"value"="INJURED"} label="受伤" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="健康备注" prop="healthNotes">
              <el-input
                v-model="formData.healthNotes"
                type="textarea"
                placeholder="请输入健康备注"
                :rows="3"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item label="签到照片" prop="checkInPhoto">
              <app-upload
                v-model="formData.checkInPhoto"
                :limit="1"
                :max-size="5"
                :action="uploadUrl"
                accept="image/*"
                list-type="picture-card"
                @success="handlePhotoSuccess"
              >
                <el-icon><Camera /></el-icon>
              </app-upload>
            </el-form-item>
          </div>
        </el-tab-pane>
        
        <!-- 签到记录 -->
        <el-tab-pane label="签到记录" name="history" v-if="isEdit && checkInHistory.length > 0">
          <div class="form-section">
            <el-table
              :data="checkInHistory"
              style="width: 100%"
              border
              stripe
            >
              <el-table-column label="时间" prop="checkInTime" width="160" />
              <el-table-column label="类型" prop="checkInType" width="100">
                <template #default="{ row }">
                  <el-tag size="small">{{ row.checkInType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" prop="status" width="100">
                <template #default="{ row }">
                  <el-tag :type="getCheckInStatusType(row.status)" size="small">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="方式" prop="checkInMethod" width="100" />
              <el-table-column label="设备" prop="checkInDevice" />
              <el-table-column label="操作人" prop="operator" width="100" />
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 表单操作 -->
      <div class="form-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEdit ? '更新' : '签到' }}
        </el-button>
        <el-button 
          type="warning" 
          @click="handleCheckOut" 
          :loading="checkingOut"
          v-if="showCheckOutButton"
        >
          签出
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import AppUpload from '../common/AppUpload.vue'
import type { CheckInFormData } from '@/types'

interface Props {
  formData: CheckInFormData
  isEdit?: boolean
}

interface Emits {
  (e: 'submit', data: CheckInFormData): void
  (e: 'checkout', data: CheckInFormData): void
  (e: 'cancel'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 表单引用
const formRef = ref<FormInstance>()

// 响应式数据
const loading = ref(false)
const checkingOut = ref(false)
const activeTab = ref('checkin')
const searchingMembers = ref(false)
const memberOptions = ref<any[]>([])
const availableCourses = ref<any[]>([])
const checkInHistory = ref<any[]>([])
const uploadUrl = computed(() => `${import.meta.env.VITE_API_BASE_URL}/upload`)

// 计算属性
const showCourseField = computed(() => {
  return ['COURSE', 'PRIVATE_TRAINING'].includes(props.formData.checkInType)
})

const showCheckOutField = computed(() => {
  return ['CHECKED_IN', 'LATE'].includes(props.formData.status)
})

const showCheckOutButton = computed(() => {
  return props.isEdit && !props.formData.checkOutTime
})

const selectedMember = computed(() => {
  if (!props.formData.memberId) return null
  // 这里应该根据会员ID获取会员详情
  // 暂时使用模拟数据
  return {
    id: props.formData.memberId,
    name: '张三',
    memberLevel: '金卡会员',
    remainingSessions: 12,
    status: 'ACTIVE',
    expireDate: '2024-12-31'
  }
})

const selectedCourse = computed(() => {
  if (!props.formData.courseId) return null
  // 这里应该根据课程ID获取课程详情
  // 暂时使用模拟数据
  return {
    id: props.formData.courseId,
    name: '瑜伽基础课',
    courseDate: '2024-01-15',
    startTime: '19:00',
    coachName: '李教练',
    type: 'GROUP_CLASS',
    status: 'ONGOING'
  }
})

// 表单验证规则
const formRules = ref<FormRules>({
  checkInType: [
    { required: true, message: '请选择签到类型', trigger: 'change' }
  ],
  memberId: [
    { required: true, message: '请选择签到会员', trigger: 'change' }
  ],
  checkInTime: [
    { required: true, message: '请选择签到时间', trigger: 'change' }
  ],
  checkInMethod: [
    { required: true, message: '请选择签到方式', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择签到状态', trigger: 'change' }
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

// 处理课程变化
const handleCourseChange = (courseId: string) => {
  if (courseId && !props.formData.checkInTime) {
    // 自动设置签到时间为当前时间
    const now = new Date()
    props.formData.checkInTime = now.toISOString().replace('T', ' ').substring(0, 19)
  }
}

// 获取会员状态类型
const getMemberStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'ACTIVE': 'success',
    'FROZEN': 'warning',
    'EXPIRED': 'danger',
    'CANCELLED': 'info'
  }
  return statusMap[status] || 'info'
}

// 获取课程状态类型
const getCourseStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'UPCOMING': 'info',
    'ONGOING': 'success',
    'COMPLETED': 'primary',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取签到状态类型
const getCheckInStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'CHECKED_IN': 'success',
    'CHECKED_OUT': 'primary',
    'LATE': 'warning',
    'ABSENT': 'danger',
    'CANCELLED': 'info'
  }
  return statusMap[status] || 'info'
}

// 处理照片上传成功
const handlePhotoSuccess = (response: any, file: any) => {
  if (response.code === 200 && response.data?.url) {
    props.formData.checkInPhoto = response.data.url
    ElMessage.success('照片上传成功')
  }
}

// 加载可用课程
const loadAvailableCourses = async () => {
  try {
    // 这里应该调用API获取可用课程列表
    // 暂时使用模拟数据
    availableCourses.value = [
      { id: '1', name: '瑜伽基础课' },
      { id: '2', name: '动感单车课' },
      { id: '3', name: '力量训练课' },
      { id: '4', name: '私教课' }
    ]
  } catch (error) {
    console.error('加载课程列表失败:', error)
  }
}

// 加载签到历史
const loadCheckInHistory = async () => {
  if (!props.isEdit || !props.formData.memberId) return
  
  try {
    // 这里应该调用API获取签到历史
    // 暂时使用模拟数据
    checkInHistory.value = [
      {
        id: '1',
        checkInTime: '2024-01-14 19:00',
        checkInType: 'COURSE',
        status: 'CHECKED_IN',
        checkInMethod: 'QR_CODE',
        checkInDevice: '前台设备001',
        operator: '系统'
      },
      {
        id: '2',
        checkInTime: '2024-01-12 18:30',
        checkInType: 'COURSE',
        status: 'CHECKED_OUT',
        checkInMethod: 'MANUAL',
        checkInDevice: '前台设备001',
        operator: '管理员'
      }
    ]
  } catch (error) {
    console.error('加载签到历史失败:', error)
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
    
    ElMessage.success(props.isEdit ? '签到信息更新成功' : '签到成功')
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理签出
const handleCheckOut = async () => {
  if (!props.formData.checkInTime) {
    ElMessage.error('请先设置签到时间')
    return
  }
  
  checkingOut.value = true
  try {
    // 设置签出时间为当前时间
    const now = new Date()
    props.formData.checkOutTime = now.toISOString().replace('T', ' ').substring(0, 19)
    props.formData.status = 'CHECKED_OUT'
    
    // 触发签出事件
    emit('checkout', props.formData)
    
    ElMessage.success('签出成功')
  } catch (error) {
    console.error('签出失败:', error)
  } finally {
    checkingOut.value = false
  }
}

// 处理取消
const handleCancel = () => {
  emit('cancel')
}

// 监听会员选择变化
watch(() => props.formData.memberId, (newMemberId) => {
  if (newMemberId && props.isEdit) {
    loadCheckInHistory()
  }
})

// 初始化
loadAvailableCourses()
if (props.isEdit && props.formData.memberId) {
  loadCheckInHistory()
}
</script>

<style scoped lang="scss">
.checkin-form {
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
      margin-left: 10px;
    }
  }
}

.member-info,
.course-info {
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

:deep(.el-tabs) {
  .el-tabs__header {
    margin-bottom: 20px;
  }
}

:deep(.el-descriptions) {
  .el-descriptions__body {
    background-color: transparent;
  }
}
</style>