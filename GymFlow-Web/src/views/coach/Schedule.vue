<template>
  <div class="course-schedule-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/coach/list' }">教练管理</el-breadcrumb-item>
          <el-breadcrumb-item>排班管理</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">{{ coachName }} - 排班管理</h1>
      </div>
      <div class="header-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button type="primary" @click="handleAddCourse">
          <el-icon><Plus /></el-icon>
          添加课程
        </el-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-content">
        <div class="filter-left">
          <el-date-picker
            v-model="currentMonth"
            type="month"
            placeholder="选择月份"
            format="YYYY年MM月"
            value-format="YYYY-MM"
            @change="handleMonthChange"
            style="width: 140px;"
          />
          <el-select 
            v-model="courseTypeFilter" 
            placeholder="课程类型" 
            clearable
            style="width: 120px; margin-left: 10px;"
          >
            <el-option label="全部" :value="null" />
            <el-option label="私教课" :value="0" />
            <el-option label="团课" :value="1" />
          </el-select>
          <el-button 
            type="primary" 
            @click="loadData"
            style="margin-left: 10px;"
            :loading="loading"
          >
            查询
          </el-button>
        </div>
        <div class="filter-right">
          <el-button-group>
            <el-button 
              :type="viewMode === 'month' ? 'primary' : ''"
              @click="viewMode = 'month'"
            >
              月视图
            </el-button>
            <el-button 
              :type="viewMode === 'week' ? 'primary' : ''"
              @click="viewMode = 'week'"
            >
              周视图
            </el-button>
          </el-button-group>
        </div>
      </div>
    </el-card>

    <!-- 月视图课程表 -->
    <el-card class="schedule-card" v-if="viewMode === 'month'" v-loading="loading">
      <div class="month-view">
        <!-- 日历头部 -->
        <div class="calendar-header">
          <div class="current-month">{{ displayMonth }}</div>
          <div class="weekday-header">
            <div class="weekday-cell" v-for="weekday in weekdays" :key="weekday">
              {{ weekday }}
            </div>
          </div>
        </div>

        <!-- 日历主体 -->
        <div class="calendar-body">
          <div 
            v-for="week in monthWeeks" 
            :key="week.weekNum"
            class="calendar-week"
          >
            <div 
              v-for="day in week.days" 
              :key="day.date"
              class="calendar-day"
              :class="{
                'today': day.isToday,
                'current-month': day.isCurrentMonth,
                'weekend': day.isWeekend,
                'has-courses': day.courses && day.courses.length > 0
              }"
              @click="handleDayClick(day)"
            >
              <div class="day-header">
                <span class="day-number">{{ day.day }}</span>
                <span class="day-of-week">{{ day.dayOfWeek }}</span>
              </div>
              
              <div class="day-courses" v-if="day.courses && day.courses.length > 0">
                <div 
                  v-for="course in day.courses" 
                  :key="course.id"
                  class="course-item"
                  :class="`course-type-${course.scheduleType}`"
                  @click.stop="handleCourseClick(course)"
                >
                  <div class="course-time">
                    {{ formatTime(course.startTime) }} - {{ formatTime(course.endTime) }}
                  </div>
                  <div class="course-type">
                    <el-tag 
                      :type="course.scheduleType === 0 ? 'primary' : 'success'"
                      size="small"
                      effect="plain"
                    >
                      {{ course.scheduleTypeDesc }}
                    </el-tag>
                  </div>
                  <div class="course-name" :title="course.notes">
                    {{ course.notes || '课程' }}
                  </div>
                </div>
              </div>
              
              <div v-else class="empty-day" @click.stop="handleAddCourseForDay(day)">
                <el-icon><Plus /></el-icon>
                <span>添加课程</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 周视图课程表 -->
    <el-card class="schedule-card" v-if="viewMode === 'week'" v-loading="loading">
      <div class="week-view">
        <!-- 周视图头部 -->
        <div class="week-header">
          <div class="time-column"></div>
          <div 
            v-for="day in weekDays" 
            :key="day.date"
            class="day-header"
            :class="{ 'today': day.isToday, 'weekend': day.isWeekend }"
          >
            <div class="date-info">
              <div class="date">{{ day.date.split('-')[2] }}</div>
              <div class="day-of-week">{{ day.dayOfWeek }}</div>
            </div>
            <div class="month">{{ day.date.split('-')[1] }}月</div>
          </div>
        </div>

        <!-- 时间轴 -->
        <div class="week-body">
          <div class="time-grid">
            <div 
              v-for="time in timeSlots" 
              :key="time"
              class="time-slot"
            >
              {{ time }}
            </div>
          </div>

          <div class="courses-grid">
            <div 
              v-for="day in weekDays" 
              :key="day.date"
              class="day-column"
              :class="{ 'today': day.isToday, 'weekend': day.isWeekend }"
            >
              <div 
                v-for="time in timeSlots" 
                :key="`${day.date}-${time}`"
                class="time-cell"
                @click="handleAddCourseForTime(day, time)"
              >
                <!-- 显示该时间段的课程 -->
                <template v-if="getCourseForTime(day.date, time)">
                  <div 
                    class="course-block"
                    :class="`course-type-${getCourseForTime(day.date, time)?.scheduleType}`"
                    :style="getCourseBlockStyle(getCourseForTime(day.date, time))"
                    @click.stop="handleCourseClick(getCourseForTime(day.date, time))"
                  >
                    <div class="course-block-content">
                      <div class="course-time">
                        {{ formatTime(getCourseForTime(day.date, time)?.startTime) }}
                      </div>
                      <div class="course-type">
                        {{ getCourseForTime(day.date, time)?.scheduleTypeDesc }}
                      </div>
                      <div class="course-notes" :title="getCourseForTime(day.date, time)?.notes">
                        {{ truncateText(getCourseForTime(day.date, time)?.notes || '课程', 10) }}
                      </div>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 课程详情弹窗 -->
    <el-dialog
      v-model="courseDialog.visible"
      :title="courseDialog.title"
      width="500px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="courseRules"
        label-width="100px"
        v-loading="courseDialog.loading"
      >
        <el-form-item label="课程日期" prop="scheduleDate">
          <el-date-picker
            v-model="courseForm.scheduleDate"
            type="date"
            placeholder="选择课程日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled="courseDialog.mode === 'edit'"
          />
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-time-select
            v-model="courseForm.startTime"
            :max-time="courseForm.endTime"
            placeholder="选择开始时间"
            start="06:00"
            step="00:30"
            end="22:00"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-time-select
            v-model="courseForm.endTime"
            :min-time="courseForm.startTime"
            placeholder="选择结束时间"
            start="06:00"
            step="00:30"
            end="22:00"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="课程类型" prop="scheduleType">
          <el-select 
            v-model="courseForm.scheduleType"
            placeholder="请选择课程类型"
            style="width: 100%"
          >
            <el-option label="私教课" :value="0" />
            <el-option label="团课" :value="1" />
          </el-select>
        </el-form-item>

        <el-form-item label="课程名称" prop="notes">
          <el-input
            v-model="courseForm.notes"
            placeholder="请输入课程名称（如：瑜伽入门、动感单车等）"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="上课地点" prop="location">
          <el-input
            v-model="courseForm.location"
            placeholder="请输入上课地点"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="courseForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入课程备注"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose" :disabled="courseDialog.loading">取消</el-button>
          <template v-if="courseDialog.mode === 'edit'">
            <el-button 
              type="danger" 
              @click="handleDeleteCourse"
              :loading="courseDialog.loading"
            >
              删除
            </el-button>
          </template>
          <el-button 
            type="primary" 
            @click="handleSaveCourse" 
            :loading="courseDialog.loading"
          >
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useCoachStore } from '@/stores/coach'
import type { CoachScheduleDTO } from '@/types/coach'

