<template>
  <div class="checkin-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">签到管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>课程预约</el-breadcrumb-item>
          <el-breadcrumb-item>签到管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleGenerateCode">
          <el-icon><QrCode /></el-icon>
          生成签到码
        </el-button>
        <el-button @click="handleScan">
          <el-icon><Camera /></el-icon>
          扫码签到
        </el-button>
      </div>
    </div>
    
    <!-- 今日课程列表 -->
    <el-card class="today-courses-card">
      <template #header>
        <div class="card-header">
          <h3>今日课程</h3>
          <el-select
            v-model="selectedCourseId"
            placeholder="请选择课程"
            style="width: 300px;"
            @change="handleCourseChange"
          >
            <el-option
              v-for="course in todayCourses"
              :key="course.id"
              :label="`${course.name} (${formatTime(course.startTime)}-${formatTime(course.endTime)})`"
              :value="course.id"
            />
          </el-select>
        </div>
      </template>
      
      <div class="course-info" v-if="selectedCourse">
        <div class="course-header">
          <div class="course-title">
            <h4>{{ selectedCourse.name }}</h4>
            <el-tag :type="getStatusType(selectedCourse.status)" size="small">
              {{ getStatusText(selectedCourse.status) }}
            </el-tag>
          </div>
          <div class="course-meta">
            <div class="meta-item">
              <el-icon><UserFilled /></el-icon>
              <span>{{ selectedCourse.coachName }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Location /></el-icon>
              <span>{{ selectedCourse.location }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(selectedCourse.startTime) }} - {{ formatTime(selectedCourse.endTime) }}</span>
            </div>
          </div>
        </div>
        
        <div class="booking-stats">
          <div class="stat-item">
            <div class="stat-value">{{ selectedCourse.currentBookings }}</div>
            <div class="stat-label">预约人数</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ selectedCourse.capacity }}</div>
            <div class="stat-label">课程容量</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ checkedInCount }}</div>
            <div class="stat-label">已签到</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ waitingCheckinCount }}</div>
            <div class="stat-label">待签到</div>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 预约列表 -->
    <el-card class="bookings-card" v-if="selectedCourse">
      <template #header>
        <div class="bookings-header">
          <h3>预约列表</h3>
          <div class="header-actions">
            <el-button
              type="primary"
              @click="handleBatchCheckIn"
              :disabled="waitingCheckinCount === 0"
            >
              <el-icon><Check /></el-icon>
              批量签到 ({{ waitingCheckinCount }}人)
            </el-button>
            <el-button @click="refreshBookings">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="bookings-grid">
        <div
          v-for="booking in bookings"
          :key="booking.id"
          class="booking-card"
          :class="{
            'checked-in': booking.status === 'CHECKED_IN',
            'checked-out': booking.status === 'CHECKED_OUT',
            'no-show': booking.status === 'NO_SHOW',
            'cancelled': booking.status === 'CANCELLED'
          }"
        >
          <div class="booking-header">
            <el-avatar :size="40" :src="booking.member?.avatar" class="member-avatar">
              {{ booking.memberName?.charAt(0) || 'M' }}
            </el-avatar>
            <div class="member-info">
              <div class="member-name">{{ booking.memberName }}</div>
              <div class="member-no">{{ booking.member?.memberNo }}</div>
            </div>
            <el-tag :type="getBookingStatusType(booking.status)" size="small">
              {{ getBookingStatusText(booking.status) }}
            </el-tag>
          </div>
          
          <div class="booking-body">
            <div class="booking-time" v-if="booking.checkInTime">
              签到: {{ formatTime(booking.checkInTime) }}
            </div>
            <div class="booking-time" v-if="booking.checkOutTime">
              签退: {{ formatTime(booking.checkOutTime) }}
            </div>
            <div class="booking-notes" v-if="booking.notes">
              {{ booking.notes }}
            </div>
          </div>
          
          <div class="booking-actions">
            <el-button
              type="primary"
              size="small"
              @click="handleCheckIn(booking)"
              v-if="booking.status === 'BOOKED'"
            >
              <el-icon><Check /></el-icon>
              签到
            </el-button>
            
            <el-button
              type="warning"
              size="small"
              @click="handleCheckOut(booking)"
              v-if="booking.status === 'CHECKED_IN'"
            >
              <el-icon><SwitchButton /></el-icon>
              签退
            </el-button>
            
            <el-button
              type="danger"
              size="small"
              @click="handleMarkNoShow(booking)"
              v-if="booking.status === 'BOOKED'"
            >
              <el-icon><Close /></el-icon>
              缺席
            </el-button>
          </div>
        </div>
        
        <!-- 空状态 -->
        <div v-if="bookings.length === 0" class="empty-bookings">
          <el-empty description="暂无预约记录" />
        </div>
      </div>
    </el-card>
    
    <!-- 快速签到 -->
    <el-card class="quick-checkin-card" v-else>
      <div class="quick-checkin-content">
        <el-empty description="请选择课程开始签到" :image-size="200">
          <template #image>
            <el-icon size="80"><Calendar /></el-icon>
          </template>
        </el-empty>
      </div>
    </el-card>
    
    <!-- 生成签到码对话框 -->
    <el-dialog
      v-model="codeDialog.visible"
      title="生成签到码"
      width="400px"
    >
      <div class="code-dialog-content" v-if="codeDialog.code">
        <div class="code-info">
          <div class="code-title">签到码信息</div>
          <div class="code-details">
            <div class="detail-item">
              <span class="label">会员:</span>
              <span class="value">{{ codeDialog.memberName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">有效期:</span>
              <span class="value">{{ formatDateTime(codeDialog.expiresAt) }}</span>
            </div>
          </div>
        </div>
        
        <div class="code-display">
          <div class="code-text">{{ codeDialog.code }}</div>
          <div class="code-hint">请将此码出示给工作人员</div>
        </div>
        
        <div class="code-actions">
          <el-button type="primary" @click="copyCode">
            <el-icon><DocumentCopy /></el-icon>
            复制
          </el-button>
          <el-button @click="printCode">
            <el-icon><Printer /></el-icon>
            打印
          </el-button>
        </div>
      </div>
      <div v-else class="no-code-content">
        <el-select
          v-model="codeDialog.memberId"
          placeholder="请选择会员"
          style="width: 100%;"
          @change="generateCode"
        >
          <el-option
            v-for="member in activeMembers"
            :key="member.id"
            :label="`${member.realName} (${member.memberNo})`"
            :value="member.id"
          />
        </el-select>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="codeDialog.visible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useCourseStore } from '@/stores/course'
import { useMemberStore } from '@/stores/member'
import { bookingApi } from '@/api/booking'
import { checkInApi } from '@/api/checkin'
import { formatTime, formatDateTime, copyToClipboard } from '@/utils'
import type { Course, CourseBooking, BookingStatus } from '@/types'

// Store
const courseStore = useCourseStore()
const memberStore = useMemberStore()

// 状态
const loading = ref(false)
const selectedCourseId = ref<number | null>(null)
const bookings = ref<CourseBooking[]>([])

const codeDialog = reactive({
  visible: false,
  memberId: null as number | null,
  memberName: '',
  code: '',
  expiresAt: ''
})

// Computed
const todayCourses = computed(() => courseStore.courses.filter(course => {
  const today = new Date().toISOString().split('T')[0]
  const courseDate = new Date(course.startTime).toISOString().split('T')[0]
  return courseDate === today
}))

const selectedCourse = computed(() => {
  return todayCourses.value.find(course => course.id === selectedCourseId.value)
})

const activeMembers = computed(() => {
  return memberStore.members.filter(member => member.status === 'ACTIVE')
})

const checkedInCount = computed(() => {
  return bookings.value.filter(b => b.status === 'CHECKED_IN' || b.status === 'CHECKED_OUT').length
})

const waitingCheckinCount = computed(() => {
  return bookings.value.filter(b => b.status === 'BOOKED').length
})

// Methods
const loadTodayCourses = async () => {
  try {
    loading.value = true
    const today = new Date().toISOString().split('T')[0]
    await courseStore.fetchCourses({
      startDate: today,
      endDate: today,
      pageSize: 20
    })
    
    // 如果有课程，默认选中第一个
    if (todayCourses.value.length > 0 && !selectedCourseId.value) {
      selectedCourseId.value = todayCourses.value[0].id
      loadBookings()
    }
  } catch (error) {
    console.error('加载今日课程失败:', error)
    ElMessage.error('加载今日课程失败')
  } finally {
    loading.value = false
  }
}

const loadBookings = async () => {
  if (!selectedCourseId.value) return
  
  try {
    loading.value = true
    const data = await bookingApi.getCourseBookings(selectedCourseId.value)
    bookings.value = data.sort((a, b) => {
      // 按状态排序：已签到 > 待签到 > 已签退 > 缺席 > 已取消
      const statusOrder = {
        'CHECKED_IN': 0,
        'BOOKED': 1,
        'CHECKED_OUT': 2,
        'NO_SHOW': 3,
        'CANCELLED': 4
      }
      return (statusOrder[a.status] || 5) - (statusOrder[b.status] || 5)
    })
  } catch (error) {
    console.error('加载预约列表失败:', error)
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

const loadMembers = async () => {
  try {
    await memberStore.fetchMembers({ status: 'ACTIVE', pageSize: 100 })
  } catch (error) {
    console.error('加载会员列表失败:', error)
  }
}

const handleCourseChange = () => {
  if (selectedCourseId.value) {
    loadBookings()
  }
}

const refreshBookings = () => {
  loadBookings()
}

const handleCheckIn = async (booking: CourseBooking) => {
  try {
    await ElMessageBox.confirm(`确认为 ${booking.memberName} 签到吗？`, '签到确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await bookingApi.checkIn(booking.id)
    ElMessage.success('签到成功')
    loadBookings()
  } catch (error) {
    console.error('签到失败:', error)
    ElMessage.error('签到失败')
  }
}

const handleCheckOut = async (booking: CourseBooking) => {
  try {
    await ElMessageBox.confirm(`确认为 ${booking.memberName} 签退吗？`, '签退确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await bookingApi.checkOut(booking.id)
    ElMessage.success('签退成功')
    loadBookings()
  } catch (error) {
    console.error('签退失败:', error)
    ElMessage.error('签退失败')
  }
}

const handleMarkNoShow = async (booking: CourseBooking) => {
  try {
    await ElMessageBox.confirm(`标记 ${booking.memberName} 为缺席吗？`, '标记缺席', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 这里需要调用标记缺席的API
    ElMessage.success('已标记为缺席')
    loadBookings()
  } catch (error) {
    console.error('标记失败:', error)
    ElMessage.error('标记失败')
  }
}

const handleBatchCheckIn = async () => {
  if (waitingCheckinCount.value === 0) return
  
  try {
    await ElMessageBox.confirm(`确认为 ${waitingCheckinCount.value} 名会员批量签到吗？`, '批量签到', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    const waitingBookings = bookings.value.filter(b => b.status === 'BOOKED')
    const bookingIds = waitingBookings.map(b => b.id)
    
    await bookingApi.batchCheckIn(bookingIds)
    ElMessage.success(`成功为 ${waitingCheckinCount.value} 名会员签到`)
    loadBookings()
  } catch (error) {
    console.error('批量签到失败:', error)
    ElMessage.error('批量签到失败')
  }
}

const handleGenerateCode = () => {
  codeDialog.visible = true
  codeDialog.memberId = null
  codeDialog.code = ''
  codeDialog.memberName = ''
  codeDialog.expiresAt = ''
}

const handleScan = () => {
  ElMessage.info('扫码签到功能开发中')
}

const generateCode = async () => {
  if (!codeDialog.memberId) return
  
  try {
    loading.value = true
    const data = await checkInApi.generateCheckInCode(codeDialog.memberId)
    
    const member = activeMembers.value.find(m => m.id === codeDialog.memberId)
    codeDialog.code = data.code
    codeDialog.expiresAt = data.expiresAt
    codeDialog.memberName = member ? `${member.realName} (${member.memberNo})` : ''
  } catch (error) {
    console.error('生成签到码失败:', error)
    ElMessage.error('生成签到码失败')
  } finally {
    loading.value = false
  }
}

const copyCode = async () => {
  if (!codeDialog.code) return
  
  const success = await copyToClipboard(codeDialog.code)
  if (success) {
    ElMessage.success('签到码已复制到剪贴板')
  } else {
    ElMessage.error('复制失败')
  }
}

const printCode = () => {
  window.print()
}

const getStatusType = (status?: string) => {
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

const getStatusText = (status?: string) => {
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

const getBookingStatusType = (status: BookingStatus) => {
  switch (status) {
    case 'BOOKED':
      return 'primary'
    case 'CHECKED_IN':
      return 'success'
    case 'CHECKED_OUT':
      return 'info'
    case 'NO_SHOW':
      return 'danger'
    case 'CANCELLED':
      return 'danger'
    default:
      return 'info'
  }
}

const getBookingStatusText = (status: BookingStatus) => {
  switch (status) {
    case 'BOOKED':
      return '已预约'
    case 'CHECKED_IN':
      return '已签到'
    case 'CHECKED_OUT':
      return '已签退'
    case 'NO_SHOW':
      return '缺席'
    case 'CANCELLED':
      return '已取消'
    default:
      return '未知'
  }
}

// 生命周期
onMounted(() => {
  loadTodayCourses()
  loadMembers()
})
</script>

<style lang="scss" scoped>
.checkin-container {
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
  
  .today-courses-card {
    border-radius: 12px;
    margin-bottom: 20px;
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid var(--gymflow-border);
    }
    
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
    
    .course-info {
      .course-header {
        margin-bottom: 20px;
        
        .course-title {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 12px;
          
          h4 {
            font-size: 20px;
            font-weight: 600;
            color: var(--gymflow-text-primary);
            margin: 0;
          }
        }
        
        .course-meta {
          display: flex;
          gap: 24px;
          
          .meta-item {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            color: var(--gymflow-text-secondary);
            
            .el-icon {
              font-size: 16px;
            }
          }
        }
      }
      
      .booking-stats {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 16px;
        padding: 16px;
        background: var(--gymflow-bg);
        border-radius: 8px;
        
        .stat-item {
          text-align: center;
          
          .stat-value {
            font-size: 24px;
            font-weight: 700;
            color: var(--gymflow-primary);
            margin-bottom: 4px;
          }
          
          .stat-label {
            font-size: 12px;
            color: var(--gymflow-text-secondary);
          }
        }
      }
    }
  }
  
  .bookings-card {
    border-radius: 12px;
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid var(--gymflow-border);
    }
    
    .bookings-header {
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
    
    .bookings-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 16px;
      
      .booking-card {
        padding: 16px;
        border-radius: 8px;
        border: 1px solid var(--gymflow-border);
        transition: all 0.3s;
        
        &.checked-in {
          border-color: var(--el-color-success);
          background: rgba(103, 194, 58, 0.05);
        }
        
        &.checked-out {
          border-color: var(--el-color-info);
          background: rgba(144, 147, 153, 0.05);
        }
        
        &.no-show {
          border-color: var(--el-color-danger);
          background: rgba(245, 108, 108, 0.05);
          opacity: 0.8;
        }
        
        &.cancelled {
          border-color: var(--el-color-danger);
          background: rgba(245, 108, 108, 0.05);
          opacity: 0.6;
        }
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        
        .booking-header {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-bottom: 12px;
          
          .member-avatar {
            background: var(--gymflow-primary);
            color: white;
          }
          
          .member-info {
            flex: 1;
            
            .member-name {
              font-size: 16px;
              font-weight: 600;
              color: var(--gymflow-text-primary);
              margin-bottom: 2px;
            }
            
            .member-no {
              font-size: 12px;
              color: var(--gymflow-text-secondary);
            }
          }
        }
        
        .booking-body {
          margin-bottom: 12px;
          
          .booking-time {
            font-size: 12px;
            color: var(--gymflow-text-secondary);
            margin-bottom: 4px;
          }
          
          .booking-notes {
            font-size: 12px;
            color: var(--gymflow-text-primary);
            font-style: italic;
          }
        }
        
        .booking-actions {
          display: flex;
          gap: 8px;
          
          .el-button {
            flex: 1;
          }
        }
      }
      
      .empty-bookings {
        grid-column: 1 / -1;
        padding: 40px 0;
      }
    }
  }
  
  .quick-checkin-card {
    border-radius: 12px;
    
    .quick-checkin-content {
      padding: 60px 0;
    }
  }
}

// 生成签到码对话框
.code-dialog-content {
  .code-info {
    margin-bottom: 20px;
    
    .code-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--gymflow-text-primary);
      margin-bottom: 12px;
    }
    
    .code-details {
      .detail-item {
        display: flex;
        margin-bottom: 8px;
        
        &:last-child {
          margin-bottom: 0;
        }
        
        .label {
          font-size: 14px;
          color: var(--gymflow-text-secondary);
          min-width: 60px;
        }
        
        .value {
          font-size: 14px;
          font-weight: 500;
          color: var(--gymflow-text-primary);
        }
      }
    }
  }
  
  .code-display {
    text-align: center;
    padding: 24px;
    background: var(--gymflow-bg);
    border-radius: 8px;
    margin-bottom: 20px;
    
    .code-text {
      font-family: monospace;
      font-size: 28px;
      font-weight: 700;
      color: var(--gymflow-primary);
      letter-spacing: 2px;
      margin-bottom: 8px;
    }
    
    .code-hint {
      font-size: 12px;
      color: var(--gymflow-text-secondary);
    }
  }
  
  .code-actions {
    display: flex;
    gap: 12px;
    justify-content: center;
  }
}

.no-code-content {
  padding: 20px 0;
}

// 响应式设计
@media (max-width: 768px) {
  .checkin-container {
    padding: 16px;
    
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    
    .today-courses-card {
      .card-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
      }
      
      .course-info {
        .booking-stats {
          grid-template-columns: repeat(2, 1fr);
        }
      }
    }
    
    .bookings-card {
      .bookings-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
      }
      
      .bookings-grid {
        grid-template-columns: 1fr;
      }
    }
  }
}
</style>