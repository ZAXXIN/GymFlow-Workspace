<template>
  <div class="error-page error-404">
    <div class="error-container">
      <!-- 错误图标 -->
      <div class="error-icon">
        <div class="icon-animation">
          <el-icon :size="100" color="#e6a23c" class="icon-search">
            <Search />
          </el-icon>
          <el-icon :size="80" color="#f56c6c" class="icon-close">
            <Close />
          </el-icon>
        </div>
      </div>
      
      <!-- 错误信息 -->
      <div class="error-content">
        <h1 class="error-title">404</h1>
        <h2 class="error-subtitle">页面未找到</h2>
        <p class="error-description">
          抱歉，您访问的页面不存在或已被移除。请检查URL是否正确，或返回首页继续浏览。
        </p>
        
        <!-- 当前路径信息 -->
        <div class="path-info">
          <el-alert
            title="找不到以下路径："
            type="warning"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="path-content">
                <code class="path-code">{{ currentPath }}</code>
                <el-button
                  type="text"
                  size="small"
                  @click="copyPath"
                  class="copy-btn"
                >
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-button>
              </div>
            </template>
          </el-alert>
        </div>
        
        <!-- 搜索建议 -->
        <div class="search-suggestion" v-if="showSearch">
          <h3 class="suggestion-title">您是不是想查找：</h3>
          <div class="suggestion-list">
            <el-tag
              v-for="suggestion in searchSuggestions"
              :key="suggestion"
              class="suggestion-tag"
              type="info"
              size="large"
              effect="plain"
              @click="handleSuggestionClick(suggestion)"
            >
              {{ suggestion }}
            </el-tag>
          </div>
          
          <div class="search-box">
            <el-input
              v-model="searchQuery"
              placeholder="搜索您想找的内容..."
              size="large"
              @keyup.enter="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </div>
        
        <!-- 操作按钮 -->
        <div class="error-actions">
          <el-button type="primary" size="large" @click="goHome">
            <el-icon><House /></el-icon>
            返回首页
          </el-button>
          
          <el-button size="large" @click="goBack">
            <el-icon><Back /></el-icon>
            返回上一页
          </el-button>
          
          <el-button size="large" @click="refreshPage">
            <el-icon><Refresh /></el-icon>
            刷新页面
          </el-button>
          
          <el-button size="large" @click="showSearch = !showSearch">
            <el-icon><Search /></el-icon>
            {{ showSearch ? '隐藏搜索' : '搜索页面' }}
          </el-button>
        </div>
        
        <!-- 常用页面 -->
        <div class="common-pages">
          <h3 class="pages-title">常用页面</h3>
          <div class="pages-grid">
            <el-card
              v-for="page in commonPages"
              :key="page.path"
              class="page-card"
              shadow="hover"
              @click="goToPage(page.path)"
            >
              <template #header>
                <div class="page-card-header">
                  <el-icon :size="20" :color="page.color">
                    <component :is="page.icon" />
                  </el-icon>
                  <span class="page-title">{{ page.title }}</span>
                </div>
              </template>
              <div class="page-description">
                {{ page.description }}
              </div>
            </el-card>
          </div>
        </div>
        
        <!-- 技术支持 -->
        <div class="support-info">
          <p class="support-text">
            如果您认为这是一个错误，请联系技术支持：
            <el-link type="primary" :underline="false" @click="contactSupport">
              support@gymflow.com
            </el-link>
          </p>
          <div class="error-details">
            <el-button type="text" size="small" @click="showDetails = !showDetails">
              {{ showDetails ? '隐藏' : '显示' }}错误详情
            </el-button>
            <div v-if="showDetails" class="details-content">
              <pre class="error-detail">{{ errorDetail }}</pre>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 背景装饰 -->
    <div class="error-background">
      <div class="bg-element bg-element-1"></div>
      <div class="bg-element bg-element-2"></div>
      <div class="bg-element bg-element-3"></div>
      <div class="bg-element bg-element-4"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Close, House, Back, Refresh, CopyDocument } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 响应式数据
const showSearch = ref(false)
const showDetails = ref(false)
const searchQuery = ref('')
const searchSuggestions = ref(['会员管理', '课程管理', '订单管理', '报表统计', '系统设置'])

// 计算属性
const currentPath = computed(() => route.fullPath)

const errorDetail = computed(() => {
  const details = {
    timestamp: new Date().toISOString(),
    path: route.fullPath,
    params: route.params,
    query: route.query,
    hash: route.hash,
    userAgent: navigator.userAgent,
    referrer: document.referrer || '无'
  }
  return JSON.stringify(details, null, 2)
})

const commonPages = computed(() => [
  {
    title: '仪表盘',
    path: '/dashboard',
    icon: 'Odometer',
    description: '查看系统概览和统计数据',
    color: '#409eff'
  },
  {
    title: '会员管理',
    path: '/member/list',
    icon: 'User',
    description: '管理会员信息和档案',
    color: '#67c23a'
  },
  {
    title: '课程管理',
    path: '/course/list',
    icon: 'List',
    description: '管理课程安排和预订',
    color: '#e6a23c'
  },
  {
    title: '订单管理',
    path: '/order/list',
    icon: 'ShoppingCart',
    description: '查看和处理订单',
    color: '#f56c6c'
  },
  {
    title: '报表统计',
    path: '/report/revenue',
    icon: 'TrendCharts',
    description: '查看营收和统计报表',
    color: '#909399'
  },
  {
    title: '系统设置',
    path: '/settings/system',
    icon: 'Setting',
    description: '配置系统参数和设置',
    color: '#722ed1'
  }
])

// 方法
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

