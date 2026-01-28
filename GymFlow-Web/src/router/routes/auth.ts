import type { RouteRecordRaw } from 'vue-router'

const authRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    meta: {
      title: '登录',
      requiresAuth: false,
      hideInMenu: true
    },
    component: () => import('@/views/auth/Login.vue')
  },
  // {
  //   path: '/register',
  //   name: 'Register',
  //   meta: {
  //     title: '注册',
  //     requiresAuth: false,
  //     hideInMenu: true
  //   },
  //   component: () => import('@/views/auth/Register.vue')
  // }
]

export default authRoutes