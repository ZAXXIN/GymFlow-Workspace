import type { RouteLocationNormalized, NavigationGuardNext } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useSettingsStore } from '@/stores/settings'
import { PageTitles } from '@/utils/constants'
import { checkRoutePermission } from '@/utils'

export const setupRouterGuards = (router: any) => {
  // 全局前置守卫
  router.beforeEach(async (to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    const authStore = useAuthStore()
    const settingsStore = useSettingsStore()
    
    // 设置页面标题
    const title = PageTitles[to.name as keyof typeof PageTitles] || 'GymFlow'
    document.title = title
    
    // 检查是否需要登录
    if (to.meta.requiresAuth && !authStore.isLoggedIn) {
      next('/login')
      return
    }
    
    // 检查角色权限
    if (to.meta.roles && authStore.userRole) {
      const hasPermission = checkRoutePermission(to, authStore.userRole)
      if (!hasPermission) {
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
  
  // 全局后置守卫
  router.afterEach((to: RouteLocationNormalized) => {
    // 可以在这里添加页面访问统计等
  })
}