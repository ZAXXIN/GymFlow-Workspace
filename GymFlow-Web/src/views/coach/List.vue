<template>
  <div class="coach-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">教练管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>教练管理</el-breadcrumb-item>
          <el-breadcrumb-item>教练列表</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAddCoach">
          <el-icon><Plus /></el-icon>
          新增教练
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
        <el-form-item label="教练编号">
          <el-input
            v-model="filterForm.coachNo"
            placeholder="请输入教练编号"
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
        <el-form-item label="专业领域">
          <el-input
            v-model="filterForm.specialization"
            placeholder="请输入专业领域"
            clearable
            style="width: 180px;"
          />
        </el-form-item>
        <el-form-item label="教练状态">
          <el-select
            v-model="filterForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 180px;"
          >
            <el-option
              v-for="item in coachStatusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
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
          <span class="table-title">教练列表</span>
          <div class="table-actions">
            <el-button text @click="refreshTable">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <app-table
        :data="coachList"
        :total="pagination.total"
        :show-add="false"
        :show-export="false"
        :page="pagination.current"
        :page-size="pagination.size"
        :show-selection="false"
        @add="handleAddCoach"
        @refresh="refreshTable"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="coachNo" label="教练编号" width="120" sortable />
        <el-table-column prop="realName" label="姓名" width="120">
          <template #default="{ row }">
            <div class="coach-info">
              <el-avatar :size="32" :src="row.user?.avatar" class="coach-avatar">
                {{ row.realName?.charAt(0) || 'C' }}
              </el-avatar>
              <span class="coach-name">{{ row.realName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="specialization" label="专业领域" width="150" />
        <el-table-column prop="yearsOfExperience" label="经验年限" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">
              {{ row.yearsOfExperience }}年
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="hourlyRate" label="时薪" width="100" align="center">
          <template #default="{ row }">
            ¥{{ row.hourlyRate }}
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="100" align="center">
          <template #default="{ row }">
            <div class="rating-cell">
              <el-rate
                v-model="row.rating"
                disabled
                show-score
                text-color="#ff9900"
                score-template="{value}"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalSessions" label="总课程" width="100" align="center" />
        <el-table-column prop="totalSales" label="总销售额" width="120" align="center">
          <template #default="{ row }">
            ¥{{ row.totalSales?.toFixed(2) }}
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
                <el-dropdown-item command="courses">
                  <el-icon><Calendar /></el-icon>
                  课程安排
                </el-dropdown-item>
                <el-dropdown-item command="members">
                  <el-icon><User /></el-icon>
                  学员列表
                </el-dropdown-item>
                <el-dropdown-item command="performance">
                  <el-icon><TrendCharts /></el-icon>
                  业绩统计
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
    
    <!-- 新增/编辑表单 -->
    <el-dialog
      v-model="formDialog.visible"
      :title="formDialog.title"
      width="600px"
    >
      <el-form
        ref="coachFormRef"
        :model="coachForm"
        :rules="coachRules"
        label-width="100px"
      >
        <el-tabs v-model="activeTab">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="coachForm.username"
                placeholder="请输入用户名"
                :disabled="formDialog.mode === 'edit'"
              />
            </el-form-item>
            
            <el-form-item v-if="formDialog.mode === 'create'" label="密码" prop="password">
              <el-input
                v-model="coachForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
              />
            </el-form-item>
            
            <el-form-item label="姓名" prop="realName">
              <el-input
                v-model="coachForm.realName"
                placeholder="请输入真实姓名"
              />
            </el-form-item>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="性别" prop="gender">
                  <el-radio-group v-model="coachForm.gender">
                    <el-radio :label="1">男</el-radio>
                    <el-radio :label="0">女</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="出生日期" prop="birthDate">
                  <el-date-picker
                    v-model="coachForm.birthDate"
                    type="date"
                    placeholder="选择出生日期"
                    style="width: 100%;"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-form-item label="邮箱" prop="email">
              <el-input
                v-model="coachForm.email"
                placeholder="请输入邮箱"
                type="email"
              />
            </el-form-item>
            
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="coachForm.phone"
                placeholder="请输入手机号"
              />
            </el-form-item>
          </el-tab-pane>
          
          <!-- 专业信息 -->
          <el-tab-pane label="专业信息" name="professional">
            <el-form-item label="专业领域" prop="specialization">
              <el-input
                v-model="coachForm.specialization"
                placeholder="请输入专业领域，如：瑜伽、力量训练"
              />
            </el-form-item>
            
            <el-form-item label="资格证书" prop="certification">
              <el-input
                v-model="coachForm.certification"
                type="textarea"
                :rows="3"
                placeholder="请输入资格证书信息"
              />
            </el-form-item>
            
            <el-form-item label="从业年限" prop="yearsOfExperience">
              <el-input-number
                v-model="coachForm.yearsOfExperience"
                :min="0"
                :max="50"
                :step="1"
                controls-position="right"
                style="width: 100%;"
              />
            </el-form-item>
            
            <el-form-item label="教练状态" prop="status">
              <el-select
                v-model="coachForm.status"
                placeholder="请选择教练状态"
                style="width: 100%;"
              >
                <el-option
                  v-for="item in coachStatusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-tab-pane>
          
          <!-- 薪酬信息 -->
          <el-tab-pane label="薪酬信息" name="salary">
            <el-form-item label="时薪" prop="hourlyRate">
              <el-input-number
                v-model="coachForm.hourlyRate"
                :min="0"
                :step="10"
                controls-position="right"
                style="width: 100%;"
              >
                <template #prefix>¥</template>
              </el-input-number>
            </el-form-item>
            
            <el-form-item label="佣金比例" prop="commissionRate">
              <el-input-number
                v-model="coachForm.commissionRate"
                :min="0"
                :max="100"
                :step="1"
                controls-position="right"
                style="width: 100%;"
              >
                <template #suffix>%</template>
              </el-input-number>
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="formDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="handleFormSubmit">
            确定
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
import { useCoachStore } from '@/stores/coach'
import { coachStatusOptions } from '@/utils/constants'
import { validatePhone, validateEmail } from '@/utils'
import type { Coach, CoachStatus, QueryParams } from '@/types'

// Store
const coachStore = useCoachStore()

// Router
const router = useRouter()

// Refs
const coachFormRef = ref<FormInstance>()
const loading = ref(false)
const activeTab = ref('basic')

// 状态
const filterForm = reactive({
  coachNo: '',
  realName: '',
  specialization: '',
  status: '' as CoachStatus | ''
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
  title: '新增教练',
  coachId: 0
})

const coachForm = reactive({
  // 账户信息
  username: '',
  password: '',
  email: '',
  phone: '',
  
  // 个人信息
  realName: '',
  gender: 1,
  birthDate: '',
  
  // 专业信息
  specialization: '',
  certification: '',
  yearsOfExperience: 0,
  status: 'ACTIVE' as CoachStatus,
  
  // 薪酬信息
  hourlyRate: 100,
  commissionRate: 10
})

// Form Rules
const coachRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { validator: validateEmailRule, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: validatePhoneRule, trigger: 'blur' }
  ],
  specialization: [
    { required: true, message: '请输入专业领域', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择教练状态', trigger: 'change' }
  ],
  hourlyRate: [
    { required: true, message: '请输入时薪', trigger: 'blur' }
  ]
}

function validateEmailRule(rule: any, value: string, callback: any) {
  if (!value) {
    callback()
    return
  }
  if (!validateEmail(value)) {
    callback(new Error('请输入有效的邮箱地址'))
  } else {
    callback()
  }
}

function validatePhoneRule(rule: any, value: string, callback: any) {
  if (!value) {
    callback()
    return
  }
  if (!validatePhone(value)) {
    callback(new Error('请输入有效的手机号码'))
  } else {
    callback()
  }
}

// Computed
const coachList = computed(() => coachStore.coaches)

// Methods
const loadCoaches = async () => {
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
    
    await coachStore.fetchCoaches(params)
    pagination.total = coachStore.total
  } catch (error) {
    console.error('加载教练列表失败:', error)
    ElMessage.error('加载教练列表失败')
  } finally {
    loading.value = false
  }
}

