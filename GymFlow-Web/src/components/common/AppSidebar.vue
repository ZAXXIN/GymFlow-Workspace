<template>
  <div class="app-sidebar" :class="{ 'collapsed': sidebar.collapse }">
    <!-- Logo区域 -->
    <div class="sidebar-logo" @click="router.push('/')">
      <img v-if="!sidebar.collapse" src="@/assets/images/logo.png" alt="GymFlow" class="logo-image">
      <div v-else class="logo-mini">GF</div>
      <h1 v-if="!sidebar.collapse" class="logo-text">GymFlow</h1>
    </div>

    <!-- 菜单区域 -->
    <el-scrollbar class="sidebar-scrollbar">
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebar.collapse"
        :unique-opened="true"
        :collapse-transition="false"
        router
        @select="handleMenuSelect"
      >
        <SidebarItem
          v-for="route in permissionRoutes"
          :key="route.path"
          :item="route"
          :base-path="route.path"
          :is-collapse="sidebar.collapse"
        />
      </el-menu>
    </el-scrollbar>

    <!-- 折叠按钮 -->
    <div class="sidebar-footer">
      <el-tooltip :content="sidebar.collapse ? '展开菜单' : '折叠菜单'" placement="right">
        <div class="collapse-btn" @click="toggleCollapse">
          <el-icon :size="18">
            <DArrowLeft v-if="!sidebar.collapse" />
            <DArrowRight v-else />
          </el-icon>
        </div>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DArrowLeft, DArrowRight } from '@element-plus/icons-vue'
import SidebarItem from './SidebarItem.vue'
import { useSidebarStore } from '@/stores/sidebar'
import { usePermissionStore } from '@/stores/permission'

const router = useRouter()
const route = useRoute()
const sidebar = useSidebarStore()
const permissionStore = usePermissionStore()

// 计算当前激活的菜单
const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) {
    return meta.activeMenu as string
  }
  return path
})

// 获取权限路由
const permissionRoutes = computed(() => {
  return permissionStore.routes
})

// 切换折叠状态
const toggleCollapse = () => {
  sidebar.toggleCollapse()
}

// 菜单选择处理
const handleMenuSelect = (index: string, indexPath: string[]) => {
  console.log('菜单选择:', index, indexPath)
}
</script>

<style scoped lang="scss">
.app-sidebar {
  width: 220px;
  height: 100vh;
  background: linear-gradient(180deg, #304156 0%, #263445 100%);
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 999;

  &.collapsed {
    width: 64px;
  }

  .sidebar-logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 20px;
    cursor: pointer;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    transition: all 0.3s;

    &:hover {
      background: rgba(255, 255, 255, 0.05);
    }

    .logo-image {
      height: 32px;
      width: auto;
    }

    .logo-mini {
      width: 36px;
      height: 36px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: bold;
      font-size: 18px;
      letter-spacing: 1px;
    }

    .logo-text {
      margin-left: 12px;
      color: white;
      font-size: 20px;
      font-weight: 600;
      white-space: nowrap;
      overflow: hidden;
      transition: all 0.3s;
    }
  }

  .sidebar-scrollbar {
    flex: 1;
    overflow: hidden;

    :deep(.el-scrollbar__wrap) {
      overflow-x: hidden;
    }

    :deep(.el-menu) {
      border: none;
      background: transparent;

      .el-menu-item,
      .el-sub-menu__title {
        color: #bfcbd9;
        background: transparent !important;
        height: 50px;
        line-height: 50px;
        margin: 2px 0;
        border-radius: 0;

        &:hover {
          color: #fff;
          background: rgba(255, 255, 255, 0.1) !important;
        }

        &.is-active {
          color: #409eff;
          background: rgba(64, 158, 255, 0.1) !important;
          border-right: 3px solid #409eff;
        }

        .el-icon {
          font-size: 18px;
          margin-right: 8px;
        }
      }

      .el-sub-menu {
        .el-menu {
          background: rgba(0, 0, 0, 0.2) !important;
        }

        &.is-active > .el-sub-menu__title {
          color: #409eff;
        }
      }
    }
  }

  .sidebar-footer {
    height: 50px;
    border-top: 1px solid rgba(255, 255, 255, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;

    .collapse-btn {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(255, 255, 255, 0.1);
      border-radius: 4px;
      cursor: pointer;
      color: #bfcbd9;
      transition: all 0.3s;

      &:hover {
        background: rgba(255, 255, 255, 0.2);
        color: #fff;
      }
    }
  }
}

// 菜单折叠时的样式
:deep(.el-menu--collapse) {
  .el-sub-menu__title {
    .el-sub-menu__icon-arrow {
      display: none;
    }

    span {
      display: none;
    }
  }

  .el-menu-item {
    .el-icon {
      margin-right: 0;
    }

    span {
      display: none;
    }
  }
}
</style>