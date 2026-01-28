import type { RouteRecordRaw } from 'vue-router'

const checkInRoutes: RouteRecordRaw[] = [
  {
    path: '/checkIns',
    name: 'CheckInList',
    component: () => import('@/views/checkIn/List.vue'),
    meta: {
      title: '签到记录',
      icon: 'Finished',
      requiresAuth: true,
      showInMenu:true,
      roles: ['ADMIN']
    }
  }
]
export default checkInRoutes