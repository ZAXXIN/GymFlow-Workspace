// utils/request.ts
// 请求封装

import { ApiResponse } from '../services/types/common.types'

// 请求配置
const BASE_URL = 'http://localhost:8080/api'

type RequestMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

// 请求参数 - 添加 params 字段
interface RequestOptions {
  url: string
  method?: RequestMethod
  data?: any
  params?: any  // 添加 Query 参数
  header?: Record<string, string>
  showLoading?: boolean
  loadingText?: string
  showError?: boolean
}

class Request {
  private token: string = ''

  constructor() {
    this.token = wx.getStorageSync('token') || ''
  }

  setToken(token: string) {
    this.token = token
    wx.setStorageSync('token', token)
  }

  clearToken() {
    this.token = ''
    wx.removeStorageSync('token')
  }

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

  private showLoading(text: string = '加载中...') {
    wx.showLoading({ title: text, mask: true })
  }

  private hideLoading() {
    wx.hideLoading()
  }

  private showError(message: string) {
    wx.showToast({ title: message, icon: 'none', duration: 2000 })
  }

  private handleResponse<T>(res: WechatMiniprogram.RequestSuccessCallbackResult): Promise<T> {
    const { statusCode, data } = res
    if (statusCode !== 200) {
      return Promise.reject(new Error(`网络错误: ${statusCode}`))
    }
    const response = data as ApiResponse<T>
    if (response.code !== 200) {
      return Promise.reject(new Error(response.message || '请求失败'))
    }
    return Promise.resolve(response.data)
  }

  /**
   * 构建完整 URL（处理 Query 参数）
   */
  private buildUrl(url: string, params?: any): string {
    let fullUrl = `${BASE_URL}${url}`
    if (params) {
      const queryString = Object.keys(params)
        .filter(key => params[key] !== undefined && params[key] !== null)
        .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
        .join('&')
      if (queryString) {
        fullUrl += (fullUrl.includes('?') ? '&' : '?') + queryString
      }
    }
    return fullUrl
  }

  /**
   * 发起请求
   */
  async request<T = any>(options: RequestOptions): Promise<T> {
    const {
      url,
      method = 'GET',
      data,
      params,
      header,
      showLoading = false,
      loadingText = '加载中...',
      showError = true
    } = options

    try {
      if (showLoading) {
        this.showLoading(loadingText)
      }

      const fullUrl = this.buildUrl(url, params)

      const res = await new Promise<WechatMiniprogram.RequestSuccessCallbackResult>((resolve, reject) => {
        wx.request({
          url: fullUrl,
          method,
          data,
          header: this.getHeader(header),
          success: resolve,
          fail: reject
        })
      })

      const result = await this.handleResponse<T>(res)
      return result
    } catch (error: any) {
      if (showError) {
        this.showError(error.message || '请求失败')
      }
      throw error
    } finally {
      if (showLoading) {
        this.hideLoading()
      }
    }
  }

  get<T = any>(url: string, data?: any, options?: Partial<RequestOptions>) {
    return this.request<T>({
      url,
      method: 'GET',
      data,
      ...options
    })
  }

  post<T = any>(url: string, data?: any, options?: Partial<RequestOptions>) {
    return this.request<T>({
      url,
      method: 'POST',
      data,
      ...options
    })
  }

  put<T = any>(url: string, data?: any, options?: Partial<RequestOptions>) {
    return this.request<T>({
      url,
      method: 'PUT',
      data,
      ...options
    })
  }

  delete<T = any>(url: string, data?: any, options?: Partial<RequestOptions>) {
    return this.request<T>({
      url,
      method: 'DELETE',
      data,
      ...options
    })
  }
}

export const wxRequest = new Request()