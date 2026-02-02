import type { RouteRecordRaw } from 'vue-router'

const productRoutes: RouteRecordRaw[] = [
  {
    path: '/Product/list',
    name: 'ProductList',
    component: () => import('@/views/Product/List.vue'),
    meta: {
      title: '商品管理',
      icon: 'Shop',
      requiresAuth: true,
      showInMenu:true,
    }
  },
  {
    path: '/Product/add',
    name: 'ProductAdd',
    component: () => import('@/views/Product/Add.vue'),
    meta: {
      title: '新增商品',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'ProductList'
    }
  },
  {
    path: '/Product/edit/:id',
    name: 'ProductEdit',
    component: () => import('@/views/Product/Add.vue'),
    meta: {
      title: '编辑商品',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'ProductList'
    }
  },
  {
    path: '/Product/detail/:id',
    name: 'ProductDetail',
    component: () => import('@/views/Product/Detail.vue'),
    meta: {
      title: '商品详情',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'ProductList'
    }
  },
]
export default productRoutes