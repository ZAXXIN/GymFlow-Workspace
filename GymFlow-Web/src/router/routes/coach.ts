import type { RouteRecordRaw } from 'vue-router'

const coachRoutes: RouteRecordRaw[] = [
  {
    path: '/coach/list',
    name: 'CoachList',
    component: () => import('@/views/coach/List.vue'),
    meta: {
      title: '教练管理',
      icon: 'UserFilled',
      requiresAuth: true,
      showInMenu:true,
    }
  },
  {
    path: '/coach/add',
    name: 'CoachCreate',
    component: () => import('@/views/coach/Add.vue'),
    meta: {
      title: '新增教练',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'CoachList'
    }
  },
  {
    path: '/coach/edit/:id',
    name: 'CoachEdit',
    component: () => import('@/views/coach/Add.vue'),
    meta: {
      title: '编辑教练',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'CoachList'
    }
  },
  {
    path: '/coach/detail/:id',
    name: 'CoachDetail',
    component: () => import('@/views/coach/Detail.vue'),
    meta: {
      title: '教练详情',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'CoachList'
    }
  },
  {
    path: '/coach/schedule/:id',
    name: 'CoachDetail',
    component: () => import('@/views/coach/Schedule.vue'),
    meta: {
      title: '教练排课',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'CoachList'
    }
  }
]
export default coachRoutes