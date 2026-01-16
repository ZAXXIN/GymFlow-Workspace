import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig
} from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'

// 创建axios实例
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: import.meta.env.VITE_TIMEOUT ? Number(import.meta.env.VITE_TIMEOUT) : 10000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    
    // 添加token
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    
    // 添加时间戳防止缓存
    if (config.method?.toLowerCase() === 'get') {
      config.params = {
        ...config.params,
        _t: Date.now()
      }
    }
    
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const { data } = response
    
    // 如果响应数据是二进制流（如下载文件），直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 处理业务错误
    if (data.code !== 200) {
      const errorMessage = data.message || '请求失败'
      
      // 401未授权
      if (data.code === 401) {
        const authStore = useAuthStore()
        authStore.logout()
        
        ElMessageBox.alert('登录已过期，请重新登录', '提示', {
          confirmButtonText: '重新登录',
          callback: () => {
            router.push('/login')
          }
        })
      }
      // 403禁止访问
      else if (data.code === 403) {
        ElMessage.error('没有权限访问该资源')
      }
      // 其他错误
      else {
        ElMessage.error(errorMessage)
      }
      
      return Promise.reject(new Error(errorMessage))
    }
    
    return data.data
  },
  (error) => {
    console.error('Response error:', error)
    
    let message = '请求失败'
    
    if (error.response) {
      // 请求已发出，服务器返回错误状态码
      switch (error.response.status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请登录'
          const authStore = useAuthStore()
          authStore.logout()
          router.push('/login')
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 408:
          message = '请求超时'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 501:
          message = '服务未实现'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        case 505:
          message = 'HTTP版本不受支持'
          break
        default:
          message = `连接错误 ${error.response.status}`
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      if (error.code === 'ECONNABORTED') {
        message = '请求超时'
      } else {
        message = '网络连接异常，请检查网络设置'
      }
    } else {
      // 请求配置出错
      message = error.message || '请求配置错误'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 导出常用的请求方法
export const http = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return request.get(url, config)
  },
  
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return request.post(url, data, config)
  },
  
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return request.put(url, data, config)
  },
  
  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return request.patch(url, data, config)
  },
  
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return request.delete(url, config)
  },
  
  upload<T = any>(url: string, file: File, config?: AxiosRequestConfig): Promise<T> {
    const formData = new FormData()
    formData.append('file', file)
    
    return request.post(url, formData, {
      ...config,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  download(url: string, filename?: string): Promise<void> {
    return request.get(url, {
      responseType: 'blob'
    }).then(response => {
      const blob = new Blob([response.data])
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = filename || 'download'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
    })
  }
}

export default request