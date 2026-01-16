import { RouteRecordRaw } from 'vue-router'

export const checkinRoutes: RouteRecordRaw[] = [
  {
    path: '/checkins',
    name: 'CheckInList',
    component: () => import('@/views/checkin/List.vue'),
    meta: {
      title: '签到记录',
      icon: 'i-ep-finished',
      requiresAuth: true,
      roles: ['ADMIN']
    }
  }
]