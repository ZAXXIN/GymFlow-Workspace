<template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- 左侧背景 -->
      <div class="login-left">
        <div class="login-background">
          <div class="background-overlay">
            <div class="brand">
              <el-icon class="brand-icon"><i-ep-fitness /></el-icon>
              <h1 class="brand-name">GymFlow</h1>
              <p class="brand-slogan">健身工作室管理系统</p>
            </div>
            <div class="features">
              <div class="feature-item">
                <el-icon><i-ep-check /></el-icon>
                <span>会员管理</span>
              </div>
              <div class="feature-item">
                <el-icon><i-ep-check /></el-icon>
                <span>课程预约</span>
              </div>
              <div class="feature-item">
                <el-icon><i-ep-check /></el-icon>
                <span>在线签到</span>
              </div>
              <div class="feature-item">
                <el-icon><i-ep-check /></el-icon>
                <span>数据统计</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧登录表单 -->
      <div class="login-right">
        <div class="login-form-wrapper">
          <div class="form-header">
            <h2 class="form-title">欢迎回来</h2>
            <p class="form-subtitle">请登录您的账户</p>
          </div>
          
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
              />
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                :prefix-icon="Lock"
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            
            <el-form-item>
              <div class="form-options">
                <el-checkbox v-model="rememberMe">记住我</el-checkbox>
                <router-link to="/forgot-password" class="forgot-link">
                  忘记密码？
                </router-link>
              </div>
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleLogin"
                class="login-btn"
              >
                登录
              </el-button>
            </el-form-item>
            
            <div class="form-divider">
              <span>其他登录方式</span>
            </div>
            
            <div class="social-login">
              <el-button
                class="social-btn wechat"
                circle
                size="large"
                @click="handleWechatLogin"
              >
                <el-icon><i-ep-wechat /></el-icon>
              </el-button>
            </div>
            
            <div class="form-footer">
              <span>还没有账户？</span>
              <router-link to="/register" class="register-link">
                立即注册
              </router-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'
import { StorageKeys } from '@/utils/constants'

// Store
const authStore = useAuthStore()
const settingsStore = useSettingsStore()

// Router
const router = useRouter()

// Refs
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

// Form Data
const loginForm = reactive({
  username: '',
  password: ''
})

// Form Rules
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// Methods
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    const result = await authStore.login(loginForm.username, loginForm.password)
    
    if (result.success) {
      ElMessage.success('登录成功')
      
      // 记住我功能
      if (rememberMe.value) {
        localStorage.setItem(StorageKeys.REMEMBER_ME, 'true')
        localStorage.setItem(StorageKeys.REMEMBER_USERNAME, loginForm.username)
      } else {
        localStorage.removeItem(StorageKeys.REMEMBER_ME)
        localStorage.removeItem(StorageKeys.REMEMBER_USERNAME)
      }
      
      // 跳转到首页或重定向页面
      const redirect = router.currentRoute.value.query.redirect as string
      router.push(redirect || '/dashboard')
    } else {
      ElMessage.error('登录失败，请检查用户名和密码')
    }
  } catch (error) {
    console.error('登录错误:', error)
  } finally {
    loading.value = false
  }
}

const handleWechatLogin = () => {
  ElMessage.info('微信登录功能开发中')
}

// 初始化记住的用户名
const initRememberedUsername = () => {
  const remembered = localStorage.getItem(StorageKeys.REMEMBER_ME)
  if (remembered === 'true') {
    rememberMe.value = true
    const rememberedUsername = localStorage.getItem(StorageKeys.REMEMBER_USERNAME)
    if (rememberedUsername) {
      loginForm.username = rememberedUsername
    }
  }
}

// 生命周期
onMounted(() => {
  initRememberedUsername()
  
  // 如果已登录，跳转到首页
  if (authStore.isLoggedIn) {
    router.push('/dashboard')
  }
})
</script>

<style lang="scss" scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: 400% 400%;
  animation: gradient 15s ease infinite;
  
  @keyframes gradient {
    0% {
      background-position: 0% 50%;
    }
    50% {
      background-position: 100% 50%;
    }
    100% {
      background-position: 0% 50%;
    }
  }
}

