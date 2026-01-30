<template>
  <div class="coach-detail-container">
    <!-- 页面头部 -->
    <el-page-header @back="goBack" class="page-header">
      <template #content>
        <div class="header-content">
          <span class="page-title">教练详情</span>
          <div class="header-actions">
            <!-- <el-button type="primary" @click="goEdit">编辑</el-button>
            <el-button type="info" @click="handleViewSchedule">排班管理</el-button>
            <el-button @click="goBack">返回</el-button> -->
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
            <el-tag :type="coachDetail?.status === 1 ? 'success' : 'danger'" size="large">
              {{ coachDetail?.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </div>
        </div>
      </template>
      
      <div class="basic-info">
        <div class="coach-section">
          <div class="coach-name">{{ coachDetail?.realName }}</div>
          <div class="coach-rating">
            <el-rate
              :model-value="coachDetail.rating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}分"
              :max="5"
              :allow-half="true"
            />
          </div>
        </div>
        
        <div class="info-details">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="教练ID">{{ coachDetail?.id || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ coachDetail?.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="专长领域">{{ coachDetail?.specialty || '-' }}</el-descriptions-item>
            <el-descriptions-item label="经验年限">{{ coachDetail?.yearsOfExperience || 0 }}年</el-descriptions-item>
            <el-descriptions-item label="时薪">
              <span class="amount">¥{{ formatAmount(coachDetail?.hourlyRate) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="提成比例">{{ coachDetail?.commissionRate || 0 }}%</el-descriptions-item>
            <el-descriptions-item label="入职时间">{{ formatDateTime(coachDetail?.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="最后更新">{{ formatDateTime(coachDetail?.updateTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      
      <!-- 资格证书 -->
      <div class="certification-section">
        <h3 class="section-title">资格证书</h3>
        <div class="certification-list">
          <el-tag
            v-for="(cert, index) in coachDetail?.certificationList"
            :key="index"
            type="info"
            size="large"
            class="certification-tag"
          >
            {{ cert }}
          </el-tag>
          <div v-if="!coachDetail?.certificationList?.length" class="no-certification">
            暂无资格证书
          </div>
        </div>
      </div>
      
      <!-- 个人简介 -->
      <div class="introduction-section">
        <h3 class="section-title">个人简介</h3>
        <div class="introduction-content">
          {{ coachDetail?.introduction || '暂无个人简介' }}
        </div>
      </div>
    </el-card>
    
    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-label">学员总数</div>
            <div class="stat-value">{{ coachDetail?.totalStudents || 0 }}</div>
            <div class="stat-unit">人</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-label">课程总数</div>
            <div class="stat-value">{{ coachDetail?.totalCourses || 0 }}</div>
            <div class="stat-unit">节</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-label">总收入</div>
            <div class="stat-value amount">¥{{ formatAmount(coachDetail?.totalIncome) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-label">平均评分</div>
            <div class="stat-value">{{ coachDetail?.rating?.toFixed(1) || '0.0' }}</div>
            <div class="stat-unit">分</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <!-- 排班信息 -->
      <el-tab-pane label="排班信息" name="schedules">
        <el-card shadow="never" class="tab-content">
          <template #header>
            <div class="tab-header">
              <span class="tab-title">排班信息</span>
              <el-button type="primary" size="small" @click="handleAddSchedule">
                <el-icon><Plus /></el-icon>
                添加排班
              </el-button>
            </div>
          </template>
          
          <div v-if="coachDetail?.schedules && coachDetail.schedules.length > 0">
            <el-table :data="coachDetail.schedules" style="width: 100%">
              <el-table-column prop="scheduleDate" label="排班日期" width="120" sortable>
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
              <el-table-column prop="scheduleTypeDesc" label="课程类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.scheduleType === 0 ? 'primary' : 'success'" size="small">
                    {{ row.scheduleTypeDesc }}
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
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button type="text" size="small" @click="handleEditSchedule(row)">
                    编辑
                  </el-button>
                  <el-button type="text" size="small" @click="handleDeleteSchedule(row)" style="color: #f56c6c;">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div v-else class="empty-data">
            <el-empty description="暂无排班信息" />
          </div>
        </el-card>
      </el-tab-pane>
      
      <!-- 课程信息 -->
      <el-tab-pane label="课程信息" name="courses">
        <el-card shadow="never" class="tab-content">
          <template #header>
            <div class="tab-header">
              <span class="tab-title">课程列表</span>
            </div>
          </template>
          
          <div v-if="coachDetail?.courses && coachDetail.courses.length > 0">
            <el-table :data="coachDetail.courses" style="width: 100%">
              <el-table-column prop="courseName" label="课程名称" min-width="150" />
              <el-table-column prop="courseType" label="课程类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.courseType === 0 ? 'primary' : 'success'" size="small">
                    {{ row.courseType === 0 ? '私教课' : '团课' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="courseDate" label="上课日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.courseDate) }}
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
              <el-table-column prop="duration" label="时长" width="80" align="center">
                <template #default="{ row }">
                  {{ row.duration }}分钟
                </template>
              </el-table-column>
              <el-table-column prop="price" label="价格" width="100" align="right">
                <template #default="{ row }">
                  <span class="amount">¥{{ formatAmount(row.price) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="location" label="地点" width="120" />
              <el-table-column prop="currentEnrollment" label="报名人数" width="100" align="center">
                <template #default="{ row }">
                  {{ row.currentEnrollment }}/{{ row.maxCapacity }}
                </template>
              </el-table-column>
              <el-table-column prop="enrollmentRate" label="满员率" width="100" align="center">
                <template #default="{ row }">
                  <el-progress 
                    :percentage="Math.round(row.enrollmentRate)" 
                    :stroke-width="18" 
                    :status="getEnrollmentStatus(row.enrollmentRate)"
                    :show-text="false"
                  />
                  <span style="font-size: 12px; margin-left: 5px;">{{ Math.round(row.enrollmentRate) }}%</span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="getCourseStatusType(row.status)" size="small">
                    {{ getCourseStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
          
          <div v-else class="empty-data">
            <el-empty description="暂无课程信息" />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useCoachStore } from '@/stores/coach'
import type { CoachDetail, CoachScheduleList, CoachCourseList } from '@/types/coach'

const router = useRouter()
const route = useRoute()
const coachStore = useCoachStore()

const loading = ref(false)
const activeTab = ref('schedules')

const coachId = computed(() => Number(route.params.id))
const coachDetail = computed(() => coachStore.currentCoach)

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

const getRandomColor = () => {
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
  return colors[Math.floor(Math.random() * colors.length)]
}

const getScheduleStatusType = (status: string) => {
  switch (status) {
    case '空闲': return 'success'
    case '已预约': return 'warning'
    case '已取消': return 'danger'
    default: return 'info'
  }
}

const getCourseStatusType = (status: number) => {
  switch (status) {
    case 0: return 'info'      // 待开始
    case 1: return 'success'   // 进行中
    case 2: return 'success'   // 已完成
    case 3: return 'danger'    // 已取消
    default: return 'info'
  }
}

const getCourseStatusText = (status: number) => {
  switch (status) {
    case 0: return '待开始'
    case 1: return '进行中'
    case 2: return '已完成'
    case 3: return '已取消'
    default: return '未知'
  }
}

const getEnrollmentStatus = (rate: number) => {
  if (rate >= 90) return 'success'
  if (rate >= 70) return 'warning'
  return 'exception'
}

// 加载教练详情
const loadCoachDetail = async () => {
  try {
    loading.value = true
    await coachStore.fetchCoachDetail(coachId.value)
  } catch (error) {
    console.error('加载教练详情失败:', error)
    ElMessage.error('加载教练详情失败')
  } finally {
    loading.value = false
  }
}

// 排班操作
const handleAddSchedule = () => {
  router.push(`/coach/schedule/add/${coachId.value}`)
}

const handleEditSchedule = (schedule: CoachScheduleList) => {
  router.push(`/coach/schedule/edit/${schedule.id}`)
}

const handleDeleteSchedule = async (schedule: CoachScheduleList) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${schedule.scheduleDate} ${schedule.startTime?.slice(0,5)} 的排班吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 这里调用API删除排班
    await coachStore.deleteCoachSchedule(schedule.id)
    ElMessage.success('删除成功')
    // 重新加载数据
    await loadCoachDetail()
  } catch (error) {
    // 用户取消
  }
}

// 导航
const goBack = () => {
  router.push('/coach/list')
}

// const goEdit = () => {
//   router.push(`/coach/edit/${coachId.value}`)
// }

// const handleViewSchedule = () => {
//   router.push(`/coach/schedule/${coachId.value}`)
// }

onMounted(() => {
  loadCoachDetail()
})
</script>

<style scoped>
.coach-detail-container {
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

.coach-section {
  text-align: center;
  flex-shrink: 0;
}

.coach-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.coach-rating {
  margin-top: 5px;
}

.info-details {
  flex: 1;
}

.certification-section,
.introduction-section {
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

.certification-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.certification-tag {
  font-size: 14px;
  padding: 6px 12px;
}

.no-certification {
  color: #909399;
  font-style: italic;
  padding: 20px 0;
  text-align: center;
}

.introduction-content {
  line-height: 1.6;
  color: #606266;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
  min-height: 80px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 20px 0;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.stat-value.amount {
  color: #67C23A;
}

.stat-unit {
  font-size: 12px;
  color: #909399;
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
  color: #67C23A;
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

:deep(.el-rate) {
  --el-rate-font-size: 16px;
}
</style>