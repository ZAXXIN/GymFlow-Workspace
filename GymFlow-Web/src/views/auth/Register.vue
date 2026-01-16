<template>
  <div class="register-container">
    <div class="register-wrapper">
      <!-- 左侧装饰区域 -->
      <div class="register-left">
        <div class="register-brand">
          <h1 class="brand-title">GymFlow</h1>
          <p class="brand-subtitle">健身工作室管理系统</p>
        </div>
        <div class="register-illustration">
          <div class="illustration-image">
            <!-- 这里可以放置SVG或图片 -->
            <div class="register-icon">
              <div class="icon-circle">
                <el-icon size="60" color="white">
                  <UserFilled />
                </el-icon>
              </div>
            </div>
          </div>
          <p class="illustration-text">加入我们，开启健身之旅</p>
        </div>
        <div class="back-to-login">
          <router-link to="/login" class="back-link">
            <el-icon><ArrowLeft /></el-icon>
            返回登录
          </router-link>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="register-right">
        <div class="register-form-wrapper">
          <div class="form-header">
            <h2 class="form-title">创建账号</h2>
            <p class="form-subtitle">填写以下信息完成注册</p>
          </div>

          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="register-form"
            @submit.prevent="handleRegister"
          >
            <div class="form-row">
              <el-form-item prop="username" class="form-item">
                <el-input
                  v-model="registerForm.username"
                  placeholder="请输入用户名"
                  size="large"
                  :prefix-icon="User"
                  autocomplete="username"
                />
              </el-form-item>
              
              <el-form-item prop="phone" class="form-item">
                <el-input
                  v-model="registerForm.phone"
                  placeholder="请输入手机号"
                  size="large"
                  :prefix-icon="Iphone"
                  maxlength="11"
                />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item prop="email" class="form-item">
                <el-input
                  v-model="registerForm.email"
                  placeholder="请输入邮箱"
                  size="large"
                  :prefix-icon="Message"
                  type="email"
                />
              </el-form-item>
              
              <el-form-item prop="realName" class="form-item">
                <el-input
                  v-model="registerForm.realName"
                  placeholder="请输入真实姓名"
                  size="large"
                  :prefix-icon="Avatar"
                />
              </el-form-item>
            </div>

            <div class="form-row">
              <el-form-item prop="password" class="form-item">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  :prefix-icon="Lock"
                  autocomplete="new-password"
                  show-password
                />
              </el-form-item>
              
              <el-form-item prop="confirmPassword" class="form-item">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请确认密码"
                  size="large"
                  :prefix-icon="Lock"
                  autocomplete="new-password"
                  show-password
                />
              </el-form-item>
            </div>

            <el-form-item prop="role" class="role-select">
              <div class="role-label">注册角色</div>
              <el-radio-group v-model="registerForm.role" class="role-options">
                <el-radio-button value="MEMBER">会员</el-radio-button>
                <el-radio-button value="COACH">教练</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item prop="agreement" class="agreement-item">
              <el-checkbox v-model="registerForm.agreement">
                我已阅读并同意
                <router-link to="/terms" class="agreement-link">服务条款</router-link>
                和
                <router-link to="/privacy" class="agreement-link">隐私政策</router-link>
              </el-checkbox>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="register-button"
                :loading="loading"
                @click="handleRegister"
              >
                注册账号
              </el-button>
            </el-form-item>
          </el-form>

          <div class="register-divider">
            <span class="divider-text">已有账号？</span>
          </div>

          <div class="login-redirect">
            <router-link to="/login" class="login-link">
              <el-icon><ArrowLeft /></el-icon>
              返回登录
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { 
  User, 
  Lock, 
  Message, 
  Iphone, 
  Avatar,
  UserFilled,
  ArrowLeft 
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)

// 注册表单
const registerForm = reactive({
  username: '',
  phone: '',
  email: '',
  realName: '',
  password: '',
  confirmPassword: '',
  role: 'MEMBER' as 'MEMBER' | 'COACH',
  agreement: false
})

// 验证密码强度
const validatePasswordStrength = (rule: any, value: string, callback: Function) => {
  if (!value) {
    callback(new Error('请输入密码'))
    return
  }
  
  if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
    return
  }
  
  if (value.length > 20) {
    callback(new Error('密码长度不能超过20位'))
    return
  }
  
  // 检查密码强度（至少包含字母和数字）
  const hasLetter = /[a-zA-Z]/.test(value)
  const hasNumber = /\d/.test(value)
  
  if (!hasLetter || !hasNumber) {
    callback(new Error('密码必须包含字母和数字'))
    return
  }
  
  callback()
}

