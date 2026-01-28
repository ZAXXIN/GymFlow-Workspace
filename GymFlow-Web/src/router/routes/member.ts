import type { RouteRecordRaw } from 'vue-router'

const memberRoutes: RouteRecordRaw[] = [
  {
    path: '/member/list',
    name: 'MemberList',
    component: () => import('@/views/member/List.vue'),
    meta: {
      title: '会员管理',
      icon: 'User',
      requiresAuth: true,
      showInMenu:true,
    }
  },
  {
    path: '/member/add',
    name: 'MemberAdd',
    component: () => import('@/views/member/Add.vue'),
    meta: {
      title: '新增会员',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'MemberList'
    }
  },
  {
    path: '/member/edit/:id',
    name: 'MemberEdit',
    component: () => import('@/views/member/Add.vue'),
    meta: {
      title: '编辑会员',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'MemberList'
    }
  },
  {
    path: '/member/detail/:id',
    name: 'MemberDetail',
    component: () => import('@/views/member/Detail.vue'),
    meta: {
      title: '会员详情',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'MemberList'
    }
  }
]
export default memberRoutes