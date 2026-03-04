<template>
  <div class="webuser-form-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/settings/webUser' }">用户管理</el-breadcrumb-item>
          <el-breadcrumb-item>{{ isEditMode ? '编辑用户' : '新增用户' }}</el-breadcrumb-item>
        </el-breadcrumb>
        <h1 class="page-title">{{ isEditMode ? '编辑用户信息' : '新增用户' }}</h1>
      </div>
      <div class="header-actions">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEditMode ? '更新' : '创建' }}
        </el-button>
      </div>
    </div>

    <!-- 主表单区域 -->
    <div class="form-content">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" class="webuser-form" status-icon>
        <!-- 基本信息 -->
        <el-card shadow="never" class="form-section">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本信息</span>
            </div>
          </template>

          <div class="form-row">
            <el-form-item label="用户名" prop="username" class="form-item">
              <el-input v-model="formData.username" placeholder="请输入用户名" maxlength="50" clearable @blur="checkUsernameAvailability" :disabled="isEditMode" />
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

            <el-form-item label="真实姓名" prop="realName" class="form-item">
              <el-input v-model="formData.realName" placeholder="请输入真实姓名" maxlength="50" clearable />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="角色" prop="role" class="form-item">
              <el-select v-model="formData.role" placeholder="请选择角色" style="width: 100%">
                <el-option v-for="item in roleOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>

            <el-form-item label="状态" prop="status" class="form-item">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>

          <!-- 新增模式：密码字段 -->
          <template v-if="!isEditMode">
            <div class="form-row">
              <el-form-item label="密码" prop="password" class="form-item">
                <el-input v-model="formData.password" type="password" placeholder="请输入密码" maxlength="255" clearable show-password autocomplete="new-password" />
              </el-form-item>

              <el-form-item label="确认密码" prop="confirmPassword" class="form-item">
                <el-input v-model="confirmPassword" type="password" placeholder="请确认密码" maxlength="255" clearable show-password autocomplete="new-password" />
              </el-form-item>
            </div>
            <div class="form-row hint-row">
              <el-alert type="info" :closable="false" show-icon>
                <span class="hint-text">密码将使用BCrypt加密存储，长度至少6位</span>
              </el-alert>
            </div>
          </template>

          <!-- 编辑模式：修改密码选项 -->
          <template v-else>
            <div class="form-row">
              <el-form-item label="修改密码" prop="changePassword" class="form-item">
                <el-checkbox v-model="formData.changePassword" label="修改密码" />
              </el-form-item>
            </div>

            <template v-if="formData.changePassword">
              <div class="form-row">
                <el-form-item label="新密码" prop="newPassword" class="form-item">
                  <el-input v-model="formData.newPassword" type="password" placeholder="请输入新密码" maxlength="255" clearable show-password autocomplete="new-password" />
                </el-form-item>

                <el-form-item label="确认新密码" prop="confirmNewPassword" class="form-item">
                  <el-input v-model="confirmNewPassword" type="password" placeholder="请确认新密码" maxlength="255" clearable show-password autocomplete="new-password" />
                </el-form-item>
              </div>
              <div class="form-row hint-row">
                <el-alert type="warning" :closable="false" show-icon>
                  <span class="hint-text">新密码将使用BCrypt加密存储，长度至少6位</span>
                </el-alert>
              </div>
            </template>
          </template>
        </el-card>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useWebUserStore } from '@/stores/settings/webUser'

const router = useRouter()
const route = useRoute()
const webUserStore = useWebUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const isEditMode = computed(() => !!route.params.id)

// 表单数据
interface WebUserForm {
  username: string
  realName: string
  password?: string
  role: number
  status: number
  changePassword?: boolean
  newPassword?: string
}

const formData = reactive<WebUserForm>({
  username: '',
  realName: '',
  password: '',
  role: 1, // 默认选择前台
  status: 1,
  changePassword: false,
  newPassword: '',
})

const confirmPassword = ref('')
const confirmNewPassword = ref('')

// 用户名检查
const usernameChecking = ref(false)
const usernameAvailable = ref<boolean | null>(null)

// 角色选项 - 只有老板和前台
const roleOptions = [
  { value: 0, label: '老板' },
  { value: 1, label: '前台' },
]

