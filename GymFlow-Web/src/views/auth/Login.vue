<template>
  <div class="login-container">
    <!-- 全局背景装饰（和小程序一致） -->
    <div class="global-bg">
      <div class="global-bg-circle global-bg-circle-1"></div>
      <div class="global-bg-circle global-bg-circle-2"></div>
    </div>
    
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
        <el-button type="success" size="large" :loading="loading" @click="handleLogin" class="login-button">
          {{ loading ? '登录中...' : '登录' }}
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
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

// 加载状态
const loading = ref(false)

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    // 调用登录接口
    const response = await authStore.login({
      username: loginForm.username,
      password: loginForm.password,
    })

    if (response.success) {
      router.replace('/dashboard')
    }
  } catch (error: any) {
    // 错误已在 auth store 中处理
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

// 页面加载时，如果已登录则跳转
onMounted(() => {
  if (authStore.isLoggedIn) {
    router.replace('/dashboard')
  }
})
</script>

<style scoped>
/* 全局背景渐变 - 和小程序保持一致 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  /* 背景渐变 - 小程序风格 */
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
  position: relative;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

/* 全局背景装饰组件 - 复制自小程序 */
.global-bg {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: 1;
  pointer-events: none;
}

.global-bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(7, 193, 96, 0.1);
}

.global-bg-circle-1 {
  width: 600px;
  height: 600px;
  top: -200px;
  right: -200px;
  background: rgba(7, 193, 96, 0.1);
}

.global-bg-circle-2 {
  width: 400px;
  height: 400px;
  bottom: -100px;
  left: -100px;
  background: rgba(7, 193, 96, 0.05);
}

.login-box {
  width: 520px;
  padding: 48px;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 2;
  border: 1px solid rgba(255, 255, 255, 0.3);
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
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    background-color: #fff;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
    
    &:hover {
      box-shadow: 0 4px 12px rgba(7, 193, 96, 0.1);
    }
    
    &.is-focus {
      box-shadow: 0 4px 12px rgba(7, 193, 96, 0.2);
      border-color: #07c160;
    }
  }

  :deep(.el-input__inner) {
    height: 48px;
  }
  
  :deep(.el-input__prefix) {
    font-size: 20px;
    color: #07c160;
  }
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
  margin-top: 10px;
  background: linear-gradient(135deg, #07c160 0%, #05a350 100%);
  border: none;
  font-weight: 500;
  letter-spacing: 2px;
  
  &:hover {
    background: linear-gradient(135deg, #05a350 0%, #048c40 100%);
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(7, 193, 96, 0.3);
  }
  
  &:active {
    transform: translateY(0);
  }
}

/* 响应式适配 */
@media screen and (max-width: 768px) {
  .login-box {
    width: 90%;
    padding: 32px;
    margin: 0 20px;
  }
  
  .global-bg-circle-1 {
    width: 400px;
    height: 400px;
    top: -150px;
    right: -150px;
  }
  
  .global-bg-circle-2 {
    width: 300px;
    height: 300px;
    bottom: -80px;
    left: -80px;
  }
}
</style>