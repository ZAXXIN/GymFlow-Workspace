<template>
  <div class="coach-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/coach' }">教练管理</el-breadcrumb-item>
          <el-breadcrumb-item>教练详情</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="title-section">
          <h1 class="page-title">{{ coachInfo.realName }} - 教练详情</h1>
          <div class="status-badges">
            <el-tag :type="getStatusType(coachInfo.status)" size="small">
              {{ getStatusText(coachInfo.status) }}
            </el-tag>
            <el-tag type="info" size="small" v-if="coachInfo.certification">
              {{ coachInfo.certification }}
            </el-tag>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="handleBack">返回</el-button>
        <el-button type="primary" @click="handleEdit">编辑信息</el-button>
        <el-dropdown @command="handleMoreAction">
          <el-button type="primary">
            更多操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="schedule">查看排班</el-dropdown-item>
              <el-dropdown-item command="courses">查看课程</el-dropdown-item>
              <el-dropdown-item command="performance">查看绩效</el-dropdown-item>
              <el-dropdown-item divided command="export">导出信息</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="detail-content">
      <!-- 左侧信息卡片 -->
      <div class="left-panel">
        <!-- 个人信息卡片 -->
        <el-card shadow="never" class="info-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">个人信息</span>
            </div>
          </template>
          
          <div class="info-content">
            <div class="avatar-section">
              <el-avatar :size="120" :src="coachInfo.user?.avatar || ''">
                {{ coachInfo.realName?.charAt(0) }}
              </el-avatar>
              <div class="avatar-info">
                <h3 class="coach-name">{{ coachInfo.realName }}</h3>
                <p class="coach-no">教练编号: {{ coachInfo.coachNo }}</p>
                <el-rate
                  v-model="coachInfo.rating"
                  disabled
                  show-score
                  text-color="#ff9900"
                  score-template="{value}"
                />
              </div>
            </div>
            
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">手机号：</span>
                <span class="info-value">{{ coachInfo.user?.phone || '未填写' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">邮箱：</span>
                <span class="info-value">{{ coachInfo.user?.email || '未填写' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">性别：</span>
                <span class="info-value">{{ getGenderText(coachInfo.gender) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">出生日期：</span>
                <span class="info-value">{{ formatDate(coachInfo.birthDate) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">从业年限：</span>
                <span class="info-value">{{ coachInfo.yearsOfExperience }} 年</span>
              </div>
              <div class="info-item">
                <span class="info-label">时薪：</span>
                <span class="info-value">{{ formatCurrency(coachInfo.hourlyRate) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">佣金比例：</span>
                <span class="info-value">{{ coachInfo.commissionRate }}%</span>
              </div>
              <div class="info-item full-width">
                <span class="info-label">专业领域：</span>
                <span class="info-value">{{ coachInfo.specialization }}</span>
              </div>
              <div class="info-item full-width">
                <span class="info-label">资格证书：</span>
                <span class="info-value">{{ coachInfo.certification || '未填写' }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 教学统计 -->
        <el-card shadow="never" class="stats-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">教学统计</span>
            </div>
          </template>
          
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-icon total-sessions">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ coachInfo.totalSessions }}</div>
                <div class="stat-label">总课时</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon total-sales">
                <el-icon><Money /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ formatCurrency(coachInfo.totalSales) }}</div>
                <div class="stat-label">总销售额</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon rating">
                <el-icon><Star /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ coachInfo.rating.toFixed(1) }}</div>
                <div class="stat-label">评分</div>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon completion">
                <el-icon><Finished /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-value">98%</div>
                <div class="stat-label">完成率</div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧标签页 -->
      <div class="right-panel">
        <el-tabs v-model="activeTab" class="detail-tabs">
          <!-- 排班安排 -->
          <el-tab-pane label="排班安排" name="schedule">
            <div class="tab-content">
              <div class="schedule-header">
                <div class="date-controls">
                  <el-date-picker
                    v-model="scheduleDate"
                    type="week"
                    format="[第]ww [周] YYYY-MM-DD"
                    placeholder="选择周次"
                    value-format="YYYY-MM-DD"
                    @change="loadSchedule"
                  />
                </div>
                <div class="schedule-legend">
                  <span class="legend-item">
                    <span class="legend-color available"></span>
                    可排班
                  </span>
                  <span class="legend-item">
                    <span class="legend-color booked"></span>
                    已排班
                  </span>
                  <span class="legend-item">
                    <span class="legend-color unavailable"></span>
                    不可排班
                  </span>
                </div>
              </div>
              
              <div class="schedule-table">
                <div class="schedule-header-row">
                  <div class="time-slot">时间</div>
                  <div 
                    v-for="day in weekSchedule" 
                    :key="day.date"
                    class="day-header"
                    :class="{ 'today': day.isToday }"
                  >
                    <div class="day-name">{{ day.weekday }}</div>
                    <div class="day-date">{{ day.date }}</div>
                  </div>
                </div>
                
                <div 
                  v-for="timeSlot in timeSlots" 
                  :key="timeSlot.value"
                  class="schedule-row"
                >
                  <div class="time-label">{{ timeSlot.label }}</div>
                  <div 
                    v-for="day in weekSchedule" 
                    :key="day.date"
                    class="day-cell"
                    :class="getScheduleCellClass(day.date, timeSlot.value)"
                    @click="handleScheduleClick(day.date, timeSlot.value)"
                  >
                    <div v-if="hasSchedule(day.date, timeSlot.value)" class="schedule-info">
                      <div class="course-name">瑜伽课程</div>
                      <div class="course-stats">8/15人</div>
                    </div>
                    <div v-else class="schedule-empty">
                      {{ getScheduleStatus(day.date, timeSlot.value) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 课程列表 -->
          <el-tab-pane label="课程列表" name="courses">
            <div class="tab-content">
              <div class="courses-filter">
                <el-select
                  v-model="courseFilter.status"
                  placeholder="课程状态"
                  style="width: 120px; margin-right: 10px"
                  clearable
                >
                  <el-option label="已排期" value="SCHEDULED" />
                  <el-option label="进行中" value="IN_PROGRESS" />
                  <el-option label="已结束" value="COMPLETED" />
                </el-select>
                
                <el-date-picker
                  v-model="courseFilter.dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                  style="width: 300px; margin-right: 10px"
                />
                
                <el-button type="primary" @click="loadCourses">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
              </div>
              
              <div class="courses-list">
                <div v-for="course in courses" :key="course.id" class="course-card">
                  <div class="course-header">
                    <div class="course-title">
                      <h4>{{ course.name }}</h4>
                      <el-tag :type="getCourseStatusType(course.status)" size="small">
                        {{ getCourseStatusText(course.status) }}
                      </el-tag>
                    </div>
                    <div class="course-meta">
                      <span class="meta-item">
                        <el-icon><Clock /></el-icon>
                        {{ course.startTime }} - {{ course.endTime }}
                      </span>
                      <span class="meta-item">
                        <el-icon><Location /></el-icon>
                        {{ course.location }}
                      </span>
                    </div>
                  </div>
                  
                  <div class="course-content">
                    <div class="course-stats">
                      <div class="stat">
                        <div class="stat-value">{{ course.currentBookings }}/{{ course.capacity }}</div>
                        <div class="stat-label">预约人数</div>
                      </div>
                      <div class="stat">
                        <div class="stat-value">{{ course.duration }}分钟</div>
                        <div class="stat-label">课程时长</div>
                      </div>
                      <div class="stat">
                        <div class="stat-value">{{ formatCurrency(course.price) }}</div>
                        <div class="stat-label">课程价格</div>
                      </div>
                    </div>
                    
                    <div class="course-actions">
                      <el-button size="small" @click="viewCourse(course)">查看详情</el-button>
                      <el-button size="small" type="primary" @click="viewBookings(course)">
                        查看预约
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>
              
              <el-empty v-if="courses.length === 0" description="暂无课程数据" />
            </div>
          </el-tab-pane>

          <!-- 学员管理 -->
          <el-tab-pane label="学员管理" name="students">
            <div class="tab-content">
              <AppTable
                :data="students"
                :loading="studentsLoading"
                :columns="studentColumns"
                :pagination="studentPagination"
                @page-change="handleStudentPageChange"
              >
                <template #status="{ row }">
                  <el-tag :type="getMemberStatusType(row.status)" size="small">
                    {{ getMemberStatusText(row.status) }}
                  </el-tag>
                </template>

                <template #actions="{ row }">
                  <el-button type="text" size="small" @click="viewStudentDetail(row)">
                    查看
                  </el-button>
                  <el-button type="text" size="small" @click="createTrainingPlan(row)">
                    制定计划
                  </el-button>
                </template>
              </AppTable>
            </div>
          </el-tab-pane>

          <!-- 绩效统计 -->
          <el-tab-pane label="绩效统计" name="performance">
            <div class="tab-content">
              <div class="performance-filters">
                <el-radio-group v-model="performancePeriod" @change="loadPerformanceData">
                  <el-radio-button label="week">本周</el-radio-button>
                  <el-radio-button label="month">本月</el-radio-button>
                  <el-radio-button label="quarter">本季</el-radio-button>
                  <el-radio-button label="year">本年</el-radio-button>
                </el-radio-group>
                
                <el-date-picker
                  v-model="performanceDate"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  value-format="YYYY-MM-DD"
                  style="width: 300px; margin-left: 20px"
                  @change="loadPerformanceData"
                />
              </div>
              
              <div class="performance-charts">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-card shadow="never">
                      <template #header>
                        <span>课时统计</span>
                      </template>
                      <div ref="sessionsChart" style="height: 300px;"></div>
                    </el-card>
                  </el-col>
                  <el-col :span="12">
                    <el-card shadow="never">
                      <template #header>
                        <span>收入统计</span>
                      </template>
                      <div ref="revenueChart" style="height: 300px;"></div>
                    </el-card>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20" style="margin-top: 20px;">
                  <el-col :span="24">
                    <el-card shadow="never">
                      <template #header>
                        <span>学员评分趋势</span>
                      </template>
                      <div ref="ratingChart" style="height: 300px;"></div>
                    </el-card>
                  </el-col>
                </el-row>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowDown,
  Calendar,
  Money,
  Star,
  Finished,
  Search,
  Clock,
  Location
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import AppTable from '@/components/common/AppTable.vue'
import type { 
  Coach, 
  Course, 
  Member,
  CoachStatus,
  CourseStatus,
  MemberStatus
} from '@/types'

const router = useRouter()
const route = useRoute()

// 教练信息
const coachInfo = ref<Coach>({
  id: 0,
  userId: 0,
  coachNo: '',
  realName: '',
  gender: 0,
  specialization: '',
  certification: '',
  yearsOfExperience: 0,
  hourlyRate: 0,
  commissionRate: 0,
  totalSessions: 0,
  totalSales: 0,
  rating: 0,
  status: 'ACTIVE',
  createdAt: '',
  updatedAt: ''
})

// 加载状态
const loading = ref(false)

// 当前激活的标签页
const activeTab = ref('schedule')

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return '未填写'
  return new Date(dateString).toLocaleDateString('zh-CN')
}

// 格式化货币
const formatCurrency = (amount: number) => {
  return `¥${amount.toFixed(2)}`
}

// 获取性别文本
const getGenderText = (gender: number) => {
  return gender === 1 ? '男' : gender === 2 ? '女' : '未知'
}

// 获取状态样式
const getStatusType = (status: CoachStatus) => {
  const map: Record<CoachStatus, string> = {
    ACTIVE: 'success',
    INACTIVE: 'info',
    ON_LEAVE: 'warning'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: CoachStatus) => {
  const map: Record<CoachStatus, string> = {
    ACTIVE: '在职',
    INACTIVE: '离职',
    ON_LEAVE: '休假'
  }
  return map[status] || '未知'
}

// 获取会员状态样式
const getMemberStatusType = (status: MemberStatus) => {
  const map: Record<MemberStatus, string> = {
    ACTIVE: 'success',
    INACTIVE: 'info',
    SUSPENDED: 'warning',
    EXPIRED: 'danger'
  }
  return map[status] || 'info'
}

// 获取会员状态文本
const getMemberStatusText = (status: MemberStatus) => {
  const map: Record<MemberStatus, string> = {
    ACTIVE: '活跃',
    INACTIVE: '未激活',
    SUSPENDED: '暂停',
    EXPIRED: '已过期'
  }
  return map[status] || '未知'
}

// 获取课程状态样式
const getCourseStatusType = (status: CourseStatus) => {
  const map: Record<CourseStatus, string> = {
    SCHEDULED: 'primary',
    IN_PROGRESS: 'success',
    COMPLETED: 'info',
    CANCELLED: 'danger'
  }
  return map[status] || 'info'
}

// 获取课程状态文本
const getCourseStatusText = (status: CourseStatus) => {
  const map: Record<CourseStatus, string> = {
    SCHEDULED: '已排期',
    IN_PROGRESS: '进行中',
    COMPLETED: '已结束',
    CANCELLED: '已取消'
  }
  return map[status] || '未知'
}

// 返回列表
const handleBack = () => {
  router.push('/coach')
}

// 编辑信息
const handleEdit = () => {
  router.push(`/coach/edit/${coachInfo.value.id}`)
}

// 更多操作
const handleMoreAction = (command: string) => {
  switch (command) {
    case 'schedule':
      activeTab.value = 'schedule'
      break
    case 'courses':
      activeTab.value = 'courses'
      break
    case 'performance':
      activeTab.value = 'performance'
      break
    case 'export':
      handleExport()
      break
  }
}

// 导出信息
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// ========== 排班相关 ==========
const scheduleDate = ref('')
const weekSchedule = ref<any[]>([])
const timeSlots = [
  { label: '09:00-10:00', value: 'morning1' },
  { label: '10:00-11:00', value: 'morning2' },
  { label: '11:00-12:00', value: 'morning3' },
  { label: '14:00-15:00', value: 'afternoon1' },
  { label: '15:00-16:00', value: 'afternoon2' },
  { label: '16:00-17:00', value: 'afternoon3' },
  { label: '19:00-20:00', value: 'evening1' },
  { label: '20:00-21:00', value: 'evening2' },
  { label: '21:00-22:00', value: 'evening3' }
]

// 加载排班
const loadSchedule = () => {
  // 模拟数据：生成一周的日期
  const baseDate = scheduleDate.value ? new Date(scheduleDate.value) : new Date()
  const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  
  weekSchedule.value = weekdays.map((weekday, index) => {
    const date = new Date(baseDate)
    date.setDate(baseDate.getDate() + index)
    const dateStr = date.toISOString().split('T')[0]
    
    return {
      weekday,
      date: dateStr,
      isToday: dateStr === new Date().toISOString().split('T')[0]
    }
  })
}

// 获取排班单元格类名
const getScheduleCellClass = (date: string, timeSlot: string) => {
  // 模拟逻辑：根据日期和时间段确定状态
  const dayIndex = weekSchedule.value.findIndex(d => d.date === date)
  const isAvailable = dayIndex < 5 // 周一到周五可排班
  
  if (!isAvailable) return 'unavailable'
  
  const hasSchedule = Math.random() > 0.7 // 70%的概率有排班
  return hasSchedule ? 'booked' : 'available'
}

// 检查是否有排班
const hasSchedule = (date: string, timeSlot: string) => {
  return getScheduleCellClass(date, timeSlot) === 'booked'
}

// 获取排班状态文本
const getScheduleStatus = (date: string, timeSlot: string) => {
  const className = getScheduleCellClass(date, timeSlot)
  switch (className) {
    case 'available': return '可预约'
    case 'booked': return '已排班'
    case 'unavailable': return '不可排'
    default: return ''
  }
}

// 排班单元格点击
const handleScheduleClick = (date: string, timeSlot: string) => {
  const className = getScheduleCellClass(date, timeSlot)
  if (className === 'available') {
    ElMessage.info(`在 ${date} ${timeSlot} 时间段安排课程`)
  }
}

// ========== 课程相关 ==========
const courses = ref<Course[]>([])
const courseFilter = reactive({
  status: '',
  dateRange: []
})

// 加载课程
const loadCourses = () => {
  // 模拟数据
  courses.value = [
    {
      id: 1,
      courseNo: 'CRS2023001',
      name: '晨间瑜伽',
      description: '适合初学者的晨间瑜伽课程',
      category: 'YOGA',
      difficulty: 'BEGINNER',
      duration: 60,
      capacity: 15,
      currentBookings: 8,
      price: 199,
      coachId: coachInfo.value.id,
      coachName: coachInfo.value.realName,
      location: 'A区瑜伽室',
      startTime: '08:30:00',
      endTime: '09:30:00',
      status: 'SCHEDULED',
      createdAt: '2023-06-01 10:00:00',
      updatedAt: '2023-06-01 10:00:00'
    },
    {
      id: 2,
      courseNo: 'CRS2023002',
      name: '力量训练',
      description: '中级力量训练课程',
      category: 'BODYBUILDING',
      difficulty: 'INTERMEDIATE',
      duration: 90,
      capacity: 10,
      currentBookings: 10,
      price: 299,
      coachId: coachInfo.value.id,
      coachName: coachInfo.value.realName,
      location: '力量训练区',
      startTime: '14:30:00',
      endTime: '16:00:00',
      status: 'COMPLETED',
      createdAt: '2023-06-01 10:00:00',
      updatedAt: '2023-06-01 10:00:00'
    },
    {
      id: 3,
      courseNo: 'CRS2023003',
      name: '私教课程',
      description: '个性化训练指导',
      category: 'PERSONAL_TRAINING',
      difficulty: 'ADVANCED',
      duration: 60,
      capacity: 1,
      currentBookings: 1,
      price: 500,
      coachId: coachInfo.value.id,
      coachName: coachInfo.value.realName,
      location: '私教区',
      startTime: '19:00:00',
      endTime: '20:00:00',
      status: 'IN_PROGRESS',
      createdAt: '2023-06-01 10:00:00',
      updatedAt: '2023-06-01 10:00:00'
    }
  ]
}

// 查看课程详情
const viewCourse = (course: Course) => {
  router.push(`/course/detail/${course.id}`)
}

// 查看课程预约
const viewBookings = (course: Course) => {
  router.push(`/booking?courseId=${course.id}`)
}

// ========== 学员相关 ==========
const students = ref<Member[]>([])
const studentsLoading = ref(false)
const studentPagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const studentColumns = [
  { prop: 'memberNo', label: '会员编号', width: 120 },
  { prop: 'realName', label: '姓名', width: 100 },
  { prop: 'gender', label: '性别', width: 80, formatter: (row: Member) => getGenderText(row.gender) },
  { prop: 'remainingSessions', label: '剩余课时', width: 100 },
  { prop: 'status', label: '状态', slot: true, width: 100 },
  { prop: 'membershipEndDate', label: '有效期至', width: 120 },
  { label: '操作', slot: true, width: 150 }
]

// 加载学员
const loadStudents = () => {
  studentsLoading.value = true
  
  // 模拟数据
  setTimeout(() => {
    students.value = [
      {
        id: 1,
        userId: 101,
        memberNo: 'M001',
        realName: '张三',
        gender: 1,
        birthDate: '1990-01-01',
        height: 175,
        weight: 70,
        bmi: 22.9,
        fitnessGoal: '增肌减脂',
        medicalHistory: '',
        emergencyContact: '李四',
        emergencyPhone: '13800138000',
        status: 'ACTIVE',
        remainingSessions: 10,
        totalSessions: 30,
        membershipStartDate: '2023-01-01',
        membershipEndDate: '2024-01-01',
        createdAt: '2023-01-01 10:00:00',
        updatedAt: '2023-06-01 10:00:00'
      },
      {
        id: 2,
        userId: 102,
        memberNo: 'M002',
        realName: '李四',
        gender: 2,
        birthDate: '1992-05-15',
        height: 165,
        weight: 55,
        bmi: 20.2,
        fitnessGoal: '塑形',
        medicalHistory: '',
        emergencyContact: '王五',
        emergencyPhone: '13900139000',
        status: 'ACTIVE',
        remainingSessions: 5,
        totalSessions: 20,
        membershipStartDate: '2023-02-01',
        membershipEndDate: '2024-02-01',
        createdAt: '2023-02-01 10:00:00',
        updatedAt: '2023-06-01 10:00:00'
      }
    ]
    
    studentPagination.total = students.value.length
    studentsLoading.value = false
  }, 500)
}

// 分页变化
const handleStudentPageChange = (page: number) => {
  studentPagination.page = page
  loadStudents()
}

// 查看学员详情
const viewStudentDetail = (student: Member) => {
  router.push(`/member/detail/${student.id}`)
}

// 制定训练计划
const createTrainingPlan = (student: Member) => {
  router.push(`/training-plan/create?memberId=${student.id}&coachId=${coachInfo.value.id}`)
}

// ========== 绩效统计相关 ==========
const performancePeriod = ref('month')
const performanceDate = ref<string[]>([])
const sessionsChart = ref<HTMLElement>()
const revenueChart = ref<HTMLElement>()
const ratingChart = ref<HTMLElement>()

// 加载绩效数据
const loadPerformanceData = () => {
  initCharts()
}

// 初始化图表
const initCharts = () => {
  nextTick(() => {
    if (sessionsChart.value && revenueChart.value && ratingChart.value) {
      // 课时统计图表
      const sessionsChartInstance = echarts.init(sessionsChart.value)
      sessionsChartInstance.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: { type: 'value' },
        series: [{
          name: '课时数',
          type: 'bar',
          data: [3, 4, 2, 5, 4, 3, 2],
          itemStyle: { color: '#409eff' }
        }]
      })

      // 收入统计图表
      const revenueChartInstance = echarts.init(revenueChart.value)
      revenueChartInstance.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月']
        },
        yAxis: { type: 'value' },
        series: [{
          name: '收入',
          type: 'line',
          data: [15000, 18000, 22000, 19500, 24000, 28000],
          itemStyle: { color: '#67c23a' }
        }]
      })

      // 评分趋势图表
      const ratingChartInstance = echarts.init(ratingChart.value)
      ratingChartInstance.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月']
        },
        yAxis: { 
          type: 'value',
          min: 4,
          max: 5
        },
        series: [{
          name: '评分',
          type: 'line',
          smooth: true,
          data: [4.5, 4.6, 4.7, 4.8, 4.9, 4.8],
          itemStyle: { color: '#e6a23c' }
        }]
      })
    }
  })
}

// ========== 初始化 ==========
const initCoachData = () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('未找到教练信息')
    router.push('/coach')
    return
  }

  loading.value = true

  // 模拟从API获取数据
  setTimeout(() => {
    coachInfo.value = {
      id: Number(id),
      userId: 1001,
      coachNo: 'C001',
      realName: '张教练',
      gender: 1,
      birthDate: '1985-05-15',
      specialization: '瑜伽、普拉提',
      certification: '国家健身教练职业资格证',
      yearsOfExperience: 8,
      hourlyRate: 300,
      commissionRate: 20,
      totalSessions: 500,
      totalSales: 150000,
      rating: 4.8,
      status: 'ACTIVE',
      createdAt: '2020-01-01 10:00:00',
      updatedAt: '2023-06-01 10:00:00',
      user: {
        id: 1001,
        username: 'coach_zhang',
        email: 'coach.zhang@example.com',
        phone: '13800138000',
        avatar: 'https://via.placeholder.com/150',
        role: 'COACH',
        status: 1,
        createdAt: '2020-01-01 10:00:00',
        updatedAt: '2023-06-01 10:00:00'
      }
    }
    
    loading.value = false
    
    // 初始化排班
    loadSchedule()
    
    // 加载课程
    loadCourses()
    
    // 加载学员
    loadStudents()
    
    // 初始化图表
    initCharts()
  }, 500)
}

