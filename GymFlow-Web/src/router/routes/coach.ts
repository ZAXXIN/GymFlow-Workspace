import type { RouteRecordRaw } from 'vue-router'
import type { PermissionCode } from '@/types/permission'

const coachRoutes: RouteRecordRaw[] = [
  {
    path: '/coach/list',
    name: 'CoachList',
    component: () => import('@/views/coach/List.vue'),
    meta: {
      title: '教练管理',
      icon: 'UserFilled',
      requiresAuth: true,
      showInMenu: true,
      permissions: ['coach:view'] as PermissionCode[]  // 查看教练列表需要的权限
    }
  },
  {
    path: '/coach/add',
    name: 'CoachCreate',
    component: () => import('@/views/coach/Add.vue'),
    meta: {
      title: '新增教练',
      requiresAuth: true,
      showInMenu: false,
      parent: 'CoachList',
      permissions: ['coach:add'] as PermissionCode[]  // 新增教练需要的权限
    }
  },
  {
    path: '/coach/edit/:id',
    name: 'CoachEdit',
    component: () => import('@/views/coach/Add.vue'),
    meta: {
      title: '编辑教练',
      requiresAuth: true,
      showInMenu: false,
      parent: 'CoachList',
      permissions: ['coach:edit'] as PermissionCode[]  // 编辑教练需要的权限
    }
  },
  {
    path: '/coach/detail/:id',
    name: 'CoachDetail',
    component: () => import('@/views/coach/Detail.vue'),
    meta: {
      title: '教练详情',
      requiresAuth: true,
      showInMenu: false,
      parent: 'CoachList',
      permissions: ['coach:detail'] as PermissionCode[]  // 查看详情需要的权限
    }
  },
  {
    path: '/coach/schedule/:id',
    name: 'CoachSchedule',
    component: () => import('@/views/coach/Schedule.vue'),
    meta: {
      title: '教练排班',
      requiresAuth: true,
      showInMenu: false,
      parent: 'CoachList',
      permissions: ['coach:schedule:view'] as PermissionCode[]  // 查看排班需要的权限
    }
  }
]

export default coachRoutes