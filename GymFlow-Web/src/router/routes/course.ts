import type { RouteRecordRaw } from 'vue-router'
import type { PermissionCode } from '@/types/permission'

const courseRoutes: RouteRecordRaw[] = [
  {
    path: '/course/list',
    name: 'CourseList',
    component: () => import('@/views/course/List.vue'),
    meta: {
      title: '课程管理',
      icon: 'Calendar',
      requiresAuth: true,
      showInMenu: true,
      permissions: ['course:view'] as PermissionCode[]  // 查看课程列表需要的权限
    }
  },
  {
    path: '/course/add',
    name: 'CourseCreate',
    component: () => import('@/views/course/Add.vue'),
    meta: {
      title: '新增课程',
      requiresAuth: true,
      showInMenu: false,
      parent: 'CourseList',
      permissions: ['course:add'] as PermissionCode[]  // 新增课程需要的权限
    }
  },
  {
    path: '/course/edit/:id',
    name: 'CourseEdit',
    component: () => import('@/views/course/Add.vue'),
    meta: {
      title: '编辑课程',
      requiresAuth: true,
      showInMenu: false,
      parent: 'CourseList',
      permissions: ['course:edit'] as PermissionCode[]  // 编辑课程需要的权限
    }
  },
  {
    path: '/course/detail/:id',
    name: 'CourseDetail',
    component: () => import('@/views/course/Detail.vue'),
    meta: {
      title: '课程详情',
      requiresAuth: true,
      showInMenu: false,
      parent: 'CourseList',
      permissions: ['course:detail'] as PermissionCode[]  // 查看详情需要的权限
    }
  },
  {
    path: '/course/schedule/:id',
    name: 'CourseSchedule',
    component: () => import('@/views/course/Schedule.vue'),
    meta: {
      title: '课程安排',
      requiresAuth: true,
      showInMenu: false,
      parent: 'CourseList',
      permissions: ['course:schedule:view'] as PermissionCode[]  // 查看排课需要的权限
    }
  }
]

export default courseRoutes