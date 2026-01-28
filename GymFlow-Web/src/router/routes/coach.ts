import type { RouteRecordRaw } from 'vue-router'

const coachRoutes: RouteRecordRaw[] = [
  {
    path: '/coaches',
    name: 'CoachList',
    component: () => import('@/views/coach/List.vue'),
    meta: {
      title: '教练管理',
      icon: 'UserFilled',
      requiresAuth: true,
      showInMenu:true,
      roles: ['ADMIN']
    }
  },
  {
    path: '/coaches/create',
    name: 'CoachCreate',
    component: () => import('@/views/coach/Form.vue'),
    meta: {
      title: '新增教练',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'CoachList'
    }
  },
  {
    path: '/coaches/:id/edit',
    name: 'CoachEdit',
    component: () => import('@/views/coach/Form.vue'),
    meta: {
      title: '编辑教练',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'CoachList'
    }
  },
  {
    path: '/coaches/:id',
    name: 'CoachDetail',
    component: () => import('@/views/coach/Detail.vue'),
    meta: {
      title: '教练详情',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'CoachList'
    }
  }
]
export default coachRoutes