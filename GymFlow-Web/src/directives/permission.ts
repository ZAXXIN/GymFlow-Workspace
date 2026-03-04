import type { App } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { PermissionCode } from '@/types/permission'

// 权限指令
export const permission = {
  mounted(el: HTMLElement, binding: any) {
    const authStore = useAuthStore()
    const { value } = binding
    
    // 如果没有权限，移除元素
    if (!authStore.hasPermission(value as PermissionCode | PermissionCode[])) {
      el.parentNode?.removeChild(el)
    }
  }
}

// 权限指令注册
export function setupPermissionDirective(app: App) {
  app.directive('permission', permission)
}

// 权限校验函数
export function checkPermission(permission: PermissionCode | PermissionCode[]): boolean {
  const authStore = useAuthStore()
  return authStore.hasPermission(permission)
}