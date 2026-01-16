import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    base: './',
    plugins: [
      vue(),
      AutoImport({
        imports: [
          'vue',
          'vue-router',
          {
            'element-plus': [
              'ElMessage',
              'ElMessageBox',
              'ElNotification'
            ]
          }
        ],
        dts: 'src/auto-imports.d.ts',
        resolvers: [ElementPlusResolver()],
      }),
      Components({
        dts: 'src/components.d.ts',
        resolvers: [
          ElementPlusResolver({
            importStyle: 'sass',
          }),
        ],
      }),
    ],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src'),
      },
    },
    server: {
      host: env.VITE_DEV_SERVER_HOST || 'localhost',
      port: parseInt(env.VITE_DEV_SERVER_PORT || '5173'),
      open: true,
      proxy: env.VITE_PROXY_TARGET ? {
        [env.VITE_PROXY_CONTEXT || '/api']: {
          target: env.VITE_PROXY_TARGET,
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^${env.VITE_PROXY_CONTEXT || '/api'}`), ''),
        },
      } : undefined,
    },
    build: {
      outDir: env.VITE_BUILD_OUTDIR || 'dist',
      assetsDir: env.VITE_BUILD_ASSETS_DIR || 'assets',
      sourcemap: env.VITE_SOURCE_MAP === 'true',
      rollupOptions: {
        output: {
          chunkFileNames: 'assets/js/[name]-[hash].js',
          entryFileNames: 'assets/js/[name]-[hash].js',
          assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
        },
      },
    },
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `@use "@/assets/styles/variables.scss" as *;`,
        },
      },
    },
    define: {
      __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: false,
    },
  }
})