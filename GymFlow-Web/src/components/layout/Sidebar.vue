<template>
  <aside class="app-sidebar" :class="{ 'collapsed': isCollapsed }">
    <!-- Logo区域 -->
    <div class="sidebar-logo" @click="goHome">
      <img src="@/assets/images/logo.png" alt="GymFlow" class="logo-image">
      <span class="logo-text" v-show="!isCollapsed">GymFlow</span>
    </div>
    
    <!-- 菜单区域 -->
    <div class="sidebar-menu">
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :collapse-transition="false"
          :unique-opened="true"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
          @select="handleMenuSelect"
        >
          <sidebar-item
            v-for="route in menuRoutes"
            :key="route.path"
            :item="route"
          />
        </el-menu>
      </el-scrollbar>
    </div>
    
    <!-- 底部操作 -->
    <!-- <div class="sidebar-footer">
      <div class="collapse-toggle" @click="toggleCollapse">
        <el-icon>
          <component :is="isCollapsed ? 'Expand' : 'Fold'" />
        </el-icon>
        <span v-show="!isCollapsed">收起菜单</span>
      </div>
    </div> -->
  </aside>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { usePermission } from '@/composables/usePermission'
import { useSettingsStore } from '@/stores/settings'
// import { filterMenuByPermission } from '@/composables/usePermission'
// import SidebarItem from '../common/SidebarItem.vue'
import type { RouteRecordRaw } from 'vue-router'

const router = useRouter()
const route = useRoute()
const { filterMenuByPermission } = usePermission()
const settingsStore = useSettingsStore()

// 菜单路由配置
const menuRoutes = computed(() => {
  const allRoutes = router.getRoutes()
  // console.log('所有路由及meta：', allRoutes.map(route => ({
  //   path: route.path,
  //   name: route.name,
  //   showInMenu: route.meta?.showInMenu,
  //   hidden: route.meta?.hidden,
  //   isMatch: route.meta?.showInMenu && !route.meta?.hidden
  // })))

  const routes = allRoutes.filter(route => {
    // console.log('当前路由：', route.path, '，是否满足条件：', route.meta?.showInMenu && !route.meta?.hidden)
    return route.meta?.showInMenu && !route.meta?.hidden
  })  

  // console.log('过滤后路由数量：', routes.length) 
  // 过滤权限
  const permissionRoutes = filterMenuByPermission(routes as RouteRecordRaw[])
  // console.log('权限过滤后路由数量：', permissionRoutes.length) 
  return permissionRoutes
})
// const menuRoutes = computed(() => {
//   const routes = router.getRoutes().filter(route => {
//     console.log(route,1)
//     return route.meta?.showInMenu && !route.meta?.hidden
//     // return true
//   })  
//   // 过滤权限
//   return filterMenuByPermission(routes as RouteRecordRaw[])
// })
// console.log(menuRoutes,2)


// 当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta?.activeMenu) {
    return meta.activeMenu as string
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
  // 菜单选择逻辑已由 SidebarItem 组件处理
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