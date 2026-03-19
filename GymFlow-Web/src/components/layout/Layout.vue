<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <img src="@/assets/images/logo.png" alt="Logo" class="logo">
        <h1 class="title">GymFlow 健身房管理系统</h1>
      </div>
      <div class="header-right">
        <el-dropdown>
          <div class="user-info">
            <span class="username">{{ userInfo.realName || userInfo.username }}</span>
            <el-icon>
              <ArrowDown />
            </el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout">
                <el-icon>
                  <SwitchButton />
                </el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <div class="main-container">
      <!-- 侧边栏菜单 -->
      <el-aside width="200px" class="sidebar">
        <el-menu :default-active="activeMenu" router unique-opened @select="handleMenuSelect">
          <!-- 动态生成菜单 -->
          <template v-for="menu in filteredMenus" :key="menu.path">
            <!-- 有子菜单的情况 -->
            <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
              <template #title>
                <el-icon v-if="menu.icon">
                  <component :is="menu.icon" />
                </el-icon>
                <span>{{ menu.title }}</span>
              </template>
              <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
                <span>{{ child.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            <!-- 没有子菜单的情况 -->
            <el-menu-item v-else :index="menu.path">
              <el-icon v-if="menu.icon">
                <component :is="menu.icon" />
              </el-icon>
              <template #title>
                <span>{{ menu.title }}</span>
              </template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { UserInfo } from '@/types/auth'
import {
  User,
  SwitchButton,
  ArrowDown,
  DataLine,
  UserFilled,
  Avatar,
  Calendar,
  ShoppingCart,
  Check,
  Setting,
  PieChart,
  Goods,
  Document,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 用户信息
const userInfo = ref<UserInfo>({
  userId: 0,
  username: '',
  realName: '',
  phone: '',
  role: 0,
  status: 1,
})

// 完整的菜单配置（与路由的 meta 保持一致）
const allMenus = [
  // 仪表盘
  {
    path: '/dashboard',
    title: '仪表盘',
    icon: DataLine,
    permissions: [], // 所有角色可见
  },

  // 会员管理
  {
    path: '/member/list',
    title: '会员管理',
    icon: User,
    permissions: ['member:view'], // 需要会员查看权限
  },

  // 教练管理
  {
    path: '/coach/list',
    title: '教练管理',
    icon: Avatar,
    permissions: ['coach:view'], // 需要教练查看权限
  },

  // 课程管理
  {
    path: '/course/list',
    title: '课程管理',
    icon: Calendar,
    permissions: ['course:view'], // 需要课程查看权限
  },

  // 商品管理
  {
    path: '/product/list',
    title: '商品管理',
    icon: Goods,
    permissions: ['product:view'], // 需要商品查看权限
  },

  // 订单管理
  {
    path: '/order/list',
    title: '订单管理',
    icon: Document,
    permissions: ['order:view'], // 需要订单查看权限
  },

  // 签到管理
  {
    path: '/checkIn/list',
    title: '签到管理',
    icon: Check,
    permissions: ['checkIn:view'], // 需要签到查看权限
  },

  // 系统设置（只有老板有权限）
  {
    path: '/settings',
    title: '系统设置',
    icon: Setting,
    permissions: ['settings:user:view', 'settings:config:view'], // 需要任一系统设置权限
    children: [
      {
        path: '/settings/webUser',
        title: '用户管理',
        permissions: ['settings:user:view'], // 需要用户管理权限
      },
      {
        path: '/settings/systemConfig',
        title: '系统配置',
        permissions: ['settings:config:view'], // 需要系统配置权限
      },
      {
        path: '/settings/role',

        title: '角色权限',
        permissions: ['settings:role:view'],
      },
    ],
  },

  // 报表统计
  {
    path: '/reports',
    title: '报表统计',
    icon: PieChart,
    permissions: ['member:view', 'order:view', 'checkIn:view'], // 需要任一报表相关权限
    children: [
      {
        path: '/reports/member',
        title: '会员统计',
        permissions: ['member:view'],
      },
      {
        path: '/reports/finance',
        title: '财务统计',
        permissions: ['order:view'],
      },
      {
        path: '/reports/course',
        title: '课程统计',
        permissions: ['course:view'],
      },
    ],
  },
]

// 根据用户权限过滤菜单
const filteredMenus = computed(() => {
  // 递归过滤菜单
  const filterMenu = (menus: any[]) => {
    return menus.filter((menu) => {
      // 检查当前菜单是否有权限
      const hasMenuPermission =
        !menu.permissions ||
        menu.permissions.length === 0 ||
        menu.permissions.some((p: string) => authStore.hasPermission(p))

      // 如果有子菜单，递归过滤子菜单
      if (menu.children && menu.children.length > 0) {
        menu.children = filterMenu(menu.children)
        // 如果子菜单过滤后还有内容，或者当前菜单本身就有权限，则保留
        return menu.children.length > 0 || hasMenuPermission
      }

      return hasMenuPermission
    })
  }

  return filterMenu(allMenus)
})

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 处理菜单选择
const handleMenuSelect = (index: string) => {
  console.log('选择菜单:', index)
}

// 处理退出登录
const handleLogout = async () => {
  try {
    await authStore.logout()
  } catch (error) {
    console.error('登出失败:', error)
  } finally {
    // 清除本地存储
    localStorage.removeItem('gymflow_token')
    localStorage.removeItem('gymflow_user_info')

    // 跳转到登录页
    window.location.href = '/login'
  }
}

// 初始化用户信息
onMounted(() => {
  const userStr = localStorage.getItem('gymflow_user_info')
  if (userStr) {
    try {
      userInfo.value = JSON.parse(userStr)
    } catch (error) {
      console.error('解析用户信息失败:', error)
    }
  }
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  /* 修改为绿色渐变，与小程序主题一致 */
  background: linear-gradient(135deg, #07c160 0%, #05a350 100%);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 32px;
  height: 32px;
}

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.username {
  font-size: 14px;
}

.main-container {
  flex: 1;
  display: flex;
  min-height: 0;  /* 重要：允许flex子项收缩 */
  overflow: hidden;  /* 防止容器溢出 */
}

.sidebar {
  background-color: #fff;
  border-right: 1px solid #f0f0f0;
  height: 100%;
  overflow-y: auto;  /* 如果侧边栏内容过多，可以在侧边栏内部滚动 */
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;  /* 只在main区域滚动 */
  height: 100%;
}

/* 菜单样式优化 */
.el-menu {
  border-right: none;
}

.el-menu-item {
  height: 48px;
  line-height: 48px;
}

.el-sub-menu .el-menu-item {
  height: 40px;
  line-height: 40px;
  padding-left: 50px !important;
}

/* 可选：美化滚动条样式 */
.main-content::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.main-content::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: #ccc;
}

.main-content::-webkit-scrollbar-track {
  background: #f5f7fa;
}
</style>