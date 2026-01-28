<template>
  <div class="error-page error-403">
    <div class="error-container">
      <!-- 错误图标 -->
      <div class="error-icon">
        <el-icon :size="120" color="#f56c6c">
          <Lock />
        </el-icon>
      </div>
      
      <!-- 错误信息 -->
      <div class="error-content">
        <h1 class="error-title">403</h1>
        <h2 class="error-subtitle">抱歉，您没有访问权限</h2>
        <p class="error-description">
          您没有权限访问此页面。如果您认为这是一个错误，请联系管理员。
        </p>
        
        <!-- 用户信息 -->
        <div class="user-info" v-if="userStore.currentUser">
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="当前用户">
              {{ userStore.currentUser.name }}
            </el-descriptions-item>
            <el-descriptions-item label="用户角色">
              <el-tag :type="getRoleTagType(userStore.currentUser.role)">
                {{ formatRole(userStore.currentUser.role) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前路径">
              <el-text truncated style="max-width: 300px;">
                {{ currentPath }}
              </el-text>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        
        <!-- 操作按钮 -->
        <div class="error-actions">
          <el-button type="primary" @click="goHome">
            <el-icon><House /></el-icon>
            返回首页
          </el-button>
          
          <el-button @click="goBack">
            <el-icon><Back /></el-icon>
            返回上一页
          </el-button>
          
          <el-button @click="refreshPage">
            <el-icon><Refresh /></el-icon>
            刷新页面
          </el-button>
          
          <el-button type="danger" @click="logout" v-if="userStore.currentUser">
            <el-icon><SwitchButton /></el-icon>
            切换账号
          </el-button>
        </div>
        
        <!-- 权限提示 -->
        <div class="permission-tips" v-if="suggestedPages.length > 0">
          <h3 class="tips-title">您可以尝试访问以下页面：</h3>
          <div class="page-suggestions">
            <el-link
              v-for="page in suggestedPages"
              :key="page.path"
              :type="page.type"
              :underline="false"
              class="page-link"
              @click="goToPage(page.path)"
            >
              <el-icon :size="16" style="margin-right: 6px;">
                <component :is="page.icon" />
              </el-icon>
              {{ page.title }}
            </el-link>
          </div>
        </div>
        
        <!-- 技术支持 -->
        <div class="support-info">
          <p class="support-text">
            如需帮助，请联系技术支持：
            <el-link type="primary" :underline="false" @click="contactSupport">
              support@gymflow.com
            </el-link>
          </p>
          <p class="error-time">
            错误时间：{{ errorTime }}
          </p>
        </div>
      </div>
    </div>
    
    <!-- 背景装饰 -->
    <div class="error-background">
      <div class="bg-circle bg-circle-1"></div>
      <div class="bg-circle bg-circle-2"></div>
      <div class="bg-circle bg-circle-3"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 计算属性
const currentPath = computed(() => route.fullPath)
const errorTime = computed(() => {
  const now = new Date()
  return now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
})

// 根据用户角色推荐页面
const suggestedPages = computed(() => {
  const role = userStore.currentUser?.role
  const suggestions = []
  
  // 基础页面
  suggestions.push({
    title: '仪表盘',
    path: '/dashboard',
    icon: 'Odometer',
    type: 'primary'
  })
  
  // 根据角色推荐
  switch (role) {
    case 'MEMBER':
      suggestions.push(
        { title: '我的课程', path: '/member/courses', icon: 'Calendar', type: 'success' },
        { title: '个人信息', path: '/settings/profile', icon: 'User', type: 'info' }
      )
      break
    case 'COACH':
      suggestions.push(
        { title: '我的排班', path: '/coach/schedule', icon: 'Calendar', type: 'success' },
        { title: '学员管理', path: '/coach/students', icon: 'User', type: 'info' }
      )
      break
    case 'ADMIN':
      suggestions.push(
        { title: '会员管理', path: '/member/list', icon: 'User', type: 'success' },
        { title: '课程管理', path: '/course/list', icon: 'List', type: 'info' },
        { title: '系统设置', path: '/settings/system', icon: 'Setting', type: 'warning' }
      )
      break
  }
  
  return suggestions
})

// 方法
const getRoleTagType = (role: string) => {
  const roleMap: Record<string, string> = {
    'MEMBER': 'success',
    'COACH': 'warning',
    'ADMIN': 'danger'
  }
  return roleMap[role] || 'info'
}

const formatRole = (role: string) => {
  const roleMap: Record<string, string> = {
    'MEMBER': '会员',
    'COACH': '教练',
    'ADMIN': '管理员'
  }
  return roleMap[role] || role
}

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    goHome()
  }
}

