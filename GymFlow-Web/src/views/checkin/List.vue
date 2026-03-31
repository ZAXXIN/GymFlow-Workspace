<template>
  <div class="checkin-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">签到管理</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleQuickCheckIn">
          <el-icon>
            <Plus />
          </el-icon>
          快速签到
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="会员姓名">
          <el-input v-model="filterForm.memberName" placeholder="请输入会员姓名" clearable style="width: 180px;" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="filterForm.memberPhone" placeholder="请输入手机号" clearable style="width: 180px;" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="签到类型">
          <el-select v-model="filterForm.hasCourseBooking" placeholder="请选择签到类型" clearable style="width: 180px;">
            <el-option label="课程签到" :value="true" />
            <el-option label="自由训练" :value="false" />
          </el-select>
        </el-form-item>
        <!-- <el-form-item label="签到时间">
          <el-date-picker v-model="filterForm.dateRange" type="daterange" value-format="YYYY-MM-DD" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 240px;" />
        </el-form-item> -->
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon>
              <Search />
            </el-icon>
            查询
          </el-button>
          <el-button @click="handleReset" :disabled="loading">
            <el-icon>
              <Refresh />
            </el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 今日统计 -->
    <el-card class="stats-card">
      <template #header>
        <div class="stats-header">
          <span class="stats-title">今日签到统计</span>
          <span class="stats-time">更新时间：{{ formatDateTime(statsUpdateTime) }}</span>
        </div>
      </template>

      <el-row :gutter="20" v-if="todayStats">
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-label">总签到数</div>
            <div class="stat-value">{{ todayStats.totalCheckIns }}</div>
            <div class="stat-trend">较昨日 {{ getTrendPercentage(todayStats.totalCheckIns, yesterdayTotalCheckIns) }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-label">课程签到</div>
            <div class="stat-value text-primary">{{ todayStats.courseCheckIns }}</div>
            <div class="stat-trend">占总签到 {{ getPercentage(todayStats.courseCheckIns, todayStats.totalCheckIns) }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-label">自由训练</div>
            <div class="stat-value text-warning">{{ todayStats.freeCheckIns }}</div>
            <div class="stat-trend">占总签到 {{ getPercentage(todayStats.freeCheckIns, todayStats.totalCheckIns) }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-label">教练签到</div>
            <div class="stat-value text-success">{{ todayStats.coachCheckIns }}</div>
            <div class="stat-trend">占签到方式 {{ getPercentage(todayStats.coachCheckIns, todayStats.totalCheckIns) }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-label">前台签到</div>
            <div class="stat-value text-info">{{ todayStats.frontDeskCheckIns }}</div>
            <div class="stat-trend">占签到方式 {{ getPercentage(todayStats.frontDeskCheckIns, todayStats.totalCheckIns) }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-item">
            <div class="stat-label">签到率</div>
            <div class="stat-value text-danger">{{ todayStats.checkInRate.toFixed(1) }}%</div>
            <div class="stat-trend">今日活跃会员：{{ todayStats.uniqueMembers }}人</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="table-title">签到记录</span>
          <div class="table-actions">
            <el-button text @click="refreshTable" :loading="loading">
              <el-icon>
                <Refresh />
              </el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="formattedCheckIns" style="width: 100%" row-key="id" v-loading="loading" stripe border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="checkinTimeFormatted" label="签到时间" width="180" fixed="left" />
        <el-table-column label="会员信息" width="200">
          <template #default="{ row }">
            <div class="member-info">
              <div class="member-name">{{ row.memberName || '-' }}</div>
              <div class="member-phone">{{ row.memberPhone || '-' }}</div>
              <div class="member-no" v-if="row.memberNo">{{ row.memberNo }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="签到类型" width="150">
          <template #default="{ row }">
            <el-tag :type="row.courseCheckIn ? 'success' : 'primary'" size="small">
              {{ row.courseCheckIn ? '课程签到' : '自由训练' }}
            </el-tag>
            <div class="course-info" v-if="row.courseCheckIn && row.courseName">
              <span class="course-name">{{ row.courseName }}</span>
              <span class="coach-name" v-if="row.coachName">({{ row.coachName }})</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="checkinMethodDesc" label="签到方式" width="100">
          <template #default="{ row }">
            <el-tag :type="row.checkinMethod === 0 ? 'warning' : 'info'" size="small">
              {{ row.checkinMethodDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notes" label="备注" width="200">
          <template #default="{ row }">
            <div class="notes-content" :title="row.notes">
              {{ row.notes || '无' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTimeFormatted" label="创建时间" width="180" />
        <el-table-column label="操作" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row.id)">
              详情
            </el-button>
            <el-button v-if="hasPermission('checkIn:edit')&&canEdit(row)" type="warning" link size="small" @click="handleEdit(row.id)">
              编辑
            </el-button>
            <el-popconfirm title="确定要删除这条记录吗？" @confirm="handleDelete(row.id)" confirm-button-text="确定" cancel-button-text="取消" v-if="!row.courseCheckIn && hasPermission('checkIn:delete')">
              <template #reference>
                <el-button type="danger" link size="small">
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 批量操作 -->
      <div class="batch-actions" v-if="selectedRows.length > 0">
        <el-button type="danger" size="small" @click="handleBatchDelete">
          <el-icon>
            <Delete />
          </el-icon>
          批量删除
        </el-button>
        <span class="selected-count">已选择 {{ selectedRows.length }} 项</span>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pageInfo.pageNum" v-model:page-size="pageInfo.pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" :disabled="loading" />
      </div>
    </el-card>

    <!-- 快速签到对话框 -->
    <el-dialog v-model="quickCheckInDialogVisible" title="快速签到" width="500px" :close-on-click-modal="false">
      <el-form :model="quickCheckInForm" :rules="quickCheckInRules" ref="quickCheckInFormRef">
        <!-- 签到类型选择 -->
        <el-form-item label="签到类型" prop="checkInType">
          <el-radio-group v-model="quickCheckInForm.checkInType">
            <el-radio :label="0">自由训练</el-radio>
            <el-radio :label="1">课程签到</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 自由训练：选择会员 -->
        <el-form-item label="选择会员" prop="memberId" v-if="quickCheckInForm.checkInType === 0">
          <el-select v-model="quickCheckInForm.memberId" placeholder="请选择会员" filterable remote :remote-method="searchMembers" :loading="searchLoading" style="width: 100%" clearable>
            <el-option v-for="member in memberOptions" :key="member.id" :label="`${member.realName}`" :value="member.id">
              <div class="member-option">
                <span class="member-name">{{ member.realName }}</span>
                <span class="member-phone">{{ member.phone }}</span>
                <span class="member-no">{{ member.memberNo }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 课程签到：输入签到码 -->
        <el-form-item label="签到码" prop="checkinCode" v-if="quickCheckInForm.checkInType === 1">
          <el-input v-model="quickCheckInForm.checkinCode" placeholder="请输入6位数字签到码" maxlength="6" show-word-limit />
          <div class="form-tip">请输入会员提供的6位数字签到码</div>
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注" prop="notes">
          <el-input v-model="quickCheckInForm.notes" placeholder="请输入备注信息（选填）" :rows="3" type="textarea" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="quickCheckInDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitQuickCheckIn" :loading="loading">
          确认签到
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { useCheckInStore } from '@/stores/checkIn'
import { useMemberStore } from '@/stores/member'
import type { CheckInQueryParams } from '@/types/checkIn'
import type { MemberListVO } from '@/types/member'
import { usePermission } from '@/composables/usePermission'

const { hasPermission } = usePermission()

const router = useRouter()
const checkInStore = useCheckInStore()
const memberStore = useMemberStore()

// 筛选表单
const filterForm = reactive({
  memberName: '',
  memberPhone: '',
  checkinMethod: 1,
  hasCourseBooking: undefined as boolean | undefined,
  dateRange: [] as string[],
})

// 选择的行
const selectedRows = ref<any[]>([])

// 今日统计相关
const todayStats = ref<any>(null)
const yesterdayTotalCheckIns = ref(0)
const statsUpdateTime = ref(new Date())

// 快速签到相关
const quickCheckInDialogVisible = ref(false)
const quickCheckInFormRef = ref<FormInstance>()
const searchLoading = ref(false)
const memberOptions = ref<MemberListVO[]>([])

const quickCheckInForm = reactive({
  checkInType: 0, // 0-自由训练，1-课程签到
  memberId: undefined as number | undefined,
  checkinCode: '',
  checkinMethod: 1,
  notes: '',
})

const quickCheckInRules = {
  checkInType: [{ required: true, message: '请选择签到类型', trigger: 'change' }],
  memberId: [
    {
      required: true,
      message: '请选择会员',
      trigger: 'change',
      validator: (rule: any, value: number, callback: Function) => {
        if (quickCheckInForm.checkInType === 0 && !value) {
          callback(new Error('请选择会员'))
        } else {
          callback()
        }
      },
    },
  ],
  checkinCode: [
    {
      required: true,
      message: '请输入签到码',
      trigger: 'blur',
      validator: (rule: any, value: string, callback: Function) => {
        if (quickCheckInForm.checkInType === 1 && !value) {
          callback(new Error('请输入签到码'))
        } else if (quickCheckInForm.checkInType === 1 && !/^\d{6}$/.test(value)) {
          callback(new Error('签到码必须是6位数字'))
        } else {
          callback()
        }
      },
    },
  ],
}
// 获取store状态
const { checkInList, total, loading, pageInfo, formattedCheckInList, formatDateTime } = checkInStore

// 格式化后的签到列表
const formattedCheckIns = computed(() => formattedCheckInList())

// 是否可以编辑（只能编辑一周内的自由训练签到）
const canEdit = (row: any) => {
  if (row.courseCheckIn) return false
  const checkinTime = new Date(row.checkinTime)
  const oneWeekAgo = new Date()
  oneWeekAgo.setDate(oneWeekAgo.getDate() - 7)
  return checkinTime > oneWeekAgo
}

// 计算百分比
const getPercentage = (part: number, total: number) => {
  if (total === 0) return '0%'
  return `${((part / total) * 100).toFixed(1)}%`
}

// 计算趋势百分比
const getTrendPercentage = (today: number, yesterday: number) => {
  if (yesterday === 0) return '+100%'
  const diff = today - yesterday
  const percentage = (diff / yesterday) * 100
  return `${percentage >= 0 ? '+' : ''}${percentage.toFixed(1)}%`
}

// 搜索会员
const searchMembers = async (query: string) => {
  if (!query.trim()) {
    memberOptions.value = []
    return
  }

  searchLoading.value = true
  try {
    const params = {
      realName: query,
      pageNum: 1,
      pageSize: 20,
    }
    const response = await memberStore.fetchMembers(params)
    memberOptions.value = response.list || []
  } catch (error) {
    console.error('搜索会员失败:', error)
    memberOptions.value = []
  } finally {
    searchLoading.value = false
  }
}

// 加载数据
const loadData = async () => {
  const params: CheckInQueryParams = {
    pageNum: pageInfo.pageNum,
    pageSize: pageInfo.pageSize,
    memberName: filterForm.memberName,
    checkinMethod: filterForm.checkinMethod,
    hasCourseBooking: filterForm.hasCourseBooking,
  }

  // 处理日期范围
  if (filterForm.dateRange?.length === 2) {
    params.startDate = filterForm.dateRange[0]
    params.endDate = filterForm.dateRange[1]
  }

  await checkInStore.fetchCheckInList(params)
}

// 加载今日统计
const loadTodayStats = async () => {
  try {
    const stats = await checkInStore.fetchTodayCheckInStats()
    todayStats.value = stats
    statsUpdateTime.value = new Date()
  } catch (error) {
    console.error('加载今日统计失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pageInfo.pageNum = 1
  loadData()
}

// 重置
const handleReset = () => {
  filterForm.memberName = ''
  filterForm.memberPhone = ''
  filterForm.checkinMethod = 1
  filterForm.hasCourseBooking = undefined
  filterForm.dateRange = []
  pageInfo.pageNum = 1
  loadData()
}

// 快速签到
const handleQuickCheckIn = () => {
  quickCheckInDialogVisible.value = true
  // 重置表单
  quickCheckInForm.checkInType = 0
  quickCheckInForm.memberId = undefined
  quickCheckInForm.checkinCode = ''
  quickCheckInForm.notes = ''
  quickCheckInForm.checkinMethod = 1
  memberOptions.value = []
}

// 提交快速签到
const submitQuickCheckIn = async () => {
  if (!quickCheckInFormRef.value) return

  try {
    await quickCheckInFormRef.value.validate()

    if (quickCheckInForm.checkInType === 0) {
      // 自由训练签到
      if (!quickCheckInForm.memberId) {
        ElMessage.error('请选择会员')
        return
      }
      await checkInStore.memberCheckIn(
        quickCheckInForm.memberId,
        quickCheckInForm.checkinMethod,
        quickCheckInForm.notes
      )
      ElMessage.success('自由训练签到成功')
    } else {
      // 课程签到（通过数字码）
      if (!quickCheckInForm.checkinCode) {
        ElMessage.error('请输入签到码')
        return
      }
      await checkInStore.verifyByCode(
        quickCheckInForm.checkinCode,
        quickCheckInForm.checkinMethod,
        quickCheckInForm.notes
      )
      ElMessage.success('课程签到成功')
    }

    quickCheckInDialogVisible.value = false
    loadData()
    loadTodayStats()
  } catch (error: any) {
    console.error('签到失败:', error)
    ElMessage.error(error?.message || '签到失败')
  }
}

// 查看详情
const handleViewDetail = (id: number) => {
  router.push(`/checkIn/detail/${id}`)
}

// 编辑签到
const handleEdit = async (id: number) => {
  try {
    const { value: notes } = await ElMessageBox.prompt('请输入新的备注信息', '编辑签到', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputValue: selectedRows.value.find((row) => row.id === id)?.notes || '',
    })

    await checkInStore.updateCheckIn(id, notes)
    ElMessage.success('更新成功')
    loadData()
  } catch (error) {
    // 用户取消
  }
}

// 删除签到
const handleDelete = async (id: number) => {
  try {
    await checkInStore.deleteCheckIn(id)
    ElMessage.success('删除成功')
    loadData()
    loadTodayStats()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的签到记录')
    return
  }

  // 检查是否有课程签到记录
  const courseCheckIns = selectedRows.value.filter((row) => row.courseCheckIn)
  if (courseCheckIns.length > 0) {
    ElMessage.warning(`包含 ${courseCheckIns.length} 条课程签到记录，不能删除`)
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条签到记录吗？`,
      '警告',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
      }
    )

    const ids = selectedRows.value.map((row) => row.id)
    await checkInStore.batchDeleteCheckIns(ids)
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadData()
    loadTodayStats()
  } catch (error) {
    // 用户取消
  }
}

// 选择变化
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

// 分页大小变化
const handleSizeChange = (size: number) => {
  pageInfo.pageSize = size
  pageInfo.pageNum = 1
  loadData()
}

// 页码变化
const handleCurrentChange = (current: number) => {
  pageInfo.pageNum = current
  loadData()
}

// 刷新表格
const refreshTable = async () => {
  await loadData()
  await loadTodayStats()
  ElMessage.success('刷新成功')
}

// 初始化加载
onMounted(() => {
  loadData()
  loadTodayStats()
})
</script>

<style scoped lang="scss">
.checkin-list-container {
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

.stats-card {
  margin-bottom: 20px;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.stats-time {
  font-size: 12px;
  color: #909399;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;

  .stat-label {
    font-size: 14px;
    color: #606266;
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 24px;
    font-weight: 700;
    margin-bottom: 4px;

    &.text-primary {
      color: #409eff;
    }

    &.text-warning {
      color: #e6a23c;
    }

    &.text-success {
      color: #67c23a;
    }

    &.text-info {
      color: #909399;
    }

    &.text-danger {
      color: #f56c6c;
    }
  }

  .stat-trend {
    font-size: 12px;
    color: #909399;
  }
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

.member-info {
  .member-name {
    font-weight: 500;
    margin-bottom: 4px;
  }

  .member-phone {
    font-size: 12px;
    color: #909399;
    margin-bottom: 2px;
  }

  .member-no {
    font-size: 11px;
    color: #c0c4cc;
    background-color: #f0f0f0;
    padding: 1px 4px;
    border-radius: 2px;
    display: inline-block;
  }
}

.course-info {
  margin-top: 4px;
  font-size: 12px;

  .course-name {
    color: #67c23a;
  }

  .coach-name {
    color: #909399;
    margin-left: 4px;
  }
}

.notes-content {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.batch-actions {
  margin: 16px 0;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 12px;

  .selected-count {
    color: #606266;
    font-size: 14px;
  }
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.member-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;

  .member-name {
    font-weight: 500;
  }

  .member-phone {
    color: #909399;
    font-size: 12px;
    margin: 0 8px;
  }

  .member-no {
    color: #c0c4cc;
    font-size: 11px;
  }
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

:deep(.el-col) {
  margin-bottom: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>