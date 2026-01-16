<template>
  <div class="course-schedule-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">课程安排</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>课程管理</el-breadcrumb-item>
          <el-breadcrumb-item>课程安排</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAddCourse">
          <el-icon><Plus /></el-icon>
          新增课程
        </el-button>
        <el-date-picker
          v-model="currentWeek"
          type="week"
          format="yyyy 第 WW 周"
          value-format="YYYY-MM-DD"
          placeholder="选择周"
          @change="handleWeekChange"
        />
      </div>
    </div>
    
    <!-- 日历视图 -->
    <el-card class="schedule-card">
      <div class="calendar-header">
        <div class="week-navigation">
          <el-button icon="i-ep-arrow-left" circle @click="prevWeek" />
          <span class="week-title">{{ weekTitle }}</span>
          <el-button icon="i-ep-arrow-right" circle @click="nextWeek" />
        </div>
        <div class="view-actions">
          <el-button-group>
            <el-button :type="viewMode === 'day' ? 'primary' : 'default'" @click="viewMode = 'day'">
              日
            </el-button>
            <el-button :type="viewMode === 'week' ? 'primary' : 'default'" @click="viewMode = 'week'">
              周
            </el-button>
          </el-button-group>
          <el-button @click="refreshSchedule">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
      
      <!-- 周视图 -->
      <div class="week-view" v-if="viewMode === 'week'">
        <div class="time-grid">
          <!-- 时间轴 -->
          <div class="time-axis">
            <div class="time-header"></div>
            <div v-for="hour in hours" :key="hour" class="time-slot">
              {{ hour }}:00
            </div>
          </div>
          
          <!-- 日期列 -->
          <div v-for="day in weekDays" :key="day.date" class="day-column">
            <div class="day-header">
              <div class="day-name">{{ day.name }}</div>
              <div class="day-date">{{ day.date }}</div>
              <div class="day-count">{{ getDayCourseCount(day.date) }}节</div>
            </div>
            
            <div class="day-time-slots">
              <div
                v-for="hour in hours"
                :key="hour"
                class="time-slot"
                @click="handleTimeSlotClick(day.date, hour)"
              >
                <!-- 课程卡片 -->
                <div
                  v-for="course in getCoursesAtTime(day.date, hour)"
                  :key="course.id"
                  class="course-card"
                  :class="{
                    [course.status.toLowerCase()]: true,
                    'full': course.currentBookings >= course.capacity
                  }"
                  @click.stop="handleCourseClick(course)"
                >
                  <div class="course-title">{{ course.name }}</div>
                  <div class="course-info">
                    <span class="coach">{{ course.coachName }}</span>
                    <span class="capacity">{{ course.currentBookings }}/{{ course.capacity }}</span>
                  </div>
                  <div class="course-time">{{ formatTime(course.startTime) }}-{{ formatTime(course.endTime) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 日视图 -->
      <div class="day-view" v-else>
        <div class="day-header-large">
          <h3>{{ currentDayDate }}</h3>
          <div class="day-stats">
            <span class="stat-item">
              <el-icon><Calendar /></el-icon>
              {{ currentDayCourses.length }} 节课
            </span>
            <span class="stat-item">
              <el-icon><User /></el-icon>
              {{ getTotalBookings() }} 个预约
            </span>
          </div>
        </div>
        
        <div class="day-timeline">
          <div
            v-for="hour in hours"
            :key="hour"
            class="timeline-hour"
          >
            <div class="hour-label">{{ hour }}:00</div>
            <div class="hour-content">
              <div
                v-for="course in getCoursesAtTime(currentDayDate, hour)"
                :key="course.id"
                class="timeline-course"
                :style="{
                  top: `${getCoursePosition(course)}px`,
                  height: `${getCourseHeight(course)}px`
                }"
                @click="handleCourseClick(course)"
              >
                <div class="course-content">
                  <div class="course-title">{{ course.name }}</div>
                  <div class="course-coach">{{ course.coachName }}</div>
                  <div class="course-time">{{ formatTime(course.startTime) }}-{{ formatTime(course.endTime) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 课程列表 -->
    <el-card class="course-list-card">
      <template #header>
        <div class="list-header">
          <h3>本周课程 ({{ filteredCourses.length }})</h3>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程..."
            style="width: 240px;"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </template>
      
      <el-table :data="filteredCourses" v-loading="loading">
        <el-table-column prop="name" label="课程名称" width="200">
          <template #default="{ row }">
            <div class="course-cell">
              <el-tag :type="getCategoryType(row.category)" size="small">
                {{ getCategoryText(row.category) }}
              </el-tag>
              <span class="course-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="coachName" label="教练" width="120" />
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column prop="startTime" label="时间" width="180">
          <template #default="{ row }">
            <div class="time-cell">
              <div>{{ formatDate(row.startTime) }}</div>
              <div class="time-range">{{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="预约/容量" width="120" align="center">
          <template #default="{ row }">
            <div class="capacity-cell">
              <el-progress
                :percentage="getBookingPercentage(row)"
                :stroke-width="8"
                :show-text="false"
                :color="getCapacityColor(row)"
              />
              <span class="capacity-text">{{ row.currentBookings }}/{{ row.capacity }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="handleCourseClick(row)">
              详情
            </el-button>
            <el-button type="text" size="small" @click="handleEdit(row.id)">
              编辑
            </el-button>
            <el-button
              type="text"
              size="small"
              class="delete-btn"
              @click="handleDelete(row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Refresh,
  Search,
  Calendar,
  User
} from '@element-plus/icons-vue'
import { useCourseStore } from '@/stores/course'
import { formatDate, formatTime } from '@/utils'
import type { Course, CourseStatus } from '@/types'

// Store
const courseStore = useCourseStore()

// Router
const router = useRouter()

// 状态
const loading = ref(false)
const viewMode = ref<'day' | 'week'>('week')
const currentWeek = ref('')
const searchKeyword = ref('')

// 时间配置
const hours = Array.from({ length: 14 }, (_, i) => i + 7) // 7:00 - 20:00

// Computed
const weekDays = computed(() => {
  const days = []
  const startDate = new Date(currentWeek.value)
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + i)
    
    days.push({
      date: formatDate(date, 'MM-DD'),
      fullDate: formatDate(date, 'YYYY-MM-DD'),
      name: getWeekdayName(date.getDay()),
      day: date.getDate(),
      month: date.getMonth() + 1
    })
  }
  
  return days
})

const weekTitle = computed(() => {
  if (!weekDays.value.length) return ''
  const firstDay = weekDays.value[0]
  const lastDay = weekDays.value[6]
  return `${firstDay.month}.${firstDay.day} - ${lastDay.month}.${lastDay.day}`
})

const currentDayDate = computed(() => {
  return weekDays.value[0]?.fullDate || ''
})

const currentWeekCourses = computed(() => {
  if (!weekDays.value.length) return []
  
  const startDate = weekDays.value[0].fullDate
  const endDate = weekDays.value[6].fullDate
  
  return courseStore.courses.filter(course => {
    const courseDate = formatDate(course.startTime, 'YYYY-MM-DD')
    return courseDate >= startDate && courseDate <= endDate
  })
})

const currentDayCourses = computed(() => {
  return currentWeekCourses.value.filter(course => {
    const courseDate = formatDate(course.startTime, 'YYYY-MM-DD')
    return courseDate === currentDayDate.value
  })
})

const filteredCourses = computed(() => {
  if (!searchKeyword.value) return currentWeekCourses.value
  
  const keyword = searchKeyword.value.toLowerCase()
  return currentWeekCourses.value.filter(course => 
    course.name.toLowerCase().includes(keyword) ||
    course.coachName.toLowerCase().includes(keyword) ||
    course.location.toLowerCase().includes(keyword)
  )
})

// Methods
const loadSchedule = async () => {
  try {
    loading.value = true
    
    const startDate = weekDays.value[0]?.fullDate
    const endDate = weekDays.value[6]?.fullDate
    
    if (!startDate || !endDate) return
    
    await courseStore.fetchCourses({
      startDate,
      endDate,
      pageSize: 100
    })
  } catch (error) {
    console.error('加载课程安排失败:', error)
    ElMessage.error('加载课程安排失败')
  } finally {
    loading.value = false
  }
}

const initCurrentWeek = () => {
  const today = new Date()
  const dayOfWeek = today.getDay()
  const diff = today.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1)
  const monday = new Date(today.setDate(diff))
  currentWeek.value = formatDate(monday, 'YYYY-MM-DD')
}

const getWeekdayName = (day: number) => {
  const names = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return names[day]
}

const prevWeek = () => {
  const date = new Date(currentWeek.value)
  date.setDate(date.getDate() - 7)
  currentWeek.value = formatDate(date, 'YYYY-MM-DD')
  loadSchedule()
}

const nextWeek = () => {
  const date = new Date(currentWeek.value)
  date.setDate(date.getDate() + 7)
  currentWeek.value = formatDate(date, 'YYYY-MM-DD')
  loadSchedule()
}

const handleWeekChange = () => {
  loadSchedule()
}

const refreshSchedule = () => {
  loadSchedule()
}

const getDayCourseCount = (date: string) => {
  return currentWeekCourses.value.filter(course => 
    formatDate(course.startTime, 'MM-DD') === date
  ).length
}

const getCoursesAtTime = (date: string, hour: number) => {
  return currentWeekCourses.value.filter(course => {
    const courseDate = formatDate(course.startTime, 'MM-DD')
    const courseHour = new Date(course.startTime).getHours()
    return courseDate === date && courseHour === hour
  })
}

const getCoursePosition = (course: Course) => {
  const startTime = new Date(course.startTime)
  const minutes = startTime.getMinutes()
  return minutes * 0.8 // 0.8px per minute
}

const getCourseHeight = (course: Course) => {
  return course.duration * 0.8 // 0.8px per minute
}

const getTotalBookings = () => {
  return currentDayCourses.value.reduce((sum, course) => sum + course.currentBookings, 0)
}

const handleTimeSlotClick = (date: string, hour: number) => {
  // 打开新增课程表单，预设时间
  const dateStr = weekDays.value.find(d => d.date === date)?.fullDate
  if (dateStr) {
    const startTime = `${dateStr} ${hour.toString().padStart(2, '0')}:00:00`
    const endTime = `${dateStr} ${(hour + 1).toString().padStart(2, '0')}:00:00`
    
    router.push({
      path: '/courses/create',
      query: {
        startTime,
        endTime,
        duration: 60
      }
    })
  }
}

const handleCourseClick = (course: Course) => {
  router.push(`/courses/${course.id}`)
}

const handleAddCourse = () => {
  router.push('/courses/create')
}

const handleEdit = (id: number) => {
  router.push(`/courses/${id}/edit`)
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该课程吗？此操作不可恢复。', '删除课程', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    
    await courseStore.deleteCourse(id)
    ElMessage.success('删除成功')
    loadSchedule()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

const getCategoryType = (category: string) => {
  const types: Record<string, string> = {
    YOGA: 'success',
    PILATES: 'info',
    CROSSFIT: 'warning',
    SPINNING: 'danger',
    BODYBUILDING: '',
    AEROBICS: 'success',
    PERSONAL_TRAINING: 'warning'
  }
  return types[category] || 'info'
}

const getCategoryText = (category: string) => {
  const map: Record<string, string> = {
    YOGA: '瑜伽',
    PILATES: '普拉提',
    CROSSFIT: '综合体能',
    SPINNING: '动感单车',
    BODYBUILDING: '力量训练',
    AEROBICS: '有氧运动',
    PERSONAL_TRAINING: '私教课'
  }
  return map[category] || category
}

const getStatusType = (status: CourseStatus) => {
  switch (status) {
    case 'SCHEDULED':
      return 'primary'
    case 'IN_PROGRESS':
      return 'warning'
    case 'COMPLETED':
      return 'success'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status: CourseStatus) => {
  switch (status) {
    case 'SCHEDULED':
      return '已安排'
    case 'IN_PROGRESS':
      return '进行中'
    case 'COMPLETED':
      return '已完成'
    case 'CANCELLED':
      return '已取消'
    default:
      return '未知'
  }
}

const getBookingPercentage = (course: Course) => {
  if (!course.capacity) return 0
  return Math.round((course.currentBookings / course.capacity) * 100)
}

const getCapacityColor = (course: Course) => {
  const percentage = getBookingPercentage(course)
  if (percentage >= 90) return '#f56c6c'
  if (percentage >= 70) return '#e6a23c'
  return '#67c23a'
}

// 生命周期
onMounted(() => {
  initCurrentWeek()
  loadSchedule()
})
</script>

<style lang="scss" scoped>
.course-schedule-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .header-left {
      .page-title {
        font-size: 24px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
        margin: 0 0 8px;
      }
      
      .el-breadcrumb {
        font-size: 14px;
        color: var(--gymflow-text-secondary);
      }
    }
  }
  
  .schedule-card {
    border-radius: 12px;
    margin-bottom: 24px;
    
    :deep(.el-card__body) {
      padding: 20px;
    }
    
    .calendar-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      
      .week-navigation {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .week-title {
          font-size: 18px;
          font-weight: 600;
          color: var(--gymflow-text-primary);
          min-width: 200px;
          text-align: center;
        }
      }
      
      .view-actions {
        display: flex;
        align-items: center;
        gap: 12px;
      }
    }
    
    .week-view {
      .time-grid {
        display: flex;
        border: 1px solid var(--gymflow-border);
        border-radius: 8px;
        overflow: hidden;
        
        .time-axis {
          width: 80px;
          flex-shrink: 0;
          
          .time-header {
            height: 60px;
            background: var(--gymflow-bg);
            border-bottom: 1px solid var(--gymflow-border);
          }
          
          .time-slot {
            height: 60px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 12px;
            color: var(--gymflow-text-secondary);
            border-bottom: 1px solid var(--gymflow-border);
            background: var(--gymflow-card-bg);
          }
        }
        
        .day-column {
          flex: 1;
          border-left: 1px solid var(--gymflow-border);
          
          .day-header {
            height: 60px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background: var(--gymflow-bg);
            border-bottom: 1px solid var(--gymflow-border);
            
            .day-name {
              font-size: 14px;
              font-weight: 600;
              color: var(--gymflow-text-primary);
            }
            
            .day-date {
              font-size: 12px;
              color: var(--gymflow-text-secondary);
              margin: 2px 0;
            }
            
            .day-count {
              font-size: 11px;
              color: var(--gymflow-primary);
              background: rgba(0, 184, 148, 0.1);
              padding: 1px 6px;
              border-radius: 10px;
            }
          }
          
          .day-time-slots {
            .time-slot {
              height: 60px;
              position: relative;
              border-bottom: 1px solid var(--gymflow-border);
              cursor: pointer;
              transition: background 0.2s;
              
              &:hover {
                background: rgba(0, 184, 148, 0.05);
              }
              
              .course-card {
                position: absolute;
                left: 4px;
                right: 4px;
                top: 2px;
                bottom: 2px;
                padding: 8px;
                border-radius: 6px;
                font-size: 12px;
                cursor: pointer;
                overflow: hidden;
                transition: transform 0.2s;
                
                &:hover {
                  transform: translateY(-2px);
                  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                }
                
                &.scheduled {
                  background: rgba(64, 158, 255, 0.1);
                  border: 1px solid rgba(64, 158, 255, 0.3);
                }
                
                &.in_progress {
                  background: rgba(230, 162, 60, 0.1);
                  border: 1px solid rgba(230, 162, 60, 0.3);
                }
                
                &.completed {
                  background: rgba(103, 194, 58, 0.1);
                  border: 1px solid rgba(103, 194, 58, 0.3);
                }
                
                &.cancelled {
                  background: rgba(245, 108, 108, 0.1);
                  border: 1px solid rgba(245, 108, 108, 0.3);
                  opacity: 0.7;
                }
                
                &.full {
                  position: relative;
                  
                  &::after {
                    content: '满';
                    position: absolute;
                    top: 2px;
                    right: 2px;
                    background: var(--el-color-danger);
                    color: white;
                    font-size: 10px;
                    padding: 1px 4px;
                    border-radius: 4px;
                  }
                }
                
                .course-title {
                  font-weight: 600;
                  color: var(--gymflow-text-primary);
                  margin-bottom: 2px;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                }
                
                .course-info {
                  display: flex;
                  justify-content: space-between;
                  font-size: 10px;
                  color: var(--gymflow-text-secondary);
                  margin-bottom: 2px;
                }
                
                .course-time {
                  font-size: 10px;
                  color: var(--gymflow-text-secondary);
                }
              }
            }
          }
        }
      }
    }
    
    .day-view {
      .day-header-large {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        padding-bottom: 16px;
        border-bottom: 1px solid var(--gymflow-border);
        
        h3 {
          font-size: 20px;
          font-weight: 600;
          color: var(--gymflow-text-primary);
          margin: 0;
        }
        
        .day-stats {
          display: flex;
          gap: 20px;
          
          .stat-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 14px;
            color: var(--gymflow-text-secondary);
            
            .el-icon {
              color: var(--gymflow-primary);
            }
          }
        }
      }
      
      .day-timeline {
        border-left: 2px solid var(--gymflow-border);
        margin-left: 80px;
        position: relative;
        
        .timeline-hour {
          display: flex;
          position: relative;
          height: 48px;
          
          .hour-label {
            position: absolute;
            left: -80px;
            top: 0;
            width: 70px;
            text-align: right;
            padding-right: 10px;
            font-size: 14px;
            color: var(--gymflow-text-secondary);
            line-height: 48px;
          }
          
          .hour-content {
            flex: 1;
            position: relative;
            border-bottom: 1px solid var(--gymflow-border);
            
            .timeline-course {
              position: absolute;
              left: 8px;
              right: 8px;
              background: rgba(0, 184, 148, 0.1);
              border: 1px solid rgba(0, 184, 148, 0.3);
              border-radius: 6px;
              padding: 8px;
              cursor: pointer;
              overflow: hidden;
              
              &:hover {
                background: rgba(0, 184, 148, 0.15);
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
              }
              
              .course-content {
                .course-title {
                  font-size: 14px;
                  font-weight: 600;
                  color: var(--gymflow-text-primary);
                  margin-bottom: 2px;
                }
                
                .course-coach {
                  font-size: 12px;
                  color: var(--gymflow-text-secondary);
                  margin-bottom: 2px;
                }
                
                .course-time {
                  font-size: 12px;
                  color: var(--gymflow-text-secondary);
                }
              }
            }
          }
        }
      }
    }
  }
  
  .course-list-card {
    border-radius: 12px;
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid var(--gymflow-border);
    }
    
    .list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
      }
    }
    
    :deep(.el-table) {
      th {
        background: var(--gymflow-bg);
        font-weight: 600;
      }
      
      .course-cell {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .course-name {
          font-weight: 500;
        }
      }
      
      .time-cell {
        .time-range {
          font-size: 12px;
          color: var(--gymflow-text-secondary);
          margin-top: 2px;
        }
      }
      
      .capacity-cell {
        display: flex;
        flex-direction: column;
        gap: 4px;
        
        .capacity-text {
          font-size: 12px;
          color: var(--gymflow-text-secondary);
          text-align: center;
        }
      }
      
      .delete-btn {
        color: var(--el-color-danger);
        
        &:hover {
          background: rgba(245, 108, 108, 0.1);
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .course-schedule-container {
    padding: 16px;
    
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    
    .schedule-card {
      .calendar-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 16px;
      }
      
      .week-view {
        overflow-x: auto;
        
        .time-grid {
          min-width: 800px;
        }
      }
      
      .day-view {
        .day-header-large {
          flex-direction: column;
          align-items: flex-start;
          gap: 12px;
        }
        
        .day-timeline {
          margin-left: 60px;
          
          .timeline-hour {
            .hour-label {
              left: -60px;
              width: 50px;
            }
          }
        }
      }
    }
  }
}
</style>