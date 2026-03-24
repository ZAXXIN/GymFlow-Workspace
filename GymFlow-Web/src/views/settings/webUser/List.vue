<template>
  <div class="user-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">用户管理</h1>
      </div>
      <div class="header-right">
        <el-button v-permission="'settings:user:add'" type="primary" @click="handleCreateUser">
          <el-icon>
            <Plus />
          </el-icon>
          新增用户
        </el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="用户名">
          <el-input v-model="filterForm.username" placeholder="请输入用户名" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="filterForm.role" placeholder="请选择角色" clearable style="width: 180px;">
            <el-option label="老板" :value="1" />
            <el-option label="前台" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable style="width: 180px;">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
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

    <!-- 数据表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <span class="table-title">用户列表</span>
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

      <el-table :data="formattedUsers" style="width: 100%" row-key="id" v-loading="loading" stripe border>
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="roleDesc" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : 'primary'" size="small">
              {{ row.roleDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTimeFormatted" label="创建时间" width="180" />
        <el-table-column prop="updateTimeFormatted" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-permission="'settings:user:edit'" type="warning" link size="small" @click="handleEdit(row.id)">
              编辑
            </el-button>
            <el-button type="success" link size="small" v-if="row.status === 0" v-permission="'settings:user:status'" @click="handleEnable(row.id)">
              启用
            </el-button>
            <el-button type="danger" link size="small" v-if="row.status === 1" v-permission="'settings:user:status'" @click="handleDisable(row.id)">
              禁用
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pageInfo.pageNum" v-model:page-size="pageInfo.pageSize" :total="total" :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange" @current-change="handleCurrentChange" :disabled="loading" />
      </div>
    </el-card>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close @close="handleDialogClose">
      <el-form ref="dialogFormRef" :model="dialogFormData" :rules="dialogFormRules" label-width="100px" status-icon>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="dialogFormData.username" placeholder="请输入用户名" maxlength="50" clearable @blur="checkUsernameAvailability" :disabled="isEditMode" />
          <div v-if="usernameChecking" class="checking-text">
            <el-icon class="is-loading">
              <Loading />
            </el-icon> 检查中...
          </div>
          <div v-if="usernameAvailable !== null && !isEditMode" :class="['check-result', usernameAvailable ? 'success' : 'error']">
            <el-icon v-if="usernameAvailable">
              <SuccessFilled />
            </el-icon>
            <el-icon v-else>
              <WarningFilled />
            </el-icon>
            {{ usernameAvailable ? '用户名可用' : '用户名已存在' }}
          </div>
        </el-form-item>

        <!-- 新增模式：密码字段 -->
        <template v-if="!isEditMode">
          <el-form-item label="密码" prop="password">
            <el-input v-model="dialogFormData.password" type="password" placeholder="请输入密码" maxlength="255" clearable show-password autocomplete="new-password" />
          </el-form-item>
        </template>

        <!-- 编辑模式：修改密码选项 -->
        <template v-else>
          <el-form-item label="修改密码" prop="changePassword">
            <el-checkbox v-model="dialogFormData.changePassword" label="修改密码" />
          </el-form-item>

          <template v-if="dialogFormData.changePassword">
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="dialogFormData.newPassword" type="password" placeholder="请输入新密码" maxlength="255" clearable show-password autocomplete="new-password" />
            </el-form-item>
          </template>
        </template>

        <el-form-item label="角色" prop="role">
          <el-select v-model="dialogFormData.role" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dialogFormData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleDialogSubmit" :loading="dialogLoading">
            {{ isEditMode ? '更新' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useWebUserStore } from '@/stores/settings/webUser'
import type { WebUserQueryParams } from '@/api/settings/webUser'
import { usePermission } from '@/composables/usePermission'

const { hasPermission } = usePermission()
const router = useRouter()
const userStore = useWebUserStore()

// 筛选表单
const filterForm = reactive({
  username: '',
  realName: '',
  role: undefined as number | undefined,
  status: undefined as number | undefined,
  dateRange: [] as string[],
})

// 选择的行
const selectedRows = ref<any[]>([])

// 获取store状态
const { userList, total, loading, pageInfo, formattedUserList } = userStore

// 格式化后的用户列表
const formattedUsers = computed(() => formattedUserList())

// ========== 弹窗相关 ==========
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const dialogFormRef = ref<any>()
const isEditMode = ref(false)
const editingUserId = ref<number | null>(null)

// 弹窗表单数据
interface DialogFormData {
  username: string
  password?: string
  role: number
  status: number
  changePassword?: boolean
  newPassword?: string
}

const dialogFormData = reactive<DialogFormData>({
  username: '',
  password: '',
  role: 1,
  status: 1,
  changePassword: false,
  newPassword: '',
})

// 用户名检查
const usernameChecking = ref(false)
const usernameAvailable = ref<boolean | null>(null)

// 角色选项
const roleOptions = [
  { value: 1, label: '老板' },
  { value: 2, label: '前台' },
]

// 弹窗标题
const dialogTitle = computed(() => isEditMode.value ? '编辑用户' : '新增用户')

// 弹窗表单验证规则
const dialogFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' },
  ],
  password: [
    {
      validator: (rule: any, value: string | undefined, callback: any) => {
        if (!isEditMode.value) {
          if (!value) {
            callback(new Error('请输入密码'))
          } else if (value.length < 6) {
            callback(new Error('密码长度至少6个字符'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  newPassword: [
    {
      validator: (rule: any, value: string | undefined, callback: any) => {
        if (isEditMode.value && dialogFormData.changePassword) {
          if (!value) {
            callback(new Error('请输入新密码'))
          } else if (value.length < 6) {
            callback(new Error('密码长度至少6个字符'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

// 检查用户名是否可用
const checkUsernameAvailability = async () => {
  const username = dialogFormData.username
  if (!username || username.length < 3) {
    usernameAvailable.value = null
    return
  }

  usernameChecking.value = true
  try {
    const exists = await userStore.checkUsernameExists(
      username,
      isEditMode.value ? editingUserId.value : undefined
    )
    usernameAvailable.value = !exists
  } catch (error) {
    console.error('检查用户名失败:', error)
    usernameAvailable.value = null
    ElMessage.error('检查用户名失败')
  } finally {
    usernameChecking.value = false
  }
}

// 重置弹窗表单
const resetDialogForm = () => {
  dialogFormData.username = ''
  dialogFormData.password = ''
  dialogFormData.role = 1
  dialogFormData.status = 1
  dialogFormData.changePassword = false
  dialogFormData.newPassword = ''
  usernameAvailable.value = null
  usernameChecking.value = false
  editingUserId.value = null
  if (dialogFormRef.value) {
    dialogFormRef.value.clearValidate()
  }
}

// 打开新增用户弹窗
const handleCreateUser = () => {
  isEditMode.value = false
  editingUserId.value = null
  resetDialogForm()
  dialogVisible.value = true
}

// 打开编辑用户弹窗
const handleEdit = async (id: number) => {
  isEditMode.value = true
  editingUserId.value = id
  dialogVisible.value = true
  dialogLoading.value = true

  try {
    const user = await userStore.fetchUserDetail(id)
    dialogFormData.username = user.username
    dialogFormData.role = user.role
    dialogFormData.status = user.status
    dialogFormData.changePassword = false
    dialogFormData.newPassword = ''
  } catch (error: any) {
    console.error('加载用户详情失败:', error)
    ElMessage.error(error.message || '加载用户信息失败')
    dialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

// 提交弹窗表单
const handleDialogSubmit = async () => {
  if (!dialogFormRef.value) return

  try {
    await dialogFormRef.value.validate()

    // 新增模式检查用户名可用性
    if (!isEditMode.value) {
      if (usernameAvailable.value === false) {
        ElMessage.warning('用户名已存在，请更换用户名')
        return
      }
      if (usernameAvailable.value === null) {
        await checkUsernameAvailability()
        if (usernameAvailable.value === false) {
          ElMessage.warning('用户名已存在，请更换用户名')
          return
        }
      }
    }

    dialogLoading.value = true

    const submitData: any = {
      username: dialogFormData.username,
      role: dialogFormData.role,
      status: dialogFormData.status,
    }

    if (isEditMode.value) {
      // 编辑模式
      if (dialogFormData.changePassword) {
        submitData.changePassword = true
        submitData.newPassword = dialogFormData.newPassword
      } else {
        submitData.changePassword = false
      }
      await userStore.updateUser(editingUserId.value!, submitData)
      ElMessage.success('用户信息更新成功')
    } else {
      // 新增模式
      submitData.password = dialogFormData.password
      await userStore.addUser(submitData)
      ElMessage.success('用户创建成功')
    }

    dialogVisible.value = false
    loadData() // 刷新列表
  } catch (error: any) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败，请检查表单')
  } finally {
    dialogLoading.value = false
  }
}

// 弹窗关闭时重置
const handleDialogClose = () => {
  resetDialogForm()
}

// ========== 列表相关方法 ==========
// 加载数据
const loadData = async () => {
  const params: WebUserQueryParams = {
    pageNum: pageInfo.pageNum,
    pageSize: pageInfo.pageSize,
    username: filterForm.username || undefined,
    realName: filterForm.realName || undefined,
    role: filterForm.role,
    status: filterForm.status,
  }

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

// 启用用户
const handleEnable = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要启用该用户吗？', '提示', {
      type: 'warning',
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
      type: 'warning',
    })

    await userStore.updateUserStatus(id, 0)
    ElMessage.success('禁用成功')
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

.checking-text {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.check-result {
  font-size: 12px;
  margin-top: 5px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.check-result.success {
  color: #67c23a;
}

.check-result.error {
  color: #f56c6c;
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

:deep(.el-radio-group) {
  width: 100%;
  display: flex;
  align-items: center;
  height: 32px;
}
</style>