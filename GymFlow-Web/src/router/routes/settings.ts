import { RouteRecordRaw } from 'vue-router'

export const settingsRoutes: RouteRecordRaw[] = [
  {
    path: '/settings',
    name: 'Settings',
    redirect: '/settings/system',
    meta: {
      title: '系统设置',
      icon: 'i-ep-setting',
      requiresAuth: true,
      roles: ['ADMIN']
    },
    children: [
      {
        path: 'system',
        name: 'SystemSettings',
        component: () => import('@/views/settings/System.vue'),
        meta: {
          title: '系统配置',
          requiresAuth: true,
          roles: ['ADMIN']
        }
      },
      {
        path: 'profile',
        name: 'UserProfile',
        component: () => import('@/views/settings/UserProfile.vue'),
        meta: {
          title: '个人资料',
          requiresAuth: true,
          roles: ['ADMIN', 'COACH']
        }
      },
      {
        path: 'security',
        name: 'SecuritySettings',
        component: () => import('@/views/settings/Security.vue'),
        meta: {
          title: '安全设置',
          requiresAuth: true,
          roles: ['ADMIN', 'COACH']
        }
      }
    ]
  }
]