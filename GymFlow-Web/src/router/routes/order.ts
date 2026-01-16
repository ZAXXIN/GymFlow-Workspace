import { RouteRecordRaw } from 'vue-router'

export const orderRoutes: RouteRecordRaw[] = [
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('@/views/order/List.vue'),
    meta: {
      title: '订单管理',
      icon: 'i-ep-shopping-cart',
      requiresAuth: true,
      roles: ['ADMIN']
    }
  },
  {
    path: '/orders/create',
    name: 'OrderCreate',
    component: () => import('@/views/order/Form.vue'),
    meta: {
      title: '创建订单',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'OrderList'
    }
  },
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: () => import('@/views/order/Detail.vue'),
    meta: {
      title: '订单详情',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'OrderList'
    }
  },
  {
    path: '/orders/:id/edit',
    name: 'OrderEdit',
    component: () => import('@/views/order/Form.vue'),
    meta: {
      title: '编辑订单',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'OrderList'
    }
  }
]