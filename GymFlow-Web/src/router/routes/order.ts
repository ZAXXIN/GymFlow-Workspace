import type { RouteRecordRaw } from 'vue-router'

const orderRoutes: RouteRecordRaw[] = [
  {
    path: '/order/list',
    name: 'OrderList',
    component: () => import('@/views/order/List.vue'),
    meta: {
      title: '订单管理',
      icon: 'Ticket',
      showInMenu:true,
      requiresAuth: true,
    }
  },
  {
    path: '/order/detail/:id',
    name: 'OrderDetail',
    component: () => import('@/views/order/Detail.vue'),
    meta: {
      title: '订单详情',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'OrderList'
    }
  },
]
export default orderRoutes