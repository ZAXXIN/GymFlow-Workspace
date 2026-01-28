import type { RouteRecordRaw } from 'vue-router'

const courseRoutes: RouteRecordRaw[] = [
  {
    path: '/courses',
    name: 'CourseList',
    component: () => import('@/views/course/List.vue'),
    meta: {
      title: '课程管理',
      icon: 'Calendar',
      requiresAuth: true,
      showInMenu:true,
      roles: ['ADMIN', 'COACH']
    }
  },
  {
    path: '/courses/create',
    name: 'CourseCreate',
    component: () => import('@/views/course/Form.vue'),
    meta: {
      title: '新增课程',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'CourseList'
    }
  },
  {
    path: '/courses/:id/edit',
    name: 'CourseEdit',
    component: () => import('@/views/course/Form.vue'),
    meta: {
      title: '编辑课程',
      requiresAuth: true,
      roles: ['ADMIN'],
      hideInMenu: true,
      parent: 'CourseList'
    }
  },
  {
    path: '/courses/:id',
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
    path: '/courses/schedule',
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