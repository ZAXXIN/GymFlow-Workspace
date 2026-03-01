import type { RouteRecordRaw } from 'vue-router'

const bookingRoutes: RouteRecordRaw[] = [
  {
    path: '/bookings',
    name: 'BookingList',
    component: () => import('@/views/booking/List.vue'),
    meta: {
      title: '课程预约',
      icon: 'i-ep-timer',
      requiresAuth: true,
      showInMenu:true,
      roles: ['ADMIN', 'COACH']
    }
  },
]
export default bookingRoutes