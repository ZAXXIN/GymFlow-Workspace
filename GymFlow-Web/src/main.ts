import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

// 导入权限指令
import { setupPermissionDirective } from '@/directives/permission'

// 导入全局样式
import '@/assets/styles/index.scss'

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

app.mount('#app')