<template>
  <div class="course-schedule-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/course/list' }">课程管理</el-breadcrumb-item>
          <el-breadcrumb-item>课程排课</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">{{ courseName }} - 课程排课</h1>
      </div>
      <div class="header-actions">
        <el-button @click="goBack">返回</el-button>
        <el-button v-permission="'course:schedule:set'" type="primary" @click="handleAddSchedule" v-if="isGroupCourse">
          <el-icon>
            <Plus />
          </el-icon>
          添加排课
        </el-button>
      </div>
    </div>

    <!-- 课程基本信息 -->
    <el-card class="course-info-card" v-loading="loading">
      <div class="course-info">
        <div class="course-name">{{ courseName }}</div>
        <div class="course-meta">
          <el-tag :type="courseType === 0 ? 'primary' : 'success'" size="large">
            {{ courseType === 0 ? '私教课' : '团课' }}
          </el-tag>
          <span class="meta-item">
            <el-icon>
              <Clock />
            </el-icon>
            时长：{{ duration }}分钟
          </span>
          <span class="meta-item">
            <el-icon>
              <PriceTag />
            </el-icon>
            价格：¥{{ price }}
          </span>
        </div>
      </div>
    </el-card>

    <!-- 排课列表 -->
    <el-card class="schedule-list-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">排课列表</span>
          <div class="header-actions" v-if="isGroupCourse">
            <el-date-picker v-model="dateFilter" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px; margin-right: 10px;" @change="handleDateFilterChange" />
            <el-button @click="refreshData">
              <el-icon>
                <Refresh />
              </el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="isGroupCourse">
        <div v-if="schedules.length > 0">
          <el-table :data="schedules" style="width: 100%" stripe border>
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
            <el-table-column prop="coachName" label="教练" width="120" />
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
            <el-table-column label="操作" width="200" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="text" size="small" @click="handleViewScheduleDetail(row)">
                  详情
                </el-button>
                <el-button type="text" size="small" @click="handleEditSchedule(row)">
                  编辑
                </el-button>
                <el-popconfirm title="确定要删除这个排课吗？" @confirm="handleDeleteSchedule(row)" confirm-button-text="确定" cancel-button-text="取消">
                  <template #reference>
                    <el-button type="text" size="small" style="color: #f56c6c;">
                      删除
                    </el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div v-else class="empty-data">
          <el-empty description="暂无排课信息" />
        </div>
      </div>
      <div v-else class="private-course-notice">
        <el-alert title="私教课无需排课" type="info" description="私教课由会员直接预约教练的时间，系统会自动创建课程实例。" show-icon :closable="false" />
      </div>
    </el-card>

    <!-- 排课表单弹窗 -->
    <el-dialog v-model="scheduleDialog.visible" :title="scheduleDialog.title" width="600px" :before-close="handleDialogClose">
      <el-form ref="scheduleFormRef" :model="scheduleForm" :rules="scheduleRules" label-width="100px" v-loading="scheduleDialog.loading">
        <el-form-item label="课程日期" prop="scheduleDate">
          <el-date-picker v-model="scheduleForm.scheduleDate" type="date" placeholder="选择课程日期" value-format="YYYY-MM-DD" style="width: 100%" :disabled-date="disabledDate" />
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-time-select v-model="scheduleForm.startTime" :max-time="scheduleForm.endTime" placeholder="选择开始时间" start="06:00" step="00:30" end="22:00" style="width: 100%" />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-time-select v-model="scheduleForm.endTime" :min-time="scheduleForm.startTime" placeholder="选择结束时间" start="06:00" step="00:30" end="22:00" style="width: 100%" />
        </el-form-item>

        <el-form-item label="教练" prop="coachId">
          <el-select v-model="scheduleForm.coachId" placeholder="请选择教练" filterable style="width: 100%">
            <el-option v-for="coach in coachOptions" :key="coach.id" :label="coach.realName" :value="coach.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="最大人数" prop="maxCapacity">
          <el-input-number v-model="scheduleForm.maxCapacity" :min="1" :max="100" :step="1" controls-position="right" style="width: 100%" placeholder="请输入最大人数" />
        </el-form-item>

        <el-form-item label="备注" prop="notes">
          <el-input v-model="scheduleForm.notes" type="textarea" :rows="3" placeholder="请输入排课备注" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose" :disabled="scheduleDialog.loading">取消</el-button>
          <el-button type="primary" @click="handleSaveSchedule" :loading="scheduleDialog.loading">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 排课详情弹窗 -->
    <el-dialog v-model="detailDialog.visible" title="排课详情" width="800px">
      <div v-if="selectedSchedule" class="schedule-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课程名称">{{ selectedSchedule.courseName }}</el-descriptions-item>
          <el-descriptions-item label="课程类型">{{ selectedSchedule.courseTypeDesc }}</el-descriptions-item>
          <el-descriptions-item label="教练">{{ selectedSchedule.coachName }}</el-descriptions-item>
          <el-descriptions-item label="上课日期">{{ formatDate(selectedSchedule.scheduleDate) }}</el-descriptions-item>
          <el-descriptions-item label="上课时间">
            {{ selectedSchedule.startTime?.slice(0, 5) }} - {{ selectedSchedule.endTime?.slice(0, 5) }}
          </el-descriptions-item>
          <el-descriptions-item label="最大人数">{{ selectedSchedule.maxCapacity }}</el-descriptions-item>
          <el-descriptions-item label="当前报名">{{ selectedSchedule.currentEnrollment }}</el-descriptions-item>
          <el-descriptions-item label="剩余名额">{{ selectedSchedule.remainingSlots }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ selectedSchedule.statusDesc }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ selectedSchedule.notes || '无' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 预约列表 -->
        <div class="booking-section" v-if="selectedSchedule.bookings && selectedSchedule.bookings.length > 0">
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
        <div v-else class="no-booking">
          <el-empty description="暂无预约" :image-size="60" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useCourseStore } from '@/stores/course'
