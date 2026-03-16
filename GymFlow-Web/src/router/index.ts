import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 导入路由模块
import authRoutes from './routes/auth'
import dashboardRoutes from './routes/dashboard'
import memberRoutes from './routes/member'
import coachRoutes from './routes/coach'
import courseRoutes from './routes/course'
import productRoutes from './routes/product'
import orderRoutes from './routes/order'
import checkInRoutes from './routes/checkIn'
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
      ...productRoutes,
      ...orderRoutes,
      ...checkInRoutes,
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
// 路由白名单（不需要登录的页面）
const whiteList = ['/login', '/403', '/404']

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  document.title = (to.meta?.title as string) || 'GymFlow'
  
  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    // 如果需要登录，但用户未登录或token无效
    if (!authStore.isLoggedIn) {
      // 如果有token但未初始化，尝试验证
      if (authStore.token && !authStore.initialized) {
        const isValid = await authStore.validateToken()
        if (!isValid) {
          next('/login')
          return
        }
      } else {
        next('/login')
        return
      }
    }
  }
  
  // 检查页面权限
  if (to.meta.permissions && Array.isArray(to.meta.permissions) && to.meta.permissions.length > 0) {
    const requiredPermissions = to.meta.permissions as string[]
    const hasRequiredPermission = requiredPermissions.some(p => authStore.hasPermission(p))
    
    console.log("页面需要权限:", requiredPermissions)
    console.log("是否有权限:", hasRequiredPermission)
    
    if (!hasRequiredPermission) {
      next('/403')
      return
    }
  } else {
    // 没有设置permissions或permissions为空数组，直接放行
    console.log("页面无需特殊权限，放行")
  }
  
  // 已登录用户访问登录页时重定向到首页
  if (to.path === '/login' && authStore.isLoggedIn) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router