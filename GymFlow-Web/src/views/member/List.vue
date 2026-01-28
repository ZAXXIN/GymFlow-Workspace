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
      <el-form :model="filterForm" inline label-width="80px">
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
        <el-form-item label="会员状态">
          <el-select
            v-model="filterForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 180px;"
          >
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="不活跃" value="INACTIVE" />
            <el-option label="暂停" value="SUSPENDED" />
            <el-option label="过期" value="EXPIRED" />
          </el-select>
        </el-form-item>
        <el-form-item label="注册时间">
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
          <span class="table-title">会员列表</span>
          <div class="table-actions">
            <el-button text @click="refreshTable">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="memberList"
        style="width: 100%"
        row-key="id"
        v-loading="loading"
      >
        <el-table-column prop="memberNo" label="会员编号" width="120" />
        <el-table-column prop="realName" label="姓名" width="100">
          <template #default="{ row }">
            <div class="member-info">
              <el-avatar :size="32" class="member-avatar">
                {{ row.realName?.charAt(0) || 'M' }}
              </el-avatar>
              <span class="member-name">{{ row.realName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="remainingSessions" label="剩余课时" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.remainingSessions > 0 ? 'success' : 'danger'" size="small">
              {{ row.remainingSessions || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="membershipStartDate" label="会籍开始" width="120">
          <template #default="{ row }">
            {{ formatDate(row.membershipStartDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="membershipEndDate" label="会籍结束" width="120">
          <template #default="{ row }">
            {{ formatDate(row.membershipEndDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="handleViewDetail(row.id)">
              详情
            </el-button>
            <el-button type="text" size="small" @click="handleEdit(row.id)">
              编辑
            </el-button>
            <el-button type="text" size="small" @click="handleDelete(row.id)" style="color: #f56c6c;">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'

const router = useRouter()

// 状态
const loading = ref(false)
const filterForm = reactive({
  memberNo: '',
  realName: '',
  phone: '',
  status: '',
  dateRange: [] as string[]
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 模拟数据
const memberList = ref([
  {
    id: 1,
    memberNo: 'M001',
    realName: '张三',
    phone: '13800138001',
    gender: 1,
    remainingSessions: 12,
    status: 'ACTIVE',
    membershipStartDate: '2024-01-01',
    membershipEndDate: '2024-12-31',
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    memberNo: 'M002',
    realName: '李四',
    phone: '13800138002',
    gender: 0,
    remainingSessions: 0,
    status: 'EXPIRED',
    membershipStartDate: '2023-01-01',
    membershipEndDate: '2023-12-31',
    createTime: '2023-01-01 10:00:00'
  },
  {
    id: 3,
    memberNo: 'M003',
    realName: '王五',
    phone: '13800138003',
    gender: 1,
    remainingSessions: 8,
    status: 'ACTIVE',
    membershipStartDate: '2024-02-01',
    membershipEndDate: '2024-11-30',
    createTime: '2024-02-01 14:30:00'
  }
])

// 方法
const formatDate = (date: string) => {
  if (!date) return '-'
  return date
}

const formatDateTime = (datetime: string) => {
  if (!datetime) return '-'
  return datetime.replace('T', ' ')
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'INACTIVE':
      return 'info'
    case 'SUSPENDED':
      return 'warning'
    case 'EXPIRED':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return '活跃'
    case 'INACTIVE':
      return '不活跃'
    case 'SUSPENDED':
      return '暂停'
    case 'EXPIRED':
      return '过期'
    default:
      return status
  }
}

const handleSearch = () => {
  console.log('搜索条件:', filterForm)
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('搜索完成')
  }, 500)
}

const handleReset = () => {
  filterForm.memberNo = ''
  filterForm.realName = ''
  filterForm.phone = ''
  filterForm.status = ''
  filterForm.dateRange = []
  pagination.current = 1
  handleSearch()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadMembers()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadMembers()
}

const refreshTable = () => {
  loadMembers()
  ElMessage.success('刷新成功')
}

const loadMembers = () => {
  loading.value = true
  setTimeout(() => {
    // 模拟加载数据
    pagination.total = memberList.value.length
    loading.value = false
  }, 500)
}

const handleAddMember = () => {
  router.push('/member/add')
}

const handleViewDetail = (id: number) => {
  router.push(`/member/detail/${id}`)
}

const handleEdit = (id: number) => {
  router.push(`/member/edit/${id}`)
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个会员吗？此操作不可恢复。',
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    // 模拟删除
    memberList.value = memberList.value.filter(member => member.id !== id)
    ElMessage.success('删除成功')
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  loadMembers()
  console.log('会员列表页面加载完成')
})
</script>

<style scoped>
.member-list-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left .page-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.table-card {
  border-radius: 12px;
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
  gap: 8px;
}

.member-avatar {
  background: #409eff;
  color: white;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0 0;
}
</style>