const copyPath = async () => {
  try {
    await navigator.clipboard.writeText(currentPath.value)
    ElMessage.success('路径已复制到剪贴板')
  } catch (err) {
    console.error('复制失败:', err)
    ElMessage.error('复制失败')
  }
}

const handleSuggestionClick = (suggestion: string) => {
  searchQuery.value = suggestion
  handleSearch()
}

const handleSearch = () => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  // 这里应该实现搜索功能
  // 暂时模拟搜索
  const searchMap: Record<string, string> = {
    '会员管理': '/member/list',
    '课程管理': '/course/list',
    '订单管理': '/order/list',
    '报表统计': '/report/revenue',
    '系统设置': '/settings/system'
  }
  
  const path = searchMap[searchQuery.value]
  if (path) {
    router.push(path)
  } else {
    ElMessage.info(`正在搜索: ${searchQuery.value}`)
    // 在实际应用中，这里应该跳转到搜索结果页面
  }
}

const contactSupport = () => {
  window.location.href = 'mailto:support@gymflow.com'
}

// 记录错误日志
onMounted(() => {
  console.warn(`404 Not Found: ${currentPath.value}`)
  
  // 自动显示搜索框，如果路径看起来像是拼写错误
  if (currentPath.value.includes('admin') || 
      currentPath.value.includes('memeber') || 
      currentPath.value.includes('cours')) {
    showSearch.value = true
  }
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
  
  &.error-404 {
    .icon-animation {
      position: relative;
      animation: float 3s ease-in-out infinite;
      
      .icon-search {
        animation: searchRotate 6s linear infinite;
      }
      
      .icon-close {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        animation: closePulse 2s ease-in-out infinite;
      }
    }
  }
}

.error-container {
  position: relative;
  z-index: 2;
  max-width: 1000px;
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
    color: #e6a23c;
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
    max-width: 700px;
    margin-left: auto;
    margin-right: auto;
  }
}

.path-info {
  max-width: 600px;
  margin: 0 auto 30px;
  
  .path-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 0;
    
    .path-code {
      flex: 1;
      padding: 8px 12px;
      background-color: #f5f5f5;
      border-radius: 4px;
      font-family: 'Monaco', 'Consolas', monospace;
      font-size: 14px;
      color: #f56c6c;
      overflow-x: auto;
      white-space: nowrap;
    }
    
    .copy-btn {
      margin-left: 10px;
    }
  }
}

.search-suggestion {
  max-width: 600px;
  margin: 0 auto 30px;
  padding: 20px;
  background-color: #f0f9ff;
  border-radius: 8px;
  
  .suggestion-title {
    font-size: 16px;
    color: #409eff;
    margin: 0 0 15px 0;
    text-align: left;
  }
  
  .suggestion-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin-bottom: 20px;
    
    .suggestion-tag {
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }
  }
  
  .search-box {
    :deep(.el-input-group__append) {
      background-color: #409eff;
      border-color: #409eff;
      color: #ffffff;
      
      .el-button {
        color: #ffffff;
        
        &:hover {
          background-color: rgba(255, 255, 255, 0.1);
        }
      }
    }
  }
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 40px;
  flex-wrap: wrap;
  
  .el-button {
    min-width: 140px;
    height: 48px;
    font-size: 16px;
    
    .el-icon {
      margin-right: 8px;
    }
  }
}

.common-pages {
  margin-bottom: 30px;
  
  .pages-title {
    font-size: 18px;
    color: #303133;
    margin: 0 0 20px 0;
    text-align: left;
  }
  
  .pages-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    
    .page-card {
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1) !important;
      }
      
      .page-card-header {
        display: flex;
        align-items: center;
        gap: 10px;
        
        .page-title {
          font-weight: 500;
          color: #303133;
        }
      }
      
      .page-description {
        font-size: 14px;
        color: #606266;
        line-height: 1.5;
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
    margin: 0 0 15px 0;
  }
  
  .error-details {
    .details-content {
      margin-top: 10px;
      text-align: left;
      
      .error-detail {
        background-color: #f5f5f5;
        padding: 15px;
        border-radius: 4px;
        font-family: 'Monaco', 'Consolas', monospace;
        font-size: 12px;
        line-height: 1.5;
        color: #666;
        overflow-x: auto;
        max-height: 200px;
        overflow-y: auto;
      }
    }
  }
}

.error-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  
  .bg-element {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    
    &.bg-element-1 {
      width: 100px;
      height: 100px;
      top: 20%;
      left: 10%;
      animation: floatElement 20s infinite linear;
    }
    
    &.bg-element-2 {
      width: 150px;
      height: 150px;
      top: 60%;
      right: 15%;
      animation: floatElement 25s infinite linear reverse;
    }
    
    &.bg-element-3 {
      width: 80px;
      height: 80px;
      bottom: 20%;
      left: 20%;
      animation: floatElement 30s infinite linear;
    }
    
    &.bg-element-4 {
      width: 120px;
      height: 120px;
      top: 15%;
      right: 20%;
      animation: floatElement 22s infinite linear reverse;
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

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

@keyframes searchRotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes closePulse {
  0%, 100% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  50% {
    opacity: 0.6;
    transform: translate(-50%, -50%) scale(1.1);
  }
}

@keyframes floatElement {
  from {
    transform: translateY(0) rotate(0deg);
  }
  to {
    transform: translateY(-100px) rotate(360deg);
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
  
  .pages-grid {
    grid-template-columns: 1fr !important;
  }
  
  .search-box {
    :deep(.el-input) {
      .el-input__wrapper {
        flex-wrap: wrap;
        
        .el-input__inner {
          min-width: 100%;
        }
      }
    }
  }
}
</style>