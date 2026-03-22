<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { usePermission } from '@/composables/usePermission'
import { useSettingsStore } from '@/stores/settings'
import type { RouteRecordRaw } from 'vue-router'

const router = useRouter()
const route = useRoute()
const { filterMenuByPermission } = usePermission()
const settingsStore = useSettingsStore()

// 菜单路由配置
const menuRoutes = computed(() => {
  const allRoutes = router.getRoutes()
  const routes = allRoutes.filter(route => {
    return route.meta?.showInMenu && !route.meta?.hidden
  })  
  // 过滤权限
  const permissionRoutes = filterMenuByPermission(routes as RouteRecordRaw[])
  return permissionRoutes
})

// 当前激活的菜单 - 修复：子页面应高亮父菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  
  // 优先使用 meta.activeMenu 指定的菜单
  if (meta?.activeMenu) {
    return meta.activeMenu as string
  }
  
  // 如果有 parent 字段，返回父菜单路径
  if (meta?.parent) {
    // 查找父菜单路由的完整路径
    const parentRoute = router.getRoutes().find(r => r.name === meta.parent)
    if (parentRoute) {
      return parentRoute.path
    }
  }
  
  // 检查是否为子路由（如 /member/detail/:id 应该匹配 /member/list）
  const pathSegments = path.split('/')
  if (pathSegments.length >= 3) {
    // 尝试匹配父级路径：如 /member/list 或 /member
    const parentPath = '/' + pathSegments[1] + '/' + pathSegments[2]
    // 检查是否有菜单路由匹配
    const hasParentMenu = menuRoutes.value.some(r => r.path === parentPath)
    if (hasParentMenu) {
      return parentPath
    }
    
    const rootPath = '/' + pathSegments[1]
    const hasRootMenu = menuRoutes.value.some(r => r.path === rootPath)
    if (hasRootMenu) {
      return rootPath
    }
  }
  
  return path
})

// 侧边栏折叠状态
const isCollapsed = computed(() => settingsStore.sidebarCollapsed)

// 方法
const goHome = () => {
  router.push('/dashboard')
}

const toggleCollapse = () => {
  settingsStore.toggleSidebar()
}

const handleMenuSelect = (index: string) => {
  console.log('菜单选择:', index)
}

// 响应式处理
watch(() => route.path, () => {
  // 路由变化时，确保菜单正确显示
}, { immediate: true })
</script>

<style scoped lang="scss">
.app-sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 210px;
  background-color: #304156;
  transition: width 0.3s ease;
  z-index: 999;
  display: flex;
  flex-direction: column;
  
  &.collapsed {
    width: 64px;
    
    .sidebar-logo {
      padding: 15px 0;
      
      .logo-text {
        display: none;
      }
    }
    
    .menu-scrollbar {
      .el-menu {
        :deep(.el-sub-menu__title) {
          padding-left: 20px !important;
          
          .el-sub-menu__icon-arrow {
            display: none;
          }
        }
        
        .el-menu-item {
          padding-left: 20px !important;
          
          .el-icon {
            margin-right: 0;
          }
          
          span {
            display: none;
          }
        }
      }
    }
    
    .sidebar-footer {
      .collapse-toggle {
        padding: 15px 20px;
        
        span {
          display: none;
        }
      }
    }
  }
  
  .sidebar-logo {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 60px;
    padding: 15px 20px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.05);
    }
    
    .logo-image {
      width: 32px;
      height: 32px;
      border-radius: 6px;
    }
    
    .logo-text {
      margin-left: 10px;
      color: #ffffff;
      font-size: 18px;
      font-weight: bold;
      white-space: nowrap;
    }
  }
  
  .sidebar-menu {
    flex: 1;
    overflow: hidden;
    
    .menu-scrollbar {
      height: 100%;
      
      :deep(.el-scrollbar__wrap) {
        overflow-x: hidden;
      }
      
      .el-menu {
        border-right: none;
        height: 100%;
        
        &:not(.el-menu--collapse) {
          width: 100%;
        }
        
        .el-menu-item {
          height: 48px;
          line-height: 48px;
          
          &:hover {
            background-color: rgba(255, 255, 255, 0.05);
          }
          
          &.is-active {
            background-color: rgba(64, 158, 255, 0.1);
            
            &::after {
              content: '';
              position: absolute;
              right: 0;
              top: 0;
              bottom: 0;
              width: 3px;
              background-color: #409eff;
            }
          }
        }
        
        .el-sub-menu {
          .el-sub-menu__title {
            height: 48px;
            line-height: 48px;
            
            &:hover {
              background-color: rgba(255, 255, 255, 0.05);
            }
          }
        }
      }
    }
  }
  
  .sidebar-footer {
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    
    .collapse-toggle {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 48px;
      padding: 0 20px;
      color: #bfcbd9;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        color: #ffffff;
        background-color: rgba(255, 255, 255, 0.05);
      }
      
      .el-icon {
        margin-right: 8px;
        font-size: 18px;
      }
      
      span {
        font-size: 14px;
        white-space: nowrap;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .app-sidebar {
    transform: translateX(-100%);
    transition: transform 0.3s ease;
    
    &.mobile-open {
      transform: translateX(0);
    }
    
    &:not(.collapsed) {
      width: 100%;
      max-width: 300px;
    }
  }
}
</style>