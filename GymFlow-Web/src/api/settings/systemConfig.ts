import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'

// 基本设置DTO
export interface BasicConfigDTO {
  systemName: string
  systemLogo?: string
}

// 业务设置DTO
export interface BusinessConfigDTO {
  businessStartTime: string
  businessEndTime: string
  courseRenewalDays: number
  courseCancelHours: number
  minClassSize: number
  maxClassCapacity: number
}

// 系统配置响应DTO
export interface SystemConfigResponseDTO {
  basic: BasicConfigDTO
  business: BusinessConfigDTO
  updateTime: string
}

// 系统配置更新请求
export interface SystemConfigUpdateRequest {
  basic: BasicConfigDTO
  business: BusinessConfigDTO
}

// 系统配置API
export const systemConfigApi = {
  /**
   * 获取系统配置
   */
  getConfig(): Promise<ApiResponse<SystemConfigResponseDTO>> {
    return request({
      url: '/settings/systemConfig',
      method: 'GET'
    })
  },

  /**
   * 更新系统配置
   */
  updateConfig(data: SystemConfigUpdateRequest): Promise<ApiResponse> {
    return request({
      url: '/settings/systemConfig',
      method: 'PUT',
      data
    })
  },

  /**
   * 重置系统配置
   */
  resetConfig(): Promise<ApiResponse> {
    return request({
      url: '/settings/systemConfig/reset',
      method: 'POST'
    })
  },

  /**
   * 上传Logo
   */
  uploadLogo(file: File): Promise<ApiResponse<{ url: string }>> {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/upload/logo',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}