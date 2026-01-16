<template>
  <div class="booking-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>预订管理</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">课程预订管理</h1>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-form">
        <div class="filter-row">
          <div class="filter-item">
            <span class="filter-label">会员姓名：</span>
            <el-input
              v-model="filterParams.keyword"
              placeholder="请输入会员姓名"
              style="width: 200px"
              clearable
            />
          </div>
          
          <div class="filter-item">
            <span class="filter-label">课程名称：</span>
            <el-input
              v-model="filterParams.courseName"
              placeholder="请输入课程名称"
              style="width: 200px"
              clearable
            />
          </div>
          
          <div class="filter-item">
            <span class="filter-label">预订状态：</span>
            <el-select
              v-model="filterParams.status"
              placeholder="请选择预订状态"
              style="width: 150px"
              clearable
            >
              <el-option label="已预订" value="BOOKED" />
              <el-option label="已签到" value="CHECKED_IN" />
              <el-option label="已签出" value="CHECKED_OUT" />
              <el-option label="已取消" value="CANCELLED" />
              <el-option label="未到" value="NO_SHOW" />
            </el-select>
          </div>
        </div>
        
        <div class="filter-row">
          <div class="filter-item">
            <span class="filter-label">预订时间：</span>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 300px"
            />
          </div>
          
          <div class="filter-item">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <AppTable
        :data="bookingList"
        :loading="loading"
        :columns="columns"
        :pagination="pagination"
        @page-change="handlePageChange"
        @sort-change="handleSortChange"
      >
        <template #status="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>

        <template #actions="{ row }">
          <div class="action-buttons">
            <el-button type="primary" text size="small" @click="handleView(row)">
              查看
            </el-button>
            <el-button 
              v-if="row.status === 'BOOKED'" 
              type="success" 
              text 
              size="small"
              @click="handleCheckIn(row)"
            >
              签到
            </el-button>
            <el-button 
              v-if="row.status === 'CHECKED_IN'" 
              type="warning" 
              text 
              size="small"
              @click="handleCheckOut(row)"
            >
              签出
            </el-button>
            <el-button 
              v-if="row.status === 'BOOKED'" 
              type="danger" 
              text 
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </div>
        </template>
      </AppTable>
    </el-card>

    <!-- 查看详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      title="预订详情"
      :size="500"
      destroy-on-close
    >
      <div v-if="currentBooking" class="booking-detail">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-item">
            <span class="detail-label">预订编号：</span>
            <span class="detail-value">{{ currentBooking.bookingNo }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">会员姓名：</span>
            <span class="detail-value">{{ currentBooking.memberName }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">课程名称：</span>
            <span class="detail-value">{{ currentBooking.course?.name }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">教练：</span>
            <span class="detail-value">{{ currentBooking.course?.coachName }}</span>
          </div>
        </div>

        <div class="detail-section">
          <h3>时间信息</h3>
          <div class="detail-item">
            <span class="detail-label">预订时间：</span>
            <span class="detail-value">{{ formatDate(currentBooking.createdAt) }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">课程时间：</span>
            <span class="detail-value">{{ formatCourseTime(currentBooking.course) }}</span>
          </div>
          <div class="detail-item" v-if="currentBooking.checkInTime">
            <span class="detail-label">签到时间：</span>
            <span class="detail-value">{{ formatDateTime(currentBooking.checkInTime) }}</span>
          </div>
          <div class="detail-item" v-if="currentBooking.checkOutTime">
            <span class="detail-label">签出时间：</span>
            <span class="detail-value">{{ formatDateTime(currentBooking.checkOutTime) }}</span>
          </div>
        </div>

        <div class="detail-section">
          <h3>状态信息</h3>
          <div class="detail-item">
            <span class="detail-label">预订状态：</span>
            <el-tag :type="getStatusType(currentBooking.status)" size="small">
              {{ getStatusText(currentBooking.status) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <span class="detail-label">备注：</span>
            <span class="detail-value">{{ currentBooking.notes || '无' }}</span>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 签到弹窗 -->
    <el-dialog
      v-model="checkInDialog.visible"
      title="会员签到"
      width="400px"
      destroy-on-close
    >
      <div v-if="checkInDialog.booking" class="checkin-dialog">
        <el-form :model="checkInForm" label-width="100px">
          <el-form-item label="会员姓名：">
            <span>{{ checkInDialog.booking.memberName }}</span>
          </el-form-item>
          <el-form-item label="课程名称：">
            <span>{{ checkInDialog.booking.course?.name }}</span>
          </el-form-item>
          <el-form-item label="签到时间：">
            <el-time-picker
              v-model="checkInForm.checkInTime"
              placeholder="选择签到时间"
              value-format="HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="备注：">
            <el-input
              v-model="checkInForm.notes"
              type="textarea"
              :rows="2"
              placeholder="请输入签到备注"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="checkInDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="confirmCheckIn">确认签到</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import AppTable from '@/components/common/AppTable.vue'
import type { CourseBooking, BookingStatus, PageParams } from '@/types'

// 数据状态
const loading = ref(false)
const bookingList = ref<CourseBooking[]>([])
const dateRange = ref<string[]>([])
const detailVisible = ref(false)
const currentBooking = ref<CourseBooking | null>(null)

// 筛选参数
const filterParams = reactive({
  keyword: '',
  courseName: '',
  status: '',
  startDate: '',
  endDate: ''
})

// 分页参数
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 表格列定义
const columns = [
  { prop: 'bookingNo', label: '预订编号', width: 140 },
  { prop: 'memberName', label: '会员姓名', width: 120 },
  { prop: 'courseName', label: '课程名称', width: 180 },
  { prop: 'coachName', label: '教练', width: 120 },
  { prop: 'courseTime', label: '课程时间', width: 180 },
  { prop: 'createdAt', label: '预订时间', width: 180 },
  { prop: 'status', label: '状态', slot: true, width: 100 },
  { label: '操作', slot: true, width: 180, fixed: 'right' }
]

// 签到弹窗
const checkInDialog = reactive({
  visible: false,
  booking: null as CourseBooking | null
})

const checkInForm = reactive({
  checkInTime: '',
  notes: ''
})

// 状态类型映射
const statusTypeMap: Record<BookingStatus, string> = {
  BOOKED: 'primary',
  CHECKED_IN: 'success',
  CHECKED_OUT: 'info',
  CANCELLED: 'danger',
  NO_SHOW: 'warning'
}

const statusTextMap: Record<BookingStatus, string> = {
  BOOKED: '已预订',
  CHECKED_IN: '已签到',
  CHECKED_OUT: '已签出',
  CANCELLED: '已取消',
  NO_SHOW: '未到'
}

// 获取状态样式
const getStatusType = (status: BookingStatus) => {
  return statusTypeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: BookingStatus) => {
  return statusTextMap[status] || '未知'
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化日期时间
const formatDateTime = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化课程时间
const formatCourseTime = (course: any) => {
  if (!course) return ''
  return `${course.startTime} ~ ${course.endTime}`
}

// 查询数据
const fetchBookingList = async () => {
  loading.value = true
  try {
    // 这里调用API获取数据
    // const response = await getBookingList({ ...filterParams, ...pagination })
    // bookingList.value = response.data.items
    // pagination.total = response.data.total
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    
    bookingList.value = [
      {
        id: 1,
        bookingNo: 'BKG202300001',
        courseId: 1,
        memberId: 1,
        memberName: '张三',
        status: 'BOOKED',
        checkInTime: undefined,
        checkOutTime: undefined,
        notes: '',
        createdAt: '2023-06-15 09:30:00',
        updatedAt: '2023-06-15 09:30:00',
        course: {
          id: 1,
          courseNo: 'CRS2023001',
          name: '晨间瑜伽',
          coachName: '张教练',
          startTime: '08:30:00',
          endTime: '09:30:00'
        }
      },
      {
        id: 2,
        bookingNo: 'BKG202300002',
        courseId: 2,
        memberId: 2,
        memberName: '李四',
        status: 'CHECKED_IN',
        checkInTime: '2023-06-15 14:20:00',
        checkOutTime: undefined,
        notes: '按时到达',
        createdAt: '2023-06-14 16:00:00',
        updatedAt: '2023-06-15 14:20:00',
        course: {
          id: 2,
          courseNo: 'CRS2023002',
          name: '力量训练',
          coachName: '李教练',
          startTime: '14:30:00',
          endTime: '15:30:00'
        }
      },
      {
        id: 3,
        bookingNo: 'BKG202300003',
        courseId: 3,
        memberId: 3,
        memberName: '王五',
        status: 'CHECKED_OUT',
        checkInTime: '2023-06-15 19:10:00',
        checkOutTime: '2023-06-15 20:00:00',
        notes: '完成训练',
        createdAt: '2023-06-13 10:00:00',
        updatedAt: '2023-06-15 20:00:00',
        course: {
          id: 3,
          courseNo: 'CRS2023003',
          name: '动感单车',
          coachName: '王教练',
          startTime: '19:30:00',
          endTime: '20:30:00'
        }
      }
    ]
    
    pagination.total = bookingList.value.length
  } catch (error) {
    console.error('获取预订列表失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 处理查询
const handleSearch = () => {
  if (dateRange.value?.length === 2) {
    filterParams.startDate = dateRange.value[0]
    filterParams.endDate = dateRange.value[1]
  } else {
    filterParams.startDate = ''
    filterParams.endDate = ''
  }
  
  pagination.page = 1
  fetchBookingList()
}

// 重置筛选
const handleReset = () => {
  filterParams.keyword = ''
  filterParams.courseName = ''
  filterParams.status = ''
  filterParams.startDate = ''
  filterParams.endDate = ''
  dateRange.value = []
  pagination.page = 1
  fetchBookingList()
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.page = page
  fetchBookingList()
}

// 排序变化
const handleSortChange = (sort: any) => {
  console.log('排序变化:', sort)
  fetchBookingList()
}

// 查看详情
const handleView = (row: CourseBooking) => {
  currentBooking.value = row
  detailVisible.value = true
}

// 签到
const handleCheckIn = (row: CourseBooking) => {
  checkInDialog.booking = row
  checkInDialog.visible = true
  checkInForm.checkInTime = new Date().toTimeString().split(' ')[0]
  checkInForm.notes = ''
}

// 确认签到
const confirmCheckIn = async () => {
  if (!checkInDialog.booking) return
  
  try {
    // 这里调用API进行签到
    // await checkInBooking(checkInDialog.booking.id, checkInForm)
    
    ElMessage.success('签到成功')
    checkInDialog.visible = false
    fetchBookingList()
  } catch (error) {
    console.error('签到失败:', error)
    ElMessage.error('签到失败')
  }
}

// 签出
const handleCheckOut = async (row: CourseBooking) => {
  try {
    await ElMessageBox.confirm(
      `确定要签出会员 ${row.memberName} 吗？`,
      '确认签出',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里调用API进行签出
    // await checkOutBooking(row.id)
    
    ElMessage.success('签出成功')
    fetchBookingList()
  } catch {
    // 用户取消操作
  }
}

// 取消预订
const handleCancel = async (row: CourseBooking) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消 ${row.memberName} 的预订吗？`,
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里调用API取消预订
    // await cancelBooking(row.id)
    
    ElMessage.success('预订已取消')
    fetchBookingList()
  } catch {
    // 用户取消操作
  }
}

// 导出数据
const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

// 初始化
onMounted(() => {
  fetchBookingList()
})
</script>

<style scoped lang="scss">
.booking-list-container {
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

.filter-card {
  margin-bottom: 20px;

  .filter-form {
    .filter-row {
      display: flex;
      align-items: center;
      margin-bottom: 15px;

      &:last-child {
        margin-bottom: 0;
      }

      .filter-item {
        display: flex;
        align-items: center;
        margin-right: 20px;

        &:last-child {
          margin-right: 0;
        }

        .filter-label {
          margin-right: 8px;
          font-size: 14px;
          color: #606266;
          white-space: nowrap;
        }
      }
    }
  }
}

.table-card {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.booking-detail {
  padding: 0 20px;

  .detail-section {
    margin-bottom: 24px;

    h3 {
      margin: 0 0 16px 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      padding-bottom: 8px;
      border-bottom: 1px solid #ebeef5;
    }

    .detail-item {
      display: flex;
      align-items: center;
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .detail-label {
        width: 100px;
        font-size: 14px;
        color: #606266;
      }

      .detail-value {
        flex: 1;
        font-size: 14px;
        color: #303133;
      }
    }
  }
}

.checkin-dialog {
  .el-form-item {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}
</style>