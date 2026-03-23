<template>
  <div class="course-detail-container">
    <!-- 页面头部 -->
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <div class="header-content">
          <span class="page-title">课程详情</span>
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

      <div class="info-details">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="课程名称">{{ courseDetail?.courseName }}</el-descriptions-item>
          <el-descriptions-item label="课程类型">{{ courseDetail?.courseTypeDesc}}</el-descriptions-item>
          <el-descriptions-item label="课时消耗">
            <span>{{ courseDetail?.sessionCost || 1 }}课时</span>
          </el-descriptions-item>
          <el-descriptions-item label="课时长">{{ courseDetail?.duration || 0 }}分钟</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(courseDetail?.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="最后更新">{{ formatDateTime(courseDetail?.updateTime) }}</el-descriptions-item>
        </el-descriptions>
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
      <!-- 排课信息 -->
      <el-tab-pane label="排课信息" name="schedules">
        <el-card shadow="never" class="tab-content">
          <template #header>
            <div class="tab-header">
              <span class="tab-title">排课列表</span>
              <el-button v-permission="'course:schedule:set'" type="primary" size="small" @click="handleAddSchedule" v-if="courseDetail?.courseType === 1">
                <el-icon>
                  <Plus />
                </el-icon>
                添加排课
              </el-button>
            </div>
          </template>

          <div v-if="courseDetail?.schedules && courseDetail.schedules.length > 0">
            <el-table :data="courseDetail.schedules" style="width: 100%">
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
              <el-table-column prop="maxCapacity" label="最大人数" width="100" align="center" />
              <el-table-column prop="currentEnrollment" label="已报人数" width="100" align="center" />
              <el-table-column prop="remainingSlots" label="剩余名额" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getRemainingSlotsType(row.remainingSlots)" size="small">
                    {{ row.remainingSlots }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="statusDesc" label="状态" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="getScheduleStatusType(row.status)" size="small">
                    {{ row.statusDesc }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="notes" label="备注" min-width="150">
                <template #default="{ row }">
                  <span class="notes-text">{{ row.notes || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button type="text" size="small" @click="handleViewScheduleDetail(row)">
                    查看预约
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

    <!-- 排课详情弹窗（显示预约列表） -->
    <el-dialog v-model="scheduleDialog.visible" :title="scheduleDialog.title" width="800px">
      <div v-if="selectedSchedule" class="schedule-detail">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="上课日期">{{ formatDate(selectedSchedule.scheduleDate) }}</el-descriptions-item>
          <el-descriptions-item label="上课时间">
            {{ selectedSchedule.startTime?.slice(0, 5) }} - {{ selectedSchedule.endTime?.slice(0, 5) }}
          </el-descriptions-item>
          <el-descriptions-item label="教练">{{ selectedSchedule.coachName }}</el-descriptions-item>
          <el-descriptions-item label="最大人数">{{ selectedSchedule.maxCapacity }}</el-descriptions-item>
          <el-descriptions-item label="已报人数">{{ selectedSchedule.currentEnrollment }}</el-descriptions-item>
          <el-descriptions-item label="剩余名额">{{ selectedSchedule.remainingSlots }}</el-descriptions-item>
        </el-descriptions>

        <!-- 预约列表 -->
        <div class="booking-section">
          <h3 class="section-title">预约列表</h3>
          <el-table :data="selectedSchedule.bookings" size="small">
            <el-table-column prop="memberName" label="会员姓名" width="100" />
            <el-table-column prop="memberPhone" label="手机号" width="120" />
            <el-table-column prop="bookingStatusDesc" label="预约状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getBookingStatusType(row.bookingStatus)" size="small">
                  {{ row.bookingStatusDesc }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="bookingTime" label="预约时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.bookingTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="checkinTime" label="签到时间" width="160">
              <template #default="{ row }">
                {{ row.checkinTime ? formatDateTime(row.checkinTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="签到码" width="100">
              <template #default="{ row }">
                <span v-if="row.signCode">{{ row.signCode }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useCourseStore } from '@/stores/course'
import type { CourseScheduleVO } from '@/types/course'

const router = useRouter()
const route = useRoute()
const courseStore = useCourseStore()

const loading = ref(false)
const activeTab = ref('schedules')

const courseId = computed(() => Number(route.params.id))
const courseDetail = computed(() => courseStore.currentCourse)

// 排课详情弹窗
const scheduleDialog = ref({
  visible: false,
  title: '排课详情',
})
const selectedSchedule = ref<CourseScheduleVO | null>(null)

// 格式化函数
const formatDate = (date: string | null | undefined) => {
  if (!date) return '-'
  return date
}

const formatDateTime = (datetime: string | null | undefined) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ')
}

const getRemainingSlotsType = (slots: number) => {
  if (slots > 5) return 'success'
  if (slots > 0) return 'warning'
  return 'danger'
}

const getScheduleStatusType = (status: number) => {
  return status === 1 ? 'success' : 'danger'
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

// 加载课程详情
const loadCourseDetail = async () => {
  try {
    loading.value = true
    await courseStore.fetchCourseDetail(courseId.value)
  } catch (error) {
    console.error('加载课程详情失败:', error)
    ElMessage.error('加载课程详情失败')
  } finally {
    loading.value = false
  }
}

// 排课操作
const handleAddSchedule = () => {
  router.push(`/course/schedule/${courseId.value}`)
}

const handleViewScheduleDetail = (schedule: CourseScheduleVO) => {
  selectedSchedule.value = schedule
  scheduleDialog.value.visible = true
}

// 导航
const goBack = () => {
  router.push('/course/list')
}

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

.info-details {
  margin-bottom: 30px;
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

.schedule-detail {
  padding: 10px;
}

.booking-section {
  margin-top: 20px;
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