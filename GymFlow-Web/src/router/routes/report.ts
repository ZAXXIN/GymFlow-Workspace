import type { RouteRecordRaw } from 'vue-router'
import type { PermissionCode } from '@/types/permission'

const reportRoutes: RouteRecordRaw[] = [
  {
    path: '/reports/revenue',
    name: 'RevenueReport',
    component: () => import('@/views/report/Revenue.vue'),
    meta: {
      title: '营收报表',
      icon: 'PieChart',
      requiresAuth: true,
      showInMenu: true,
      permissions: ['order:view'] as PermissionCode[]  // 营收报表需要的权限
    }
  },
  {
    path: '/reports/attendance',
    name: 'AttendanceReport',
    component: () => import('@/views/report/Attendance.vue'),
    meta: {
      title: '出勤报表',
      icon: 'TrendCharts',
      requiresAuth: true,
      showInMenu: true,
      permissions: ['checkIn:view'] as PermissionCode[]  // 出勤报表需要的权限
    }
  },
  {
    path: '/reports/performance',
    name: 'PerformanceReport',
    component: () => import('@/views/report/Performance.vue'),
    meta: {
      title: '业绩报表',
      icon: 'DataAnalysis',
      requiresAuth: true,
      showInMenu: true,
      permissions: ['order:view', 'member:view'] as PermissionCode[]  // 业绩报表需要的权限
    }
  }
]

export default reportRoutes