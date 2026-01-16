<template>
  <div class="member-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">会员管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>会员管理</el-breadcrumb-item>
          <el-breadcrumb-item>会员列表</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAddMember">
          <el-icon><Plus /></el-icon>
          新增会员
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
            <el-option
              v-for="item in memberStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
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
        v-loading="loading"
        :data="memberList"
        style="width: 100%"
        row-key="id"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="memberNo" label="会员编号" width="120" sortable />
        <el-table-column prop="realName" label="姓名" width="100">
          <template #default="{ row }">
            <div class="member-info">
              <el-avatar :size="32" :src="row.user?.avatar" class="member-avatar">
                {{ row.realName?.charAt(0) || 'M' }}
              </el-avatar>
              <span class="member-name">{{ row.realName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="user.phone" label="手机号" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="remainingSessions" label="剩余课时" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.remainingSessions > 0 ? 'success' : 'danger'" size="small">
              {{ row.remainingSessions }}
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
        <el-table-column prop="createdAt" label="注册时间" width="160" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
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
                  <el-dropdown-item command="health">
                    <el-icon><Document /></el-icon>
                    健康档案
                  </el-dropdown-item>
                  <el-dropdown-item command="renew">
                    <el-icon><RefreshRight /></el-icon>
                    续费
                  </el-dropdown-item>
                  <el-dropdown-item command="suspend" v-if="row.status === 'ACTIVE'">
                    <el-icon><CircleClose /></el-icon>
                    暂停会籍
                  </el-dropdown-item>
                  <el-dropdown-item command="resume" v-else-if="row.status === 'SUSPENDED'">
                    <el-icon><CircleCheck /></el-icon>
                    恢复会籍
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
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑对话框 -->
    <member-form-dialog
      v-model="formDialog.visible"
      :mode="formDialog.mode"
      :member-id="formDialog.memberId"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Download,
  Search,
  Refresh,
  View,
  Edit,
  MoreFilled,
  Document,
  RefreshRight,
  CircleClose,
  CircleCheck,
  Delete
} from '@element-plus/icons-vue'
import { useMemberStore } from '@/stores/member'
import { memberStatusOptions } from '@/utils/constants'
import { formatDate, formatDateTime } from '@/utils'
import type { Member, MemberStatus, QueryParams } from '@/types'
import MemberFormDialog from '@/components/business/MemberFormDialog.vue'

// Store
const memberStore = useMemberStore()

// Router
const router = useRouter()

// 状态
const loading = ref(false)
const filterForm = reactive({
  memberNo: '',
  realName: '',
  phone: '',
  status: '' as MemberStatus | '',
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

const formDialog = reactive({
  visible: false,
  mode: 'create' as 'create' | 'edit',
  memberId: 0
})

// Computed
const memberList = computed(() => memberStore.members)

// Methods
const loadMembers = async () => {
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
    
    await memberStore.fetchMembers(params)
    pagination.total = memberStore.total
  } catch (error) {
    console.error('加载会员列表失败:', error)
    ElMessage.error('加载会员列表失败')
  } finally {
    loading.value = false
  }
}

const buildFilterParams = () => {
  const params: Record<string, any> = {}
  
  if (filterForm.memberNo) {
    params.memberNo = filterForm.memberNo
  }
  
  if (filterForm.realName) {
    params.realName = filterForm.realName
  }
  
  if (filterForm.phone) {
    params.phone = filterForm.phone
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
  loadMembers()
}

const handleReset = () => {
  filterForm.memberNo = ''
  filterForm.realName = ''
  filterForm.phone = ''
  filterForm.status = ''
  filterForm.dateRange = []
  
  pagination.current = 1
  loadMembers()
}

const handleSortChange = ({ prop, order }: { prop: string; order: 'ascending' | 'descending' | null }) => {
  sortParams.sortBy = prop
  sortParams.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  loadMembers()
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
}

const handleAddMember = () => {
  formDialog.mode = 'create'
  formDialog.memberId = 0
  formDialog.visible = true
}

const handleEdit = (id: number) => {
  formDialog.mode = 'edit'
  formDialog.memberId = id
  formDialog.visible = true
}

const handleViewDetail = (id: number) => {
  router.push(`/members/${id}`)
}

const handleMoreAction = async (command: string, row: Member) => {
  switch (command) {
    case 'health':
      router.push(`/members/${row.id}/health`)
      break
      
    case 'renew':
      handleRenew(row.id)
      break
      
    case 'suspend':
      handleSuspend(row.id)
      break
      
    case 'resume':
      handleResume(row.id)
      break
      
    case 'delete':
      handleDelete(row.id)
      break
  }
}

const handleRenew = async (id: number) => {
  try {
    await ElMessageBox.prompt('请输入续费月数', '会员续费', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^[1-9]\d*$/,
      inputErrorMessage: '请输入有效的月数'
    })
    
    // 这里调用续费API
    ElMessage.success('续费成功')
    loadMembers()
  } catch (error) {
    console.error('续费失败:', error)
  }
}

const handleSuspend = async (id: number) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入暂停天数', '暂停会籍', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^[1-9]\d*$/,
      inputErrorMessage: '请输入有效的天数'
    })
    
    // 这里调用暂停API
    ElMessage.success('会籍已暂停')
    loadMembers()
  } catch (error) {
    console.error('暂停会籍失败:', error)
  }
}

const handleResume = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认恢复该会员的会籍吗？', '恢复会籍', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 这里调用恢复API
    ElMessage.success('会籍已恢复')
    loadMembers()
  } catch (error) {
    console.error('恢复会籍失败:', error)
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该会员吗？此操作不可恢复。', '删除会员', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    
    await memberStore.deleteMember(id)
    ElMessage.success('删除成功')
    loadMembers()
  } catch (error) {
    console.error('删除会员失败:', error)
    ElMessage.error('删除失败')
  }
}

const handleExport = async () => {
  try {
    loading.value = true
    const params = buildFilterParams()
    await memberStore.exportMembers(params)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status: MemberStatus) => {
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

const getStatusText = (status: MemberStatus) => {
  switch (status) {
    case 'ACTIVE':
      return '活跃'
    case 'INACTIVE':
      return '不活跃'
    case 'SUSPENDED':
      return '暂停'
    case 'EXPIRED':
      return '已过期'
    default:
      return '未知'
  }
}

const handleFormSuccess = () => {
  formDialog.visible = false
  loadMembers()
}

// 生命周期
onMounted(() => {
  loadMembers()
})
</script>

<style lang="scss" scoped>
.member-list-container {
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
    
    .member-info {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .member-avatar {
        background: var(--gymflow-primary);
        color: white;
      }
      
      .member-name {
        font-weight: 500;
      }
    }
    
    :deep(.el-table) {
      th {
        font-weight: 600;
        color: var(--gymflow-text-primary);
        background: var(--gymflow-bg);
      }
      
      .el-button--text {
        padding: 4px 8px;
        
        .el-icon {
          margin-right: 4px;
        }
      }
    }
    
    .pagination-wrapper {
      display: flex;
      justify-content: center;
      padding: 20px 0 0;
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
  .member-list-container {
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