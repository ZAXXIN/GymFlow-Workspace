<template>
  <div class="app-content">
    <!-- 标签页导航 -->
    <div class="tabs-container" v-if="showTabs">
      <el-tabs
        v-model="activeTab"
        type="card"
        closable
        @tab-remove="removeTab"
        @tab-click="handleTabClick"
      >
        <el-tab-pane
          v-for="tab in tabs"
          :key="tab.name"
          :label="tab.title"
          :name="tab.name"
        />
      </el-tabs>
      
      <div class="tabs-actions">
        <el-dropdown @command="handleTabAction">
          <el-icon class="tab-action-icon">
            <MoreFilled />
          </el-icon>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="closeCurrent">关闭当前</el-dropdown-item>
              <el-dropdown-item command="closeOthers">关闭其他</el-dropdown-item>
              <el-dropdown-item command="closeAll">关闭所有</el-dropdown-item>
              <el-dropdown-item divided command="refresh">刷新当前</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container" v-if="showBreadcrumb">
      <app-breadcrumb />
    </div>
    
    <!-- 页面内容 -->
    <div class="page-content" :class="{ 'with-tabs': showTabs, 'with-breadcrumb': showBreadcrumb }">
      <router-view v-slot="{ Component }">
        <transition name="fade-transform" mode="out-in">
          <keep-alive :include="keepAliveComponents">
            <component :is="Component" :key="route.fullPath" />
          </keep-alive>
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useSettingsStore } from '@/stores/settings'
import AppBreadcrumb from '../common/AppBreadcrumb.vue'
import { MoreFilled } from '@element-plus/icons-vue'

interface TabItem {
  name: string
  title: string
  path: string
}

const router = useRouter()
const route = useRoute()
const settingsStore = useSettingsStore()

// 响应式数据
const tabs = ref<TabItem[]>([
  { name: 'dashboard', title: '仪表盘', path: '/dashboard' }
])
const activeTab = ref('dashboard')

// 计算属性
const showTabs = computed(() => settingsStore.showTabs)
const showBreadcrumb = computed(() => settingsStore.showBreadcrumb)
const keepAliveComponents = computed(() => {
  return tabs.value.map(tab => tab.name)
})

// 添加标签页
const addTab = (routeInfo: any) => {
  if (!routeInfo.name || routeInfo.meta?.hidden) {
    return
  }
  
  const tabName = routeInfo.name as string
  const tabTitle = routeInfo.meta?.title || routeInfo.name
  const tabPath = routeInfo.path
  
  // 检查标签页是否已存在
  const existingTab = tabs.value.find(tab => tab.name === tabName)
  if (!existingTab) {
    tabs.value.push({
      name: tabName,
      title: tabTitle,
      path: tabPath
    })
  }
  
  // 激活当前标签页
  activeTab.value = tabName
}

// 移除标签页
const removeTab = (targetName: string) => {
  if (tabs.value.length <= 1) {
    return
  }
  
  const tabsArray = tabs.value
  let activeName = activeTab.value
  
  if (activeName === targetName) {
    tabsArray.forEach((tab, index) => {
      if (tab.name === targetName) {
        const nextTab = tabsArray[index + 1] || tabsArray[index - 1]
        if (nextTab) {
          activeName = nextTab.name
        }
      }
    })
  }
  
  activeTab.value = activeName
  tabs.value = tabsArray.filter(tab => tab.name !== targetName)
  
  // 跳转到激活的标签页
  const activeTabItem = tabs.value.find(tab => tab.name === activeName)
  if (activeTabItem) {
    router.push(activeTabItem.path)
  }
}

// 标签页点击
const handleTabClick = (tab: any) => {
  const tabItem = tabs.value.find(item => item.name === tab.paneName)
  if (tabItem && tabItem.path !== route.path) {
    router.push(tabItem.path)
  }
}

// 标签页操作
const handleTabAction = (command: string) => {
  switch (command) {
    case 'closeCurrent':
      removeTab(activeTab.value)
      break
    case 'closeOthers':
      tabs.value = tabs.value.filter(tab => 
        tab.name === activeTab.value || tab.name === 'dashboard'
      )
      break
    case 'closeAll':
      tabs.value = tabs.value.filter(tab => tab.name === 'dashboard')
      activeTab.value = 'dashboard'
      router.push('/dashboard')
      break
    case 'refresh':
      // 刷新当前页面
      const currentPath = route.fullPath
      router.replace('/redirect' + currentPath)
      break
  }
}

// 监听路由变化
watch(() => route, (newRoute) => {
  addTab(newRoute)
}, { immediate: true, deep: true })
</script>

<style scoped lang="scss">
.app-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  
  .tabs-container {
    display: flex;
    align-items: center;
    background-color: #ffffff;
    padding: 0 20px;
    border-bottom: 1px solid #e4e7ed;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    
    :deep(.el-tabs) {
      flex: 1;
      
      .el-tabs__header {
        margin: 0;
        border: none;
        
        .el-tabs__nav-wrap {
          &::after {
            height: 1px;
          }
        }
        
        .el-tabs__item {
          height: 36px;
          line-height: 36px;
          border: 1px solid #e4e7ed;
          border-radius: 4px 4px 0 0;
          margin-right: 5px;
          padding: 0 15px;
          background-color: #f5f7fa;
          
          &.is-active {
            background-color: #ffffff;
            border-bottom-color: #ffffff;
          }
          
          .is-icon-close {
            margin-left: 5px;
          }
        }
      }
    }
    
    .tabs-actions {
      margin-left: 10px;
      
      .tab-action-icon {
        cursor: pointer;
        color: #909399;
        font-size: 18px;
        padding: 4px;
        border-radius: 4px;
        transition: all 0.3s;
        
        &:hover {
          background-color: #f5f7fa;
          color: #409eff;
        }
      }
    }
  }
  
  .breadcrumb-container {
    background-color: #ffffff;
    padding: 12px 20px;
    border-bottom: 1px solid #e4e7ed;
  }
  
  .page-content {
    flex: 1;
    overflow: auto;
    padding: 20px;
    
    &.with-tabs {
      padding-top: 0;
    }
    
    &.with-breadcrumb {
      padding-top: 0;
    }
  }
}

// 页面切换动画
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

// 响应式设计
@media (max-width: 768px) {
  .app-content {
    .tabs-container {
      padding: 0 10px;
      
      :deep(.el-tabs__item) {
        padding: 0 10px !important;
        font-size: 12px;
      }
    }
    
    .page-content {
      padding: 10px;
    }
  }
}
</style>