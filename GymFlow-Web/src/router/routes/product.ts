import type { RouteRecordRaw } from 'vue-router'
import type { PermissionCode } from '@/types/permission'

const productRoutes: RouteRecordRaw[] = [
  {
    path: '/product/list',  // 注意：这里统一改为小写 product
    name: 'ProductList',
    component: () => import('@/views/product/List.vue'),  // 注意：路径统一改为小写 product
    meta: {
      title: '商品管理',
      icon: 'Shop',
      requiresAuth: true,
      showInMenu: true,
      permissions: ['product:view'] as PermissionCode[]  // 查看商品列表需要的权限
    }
  },
  {
    path: '/product/add',
    name: 'ProductAdd',
    component: () => import('@/views/product/Add.vue'),
    meta: {
      title: '新增商品',
      requiresAuth: true,
      showInMenu: false,
      parent: 'ProductList',
      permissions: ['product:add'] as PermissionCode[]  // 新增商品需要的权限
    }
  },
  {
    path: '/product/edit/:id',
    name: 'ProductEdit',
    component: () => import('@/views/product/Add.vue'),
    meta: {
      title: '编辑商品',
      requiresAuth: true,
      showInMenu: false,
      parent: 'ProductList',
      permissions: ['product:edit'] as PermissionCode[]  // 编辑商品需要的权限
    }
  },
  {
    path: '/product/detail/:id',
    name: 'ProductDetail',
    component: () => import('@/views/product/Detail.vue'),
    meta: {
      title: '商品详情',
      requiresAuth: true,
      showInMenu: false,
      parent: 'ProductList',
      permissions: ['product:detail'] as PermissionCode[]  // 查看商品详情需要的权限
    }
  }
]

export default productRoutes