const router = useRouter()
const route = useRoute()
const coachStore = useCoachStore()

const loading = ref(false)
const coachId = computed(() => Number(route.params.id))
const coachName = ref('')

// 视图模式
const viewMode = ref<'month' | 'week'>('month')

// 筛选条件
const currentMonth = ref<string>('')
const courseTypeFilter = ref<number | null>(null)

// 课程数据
const courses = ref<any[]>([])

// 日期相关
const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const timeSlots = [
  '06:00', '07:00', '08:00', '09:00', '10:00', '11:00',
  '12:00', '13:00', '14:00', '15:00', '16:00', '17:00',
  '18:00', '19:00', '20:00', '21:00', '22:00'
]

// 课程弹窗
const courseFormRef = ref<FormInstance>()
const courseDialog = reactive({
  visible: false,
  mode: 'add', // 'add' | 'edit'
  title: '',
  loading: false,
  editingCourseId: null as number | null
})

const courseForm = reactive({
  scheduleDate: '',
  startTime: '',
  endTime: '',
  scheduleType: 0,
  notes: '',
  location: '',
  remark: ''
})

const courseRules: FormRules = {
  scheduleDate: [
    { required: true, message: '请选择课程日期', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  scheduleType: [
    { required: true, message: '请选择课程类型', trigger: 'change' }
  ],
  notes: [
    { required: true, message: '请输入课程名称', trigger: 'blur' }
  ]
}

// 计算属性
const displayMonth = computed(() => {
  if (!currentMonth.value) return formatDate(new Date(), 'YYYY年MM月')
  const [year, month] = currentMonth.value.split('-')
  return `${year}年${month}月`
})

// 月视图的日历数据
const monthWeeks = computed(() => {
  const result = []
  const [year, month] = currentMonth.value.split('-').map(Number)
  const firstDay = new Date(year, month - 1, 1)
  const lastDay = new Date(year, month, 0)
  
  // 获取当月第一天是周几（0-6，0是周日）
  const firstDayOfWeek = firstDay.getDay()
  
  // 获取上个月的最后几天
  const prevMonthLastDay = new Date(year, month - 1, 0).getDate()
  
  let currentDate = 1
  let nextMonthDate = 1
  
  for (let week = 0; week < 6; week++) {
    const days = []
    
    for (let dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
      let date: Date
      let isCurrentMonth = true
      
      if (week === 0 && dayOfWeek < firstDayOfWeek) {
        // 上个月的日期
        const prevMonth = month - 2 < 0 ? 11 : month - 2
        const prevYear = month - 2 < 0 ? year - 1 : year
        date = new Date(prevYear, prevMonth, prevMonthLastDay - firstDayOfWeek + dayOfWeek + 1)
        isCurrentMonth = false
      } else if (currentDate > lastDay.getDate()) {
        // 下个月的日期
        const nextMonth = month > 11 ? 0 : month
        const nextYear = month > 11 ? year + 1 : year
        date = new Date(nextYear, nextMonth, nextMonthDate++)
        isCurrentMonth = false
      } else {
        // 当月的日期
        date = new Date(year, month - 1, currentDate++)
      }
      
      const dateStr = formatDate(date, 'YYYY-MM-DD')
      const today = new Date()
      const isToday = formatDate(today, 'YYYY-MM-DD') === dateStr
      const isWeekend = date.getDay() === 0 || date.getDay() === 6
      
      // 获取当天的课程
      const dayCourses = courses.value.filter(course => 
        course.scheduleDate === dateStr
      )
      
      days.push({
        date: dateStr,
        day: date.getDate(),
        dayOfWeek: weekdays[date.getDay()],
        isToday,
        isCurrentMonth,
        isWeekend,
        courses: dayCourses
      })
    }
    
    result.push({
      weekNum: week + 1,
      days
    })
    
    // 如果已经显示完所有日期，并且下个月的日期也从第一天开始，就结束循环
    if (currentDate > lastDay.getDate() && nextMonthDate > 1) {
      break
    }
  }
  
  return result
})

// 周视图的日期数据
const weekDays = computed(() => {
  const result = []
  const today = new Date()
  const currentDate = currentMonth.value ? 
    new Date(currentMonth.value + '-01') : today
  
  // 找到本周一
  const monday = new Date(currentDate)
  const day = monday.getDay()
  const diff = monday.getDate() - day + (day === 0 ? -6 : 1)
  monday.setDate(diff)
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(monday)
    date.setDate(monday.getDate() + i)
    
    const dateStr = formatDate(date, 'YYYY-MM-DD')
    const todayStr = formatDate(today, 'YYYY-MM-DD')
    const isToday = dateStr === todayStr
    const isWeekend = date.getDay() === 0 || date.getDay() === 6
    
    result.push({
      date: dateStr,
      dayOfWeek: weekdays[date.getDay()],
      isToday,
      isWeekend,
      courses: courses.value.filter(course => course.scheduleDate === dateStr)
    })
  }
  
  return result
})

// 工具函数
const formatDate = (date: Date, format: string) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  
  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
}

