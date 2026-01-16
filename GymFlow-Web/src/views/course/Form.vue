<template>
  <div class="course-form-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/course' }">课程管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ formData.id ? '编辑课程' : '新增课程' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">{{ formData.id ? '编辑课程信息' : '新增课程' }}</h1>
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
        class="course-form"
      >
        <!-- 基本信息 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
            </div>
          </template>
          
          <div class="form-row">
            <el-form-item label="课程名称" prop="name" class="form-item">
              <el-input 
                v-model="formData.name"
                placeholder="请输入课程名称"
                maxlength="50"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="课程编号" prop="courseNo" class="form-item">
              <el-input 
                v-model="formData.courseNo"
                placeholder="自动生成"
                :disabled="true"
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="课程分类" prop="category" class="form-item">
              <el-select v-model="formData.category" placeholder="请选择课程分类" style="width: 100%">
                <el-option 
                  v-for="item in categoryOptions" 
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="难度等级" prop="difficulty" class="form-item">
              <el-select v-model="formData.difficulty" placeholder="请选择难度等级" style="width: 100%">
                <el-option 
                  v-for="item in difficultyOptions" 
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="教练" prop="coachId" class="form-item">
              <el-select 
                v-model="formData.coachId"
                filterable
                placeholder="请选择教练"
                style="width: 100%"
                @change="handleCoachChange"
              >
                <el-option 
                  v-for="coach in coachList"
                  :key="coach.id"
                  :label="coach.realName"
                  :value="coach.id"
                />
              </el-select>
            </el-form-item>
            
            <el-form-item label="课程状态" prop="status" class="form-item">
              <el-select v-model="formData.status" placeholder="请选择课程状态" style="width: 100%">
                <el-option 
                  v-for="item in statusOptions" 
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
          
          <el-form-item label="课程描述" prop="description" class="full-width">
            <el-input 
              v-model="formData.description"
              type="textarea"
              :rows="3"
              placeholder="请输入课程描述"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-card>

        <!-- 时间与容量 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">时间与容量</span>
            </div>
          </template>
          
          <div class="form-row">
            <el-form-item label="上课地点" prop="location" class="form-item">
              <el-input 
                v-model="formData.location"
                placeholder="请输入上课地点"
                maxlength="100"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="课程时长(分钟)" prop="duration" class="form-item">
              <el-input-number
                v-model="formData.duration"
                :min="30"
                :max="180"
                :step="5"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="开课日期" prop="startDate" class="form-item">
              <el-date-picker
                v-model="formData.startDate"
                type="date"
                placeholder="选择开课日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="上课时间" prop="startTime" class="form-item">
              <el-time-picker
                v-model="formData.startTime"
                placeholder="选择上课时间"
                value-format="HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="结束时间" prop="endTime" class="form-item">
              <el-time-picker
                v-model="formData.endTime"
                placeholder="选择结束时间"
                value-format="HH:mm:ss"
                :disabled="!formData.startTime"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="重复周期" prop="repeatType" class="form-item">
              <el-select v-model="formData.repeatType" placeholder="请选择重复周期" style="width: 100%">
                <el-option label="单次课程" value="ONCE" />
                <el-option label="每日重复" value="DAILY" />
                <el-option label="每周重复" value="WEEKLY" />
                <el-option label="每月重复" value="MONTHLY" />
              </el-select>
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="最大容量" prop="capacity" class="form-item">
              <el-input-number
                v-model="formData.capacity"
                :min="1"
                :max="50"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="当前预约" prop="currentBookings" class="form-item">
              <el-input-number
                v-model="formData.currentBookings"
                :min="0"
                :max="formData.capacity"
                :step="1"
                controls-position="right"
                :disabled="!formData.id"
                style="width: 100%"
              />
            </el-form-item>
          </div>
        </el-card>

        <!-- 价格与预约 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">价格与预约</span>
            </div>
          </template>
          
          <div class="form-row">
            <el-form-item label="课程价格(元)" prop="price" class="form-item">
              <el-input-number
                v-model="formData.price"
                :min="0"
                :max="10000"
                :step="10"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="提前预约时间(小时)" prop="advanceBookingHours" class="form-item">
              <el-input-number
                v-model="formData.advanceBookingHours"
                :min="1"
                :max="168"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
              <span class="form-tip">开课前多久可以预约</span>
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="取消期限(小时)" prop="cancelDeadlineHours" class="form-item">
              <el-input-number
                v-model="formData.cancelDeadlineHours"
                :min="1"
                :max="48"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
              <span class="form-tip">开课前多久可以免费取消</span>
            </el-form-item>
            
            <el-form-item label="最少开课人数" prop="minParticipants" class="form-item">
              <el-input-number
                v-model="formData.minParticipants"
                :min="1"
                :max="formData.capacity"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
            </el-form-item>
          </div>
          
          <el-form-item label="课程要求" prop="requirements" class="full-width">
            <el-input 
              v-model="formData.requirements"
              type="textarea"
              :rows="2"
              placeholder="请输入课程要求"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="注意事项" prop="notes" class="full-width">
            <el-input 
              v-model="formData.notes"
              type="textarea"
              :rows="2"
              placeholder="请输入注意事项"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-card>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import type { 
  Course, 
  CourseCategory, 
  CourseDifficulty, 
  CourseStatus,
  Coach 
} from '@/types'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const loading = ref(false)