onMounted(() => {
  initCoachData()
})
</script>

<style scoped lang="scss">
.coach-detail-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

  .header-left {
    flex: 1;
    
    .title-section {
      margin-top: 10px;
      
      .page-title {
        margin: 0 0 10px 0;
        font-size: 20px;
        font-weight: 600;
        color: #303133;
      }
      
      .status-badges {
        display: flex;
        gap: 10px;
      }
    }
  }

  .header-actions {
    display: flex;
    gap: 10px;
    align-items: flex-start;
  }
}

.detail-content {
  display: flex;
  gap: 20px;
}

.left-panel {
  width: 400px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.info-card {
  border: 1px solid #ebeef5;

  .card-header {
    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.info-content {
  .avatar-section {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #ebeef5;

    .avatar-info {
      margin-left: 20px;
      
      .coach-name {
        margin: 0 0 5px 0;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
      }
      
      .coach-no {
        margin: 0 0 10px 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }

  .info-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;

    .info-item {
      display: flex;
      align-items: center;
      width: calc(50% - 8px);
      margin-bottom: 8px;

      &.full-width {
        width: 100%;
      }

      .info-label {
        min-width: 80px;
        font-size: 14px;
        color: #909399;
      }

      .info-value {
        flex: 1;
        font-size: 14px;
        color: #303133;
        font-weight: 500;
        word-break: break-all;
      }
    }
  }
}

.stats-card {
  border: 1px solid #ebeef5;

  .stats-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;

    .stat-item {
      display: flex;
      align-items: center;
      padding: 16px;
      background: #f9fafc;
      border-radius: 8px;
      transition: all 0.3s;

      &:hover {
        background: #f0f9ff;
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16px;
        font-size: 24px;
        
        &.total-sessions {
          background: rgba(64, 158, 255, 0.1);
          color: #409eff;
        }
        
        &.total-sales {
          background: rgba(103, 194, 58, 0.1);
          color: #67c23a;
        }
        
        &.rating {
          background: rgba(230, 162, 60, 0.1);
          color: #e6a23c;
        }
        
        &.completion {
          background: rgba(144, 147, 153, 0.1);
          color: #909399;
        }
      }

      .stat-content {
        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }
}

.detail-tabs {
  flex: 1;
  
  :deep(.el-tabs__header) {
    margin: 0;
  }
  
  :deep(.el-tabs__content) {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
}

.tab-content {
  flex: 1;
  padding: 20px;
  background: #fff;
  border-radius: 0 0 8px 8px;
  border: 1px solid #ebeef5;
  border-top: none;
}

// 排班样式
.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .schedule-legend {
    display: flex;
    gap: 20px;
    
    .legend-item {
      display: flex;
      align-items: center;
      font-size: 14px;
      color: #606266;
      
      .legend-color {
        width: 16px;
        height: 16px;
        border-radius: 4px;
        margin-right: 6px;
        
        &.available {
          background: #f0f9ff;
          border: 1px solid #409eff;
        }
        
        &.booked {
          background: #f0f9eb;
          border: 1px solid #67c23a;
        }
        
        &.unavailable {
          background: #fef0f0;
          border: 1px solid #f56c6c;
        }
      }
    }
  }
}

.schedule-table {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.schedule-header-row {
  display: flex;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;

  .time-slot {
    width: 120px;
    padding: 12px;
    text-align: center;
    font-weight: 500;
    color: #606266;
    background: #fafafa;
    border-right: 1px solid #ebeef5;
  }

  .day-header {
    flex: 1;
    padding: 12px;
    text-align: center;
    border-right: 1px solid #ebeef5;
    
    &:last-child {
      border-right: none;
    }
    
    &.today {
      background: #f0f9ff;
      
      .day-name, .day-date {
        color: #409eff;
        font-weight: 600;
      }
    }
    
    .day-name {
      font-weight: 500;
      color: #606266;
      margin-bottom: 4px;
    }
    
    .day-date {
      font-size: 12px;
      color: #909399;
    }
  }
}

.schedule-row {
  display: flex;
  border-bottom: 1px solid #ebeef5;

  &:last-child {
    border-bottom: none;
  }

  .time-label {
    width: 120px;
    padding: 12px;
    text-align: center;
    color: #606266;
    background: #fafafa;
    border-right: 1px solid #ebeef5;
    font-size: 14px;
  }

  .day-cell {
    flex: 1;
    min-height: 80px;
    padding: 8px;
    border-right: 1px solid #ebeef5;
    cursor: pointer;
    transition: all 0.3s;
    
    &:last-child {
      border-right: none;
    }
    
    &.available {
      background: #f0f9ff;
      
      &:hover {
        background: #e6f7ff;
      }
      
      .schedule-empty {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;
        color: #409eff;
        font-size: 14px;
      }
    }
    
    &.booked {
      background: #f0f9eb;
      
      .schedule-info {
        .course-name {
          font-size: 14px;
          font-weight: 500;
          color: #67c23a;
          margin-bottom: 4px;
        }
        
        .course-stats {
          font-size: 12px;
          color: #909399;
        }
      }
    }
    
    &.unavailable {
      background: #fef0f0;
      cursor: not-allowed;
      
      .schedule-empty {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;
        color: #f56c6c;
        font-size: 14px;
        opacity: 0.6;
      }
    }
  }
}

// 课程列表样式
.courses-filter {
  margin-bottom: 20px;
}

.courses-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.course-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.1);
  }

  .course-header {
    margin-bottom: 16px;

    .course-title {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;

      h4 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }

    .course-meta {
      display: flex;
      gap: 16px;

      .meta-item {
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #606266;

        .el-icon {
          margin-right: 4px;
        }
      }
    }
  }

  .course-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .course-stats {
      display: flex;
      gap: 24px;

      .stat {
        text-align: center;

        .stat-value {
          font-size: 18px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }

        .stat-label {
          font-size: 12px;
          color: #909399;
        }
      }
    }

    .course-actions {
      display: flex;
      gap: 8px;
    }
  }
}

// 绩效统计样式
.performance-filters {
  margin-bottom: 20px;
}

.performance-charts {
  .el-row {
    margin-bottom: 0;
  }
}

// 通用样式
:deep(.el-card__header) {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 20px;
}

:deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
}
</style>