<template>
  <div class="course-detail-container">
    <!-- 页面头部 -->
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <div class="header-content">
          <span class="page-title">课程详情</span>
          <!-- <div class="header-actions">
            <el-button type="primary" @click="goEdit">编辑</el-button>
            <el-button type="info" @click="handleViewSchedule">排课管理</el-button>
            <el-button @click="goBack">返回</el-button>
          </div> -->
        </div>
      </template>
    </el-page-header>

    <!-- 基本信息卡片 -->
    <el-card class="info-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">基本信息</span>
          <div class="card-actions">
            <el-tag :type="courseDetail?.status == 1 ? 'success' : 'danger'" size="large">
              {{ courseDetail?.status == 1 ? '正常' : '禁用' }}
            </el-tag>
          </div>
        </div>
      </template>

      <div class="basic-info">
        <!-- <div class="course-section">
          <div class="course-name">{{ courseDetail?.courseName }}</div>
          <div class="course-type">
            <el-tag :type="courseDetail?.courseType === 0 ? 'primary' : 'success'" size="large">
              {{ courseDetail?.courseTypeDesc }}
            </el-tag>
          </div>
        </div> -->

        <div class="info-details">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="课程名称">{{ courseDetail?.courseName }}</el-descriptions-item>
            <el-descriptions-item label="课程类型">{{ courseDetail?.courseTypeDesc}}</el-descriptions-item>
            <el-descriptions-item label="价格">
              <span class="amount">¥{{ formatAmount(courseDetail?.price) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="最大容量">{{ courseDetail?.maxCapacity || 0 }}人</el-descriptions-item>
            <el-descriptions-item label="当前报名">{{ courseDetail?.currentEnrollment || 0 }}人</el-descriptions-item>
            <el-descriptions-item label="课时长">{{ courseDetail?.duration || 0 }}分钟</el-descriptions-item>
            <el-descriptions-item label="上课地点">{{ courseDetail?.location || '-' }}</el-descriptions-item>
            <!-- <el-descriptions-item label="创建时间">{{ formatDateTime(courseDetail?.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="最后更新">{{ formatDateTime(courseDetail?.updateTime) }}</el-descriptions-item> -->
          </el-descriptions>
        </div>
      </div>

      <!-- 绑定教练 -->
      <div class="coach-section">
        <h3 class="section-title">绑定教练</h3>
        <div class="coach-list">
          <div v-for="coach in courseDetail?.coaches" :key="coach.id" class="coach-item">
            <div class="coach-avatar">
              {{ coach.realName?.charAt(0) }}
            </div>
            <div class="coach-info">
              <div class="coach-name">{{ coach.realName }}</div>
              <div class="coach-specialty">{{ coach.specialty || '暂无专长' }}</div>
              <div class="coach-id" style="font-size: 12px; color: #909399;">
                教练ID: {{ coach.id }}
              </div>
            </div>
          </div>
          <div v-if="!courseDetail?.coaches?.length" class="no-coach">
            暂无绑定的教练
          </div>
        </div>
      </div>

      <!-- 课程描述 -->
      <div class="description-section" v-if="courseDetail?.description">
        <h3 class="section-title">课程描述</h3>
        <div class="description-content">
          {{ courseDetail.description }}
        </div>
      </div>

      <!-- 课程须知 -->
      <div class="notice-section" v-if="courseDetail?.notice">
        <h3 class="section-title">课程须知</h3>
        <div class="notice-content">
          {{ courseDetail.notice }}
        </div>
      </div>
    </el-card>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <!-- 预约信息 -->
      <el-tab-pane label="预约信息" name="bookings">
        <el-card shadow="never" class="tab-content">
          <template #header>
            <div class="tab-header">
              <span class="tab-title">预约列表</span>
              <div class="tab-actions">
                <el-select v-model="bookingFilter.status" placeholder="预约状态" size="small" style="width: 120px;" clearable>
                  <el-option label="待上课" :value="0" />
                  <el-option label="已签到" :value="1" />
                  <el-option label="已完成" :value="2" />
                  <el-option label="已取消" :value="3" />
                </el-select>
              </div>
            </div>
          </template>

          <div v-if="courseDetail?.bookings && courseDetail.bookings.length > 0">
            <el-table :data="filteredBookings" style="width: 100%">
              <el-table-column prop="memberName" label="会员姓名" width="120" />
              <el-table-column prop="memberPhone" label="手机号" width="120" />
              <el-table-column prop="bookingTime" label="预约时间" width="160">
                <template #default="{ row }">
                  {{ formatDateTime(row.bookingTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="bookingStatusDesc" label="预约状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getBookingStatusType(row.bookingStatus)" size="small">
                    {{ row.bookingStatusDesc }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="checkinTime" label="签到时间" width="160">
                <template #default="{ row }">
                  {{ row.checkinTime ? formatDateTime(row.checkinTime) : '-' }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <template v-if="row.bookingStatus === 0">
                    <el-button type="primary" size="small" @click="handleVerifyBooking(row.id)">
                      核销
                    </el-button>
                    <el-button type="warning" size="small" @click="handleCancelBooking(row)">
                      取消
                    </el-button>
                  </template>
                  <template v-else-if="row.bookingStatus === 3">
                    <el-tag type="info" size="small">已取消</el-tag>
                  </template>
                  <template v-else>
                    <el-tag type="success" size="small">已完成</el-tag>
                  </template>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div v-else class="empty-data">
            <el-empty description="暂无预约信息" />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 排课信息 -->
      <el-tab-pane label="排课信息" name="schedules">
        <el-card shadow="never" class="tab-content">
          <template #header>
            <div class="tab-header">
              <span class="tab-title">排课列表</span>
              <el-button type="primary" size="small" @click="handleAddSchedule" v-if="courseDetail?.courseType === 1">
                <el-icon>
                  <Plus />
                </el-icon>
                添加排课
              </el-button>
            </div>
          </template>

          <div v-if="courseSchedules.length > 0">
            <el-table :data="courseSchedules" style="width: 100%">
              <el-table-column prop="scheduleDate" label="排课日期" width="120" sortable>
                <template #default="{ row }">
                  {{ formatDate(row.scheduleDate) }}
                </template>
              </el-table-column>
              <el-table-column prop="startTime" label="开始时间" width="100">
                <template #default="{ row }">
                  {{ row.startTime?.slice(0, 5) }}
                </template>
              </el-table-column>
              <el-table-column prop="endTime" label="结束时间" width="100">
                <template #default="{ row }">
                  {{ row.endTime?.slice(0, 5) }}
                </template>
              </el-table-column>
              <el-table-column prop="coachName" label="教练" width="100" />
              <el-table-column prop="maxParticipants" label="最大人数" width="100" align="center" />
              <el-table-column prop="currentParticipants" label="已报人数" width="100" align="center" />
              <el-table-column prop="remainingSlots" label="剩余名额" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.remainingSlots > 0 ? 'success' : 'danger'" size="small">
                    {{ row.remainingSlots }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getScheduleStatusType(row.status)" size="small">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="notes" label="备注" min-width="150">
                <template #default="{ row }">
                  <span class="notes-text">{{ row.notes || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="180" fixed="right">
                <template #default="{ row }">
                  <el-button type="text" size="small" @click="handleViewScheduleDetail(row)">
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div v-else class="empty-data">
            <el-empty description="暂无排课信息" />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 核销弹窗 -->
    <el-dialog v-model="verifyDialog.visible" title="核销预约" width="400px">
      <el-form :model="verifyForm" label-width="80px">
        <el-form-item label="核销方式" required>
          <el-radio-group v-model="verifyForm.checkinMethod">
            <el-radio :label="1">扫码核销</el-radio>
            <el-radio :label="2">手动核销</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="verifyDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="confirmVerify" :loading="verifyDialog.loading">
            确定核销
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 取消预约弹窗 -->
    <el-dialog v-model="cancelDialog.visible" title="取消预约" width="400px">
      <el-form :model="cancelForm" label-width="80px">
        <el-form-item label="取消原因" required>
          <el-input v-model="cancelForm.reason" type="textarea" :rows="3" placeholder="请输入取消原因" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="confirmCancel" :loading="cancelDialog.loading">
            确定取消
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useCourseStore } from '@/stores/course'
import type { CourseDetail, CourseScheduleVO, CourseBookingDTO } from '@/types/course'

const router = useRouter()
const route = useRoute()
const courseStore = useCourseStore()

const loading = ref(false)
const activeTab = ref('bookings')
const courseSchedules = ref<CourseScheduleVO[]>([])

// 筛选条件
const bookingFilter = ref({
  status: undefined as number | undefined,
})

const courseId = computed(() => Number(route.params.id))
const courseDetail = computed(() => courseStore.currentCourse)

const filteredBookings = computed(() => {
  if (!courseDetail.value?.bookings) return []

  let bookings = courseDetail.value.bookings
  if (bookingFilter.value.status !== undefined) {
    bookings = bookings.filter((b) => b.bookingStatus === bookingFilter.value.status)
  }
  return bookings
})

// 弹窗相关
const verifyDialog = ref({
  visible: false,
  loading: false,
  bookingId: null as number | null,
})

const verifyForm = ref({
  checkinMethod: 1,
})

const cancelDialog = ref({
  visible: false,
  loading: false,
  bookingId: null as number | null,
})

const cancelForm = ref({
  reason: '',
})

// 格式化函数
const formatDate = (date: string | null | undefined) => {
  if (!date) return '-'
  return date
}

const formatDateTime = (datetime: string | null | undefined) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ')
}

const formatAmount = (amount: number | null | undefined) => {
  if (!amount) return '0.00'
  return amount.toFixed(2)
}

const getBookingStatusType = (status: number) => {
  switch (status) {
    case 0:
      return 'warning' // 待上课
    case 1:
      return 'success' // 已签到
    case 2:
      return 'info' // 已完成
    case 3:
      return 'danger' // 已取消
    default:
      return 'info'
  }
}

const getScheduleStatusType = (status: string) => {
  if (status === '正常') return 'success'
  if (status === '禁用') return 'danger'
  return 'info'
}

// 加载课程详情
const loadCourseDetail = async () => {
  try {
    loading.value = true
    await courseStore.fetchCourseDetail(courseId.value)
    courseSchedules.value = courseStore.courseSchedules
  } catch (error) {
    console.error('加载课程详情失败:', error)
    ElMessage.error('加载课程详情失败')
  } finally {
    loading.value = false
  }
}

// 核销预约
const handleVerifyBooking = (bookingId: number) => {
  verifyDialog.value.bookingId = bookingId
  verifyDialog.value.visible = true
  verifyForm.value.checkinMethod = 1
}

const confirmVerify = async () => {
  if (!verifyDialog.value.bookingId) return

  try {
    verifyDialog.value.loading = true
    await courseStore.verifyCourseBooking(
      verifyDialog.value.bookingId,
      verifyForm.value.checkinMethod
    )
    ElMessage.success('核销成功')
    verifyDialog.value.visible = false
    await loadCourseDetail()
  } catch (error) {
    console.error('核销失败:', error)
    ElMessage.error('核销失败')
  } finally {
    verifyDialog.value.loading = false
  }
}

// 取消预约
const handleCancelBooking = (booking: CourseBookingDTO) => {
  cancelDialog.value.bookingId = booking.id
  cancelDialog.value.visible = true
  cancelForm.value.reason = booking.cancellationReason || ''
}

const confirmCancel = async () => {
  if (!cancelDialog.value.bookingId || !cancelForm.value.reason.trim()) {
    ElMessage.warning('请输入取消原因')
    return
  }

  try {
    cancelDialog.value.loading = true
    await courseStore.cancelCourseBooking(cancelDialog.value.bookingId, cancelForm.value.reason)
    ElMessage.success('取消预约成功')
    cancelDialog.value.visible = false
    await loadCourseDetail()
  } catch (error) {
    console.error('取消预约失败:', error)
    ElMessage.error('取消预约失败')
  } finally {
    cancelDialog.value.loading = false
  }
}

// 排课操作
const handleAddSchedule = () => {
  router.push(`/course/schedule/${courseId.value}`)
}

const handleViewScheduleDetail = (schedule: CourseScheduleVO) => {
  // 可以跳转到排课详情页
  console.log('查看排课详情:', schedule)
}

// 导航
const goBack = () => {
  router.push('/course/list')
}

// const goEdit = () => {
//   router.push(`/course/edit/${courseId.value}`)
// }

const handleViewSchedule = () => {
  activeTab.value = 'schedules'
}

// 监听筛选条件变化
watch(
  bookingFilter,
  () => {
    // 这里可以添加筛选逻辑
  },
  { deep: true }
)

onMounted(() => {
  loadCourseDetail()
})
</script>

<style scoped>
.course-detail-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.basic-info {
  display: flex;
  gap: 40px;
  margin-bottom: 30px;
}

.course-section {
  text-align: center;
  flex-shrink: 0;
  min-width: 200px;
}

.course-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
}

.info-details {
  flex: 1;
}

.coach-section,
.description-section,
.notice-section {
  margin-top: 30px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.coach-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.coach-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
  min-width: 200px;
}

.coach-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 16px;
}

.coach-info {
  flex: 1;
}

.coach-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.coach-specialty {
  font-size: 12px;
  color: #909399;
}

.no-coach {
  color: #909399;
  font-style: italic;
  padding: 20px 0;
  text-align: center;
}

.description-content,
.notice-content {
  line-height: 1.6;
  color: #606266;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.detail-tabs {
  margin-top: 20px;
}

.tab-content {
  margin-top: 0;
  border: none;
}

.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.tab-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.empty-data {
  padding: 40px 0;
  text-align: center;
}

.amount {
  font-weight: 600;
  color: #67c23a;
}

.notes-text {
  color: #606266;
  font-size: 13px;
  line-height: 1.4;
}

:deep(.el-card__header) {
  padding: 16px 20px;
}

:deep(.el-tabs__header) {
  background-color: white;
  padding: 0 20px;
  margin: 0;
}

:deep(.el-descriptions__body) {
  background-color: white;
}

:deep(.el-descriptions__cell) {
  padding: 12px 16px;
}

:deep(.el-table__header) {
  background-color: #f8f9fa;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}
</style>