<template>
  <div class="app-layout">
    <!-- 头部 -->
    <app-header v-if="showHeader" />
    
    <!-- 侧边栏 -->
    <app-sidebar v-if="showSidebar" />
    
    <!-- 主内容区 -->
    <main :class="['app-content', { 'with-sidebar': showSidebar, 'with-header': showHeader }]">
      <!-- 面包屑 -->
      <app-breadcrumb v-if="showBreadcrumb" />
      
      <!-- 页面内容 -->
      <div class="content-wrapper">
        <slot />
      </div>
    </main>
    
    <!-- 页脚 -->
    <app-footer v-if="showFooter" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './Header.vue'
import AppSidebar from './Sidebar.vue'
import AppFooter from './Footer.vue'
import AppBreadcrumb from '../common/AppBreadcrumb.vue'

interface Props {
  showHeader?: boolean
  showSidebar?: boolean
  showFooter?: boolean
  showBreadcrumb?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showHeader: true,
  showSidebar: true,
  showFooter: false,
  showBreadcrumb: true
})

const route = useRoute()

// 根据路由元信息动态显示布局元素
const showHeader = computed(() => {
  return props.showHeader && route.meta?.showHeader !== false
})

const showSidebar = computed(() => {
  return props.showSidebar && route.meta?.showSidebar !== false
})

const showFooter = computed(() => {
  return props.showFooter && route.meta?.showFooter !== false
})

const showBreadcrumb = computed(() => {
  return props.showBreadcrumb && route.meta?.showBreadcrumb !== false
})
</script>

<style scoped lang="scss">
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f7fa;
  
  .app-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    transition: margin-left 0.3s ease;
    
    &.with-sidebar {
      margin-left: 210px;
    }
    
    &.with-header {
      margin-top: 60px;
    }
    
    .content-wrapper {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
      background-color: #ffffff;
      border-radius: 4px;
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
      margin: 20px;
      
      &:first-child {
        margin-top: 0;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .app-layout {
    .app-content {
      &.with-sidebar {
        margin-left: 0;
      }
      
      .content-wrapper {
        margin: 10px;
        padding: 15px;
      }
    }
  }
}
</style>