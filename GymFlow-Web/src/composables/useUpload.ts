import { ref, reactive } from 'vue'
import axios from 'axios'
import { v4 as uuidv4 } from 'uuid'

interface UploadOptions {
  // 上传配置
  action?: string
  headers?: Record<string, string>
  multiple?: boolean
  accept?: string
  limit?: number
  maxSize?: number // MB
  autoUpload?: boolean
  showFileList?: boolean
  drag?: boolean
  
  // 文件类型验证
  allowedTypes?: string[]
  
  // 事件回调
  onStart?: (file: UploadFile) => void
  onProgress?: (file: UploadFile, progress: number) => void
  onSuccess?: (file: UploadFile, response: any) => void
  onError?: (file: UploadFile, error: any) => void
  onRemove?: (file: UploadFile) => void
  onExceed?: (files: File[], fileList: UploadFile[]) => void
  onBeforeUpload?: (file: UploadRawFile) => boolean | Promise<boolean>
  onBeforeRemove?: (file: UploadFile) => boolean | Promise<boolean>
}

interface FileItem {
  id: string
  name: string
  size: number
  type: string
  url?: string
  status: 'pending' | 'uploading' | 'success' | 'error'
  progress: number
  response?: any
  error?: string
  file: File
}

/**
 * 上传功能组合式函数
 * @param options 上传配置选项
 * @returns 上传相关的状态和方法
 */