const buildFilterParams = () => {
  const params: Record<string, any> = {}
  
  if (filterForm.coachNo) {
    params.coachNo = filterForm.coachNo
  }
  
  if (filterForm.realName) {
    params.realName = filterForm.realName
  }
  
  if (filterForm.specialization) {
    params.specialization = filterForm.specialization
  }
  
  if (filterForm.status) {
    params.status = filterForm.status
  }
  
  return params
}

const handleSearch = () => {
  pagination.current = 1
  loadCoaches()
}

const handleReset = () => {
  filterForm.coachNo = ''
  filterForm.realName = ''
  filterForm.specialization = ''
  filterForm.status = ''
  
  pagination.current = 1
  loadCoaches()
}

const handleSortChange = ({ prop, order }: { prop: string; order: 'ascending' | 'descending' | null }) => {
  sortParams.sortBy = prop
  sortParams.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  loadCoaches()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.current = 1
  loadCoaches()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadCoaches()
}

const refreshTable = () => {
  loadCoaches()
}

const handleAddCoach = () => {
  formDialog.mode = 'create'
  formDialog.title = '新增教练'
  formDialog.coachId = 0
  resetCoachForm()
  formDialog.visible = true
}

const handleEdit = (id: number) => {
  formDialog.mode = 'edit'
  formDialog.title = '编辑教练'
  formDialog.coachId = id
  loadCoachData(id)
  formDialog.visible = true
}

