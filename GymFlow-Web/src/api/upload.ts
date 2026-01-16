import { http } from '@/utils/request'
import type { ApiResponse } from '@/types'

export const uploadApi = {
  /**
   * 上传文件
   */
  uploadFile(file: File): Promise<{ url: string; name: string; size: number }> {
    const formData = new FormData()
    formData.append('file', file)
    
    return http.post('/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 批量上传文件
   */
  uploadFiles(files: File[]): Promise<{ url: string; name: string; size: number }[]> {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    
    return http.post('/upload/batch', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 删除文件
   */
  deleteFile(url: string): Promise<void> {
    return http.delete('/upload', { data: { url } })
  }
}