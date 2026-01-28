import type { RouteRecordRaw } from 'vue-router'

const dashboardRoutes: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/Index.vue'),
    meta: {
      title: '仪表盘',
      icon: 'DataBoard',
      showInMenu:true,
      requiresAuth: true,
      roles: ['ADMIN', 'COACH']
    }
  }
]
export default dashboardRoutes