// import type { RouteRecordRaw } from 'vue-router'

// const settingsRoutes: RouteRecordRaw[] = [
//   {
//     path: '/setting',
//     name: 'Setting',
//     redirect: '/setting/system',
//     meta: {
//       title: '系统设置',
//       icon: 'Setting',
//       requiresAuth: true,
//       showInMenu:true,
//     },
//     children: [
//       {
//         path: 'webUser',
//         name: 'SystemSetting',
//         component: () => import('@/views/setting/webUser/List.vue'),
//         meta: {
//           title: '系统配置',
//           requiresAuth: true,
//           roles: ['ADMIN']
//         }
//       },
//       {
//         path: 'security',
//         name: 'SecuritySettings',
//         component: () => import('@/views/settings/Security.vue'),
//         meta: {
//           title: '角色权限',
//           requiresAuth: true,
//           roles: ['ADMIN', 'COACH']
//         }
//       },
//       {
//         path: 'system',
//         name: 'SystemSetting',
//         component: () => import('@/views/settings/System.vue'),
//         meta: {
//           title: '系统配置',
//           requiresAuth: true,
//           roles: ['ADMIN']
//         }
//       },
//     ]
//   }
// ]
// export default settingsRoutes