import { ref } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import axios from 'axios'
import { saveAs } from 'file-saver'

interface DownloadOptions {
  // 下载配置
  showProgress?: boolean
  showSuccessMessage?: boolean
  showErrorMessage?: boolean
  timeout?: number
  headers?: Record<string, string>
  
  // 事件回调
  onStart?: () => void
  onProgress?: (progress: number) => void
  onSuccess?: (data: any) => void
  onError?: (error: any) => void
  onComplete?: () => void
}

/**
 * 下载功能组合式函数
 * @param options 下载配置选项
 * @returns 下载相关的状态和方法
 */
export function useDownload(options: DownloadOptions = {}) {
  // 下载状态
  const downloading = ref(false)
  const progress = ref(0)
  const currentTask = ref<string | null>(null)
  
  // 默认配置
  const defaultOptions: DownloadOptions = {
    showProgress: true,
    showSuccessMessage: true,
    showErrorMessage: true,
    timeout: 30000,
    headers: {
      'Content-Type': 'application/json'
    },
    ...options
  }
  
  // 下载文件
  const downloadFile = async (
    url: string,
    filename?: string,
    requestOptions?: {
      method?: string
      data?: any
      params?: any
      headers?: Record<string, string>
    }
  ): Promise<boolean> => {
    try {
      downloading.value = true
      progress.value = 0
      currentTask.value = filename || '文件下载'
      
      // 回调函数
      defaultOptions.onStart?.()
      
      if (defaultOptions.showProgress) {
        ElNotification({
          title: '下载开始',
          message: `正在下载: ${currentTask.value}`,
          type: 'info',
          duration: 2000
        })
      }
      
      const response = await axios({
        url,
        method: requestOptions?.method || 'GET',
        data: requestOptions?.data,
        params: requestOptions?.params,
        headers: {
          ...defaultOptions.headers,
          ...requestOptions?.headers
        },
        responseType: 'blob',
        timeout: defaultOptions.timeout,
        onDownloadProgress: (progressEvent) => {
          if (progressEvent.total) {
            const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
            progress.value = percent
            
            // 回调函数
            defaultOptions.onProgress?.(percent)
          }
        }
      })
      
      // 获取文件名
      let finalFilename = filename
      if (!finalFilename) {
        // 从Content-Disposition头中获取文件名
        const contentDisposition = response.headers['content-disposition']
        if (contentDisposition) {
          const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
          if (filenameMatch && filenameMatch[1]) {
            finalFilename = filenameMatch[1].replace(/['"]/g, '')
          }
        }
        
        // 如果还是没有文件名，使用时间戳
        if (!finalFilename) {
          finalFilename = `下载文件_${new Date().getTime()}`
        }
      }
      
      // 处理响应
      const blob = response.data
      
      // 检查是否是错误响应（JSON格式）
      if (blob.type === 'application/json') {
        const reader = new FileReader()
        reader.onload = () => {
          try {
            const result = JSON.parse(reader.result as string)
            if (result.code !== 200) {
              throw new Error(result.message || '下载失败')
            }
          } catch (error) {
            throw error
          }
        }
        reader.readAsText(blob)
        return false
      }
      
      // 保存文件
      saveAs(blob, finalFilename)
      
      // 更新状态
      progress.value = 100
      
      // 回调函数
      defaultOptions.onSuccess?.(response.data)
      
      if (defaultOptions.showSuccessMessage) {
        ElMessage.success('下载成功')
      }
      
      return true
    } catch (error: any) {
      console.error('下载失败:', error)
      
      // 回调函数
      defaultOptions.onError?.(error)
      
      if (defaultOptions.showErrorMessage) {
        let errorMessage = '下载失败'
        
        if (error.response) {
          // HTTP错误
          if (error.response.status === 404) {
            errorMessage = '文件不存在'
          } else if (error.response.status === 403) {
            errorMessage = '没有下载权限'
          } else if (error.response.status === 500) {
            errorMessage = '服务器错误'
          }
        } else if (error.message) {
          errorMessage = error.message
        }
        
        ElMessage.error(errorMessage)
      }
      
      return false
    } finally {
      downloading.value = false
      
      // 延迟清空进度
      setTimeout(() => {
        progress.value = 0
        currentTask.value = null
      }, 1000)
      
      // 回调函数
      defaultOptions.onComplete?.()
    }
  }
  
  // 下载文本内容
  const downloadText = (content: string, filename: string, mimeType: string = 'text/plain') => {
    try {
      const blob = new Blob([content], { type: mimeType })
      saveAs(blob, filename)
      
      if (defaultOptions.showSuccessMessage) {
        ElMessage.success('下载成功')
      }
      
      return true
    } catch (error) {
      console.error('下载文本失败:', error)
      
      if (defaultOptions.showErrorMessage) {
        ElMessage.error('下载失败')
      }
      
      return false
    }
  }
  
  // 下载JSON数据
  const downloadJson = (data: any, filename: string) => {
    const content = JSON.stringify(data, null, 2)
    return downloadText(content, filename, 'application/json')
  }
  
  // 下载CSV数据
  const downloadCsv = (data: any[], filename: string) => {
    if (!data || data.length === 0) {
      ElMessage.warning('没有数据可下载')
      return false
    }
    
    try {
      // 获取表头
      const headers = Object.keys(data[0])
      
      // 构建CSV内容
      let csvContent = headers.join(',') + '\n'
      
      // 添加数据行
      data.forEach(row => {
        const values = headers.map(header => {
          let value = row[header]
          
          // 处理特殊字符
          if (typeof value === 'string') {
            // 如果包含逗号、引号或换行符，用引号包裹
            if (value.includes(',') || value.includes('"') || value.includes('\n')) {
              value = `"${value.replace(/"/g, '""')}"`
            }
          }
          
          // 处理null或undefined
          if (value === null || value === undefined) {
            value = ''
          }
          
          return value
        })
        
        csvContent += values.join(',') + '\n'
      })
      
      return downloadText(csvContent, filename, 'text/csv')
    } catch (error) {
      console.error('生成CSV失败:', error)
      
      if (defaultOptions.showErrorMessage) {
        ElMessage.error('生成CSV失败')
      }
      
      return false
    }
  }
  
  // 取消下载
  const cancelDownload = () => {
    // 注意：这里需要配合axios的cancel token使用
    // 在实际使用中，需要保存cancel token并在需要时调用cancel方法
    downloading.value = false
    progress.value = 0
    currentTask.value = null
    
    ElMessage.info('下载已取消')
  }
  
  return {
    // 状态
    downloading,
    progress,
    currentTask,
    
    // 方法
    downloadFile,
    downloadText,
    downloadJson,
    downloadCsv,
    cancelDownload
  }
}