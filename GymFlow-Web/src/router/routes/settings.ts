import type { RouteRecordRaw } from 'vue-router'

const settingRoutes: RouteRecordRaw[] = [
  {
    path: '/settings/webUser',
    name: 'webUserList',
    component: () => import('@/views/settings/webUser/List.vue'),
    meta: {
      title: '用户管理',
      // icon: 'UserFilled',
      requiresAuth: true,
      showInMenu:true,
    }
  },
  {
    path: '/settings/webUser/detail/:id',
    name: 'webUserDetail',
    component: () => import('@/views/settings/webUser/Detail.vue'),
    meta: {
      title: '用户详情',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'webUserList'
    }
  },
  {
    path: '/settings/webUser/add',
    name: 'editWebUser',
    component: () => import('@/views/settings/webUser/Add.vue'),
    meta: {
      title: '新增用户',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'webUserList'
    }
  },
  {
    path: '/settings/webUser/edit/:id',
    name: 'editWebUser',
    component: () => import('@/views/settings/webUser/Add.vue'),
    meta: {
      title: '编辑用户',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'webUserList'
    }
  },
  {
    path: '/settings/systemConfig',
    name: 'systemConfig',
    component: () => import('@/views/settings/SystemConfig.vue'),
    meta: {
      title: '系统配置',
      requiresAuth: true,
      hideInMenu: true,
    }
  },
  // {
  //   path: '/settings/role',
  //   name: 'roleAuth',
  //   component: () => import('@/views/settings/role.vue'),
  //   meta: {
  //     title: '角色权限',
  //     requiresAuth: true,
  //     hideInMenu: true,
  //     // parent: 'CoachList'
  //   }
  // },
  // {
  //   path: '/coach/detail/:id',
  //   name: 'CoachDetail',
  //   component: () => import('@/views/coach/Detail.vue'),
  //   meta: {
  //     title: '教练详情',
  //     requiresAuth: true,
  //     hideInMenu: true,
  //     parent: 'CoachList'
  //   }
  // },
  // {
  //   path: '/coach/schedule/:id',
  //   name: 'CoachDetail',
  //   component: () => import('@/views/coach/Schedule.vue'),
  //   meta: {
  //     title: '教练排课',
  //     requiresAuth: true,
  //     hideInMenu: true,
  //     parent: 'CoachList'
  //   }
  // }
]
export default settingRoutes