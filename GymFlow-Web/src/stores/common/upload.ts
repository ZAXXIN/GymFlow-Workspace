import { defineStore } from 'pinia'
import { ref } from 'vue'
import { uploadApi, type UploadResult } from '@/api/common/upload'
import { ElMessage } from 'element-plus'

export const useUploadStore = defineStore('upload', () => {
  // 上传状态
  const uploading = ref(false)
  const uploadProgress = ref(0)
  const uploadError = ref<string | null>(null)

  /**
   * 上传图片（通用方法）
   * @param file 文件对象
   * @param options 配置选项
   */
  const uploadImage = async (
    file: File, 
    options?: {
      onSuccess?: (url: string) => void
      onError?: (error: Error) => void
      showMessage?: boolean
    }
  ): Promise<string | null> => {
    uploading.value = true
    uploadProgress.value = 0
    uploadError.value = null
    
    try {
      // 可以在这里添加通用验证
      validateImage(file)
      
      const response = await uploadApi.uploadImage(file)
      
      if (response.code === 200 && response.data?.url) {
        const url = response.data.url
        
        // 成功回调
        if (options?.onSuccess) {
          options.onSuccess(url)
        }
        
        // 显示成功消息
        if (options?.showMessage !== false) {
          ElMessage.success('上传成功')
        }
        
        uploadProgress.value = 100
        return url
      } else {
        throw new Error(response.message || '上传失败')
      }
    } catch (error: any) {
      uploadError.value = error.message
      
      // 错误回调
      if (options?.onError) {
        options.onError(error)
      }
      
      // 显示错误消息
      if (options?.showMessage !== false) {
        ElMessage.error(error.message || '上传失败')
      }
      
      return null
    } finally {
      uploading.value = false
    }
  }

  /**
   * 上传文件（通用方法）
   */
  const uploadFile = async (
    file: File,
    options?: {
      onSuccess?: (result: UploadResult) => void
      onError?: (error: Error) => void
      showMessage?: boolean
    }
  ): Promise<UploadResult | null> => {
    uploading.value = true
    uploadError.value = null
    
    try {
      const response = await uploadApi.uploadFile(file)
      
      if (response.code === 200 && response.data) {
        if (options?.onSuccess) {
          options.onSuccess(response.data)
        }
        
        if (options?.showMessage !== false) {
          ElMessage.success('上传成功')
        }
        
        return response.data
      } else {
        throw new Error(response.message || '上传失败')
      }
    } catch (error: any) {
      uploadError.value = error.message
      
      if (options?.onError) {
        options.onError(error)
      }
      
      if (options?.showMessage !== false) {
        ElMessage.error(error.message || '上传失败')
      }
      
      return null
    } finally {
      uploading.value = false
    }
  }

  /**
   * 删除文件
   */
  const deleteFile = async (url: string, showMessage = true): Promise<boolean> => {
    try {
      const response = await uploadApi.deleteFile(url)
      
      if (response.code === 200) {
        if (showMessage) {
          ElMessage.success('删除成功')
        }
        return true
      }
      return false
    } catch (error: any) {
      if (showMessage) {
        ElMessage.error(error.message || '删除失败')
      }
      return false
    }
  }

  /**
   * 验证图片
   */
  const validateImage = (file: File) => {
    const isImage = file.type.startsWith('image/')
    const isLt5M = file.size / 1024 / 1024 < 5
    
    if (!isImage) {
      throw new Error('只能上传图片文件')
    }
    
    if (!isLt5M) {
      throw new Error('图片大小不能超过5MB')
    }
    
    return true
  }

  return {
    // 状态
    uploading,
    uploadProgress,
    uploadError,
    
    // 方法
    uploadImage,
    uploadFile,
    deleteFile,
    validateImage
  }
})