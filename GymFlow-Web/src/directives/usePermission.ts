import { useAuthStore } from '@/stores/auth'
import type { PermissionCode } from '@/types/permission'
import type { RouteRecordRaw } from 'vue-router'

export function usePermission() {
  const authStore = useAuthStore()

  /**
   * 检查是否有权限
   */
  const hasPermission = (permission: PermissionCode | PermissionCode[]): boolean => {
    return authStore.hasPermission(permission)
  }

  /**
   * 检查是否有所有权限
   */
  const hasAllPermissions = (permissions: PermissionCode[]): boolean => {
    return authStore.hasAllPermissions(permissions)
  }

  /**
   * 过滤路由（基于权限）
   */
  const filterRoutesByPermission = (routes: RouteRecordRaw[]): RouteRecordRaw[] => {
    return routes.filter(route => {
      const requiredPermissions = route.meta?.permissions as PermissionCode[] | undefined
      
      // 如果没有权限要求，则显示
      if (!requiredPermissions || requiredPermissions.length === 0) {
        return true
      }
      
      // 检查权限
      return hasPermission(requiredPermissions)
    })
  }

  /**
   * 过滤菜单（基于权限）
   */
  const filterMenuByPermission = (menus: any[]): any[] => {
    return menus.filter(menu => {
      const requiredPermissions = menu.permissions as PermissionCode[] | undefined
      
      // 如果没有权限要求，则显示
      if (!requiredPermissions || requiredPermissions.length === 0) {
        return true
      }
      
      // 检查权限
      const hasMenuPermission = hasPermission(requiredPermissions)
      
      // 如果有子菜单，递归过滤
      if (menu.children && menu.children.length > 0) {
        menu.children = filterMenuByPermission(menu.children)
        return menu.children.length > 0 || hasMenuPermission
      }
      
      return hasMenuPermission
    })
  }

  return {
    hasPermission,
    hasAllPermissions,
    filterRoutesByPermission,
    filterMenuByPermission
  }
}