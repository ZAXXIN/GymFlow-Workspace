<template>
  <div class="app-header">
    <div class="header-left">
      <!-- 侧边栏折叠按钮 -->
      <el-icon class="collapse-icon" @click="toggleSidebar">
        <Fold v-if="!sidebar.collapse" />
        <Expand v-else />
      </el-icon>
      
      <!-- 面包屑导航 -->
      <AppBreadcrumb />
    </div>
    
    <div class="header-right">
      <!-- 搜索框 -->
      <div class="search-container">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索..."
          size="small"
          :prefix-icon="Search"
          @keyup.enter="handleSearch"
          style="width: 200px"
        />
      </div>
      
      <!-- 通知中心 -->
      <el-dropdown @command="handleNotificationCommand">
        <div class="notification-badge">
          <el-badge :value="unreadCount" :max="99">
            <el-icon class="notification-icon">
              <Bell />
            </el-icon>
          </el-badge>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="all">
              <div class="dropdown-item">
                <el-icon><Bell /></el-icon>
                全部通知
              </div>
            </el-dropdown-item>
            <el-dropdown-item command="system">
              <div class="dropdown-item">
                <el-icon><MessageBox /></el-icon>
                系统消息
              </div>
            </el-dropdown-item>
            <el-dropdown-item command="course">
              <div class="dropdown-item">
                <el-icon><Calendar /></el-icon>
                课程提醒
              </div>
            </el-dropdown-item>
            <el-dropdown-item command="order">
              <div class="dropdown-item">
                <el-icon><ShoppingCart /></el-icon>
                订单通知
              </div>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      
      <!-- 全屏切换 -->
      <el-tooltip content="全屏" placement="bottom">
        <el-icon class="fullscreen-icon" @click="toggleFullscreen">
          <FullScreen />
        </el-icon>
      </el-tooltip>
      
      <!-- 用户信息 -->
      <el-dropdown @command="handleUserCommand">
        <div class="user-info">
          <el-avatar :size="32" :src="userStore.userInfo?.avatar">
            {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
          </el-avatar>
          <span class="user-name">{{ userStore.userInfo?.realName || '用户' }}</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <div class="dropdown-item">
                <el-icon><User /></el-icon>
                个人资料
              </div>
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <div class="dropdown-item">
                <el-icon><Setting /></el-icon>
                系统设置
              </div>
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <div class="dropdown-item">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </div>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Fold,
  Expand,
  Search,
  Bell,
  FullScreen,
  ArrowDown,
  User,
  Setting,
  SwitchButton,
  MessageBox,
  Calendar,
  ShoppingCart
} from '@element-plus/icons-vue'
import AppBreadcrumb from './AppBreadcrumb.vue'
import { useUserStore } from '@/stores/user'
import { useSidebarStore } from '@/stores/sidebar'

const router = useRouter()
const userStore = useUserStore()
const sidebar = useSidebarStore()

const searchKeyword = ref('')
const unreadCount = ref(3) // 模拟未读通知数量

// 切换侧边栏
const toggleSidebar = () => {
  sidebar.toggleCollapse()
}

// 处理搜索
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    ElMessage.info(`搜索: ${searchKeyword.value}`)
    // 这里可以跳转到搜索页面或显示搜索结果
  }
}

// 处理通知命令
const handleNotificationCommand = (command: string) => {
  switch (command) {
    case 'all':
      router.push('/notifications')
      break
    case 'system':
    case 'course':
    case 'order':
      ElMessage.info(`查看${command}通知`)
      break
  }
}

// 处理用户命令
const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/settings/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 切换全屏
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().catch(err => {
      console.error('全屏失败:', err)
    })
  } else {
    document.exitFullscreen()
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch {
    // 用户取消操作
  }
}
</script>

<style scoped lang="scss">
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1000;

  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;

    .collapse-icon {
      font-size: 20px;
      color: #606266;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        color: #409eff;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .search-container {
      transition: all 0.3s;

      &:hover :deep(.el-input__wrapper) {
        box-shadow: 0 0 0 1px #409eff inset;
      }
    }

    .notification-badge {
      cursor: pointer;
      position: relative;

      .notification-icon {
        font-size: 20px;
        color: #606266;
        transition: all 0.3s;

        &:hover {
          color: #409eff;
        }
      }

      :deep(.el-badge__content) {
        border: 2px solid #fff;
      }
    }

    .fullscreen-icon {
      font-size: 20px;
      color: #606266;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        color: #409eff;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;
      transition: all 0.3s;

      &:hover {
        background: #f5f7fa;
      }

      .user-name {
        font-size: 14px;
        font-weight: 500;
        color: #303133;
      }

      .el-icon {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    font-size: 16px;
  }
}
</style>