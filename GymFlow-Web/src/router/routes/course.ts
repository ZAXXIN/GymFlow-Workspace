import type { RouteRecordRaw } from 'vue-router'

const courseRoutes: RouteRecordRaw[] = [
  {
    path: '/course/list',
    name: 'CourseList',
    component: () => import('@/views/course/List.vue'),
    meta: {
      title: '课程管理',
      icon: 'Calendar',
      requiresAuth: true,
      showInMenu:true,
    }
  },
  {
    path: '/course/add',
    name: 'CourseCreate',
    component: () => import('@/views/course/Add.vue'),
    meta: {
      title: '新增课程',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'CourseList'
    }
  },
  {
    path: '/course/edit/:id',
    name: 'CourseEdit',
    component: () => import('@/views/course/Add.vue'),
    meta: {
      title: '编辑课程',
      requiresAuth: true,
      hideInMenu: true,
      parent: 'CourseList'
    }
  },
  {
    path: '/course/detail/:id',
    name: 'CourseDetail',
    component: () => import('@/views/course/Detail.vue'),
    meta: {
      title: '课程详情',
      requiresAuth: true,
      roles: ['ADMIN', 'COACH'],
      hideInMenu: true,
      parent: 'CourseList'
    }
  },
  {
    path: '/course/schedule',
    name: 'CourseSchedule',
    component: () => import('@/views/course/Schedule.vue'),
    meta: {
      title: '课程安排',
      icon: 'i-ep-date',
      requiresAuth: true,
      roles: ['ADMIN', 'COACH']
    }
  }
]
export default courseRoutes