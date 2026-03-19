// 请求封装
import { ApiResponse } from '../services/types/common.types'

// 请求配置
const BASE_URL = 'http://localhost:8080/api' // 开发环境
// const BASE_URL = 'http://10.60.161.105:8080/api'

// 请求方法类型
type RequestMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

// 请求参数
interface RequestOptions {
  url: string
  method?: RequestMethod
  data?: any
  header?: Record<string, string>
  showLoading?: boolean
  loadingText?: string
  showError?: boolean
}

// 创建请求实例
class Request {
  private token: string = ''

  constructor() {
    // 初始化时从存储获取token
    this.token = wx.getStorageSync('token') || ''
  }

  /**
   * 设置token
   */
  setToken(token: string) {
    this.token = token
    wx.setStorageSync('token', token)
  }

  /**
   * 清除token
   */
  clearToken() {
    this.token = ''
    wx.removeStorageSync('token')
  }

  /**
   * 获取请求头
   */
  private getHeader(customHeader?: Record<string, string>): Record<string, string> {
    const header: Record<string, string> = {
      'Content-Type': 'application/json',
      ...customHeader
    }

    if (this.token) {
      header['Authorization'] = `Bearer ${this.token}`
    }

    return header
  }

  /**
   * 显示加载中
   */
  private showLoading(text: string = '加载中...') {
    wx.showLoading({
      title: text,
      mask: true
    })
  }

  /**
   * 隐藏加载中
   */
  private hideLoading() {
    wx.hideLoading()
  }

  /**
   * 显示错误提示
   */
  private showError(message: string) {
    wx.showToast({
      title: message,
      icon: 'none',
      duration: 2000
    })
  }

  /**
   * 处理响应
   */
  private handleResponse<T>(res: WechatMiniprogram.RequestSuccessCallbackResult): Promise<T> {
    const { statusCode, data } = res
    
    // HTTP状态码错误
    if (statusCode !== 200) {
      return Promise.reject(new Error(`网络错误: ${statusCode}`))
    }

    const response = data as ApiResponse<T>

    // 业务状态码错误
    if (response.code !== 200) {
      return Promise.reject(new Error(response.message || '请求失败'))
    }

    return Promise.resolve(response.data)
  }

  /**
   * 发起请求
   */
  async request<T = any>(options: RequestOptions): Promise<T> {
    const {
      url,
      method = 'GET',
      data,
      header,
      showLoading = false,
      loadingText = '加载中...',
      showError = true
    } = options

    try {
      // 显示加载中
      if (showLoading) {
        this.showLoading(loadingText)
      }

      // 发起请求
      const res = await new Promise<WechatMiniprogram.RequestSuccessCallbackResult>((resolve, reject) => {
        wx.request({
          url: `${BASE_URL}${url}`,
          method,
          data,
          header: this.getHeader(header),
          success: resolve,
          fail: reject
        })
      })

      // 处理响应
      const result = await this.handleResponse<T>(res)
      
      return result
    } catch (error: any) {
      // 显示错误提示
      if (showError) {
        this.showError(error.message || '请求失败')
      }
      
      throw error
    } finally {
      // 隐藏加载中
      if (showLoading) {
        this.hideLoading()
      }
    }
  }

  /**
   * GET请求
   */
  get<T = any>(url: string, data?: any, options?: Partial<RequestOptions>) {
    return this.request<T>({
      url,
      method: 'GET',
      data,
      ...options
    })
  }

  /**
   * POST请求
   */
  post<T = any>(url: string, data?: any, options?: Partial<RequestOptions>) {
    return this.request<T>({
      url,
      method: 'POST',
      data,
      ...options
    })
  }

  /**
   * PUT请求
   */
  put<T = any>(url: string, data?: any, options?: Partial<RequestOptions>) {
    return this.request<T>({
      url,
      method: 'PUT',
      data,
      ...options
    })
  }

  /**
   * DELETE请求
   */
  delete<T = any>(url: string, data?: any, options?: Partial<RequestOptions>) {
    return this.request<T>({
      url,
      method: 'DELETE',
      data,
      ...options
    })
  }
}

// 导出单例
export const wxRequest = new Request()