const handleViewDetail = (id: number) => {
  router.push(`/coaches/${id}`)
}

const handleMoreAction = async (command: string, row: Coach) => {
  switch (command) {
    case 'courses':
      router.push(`/courses?coachId=${row.id}`)
      break
    case 'members':
      // 跳转到学员管理页面，筛选该教练的学员
      router.push(`/members?coachId=${row.id}`)
      break
    case 'performance':
      // 跳转到业绩统计页面
      router.push(`/reports/performance?coachId=${row.id}`)
      break
    case 'delete':
      handleDelete(row.id)
      break
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除该教练吗？此操作不可恢复。', '删除教练', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    
    await coachStore.deleteCoach(id)
    ElMessage.success('删除成功')
    loadCoaches()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

const handleExport = async () => {
  try {
    loading.value = true
    const params = buildFilterParams()
    await coachStore.exportCoaches(params)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status: CoachStatus) => {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'INACTIVE':
      return 'info'
    case 'ON_LEAVE':
      return 'warning'
    default:
      return 'info'
  }
}

const getStatusText = (status: CoachStatus) => {
  switch (status) {
    case 'ACTIVE':
      return '在职'
    case 'INACTIVE':
      return '离职'
    case 'ON_LEAVE':
      return '休假'
    default:
      return '未知'
  }
}

const resetCoachForm = () => {
  Object.assign(coachForm, {
    username: '',
    password: '',
    email: '',
    phone: '',
    realName: '',
    gender: 1,
    birthDate: '',
    specialization: '',
    certification: '',
    yearsOfExperience: 0,
    status: 'ACTIVE',
    hourlyRate: 100,
    commissionRate: 10
  })
  activeTab.value = 'basic'
}

const loadCoachData = async (id: number) => {
  try {
    const coach = await coachStore.fetchCoachById(id)
    
    // 填充表单数据
    Object.keys(coachForm).forEach(key => {
      if (key in coach) {
        coachForm[key as keyof typeof coachForm] = coach[key]
      }
    })
    
    // 设置用户信息
    if (coach.user) {
      coachForm.username = coach.user.username
      coachForm.email = coach.user.email
      coachForm.phone = coach.user.phone
    }
    
    // 清空密码字段
    coachForm.password = ''
  } catch (error) {
    console.error('加载教练数据失败:', error)
    ElMessage.error('加载教练数据失败')
  }
}

const handleFormSubmit = async () => {
  if (!coachFormRef.value) return
  
  try {
    await coachFormRef.value.validate()
    loading.value = true
    
    if (formDialog.mode === 'create') {
      await coachStore.createCoach(coachForm)
      ElMessage.success('创建成功')
    } else {
      await coachStore.updateCoach(formDialog.coachId, coachForm)
      ElMessage.success('更新成功')
    }
    
    formDialog.visible = false
    loadCoaches()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadCoaches()
})
</script>

<style lang="scss" scoped>
.coach-list-container {
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
    
    .coach-info {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .coach-avatar {
        background: var(--gymflow-secondary);
        color: white;
      }
      
      .coach-name {
        font-weight: 500;
      }
    }
    
    .rating-cell {
      :deep(.el-rate) {
        display: inline-block;
      }
    }
  }
}

// 对话框样式
:deep(.el-dialog) {
  border-radius: 12px;
  
  .el-dialog__header {
    padding: 20px;
    border-bottom: 1px solid var(--gymflow-border);
  }
  
  .el-dialog__body {
    padding: 20px;
    
    .el-tabs {
      :deep(.el-tabs__content) {
        padding: 20px 0 0;
      }
    }
  }
  
  .el-dialog__footer {
    padding: 20px;
    border-top: 1px solid var(--gymflow-border);
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
  .coach-list-container {
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
        .el-select {
          width: 100%;
        }
      }
    }
  }
}
</style>