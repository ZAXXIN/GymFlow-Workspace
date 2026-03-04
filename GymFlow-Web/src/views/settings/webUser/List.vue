<template>
  <div class="user-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">用户管理</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreateUser">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>
    </div>
    
    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="用户名">
          <el-input
            v-model="filterForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <!-- <el-form-item label="真实姓名">
          <el-input
            v-model="filterForm.realName"
            placeholder="请输入真实姓名"
            clearable
            style="width: 180px;"
          />
        </el-form-item> -->
        <el-form-item label="角色">
          <el-select
            v-model="filterForm.role"
            placeholder="请选择角色"
            clearable
            style="width: 180px;"
          >
            <el-option label="老板" :value="0" />
            <el-option label="前台" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="filterForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 180px;"
          >
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <!-- <el-form-item label="创建时间">
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
          <span class="table-title">用户列表</span>
          <div class="table-actions">
            <el-button text @click="refreshTable" :loading="loading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        :data="formattedUsers"
        style="width: 100%"
        row-key="id"
        v-loading="loading"
        stripe
        border
      >
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="150" />
        <el-table-column prop="roleDesc" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 0 ? 'danger' : 'primary'" size="small">
              {{ row.roleDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'info'"
              size="small"
            >
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTimeFormatted" label="创建时间" width="180" />
        <el-table-column prop="updateTimeFormatted" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row.id)">
              详情
            </el-button>
            <el-button type="warning" link size="small" @click="handleEdit(row.id)">
              编辑
            </el-button>
            <el-button
              type="success"
              link
              size="small"
              v-if="row.status === 0"
              @click="handleEnable(row.id)"
            >
              启用
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              v-if="row.status === 1"
              @click="handleDisable(row.id)"
            >
              禁用
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 批量操作 -->
      <div class="batch-actions" v-if="selectedRows.length > 0">
        <el-button type="danger" size="small" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <span class="selected-count">已选择 {{ selectedRows.length }} 项</span>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageInfo.pageNum"
          v-model:page-size="pageInfo.pageSize"
          :total="total"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useWebUserStore } from '@/stores/settings'
import type { WebUserQueryParams } from '@/api/settings'

const router = useRouter()
const userStore = useWebUserStore()

// 筛选表单
const filterForm = reactive({
  username: '',
  realName: '',
  role: undefined as number | undefined,
  status: undefined as number | undefined,
  dateRange: [] as string[]
})

// 选择的行
const selectedRows = ref<any[]>([])

// 获取store状态
const { userList, total, loading, pageInfo, formattedUserList } = userStore

// 格式化后的用户列表
const formattedUsers = computed(() => formattedUserList())

// 选择变化
// const handleSelectionChange = (val: any[]) => {
//   selectedRows.value = val
// }

// 加载数据
const loadData = async () => {
  const params: WebUserQueryParams = {
    pageNum: pageInfo.pageNum,
    pageSize: pageInfo.pageSize,
    username: filterForm.username || undefined,
    realName: filterForm.realName || undefined,
    role: filterForm.role,
    status: filterForm.status
  }

  // 处理日期范围（如果需要）
  // if (filterForm.dateRange?.length === 2) {
  //   params.startDate = filterForm.dateRange[0]
  //   params.endDate = filterForm.dateRange[1]
  // }

  await userStore.fetchUserList(params)
}

// 搜索
const handleSearch = () => {
  pageInfo.pageNum = 1
  loadData()
}

// 重置
const handleReset = () => {
  filterForm.username = ''
  filterForm.realName = ''
  filterForm.role = undefined
  filterForm.status = undefined
  filterForm.dateRange = []
  pageInfo.pageNum = 1
  loadData()
}

// 创建用户
const handleCreateUser = () => {
  router.push('/settings/webUser/add')
}

// 查看详情
const handleViewDetail = (id: number) => {
  router.push(`/settings/webUser/detail/${id}`)
}

// 编辑用户
const handleEdit = (id: number) => {
  router.push(`/settings/webUser/edit/${id}`)
}

// 启用用户
const handleEnable = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要启用该用户吗？', '提示', {
      type: 'warning'
    })
    
    await userStore.updateUserStatus(id, 1)
    ElMessage.success('启用成功')
    loadData()
  } catch (error) {
    // 用户取消
  }
}

// 禁用用户
const handleDisable = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要禁用该用户吗？', '提示', {
      type: 'warning'
    })
    
    await userStore.updateUserStatus(id, 0)
    ElMessage.success('禁用成功')
    loadData()
  } catch (error) {
    // 用户取消
  }
}

// 重置密码
// const handleResetPassword = async (id: number) => {
//   try {
//     await userStore.resetPassword(id)
//     ElMessage.success('密码已重置为123456')
//   } catch (error) {
//     console.error('重置密码失败:', error)
//     ElMessage.error('重置密码失败')
//   }
// }

// 删除用户
const handleDelete = async (id: number) => {
  try {
    await userStore.deleteUser(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的用户')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 个用户吗？`,
      '警告',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消'
      }
    )

    // 批量删除接口暂未实现，这里逐个删除
    for (const row of selectedRows.value) {
      await userStore.deleteUser(row.id)
    }
    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadData()
  } catch (error) {
    // 用户取消
  }
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
  ElMessage.success('刷新成功')
}

// 初始化加载
onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.user-list-container {
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