export function useUpload(options: UploadOptions = {}) {
  // 上传状态
  const uploading = ref(false)
  const fileList = ref<FileItem[]>([])
  const totalProgress = ref(0)
  
  // 上传配置
  const uploadConfig = reactive({
    action: options.action || '/api/upload',
    headers: options.headers || {},
    multiple: options.multiple ?? true,
    accept: options.accept || '*',
    limit: options.limit || 10,
    maxSize: options.maxSize || 10, // MB
    autoUpload: options.autoUpload ?? true,
    showFileList: options.showFileList ?? true,
    drag: options.drag ?? false,
    allowedTypes: options.allowedTypes || []
  })
  
  // 检查文件类型
  const checkFileType = (file: File): boolean => {
    if (uploadConfig.allowedTypes.length === 0) return true
    
    const fileType = file.type.toLowerCase()
    const extension = file.name.split('.').pop()?.toLowerCase() || ''
    
    return uploadConfig.allowedTypes.some(type => {
      const typeLower = type.toLowerCase()
      return fileType.includes(typeLower) || extension === typeLower
    })
  }
  
  // 检查文件大小
  const checkFileSize = (file: File): boolean => {
    const maxSize = uploadConfig.maxSize * 1024 * 1024 // 转换为字节
    return file.size <= maxSize
  }
  
  // 添加文件
  const addFiles = (files: File[]) => {
    // 检查文件数量限制
    const remainingSlots = uploadConfig.limit - fileList.value.length
    if (files.length > remainingSlots) {
      const exceededFiles = files.slice(remainingSlots)
      options.onExceed?.(exceededFiles, fileList.value as any as UploadFile[])
      
      if (remainingSlots <= 0) {
        ElMessage.warning(`最多只能上传 ${uploadConfig.limit} 个文件`)
        return []
      }
      
      files = files.slice(0, remainingSlots)
    }
    
    const newFileItems: FileItem[] = []
    
    files.forEach(file => {
      // 检查文件类型
      if (!checkFileType(file)) {
        ElMessage.error(`文件 ${file.name} 类型不支持`)
        return
      }
      
      // 检查文件大小
      if (!checkFileSize(file)) {
        ElMessage.error(`文件 ${file.name} 大小超过 ${uploadConfig.maxSize}MB`)
        return
      }
      
      const fileItem: FileItem = {
        id: uuidv4(),
        name: file.name,
        size: file.size,
        type: file.type,
        status: 'pending',
        progress: 0,
        file: file
      }
      
      fileList.value.push(fileItem)
      newFileItems.push(fileItem)
      
      // 回调函数
      options.onStart?.(fileItem as any as UploadFile)
    })
    
    // 如果启用自动上传，开始上传
    if (uploadConfig.autoUpload && newFileItems.length > 0) {
      uploadFiles(newFileItems)
    }
    
    return newFileItems
  }
  
  // 上传文件
  const uploadFiles = async (filesToUpload: FileItem[] = fileList.value) => {
    uploading.value = true
    
    const uploadPromises = filesToUpload
      .filter(file => file.status === 'pending' || file.status === 'error')
      .map(file => uploadFile(file))
    
    if (uploadPromises.length === 0) {
      uploading.value = false
      return []
    }
    
    try {
      const results = await Promise.allSettled(uploadPromises)
      
      // 更新总进度
      const successCount = results.filter(r => r.status === 'fulfilled').length
      totalProgress.value = Math.round((successCount / results.length) * 100)
      
      return results
    } finally {
      uploading.value = false
    }
  }
  
  // 上传单个文件
  const uploadFile = async (fileItem: FileItem): Promise<void> => {
    return new Promise((resolve, reject) => {
      fileItem.status = 'uploading'
      fileItem.progress = 0
      
      const formData = new FormData()
      formData.append('file', fileItem.file)
      
      // 可以添加其他参数
      formData.append('filename', fileItem.name)
      formData.append('timestamp', Date.now().toString())
      
      axios.post(uploadConfig.action, formData, {
        headers: {
          ...uploadConfig.headers,
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: (progressEvent) => {
          if (progressEvent.total) {
            const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
            fileItem.progress = percent
            
            // 回调函数
            options.onProgress?.(fileItem as any as UploadFile, percent)
          }
        }
      })
      .then(response => {
        fileItem.status = 'success'
        fileItem.progress = 100
        fileItem.response = response.data
        
        // 如果响应中有文件URL，保存它
        if (response.data?.data?.url) {
          fileItem.url = response.data.data.url
        }
        
        // 回调函数
        options.onSuccess?.(fileItem as any as UploadFile, response.data)
        
        ElNotification({
          title: '上传成功',
          message: `文件 ${fileItem.name} 上传成功`,
          type: 'success',
          duration: 2000
        })
        
        resolve()
      })
      .catch(error => {
        fileItem.status = 'error'
        fileItem.progress = 0
        fileItem.error = error.message || '上传失败'
        
        // 回调函数
        options.onError?.(fileItem as any as UploadFile, error)
        
        ElNotification({
          title: '上传失败',
          message: `文件 ${fileItem.name} 上传失败: ${error.message}`,
          type: 'error',
          duration: 3000
        })
        
        reject(error)
      })
    })
  }
  
  // 移除文件
  const removeFile = async (fileItem: FileItem) => {
    if (options.onBeforeRemove) {
      const shouldRemove = await options.onBeforeRemove(fileItem as any as UploadFile)
      if (!shouldRemove) return
    }
    
    const index = fileList.value.findIndex(file => file.id === fileItem.id)
    if (index !== -1) {
      fileList.value.splice(index, 1)
      
      // 回调函数
      options.onRemove?.(fileItem as any as UploadFile)
    }
  }
  
  // 清空文件列表
  const clearFiles = () => {
    fileList.value = []
  }
  
  // 获取已上传的文件URL列表
  const getUploadedUrls = (): string[] => {
    return fileList.value
      .filter(file => file.status === 'success' && file.url)
      .map(file => file.url!)
  }
  
  // 获取已上传的文件信息
  const getUploadedFiles = (): FileItem[] => {
    return fileList.value.filter(file => file.status === 'success')
  }
  
  // 检查是否有上传失败的文件
  const hasErrorFiles = (): boolean => {
    return fileList.value.some(file => file.status === 'error')
  }
  
  // 重新上传失败的文件
  const retryErrorFiles = async () => {
    const errorFiles = fileList.value.filter(file => file.status === 'error')
    if (errorFiles.length === 0) {
      ElMessage.info('没有失败的文件需要重新上传')
      return
    }
    
    return uploadFiles(errorFiles)
  }
  
  // 模拟选择文件
  const triggerFileSelect = () => {
    const input = document.createElement('input')
    input.type = 'file'
    input.multiple = uploadConfig.multiple
    input.accept = uploadConfig.accept
    
    input.onchange = (event) => {
      const target = event.target as HTMLInputElement
      if (target.files && target.files.length > 0) {
        const files = Array.from(target.files)
        addFiles(files)
      }
    }
    
    input.click()
  }
  
  // 获取上传配置（用于Element Plus的Upload组件）
  const getUploadProps = (): Partial<UploadProps> => {
    return {
      action: uploadConfig.action,
      headers: uploadConfig.headers,
      multiple: uploadConfig.multiple,
      accept: uploadConfig.accept,
      limit: uploadConfig.limit,
      autoUpload: uploadConfig.autoUpload,
      showFileList: uploadConfig.showFileList,
      drag: uploadConfig.drag,
      onExceed: (files: File[], uploadFiles: UploadFiles) => {
        options.onExceed?.(files, uploadFiles)
        ElMessage.warning(`最多只能上传 ${uploadConfig.limit} 个文件`)
      },
      beforeUpload: async (file: UploadRawFile) => {
        if (options.onBeforeUpload) {
          const result = await options.onBeforeUpload(file)
          if (!result) return false
        }
        
        // 检查文件类型
        if (!checkFileType(file as any as File)) {
          ElMessage.error(`文件 ${file.name} 类型不支持`)
          return false
        }
        
        // 检查文件大小
        if (!checkFileSize(file as any as File)) {
          ElMessage.error(`文件 ${file.name} 大小超过 ${uploadConfig.maxSize}MB`)
          return false
        }
        
        return true
      },
      onRemove: (file: UploadFile) => {
        options.onRemove?.(file)
      }
    }
  }
  
  return {
    // 状态
    uploading,
    fileList,
    totalProgress,
    uploadConfig,
    
    // 方法
    addFiles,
    uploadFiles,
    uploadFile,
    removeFile,
    clearFiles,
    getUploadedUrls,
    getUploadedFiles,
    hasErrorFiles,
    retryErrorFiles,
    triggerFileSelect,
    getUploadProps
  }
}