// 验证确认密码
const validateConfirmPassword = (rule: any, value: string, callback: Function) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度在2-10个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePasswordStrength, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择注册角色', trigger: 'change' }
  ],
  agreement: [
    {
      validator: (rule: any, value: boolean, callback: Function) => {
        if (!value) {
          callback(new Error('请同意服务条款和隐私政策'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    loading.value = true

    // 调用注册API
    await authStore.register({
      username: registerForm.username,
      phone: registerForm.phone,
      email: registerForm.email,
      realName: registerForm.realName,
      password: registerForm.password,
      role: registerForm.role
    })

    // 注册成功后重定向
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    console.error('注册失败:', error)
    ElMessage.error(error.message || '注册失败，请检查输入信息')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  padding: 20px;
}

.register-wrapper {
  display: flex;
  width: 1000px;
  height: 700px;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
}

.register-left {
  flex: 1;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 60px 40px;
  position: relative;
}

.register-brand {
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

.register-illustration {
  text-align: center;

  .illustration-image {
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 20px;

    .register-icon {
      .icon-circle {
        width: 120px;
        height: 120px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        backdrop-filter: blur(10px);
        border: 2px solid rgba(255, 255, 255, 0.3);
      }
    }
  }

  .illustration-text {
    font-size: 16px;
    opacity: 0.8;
    margin: 0;
  }
}

.back-to-login {
  .back-link {
    display: inline-flex;
    align-items: center;
    color: white;
    text-decoration: none;
    font-size: 14px;
    opacity: 0.8;
    transition: opacity 0.3s;

    &:hover {
      opacity: 1;
    }

    .el-icon {
      margin-right: 6px;
    }
  }
}

.register-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  overflow-y: auto;
}

.register-form-wrapper {
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

.register-form {
  .form-row {
    display: flex;
    gap: 20px;
    margin-bottom: 16px;

    .form-item {
      flex: 1;
      margin-bottom: 0;
    }
  }

  .role-select {
    margin-bottom: 24px;

    .role-label {
      margin-bottom: 12px;
      font-size: 14px;
      color: #606266;
      font-weight: 500;
    }

    .role-options {
      width: 100%;

      :deep(.el-radio-button) {
        flex: 1;

        .el-radio-button__inner {
          width: 100%;
          text-align: center;
          border-radius: 8px;
          border: 1px solid #dcdfe6;
          background: white;
          color: #606266;
          transition: all 0.3s;
        }

        &:first-child .el-radio-button__inner {
          border-right: none;
          border-top-right-radius: 0;
          border-bottom-right-radius: 0;
        }

        &:last-child .el-radio-button__inner {
          border-left: none;
          border-top-left-radius: 0;
          border-bottom-left-radius: 0;
        }

        &.is-active .el-radio-button__inner {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          border-color: #f5576c;
          color: white;
          box-shadow: 0 2px 12px rgba(245, 87, 108, 0.2);
        }
      }
    }
  }

  .agreement-item {
    margin-bottom: 24px;

    :deep(.el-checkbox__label) {
      color: #606266;

      .agreement-link {
        color: #409eff;
        text-decoration: none;

        &:hover {
          color: #66b1ff;
        }
      }
    }
  }

  :deep(.el-form-item) {
    margin-bottom: 20px;
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

.register-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;

  &:hover {
    opacity: 0.9;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3);
  }

  &:active {
    transform: translateY(0);
  }
}

.register-divider {
  text-align: center;
  margin: 32px 0;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    right: 0;
    top: 50%;
    height: 1px;
    background: #ebeef5;
  }

  .divider-text {
    display: inline-block;
    padding: 0 16px;
    background: white;
    color: #909399;
    font-size: 14px;
    position: relative;
    z-index: 1;
  }
}

.login-redirect {
  text-align: center;

  .login-link {
    display: inline-flex;
    align-items: center;
    color: #409eff;
    text-decoration: none;
    font-size: 16px;
    font-weight: 500;
    transition: color 0.3s;

    &:hover {
      color: #66b1ff;
    }

    .el-icon {
      margin-right: 8px;
    }
  }
}
</style>