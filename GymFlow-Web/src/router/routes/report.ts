import { RouteRecordRaw } from 'vue-router'

export const reportRoutes: RouteRecordRaw[] = [
  {
    path: '/reports/revenue',
    name: 'RevenueReport',
    component: () => import('@/views/report/Revenue.vue'),
    meta: {
      title: '营收报表',
      icon: 'i-ep-pie-chart',
      requiresAuth: true,
      roles: ['ADMIN']
    }
  },
  {
    path: '/reports/attendance',
    name: 'AttendanceReport',
    component: () => import('@/views/report/Attendance.vue'),
    meta: {
      title: '出勤报表',
      icon: 'i-ep-trend-charts',
      requiresAuth: true,
      roles: ['ADMIN']
    }
  },
  {
    path: '/reports/performance',
    name: 'PerformanceReport',
    component: () => import('@/views/report/Performance.vue'),
    meta: {
      title: '业绩报表',
      icon: 'i-ep-data-analysis',
      requiresAuth: true,
      roles: ['ADMIN']
    }
  }
]