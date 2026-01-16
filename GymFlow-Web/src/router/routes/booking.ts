import { RouteRecordRaw } from 'vue-router'

export const bookingRoutes: RouteRecordRaw[] = [
  {
    path: '/bookings',
    name: 'BookingList',
    component: () => import('@/views/booking/List.vue'),
    meta: {
      title: '课程预约',
      icon: 'i-ep-timer',
      requiresAuth: true,
      roles: ['ADMIN', 'COACH']
    }
  },
  {
    path: '/checkin',
    name: 'CheckIn',
    component: () => import('@/views/booking/CheckIn.vue'),
    meta: {
      title: '签到管理',
      icon: 'i-ep-check',
      requiresAuth: true,
      roles: ['ADMIN', 'COACH']
    }
  }
]