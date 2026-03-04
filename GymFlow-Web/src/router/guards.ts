import type { RouteLocationNormalized, NavigationGuardNext } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePermission } from '@/composables/usePermission'
import { ElMessage } from 'element-plus'

export const setupRouterGuards = (router: any) => {
  // 全局前置守卫
  router.beforeEach(async (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    const authStore = useAuthStore()
    const { hasPermission } = usePermission()
    
    // 设置页面标题
    document.title = (to.meta?.title as string) || 'GymFlow'
    
    // 检查是否需要登录
    if (to.meta.requiresAuth && !authStore.isLoggedIn) {
      next('/login')
      return
    }
    
    // 检查页面权限
    if (to.meta.permissions) {
      const requiredPermissions = to.meta.permissions as string[]
      const hasRequiredPermission = requiredPermissions.some(p => hasPermission(p))
      
      if (!hasRequiredPermission) {
        ElMessage.error('没有权限访问该页面')
        next('/403')
        return
      }
    }
    
    // 已登录用户访问登录页时重定向到首页
    if (to.path === '/login' && authStore.isLoggedIn) {
      next('/dashboard')
      return
    }
    
    next()
  })
}