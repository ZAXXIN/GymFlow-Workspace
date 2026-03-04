import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'

export interface UploadResult {
  url: string
  filename: string
  size?: string
}

export const uploadApi = {
  /**
   * 上传图片
   */
  uploadImage(file: File): Promise<ApiResponse<UploadResult>> {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/common/upload/image',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 上传通用文件
   */
  uploadFile(file: File): Promise<ApiResponse<UploadResult>> {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/common/upload/file',
      method: 'POST',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 删除文件
   */
  deleteFile(url: string): Promise<ApiResponse> {
    return request({
      url: '/common/upload',
      method: 'DELETE',
      params: { url }
    })
  }
}