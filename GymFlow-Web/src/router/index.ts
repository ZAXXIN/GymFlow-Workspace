import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// 导入路由模块
import authRoutes from './routes/auth'
import dashboardRoutes from './routes/dashboard'
import memberRoutes from './routes/member'
import coachRoutes from './routes/coach'
import courseRoutes from './routes/course'
import orderRoutes from './routes/order'
import checkinRoutes from './routes/checkIn'
import settingsRoutes from './routes/settings'
import reportRoutes from './routes/report'

// 合并所有路由
const routes: RouteRecordRaw[] = [
  ...authRoutes,
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      ...dashboardRoutes,
      ...memberRoutes,
      ...coachRoutes,
      ...courseRoutes,
      ...orderRoutes,
      ...checkinRoutes,
      ...settingsRoutes,
      ...reportRoutes
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 获取token
  const token = localStorage.getItem('gymflow_token') || sessionStorage.getItem('gymflow_token')
  
  // 路由白名单（不需要登录的页面）
  const whiteList = ['/login', '/register', '/forgot-password']
  
  // 如果用户已登录且访问登录页，重定向到首页
  if (token && to.path === '/login') {
    next('/dashboard')
    return
  }
  
  // 如果需要登录的页面且没有token，跳转到登录页
  if (!token && !whiteList.includes(to.path)) {
    next('/login')
    return
  }
  
  next()
})

export default router