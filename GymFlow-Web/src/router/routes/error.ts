import type { RouteRecordRaw } from 'vue-router'

const errorRoutes: RouteRecordRaw[] = [
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: {
      title: '403 Forbidden',
      requiresAuth: false,
      showInMenu: false
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '404 Not Found',
      requiresAuth: false,
      showInMenu: false
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

export default errorRoutes