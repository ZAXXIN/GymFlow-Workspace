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
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="goToProfile">
                <el-icon><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <div class="main-container">
      <!-- 侧边栏菜单 -->
      <el-aside width="200px" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          router
          unique-opened
          @select="handleMenuSelect"
        >
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
              <el-menu-item
                v-for="child in menu.children"
                :key="child.path"
                :index="child.path"
              >
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
  Document,
  Setting,
  PieChart
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
  status: 1
})

// 完整的菜单配置
const allMenus = [
  // 仪表盘
  {
    path: '/dashboard',
    title: '仪表盘',
    icon: DataLine,
    roles: [0, 1, 2, 3] // 所有角色可见
  },
  
  // 会员管理
  {
    path: '/member',
    title: '会员管理',
    icon: UserFilled,
    roles: [0, 1], // 管理员和前台可见
    children: [
      { path: '/member/list', title: '会员列表' },
      { path: '/member/add', title: '添加会员' }
    ]
  },
  
  // 教练管理
  {
    path: '/coach',
    title: '教练管理',
    icon: Avatar,
    roles: [0, 1], // 管理员和前台可见
    children: [
      { path: '/coach/list', title: '教练列表' },
      { path: '/coach/add', title: '添加教练' },
      { path: '/coach/schedule', title: '排课管理' }
    ]
  },
  
  // 课程管理
  {
    path: '/course',
    title: '课程管理',
    icon: Calendar,
    roles: [0, 1, 2], // 管理员、前台、教练可见
    children: [
      { path: '/course/list', title: '课程列表' },
      { path: '/course/add', title: '添加课程' },
      { path: '/course/booking', title: '课程预约' }
    ]
  },
  
  // 订单管理
  {
    path: '/order',
    title: '订单管理',
    icon: ShoppingCart,
    roles: [0, 1], // 管理员和前台可见
    children: [
      { path: '/order/list', title: '订单列表' },
      { path: '/order/statistics', title: '销售统计' }
    ]
  },
  
  // 签到管理
  {
    path: '/checkin',
    title: '签到管理',
    icon: Check,
    roles: [0, 1, 2], // 管理员、前台、教练可见
    children: [
      { path: '/checkin/records', title: '签到记录' },
      { path: '/checkin/statistics', title: '签到统计' }
    ]
  },
  // 系统设置
  {
    path: '/settings',
    title: '系统设置',
    icon: Setting,
    roles: [0], // 仅管理员可见
    children: [
      { path: '/settings/user', title: '用户管理' },
      { path: '/settings/role', title: '角色权限' },
      { path: '/settings/system', title: '系统配置' }
    ]
  },
  
  // 报表统计
  {
    path: '/reports',
    title: '报表统计',
    icon: PieChart,
    roles: [0, 1], // 管理员和前台可见
    children: [
      { path: '/reports/member', title: '会员统计' },
      { path: '/reports/finance', title: '财务统计' },
      { path: '/reports/course', title: '课程统计' }
    ]
  }
]

// 根据用户角色过滤菜单
const filteredMenus = computed(() => {
  const role = userInfo.value.role
  return allMenus.filter(menu => menu.roles.includes(role))
})

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 处理菜单选择
const handleMenuSelect = (index: string) => {
  console.log('选择菜单:', index)
}

// 跳转到个人中心
const goToProfile = () => {
  router.push('/member/profile')
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
}

.header {
  background-color: #001529;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid #f0f0f0;
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
}

.sidebar {
  background-color: #fff;
  border-right: 1px solid #f0f0f0;
}

.main-content {
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
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
</style>