import type { CourseScheduleDTO, CourseScheduleVO } from '@/types/course'

const router = useRouter()
const route = useRoute()
const courseStore = useCourseStore()

const loading = ref(false)
const courseId = computed(() => Number(route.params.id))

// 课程信息
const courseName = ref('')
const courseType = ref(1)
const duration = ref(60)
const price = ref(0)
const isGroupCourse = computed(() => courseType.value === 1)

// 排课数据
const schedules = ref<CourseScheduleVO[]>([])
const dateFilter = ref<string[]>([])

// 教练选项
const coachOptions = ref<any[]>([])

// 弹窗相关
const scheduleFormRef = ref<FormInstance>()
const scheduleDialog = reactive({
  visible: false,
  mode: 'add', // 'add' | 'edit'
  title: '',
  loading: false,
  editingScheduleId: null as number | null,
})

const scheduleForm = reactive({
  courseId: 0,
  coachId: undefined as number | undefined,
  scheduleDate: '',
  startTime: '',
  endTime: '',
  maxCapacity: 20,
  notes: '',
})

const scheduleRules: FormRules = {
  scheduleDate: [{ required: true, message: '请选择课程日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  coachId: [{ required: true, message: '请选择教练', trigger: 'change' }],
  maxCapacity: [
    { required: true, message: '请输入最大人数', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: '最大人数在1-100之间', trigger: 'blur' },
  ],
}

// 详情弹窗
const detailDialog = ref({
  visible: false,
})
const selectedSchedule = ref<CourseScheduleVO | null>(null)

// 格式化函数
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

const formatDateTime = (datetime: string) => {
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

// 加载数据
const loadData = async () => {
  try {
    loading.value = true

    // 加载课程详情
    const courseResponse = await courseStore.fetchCourseDetail(courseId.value)
    if (courseResponse) {
      courseName.value = courseResponse.courseName
      courseType.value = courseResponse.courseType
      duration.value = courseResponse.duration
      price.value = courseResponse.price

      // 加载教练列表（从课程绑定的教练中获取）
      coachOptions.value = courseResponse.coaches.map((item) => ({
        id: item.id,
        realName: item.realName,
      }))
    }

    // 加载排课列表
    if (isGroupCourse.value) {
      await loadSchedules()
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载排课列表
const loadSchedules = async () => {
  try {
    const schedulesData = await courseStore.fetchCourseSchedules(courseId.value)
    if (schedulesData) {
      schedules.value = schedulesData
    }
  } catch (error) {
    console.error('加载排课列表失败:', error)
    throw error
  }
}

// 日期过滤
const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 8.64e7 // 禁用今天之前的日期
}

const handleDateFilterChange = () => {
  // 这里可以实现日期过滤逻辑
  loadSchedules()
}

// 刷新数据
const refreshData = async () => {
  await loadData()
  ElMessage.success('刷新成功')
}

// 添加排课
const handleAddSchedule = () => {
  if (!isGroupCourse.value) {
    ElMessage.warning('只有团课可以排课')
    return
  }

  scheduleDialog.mode = 'add'
  scheduleDialog.title = '添加排课'
  scheduleDialog.editingScheduleId = null
  scheduleDialog.visible = true

  // 重置表单
  Object.assign(scheduleForm, {
    courseId: courseId.value,
    coachId: undefined,
    scheduleDate: '',
    startTime: '09:00',
    endTime: '10:00',
    maxCapacity: 20,
    notes: '',
  })
}

// 编辑排课
const handleEditSchedule = (schedule: CourseScheduleVO) => {
  scheduleDialog.mode = 'edit'
  scheduleDialog.title = '编辑排课'
  scheduleDialog.editingScheduleId = schedule.scheduleId
  scheduleDialog.visible = true

  Object.assign(scheduleForm, {
    courseId: courseId.value,
    coachId: schedule.coachId,
    scheduleDate: schedule.scheduleDate,
    startTime: schedule.startTime.slice(0, 5),
    endTime: schedule.endTime.slice(0, 5),
    maxCapacity: schedule.maxCapacity,
    notes: schedule.notes || '',
  })
}

// 查看排课详情
const handleViewScheduleDetail = (schedule: CourseScheduleVO) => {
  selectedSchedule.value = schedule
  detailDialog.value.visible = true
}

// 删除排课
const handleDeleteSchedule = async (schedule: CourseScheduleVO) => {
  try {
    scheduleDialog.loading = true
    // 这里需要调用API删除排课
    // 暂时从列表中移除
    schedules.value = schedules.value.filter((s) => s.scheduleId !== schedule.scheduleId)
    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除排课失败:', error)
    ElMessage.error('删除失败')
  } finally {
    scheduleDialog.loading = false
  }
}

// 保存排课
const handleSaveSchedule = async () => {
  if (!scheduleFormRef.value) return

  try {
    await scheduleFormRef.value.validate()

    scheduleDialog.loading = true

    const scheduleData: CourseScheduleDTO = {
      courseId: scheduleForm.courseId,
      coachId: scheduleForm.coachId!,
      scheduleDate: scheduleForm.scheduleDate,
      startTime: scheduleForm.startTime + ':00',
      endTime: scheduleForm.endTime + ':00',
      maxCapacity: scheduleForm.maxCapacity,
      notes: scheduleForm.notes,
    }

    if (scheduleDialog.mode === 'add') {
      await courseStore.scheduleCourse(scheduleData)
      ElMessage.success('添加排课成功')
    } else if (scheduleDialog.editingScheduleId) {
      // 更新排课（需要实现updateCourseSchedule接口）
      ElMessage.warning('更新排课功能待实现')
    }
    scheduleDialog.visible = false
    await loadSchedules()
  } catch (error) {
    console.error('保存排课失败:', error)
    ElMessage.error('保存排课失败')
  } finally {
    scheduleDialog.loading = false
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  scheduleDialog.visible = false
  scheduleFormRef.value?.clearValidate()
}

// 导航
const goBack = () => {
  router.push('/course/list')
}

onMounted(() => {
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

.course-info-card {
  margin-bottom: 20px;
}

.course-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.course-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.course-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
}

.meta-item .el-icon {
  color: #409eff;
}

.schedule-list-card {
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

.header-actions {
  display: flex;
  align-items: center;
}

.empty-data {
  padding: 40px 0;
  text-align: center;
}

.private-course-notice {
  padding: 20px;
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

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.no-booking {
  padding: 40px 0;
  text-align: center;
}

:deep(.el-card__header) {
  padding: 16px 20px;
}

:deep(.el-table__header) {
  background-color: #f8f9fa;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

:deep(.el-alert) {
  margin-bottom: 0;
}

:deep(.el-descriptions__body) {
  background-color: white;
}

:deep(.el-descriptions__cell) {
  padding: 12px 16px;
}
</style>