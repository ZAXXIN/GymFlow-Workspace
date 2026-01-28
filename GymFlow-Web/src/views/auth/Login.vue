<template>
  <div class="login-container">
    <div class="login-box">
      <!-- Logo和标题 -->
      <div class="logo-container">
        <img src="@/assets/images/logo.png" alt="GymFlow" class="logo">
        <h1 class="logo-title">GymFlow 健身房管理系统</h1>
        <p class="logo-subtitle">专业健身房管理解决方案</p>
      </div>

      <!-- 登录表单 -->
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" size="large" @keyup.enter="handleLogin" />
        </el-form-item>

        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>

        <!-- 登录按钮 -->
        <el-button type="primary" size="large" :loading="loading" @click="handleLogin" class="login-button">
          {{ loading ? '登录中...' : '登录' }}
        </el-button>
      </el-form>

      <!-- 测试账号提示 -->
      <div class="test-accounts">
        <el-row :gutter="20">
          <el-col :span="12" v-for="account in testAccounts" :key="account.username">
            <el-card shadow="hover" class="account-card">
              <template #header>
                <div class="account-header">
                  <el-tag :type="account.roleType" size="small">{{ account.roleName }}</el-tag>
                </div>
              </template>
              <div class="account-content">
                <p><strong>用户名：</strong>{{ account.username }}</p>
                <p><strong>密码：</strong>{{ account.password }}</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStores = useAuthStore()
const loginFormRef = ref<FormInstance>()

// 登录表单数据
const loginForm = reactive({
  username: '',
  password: '',
})

// 登录表单验证规则
const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// 测试账号
const testAccounts = ref([
  {
    username: 'admin',
    password: '123456',
    roleName: '管理员',
    roleType: 'danger',
  },
  {
    username: 'front1',
    password: '123456',
    roleName: '前台',
    roleType: 'warning',
  },
])

// 加载状态
const loading = ref(false)

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    // 调用登录接口
    const response = await authStores.login({
      username: loginForm.username,
      password: loginForm.password,
    })

    if (response.code === 200 && response.data) {
      // 存储token和用户信息
      const { token, ...userInfo } = response.data

      // 存储到localStorage
      localStorage.setItem('gymflow_token', token)
      localStorage.setItem('gymflow_user_info', JSON.stringify(userInfo))

      ElMessage.success('登录成功')

      router.replace('/dashboard');
    } else {
      ElMessage.error(response.message || '登录失败')
    }
  } catch (error: any) {
    // 这里会捕获到拦截器中的错误
    if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('登录失败')
    }
  } finally {
    loading.value = false
  }
}

// 页面加载时，如果已登录则跳转
onMounted(() => {
  const token = localStorage.getItem('gymflow_token')
  if (token) {
    router.push('/dashboard')
  }
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 520px;
  padding: 40px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}

.logo-container {
  text-align: center;
  margin-bottom: 30px;
}

.logo {
  width: 80px;
  height: 80px;
  margin: auto;
  margin-bottom: 15px;
}

.logo-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.logo-subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: #666;
}

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 6px;
  }
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 6px;
  margin-top: 10px;
}

.test-accounts {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.test-accounts h3 {
  margin-bottom: 15px;
  font-size: 16px;
  color: #666;
  text-align: center;
}

.account-card {
  margin-bottom: 10px;
}

.account-header {
  text-align: center;
}

.account-content {
  font-size: 13px;
}

.account-content p {
  margin: 5px 0;
}

.account-content strong {
  color: #333;
}
</style>