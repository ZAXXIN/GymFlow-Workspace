<template>
  <div class="checkin-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">签到记录</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>签到管理</el-breadcrumb-item>
          <el-breadcrumb-item>签到记录</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleBatchCheckin">
          <el-icon><Plus /></el-icon>
          批量签到
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>
    
    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline label-width="80px">
        <el-form-item label="会员姓名">
          <el-input
            v-model="filterForm.memberName"
            placeholder="请输入会员姓名"
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="签到类型">
          <el-select
            v-model="filterForm.type"
            placeholder="请选择类型"
            clearable
            style="width: 180px;"
          >
            <el-option
              v-for="item in checkinTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="签到状态">
          <el-select
            v-model="filterForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 180px;"
          >
            <el-option
              v-for="item in checkinStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="签到时间">
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
    
    <!-- 今日签到统计 -->
    <div class="today-stats">
      <el-card class="stats-card">
        <div class="stats-content">
          <div class="stats-icon today">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-number">{{ todayStats.todayCheckins || 0 }}</div>
            <div class="stats-label">今日签到</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stats-card">
        <div class="stats-content">
          <div class="stats-icon course">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-number">{{ todayStats.todayCourses || 0 }}</div>
            <div class="stats-label">今日课程</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stats-card">
        <div class="stats-content">
          <div class="stats-icon attendance">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-number">{{ todayStats.attendanceRate || 0 }}%</div>
            <div class="stats-label">出勤率</div>
          </div>
        </div>
      </el-card>
      
      <el-card class="stats-card">
        <div class="stats-content">
          <div class="stats-icon member">
            <el-icon><User /></el-icon>
          </div>
          <div class="stats-info">
            <div class="stats-number">{{ todayStats.activeMembers || 0 }}</div>
            <div class="stats-label">活跃会员</div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 签到记录表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="table-title">签到记录</span>
          <div class="table-actions">
            <el-button text @click="refreshTable">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <app-table
        :data="checkinList"
        :total="pagination.total"
        :show-add="false"
        :show-export="false"
        :page="pagination.current"
        :page-size="pagination.size"
        :show-selection="false"
        @refresh="refreshTable"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="checkInNo" label="签到编号" width="140" sortable />
        <el-table-column prop="memberName" label="会员" width="120">
          <template #default="{ row }">
            <div class="member-cell">
              <span class="member-name">{{ row.memberName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="签到类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getCheckinTypeType(row.type)" size="small">
              {{ getCheckinTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="签到地点" width="120" />
        <el-table-column prop="checkInTime" label="签到时间" width="160">
          <template #default="{ row }">
            <div class="time-cell">
              <div class="checkin-time">{{ formatDateTime(row.checkInTime) }}</div>
              <div v-if="row.checkOutTime" class="checkout-time">
                签退: {{ formatTime(row.checkOutTime) }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="停留时长" width="100">
          <template #default="{ row }">
            <span v-if="row.checkOutTime" class="duration">
              {{ calculateDuration(row.checkInTime, row.checkOutTime) }}
            </span>
            <span v-else class="in-progress">进行中</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="签到状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getCheckinStatusType(row.status)" size="small">
              {{ getCheckinStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <template #actions="{ row }">
          <el-button
            type="text"
            size="small"
            @click="handleCheckOut(row)"
            v-if="row.status === 'CHECKED_IN'"
          >
            <el-icon><SwitchButton /></el-icon>
            签退
          </el-button>
          <el-button type="text" size="small" @click="handleViewMember(row.memberId)">
            <el-icon><User /></el-icon>
            会员详情
          </el-button>
          <el-dropdown @command="handleMoreAction($event, row)" trigger="click">
            <el-button type="text" size="small">
              <el-icon><MoreFilled /></el-icon>
              更多
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">
                  <el-icon><Edit /></el-icon>
                  编辑
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
    
    <!-- 批量签到对话框 -->
    <el-dialog
      v-model="batchDialog.visible"
      title="批量签到"
      width="500px"
    >
      <el-form
        ref="batchFormRef"
        :model="batchForm"
        :rules="batchRules"
        label-width="100px"
      >
        <el-form-item label="签到类型" prop="type">
          <el-select
            v-model="batchForm.type"
            placeholder="请选择签到类型"
            style="width: 100%;"
          >
            <el-option
              v-for="item in checkinTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="签到地点" prop="location">
          <el-input
            v-model="batchForm.location"
            placeholder="请输入签到地点"
          />
        </el-form-item>
        
        <el-form-item label="选择会员" prop="memberIds">
          <el-transfer
            v-model="batchForm.memberIds"
            :data="memberOptions"
            :titles="['所有会员', '已选择']"
            :props="{
              key: 'id',
              label: 'name'
            }"
            filterable
            :filter-method="filterMember"
            filter-placeholder="搜索会员"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleBatchSubmit">
            确定 ({{ batchForm.memberIds.length }}人)
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import AppTable from '@/components/common/AppTable.vue'
import { useCheckInStore } from '@/stores/checkin'
import { useMemberStore } from '@/stores/member'
import { formatDateTime, formatTime } from '@/utils'
import type { CheckIn, QueryParams, CheckInType, CheckInStatus } from '@/types'

// Store
const checkInStore = useCheckInStore()
const memberStore = useMemberStore()

// Router
const router = useRouter()

// Refs
const batchFormRef = ref<FormInstance>()

// 状态
const loading = ref(false)

const filterForm = reactive({
  memberName: '',
  type: '' as CheckInType | '',
  status: '' as CheckInStatus | '',
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

const todayStats = reactive({
  todayCheckins: 0,
  todayCourses: 0,
  attendanceRate: 0,
  activeMembers: 0
})

const batchDialog = reactive({
  visible: false
})

const batchForm = reactive({
  type: 'GYM_ACCESS' as CheckInType,
  location: '',
  memberIds: [] as number[]
})

const batchRules: FormRules = {
  type: [
    { required: true, message: '请选择签到类型', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入签到地点', trigger: 'blur' }
  ],
  memberIds: [
    { required: true, message: '请选择会员', trigger: 'change' }
  ]
}

// 选项配置
const checkinTypeOptions = [
  { label: '课程签到', value: 'COURSE' },
  { label: '场馆进入', value: 'GYM_ACCESS' },
  { label: '私教课', value: 'PERSONAL_TRAINING' }
]

const checkinStatusOptions = [
  { label: '已签到', value: 'CHECKED_IN' },
  { label: '已签退', value: 'CHECKED_OUT' }
]

// Computed
const checkinList = computed(() => checkInStore.checkIns)

const memberOptions = computed(() => {
  return memberStore.members.map(member => ({
    id: member.id,
    name: `${member.realName} (${member.memberNo})`
  }))
})

// Methods
const loadCheckins = async () => {
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
    
    await checkInStore.fetchCheckIns(params)
    pagination.total = checkInStore.total
  } catch (error) {
    console.error('加载签到记录失败:', error)
    ElMessage.error('加载签到记录失败')
  } finally {
    loading.value = false
  }
}

const loadTodayStats = async () => {
  try {
    const data = await checkInStore.getCheckInStatistics()
    todayStats.todayCheckins = data.todayCheckins || 0
    todayStats.todayCourses = data.todayCourses || 0
    todayStats.attendanceRate = data.attendanceRate || 0
    todayStats.activeMembers = data.activeMembers || 0
  } catch (error) {
    console.error('加载今日统计失败:', error)
  }
}

const loadMembers = async () => {
  try {
    await memberStore.fetchMembers({ pageSize: 100 })
  } catch (error) {
    console.error('加载会员列表失败:', error)
  }
}

const buildFilterParams = () => {
  const params: Record<string, any> = {}
  
  if (filterForm.memberName) {
    params.memberName = filterForm.memberName
  }
  
  if (filterForm.type) {
    params.type = filterForm.type
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
  loadCheckins()
}

const handleReset = () => {
  filterForm.memberName = ''
  filterForm.type = ''
  filterForm.status = ''
  filterForm.dateRange = []
  
  pagination.current = 1
  loadCheckins()
}

const handleSortChange = ({ prop, order }: { prop: string; order: 'ascending' | 'descending' | null }) => {
  sortParams.sortBy = prop
  sortParams.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  loadCheckins()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadCheckins()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadCheckins()
}

const refreshTable = () => {
  loadCheckins()
  loadTodayStats()
}

const handleBatchCheckin = () => {
  batchForm.memberIds = []
  batchForm.location = '健身房前台'
  batchDialog.visible = true
}

const handleExport = async () => {
  try {
    loading.value = true
    const params = buildFilterParams()
    await checkInStore.exportCheckIns(params)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

const handleCheckOut = async (row: CheckIn) => {
  try {
    await ElMessageBox.confirm('确认为该会员进行签退吗？', '签退', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    // 调用签退API
    await checkInStore.updateCheckIn(row.id, {
      checkOutTime: new Date().toISOString(),
      status: 'CHECKED_OUT'
    })
    
    ElMessage.success('签退成功')
    loadCheckins()
  } catch (error) {
    console.error('签退失败:', error)
    ElMessage.error('签退失败')
  }
}

const handleViewMember = (memberId: number) => {
  router.push(`/members/${memberId}`)
}

const handleMoreAction = async (command: string, row: CheckIn) => {
  switch (command) {
    case 'edit':
      handleEditCheckin(row)
      break
    case 'delete':
      handleDeleteCheckin(row.id)
      break
  }
}

const handleEditCheckin = (checkin: CheckIn) => {
  // 实现编辑签到记录功能
  ElMessage.info('编辑功能开发中')
}

const handleDeleteCheckin = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该签到记录吗？此操作不可恢复。', '删除签到记录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    
    await checkInStore.deleteCheckIn(id)
    ElMessage.success('删除成功')
    loadCheckins()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

const filterMember = (query: string, item: any) => {
  return item.name.toLowerCase().includes(query.toLowerCase())
}

const handleBatchSubmit = async () => {
  if (!batchFormRef.value) return
  
  try {
    await batchFormRef.value.validate()
    
    if (batchForm.memberIds.length === 0) {
      ElMessage.warning('请至少选择一名会员')
      return
    }
    
    loading.value = true
    
    await checkInStore.batchCheckIn({
      memberIds: batchForm.memberIds,
      type: batchForm.type
    })
    
    ElMessage.success(`成功为 ${batchForm.memberIds.length} 名会员签到`)
    batchDialog.visible = false
    loadCheckins()
    loadTodayStats()
  } catch (error) {
    console.error('批量签到失败:', error)
    ElMessage.error('批量签到失败')
  } finally {
    loading.value = false
  }
}

const getCheckinTypeType = (type: CheckInType) => {
  switch (type) {
    case 'COURSE':
      return 'primary'
    case 'GYM_ACCESS':
      return 'success'
    case 'PERSONAL_TRAINING':
      return 'warning'
    default:
      return 'info'
  }
}

const getCheckinTypeText = (type: CheckInType) => {
  switch (type) {
    case 'COURSE':
      return '课程签到'
    case 'GYM_ACCESS':
      return '场馆进入'
    case 'PERSONAL_TRAINING':
      return '私教课'
    default:
      return type
  }
}

const getCheckinStatusType = (status: CheckInStatus) => {
  switch (status) {
    case 'CHECKED_IN':
      return 'primary'
    case 'CHECKED_OUT':
      return 'success'
    default:
      return 'info'
  }
}

const getCheckinStatusText = (status: CheckInStatus) => {
  switch (status) {
    case 'CHECKED_IN':
      return '已签到'
    case 'CHECKED_OUT':
      return '已签退'
    default:
      return '未知'
  }
}

const calculateDuration = (checkInTime: string, checkOutTime: string) => {
  const checkIn = new Date(checkInTime)
  const checkOut = new Date(checkOutTime)
  const diffMs = checkOut.getTime() - checkIn.getTime()
  
  const hours = Math.floor(diffMs / (1000 * 60 * 60))
  const minutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  }
  return `${minutes}分钟`
}

// 生命周期
onMounted(() => {
  loadCheckins()
  loadTodayStats()
  loadMembers()
})
</script>

<style lang="scss" scoped>
.checkin-list-container {
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
  
  .today-stats {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 24px;
    
    .stats-card {
      border-radius: 12px;
      border: 1px solid var(--gymflow-border);
      
      :deep(.el-card__body) {
        padding: 20px;
      }
      
      .stats-content {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .stats-icon {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 48px;
          height: 48px;
          border-radius: 12px;
          font-size: 24px;
          
          &.today {
            background: rgba(64, 158, 255, 0.1);
            color: #409eff;
          }
          
          &.course {
            background: rgba(103, 194, 58, 0.1);
            color: #67c23a;
          }
          
          &.attendance {
            background: rgba(230, 162, 60, 0.1);
            color: #e6a23c;
          }
          
          &.member {
            background: rgba(155, 89, 182, 0.1);
            color: #9b59b6;
          }
        }
        
        .stats-info {
          .stats-number {
            font-size: 24px;
            font-weight: 700;
            color: var(--gymflow-text-primary);
            margin-bottom: 4px;
            line-height: 1;
          }
          
          .stats-label {
            font-size: 12px;
            color: var(--gymflow-text-secondary);
          }
        }
      }
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
    
    .member-cell {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .member-name {
        font-weight: 500;
      }
    }
    
    .time-cell {
      .checkin-time {
        font-size: 14px;
        color: var(--gymflow-text-primary);
      }
      
      .checkout-time {
        font-size: 12px;
        color: var(--gymflow-text-secondary);
        margin-top: 2px;
      }
    }
    
    .duration {
      font-weight: 500;
      color: var(--gymflow-primary);
    }
    
    .in-progress {
      color: var(--el-color-warning);
      font-weight: 500;
    }
  }
}

:deep(.delete-item) {
  color: var(--el-color-danger);
  
  &:hover {
    background: rgba(245, 108, 108, 0.1);
  }
}

// 批量签到对话框
:deep(.el-dialog) {
  border-radius: 12px;
  
  .el-dialog__header {
    padding: 20px;
    border-bottom: 1px solid var(--gymflow-border);
  }
  
  .el-dialog__body {
    padding: 20px;
    
    .el-transfer {
      display: flex;
      justify-content: center;
      
      :deep(.el-transfer-panel) {
        width: 200px;
        
        .el-transfer-panel__body {
          height: 300px;
        }
      }
    }
  }
  
  .el-dialog__footer {
    padding: 20px;
    border-top: 1px solid var(--gymflow-border);
  }
}

// 响应式设计
@media (max-width: 768px) {
  .checkin-list-container {
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
    
    .today-stats {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}
</style>