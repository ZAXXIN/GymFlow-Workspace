<template>
  <div class="member-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">会员管理</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAddMember">
          <el-icon><Plus /></el-icon>
          新增会员
        </el-button>
      </div>
    </div>
    
    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="会员编号">
          <el-input
            v-model="filterForm.memberNo"
            placeholder="请输入会员编号"
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input
            v-model="filterForm.realName"
            placeholder="请输入姓名"
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
        <el-form-item label="会员卡状态">
          <el-select
            v-model="filterForm.cardStatus"
            placeholder="请选择状态"
            clearable
            style="width: 180px;"
          >
            <el-option label="有效" value="ACTIVE" />
            <el-option label="过期" value="EXPIRED" />
            <el-option label="用完" value="USED_UP" />
          </el-select>
        </el-form-item>
        <!-- <el-form-item label="注册时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px;"
          />
        </el-form-item> -->
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
          <span class="table-title">会员列表</span>
          <div class="table-actions">
            <el-button text @click="refreshTable" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="memberStore.members"
        style="width: 100%"
        row-key="id"
        v-loading="memberStore.loading"
        stripe
        border
      >
        <el-table-column prop="memberNo" label="会员编号" width="120" fixed="left" />
        <el-table-column prop="realName" label="姓名" width="100">
          <template #default="{ row }">
            <div class="member-info">
              <span class="member-name">{{ row.realName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 0 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" align="center" />
        <el-table-column prop="cardTypeDesc" label="会员卡类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getCardTypeTag(row.cardType)" size="small">
              {{ row.cardTypeDesc || '无' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cardStatusDesc" label="会员卡状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getCardStatusTag(row.cardStatus)" size="small">
              {{ row.cardStatusDesc || '无' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remainingSessions" label="剩余课时" width="100" align="center">
          <template #default="{ row }">
            <div v-if="row.remainingSessions > 0">
              <el-tag type="success" size="small">
                {{ row.remainingSessions }}
              </el-tag>
            </div>
            <div v-else-if="row.cardType === 0 || row.cardType === 1">
              <el-tag type="danger" size="small">0</el-tag>
            </div>
            <div v-else>
              <span class="no-sessions">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="cardEndDate" label="有效期至" width="120">
          <template #default="{ row }">
            <div v-if="row.cardEndDate">
              {{ formatDate(row.cardEndDate) }}
            </div>
            <div v-else>
              <span class="no-date">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="personalCoachName" label="专属教练" width="120" />
        <el-table-column prop="totalCheckins" label="总签到" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">
              {{ row.totalCheckins || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalCourseHours" label="总课时" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="warning" size="small">
              {{ row.totalCourseHours || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalSpent" label="累计消费" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ formatAmount(row.totalSpent) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row.id)">
              详情
            </el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row.id)">
              编辑
            </el-button>
            <el-popconfirm
              title="确定要删除这个会员吗？"
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
          v-model:current-page="memberStore.pageInfo.pageNum"
          v-model:page-size="memberStore.pageInfo.pageSize"
          :total="memberStore.total"
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, View, Edit, Delete } from '@element-plus/icons-vue'
import { useMemberStore } from '@/stores/member'
import type { MemberQueryDTO } from '@/types/member'

const router = useRouter()
const memberStore = useMemberStore()

// 筛选表单
const filterForm = reactive({
  memberNo: '',
  realName: '',
  phone: '',
  cardStatus: '',
  // dateRange: [] as string[]
})

const loading = computed(() => memberStore.loading)

// 方法
const formatDate = (date: string | Date) => {
  if (!date) return '-'
  if (typeof date === 'string') {
    return date.split('T')[0]
  }
  return date.toISOString().split('T')[0]
}

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

// 卡片类型标签样式
const getCardTypeTag = (cardType: number) => {
  switch (cardType) {
    case 0: return 'primary'   // 私教课
    case 1: return 'success'   // 团课
    case 2: return 'warning'   // 月卡
    case 3: return 'danger'    // 年卡
    case 4: return 'info'      // 周卡
    default: return 'info'
  }
}

// 卡片状态标签样式
const getCardStatusTag = (cardStatus: string) => {
  switch (cardStatus) {
    case 'ACTIVE': return 'success'
    case 'EXPIRED': return 'danger'
    case 'USED_UP': return 'warning'
    default: return 'info'
  }
}

// 搜索处理
const handleSearch = async () => {
  const queryParams: MemberQueryDTO = {
    pageNum: memberStore.pageInfo.pageNum,
    pageSize: memberStore.pageInfo.pageSize,
    memberNo: filterForm.memberNo,
    realName: filterForm.realName,
    phone: filterForm.phone,
    cardStatus: filterForm.cardStatus,
    // startDate: filterForm.dateRange?.[0],
    // endDate: filterForm.dateRange?.[1]
  }
  
  await memberStore.fetchMembers(queryParams)
}

const handleReset = () => {
  filterForm.memberNo = ''
  filterForm.realName = ''
  filterForm.phone = ''
  filterForm.cardStatus = ''
  // filterForm.dateRange = []
  memberStore.setPageInfo(1, 10)
  handleSearch()
}

// 分页处理
const handleSizeChange = (size: number) => {
  memberStore.setPageInfo(1, size)
  handleSearch()
}

const handleCurrentChange = (current: number) => {
  memberStore.setPageInfo(current, memberStore.pageInfo.pageSize)
  handleSearch()
}

// 刷新表格
const refreshTable = async () => {
  await handleSearch()
  ElMessage.success('刷新成功')
}

// 导航操作
const handleAddMember = () => {
  router.push('/member/add')
}

const handleViewDetail = (id: number) => {
  router.push(`/member/detail/${id}`)
}

const handleEdit = (id: number) => {
  router.push(`/member/edit/${id}`)
}

// 删除会员
const handleDelete = async (id: number) => {
  try {
    await memberStore.deleteMember(id)
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
.member-list-container {
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

.member-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.member-name {
  font-weight: 500;
}

.amount {
  font-weight: 600;
  color: #67C23A;
}

.no-sessions,
.no-date {
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
</style>