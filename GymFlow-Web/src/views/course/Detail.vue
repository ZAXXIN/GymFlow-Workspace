<template>
  <div class="course-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button icon="i-ep-arrow-left" @click="goBack">
          返回
        </el-button>
        <h1 class="page-title">课程详情</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleEdit" v-if="canEdit">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-dropdown @command="handleMoreAction">
          <el-button>
            <el-icon><MoreFilled /></el-icon>
            更多操作
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="bookings">
                <el-icon><List /></el-icon>
                查看预约
              </el-dropdown-item>
              <el-dropdown-item command="checkin" v-if="canCheckin">
                <el-icon><Check /></el-icon>
                批量签到
              </el-dropdown-item>
              <el-dropdown-item command="cancel" v-if="canCancel">
                <el-icon><Close /></el-icon>
                取消课程
              </el-dropdown-item>
              <el-dropdown-item command="complete" v-if="canComplete">
                <el-icon><Finished /></el-icon>
                完成课程
              </el-dropdown-item>
              <el-dropdown-divider />
              <el-dropdown-item command="delete" class="delete-item" v-if="canDelete">
                <el-icon><Delete /></el-icon>
                删除课程
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 课程基本信息 -->
    <el-row :gutter="20" class="course-info-section">
      <el-col :xs="24" :md="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <h3>课程信息</h3>
              <el-tag :type="getStatusType(courseData?.status)" size="large">
                {{ getStatusText(courseData?.status) }}
              </el-tag>
            </div>
          </template>
          
          <div class="course-basic-info">
            <h2 class="course-title">{{ courseData?.name }}</h2>
            <p class="course-description">{{ courseData?.description || '暂无描述' }}</p>
            
            <div class="info-grid">
              <div class="info-item">
                <span class="label">课程编号:</span>
                <span class="value">{{ courseData?.courseNo }}</span>
              </div>
              <div class="info-item">
                <span class="label">课程分类:</span>
                <el-tag :type="getCategoryType(courseData?.category)" size="small">
                  {{ getCategoryText(courseData?.category) }}
                </el-tag>
              </div>
              <div class="info-item">
                <span class="label">课程难度:</span>
                <el-tag :type="getDifficultyType(courseData?.difficulty)" size="small">
                  {{ getDifficultyText(courseData?.difficulty) }}
                </el-tag>
              </div>
              <div class="info-item">
                <span class="label">授课教练:</span>
                <span class="value">{{ courseData?.coachName }}</span>
              </div>
              <div class="info-item">
                <span class="label">上课地点:</span>
                <span class="value">{{ courseData?.location }}</span>
              </div>
              <div class="info-item">
                <span class="label">课程时长:</span>
                <span class="value">{{ courseData?.duration }}分钟</span>
              </div>
              <div class="info-item">
                <span class="label">课程价格:</span>
                <span class="value price">¥{{ courseData?.price }}</span>
              </div>
              <div class="info-item">
                <span class="label">课程容量:</span>
                <span class="value">{{ courseData?.capacity }}人</span>
              </div>
            </div>
            
            <div class="capacity-progress">
              <div class="progress-header">
                <span>预约进度</span>
                <span class="booking-count">
                  {{ courseData?.currentBookings || 0 }}/{{ courseData?.capacity || 0 }}
                </span>
              </div>
              <el-progress
                :percentage="getBookingPercentage()"
                :stroke-width="12"
                :color="getCapacityColor()"
                :show-text="false"
              />
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="8">
        <!-- 课程时间 -->
        <el-card class="time-card">
          <template #header>
            <h3>课程时间</h3>
          </template>
          
          <div class="time-info">
            <div class="time-item">
              <el-icon class="time-icon"><Clock /></el-icon>
              <div class="time-details">
                <div class="date">{{ formatDate(courseData?.startTime) }}</div>
                <div class="time-range">
                  {{ formatTime(courseData?.startTime) }} - {{ formatTime(courseData?.endTime) }}
                </div>
              </div>
            </div>
            
            <div class="time-remaining" v-if="isUpcoming">
              <el-icon><Timer /></el-icon>
              <span>距离课程开始还有 {{ getTimeRemaining() }}</span>
            </div>
            
            <div class="time-passed" v-else-if="isInProgress">
              <el-icon><Timer /></el-icon>
              <span>课程进行中，已进行 {{ getTimeElapsed() }}</span>
            </div>
            
            <div class="time-completed" v-else-if="isCompleted">
              <el-icon><Finished /></el-icon>
              <span>课程已完成</span>
            </div>
          </div>
        </el-card>
        
        <!-- 操作面板 -->
        <el-card class="action-card" v-if="showActions">
          <template #header>
            <h3>快速操作</h3>
          </template>
          
          <div class="action-buttons">
            <el-button
              type="primary"
              class="action-btn"
              @click="handleViewBookings"
              :disabled="!courseData?.currentBookings"
            >
              <el-icon><List /></el-icon>
              查看预约 ({{ courseData?.currentBookings || 0 }})
            </el-button>
            
            <el-button
              type="success"
              class="action-btn"
              @click="handleQuickCheckin"
              v-if="canCheckin"
            >
              <el-icon><Check /></el-icon>
              批量签到
            </el-button>
            
            <el-button
              type="warning"
              class="action-btn"
              @click="handleAddBooking"
              v-if="canAddBooking"
            >
              <el-icon><User /></el-icon>
              添加预约
            </el-button>
            
            <el-button
              type="danger"
              class="action-btn"
              @click="handleCancelCourse"
              v-if="canCancel"
            >
              <el-icon><Close /></el-icon>
              取消课程
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- Tab页 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <!-- 预约列表 -->
      <el-tab-pane label="预约列表" name="bookings">
        <booking-list :course-id="courseId" />
      </el-tab-pane>
      
      <!-- 签到记录 -->
      <el-tab-pane label="签到记录" name="checkins">
        <checkin-list :course-id="courseId" />
      </el-tab-pane>
      
      <!-- 课程统计 -->
      <el-tab-pane label="课程统计" name="statistics">
        <course-statistics :course-id="courseId" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCourseStore } from '@/stores/course'
