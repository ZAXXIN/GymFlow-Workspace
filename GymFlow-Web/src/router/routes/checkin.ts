import type { RouteRecordRaw } from 'vue-router'
import type { PermissionCode } from '@/types/permission'

const checkInRoutes: RouteRecordRaw[] = [
  {
    path: '/checkIn/list',
    name: 'CheckInList',
    component: () => import('@/views/checkIn/List.vue'),
    meta: {
      title: '签到记录',
      icon: 'Finished',
      requiresAuth: true,
      showInMenu: true,
      permissions: ['checkIn:view'] as PermissionCode[]  // 查看签到列表需要的权限
    }
  },
  {
    path: '/checkIn/detail/:id',
    name: 'CheckInDetail',
    component: () => import('@/views/checkIn/Detail.vue'),
    meta: {
      title: '签到详情',
      requiresAuth: true,
      showInMenu: false, 
      parent: 'CheckInList',
      permissions: ['checkIn:detail'] as PermissionCode[]  // 查看详情需要的权限
    }
  }
]

export default checkInRoutes