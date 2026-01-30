<template>
  <header class="app-header">
    <div class="header-left">
      <!-- 侧边栏折叠按钮 -->
      <div class="sidebar-toggle" @click="toggleSidebar">
        <el-icon>
          <component :is="isCollapsed ? 'Expand' : 'Fold'" />
        </el-icon>
      </div>

      <!-- Logo -->
      <div class="logo">
        <img src="@/assets/images/logo.png" alt="GymFlow" class="logo-image">
        <span class="logo-text" v-show="!isCollapsed">GymFlow 健身工作室</span>
      </div>
    </div>

    <div class="header-right">
      <!-- 消息通知 -->
      <el-dropdown v-if="showNotifications" trigger="click" class="notification-dropdown">
        <div class="notification-icon">
          <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
            <el-icon :size="20">
              <Bell />
            </el-icon>
          </el-badge>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item v-if="notifications.length === 0">
              暂无通知
            </el-dropdown-item>
            <template v-else>
              <el-dropdown-item v-for="notification in notifications.slice(0, 5)" :key="notification.id" :class="{ 'unread': !notification.read }" @click="handleNotificationClick(notification)">
                <div class="notification-item">
                  <div class="notification-title">
                    {{ notification.title }}
                  </div>
                  <div class="notification-time">
                    {{ formatTime(notification.time) }}
                  </div>
                </div>
              </el-dropdown-item>
              <el-dropdown-item divided>
                <div class="view-all" @click="viewAllNotifications">
                  查看所有通知
                </div>
              </el-dropdown-item>
            </template>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 全屏切换 -->
      <div class="fullscreen-toggle" @click="toggleFullscreen">
        <el-icon :size="20">
          <component :is="isFullscreen ? 'FullScreen' : 'CropFree'" />
        </el-icon>
      </div>

      <!-- 用户信息 -->
      <el-dropdown trigger="click" class="user-dropdown">
        <div class="user-info">
          <span class="user-name" v-show="!isCollapsed">
            {{ authStore.userInfo?.username || '用户' }}
          </span>
          <el-icon class="user-arrow">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item divided @click="userLogout">
              <el-icon>
                <SwitchButton />
              </el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { formatTime } from '@/utils'
import type { Notification } from '@/types'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'
import { useUserStore } from '@/stores/user'

const authStore = useAuthStore()
const userStore = useUserStore()
const settingsStore = useSettingsStore()
const router = useRouter()

// 响应式数据
const isFullscreen = ref(false)
const notifications = ref<Notification[]>([
  {
    id: '1',
    title: '新会员注册',
    content: '张三刚刚注册成为会员',
    time: new Date(Date.now() - 1000 * 60 * 30),
    read: false,
    type: 'member',
  },
  {
    id: '2',
    title: '课程预约提醒',
    content: '您的课程将在30分钟后开始',
    time: new Date(Date.now() - 1000 * 60 * 60),
    read: true,
    type: 'course',
  },
])

// 计算属性
const userInfo = computed(() => userStore.currentUser)
const isCollapsed = computed(() => settingsStore.sidebarCollapsed)
const showNotifications = computed(() => settingsStore.showNotifications)
const unreadCount = computed(() => notifications.value.filter((n) => !n.read).length)

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
      isFullscreen.value = false
    }
  }
  console.log(userInfo)
}

const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

const userLogout = async () => {
  try {
    await authStore.logout();
    router.replace('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

const handleNotificationClick = (notification: Notification) => {
  notification.read = true

  // 根据通知类型跳转到不同页面
  switch (notification.type) {
    case 'member':
      router.push('/member/list')
      break
    case 'course':
      router.push('/course/schedule')
      break
    case 'order':
      router.push('/order/list')
      break
  }
}

const viewAllNotifications = () => {
  // 跳转到通知页面
  console.log('查看所有通知')
}

// 生命周期
onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>

<style scoped lang="scss">
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  z-index: 1000;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  .header-left {
    display: flex;
    align-items: center;
    flex: 0 0 auto;

    .sidebar-toggle {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      margin-right: 10px;
      cursor: pointer;
      color: #ffffff;
      border-radius: 50%;
      transition: background-color 0.3s;

      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }

      .el-icon {
        font-size: 20px;
      }
    }

    .logo {
      display: flex;
      align-items: center;
      cursor: pointer;

      .logo-image {
        width: 36px;
        height: 36px;
        margin-right: 10px;
        border-radius: 6px;
      }

      .logo-text {
        color: #ffffff;
        font-size: 18px;
        font-weight: bold;
        white-space: nowrap;
      }
    }
  }

  .header-center {
    flex: 1;
    max-width: 500px;
    margin: 0 20px;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .notification-dropdown,
    .user-dropdown {
      cursor: pointer;
    }

    .notification-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      color: #ffffff;
      border-radius: 50%;
      transition: background-color 0.3s;

      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }
    }

    .fullscreen-toggle {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      color: #ffffff;
      border-radius: 50%;
      cursor: pointer;
      transition: background-color 0.3s;

      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 4px 8px;
      border-radius: 20px;
      transition: background-color 0.3s;

      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }
      .user-name {
        color: #ffffff;
        font-size: 14px;
        max-width: 100px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .user-arrow {
        color: #ffffff;
        font-size: 12px;
      }
    }
  }
}

.notification-item {
  min-width: 200px;
  padding: 5px 0;

  .notification-title {
    font-size: 14px;
    color: #333;
    margin-bottom: 2px;
  }

  .notification-time {
    font-size: 12px;
    color: #999;
  }

  &.unread {
    .notification-title {
      font-weight: bold;
    }
  }
}

.view-all {
  text-align: center;
  color: #1890ff;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .app-header {
    padding: 0 10px;

    .logo-text {
      display: none;
    }

    .header-center {
      display: none;
    }

    .user-name {
      display: none;
    }

    .user-arrow {
      display: none;
    }
  }
}
</style>