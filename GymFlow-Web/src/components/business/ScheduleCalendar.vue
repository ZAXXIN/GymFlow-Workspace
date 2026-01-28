<template>
  <div class="schedule-calendar">
    <!-- 日历头部 -->
    <div class="calendar-header">
      <div class="header-left">
        <h2 class="calendar-title">{{ calendarTitle }}</h2>
        <el-tag type="info" size="small">{{ currentView }}视图</el-tag>
      </div>
      
      <div class="header-center">
        <el-button-group>
          <el-button @click="previousPeriod">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <el-button @click="today">{{ todayText }}</el-button>
          <el-button @click="nextPeriod">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </el-button-group>
        
        <div class="current-period">{{ currentPeriodText }}</div>
      </div>
      
      <div class="header-right">
        <el-button-group>
          <el-button
            :type="currentView === 'day' ? 'primary' : ''"
            @click="switchView('day')"
          >
            日
          </el-button>
          <el-button
            :type="currentView === 'week' ? 'primary' : ''"
            @click="switchView('week')"
          >
            周
          </el-button>
          <el-button
            :type="currentView === 'month' ? 'primary' : ''"
            @click="switchView('month')"
          >
            月
          </el-button>
        </el-button-group>
        
        <el-button
          type="primary"
          @click="handleCreateSchedule"
          style="margin-left: 10px;"
        >
          新建排班
        </el-button>
      </div>
    </div>
    
    <!-- 日历视图切换 -->
    <div class="calendar-view">
      <!-- 月视图 -->
      <div v-if="currentView === 'month'" class="month-view">
        <div class="month-grid">
          <!-- 星期标题 -->
          <div class="weekdays">
            <div class="weekday" v-for="day in weekdays" :key="day">
              {{ day }}
            </div>
          </div>
          
          <!-- 日期格子 -->
          <div class="month-days">
            <div
              v-for="day in monthDays"
              :key="day.date"
              class="day-cell"
              :class="{
                'today': day.isToday,
                'current-month': day.isCurrentMonth,
                'weekend': day.isWeekend,
                'selected': day.isSelected
              }"
              @click="selectDay(day)"
            >
              <div class="day-header">
                <div class="day-number">{{ day.day }}</div>
                <div class="day-lunar" v-if="day.lunar">{{ day.lunar }}</div>
              </div>
              
              <div class="day-schedules">
                <div
                  v-for="schedule in getSchedulesForDay(day.date)"
                  :key="schedule.id"
                  class="schedule-item"
                  :style="{ backgroundColor: getScheduleColor(schedule) }"
                  @click.stop="handleScheduleClick(schedule)"
                >
                  <div class="schedule-time">
                    {{ formatTime(schedule.startTime) }} - {{ formatTime(schedule.endTime) }}
                  </div>
                  <div class="schedule-title">{{ schedule.title }}</div>
                  <div class="schedule-coach">{{ schedule.coachName }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 周视图 -->
      <div v-else-if="currentView === 'week'" class="week-view">
        <div class="week-grid">
          <!-- 时间轴 -->
          <div class="time-axis">
            <div class="time-label"></div>
            <div
              v-for="hour in hours"
              :key="hour"
              class="time-slot"
            >
              {{ hour }}:00
            </div>
          </div>
          
          <!-- 日期列 -->
          <div
            v-for="day in weekDays"
            :key="day.date"
            class="day-column"
            :class="{ 'today': day.isToday, 'weekend': day.isWeekend }"
          >
            <!-- 日期标题 -->
            <div class="day-header">
              <div class="day-title">
                <div class="day-name">{{ day.dayName }}</div>
                <div class="day-date">{{ day.date }}</div>
              </div>
              <div class="day-schedule-count">
                {{ getSchedulesForDay(day.date).length }} 节课
              </div>
            </div>
            
            <!-- 时间格子 -->
            <div class="day-time-slots">
              <div
                v-for="hour in hours"
                :key="hour"
                class="time-slot"
                :class="{ 'current-hour': isCurrentHour(day.date, hour) }"
                @click="handleTimeSlotClick(day.date, hour)"
              >
                <!-- 日程安排 -->
                <div
                  v-for="schedule in getSchedulesForTimeSlot(day.date, hour)"
                  :key="schedule.id"
                  class="schedule-block"
                  :style="getScheduleBlockStyle(schedule)"
                  @click.stop="handleScheduleClick(schedule)"
                >
                  <div class="schedule-content">
                    <div class="schedule-time">
                      {{ formatTime(schedule.startTime) }}
                    </div>
                    <div class="schedule-title">{{ schedule.title }}</div>
                    <div class="schedule-coach">{{ schedule.coachName }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 日视图 -->
      <div v-else class="day-view">
        <div class="day-grid">
          <!-- 时间轴 -->
          <div class="time-axis">
            <div
              v-for="hour in hours"
              :key="hour"
              class="time-slot"
            >
              <div class="time-label">{{ hour }}:00</div>
              <div class="time-grid"></div>
            </div>
          </div>
          
          <!-- 日程区域 -->
          <div class="schedule-area">
            <!-- 当前日期的日程 -->
            <div
              v-for="schedule in getSchedulesForDay(currentDate)"
              :key="schedule.id"
              class="schedule-event"
              :style="getScheduleEventStyle(schedule)"
              @click="handleScheduleClick(schedule)"
            >
              <div class="event-content">
                <div class="event-time">
                  {{ formatTime(schedule.startTime) }} - {{ formatTime(schedule.endTime) }}
                </div>
                <div class="event-title">{{ schedule.title }}</div>
                <div class="event-info">
                  <span class="coach">{{ schedule.coachName }}</span>
                  <span class="capacity">{{ schedule.enrolled }}/{{ schedule.capacity }}人</span>
                </div>
                <el-tag
                  v-if="schedule.status"
                  :type="getScheduleStatusType(schedule.status)"
                  size="small"
                  class="event-status"
                >
                  {{ schedule.status }}
                </el-tag>
              </div>
            </div>
            
            <!-- 时间线 -->
            <div
              class="current-time-line"
              v-if="showCurrentTimeLine"
              :style="{ top: getCurrentTimePosition() + 'px' }"
            >
              <div class="time-line-dot"></div>
              <div class="time-line"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 日程详情侧边栏 -->
    <el-drawer
      v-model="showScheduleDetail"
      title="日程详情"
      :size="400"
      destroy-on-close
    >
      <schedule-detail
        v-if="selectedSchedule"
        :schedule="selectedSchedule"
        @update="handleScheduleUpdate"
        @delete="handleScheduleDelete"
      />
    </el-drawer>
    
    <!-- 新建日程弹窗 -->
    <el-dialog
      v-model="showCreateDialog"
      title="新建排班"
      width="600px"
      destroy-on-close
    >
      <schedule-form
        v-if="showCreateDialog"
        :form-data="newScheduleForm"
        @submit="handleCreateSubmit"
        @cancel="showCreateDialog = false"
      />
    </el-dialog>
    
    <!-- 日程统计 -->
    <div class="schedule-stats">
      <el-card shadow="never">
        <template #header>
          <div class="stats-header">
            <h3>日程统计</h3>
            <el-date-picker
              v-model="statsDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              size="small"
              @change="loadScheduleStats"
            />
          </div>
        </template>
        
        <div class="stats-content">
          <el-row :gutter="20">
            <el-col :span="6" v-for="stat in scheduleStats" :key="stat.label">
              <div class="stat-item">
                <div class="stat-label">{{ stat.label }}</div>
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-trend" :class="getTrendClass(stat.trend)">
                  {{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%
                </div>
              </div>
            </el-col>
          </el-row>
          
          <div class="stats-chart">
            <div ref="statsChartRef" class="chart-container"></div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { useChart } from '@/composables/useChart'
import ScheduleDetail from './ScheduleDetail.vue'
import ScheduleForm from './ScheduleForm.vue'
import type { Schedule, ScheduleFormData } from '@/types'

// 日历视图类型
type CalendarView = 'day' | 'week' | 'month'

// 图表相关
const statsChartRef = ref<HTMLElement>()
const statsChart = useChart({ autoResize: true })

// 响应式数据
const currentView = ref<CalendarView>('week')
const currentDate = ref(new Date())
const selectedDate = ref<string>('')
const showScheduleDetail = ref(false)
const showCreateDialog = ref(false)
const showCurrentTimeLine = ref(true)
const statsDateRange = ref<[string, string]>(['', ''])
const selectedSchedule = ref<Schedule | null>(null)
const newScheduleForm = ref<ScheduleFormData>({
  title: '',
  type: 'COURSE',
  coachId: '',
  startTime: '',
  endTime: '',
  date: '',
  capacity: 10,
  status: 'UPCOMING'
})

// 日程数据
const schedules = ref<Schedule[]>([
  {
    id: '1',
    title: '瑜伽基础课',
    type: 'COURSE',
    coachId: '1',
    coachName: '张教练',
    date: '2024-01-15',
    startTime: '09:00:00',
    endTime: '10:00:00',
    capacity: 15,
    enrolled: 12,
    status: 'UPCOMING',
    color: '#91cc75'
  },
  {
    id: '2',
    title: '动感单车',
    type: 'COURSE',
    coachId: '2',
    coachName: '李教练',
    date: '2024-01-15',
    startTime: '10:30:00',
    endTime: '11:30:00',
    capacity: 20,
    enrolled: 18,
    status: 'UPCOMING',
    color: '#ee6666'
  },
  {
    id: '3',
    title: '私教课',
    type: 'PRIVATE_TRAINING',
    coachId: '3',
    coachName: '王教练',
    date: '2024-01-15',
    startTime: '14:00:00',
    endTime: '15:00:00',
    capacity: 1,
    enrolled: 1,
    status: 'CONFIRMED',
    color: '#5470c6'
  },
  {
    id: '4',
    title: '力量训练',
    type: 'COURSE',
    coachId: '1',
    coachName: '张教练',
    date: '2024-01-16',
    startTime: '19:00:00',
    endTime: '20:00:00',
    capacity: 12,
    enrolled: 8,
    status: 'UPCOMING',
    color: '#fac858'
  }
])

// 统计数据
const scheduleStats = ref([
  { label: '总课程数', value: '156', trend: 12.5 },
  { label: '已排班数', value: '142', trend: 8.3 },
  { label: '满员课程', value: '89', trend: 15.2 },
  { label: '平均出勤率', value: '85%', trend: 3.4 }
])

// 计算属性
const calendarTitle = computed(() => {
  const now = new Date()
  const today = now.toISOString().split('T')[0]
  
  if (selectedDate.value === today) {
    return '今日排班'
  } else if (selectedDate.value) {
    return `${selectedDate.value} 排班`
  }
  
  return '课程排班日历'
})

const todayText = computed(() => {
  const today = new Date()
  const selected = new Date(selectedDate.value || currentDate.value)
  
  const isToday = today.toDateString() === selected.toDateString()
  return isToday ? '今天' : '返回今天'
})

const currentPeriodText = computed(() => {
  if (currentView.value === 'month') {
    const date = new Date(currentDate.value)
    return `${date.getFullYear()}年${date.getMonth() + 1}月`
  } else if (currentView.value === 'week') {
    const start = new Date(weekDays.value[0].date)
    const end = new Date(weekDays.value[6].date)
    return `${start.getMonth() + 1}月${start.getDate()}日 - ${end.getMonth() + 1}月${end.getDate()}日`
  } else {
    const date = new Date(currentDate.value)
    return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
  }
})

const weekdays = computed(() => ['日', '一', '二', '三', '四', '五', '六'])

const monthDays = computed(() => {
  const date = new Date(currentDate.value)
  const year = date.getFullYear()
  const month = date.getMonth()
  
  // 获取当月第一天
  const firstDay = new Date(year, month, 1)
  // 获取当月最后一天
  const lastDay = new Date(year, month + 1, 0)
  // 获取第一天是星期几（0-6，0代表星期日）
  const firstDayOfWeek = firstDay.getDay()
  // 获取上个月的最后一天
  const prevMonthLastDay = new Date(year, month, 0).getDate()
  
  const days = []
  const today = new Date()
  const todayStr = today.toISOString().split('T')[0]
  
  // 添加上个月的日期
  for (let i = firstDayOfWeek - 1; i >= 0; i--) {
    const day = prevMonthLastDay - i
    const dateStr = new Date(year, month - 1, day).toISOString().split('T')[0]
    days.push({
      date: dateStr,
      day: day,
      lunar: getLunarDate(dateStr),
      isToday: dateStr === todayStr,
      isCurrentMonth: false,
      isWeekend: false,
      isSelected: dateStr === selectedDate.value
    })
  }
  
  // 添加当月的日期
  for (let day = 1; day <= lastDay.getDate(); day++) {
    const dateStr = new Date(year, month, day).toISOString().split('T')[0]
    const dayOfWeek = new Date(year, month, day).getDay()
    days.push({
      date: dateStr,
      day: day,
      lunar: day === 1 ? `${month + 1}月` : getLunarDate(dateStr),
      isToday: dateStr === todayStr,
      isCurrentMonth: true,
      isWeekend: dayOfWeek === 0 || dayOfWeek === 6,
      isSelected: dateStr === selectedDate.value
    })
  }
  
  // 添加下个月的日期
  const remainingDays = 42 - days.length
  for (let day = 1; day <= remainingDays; day++) {
    const dateStr = new Date(year, month + 1, day).toISOString().split('T')[0]
    days.push({
      date: dateStr,
      day: day,
      lunar: getLunarDate(dateStr),
      isToday: dateStr === todayStr,
      isCurrentMonth: false,
      isWeekend: false,
      isSelected: dateStr === selectedDate.value
    })
  }
  
  return days
})

const weekDays = computed(() => {
  const date = new Date(currentDate.value)
  const dayOfWeek = date.getDay()
  const startDate = new Date(date)
  startDate.setDate(date.getDate() - dayOfWeek)
  
  const days = []
  const today = new Date()
  const todayStr = today.toISOString().split('T')[0]
  
  for (let i = 0; i < 7; i++) {
    const currentDate = new Date(startDate)
    currentDate.setDate(startDate.getDate() + i)
    const dateStr = currentDate.toISOString().split('T')[0]
    const dayOfWeek = currentDate.getDay()
    
    days.push({
      date: dateStr,
      day: currentDate.getDate(),
      dayName: weekdays.value[dayOfWeek],
      isToday: dateStr === todayStr,
      isWeekend: dayOfWeek === 0 || dayOfWeek === 6
    })
  }
  
  return days
})

const hours = computed(() => {
  return Array.from({ length: 14 }, (_, i) => i + 8) // 8:00 - 21:00
})

// 方法
const switchView = (view: CalendarView) => {
  currentView.value = view
}

const previousPeriod = () => {
  const date = new Date(currentDate.value)
  
  if (currentView.value === 'month') {
    date.setMonth(date.getMonth() - 1)
  } else if (currentView.value === 'week') {
    date.setDate(date.getDate() - 7)
  } else {
    date.setDate(date.getDate() - 1)
  }
  
  currentDate.value = date
}

const nextPeriod = () => {
  const date = new Date(currentDate.value)
  
  if (currentView.value === 'month') {
    date.setMonth(date.getMonth() + 1)
  } else if (currentView.value === 'week') {
    date.setDate(date.getDate() + 7)
  } else {
    date.setDate(date.getDate() + 1)
  }
  
  currentDate.value = date
}

const today = () => {
  currentDate.value = new Date()
  selectedDate.value = new Date().toISOString().split('T')[0]
}

const selectDay = (day: any) => {
  selectedDate.value = day.date
  if (currentView.value === 'month') {
    currentView.value = 'day'
    currentDate.value = new Date(day.date)
  }
}

const getSchedulesForDay = (date: string) => {
  return schedules.value.filter(schedule => schedule.date === date)
}

const getSchedulesForTimeSlot = (date: string, hour: number) => {
  return schedules.value.filter(schedule => {
    if (schedule.date !== date) return false
    
    const startHour = parseInt(schedule.startTime.split(':')[0])
    const endHour = parseInt(schedule.endTime.split(':')[0])
    
    return startHour <= hour && hour < endHour
  })
}

const getScheduleColor = (schedule: Schedule) => {
  return schedule.color || '#5470c6'
}

const getScheduleStatusType = (status: string) => {
  const statusMap: Record<string, any> = {
    'UPCOMING': 'info',
    'ONGOING': 'success',
    'COMPLETED': 'primary',
    'CANCELLED': 'danger',
    'CONFIRMED': 'success'
  }
  return statusMap[status] || 'info'
}

const formatTime = (timeStr: string) => {
  const [hour, minute] = timeStr.split(':')
  return `${hour}:${minute}`
}

const getLunarDate = (dateStr: string) => {
  // 这里应该实现农历计算
  // 暂时返回空字符串
  return ''
}

const isCurrentHour = (date: string, hour: number) => {
  const now = new Date()
  const today = now.toISOString().split('T')[0]
  const currentHour = now.getHours()
  
  return date === today && hour === currentHour
}

const getScheduleBlockStyle = (schedule: Schedule) => {
  const startHour = parseInt(schedule.startTime.split(':')[0])
  const startMinute = parseInt(schedule.startTime.split(':')[1])
  const endHour = parseInt(schedule.endTime.split(':')[0])
  const endMinute = parseInt(schedule.endTime.split(':')[1])
  
  const durationHours = endHour - startHour
  const durationMinutes = endMinute - startMinute
  const totalDuration = durationHours * 60 + durationMinutes
  
  const top = (startHour - 8) * 60 + startMinute
  const height = totalDuration
  
  return {
    top: `${top}px`,
    height: `${height}px`,
    backgroundColor: getScheduleColor(schedule)
  }
}

const getScheduleEventStyle = (schedule: Schedule) => {
  const startHour = parseInt(schedule.startTime.split(':')[0])
  const startMinute = parseInt(schedule.startTime.split(':')[1])
  const endHour = parseInt(schedule.endTime.split(':')[0])
  const endMinute = parseInt(schedule.endTime.split(':')[1])
  
  const top = (startHour - 8) * 60 + startMinute
  const height = (endHour - startHour) * 60 + (endMinute - startMinute)
  
  return {
    top: `${top}px`,
    height: `${height}px`,
    backgroundColor: getScheduleColor(schedule)
  }
}

const getCurrentTimePosition = () => {
  const now = new Date()
  const hours = now.getHours()
  const minutes = now.getMinutes()
  
  if (hours < 8 || hours >= 22) return 0
  
  const position = (hours - 8) * 60 + minutes
  return position
}

const getTrendClass = (trend: number) => {
  if (trend > 0) return 'positive'
  if (trend < 0) return 'negative'
  return 'neutral'
}

const handleTimeSlotClick = (date: string, hour: number) => {
  newScheduleForm.value = {
    title: '',
    type: 'COURSE',
    coachId: '',
    startTime: `${hour.toString().padStart(2, '0')}:00:00`,
    endTime: `${(hour + 1).toString().padStart(2, '0')}:00:00`,
    date: date,
    capacity: 10,
    status: 'UPCOMING'
  }
  showCreateDialog.value = true
}

const handleScheduleClick = (schedule: Schedule) => {
  selectedSchedule.value = schedule
  showScheduleDetail.value = true
}

const handleCreateSchedule = () => {
  newScheduleForm.value = {
    title: '',
    type: 'COURSE',
    coachId: '',
    startTime: '09:00:00',
    endTime: '10:00:00',
    date: selectedDate.value || new Date().toISOString().split('T')[0],
    capacity: 10,
    status: 'UPCOMING'
  }
  showCreateDialog.value = true
}

const handleCreateSubmit = (formData: ScheduleFormData) => {
  const newSchedule: Schedule = {
    id: Date.now().toString(),
    title: formData.title,
    type: formData.type,
    coachId: formData.coachId,
    coachName: '新教练', // 这里应该根据coachId获取教练姓名
    date: formData.date,
    startTime: formData.startTime,
    endTime: formData.endTime,
    capacity: formData.capacity,
    enrolled: 0,
    status: formData.status,
    color: formData.type === 'PRIVATE_TRAINING' ? '#5470c6' : 
           formData.type === 'COURSE' ? '#91cc75' : '#fac858'
  }
  
  schedules.value.push(newSchedule)
  showCreateDialog.value = false
  ElMessage.success('排班创建成功')
}

const handleScheduleUpdate = (updatedSchedule: Schedule) => {
  const index = schedules.value.findIndex(s => s.id === updatedSchedule.id)
  if (index !== -1) {
    schedules.value[index] = { ...schedules.value[index], ...updatedSchedule }
    ElMessage.success('日程更新成功')
  }
  showScheduleDetail.value = false
}

const handleScheduleDelete = (scheduleId: string) => {
  schedules.value = schedules.value.filter(s => s.id !== scheduleId)
  ElMessage.success('日程删除成功')
  showScheduleDetail.value = false
}

const loadScheduleStats = () => {
  // 这里应该根据日期范围加载统计数据
  renderStatsChart()
}

const renderStatsChart = () => {
  statsChart.chartContainer = statsChartRef
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['课程数', '出勤率']
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: [
      {
        type: 'value',
        name: '课程数',
        position: 'left'
      },
      {
        type: 'value',
        name: '出勤率',
        position: 'right',
        min: 0,
        max: 100,
        axisLabel: {
          formatter: '{value}%'
        }
      }
    ],
    series: [
      {
        name: '课程数',
        type: 'bar',
        data: [12, 15, 11, 14, 16, 10, 8],
        itemStyle: {
          color: '#5470c6'
        }
      },
      {
        name: '出勤率',
        type: 'line',
        yAxisIndex: 1,
        data: [85, 88, 82, 90, 87, 80, 75],
        itemStyle: {
          color: '#91cc75'
        }
      }
    ]
  }
  
  statsChart.setChartOption(option)
}

// 生命周期
onMounted(() => {
  // 设置默认选中日期为今天
  selectedDate.value = new Date().toISOString().split('T')[0]
  
  // 设置默认统计日期范围为最近7天
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(endDate.getDate() - 7)
  
  statsDateRange.value = [
    startDate.toISOString().split('T')[0],
    endDate.toISOString().split('T')[0]
  ]
  
  // 渲染统计图表
  renderStatsChart()
  
  // 更新当前时间线
  const updateTimeLine = () => {
    const now = new Date()
    const hours = now.getHours()
    showCurrentTimeLine.value = hours >= 8 && hours < 22
  }
  
  updateTimeLine()
  const timer = setInterval(updateTimeLine, 60000) // 每分钟更新一次
  
  onUnmounted(() => {
    clearInterval(timer)
    statsChart.disposeChart()
  })
})

// 监听当前日期变化
watch(() => currentDate.value, () => {
  // 这里可以添加加载对应日期数据的逻辑
}, { immediate: true })
</script>

<style scoped lang="scss">
.schedule-calendar {
  .calendar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 15px;
    background-color: #ffffff;
    border-radius: 4px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 10px;
      
      .calendar-title {
        margin: 0;
        font-size: 20px;
        color: #303133;
      }
    }
    
    .header-center {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      
      .current-period {
        font-size: 16px;
        font-weight: 500;
        color: #606266;
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
    }
  }
  
  .calendar-view {
    background-color: #ffffff;
    border-radius: 4px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    
    .month-view {
      .month-grid {
        .weekdays {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          margin-bottom: 10px;
          
          .weekday {
            text-align: center;
            padding: 8px;
            font-weight: 500;
            color: #606266;
            border-bottom: 1px solid #e4e7ed;
          }
        }
        
        .month-days {
          display: grid;
          grid-template-columns: repeat(7, 1fr);
          grid-gap: 1px;
          background-color: #e4e7ed;
          
          .day-cell {
            min-height: 120px;
            background-color: #ffffff;
            padding: 8px;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover {
              background-color: #f5f7fa;
            }
            
            &.today {
              background-color: #f0f9ff;
              
              .day-number {
                color: #409eff;
                font-weight: bold;
              }
            }
            
            &.current-month {
              .day-number {
                color: #303133;
              }
            }
            
            &:not(.current-month) {
              .day-number,
              .day-lunar {
                color: #c0c4cc;
              }
            }
            
            &.weekend {
              background-color: #f8f9fa;
            }
            
            &.selected {
              background-color: #ecf5ff;
              border: 2px solid #409eff;
            }
            
            .day-header {
              margin-bottom: 4px;
              
              .day-number {
                font-size: 16px;
                margin-bottom: 2px;
              }
              
              .day-lunar {
                font-size: 12px;
                color: #909399;
              }
            }
            
            .day-schedules {
              .schedule-item {
                padding: 4px 6px;
                margin-bottom: 4px;
                border-radius: 2px;
                font-size: 12px;
                color: #ffffff;
                cursor: pointer;
                transition: all 0.3s;
                
                &:hover {
                  opacity: 0.9;
                  transform: translateX(2px);
                }
                
                .schedule-time {
                  font-size: 10px;
                  opacity: 0.9;
                }
                
                .schedule-title {
                  font-weight: 500;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                }
                
                .schedule-coach {
                  font-size: 10px;
                  opacity: 0.9;
                }
              }
            }
          }
        }
      }
    }
    
    .week-view {
      .week-grid {
        display: grid;
        grid-template-columns: auto repeat(7, 1fr);
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        overflow: hidden;
        
        .time-axis {
          background-color: #f8f9fa;
          border-right: 1px solid #e4e7ed;
          
          .time-label {
            height: 60px;
            border-bottom: 1px solid #e4e7ed;
          }
          
          .time-slot {
            height: 60px;
            padding: 4px 8px;
            font-size: 12px;
            color: #909399;
            border-bottom: 1px solid #e4e7ed;
            display: flex;
            align-items: flex-end;
          }
        }
        
        .day-column {
          border-right: 1px solid #e4e7ed;
          
          &:last-child {
            border-right: none;
          }
          
          &.today {
            background-color: #f0f9ff;
          }
          
          &.weekend {
            background-color: #f8f9fa;
          }
          
          .day-header {
            height: 60px;
            padding: 8px;
            border-bottom: 1px solid #e4e7ed;
            background-color: #f5f7fa;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            
            .day-title {
              text-align: center;
              margin-bottom: 4px;
              
              .day-name {
                font-size: 14px;
                color: #303133;
                font-weight: 500;
              }
              
              .day-date {
                font-size: 12px;
                color: #909399;
              }
            }
            
            .day-schedule-count {
              font-size: 10px;
              color: #909399;
            }
          }
          
          .day-time-slots {
            .time-slot {
              height: 60px;
              border-bottom: 1px solid #e4e7ed;
              position: relative;
              cursor: pointer;
              transition: background-color 0.3s;
              
              &:hover {
                background-color: #f0f9ff;
              }
              
              &.current-hour {
                background-color: #fef0f0;
              }
              
              .schedule-block {
                position: absolute;
                left: 2px;
                right: 2px;
                border-radius: 2px;
                padding: 4px;
                color: #ffffff;
                cursor: pointer;
                overflow: hidden;
                z-index: 1;
                
                &:hover {
                  opacity: 0.9;
                  z-index: 2;
                }
                
                .schedule-content {
                  .schedule-time {
                    font-size: 10px;
                    opacity: 0.9;
                  }
                  
                  .schedule-title {
                    font-size: 12px;
                    font-weight: 500;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                  }
                  
                  .schedule-coach {
                    font-size: 10px;
                    opacity: 0.9;
                  }
                }
              }
            }
          }
        }
      }
    }
    
    .day-view {
      .day-grid {
        display: grid;
        grid-template-columns: auto 1fr;
        height: 600px;
        border: 1px solid #e4e7ed;
        border-radius: 4px;
        overflow: hidden;
        
        .time-axis {
          background-color: #f8f9fa;
          border-right: 1px solid #e4e7ed;
          
          .time-slot {
            height: 60px;
            border-bottom: 1px solid #e4e7ed;
            display: flex;
            align-items: center;
            
            .time-label {
              width: 60px;
              padding: 0 8px;
              font-size: 12px;
              color: #909399;
              text-align: right;
            }
            
            .time-grid {
              flex: 1;
              border-top: 1px solid #e4e7ed;
            }
          }
        }
        
        .schedule-area {
          position: relative;
          background-color: #ffffff;
          
          .schedule-event {
            position: absolute;
            left: 4px;
            right: 4px;
            border-radius: 4px;
            padding: 8px;
            color: #ffffff;
            cursor: pointer;
            transition: all 0.3s;
            z-index: 1;
            
            &:hover {
              opacity: 0.9;
              transform: translateY(-2px);
              box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
              z-index: 2;
            }
            
            .event-content {
              height: 100%;
              overflow: hidden;
              
              .event-time {
                font-size: 12px;
                opacity: 0.9;
                margin-bottom: 4px;
              }
              
              .event-title {
                font-size: 14px;
                font-weight: 500;
                margin-bottom: 4px;
              }
              
              .event-info {
                display: flex;
                justify-content: space-between;
                font-size: 12px;
                opacity: 0.9;
                margin-bottom: 4px;
              }
              
              .event-status {
                position: absolute;
                bottom: 8px;
                right: 8px;
              }
            }
          }
          
          .current-time-line {
            position: absolute;
            left: 0;
            right: 0;
            z-index: 3;
            pointer-events: none;
            
            .time-line-dot {
              position: absolute;
              left: -4px;
              top: -4px;
              width: 8px;
              height: 8px;
              border-radius: 50%;
              background-color: #f56c6c;
            }
            
            .time-line {
              height: 1px;
              background-color: #f56c6c;
            }
          }
        }
      }
    }
  }
  
  .schedule-stats {
    margin-top: 20px;
    
    .stats-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
        color: #303133;
      }
    }
    
    .stats-content {
      .stat-item {
        padding: 15px;
        background-color: #f8f9fa;
        border-radius: 4px;
        text-align: center;
        
        .stat-label {
          font-size: 14px;
          color: #606266;
          margin-bottom: 8px;
        }
        
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #303133;
          margin-bottom: 4px;
        }
        
        .stat-trend {
          font-size: 12px;
          
          &.positive {
            color: #67c23a;
          }
          
          &.negative {
            color: #f56c6c;
          }
          
          &.neutral {
            color: #909399;
          }
        }
      }
      
      .stats-chart {
        margin-top: 20px;
        
        .chart-container {
          height: 300px;
        }
      }
    }
  }
}
</style>