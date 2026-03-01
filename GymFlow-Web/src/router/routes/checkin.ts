import type { RouteRecordRaw } from 'vue-router'

const checkInRoutes: RouteRecordRaw[] = [
  {
    path: '/checkIn/list',
    name: 'CheckInList',
    component: () => import('@/views/checkIn/List.vue'),
    meta: {
      title: '签到记录',
      icon: 'Finished',
      requiresAuth: true,
      showInMenu:true,
    }
  },
  {
    path: '/checkIn/detail/:id',
    name: 'CheckInDetail',
    component: () => import('@/views/checkIn/Detail.vue'),
    meta: {
      title: '签到详情',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'CheckInList'
    }
  },
]
export default checkInRoutes