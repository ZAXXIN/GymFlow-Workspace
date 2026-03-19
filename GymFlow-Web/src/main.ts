import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { useAuthStore } from './stores/auth'

// 导入权限指令
import { setupPermissionDirective } from '@/directives/permission'

// 导入全局样式
import '@/assets/styles/index.scss'
import '@/assets/styles/element-override.scss' 

const app = createApp(App)

// 注册Element Plus
app.use(ElementPlus)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 创建 Pinia 实例
const pinia = createPinia()
app.use(pinia)

// 注册路由
app.use(router)

// 注册权限指令
setupPermissionDirective(app)

// 在应用挂载前初始化认证状态
const initApp = async () => {
  const authStore = useAuthStore()
  await authStore.initAuth()
  app.mount('#app')
}

initApp()