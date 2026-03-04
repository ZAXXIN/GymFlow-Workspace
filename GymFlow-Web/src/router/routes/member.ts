import type { RouteRecordRaw } from 'vue-router'
import type { PermissionCode } from '@/types/permission'

const memberRoutes: RouteRecordRaw[] = [
  {
    path: '/member/list',
    name: 'MemberList',
    component: () => import('@/views/member/List.vue'),
    meta: {
      title: '会员管理',
      icon: 'User',
      requiresAuth: true,
      showInMenu: true,
      permissions: ['member:view'] as PermissionCode[]  // 查看会员列表需要的权限
    }
  },
  {
    path: '/member/add',
    name: 'MemberAdd',
    component: () => import('@/views/member/Add.vue'),
    meta: {
      title: '新增会员',
      requiresAuth: true,
      showInMenu: false,
      parent: 'MemberList',
      permissions: ['member:add'] as PermissionCode[]  // 新增会员需要的权限
    }
  },
  {
    path: '/member/edit/:id',
    name: 'MemberEdit',
    component: () => import('@/views/member/Add.vue'),
    meta: {
      title: '编辑会员',
      requiresAuth: true,
      showInMenu: false,
      parent: 'MemberList',
      permissions: ['member:edit'] as PermissionCode[]  // 编辑会员需要的权限
    }
  },
  {
    path: '/member/detail/:id',
    name: 'MemberDetail',
    component: () => import('@/views/member/Detail.vue'),
    meta: {
      title: '会员详情',
      requiresAuth: true,
      showInMenu: false,
      parent: 'MemberList',
      permissions: ['member:detail'] as PermissionCode[]  // 查看详情需要的权限
    }
  }
]

export default memberRoutes