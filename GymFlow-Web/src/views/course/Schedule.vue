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
        <el-button type="primary" @click="handleAddSchedule" v-if="isGroupCourse">
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
              <User />
            </el-icon>
            最大容量：{{ maxCapacity }}人
          </span>
          <span class="meta-item">
            <el-icon>
              <Clock />
            </el-icon>
            时长：{{ duration }}分钟
          </span>
          <span class="meta-item">
            <el-icon>
              <Location />
            </el-icon>
            地点：{{ location }}
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
            <el-table-column prop="maxParticipants" label="最大人数" width="100" align="center" />
            <el-table-column prop="currentParticipants" label="已报人数" width="100" align="center" />
            <el-table-column prop="remainingSlots" label="剩余名额" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getRemainingSlotsType(row.remainingSlots)" size="small">
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
            <el-table-column label="操作" width="200" fixed="right" align="center">
              <template #default="{ row }">
                <el-button type="text" size="small" @click="handleViewScheduleDetail(row)">
                  详情
                </el-button>
                <el-button type="text" size="small" @click="handleEditSchedule(row)">
                  编辑
                </el-button>
                <el-button type="text" size="small" style="color: #f56c6c;" @click="handleDeleteSchedule(row)">
                  删除
                </el-button>
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

    <!-- 排课详情弹窗 -->
    <el-dialog v-model="scheduleDialog.visible" :title="scheduleDialog.title" width="600px" :before-close="handleDialogClose">
      <el-form ref="scheduleFormRef" :model="scheduleForm" :rules="scheduleRules" label-width="100px" v-loading="scheduleDialog.loading">
        <el-form-item label="课程日期" prop="courseDate">
          <el-date-picker v-model="scheduleForm.courseDate" type="date" placeholder="选择课程日期" value-format="YYYY-MM-DD" style="width: 100%" :disabled-date="disabledDate" />
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

        <el-form-item label="最多人数" prop="maxParticipants">
          <el-input-number v-model="scheduleForm.maxParticipants" :min="1" :max="maxCapacity" :step="1" controls-position="right" style="width: 100%" placeholder="请输入最多排课人数" />
        </el-form-item>

        <el-form-item label="备注" prop="notes">
          <el-input v-model="scheduleForm.notes" type="textarea" :rows="3" placeholder="请输入排课备注" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose" :disabled="scheduleDialog.loading">取消</el-button>
          <template v-if="scheduleDialog.mode === 'edit'">
            <el-button type="danger" @click="handleDeleteScheduleConfirm" :loading="scheduleDialog.loading">
              删除
            </el-button>
          </template>
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
          <el-descriptions-item label="课程类型">{{ selectedSchedule.courseType }}</el-descriptions-item>
          <el-descriptions-item label="教练">{{ selectedSchedule.coachName }}</el-descriptions-item>
          <el-descriptions-item label="上课地点">{{ location }}</el-descriptions-item>
          <el-descriptions-item label="上课日期">{{ formatDate(selectedSchedule.scheduleDate) }}</el-descriptions-item>
          <el-descriptions-item label="上课时间">
            {{ selectedSchedule.startTime?.slice(0, 5) }} - {{ selectedSchedule.endTime?.slice(0, 5) }}
          </el-descriptions-item>
          <el-descriptions-item label="最大人数">{{ selectedSchedule.maxParticipants }}</el-descriptions-item>
          <el-descriptions-item label="当前报名">{{ selectedSchedule.currentParticipants }}</el-descriptions-item>
          <el-descriptions-item label="剩余名额">{{ selectedSchedule.remainingSlots }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ selectedSchedule.status }}</el-descriptions-item>
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
import { coachApi } from '@/api/coach'

const router = useRouter()
const route = useRoute()
const courseStore = useCourseStore()

const loading = ref(false)
const courseId = computed(() => Number(route.params.id))

// 课程信息
const courseName = ref('')
const courseType = ref(1)
const maxCapacity = ref(20)
const duration = ref(60)
const location = ref('')
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
  courseDate: '',
  startTime: '',
  endTime: '',
  maxParticipants: 20,
  notes: '',
})

const scheduleRules: FormRules = {
  courseDate: [{ required: true, message: '请选择课程日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  coachId: [{ required: true, message: '请选择教练', trigger: 'change' }],
  maxParticipants: [
    { required: true, message: '请输入最多人数', trigger: 'blur' },
    {
      type: 'number',
      min: 1,
      max: maxCapacity.value,
      message: `最多人数在1-${maxCapacity.value}之间`,
      trigger: 'blur',
    },
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

const getScheduleStatusType = (status: string) => {
  if (status === '正常') return 'success'
  if (status === '禁用') return 'danger'
  return 'info'
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

// 加载数据
const loadData = async () => {
  try {
    loading.value = true

    // 加载课程详情
    const courseResponse = await courseStore.fetchCourseDetail(courseId.value)
    if (courseResponse) {
      courseName.value = courseResponse.courseName
      courseType.value = courseResponse.courseType
      maxCapacity.value = courseResponse.maxCapacity
      duration.value = courseResponse.duration
      location.value = courseResponse.location || ''
      // 加载教练列表
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
    const response = await courseStore.fetchCourseSchedules(courseId.value)
    if (response) {
      schedules.value = response
    }
  } catch (error) {
    console.error('加载排课列表失败:', error)
    throw error
  }
}

// 加载教练列表
// const loadCoachOptions = async () => {

//   const response = await coachApi.getCoachList({})
//   try {
//     if (response.code === 200) {
//       coachOptions.value = response.data.list.map(item => ({
//         id: item.id,
//         realName: item.realName
//       }));
//     }
//   } catch (error) {
//     console.error('加载教练列表失败:', error)
//   }
// }

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
    courseDate: '',
    startTime: '09:00',
    endTime: '10:00',
    maxParticipants: maxCapacity.value,
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
    courseDate: schedule.scheduleDate,
    startTime: schedule.startTime.slice(0, 5),
    endTime: schedule.endTime.slice(0, 5),
    maxParticipants: schedule.maxParticipants,
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
    await ElMessageBox.confirm(
      `确定要删除 ${schedule.scheduleDate} ${schedule.startTime.slice(0, 5)} 的排课吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    // 这里应该调用API删除排课
    // 为了演示，这里从列表中移除
    schedules.value = schedules.value.filter((s) => s.scheduleId !== schedule.scheduleId)
    ElMessage.success('删除成功')
  } catch (error) {
    // 用户取消
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
      courseDate: scheduleForm.courseDate,
      startTime: scheduleForm.startTime + ':00',
      endTime: scheduleForm.endTime + ':00',
      maxParticipants: scheduleForm.maxParticipants,
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

// 确认删除排课
const handleDeleteScheduleConfirm = async () => {
  if (!scheduleDialog.editingScheduleId) return

  try {
    await ElMessageBox.confirm('确定要删除这个排课吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    scheduleDialog.loading = true
    // 这里应该调用API删除排课
    schedules.value = schedules.value.filter(
      (s) => s.scheduleId !== scheduleDialog.editingScheduleId
    )
    ElMessage.success('删除排课成功')
    scheduleDialog.visible = false
    await loadSchedules()
  } catch (error) {
    console.error('删除排课失败:', error)
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

.empty-actions {
  margin-top: 20px;
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