.login-wrapper {
  display: flex;
  width: 900px;
  height: 600px;
  background: var(--gymflow-card-bg);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, var(--gymflow-primary) 0%, var(--gymflow-secondary) 100%);
  position: relative;
  
  .login-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url('data:image/svg+xml,<svg width="100" height="100" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg"><path d="M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z" fill="%23ffffff" fill-opacity="0.1" fill-rule="evenodd"/></svg>');
    
    .background-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      padding: 40px;
      color: white;
      
      .brand {
        text-align: center;
        margin-bottom: 60px;
        
        .brand-icon {
          font-size: 48px;
          margin-bottom: 20px;
          opacity: 0.9;
        }
        
        .brand-name {
          font-size: 36px;
          font-weight: 700;
          margin: 0 0 10px;
          letter-spacing: 2px;
        }
        
        .brand-slogan {
          font-size: 16px;
          opacity: 0.8;
          margin: 0;
        }
      }
      
      .features {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 20px;
        max-width: 300px;
        
        .feature-item {
          display: flex;
          align-items: center;
          gap: 10px;
          font-size: 14px;
          opacity: 0.9;
          
          .el-icon {
            font-size: 16px;
          }
        }
      }
    }
  }
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  
  .login-form-wrapper {
    width: 100%;
    max-width: 400px;
    
    .form-header {
      text-align: center;
      margin-bottom: 40px;
      
      .form-title {
        font-size: 28px;
        font-weight: 600;
        color: var(--gymflow-text-primary);
        margin: 0 0 10px;
      }
      
      .form-subtitle {
        font-size: 14px;
        color: var(--gymflow-text-secondary);
        margin: 0;
      }
    }
    
    .login-form {
      :deep(.el-form-item) {
        margin-bottom: 24px;
        
        .el-input__wrapper {
          border-radius: 12px;
          height: 48px;
          
          &.is-focus {
            box-shadow: 0 0 0 2px var(--gymflow-primary);
          }
        }
      }
      
      .form-options {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
        
        :deep(.el-checkbox) {
          .el-checkbox__label {
            font-size: 14px;
            color: var(--gymflow-text-secondary);
          }
        }
        
        .forgot-link {
          font-size: 14px;
          color: var(--gymflow-primary);
          text-decoration: none;
          transition: opacity 0.3s;
          
          &:hover {
            opacity: 0.8;
          }
        }
      }
      
      .login-btn {
        width: 100%;
        height: 48px;
        font-size: 16px;
        font-weight: 500;
        border-radius: 12px;
        background: var(--gymflow-primary);
        border: none;
        
        &:hover {
          background: var(--gymflow-primary);
          opacity: 0.9;
        }
        
        &:active {
          transform: translateY(1px);
        }
      }
      
      .form-divider {
        display: flex;
        align-items: center;
        margin: 30px 0;
        color: var(--gymflow-text-secondary);
        
        &::before,
        &::after {
          content: '';
          flex: 1;
          height: 1px;
          background: var(--gymflow-border);
        }
        
        span {
          padding: 0 15px;
          font-size: 14px;
        }
      }
      
      .social-login {
        display: flex;
        justify-content: center;
        gap: 20px;
        margin-bottom: 30px;
        
        .social-btn {
          width: 48px;
          height: 48px;
          border-radius: 50%;
          border: 1px solid var(--gymflow-border);
          background: transparent;
          transition: all 0.3s;
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          }
          
          &.wechat {
            color: #07C160;
            
            &:hover {
              background: #07C160;
              color: white;
              border-color: #07C160;
            }
          }
          
          .el-icon {
            font-size: 20px;
          }
        }
      }
      
      .form-footer {
        text-align: center;
        font-size: 14px;
        color: var(--gymflow-text-secondary);
        
        .register-link {
          color: var(--gymflow-primary);
          text-decoration: none;
          margin-left: 5px;
          font-weight: 500;
          transition: opacity 0.3s;
          
          &:hover {
            opacity: 0.8;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .login-wrapper {
    width: 100%;
    height: 100%;
    border-radius: 0;
    flex-direction: column;
  }
  <template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- 左侧装饰区域 -->
      <div class="login-left">
        <div class="login-brand">
          <h1 class="brand-title">GymFlow</h1>
          <p class="brand-subtitle">健身工作室管理系统</p>
        </div>
        <div class="login-illustration">
          <div class="illustration-image">
            <!-- 这里可以放置SVG或图片 -->
            <div class="gym-icon">
              <div class="dumbbell"></div>
              <div class="dumbbell"></div>
            </div>
          </div>
          <p class="illustration-text">科学管理，高效运营</p>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-right">
        <div class="login-form-wrapper">
          <div class="form-header">
            <h2 class="form-title">欢迎回来</h2>
            <p class="form-subtitle">请登录您的账号</p>
          </div>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                autocomplete="username"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                autocomplete="current-password"
                show-password
              />
            </el-form-item>

            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <router-link to="/forgot-password" class="forgot-link">
                忘记密码？
              </router-link>
            </div>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-button"
                :loading="loading"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>

            <div class="form-footer">
              <p class="register-tip">
                还没有账号？
                <router-link to="/register" class="register-link">
                  立即注册
                </router-link>
              </p>
            </div>
          </el-form>

          <div class="login-divider">
            <span class="divider-text">或</span>
          </div>

          <div class="social-login">
            <el-button class="social-button wechat">
              <el-icon size="20">
                <svg viewBox="0 0 1024 1024">
                  <path d="M693.6 284.8c84.8 84.8 84.8 222.4 0 307.2-84.8 84.8-222.4 84.8-307.2 0-84.8-84.8-84.8-222.4 0-307.2 84.8-84.8 222.4-84.8 307.2 0z" fill="#07C160"></path>
                  <path d="M511.2 223.2c-159.2 0-288 128.8-288 288s128.8 288 288 288 288-128.8 288-288-128.8-288-288-288z m144 331.2c-19.2 35.2-48 65.6-84 86.4-38.4 22.4-80.8 33.6-126.4 33.6-45.6 0-88-11.2-126.4-33.6-36-20.8-64.8-51.2-84-86.4-19.2-35.2-28.8-73.6-28.8-114.4 0-40.8 9.6-79.2 28.8-114.4 19.2-35.2 48-65.6 84-86.4 38.4-22.4 80.8-33.6 126.4-33.6 45.6 0 88 11.2 126.4 33.6 36 20.8 64.8 51.2 84 86.4 19.2 35.2 28.8 73.6 28.8 114.4 0 40.8-9.6 79.2-28.8 114.4z" fill="#FFFFFF"></path>
                </svg>
              </el-icon>
              微信登录
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
    loading.value = true

    // 调用登录API
    await authStore.login({
      username: loginForm.username,
      password: loginForm.password
    })

    // 登录成功后重定向
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error: any) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

// 检查记住我功能
onMounted(() => {
  const savedUsername = localStorage.getItem('rememberedUsername')
  if (savedUsername) {
    loginForm.username = savedUsername
    rememberMe.value = true
  }
})

// 监听记住我变化
watch(rememberMe, (value) => {
  if (value && loginForm.username) {
    localStorage.setItem('rememberedUsername', loginForm.username)
  } else {
    localStorage.removeItem('rememberedUsername')
  }
})

// 监听用户名变化
watch(() => loginForm.username, (value) => {
  if (rememberMe.value && value) {
    localStorage.setItem('rememberedUsername', value)
  }
})
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-wrapper {
  display: flex;
  width: 1000px;
  height: 600px;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 60px 40px;
}

.login-brand {
  .brand-title {
    font-size: 48px;
    font-weight: 700;
    margin: 0 0 10px 0;
    letter-spacing: 2px;
  }

  .brand-subtitle {
    font-size: 18px;
    opacity: 0.9;
    margin: 0;
  }
}

.login-illustration {
  text-align: center;

  .illustration-image {
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20px;

    .gym-icon {
      position: relative;
      width: 120px;
      height: 120px;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;

      .dumbbell {
        width: 60px;
        height: 12px;
        background: white;
        border-radius: 6px;
        position: relative;

        &:first-child {
          margin-right: 20px;
        }

        &:last-child {
          margin-left: 20px;
        }

        &::before,
        &::after {
          content: '';
          position: absolute;
          width: 20px;
          height: 20px;
          background: white;
          border-radius: 50%;
          top: 50%;
          transform: translateY(-50%);
        }

        &::before {
          left: -10px;
        }

        &::after {
          right: -10px;
        }
      }
    }
  }

  .illustration-text {
    font-size: 16px;
    opacity: 0.8;
    margin: 0;
  }
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-form-wrapper {
  width: 100%;
  max-width: 400px;
}

.form-header {
  text-align: center;
  margin-bottom: 40px;

  .form-title {
    font-size: 32px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 10px 0;
  }

  .form-subtitle {
    font-size: 16px;
    color: #909399;
    margin: 0;
  }
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }

  :deep(.el-input__wrapper) {
    height: 48px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  :deep(.el-input__inner) {
    font-size: 16px;
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  :deep(.el-checkbox__label) {
    color: #606266;
  }

  .forgot-link {
    color: #409eff;
    text-decoration: none;
    font-size: 14px;

    &:hover {
      color: #66b1ff;
    }
  }
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;

  &:hover {
    opacity: 0.9;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
  }

  &:active {
    transform: translateY(0);
  }
}

.form-footer {
  text-align: center;
  margin-top: 24px;

  .register-tip {
    color: #606266;
    font-size: 14px;
    margin: 0;

    .register-link {
      color: #409eff;
      text-decoration: none;
      font-weight: 500;

      &:hover {
        color: #66b1ff;
      }
    }
  }
}

.login-divider {
  display: flex;
  align-items: center;
  margin: 32px 0;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: #ebeef5;
  }

  .divider-text {
    padding: 0 16px;
    color: #909399;
    font-size: 14px;
  }
}

.social-login {
  .social-button {
    width: 100%;
    height: 48px;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 500;
    border: 1px solid #ebeef5;
    background: white;

    &.wechat {
      color: #07C160;
      border-color: #07C160;

      &:hover {
        background: rgba(7, 193, 96, 0.1);
        border-color: #07C160;
      }
    }

    .el-icon {
      margin-right: 8px;
    }
  }
}
</style>
  .login-left {
    display: none;
  }
  
  .login-right {
    padding: 20px;
  }
}
</style>