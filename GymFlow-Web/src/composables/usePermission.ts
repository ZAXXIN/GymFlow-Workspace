import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUserStore } from '@/stores/user'
import type { UserRole, Permission } from '@/types'

/**
 * 权限管理组合式函数
 * @returns 权限相关的状态和方法
 */
export function usePermission() {
  const router = useRouter()
  const authStore = useAuthStore()
  const userStore = useUserStore()
  
  // 当前用户角色
  const currentRole = computed<UserRole | null>(() => {
    return userStore.currentUser?.role || null
  })
  
  // 当前用户权限列表
  const currentPermissions = computed<Permission[]>(() => {
    return userStore.currentUser?.permissions || []
  })
  
  // 检查是否已登录
  const isAuthenticated = computed<boolean>(() => {
    return authStore.isAuthenticated
  })
  
  // 检查是否有特定角色
  const hasRole = (role: UserRole | UserRole[]): boolean => {
    if (!currentRole.value) return false
    
    if (Array.isArray(role)) {
      return role.includes(currentRole.value)
    }
    
    return currentRole.value === role
  }
  
  // 检查是否有任意角色
  const hasAnyRole = (roles: UserRole[]): boolean => {
    return roles.some(role => hasRole(role))
  }
  
  // 检查是否有所有角色
  const hasAllRoles = (roles: UserRole[]): boolean => {
    return roles.every(role => hasRole(role))
  }
  
  // 检查是否有特定权限
  const hasPermission = (permission: Permission | string): boolean => {
    if (!currentPermissions.value.length) return false
    
    return currentPermissions.value.some(p => 
      typeof permission === 'string' ? p.code === permission : p.code === permission.code
    )
  }
  
  // 检查是否有任意权限
  const hasAnyPermission = (permissions: (Permission | string)[]): boolean => {
    return permissions.some(permission => hasPermission(permission))
  }
  
  // 检查是否有所有权限
  const hasAllPermissions = (permissions: (Permission | string)[]): boolean => {
    return permissions.every(permission => hasPermission(permission))
  }
  
  // 根据权限过滤菜单
  const filterMenuByPermission = <T extends { permission?: string; children?: T[] }>(
    menuItems: T[]
  ): T[] => {
    return menuItems.filter(item => {
      // 如果菜单项没有权限要求，或者用户有权限，则显示
      const hasAccess = !item.permission || hasPermission(item.permission)
      
      // 递归过滤子菜单
      if (item.children && item.children.length > 0) {
        item.children = filterMenuByPermission(item.children)
        
        // 如果过滤后子菜单为空，且菜单项本身没有权限要求，则隐藏该菜单项
        if (item.children.length === 0 && !item.permission) {
          return false
        }
      }
      
      return hasAccess
    })
  }
  
  // 检查路由访问权限
  const checkRouteAccess = (routeName: string): boolean => {
    const route = router.getRoutes().find(r => r.name === routeName)
    if (!route) return false
    
    // 检查路由元信息中的权限要求
    const meta = route.meta
    if (!meta) return true
    
    // 检查角色要求
    if (meta.roles && meta.roles.length > 0) {
      if (!hasAnyRole(meta.roles)) {
        return false
      }
    }
    
    // 检查权限要求
    if (meta.permissions && meta.permissions.length > 0) {
      if (!hasAnyPermission(meta.permissions)) {
        return false
      }
    }
    
    return true
  }
  
  // 跳转到无权限页面
  const redirectToNoPermission = () => {
    router.push({ name: '403' })
  }
  
  // 跳转到登录页面
  const redirectToLogin = (redirect?: string) => {
    router.push({
      name: 'login',
      query: redirect ? { redirect } : undefined
    })
  }
  
  // 检查并处理访问权限
  const checkAccess = (options: {
    roles?: UserRole[]
    permissions?: (Permission | string)[]
    redirectTo?: string
  } = {}): boolean => {
    // 检查是否已登录
    if (!isAuthenticated.value) {
      redirectToLogin(options.redirectTo)
      return false
    }
    
    // 检查角色权限
    if (options.roles && options.roles.length > 0) {
      if (!hasAnyRole(options.roles)) {
        redirectToNoPermission()
        return false
      }
    }
    
    // 检查操作权限
    if (options.permissions && options.permissions.length > 0) {
      if (!hasAnyPermission(options.permissions)) {
        redirectToNoPermission()
        return false
      }
    }
    
    return true
  }
  
  // 权限检查装饰器（用于组合式函数）
  const withPermission = <T extends (...args: any[]) => any>(
    fn: T,
    options: {
      roles?: UserRole[]
      permissions?: (Permission | string)[]
      errorMessage?: string
    } = {}
  ): T => {
    return ((...args: Parameters<T>): ReturnType<T> => {
      if (!checkAccess(options)) {
        if (options.errorMessage) {
          throw new Error(options.errorMessage)
        }
        throw new Error('权限不足')
      }
      
      return fn(...args)
    }) as T
  }
  
  // 获取用户角色显示文本
  const getRoleText = (role: UserRole): string => {
    const roleTextMap: Record<UserRole, string> = {
      MEMBER: '会员',
      COACH: '教练',
      ADMIN: '管理员'
    }
    
    return roleTextMap[role] || role
  }
  
  // 获取用户权限显示文本
  const getPermissionText = (permissionCode: string): string => {
    const permissionTextMap: Record<string, string> = {
      'member:view': '查看会员',
      'member:create': '创建会员',
      'member:edit': '编辑会员',
      'member:delete': '删除会员',
      'coach:view': '查看教练',
      'coach:create': '创建教练',
      'coach:edit': '编辑教练',
      'coach:delete': '删除教练',
      'course:view': '查看课程',
      'course:create': '创建课程',
      'course:edit': '编辑课程',
      'course:delete': '删除课程',
      'order:view': '查看订单',
      'order:create': '创建订单',
      'order:edit': '编辑订单',
      'order:delete': '删除订单',
      'report:view': '查看报表',
      'report:export': '导出报表',
      'system:settings': '系统设置',
      'system:users': '用户管理'
    }
    
    return permissionTextMap[permissionCode] || permissionCode
  }
  
  return {
    // 计算属性
    currentRole,
    currentPermissions,
    isAuthenticated,
    
    // 权限检查方法
    hasRole,
    hasAnyRole,
    hasAllRoles,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    
    // 权限过滤方法
    filterMenuByPermission,
    checkRouteAccess,
    
    // 路由跳转方法
    redirectToNoPermission,
    redirectToLogin,
    
    // 综合权限检查
    checkAccess,
    withPermission,
    
    // 显示文本方法
    getRoleText,
    getPermissionText
  }
}