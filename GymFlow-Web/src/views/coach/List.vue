<template>
  <div class="coach-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">教练管理</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAddCoach">
          <el-icon><Plus /></el-icon>
          新增教练
        </el-button>
      </div>
    </div>
    
    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="姓名">
          <el-input
            v-model="filterForm.realName"
            placeholder="请输入教练姓名"
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="filterForm.phone"
            placeholder="请输入手机号"
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="专长">
          <el-input
            v-model="filterForm.specialty"
            placeholder="请输入专长"
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filterForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 180px;"
          >
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="0" />
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
          <span class="table-title">教练列表</span>
          <div class="table-actions">
            <el-button text @click="refreshTable" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="coachStore.coachList"
        style="width: 100%"
        row-key="id"
        v-loading="coachStore.loading"
        stripe
        border
      >
        <el-table-column prop="realName" label="姓名" width="100">
          <template #default="{ row }">
            <div class="coach-info">
              <span class="coach-name">{{ row.realName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="specialty" label="专长" width="120">
          <template #default="{ row }">
            <span>{{ row.specialty || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="yearsOfExperience" label="经验年限" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.yearsOfExperience" type="info" size="small">
              {{ row.yearsOfExperience }}年
            </el-tag>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="hourlyRate" label="时薪" width="100" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ formatAmount(row.hourlyRate) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusDesc" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.statusDesc || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalStudents" label="学员数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">
              {{ row.totalStudents || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalCourses" label="课程数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="warning" size="small">
              {{ row.totalCourses || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalIncome" label="总收入" width="120" align="center">
          <template #default="{ row }">
            <span class="amount">¥{{ formatAmount(row.totalIncome) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="180" align="center">
          <template #default="{ row }">
            <el-rate
              v-model="row.rating"
              disabled
              show-score
              text-color="#ff9900"
              score-template="{value}"
              :max="5"
              :allow-half="true"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="入职时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row.id)">
              详情
            </el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row.id)">
              编辑
            </el-button>
            <el-button type="info" link size="small" @click="handleViewSchedule(row.id)">
              排班
            </el-button>
            <el-popconfirm
              title="确定要删除这个教练吗？"
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
          v-model:current-page="coachStore.pageNum"
          v-model:page-size="coachStore.pageSize"
          :total="coachStore.total"
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
import { Plus, Search, Refresh, View, Edit, Delete, Calendar } from '@element-plus/icons-vue'
import { useCoachStore } from '@/stores/coach'
import type { CoachQueryParams } from '@/types/coach'

const router = useRouter()
const coachStore = useCoachStore()

// 筛选表单
const filterForm = reactive({
  realName: '',
  phone: '',
  specialty: '',
  status: undefined as number | undefined
})

const loading = computed(() => coachStore.loading)

// 方法
const formatDateTime = (datetime: string) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ')
}

const formatAmount = (amount: number) => {
  if (!amount) return '0.00'
  return amount.toFixed(2)
}

const getRandomColor = () => {
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
  return colors[Math.floor(Math.random() * colors.length)]
}

// 搜索处理
const handleSearch = async () => {
  const queryParams: CoachQueryParams = {
    pageNum: coachStore.pageNum,
    pageSize: coachStore.pageSize,
    realName: filterForm.realName,
    phone: filterForm.phone,
    specialty: filterForm.specialty,
    status: filterForm.status
  }
  
  await coachStore.fetchCoachList(queryParams)
}

const handleReset = () => {
  filterForm.realName = ''
  filterForm.phone = ''
  filterForm.specialty = ''
  filterForm.status = undefined
  coachStore.pageNum = 1
  coachStore.pageSize = 10
  handleSearch()
}

// 分页处理
const handleSizeChange = (size: number) => {
  coachStore.pageSize = size
  coachStore.pageNum = 1
  handleSearch()
}

const handleCurrentChange = (current: number) => {
  coachStore.pageNum = current
  handleSearch()
}

// 刷新表格
const refreshTable = async () => {
  await handleSearch()
  ElMessage.success('刷新成功')
}

// 导航操作
const handleAddCoach = () => {
  router.push('/coach/add')
}

const handleViewDetail = (id: number) => {
  router.push(`/coach/detail/${id}`)
}

const handleEdit = (id: number) => {
  router.push(`/coach/edit/${id}`)
}

const handleViewSchedule = (id: number) => {
  router.push(`/coach/schedule/${id}`)
}

// 删除教练
const handleDelete = async (id: number) => {
  try {
    await coachStore.deleteCoach(id)
    ElMessage.success('删除成功')
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 初始化加载
onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.coach-list-container {
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

.coach-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.coach-name {
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

:deep(.el-rate) {
  --el-rate-font-size: 12px;
}
</style>