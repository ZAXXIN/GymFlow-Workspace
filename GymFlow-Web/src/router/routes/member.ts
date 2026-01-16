import { RouteRecordRaw } from 'vue-router'

export const memberRoutes: RouteRecordRaw[] = [
  {
    path: '/members',
    name: 'MemberList',
    component: () => import('@/views/member/List.vue'),
    meta: {
      title: '会员管理',
      icon: 'i-ep-user',
      requiresAuth: true,
      roles: ['ADMIN']
    }
  },
  {
    path: '/members/create',
    name: 'MemberCreate',
    component: () => import('@/views/member/Form.vue'),
    meta: {
      title: '新增会员',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'MemberList'
    }
  },
  {
    path: '/members/:id/edit',
    name: 'MemberEdit',
    component: () => import('@/views/member/Form.vue'),
    meta: {
      title: '编辑会员',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'MemberList'
    }
  },
  {
    path: '/members/:id',
    name: 'MemberDetail',
    component: () => import('@/views/member/Detail.vue'),
    meta: {
      title: '会员详情',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'MemberList'
    }
  },
  {
    path: '/members/:id/health',
    name: 'MemberHealth',
    component: () => import('@/views/member/HealthRecord.vue'),
    meta: {
      title: '健康档案',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'MemberDetail'
    }
  }
]