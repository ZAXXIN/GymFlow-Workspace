import type { RouteRecordRaw } from 'vue-router'

const authRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    meta: {
      title: '登录',
      requiresAuth: false,
      showInMenu: false
    },
    component: () => import('@/views/auth/Login.vue')
  }
]

export default authRoutes