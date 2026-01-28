<template>
  <div class="course-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">课程管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>课程管理</el-breadcrumb-item>
          <el-breadcrumb-item>课程列表</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAddCourse">
          <el-icon><Plus /></el-icon>
          新增课程
        </el-button>
        <el-button @click="handleBatchCreate">
          <el-icon><DocumentAdd /></el-icon>
          批量排课
        </el-button>
      </div>
    </div>
    
    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline label-width="80px">
        <el-form-item label="课程名称">
          <el-input
            v-model="filterForm.name"
            placeholder="请输入课程名称"
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="课程分类">
          <el-select
            v-model="filterForm.category"
            placeholder="请选择分类"
            clearable
            style="width: 180px;"
          >
            <el-option
              v-for="item in courseCategoryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="教练">
          <el-select
            v-model="filterForm.coachId"
            placeholder="请选择教练"
            clearable
            filterable
            style="width: 180px;"
          >
            <el-option
              v-for="coach in coaches"
              :key="coach.id"
              :label="coach.realName"
              :value="coach.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程状态">
          <el-select
            v-model="filterForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 180px;"
          >
            <el-option
              v-for="item in courseStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="table-title">课程列表</span>
          <div class="table-actions">
            <el-button text @click="refreshTable">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button text @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>
      
      <app-table
        :data="courseList"
        :total="pagination.total"
        :show-add="false"
        :show-export="false"
        :page="pagination.current"
        :page-size="pagination.size"
        :show-selection="false"
        @add="handleAddCourse"
        @refresh="refreshTable"
        @export="handleExport"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="courseNo" label="课程编号" width="120" sortable />
        <el-table-column prop="name" label="课程名称" width="180" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">
            <el-tag :type="getCategoryType(row.category)" size="small">
              {{ getCategoryText(row.category) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="100">
          <template #default="{ row }">
            <el-tag :type="getDifficultyType(row.difficulty)" size="small">
              {{ getDifficultyText(row.difficulty) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="coachName" label="教练" width="100" />
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column prop="startTime" label="时间" width="180">
          <template #default="{ row }">
            <div class="time-cell">
              <div>{{ formatDate(row.startTime) }}</div>
              <div class="time-range">{{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="80" align="center">
          <template #default="{ row }">
            {{ row.duration }}分钟
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
        <el-table-column prop="price" label="价格" width="100" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <template #actions="{ row }">
          <el-button type="text" size="small" @click="handleViewDetail(row.id)">
            <el-icon><View /></el-icon>
            详情
          </el-button>
          <el-button type="text" size="small" @click="handleEdit(row.id)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-dropdown @command="handleMoreAction($event, row)" trigger="click">
            <el-button type="text" size="small">
              <el-icon><MoreFilled /></el-icon>
              更多
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="bookings">
                  <el-icon><List /></el-icon>
                  预约列表
                </el-dropdown-item>
                <el-dropdown-item command="checkin">
                  <el-icon><Check /></el-icon>
                  批量签到
                </el-dropdown-item>
                <el-dropdown-item command="cancel" v-if="row.status === 'SCHEDULED'">
                  <el-icon><Close /></el-icon>
                  取消课程
                </el-dropdown-item>
                <el-dropdown-divider />
                <el-dropdown-item command="delete" class="delete-item">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </app-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import AppTable from '@/components/common/AppTable.vue'
import { useCourseStore } from '@/stores/course'
import { useCoachStore } from '@/stores/coach'
import { courseCategoryOptions, CourseDifficulty, CourseStatus } from '@/utils/constants'
import { formatDate, formatTime } from '@/utils'
import type { Course, QueryParams } from '@/types'

// Store
const courseStore = useCourseStore()
const coachStore = useCoachStore()

// Router
const router = useRouter()

// 状态
const loading = ref(false)

const filterForm = reactive({
  name: '',
  category: '',
  coachId: undefined as number | undefined,
  status: '' as CourseStatus | '',
  dateRange: [] as string[]
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const sortParams = reactive({
  sortBy: '',
  sortOrder: '' as 'asc' | 'desc' | ''
})

const courseStatusOptions = [
  { label: '已安排', value: 'SCHEDULED' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

// Computed
const courseList = computed(() => courseStore.courses)
const coaches = computed(() => coachStore.coaches)

// Methods
const loadCourses = async () => {
  try {
    loading.value = true
    
    const params: QueryParams = {
      page: pagination.current,
      pageSize: pagination.size,
      ...buildFilterParams()
    }
    
    if (sortParams.sortBy) {
      params.sortBy = sortParams.sortBy
      params.sortOrder = sortParams.sortOrder
    }
    
    await courseStore.fetchCourses(params)
    pagination.total = courseStore.total
  } catch (error) {
    console.error('加载课程列表失败:', error)
    ElMessage.error('加载课程列表失败')
  } finally {
    loading.value = false
  }
}

const loadCoaches = async () => {
  try {
    await coachStore.fetchCoaches({ pageSize: 100 })
  } catch (error) {
    console.error('加载教练列表失败:', error)
  }
}

const buildFilterParams = () => {
  const params: Record<string, any> = {}
  
  if (filterForm.name) {
    params.name = filterForm.name
  }
  
  if (filterForm.category) {
    params.category = filterForm.category
  }
  
  if (filterForm.coachId) {
    params.coachId = filterForm.coachId
  }
  
  if (filterForm.status) {
    params.status = filterForm.status
  }
  
  if (filterForm.dateRange?.length === 2) {
    params.startDate = filterForm.dateRange[0]
    params.endDate = filterForm.dateRange[1]
  }
  
  return params
}

const handleSearch = () => {
  pagination.current = 1
  loadCourses()
}

const handleReset = () => {
  filterForm.name = ''
  filterForm.category = ''
  filterForm.coachId = undefined
  filterForm.status = ''
  filterForm.dateRange = []
  
  pagination.current = 1
  loadCourses()
}

const handleSortChange = ({ prop, order }: { prop: string; order: 'ascending' | 'descending' | null }) => {
  sortParams.sortBy = prop
  sortParams.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  loadCourses()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadCourses()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadCourses()
}

const refreshTable = () => {
  loadCourses()
}

const handleAddCourse = () => {
  router.push('/courses/create')
}

const handleBatchCreate = () => {
  router.push('/courses/schedule')
}

const handleEdit = (id: number) => {
  router.push(`/courses/${id}/edit`)
}

const handleViewDetail = (id: number) => {
  router.push(`/courses/${id}`)
}

const handleMoreAction = async (command: string, row: Course) => {
  switch (command) {
    case 'bookings':
      handleViewBookings(row.id)
      break
    case 'checkin':
      handleCheckin(row.id)
      break
    case 'cancel':
      handleCancelCourse(row.id)
      break
    case 'delete':
      handleDelete(row.id)
      break
  }
}

const handleViewBookings = (courseId: number) => {
  router.push(`/bookings?courseId=${courseId}`)
}

const handleCheckin = (courseId: number) => {
  router.push(`/checkin?courseId=${courseId}`)
}

const handleCancelCourse = async (courseId: number) => {
  try {
    await ElMessageBox.confirm('确认取消该课程吗？已预约的会员将收到通知。', '取消课程', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用取消课程API
    await courseStore.updateCourse(courseId, { status: 'CANCELLED' })
    ElMessage.success('课程已取消')
    loadCourses()
  } catch (error) {
    console.error('取消课程失败:', error)
  }
}

const handleDelete = async (courseId: number) => {
  try {
    await ElMessageBox.confirm('确认删除该课程吗？此操作不可恢复。', '删除课程', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    
    await courseStore.deleteCourse(courseId)
    ElMessage.success('删除成功')
    loadCourses()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

const handleExport = async () => {
  try {
    loading.value = true
    const params = buildFilterParams()
    await courseStore.exportCourses(params)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
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

const getDifficultyType = (difficulty: CourseDifficulty) => {
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

const getDifficultyText = (difficulty: CourseDifficulty) => {
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
  loadCourses()
  loadCoaches()
})
</script>

<style lang="scss" scoped>
.course-list-container {
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
  
  .filter-card {
    margin-bottom: 20px;
    border-radius: 12px;
    
    :deep(.el-card__body) {
      padding: 20px;
    }
    
    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }
  
  .table-card {
    border-radius: 12px;
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid var(--gymflow-border);
    }
    
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .table-title {
        font-size: 16px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
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
      
      :deep(.el-progress) {
        .el-progress-bar {
          padding-right: 0;
        }
      }
    }
    
    .price {
      font-weight: 600;
      color: var(--gymflow-primary);
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
  .course-list-container {
    padding: 16px;
    
    .page-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
    }
    
    .filter-card {
      :deep(.el-form) {
        display: flex;
        flex-direction: column;
        gap: 16px;
      }
      
      :deep(.el-form-item) {
        width: 100%;
        margin-right: 0;
        
        .el-input,
        .el-select,
        .el-date-editor {
          width: 100%;
        }
      }
    }
  }
}
</style>