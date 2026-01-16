/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

interface ImportMetaEnv {
  // 应用基础配置
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_VERSION: string
  
  // API 配置
  readonly VITE_API_BASE_URL: string
  readonly VITE_API_TIMEOUT: number
  readonly VITE_API_PREFIX: string
  
  // 上传配置
  readonly VITE_UPLOAD_URL: string
  readonly VITE_UPLOAD_MAX_SIZE: string
  
  // 认证配置
  readonly VITE_TOKEN_KEY: string
  readonly VITE_TOKEN_EXPIRES: string
  readonly VITE_REFRESH_TOKEN_KEY: string
  
  // 分页配置
  readonly VITE_DEFAULT_PAGE_SIZE: string
  readonly VITE_PAGE_SIZES: string
  
  // 功能开关
  readonly VITE_ENABLE_WEBSOCKET: string
  readonly VITE_ENABLE_PRINT: string
  readonly VITE_ENABLE_CHART: string
  
  // 开发配置
  readonly VITE_DEV_SERVER_HOST: string
  readonly VITE_DEV_SERVER_PORT: string
  readonly VITE_DEV_SERVER_HTTPS: string
  
  // 调试配置
  readonly VITE_DEBUG: string
  readonly VITE_SOURCE_MAP: string
  readonly VITE_DROP_CONSOLE: string
  
  // 代理配置
  readonly VITE_PROXY_TARGET: string
  readonly VITE_PROXY_CONTEXT: string
  
  // 模拟数据
  readonly VITE_ENABLE_MOCK: string
  readonly VITE_MOCK_TIMEOUT: string
  
  // 构建配置
  readonly VITE_BUILD_OUTDIR: string
  readonly VITE_BUILD_ASSETS_DIR: string
  readonly VITE_BUILD_ASSETS_INLINE_LIMIT: number
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

// 扩展 window 对象
declare global {
  interface Window {
    __VUE_DEVTOOLS_GLOBAL_HOOK__: any
  }
}