// 表单数据
const formData = reactive<Partial<Course & {
  startDate: string
  startTime: string
  repeatType: string
  advanceBookingHours: number
  cancelDeadlineHours: number
  minParticipants: number
  requirements: string
  notes: string
}>>({
  name: '',
  courseNo: '',
  description: '',
  category: CourseCategory.YOGA,
  difficulty: CourseDifficulty.BEGINNER,
  duration: 60,
  capacity: 20,
  currentBookings: 0,
  price: 199,
  coachId: undefined,
  coachName: '',
  location: 'A区瑜伽室',
  startDate: '',
  startTime: '',
  endTime: '',
  status: CourseStatus.SCHEDULED,
  repeatType: 'ONCE',
  advanceBookingHours: 24,
  cancelDeadlineHours: 2,
  minParticipants: 3,
  requirements: '',
  notes: ''
})

// 教练列表
const coachList = ref<Coach[]>([
  { id: 1, coachNo: 'C001', realName: '张教练', userId: 2, gender: 1, specialization: '瑜伽', yearsOfExperience: 5, hourlyRate: 200, commissionRate: 15, totalSessions: 100, totalSales: 50000, rating: 4.8, status: 'ACTIVE', createdAt: '', updatedAt: '' },
  { id: 2, coachNo: 'C002', realName: '李教练', userId: 3, gender: 1, specialization: '力量训练', yearsOfExperience: 8, hourlyRate: 300, commissionRate: 20, totalSessions: 150, totalSales: 80000, rating: 4.9, status: 'ACTIVE', createdAt: '', updatedAt: '' },
  { id: 3, coachNo: 'C003', realName: '王教练', userId: 4, gender: 2, specialization: '普拉提', yearsOfExperience: 6, hourlyRate: 250, commissionRate: 18, totalSessions: 120, totalSales: 60000, rating: 4.7, status: 'ACTIVE', createdAt: '', updatedAt: '' }
])

// 选项数据
const categoryOptions = [
  { label: '瑜伽', value: CourseCategory.YOGA },
  { label: '普拉提', value: CourseCategory.PILATES },
  { label: 'CrossFit', value: CourseCategory.CROSSFIT },
  { label: '动感单车', value: CourseCategory.SPINNING },
  { label: '力量训练', value: CourseCategory.BODYBUILDING },
  { label: '有氧操', value: CourseCategory.AEROBICS },
  { label: '私教课程', value: CourseCategory.PERSONAL_TRAINING }
]

