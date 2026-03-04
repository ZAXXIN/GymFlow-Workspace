<template>
  <div class="checkin-detail-container">
    <!-- 页面头部 -->
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <div class="header-content">
          <span class="page-title">签到详情</span>
          <div class="header-actions">
            <el-button-group>
              <el-button type="warning" v-if="canEdit" @click="handleEdit">
                <el-icon>
                  <Edit />
                </el-icon>
                编辑
              </el-button>
              <!-- <el-button
                type="danger"
                v-if="canDelete"
                @click="handleDelete"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
              <el-dropdown @command="handleMoreAction">
                <el-button>
                  更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="print">
                      <el-icon><Printer /></el-icon>
                      打印记录
                    </el-dropdown-item>
                    <el-dropdown-item command="member" divided>
                      <el-icon><User /></el-icon>
                      查看会员
                    </el-dropdown-item>
                    <el-dropdown-item v-if="isCourseCheckIn" command="course">
                      <el-icon><Calendar /></el-icon>
                      查看课程
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown> -->
            </el-button-group>
          </div>
        </div>
      </template>
    </el-page-header>

    <!-- 基本信息卡片 -->
    <el-card class="info-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">基本信息</span>
          <div class="card-actions">
            <el-tag :type="getCheckInTypeTagType(currentCheckIn)" size="large">
              {{ currentCheckIn?.courseCheckIn ? '课程签到' : '自由训练' }}
            </el-tag>
            <span class="checkin-time">{{ formatDateTime(currentCheckIn?.checkinTime) }}</span>
          </div>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="会员信息">
          <div class="member-info">
            <div class="member-name">{{ currentCheckIn?.memberName || '-' }}</div>
            <div class="member-detail">
              <span>手机：{{ currentCheckIn?.memberPhone || '-' }}</span>
              <span>会员号：{{ currentCheckIn?.memberNo || '-' }}</span>
            </div>
            <div class="member-detail">
              <span>性别：{{ currentCheckIn?.genderDesc || '-' }}</span>
              <span v-if="currentCheckIn?.personalCoachName">专属教练：{{ currentCheckIn.personalCoachName }}</span>
            </div>
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="签到信息">
          <div class="checkin-info">
            <div class="checkin-method">
              <el-tag :type="currentCheckIn?.checkinMethod === 0 ? 'warning' : 'info'" size="small">
                {{ currentCheckIn?.checkinMethodDesc || '-' }}
              </el-tag>
            </div>
            <div class="checkin-time-detail">
              签到时间：{{ formatDateTime(currentCheckIn?.checkinTime) }}
            </div>
            <div class="create-time">
              创建时间：{{ formatDateTime(currentCheckIn?.createTime) }}
            </div>
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="备注" :span="2">
          <div class="notes-content">
            {{ currentCheckIn?.notes || '无' }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 课程信息卡片（如果是课程签到） -->
    <el-card class="course-card" v-if="isCourseCheckIn">
      <template #header>
        <div class="card-header">
          <span class="card-title">课程信息</span>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="预约编号">
          <span class="info-value">{{ currentCheckIn?.courseBookingId || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="预约时间">
          {{ formatDateTime(currentCheckIn?.bookingTime) }}
        </el-descriptions-item>

        <el-descriptions-item label="课程名称">
          <div class="course-name">{{ currentCheckIn?.courseName || '-' }}</div>
          <div class="course-type">{{ currentCheckIn?.courseTypeDesc || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="课程时间">
          <div v-if="currentCheckIn?.courseDate && currentCheckIn?.startTime && currentCheckIn?.endTime">
            <div>{{ formatDate(currentCheckIn.courseDate) }}</div>
            <div>{{ currentCheckIn.startTime.slice(0, 5) }} - {{ currentCheckIn.endTime.slice(0, 5) }}</div>
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="上课地点">
          {{ currentCheckIn?.location || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="教练信息">
          <div v-if="currentCheckIn?.coachName">
            <div class="coach-name">{{ currentCheckIn.coachName }}</div>
            <div class="coach-phone">{{ currentCheckIn.coachPhone }}</div>
          </div>
        </el-descriptions-item>

        <el-descriptions-item label="预约状态" :span="2">
          <el-tag :type="getBookingStatusTagType(currentCheckIn?.bookingStatus)" size="small">
            {{ currentCheckIn?.bookingStatusDesc || '-' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 会员近期签到记录 -->
    <el-card class="recent-checkins-card" v-if="currentCheckIn">
      <template #header>
        <div class="card-header">
          <span class="card-title">会员近期签到记录</span>
          <router-link :to="`/member/detail/${currentCheckIn.memberId}`" class="member-link">
            <el-button type="primary" link size="small">
              查看会员详情
            </el-button>
          </router-link>
        </div>
      </template>

      <div v-if="recentCheckIns && recentCheckIns.length > 0">
        <el-timeline>
          <el-timeline-item v-for="(item, index) in recentCheckIns" :key="index" :timestamp="formatDateTime(item.checkinTime)" placement="top" :type="item.id === currentCheckIn.id ? 'primary' : 'info'" :hollow="item.id !== currentCheckIn.id">
            <el-card shadow="hover">
              <div class="timeline-content">
                <div class="checkin-item">
                  <div class="checkin-header">
                    <el-tag :type="item.checkinMethod === 0 ? 'warning' : 'info'" size="small">
                      {{ item.checkinMethodDesc }}
                    </el-tag>
                    <el-tag :type="item.courseCheckIn ? 'success' : 'primary'" size="small">
                      {{ item.courseCheckIn ? '课程签到' : '自由训练' }}
                    </el-tag>
                    <span class="notes" v-if="item.notes">{{ item.notes }}</span>
                  </div>
                  <div class="checkin-footer">
                    <span class="course-name" v-if="item.courseName">
                      {{ item.courseName }}
                    </span>
                    <span class="time-diff" v-if="index > 0">
                      距上次签到：{{ getTimeDiff(item.checkinTime, recentCheckIns[index - 1].checkinTime) }}
                    </span>
                  </div>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <div v-else class="empty-data">
        <el-empty description="暂无近期签到记录" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCheckInStore } from '@/stores/checkIn'
import type { CheckInDetailVO } from '@/types/checkIn'

const router = useRouter()
const route = useRoute()
const checkInStore = useCheckInStore()

const loading = ref(false)
const recentCheckIns = ref<any[]>([])

const checkInId = computed(() => Number(route.params.id))
const currentCheckIn = computed(() => checkInStore.formattedCurrentCheckIn())

// 是否为课程签到
const isCourseCheckIn = computed(() => {
  return currentCheckIn.value?.courseCheckIn || false
})

// 是否可以编辑（只能编辑一周内的自由训练签到）
const canEdit = computed(() => {
  if (!currentCheckIn.value || isCourseCheckIn.value) return false
  const checkinTime = new Date(currentCheckIn.value.checkinTime)
  const oneWeekAgo = new Date()
  oneWeekAgo.setDate(oneWeekAgo.getDate() - 7)
  return checkinTime > oneWeekAgo
})

// 是否可以删除（只能删除自由训练签到）
const canDelete = computed(() => {
  return !isCourseCheckIn.value
})

// 格式化函数
const formatDate = (date: string | null | undefined) => {
  if (!date) return '-'
  return date.split('T')[0]
}

const formatDateTime = (datetime: string | null | undefined) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ')
}

// 获取签到类型标签类型
const getCheckInTypeTagType = (checkIn: CheckInDetailVO | null) => {
  if (!checkIn) return 'info'
  return checkIn.courseCheckIn ? 'success' : 'primary'
}

// 获取预约状态标签类型
const getBookingStatusTagType = (status: number | undefined) => {
  if (status === undefined) return 'info'
  switch (status) {
    case 0:
      return 'warning'
    case 1:
      return 'success'
    case 2:
      return 'info'
    case 3:
      return 'danger'
    default:
      return 'info'
  }
}

// 获取时间差
const getTimeDiff = (time1: string, time2: string) => {
  const diff = Math.abs(new Date(time1).getTime() - new Date(time2).getTime())
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))

  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

// 加载签到详情
const loadCheckInDetail = async () => {
  try {
    loading.value = true
    await checkInStore.fetchCheckInDetail(checkInId.value)

    // 加载会员近期签到记录
    if (currentCheckIn.value?.memberId) {
      await loadRecentCheckIns(currentCheckIn.value.memberId)
    }
  } catch (error) {
    console.error('加载签到详情失败:', error)
    ElMessage.error('加载签到详情失败')
  } finally {
    loading.value = false
  }
}

// 加载会员近期签到记录
const loadRecentCheckIns = async (memberId: number) => {
  try {
    const params = {
      memberId,
      pageNum: 1,
      pageSize: 10,
    }
    const response = await checkInStore.fetchMemberCheckIns(memberId, params)
    recentCheckIns.value = response.list || []
  } catch (error) {
    console.error('加载近期签到记录失败:', error)
  }
}

// 编辑签到
const handleEdit = async () => {
  try {
    const { value: notes } = await ElMessageBox.prompt('请输入新的备注信息', '编辑签到', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputValue: currentCheckIn.value?.notes || '',
    })

    await checkInStore.updateCheckIn(checkInId.value, notes)
    ElMessage.success('更新成功')
    await loadCheckInDetail()
  } catch (error) {
    // 用户取消
  }
}

// 删除签到
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这条签到记录吗？', '警告', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })

    await checkInStore.deleteCheckIn(checkInId.value)
    ElMessage.success('删除成功')
    goBack()
  } catch (error) {
    // 用户取消
  }
}

// 更多操作
const handleMoreAction = (command: string) => {
  switch (command) {
    case 'print':
      handlePrint()
      break
    case 'member':
      handleViewMember()
      break
    case 'course':
      handleViewCourse()
      break
  }
}

// 打印记录
const handlePrint = () => {
  ElMessage.info('打印功能开发中...')
}

// 查看会员
const handleViewMember = () => {
  if (currentCheckIn.value?.memberId) {
    router.push(`/member/detail/${currentCheckIn.value.memberId}`)
  }
}

// 查看课程
const handleViewCourse = () => {
  if (currentCheckIn.value?.courseId) {
    router.push(`/course/detail/${currentCheckIn.value.courseId}`)
  }
}

// 返回
const goBack = () => {
  router.push('/checkIn/list')
}

onMounted(() => {
  loadCheckInDetail()
})
</script>

<style scoped lang="scss">
.checkin-detail-container {
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

.header-actions {
  display: flex;
  gap: 8px;
}

.info-card,
.course-card,
.recent-checkins-card {
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

.card-actions {
  display: flex;
  align-items: center;
  gap: 12px;

  .checkin-time {
    font-size: 14px;
    color: #909399;
  }
}

.member-info {
  .member-name {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  .member-detail {
    font-size: 14px;
    color: #606266;
    margin-bottom: 4px;

    span {
      margin-right: 20px;
    }
  }
}

.checkin-info {
  .checkin-method {
    margin-bottom: 8px;
  }

  .checkin-time-detail,
  .create-time {
    font-size: 14px;
    color: #606266;
    margin-bottom: 4px;
  }
}

.notes-content {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.course-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.course-type {
  font-size: 12px;
  color: #909399;
}

.coach-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.coach-phone {
  font-size: 12px;
  color: #909399;
}

.info-value {
  font-weight: 500;
  color: #409eff;
}

.member-link {
  text-decoration: none;
}

.timeline-content {
  .checkin-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;

    .notes {
      color: #909399;
      font-size: 13px;
      flex: 1;
    }
  }

  .checkin-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 13px;

    .course-name {
      color: #67c23a;
    }

    .time-diff {
      color: #909399;
    }
  }
}

.empty-data {
  padding: 40px 0;
  text-align: center;
}

:deep(.el-card__header) {
  padding: 16px 20px;
}

:deep(.el-descriptions__body) {
  background-color: white;
}

:deep(.el-descriptions__cell) {
  padding: 12px 16px;
}

:deep(.el-timeline) {
  padding-left: 40px;
}
</style>