import { useAuthStore } from '@/stores/auth'
import { formatDate, formatTime } from '@/utils'
import { CourseStatus } from '@/types'
import BookingList from './BookingList.vue'
import CheckinList from './CheckinList.vue'
import CourseStatistics from './CourseStatistics.vue'

// Store
const courseStore = useCourseStore()
const authStore = useAuthStore()

// Router
const route = useRoute()
const router = useRouter()

// 状态
const loading = ref(false)
const activeTab = ref('bookings')

// Computed
const courseId = computed(() => parseInt(route.params.id as string))
const courseData = computed(() => courseStore.currentCourse)

const isUpcoming = computed(() => {
  if (!courseData.value?.startTime) return false
  const startTime = new Date(courseData.value.startTime)
  const now = new Date()
  return startTime > now && courseData.value.status === 'SCHEDULED'
})

const isInProgress = computed(() => {
  if (!courseData.value?.startTime || !courseData.value?.endTime) return false
  const startTime = new Date(courseData.value.startTime)
  const endTime = new Date(courseData.value.endTime)
  const now = new Date()
  return now >= startTime && now <= endTime && courseData.value.status === 'IN_PROGRESS'
})

const isCompleted = computed(() => {
  return courseData.value?.status === 'COMPLETED'
})

const canEdit = computed(() => {
  if (!courseData.value) return false
  return authStore.isAdmin && ['SCHEDULED', 'IN_PROGRESS'].includes(courseData.value.status)
})

const canCheckin = computed(() => {
  if (!courseData.value) return false
  return (authStore.isAdmin || authStore.isCoach) && 
    ['IN_PROGRESS', 'SCHEDULED'].includes(courseData.value.status)
})

const canCancel = computed(() => {
  if (!courseData.value) return false
  return authStore.isAdmin && courseData.value.status === 'SCHEDULED'
})

const canComplete = computed(() => {
  if (!courseData.value) return false
  return authStore.isAdmin && courseData.value.status === 'IN_PROGRESS'
})

const canDelete = computed(() => {
  if (!courseData.value) return false
  return authStore.isAdmin && ['CANCELLED', 'COMPLETED'].includes(courseData.value.status)
})

const canAddBooking = computed(() => {
  if (!courseData.value) return false
  return authStore.isAdmin && courseData.value.status === 'SCHEDULED' && 
    (courseData.value.currentBookings || 0) < courseData.value.capacity
})

const showActions = computed(() => {
  return canCheckin.value || canAddBooking.value || canCancel.value
})

// Methods
const goBack = () => {
  router.push('/courses')
}

