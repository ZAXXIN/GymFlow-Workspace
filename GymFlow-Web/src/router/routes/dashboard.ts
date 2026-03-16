import type { RouteRecordRaw } from 'vue-router'
import type { PermissionCode } from '@/types/permission'

const dashboardRoutes: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/Index.vue'),
    meta: {
      title: '仪表盘',
      icon: 'DataBoard',
      showInMenu: true,
      requiresAuth: true,
      permissions: [] // 仪表盘所有人都可见
    }
  }
]

export default dashboardRoutes