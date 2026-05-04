import type { RouteRecordRaw } from 'vue-router'
import type { PermissionCode } from '@/types/permission'

const settingsRoutes: RouteRecordRaw[] = [
  {
    path: '/settings/webUser',
    name: 'webUserList',
    component: () => import('@/views/settings/webUser/List.vue'),
    meta: {
      title: '用户管理',
      requiresAuth: true,
      showInMenu: true,
      permissions: ['settings:user'] as PermissionCode[]  // 查看用户列表需要的权限
    }
  },
  {
    path: '/settings/webUser/add',
    name: 'addWebUser',
    component: () => import('@/views/settings/webUser/Add.vue'),
    meta: {
      title: '新增用户',
      requiresAuth: true,
      showInMenu: false,
      parent: 'webUserList',
      permissions: ['settings:user'] as PermissionCode[]  // 新增用户需要的权限
    }
  },
  {
    path: '/settings/webUser/edit/:id',
    name: 'editWebUser',
    component: () => import('@/views/settings/webUser/Add.vue'),
    meta: {
      title: '编辑用户',
      requiresAuth: true,
      showInMenu: false,
      parent: 'webUserList',
      permissions: ['settings:user'] as PermissionCode[]  // 编辑用户需要的权限
    }
  },
  {
    path: '/settings/role',
    name: 'rolePermission',
    component: () => import('@/views/settings/RolePermission.vue'),
    meta: {
      title: '角色权限',
      requiresAuth: true,
      showInMenu: true,
      permissions: ['settings:role']
    }
  },
  {
    path: '/settings/systemConfig',
    name: 'systemConfig',
    component: () => import('@/views/settings/SystemConfig.vue'),
    meta: {
      title: '系统配置',
      requiresAuth: true,
      showInMenu: true,
      permissions: ['settings:config'] as PermissionCode[]  // 查看系统配置需要的权限
    }
  }
]

export default settingsRoutes