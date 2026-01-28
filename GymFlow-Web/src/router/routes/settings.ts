import type { RouteRecordRaw } from 'vue-router'

const settingsRoutes: RouteRecordRaw[] = [
  {
    path: '/settings',
    name: 'Settings',
    redirect: '/settings/system',
    meta: {
      title: '系统设置',
      icon: 'Setting',
      requiresAuth: true,
      showInMenu:true,
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
export default settingsRoutes