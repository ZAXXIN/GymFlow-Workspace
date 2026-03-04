import type { RouteRecordRaw } from 'vue-router'
import type { PermissionCode } from '@/types/permission'

const orderRoutes: RouteRecordRaw[] = [
  {
    path: '/order/list',
    name: 'OrderList',
    component: () => import('@/views/order/List.vue'),
    meta: {
      title: '订单管理',
      icon: 'Ticket',
      showInMenu: true,
      requiresAuth: true,
      permissions: ['order:view'] as PermissionCode[]  // 查看订单列表需要的权限
    }
  },
  {
    path: '/order/detail/:id',
    name: 'OrderDetail',
    component: () => import('@/views/order/Detail.vue'),
    meta: {
      title: '订单详情',
      requiresAuth: true,
      showInMenu: false,
      parent: 'OrderList',
      permissions: ['order:detail'] as PermissionCode[]  // 查看订单详情需要的权限
    }
  }
]

export default orderRoutes