const loadCourseData = async () => {
  try {
    loading.value = true
    await courseStore.fetchCourseById(courseId.value)
  } catch (error) {
    console.error('加载课程数据失败:', error)
    ElMessage.error('加载课程数据失败')
    goBack()
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  router.push(`/courses/${courseId.value}/edit`)
}

const handleMoreAction = async (command: string) => {
  switch (command) {
    case 'bookings':
      handleViewBookings()
      break
    case 'checkin':
      handleQuickCheckin()
      break
    case 'cancel':
      handleCancelCourse()
      break
    case 'complete':
      handleCompleteCourse()
      break
    case 'delete':
      handleDeleteCourse()
      break
  }
}

const handleViewBookings = () => {
  activeTab.value = 'bookings'
}

const handleQuickCheckin = () => {
  router.push(`/checkin?courseId=${courseId.value}`)
}

const handleAddBooking = () => {
  // 实现添加预约功能
  ElMessage.info('添加预约功能开发中')
}

const handleCancelCourse = async () => {
  try {
    await ElMessageBox.confirm('确认取消该课程吗？已预约的会员将收到通知。', '取消课程', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await courseStore.updateCourse(courseId.value, { status: 'CANCELLED' })
    ElMessage.success('课程已取消')
    loadCourseData()
  } catch (error) {
    console.error('取消课程失败:', error)
    ElMessage.error('取消课程失败')
  }
}

const handleCompleteCourse = async () => {
  try {
    await ElMessageBox.confirm('确认完成该课程吗？', '完成课程', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await courseStore.updateCourse(courseId.value, { status: 'COMPLETED' })
    ElMessage.success('课程已完成')
    loadCourseData()
  } catch (error) {
    console.error('完成课程失败:', error)
    ElMessage.error('完成课程失败')
  }
}

const handleDeleteCourse = async () => {
  try {
    await ElMessageBox.confirm('确认删除该课程吗？此操作不可恢复。', '删除课程', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    
    await courseStore.deleteCourse(courseId.value)
    ElMessage.success('删除成功')
    goBack()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

const getStatusType = (status?: CourseStatus) => {
  if (!status) return 'info'
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

const getStatusText = (status?: CourseStatus) => {
  if (!status) return '未知'
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

const getCategoryType = (category?: string) => {
  if (!category) return 'info'
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

const getCategoryText = (category?: string) => {
  if (!category) return ''
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

const getDifficultyType = (difficulty?: string) => {
  if (!difficulty) return 'info'
  switch (difficulty) {
    case 'BEGINNER':
      return 'success'
    case 'INTERMEDIATE':
      return 'warning'
    case 'ADVANCED':
      return 'danger'
    default:
      return 'info'
  }
}

const getDifficultyText = (difficulty?: string) => {
  if (!difficulty) return ''
  switch (difficulty) {
    case 'BEGINNER':
      return '初级'
    case 'INTERMEDIATE':
      return '中级'
    case 'ADVANCED':
      return '高级'
    default:
      return difficulty
  }
}

const getBookingPercentage = () => {
  if (!courseData.value?.capacity || !courseData.value?.currentBookings) return 0
  return Math.round((courseData.value.currentBookings / courseData.value.capacity) * 100)
}

const getCapacityColor = () => {
  const percentage = getBookingPercentage()
  if (percentage >= 90) return '#f56c6c'
  if (percentage >= 70) return '#e6a23c'
  return '#67c23a'
}

const getTimeRemaining = () => {
  if (!courseData.value?.startTime) return ''
  const startTime = new Date(courseData.value.startTime)
  const now = new Date()
  const diffMs = startTime.getTime() - now.getTime()
  
  if (diffMs <= 0) return '已开始'
  
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  const diffHours = Math.floor((diffMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  
  if (diffDays > 0) return `${diffDays}天${diffHours}小时`
  if (diffHours > 0) return `${diffHours}小时${diffMinutes}分钟`
  return `${diffMinutes}分钟`
}

const getTimeElapsed = () => {
  if (!courseData.value?.startTime) return ''
  const startTime = new Date(courseData.value.startTime)
  const now = new Date()
  const diffMs = now.getTime() - startTime.getTime()
  
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  
  if (diffHours > 0) return `${diffHours}小时${diffMinutes}分钟`
  return `${diffMinutes}分钟`
}

// 生命周期
onMounted(() => {
  loadCourseData()
})
</script>

<style lang="scss" scoped>
.course-detail-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .page-title {
        font-size: 24px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
        margin: 0;
      }
    }
    
    .header-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }
  
  .course-info-section {
    margin-bottom: 24px;
    
    .info-card {
      border-radius: 12px;
      height: 100%;
      
      :deep(.el-card__header) {
        padding: 20px;
        border-bottom: 1px solid var(--gymflow-border);
        
        .card-header {
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
      }
      
      .course-basic-info {
        .course-title {
          font-size: 24px;
          font-weight: 700;
          color: var(--gymflow-text-primary);
          margin: 0 0 12px;
        }
        
        .course-description {
          font-size: 14px;
          color: var(--gymflow-text-secondary);
          line-height: 1.6;
          margin: 0 0 24px;
          padding: 16px;
          background: var(--gymflow-bg);
          border-radius: 8px;
        }
        
        .info-grid {
          display: grid;
          grid-template-columns: repeat(2, 1fr);
          gap: 16px;
          margin-bottom: 24px;
          
          .info-item {
            display: flex;
            align-items: center;
            gap: 8px;
            
            .label {
              font-size: 14px;
              color: var(--gymflow-text-secondary);
              min-width: 80px;
            }
            
            .value {
              font-size: 14px;
              font-weight: 500;
              color: var(--gymflow-text-primary);
              
              &.price {
                color: var(--gymflow-primary);
                font-weight: 600;
              }
            }
          }
        }
        
        .capacity-progress {
          .progress-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
            
            span {
              font-size: 14px;
              color: var(--gymflow-text-primary);
              font-weight: 500;
            }
            
            .booking-count {
              font-size: 16px;
              font-weight: 600;
              color: var(--gymflow-primary);
            }
          }
        }
      }
    }
    
    .time-card {
      border-radius: 12px;
      margin-bottom: 20px;
      
      :deep(.el-card__header) {
        padding: 16px 20px;
        border-bottom: 1px solid var(--gymflow-border);
        
        h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
          color: var(--gymflow-text-primary);
        }
      }
      
      .time-info {
        .time-item {
          display: flex;
          align-items: center;
          gap: 16px;
          margin-bottom: 20px;
          
          .time-icon {
            font-size: 24px;
            color: var(--gymflow-primary);
          }
          
          .time-details {
            .date {
              font-size: 18px;
              font-weight: 600;
              color: var(--gymflow-text-primary);
              margin-bottom: 4px;
            }
            
            .time-range {
              font-size: 14px;
              color: var(--gymflow-text-secondary);
            }
          }
        }
        
        .time-remaining,
        .time-passed,
        .time-completed {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 12px;
          border-radius: 8px;
          background: var(--gymflow-bg);
          
          .el-icon {
            color: var(--gymflow-primary);
          }
          
          span {
            font-size: 14px;
            color: var(--gymflow-text-primary);
            font-weight: 500;
          }
        }
        
        .time-remaining {
          background: rgba(64, 158, 255, 0.1);
          
          .el-icon {
            color: #409eff;
          }
        }
        
        .time-passed {
          background: rgba(230, 162, 60, 0.1);
          
          .el-icon {
            color: #e6a23c;
          }
        }
        
        .time-completed {
          background: rgba(103, 194, 58, 0.1);
          
          .el-icon {
            color: #67c23a;
          }
        }
      }
    }
    
    .action-card {
      border-radius: 12px;
      
      :deep(.el-card__header) {
        padding: 16px 20px;
        border-bottom: 1px solid var(--gymflow-border);
        
        h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
          color: var(--gymflow-text-primary);
        }
      }
      
      .action-buttons {
        display: flex;
        flex-direction: column;
        gap: 12px;
        
        .action-btn {
          width: 100%;
          justify-content: flex-start;
          padding: 12px 16px;
          
          .el-icon {
            margin-right: 8px;
          }
        }
      }
    }
  }
  
  .detail-tabs {
    :deep(.el-tabs__header) {
      margin: 0;
      background: var(--gymflow-card-bg);
      border-radius: 8px 8px 0 0;
      padding: 0 20px;
    }
    
    :deep(.el-tabs__content) {
      background: var(--gymflow-card-bg);
      border-radius: 0 0 8px 8px;
      padding: 20px;
    }
  }
}

:deep(.delete-item) {
  color: var(--el-color-danger);
  
  &:hover {
    background: rgba(245, 108, 108, 0.1);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .course-detail-container {
    padding: 16px;
    
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    
    .course-info-section {
      .info-card {
        .course-basic-info {
          .info-grid {
            grid-template-columns: 1fr;
          }
        }
      }
    }
  }
}
</style>