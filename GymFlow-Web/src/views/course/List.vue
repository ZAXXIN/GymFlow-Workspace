<template>
  <div class="course-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">课程管理</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAddCourse">
          <el-icon><Plus /></el-icon>
          新增课程
        </el-button>
      </div>
    </div>
    
    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="课程名称">
          <el-input
            v-model="filterForm.courseName"
            placeholder="请输入课程名称"
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select
            v-model="filterForm.courseType"
            placeholder="请选择课程类型"
            clearable
            style="width: 120px;"
          >
            <el-option label="私教课" :value="0" />
            <el-option label="团课" :value="1" />
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
              v-for="coach in coachOptions"
              :key="coach.id"
              :label="coach.realName"
              :value="coach.id"
            />
          </el-select>
        </el-form-item>
        <!-- <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px;"
          />
        </el-form-item> -->
        <el-form-item label="状态">
          <el-select
            v-model="filterForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 100px;"
          >
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset" :disabled="loading">
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
            <el-button text @click="refreshTable" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="courseStore.formattedCourseList()"
        style="width: 100%"
        row-key="id"
        v-loading="courseStore.loading"
        stripe
        border
      >
        <el-table-column prop="courseName" label="课程名称" min-width="150">
          <template #default="{ row }">
            <div class="course-info">
              <span class="course-name">{{ row.courseName }}</span>
              <el-tag 
                :type="row.courseType === 0 ? 'primary' : 'success'" 
                size="small"
                effect="plain"
              >
                {{ row.courseTypeDesc }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="coachNames" label="教练" width="120">
          <template #default="{ row }">
            <span v-if="row.coachNames && row.coachNames.length > 0">
              {{ row.coachNames.join(', ') }}
            </span>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="maxCapacity" label="容量" width="100" align="center">
          <template #default="{ row }">
            {{ row.currentEnrollment }}/{{ row.maxCapacity }}
          </template>
        </el-table-column>
        <el-table-column prop="enrollmentRateFormatted" label="满员率" width="120" align="center">
          <template #default="{ row }">
            <el-progress 
              :percentage="parseFloat(row.enrollmentRate) || 0" 
              :stroke-width="16" 
              :status="getEnrollmentStatus(row.enrollmentRate)"
              :show-text="false"
            />
            <span style="font-size: 12px; margin-left: 5px;">{{ row.enrollmentRateFormatted }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="durationFormatted" label="时长" width="100" align="center" />
        <el-table-column prop="priceFormatted" label="价格" width="100" align="right">
          <template #default="{ row }">
            <span class="amount">{{ row.priceFormatted }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="120" />
        <el-table-column prop="statusDesc" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="courseDateFormatted" label="课程日期" width="120">
          <template #default="{ row }">
            {{ row.courseDateFormatted || '-' }}
          </template>
        </el-table-column>
        <!-- <el-table-column prop="createTimeFormatted" label="创建时间" width="160" /> -->
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row.id)">
              详情
            </el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row.id)">
              编辑
            </el-button>
            <el-button type="info" link size="small" @click="handleViewSchedule(row.id)">
              排课
            </el-button>
            <el-popconfirm
              title="确定要删除这个课程吗？"
              @confirm="handleDelete(row.id)"
              confirm-button-text="确定"
              cancel-button-text="取消"
            >
              <template #reference>
                <el-button type="danger" link size="small">
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="courseStore.pageInfo.pageNum"
          v-model:page-size="courseStore.pageInfo.pageSize"
          :total="courseStore.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :disabled="loading"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { useCourseStore } from '@/stores/course'
import type { CourseQueryParams } from '@/types/course'

const router = useRouter()
const courseStore = useCourseStore()

// 筛选表单
const filterForm = reactive({
  courseName: '',
  courseType: undefined as number | undefined,
  coachId: undefined as number | undefined,
  status: undefined as number | undefined
})

// const dateRange = ref<string[]>([])
const coachOptions = ref<any[]>([])
const loading = computed(() => courseStore.loading)

// 方法
const getEnrollmentStatus = (rate: number) => {
  if (rate >= 90) return 'success'
  if (rate >= 70) return 'warning'
  return 'exception'
}

// 搜索处理
const handleSearch = async () => {
  const queryParams: CourseQueryParams = {
    pageNum: courseStore.pageInfo.pageNum,
    pageSize: courseStore.pageInfo.pageSize,
    courseName: filterForm.courseName,
    courseType: filterForm.courseType,
    coachId: filterForm.coachId,
    status: filterForm.status
  }
  
  // if (dateRange.value && dateRange.value.length === 2) {
  //   queryParams.startDate = dateRange.value[0]
  //   queryParams.endDate = dateRange.value[1]
  // }
  
  await courseStore.fetchCourseList(queryParams)
}

const handleReset = () => {
  filterForm.courseName = ''
  filterForm.courseType = undefined
  filterForm.coachId = undefined
  filterForm.status = undefined
  // dateRange.value = []
  courseStore.pageInfo.pageNum = 1
  courseStore.pageInfo.pageSize = 10
  handleSearch()
}

// 分页处理
const handleSizeChange = (size: number) => {
  courseStore.pageInfo.pageSize = size
  courseStore.pageInfo.pageNum = 1
  handleSearch()
}

const handleCurrentChange = (current: number) => {
  courseStore.pageInfo.pageNum = current
  handleSearch()
}

// 刷新表格
const refreshTable = async () => {
  await handleSearch()
  ElMessage.success('刷新成功')
}

// 导航操作
const handleAddCourse = () => {
  router.push('/course/add')
}

const handleViewDetail = (id: number) => {
  router.push(`/course/detail/${id}`)
}

const handleEdit = (id: number) => {
  router.push(`/course/edit/${id}`)
}

const handleViewSchedule = (id: number) => {
  router.push(`/course/schedule/${id}`)
}

// 删除课程
const handleDelete = async (id: number) => {
  try {
    await courseStore.deleteCourse(id)
    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 加载教练列表（模拟）
const loadCoachOptions = async () => {
  try {
    // 这里应该调用教练API获取教练列表
    // 为了演示，这里使用模拟数据
    coachOptions.value = [
      { id: 1, realName: '张三' },
      { id: 2, realName: '李四' },
      { id: 3, realName: '王五' }
    ]
  } catch (error) {
    console.error('加载教练列表失败:', error)
  }
}

// 初始化加载
onMounted(() => {
  loadCoachOptions()
  handleSearch()
})
</script>

<style scoped>
.course-list-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.course-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.course-name {
  font-weight: 500;
}

.amount {
  font-weight: 600;
  color: #67C23A;
}

.no-data {
  color: #909399;
  font-style: italic;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
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
</style>