import { RouteRecordRaw } from 'vue-router'

export const dashboardRoutes: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/dashboard/Index.vue'),
    meta: {
      title: '仪表盘',
      icon: 'i-ep-data-board',
      requiresAuth: true,
      roles: ['ADMIN', 'COACH']
    }
  }
]