const formatTime = (time: string) => {
  if (!time) return ''
  return time.slice(0, 5)
}

const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const getCourseForTime = (date: string, time: string) => {
  return courses.value.find(course => {
    if (course.scheduleDate !== date) return false
    const courseStart = formatTime(course.startTime)
    const courseEnd = formatTime(course.endTime)
    return courseStart === time
  })
}

const getCourseBlockStyle = (course: any) => {
  if (!course) return {}
  
  const startTime = new Date(`2000-01-01T${course.startTime}`)
  const endTime = new Date(`2000-01-01T${course.endTime}`)
  const durationMinutes = (endTime.getTime() - startTime.getTime()) / (1000 * 60)
  
  return {
    height: `${(durationMinutes / 60) * 100}%`,
    top: `${(startTime.getHours() - 6 + startTime.getMinutes() / 60) * 100}%`
  }
}

// 加载数据
const loadData = async () => {
  try {
    loading.value = true
    
    // 加载教练信息
    const coachResponse = await coachStore.fetchCoachDetail(coachId.value)
    if (coachResponse.code === 200 && coachResponse.data) {
      coachName.value = coachResponse.data.realName
    }
    
    // 加载排班数据
    const scheduleResponse = await coachStore.fetchCoachSchedules(coachId.value)
    if (scheduleResponse.code === 200 && scheduleResponse.data) {
      // 过滤数据
      let filteredCourses = scheduleResponse.data
      
      // 按课程类型过滤
      if (courseTypeFilter.value !== null) {
        filteredCourses = filteredCourses.filter(
          (course: any) => course.scheduleType === courseTypeFilter.value
        )
      }
      
      // 按月份过滤
      if (currentMonth.value) {
        const [year, month] = currentMonth.value.split('-')
        filteredCourses = filteredCourses.filter((course: any) => {
          const courseDate = new Date(course.scheduleDate)
          return courseDate.getFullYear() === Number(year) && 
                 (courseDate.getMonth() + 1) === Number(month)
        })
      }
      
      // 添加课程类型描述
      filteredCourses = filteredCourses.map((course: any) => ({
        ...course,
        scheduleTypeDesc: course.scheduleType === 0 ? '私教课' : '团课'
      }))
      
      courses.value = filteredCourses
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 初始化当前月份
const initCurrentMonth = () => {
  const today = new Date()
  currentMonth.value = formatDate(today, 'YYYY-MM')
}

// 事件处理
const handleMonthChange = () => {
  loadData()
}

const handleDayClick = (day: any) => {
  if (day.courses && day.courses.length > 0) {
    // 如果有课程，显示第一个课程的详情
    handleCourseClick(day.courses[0])
  } else {
    // 否则添加课程
    handleAddCourseForDay(day)
  }
}

const handleCourseClick = (course: any) => {
  courseDialog.mode = 'edit'
  courseDialog.title = '编辑课程'
  courseDialog.editingCourseId = course.id
  courseDialog.visible = true
  
  Object.assign(courseForm, {
    scheduleDate: course.scheduleDate,
    startTime: formatTime(course.startTime),
    endTime: formatTime(course.endTime),
    scheduleType: course.scheduleType,
    notes: course.notes || '',
    location: course.location || '',
    remark: ''
  })
}

const handleAddCourse = () => {
  courseDialog.mode = 'add'
  courseDialog.title = '添加课程'
  courseDialog.editingCourseId = null
  courseDialog.visible = true
  
  // 重置表单
  Object.assign(courseForm, {
    scheduleDate: formatDate(new Date(), 'YYYY-MM-DD'),
    startTime: '09:00',
    endTime: '10:00',
    scheduleType: 0,
    notes: '',
    location: '',
    remark: ''
  })
}

const handleAddCourseForDay = (day: any) => {
  courseDialog.mode = 'add'
  courseDialog.title = '添加课程'
  courseDialog.editingCourseId = null
  courseDialog.visible = true
  
  Object.assign(courseForm, {
    scheduleDate: day.date,
    startTime: '09:00',
    endTime: '10:00',
    scheduleType: 0,
    notes: '',
    location: '',
    remark: ''
  })
}

const handleAddCourseForTime = (day: any, time: string) => {
  courseDialog.mode = 'add'
  courseDialog.title = '添加课程'
  courseDialog.editingCourseId = null
  courseDialog.visible = true
  
  const [hours, minutes] = time.split(':')
  const endTime = new Date()
  endTime.setHours(parseInt(hours) + 1)
  endTime.setMinutes(parseInt(minutes))
  
  Object.assign(courseForm, {
    scheduleDate: day.date,
    startTime: time,
    endTime: `${endTime.getHours().toString().padStart(2, '0')}:${endTime.getMinutes().toString().padStart(2, '0')}`,
    scheduleType: 0,
    notes: '',
    location: '',
    remark: ''
  })
}

const handleSaveCourse = async () => {
  if (!courseFormRef.value) return
  
  try {
    await courseFormRef.value.validate()
    
    courseDialog.loading = true
    
    const courseData: CoachScheduleDTO = {
      scheduleDate: courseForm.scheduleDate,
      startTime: courseForm.startTime + ':00',
      endTime: courseForm.endTime + ':00',
      scheduleType: courseForm.scheduleType,
      notes: courseForm.notes,
      location: courseForm.location,
      remark: courseForm.remark
    }
    
    if (courseDialog.mode === 'add') {
      await coachStore.addCoachSchedule(coachId.value, courseData)
      ElMessage.success('添加课程成功')
    } else if (courseDialog.editingCourseId) {
      await coachStore.updateCoachSchedule(courseDialog.editingCourseId, courseData)
      ElMessage.success('更新课程成功')
    }
    
    courseDialog.visible = false
    loadData()
  } catch (error) {
    console.error('保存课程失败:', error)
    ElMessage.error('保存课程失败')
  } finally {
    courseDialog.loading = false
  }
}

const handleDeleteCourse = async () => {
  if (!courseDialog.editingCourseId) return
  
  try {
    await ElMessageBox.confirm('确定要删除这个课程吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    courseDialog.loading = true
    await coachStore.deleteCoachSchedule(courseDialog.editingCourseId)
    ElMessage.success('删除课程成功')
    courseDialog.visible = false
    loadData()
  } catch (error) {
    console.error('删除课程失败:', error)
  } finally {
    courseDialog.loading = false
  }
}

const handleDialogClose = () => {
  courseDialog.visible = false
  courseFormRef.value?.clearValidate()
}

const goBack = () => {
  router.push('/coach/list')
}

// 监听筛选条件变化
watch([currentMonth, courseTypeFilter], () => {
  loadData()
})

onMounted(() => {
  initCurrentMonth()
  loadData()
})
</script>

<style scoped>
.course-schedule-container {
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

.filter-card {
  margin-bottom: 20px;
}

.filter-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-left {
  display: flex;
  align-items: center;
}

.filter-right {
  display: flex;
  align-items: center;
}

.schedule-card {
  margin-bottom: 20px;
  min-height: 600px;
}

/* 月视图样式 */
.month-view {
  height: 100%;
}

.calendar-header {
  margin-bottom: 10px;
}

.current-month {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  text-align: center;
}

.weekday-header {
  display: flex;
  background-color: #f8f9fa;
  border-radius: 4px;
  overflow: hidden;
}

.weekday-cell {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-weight: 600;
  color: #606266;
  border-right: 1px solid #e4e7ed;
}

.weekday-cell:last-child {
  border-right: none;
}

.calendar-body {
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.calendar-week {
  display: flex;
  border-bottom: 1px solid #e4e7ed;
}

.calendar-week:last-child {
  border-bottom: none;
}

.calendar-day {
  flex: 1;
  min-height: 120px;
  padding: 8px;
  border-right: 1px solid #e4e7ed;
  background-color: #fff;
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;
}

.calendar-day:last-child {
  border-right: none;
}

.calendar-day.today {
  background-color: #f0f9ff;
}

.calendar-day.today .day-number {
  color: #409eff;
  font-weight: 600;
}

.calendar-day.weekend {
  background-color: #fafafa;
}

.calendar-day:not(.current-month) {
  background-color: #f9f9f9;
  color: #c0c4cc;
}

.calendar-day:hover {
  background-color: #f5f7fa;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid #f0f0f0;
}

.day-number {
  font-size: 16px;
  font-weight: 500;
}

.day-of-week {
  font-size: 12px;
  color: #909399;
}

.day-courses {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 80px;
  overflow-y: auto;
}

.course-item {
  padding: 4px 6px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: transform 0.2s;
}

.course-item:hover {
  transform: translateX(2px);
}

.course-item.course-type-0 {
  background-color: #e6f4ff;
  border-left: 3px solid #409eff;
}

.course-item.course-type-1 {
  background-color: #f0f9eb;
  border-left: 3px solid #67c23a;
}

.course-time {
  font-size: 11px;
  color: #606266;
  margin-bottom: 2px;
}

.course-type {
  margin-bottom: 2px;
}

.course-name {
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.empty-day {
  height: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #c0c4cc;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  margin-top: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.empty-day:hover {
  border-color: #409eff;
  color: #409eff;
}

.empty-day .el-icon {
  font-size: 16px;
  margin-bottom: 4px;
}

.empty-day span {
  font-size: 12px;
}

/* 周视图样式 */
.week-view {
  height: 100%;
}

.week-header {
  display: flex;
  background-color: #f8f9fa;
  border-radius: 4px 4px 0 0;
  overflow: hidden;
}

.time-column {
  width: 80px;
  flex-shrink: 0;
}

.day-header {
  flex: 1;
  text-align: center;
  padding: 12px;
  border-right: 1px solid #e4e7ed;
  background-color: #f8f9fa;
}

.day-header:last-child {
  border-right: none;
}

.day-header.today {
  background-color: #e6f4ff;
}

.day-header.weekend {
  background-color: #faf5ff;
}

.date-info {
  display: flex;
  justify-content: center;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 4px;
}

.date {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.day-of-week {
  font-size: 14px;
  color: #606266;
}

.month {
  font-size: 12px;
  color: #909399;
}

.week-body {
  display: flex;
  border: 1px solid #e4e7ed;
  border-top: none;
  border-radius: 0 0 4px 4px;
  overflow: hidden;
}

.time-grid {
  width: 80px;
  flex-shrink: 0;
  background-color: #fafafa;
}

.time-slot {
  height: 60px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: 8px 12px;
  font-size: 12px;
  color: #909399;
}

.time-slot:last-child {
  border-bottom: none;
}

.courses-grid {
  flex: 1;
  display: flex;
}

.day-column {
  flex: 1;
  border-right: 1px solid #f0f0f0;
  position: relative;
}

.day-column:last-child {
  border-right: none;
}

.day-column.today {
  background-color: #fafcff;
}

.day-column.weekend {
  background-color: #fafafa;
}

.time-cell {
  height: 60px;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
  cursor: pointer;
}

.time-cell:hover {
  background-color: #f5f7fa;
}

.time-cell:last-child {
  border-bottom: none;
}

.course-block {
  position: absolute;
  left: 4px;
  right: 4px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  z-index: 1;
}

.course-block:hover {
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.course-block.course-type-0 {
  background-color: #e6f4ff;
  border: 1px solid #b3d8ff;
}

.course-block.course-type-1 {
  background-color: #f0f9eb;
  border: 1px solid #c2e7b0;
}

.course-block-content {
  padding: 6px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.course-time {
  font-size: 11px;
  color: #606266;
  margin-bottom: 2px;
}

.course-type {
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 4px;
}

.course-block.course-type-0 .course-type {
  color: #409eff;
}

.course-block.course-type-1 .course-type {
  color: #67c23a;
}

.course-notes {
  font-size: 11px;
  color: #303133;
  line-height: 1.3;
  flex: 1;
  overflow: hidden;
}

/* 弹窗样式 */
:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>