const difficultyOptions = [
  { label: '初级', value: CourseDifficulty.BEGINNER },
  { label: '中级', value: CourseDifficulty.INTERMEDIATE },
  { label: '高级', value: CourseDifficulty.ADVANCED }
]

const statusOptions = [
  { label: '已排期', value: CourseStatus.SCHEDULED },
  { label: '进行中', value: CourseStatus.IN_PROGRESS },
  { label: '已结束', value: CourseStatus.COMPLETED },
  { label: '已取消', value: CourseStatus.CANCELLED }
]

// 计算结束时间
const computedEndTime = computed(() => {
  if (formData.startTime && formData.duration) {
    const start = new Date(`2000-01-01T${formData.startTime}`)
    start.setMinutes(start.getMinutes() + formData.duration)
    const hours = start.getHours().toString().padStart(2, '0')
    const minutes = start.getMinutes().toString().padStart(2, '0')
    return `${hours}:${minutes}:00`
  }
  return ''
})

// 监听开始时间和时长变化
watch([() => formData.startTime, () => formData.duration], () => {
  if (formData.startTime && formData.duration) {
    formData.endTime = computedEndTime.value
  }
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 50, message: '课程名称长度在2-50个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择课程分类', trigger: 'change' }
  ],
  difficulty: [
    { required: true, message: '请选择难度等级', trigger: 'change' }
  ],
  coachId: [
    { required: true, message: '请选择教练', trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入课程时长', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入最大容量', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入课程价格', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入上课地点', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开课日期', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择上课时间', trigger: 'change' }
  ]
}

// 教练选择变化
const handleCoachChange = (coachId: number) => {
  const selectedCoach = coachList.value.find(coach => coach.id === coachId)
  if (selectedCoach) {
    formData.coachName = selectedCoach.realName
  }
}

// 保存表单
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    // 生成课程编号（如果是新增）
    if (!formData.id && !formData.courseNo) {
      formData.courseNo = `CRS${Date.now().toString().slice(-6)}`
    }
    
    // 这里调用API保存数据
    // await saveCourse(formData)
    
    ElMessage.success(formData.id ? '课程信息更新成功' : '课程创建成功')
    router.push('/course')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请检查表单')
  } finally {
    loading.value = false
  }
}

// 取消编辑
const handleCancel = () => {
  router.push('/course')
}

// 初始化数据（编辑模式）
const initFormData = () => {
  const id = route.params.id
  if (id) {
    // 编辑模式：从API获取数据
    // const data = await getCourseDetail(id)
    // Object.assign(formData, data)
    
    // 模拟数据
    const today = new Date()
    const nextWeek = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000)
    
    Object.assign(formData, {
      id: id,
      name: '晨间瑜伽',
      courseNo: 'CRS2023001',
      description: '适合初学者的晨间瑜伽课程，帮助唤醒身体，开启美好一天',
      category: CourseCategory.YOGA,
      difficulty: CourseDifficulty.BEGINNER,
      duration: 60,
      capacity: 15,
      currentBookings: 8,
      price: 299,
      coachId: 1,
      coachName: '张教练',
      location: 'A区瑜伽室',
      startDate: nextWeek.toISOString().split('T')[0],
      startTime: '08:30:00',
      endTime: '09:30:00',
      status: CourseStatus.SCHEDULED,
      repeatType: 'WEEKLY',
      advanceBookingHours: 24,
      cancelDeadlineHours: 2,
      minParticipants: 3,
      requirements: '请自备瑜伽垫',
      notes: '建议空腹或饭后2小时参加'
    })
  } else {
    // 新增模式：生成课程编号
    formData.courseNo = `CRS${Date.now().toString().slice(-6)}`
  }
}

onMounted(() => {
  initFormData()
})
</script>

<style scoped lang="scss">
.course-form-container {
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

.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 20px;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-date-editor),
:deep(.el-time-picker) {
  width: 100%;
}
</style>