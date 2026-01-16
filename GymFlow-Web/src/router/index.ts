import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuards } from './guards'
import { authRoutes } from './routes/auth'
import { dashboardRoutes } from './routes/dashboard'
import { memberRoutes } from './routes/member'
import { coachRoutes } from './routes/coach'
import { courseRoutes } from './routes/course'
import { bookingRoutes } from './routes/booking'
import { orderRoutes } from './routes/order'
import { checkinRoutes } from './routes/checkin'
import { reportRoutes } from './routes/report'
import { settingsRoutes } from './routes/settings'
import { errorRoutes } from './routes/error'
import Layout from '@/components/layout/Layout.vue'

const routes = [
  // 重定向首页
  {
    path: '/',
    redirect: '/dashboard'
  },
  
  // 认证相关页面（不包含在布局中）
  ...authRoutes,
  
  // 主布局页面
  {
    path: '/',
    component: Layout,
    children: [
      ...dashboardRoutes,
      ...memberRoutes,
      ...coachRoutes,
      ...courseRoutes,
      ...bookingRoutes,
      ...orderRoutes,
      ...checkinRoutes,
      ...reportRoutes,
      ...settingsRoutes
    ]
  },
  
  // 错误页面
  ...errorRoutes
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 设置路由守卫
setupRouterGuards(router)

export default router