// 表单验证规则
const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在3-50个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' },
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '真实姓名长度在2-50个字符', trigger: 'blur' },
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
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
  confirmPassword: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if (!isEditMode.value) {
          if (!value) {
            callback(new Error('请确认密码'))
          } else if (value !== formData.password) {
            callback(new Error('两次输入的密码不一致'))
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
        if (isEditMode.value && formData.changePassword) {
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
  confirmNewPassword: [
    {
      validator: (rule: any, value: string, callback: any) => {
        if (isEditMode.value && formData.changePassword) {
          if (!value) {
            callback(new Error('请确认新密码'))
          } else if (value !== formData.newPassword) {
            callback(new Error('两次输入的密码不一致'))
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
}

// 检查用户名是否可用
const checkUsernameAvailability = async () => {
  const username = formData.username
  if (!username || username.length < 3) {
    usernameAvailable.value = null
    return
  }

  usernameChecking.value = true
  try {
    // 调用API检查用户名是否存在 - 修正方法名
    const exists = await webUserStore.checkUsernameExists(
      username,
      isEditMode.value ? Number(route.params.id) : undefined
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

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    // 新增模式检查用户名可用性
    if (!isEditMode.value) {
      if (usernameAvailable.value === false) {
        ElMessage.warning('用户名已存在，请更换用户名')
        return
      }
      // 如果没有主动检查过，立即检查一次
      if (usernameAvailable.value === null) {
        await checkUsernameAvailability()
        if (usernameAvailable.value === false) {
          ElMessage.warning('用户名已存在，请更换用户名')
          return
        }
      }
    }

    loading.value = true

    // 准备提交数据
    const submitData: any = {
      username: formData.username,
      realName: formData.realName,
      role: formData.role,
      status: formData.status,
    }

    if (isEditMode.value) {
      // 编辑模式
      if (formData.changePassword) {
        submitData.changePassword = true
        submitData.newPassword = formData.newPassword
      } else {
        submitData.changePassword = false
      }
      await webUserStore.updateUser(Number(route.params.id), submitData)
      ElMessage.success('用户信息更新成功')
    } else {
      // 新增模式
      submitData.password = formData.password
      await webUserStore.addUser(submitData)
      ElMessage.success('用户创建成功')
    }

    router.push('/settings/webUser')
  } catch (error: any) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败，请检查表单')
  } finally {
    loading.value = false
  }
}

// 取消
const handleCancel = () => {
  ElMessageBox.confirm('确定要取消吗？未保存的内容将丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      router.push('/settings/webUser')
    })
    .catch(() => {})
}

// 初始化表单数据（编辑模式）
const initFormData = async () => {
  if (!isEditMode.value) return

  try {
    loading.value = true
    const userId = Number(route.params.id)
    // 修正方法名：fetchUserDetail 而不是 fetchWebUserDetail
    const user = await webUserStore.fetchUserDetail(userId)

    formData.username = user.username
    formData.realName = user.realName
    formData.role = user.role
    formData.status = user.status
    formData.changePassword = false
    formData.newPassword = ''
    confirmNewPassword.value = ''
  } catch (error: any) {
    console.error('加载用户详情失败:', error)
    ElMessage.error(error.message || '加载用户信息失败')
    // 加载失败后返回列表页
    router.push('/settings/webUser')
  } finally {
    loading.value = false
  }
}

// 重置修改密码相关字段
watch(
  () => formData.changePassword,
  (newVal) => {
    if (!newVal) {
      formData.newPassword = ''
      confirmNewPassword.value = ''
    }
  }
)

// 监听用户名变化，重置可用性状态
watch(
  () => formData.username,
  () => {
    if (!isEditMode.value) {
      usernameAvailable.value = null
    }
  }
)

onMounted(() => {
  initFormData()
})
</script>

<style scoped>
.webuser-form-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left .page-title {
  margin: 10px 0 0 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  margin-top: 10px;
}

.form-content {
  background-color: white;
  border-radius: 4px;
  padding: 20px;
}

.form-section {
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.form-item {
  flex: 1;
  margin-bottom: 0;
}

.hint-row {
  margin-top: -10px;
  margin-bottom: 10px;
}

.hint-text {
  font-size: 12px;
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
  background-color: #fafafa;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-radio-group) {
  width: 100%;
  display: flex;
  align-items: center;
  height: 32px;
}

:deep(.el-alert) {
  padding: 8px 12px;
  width: 100%;
}
</style>