const refreshPage = () => {
  window.location.reload()
}

const goToPage = (path: string) => {
  router.push(path)
}

const logout = async () => {
  try {
    await userStore.logout()
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

const contactSupport = () => {
  window.location.href = 'mailto:support@gymflow.com'
}

// 记录错误日志
onMounted(() => {
  console.error(`403 Forbidden: User ${userStore.currentUser?.name} tried to access ${currentPath.value}`)
})
</script>

<style scoped lang="scss">
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  
  &.error-403 {
    .error-icon {
      .el-icon {
        animation: pulse 2s infinite;
      }
    }
  }
}

.error-container {
  position: relative;
  z-index: 2;
  max-width: 800px;
  width: 100%;
  padding: 40px;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  animation: slideUp 0.6s ease-out;
}

.error-icon {
  text-align: center;
  margin-bottom: 30px;
}

.error-content {
  text-align: center;
  
  .error-title {
    font-size: 120px;
    font-weight: 700;
    color: #f56c6c;
    margin: 0;
    line-height: 1;
    text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.1);
  }
  
  .error-subtitle {
    font-size: 32px;
    font-weight: 500;
    color: #303133;
    margin: 20px 0;
  }
  
  .error-description {
    font-size: 16px;
    color: #606266;
    line-height: 1.6;
    margin-bottom: 30px;
    max-width: 600px;
    margin-left: auto;
    margin-right: auto;
  }
}

.user-info {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
  text-align: left;
  
  :deep(.el-descriptions) {
    .el-descriptions__label {
      width: 100px;
    }
  }
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 30px;
  flex-wrap: wrap;
  
  .el-button {
    min-width: 120px;
    height: 40px;
    font-size: 14px;
    
    .el-icon {
      margin-right: 6px;
    }
  }
}

.permission-tips {
  background-color: #f0f9ff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 30px;
  
  .tips-title {
    font-size: 16px;
    color: #409eff;
    margin: 0 0 15px 0;
    text-align: left;
  }
  
  .page-suggestions {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    
    .page-link {
      display: inline-flex;
      align-items: center;
      padding: 8px 16px;
      background-color: #ffffff;
      border-radius: 6px;
      border: 1px solid #dcdfe6;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
    }
  }
}

.support-info {
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
  
  .support-text {
    font-size: 14px;
    color: #909399;
    margin: 0 0 10px 0;
  }
  
  .error-time {
    font-size: 12px;
    color: #c0c4cc;
    margin: 0;
  }
}

.error-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  
  .bg-circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    
    &.bg-circle-1 {
      width: 300px;
      height: 300px;
      top: -100px;
      right: -100px;
    }
    
    &.bg-circle-2 {
      width: 200px;
      height: 200px;
      bottom: 50px;
      left: -50px;
    }
    
    &.bg-circle-3 {
      width: 150px;
      height: 150px;
      bottom: -50px;
      right: 30%;
    }
  }
}

// 动画
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .error-container {
    padding: 20px;
    margin: 20px;
  }
  
  .error-content {
    .error-title {
      font-size: 80px;
    }
    
    .error-subtitle {
      font-size: 24px;
    }
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
    
    .el-button {
      width: 100%;
      max-width: 300px;
    }
  }
  
  .page-suggestions {
    flex-direction: column;
  }
}
</style>