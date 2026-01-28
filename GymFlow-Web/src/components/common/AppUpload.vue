<template>
  <div class="app-upload">
    <!-- 上传区域 -->
    <el-upload
      ref="uploadRef"
      v-bind="uploadProps"
      :file-list="fileList"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :before-remove="beforeRemove"
      :on-exceed="handleExceed"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-progress="handleProgress"
      :on-change="handleChange"
      :before-upload="beforeUpload"
      :http-request="httpRequest"
      class="upload-main"
      :class="{ 'drag-upload': drag }"
    >
      <!-- 拖拽上传 -->
      <template v-if="drag">
        <div class="drag-area">
          <el-icon class="upload-icon">
            <UploadFilled />
          </el-icon>
          <div class="drag-text">
            <p class="primary-text">将文件拖到此处，或<em>点击上传</em></p>
            <p v-if="tip" class="tip-text">{{ tip }}</p>
            <p v-if="limit" class="limit-text">
              支持{{ accept }}格式，最多{{ limit }}个文件，单个不超过{{ formatSize(maxSize) }}
            </p>
          </div>
        </div>
      </template>

      <!-- 按钮上传 -->
      <template v-else>
        <el-button :type="buttonType" :size="size" :icon="Upload">
          {{ buttonText }}
        </el-button>
        <div v-if="tip" class="button-tip">{{ tip }}</div>
      </template>

      <!-- 自定义插槽 -->
      <slot />
    </el-upload>

    <!-- 文件列表 -->
    <div v-if="showFileList && fileList.length > 0" class="file-list">
      <div class="list-header">
        <span class="list-title">已上传文件</span>
        <span class="list-count">共{{ fileList.length }}个文件</span>
        <el-button
          v-if="showClear && fileList.length > 0"
          type="text"
          size="small"
          @click="handleClear"
          class="clear-btn"
        >
          清空列表
        </el-button>
      </div>
      
      <div class="list-content">
        <div
          v-for="(file, index) in fileList"
          :key="file.uid"
          class="file-item"
          :class="{ 'uploading': file.status === 'uploading' }"
        >
          <div class="file-info">
            <!-- 文件图标 -->
            <div class="file-icon">
              <el-icon v-if="getFileIcon(file)" :size="24">
                <component :is="getFileIcon(file)" />
              </el-icon>
              <img
                v-else-if="isImage(file) && file.url"
                :src="file.url"
                :alt="file.name"
                class="file-thumbnail"
              />
              <span v-else class="file-extension">
                {{ getFileExtension(file) }}
              </span>
            </div>
            
            <!-- 文件详情 -->
            <div class="file-details">
              <div class="file-name" :title="file.name">{{ file.name }}</div>
              <div class="file-meta">
                <span class="file-size">{{ formatSize(file.size) }}</span>
                <span v-if="file.status" class="file-status">
                  <el-tag :type="getStatusTag(file.status)" size="small">
                    {{ getStatusText(file.status) }}
                  </el-tag>
                </span>
                <span v-if="file.status === 'uploading' && file.percentage" class="file-progress">
                  {{ file.percentage }}%
                </span>
              </div>
            </div>
          </div>
          
          <!-- 文件操作 -->
          <div class="file-actions">
            <el-tooltip content="预览" placement="top">
              <el-icon
                v-if="isPreviewable(file)"
                class="action-icon preview"
                @click="handlePreview(file)"
              >
                <View />
              </el-icon>
            </el-tooltip>
            
            <el-tooltip content="下载" placement="top">
              <el-icon
                v-if="file.url"
                class="action-icon download"
                @click="handleDownload(file)"
              >
                <Download />
              </el-icon>
            </el-tooltip>
            
            <el-tooltip content="删除" placement="top">
              <el-icon
                v-if="!disabled"
                class="action-icon delete"
                @click="handleRemove(file, fileList)"
              >
                <Delete />
              </el-icon>
            </el-tooltip>
            
            <el-tooltip v-if="showCopyUrl && file.url" content="复制链接" placement="top">
              <el-icon
                class="action-icon copy"
                @click="handleCopyUrl(file)"
              >
                <CopyDocument />
              </el-icon>
            </el-tooltip>
          </div>
        </div>
      </div>
    </div>

    <!-- 预览模态框 -->
    <el-dialog
      v-model="previewVisible"
      title="文件预览"
      :width="previewWidth"
      :fullscreen="previewFullscreen"
      destroy-on-close
    >
      <div v-if="previewFile" class="preview-content">
        <!-- 图片预览 -->
        <div v-if="isImage(previewFile)" class="image-preview">
          <img :src="previewFile.url || previewFile.raw?.url" :alt="previewFile.name" class="preview-image" />
        </div>
        
        <!-- PDF预览 -->
        <div v-else-if="isPDF(previewFile)" class="pdf-preview">
          <iframe
            :src="previewFile.url || previewFile.raw?.url"
            class="preview-pdf"
            frameborder="0"
          ></iframe>
        </div>
        
        <!-- 文本预览 -->
        <div v-else-if="isText(previewFile)" class="text-preview">
          <pre class="preview-text">{{ previewFile.content || '文件内容加载中...' }}</pre>
        </div>
        
        <!-- 其他文件 -->
        <div v-else class="other-preview">
          <el-empty :description="`无法预览 ${previewFile.name}，请下载后查看`" />
        </div>
      </div>
      
      <template #footer>
        <div class="preview-footer">
          <span class="file-name">{{ previewFile?.name }}</span>
          <div class="footer-actions">
            <el-button @click="previewVisible = false">关闭</el-button>
            <el-button
              v-if="previewFile?.url"
              type="primary"
              @click="handleDownload(previewFile)"
            >
              下载
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'


interface UploadPropsExtends {
  // 基础配置
  modelValue?: UploadFile[]
  action?: string
  headers?: Record<string, string>
  data?: Record<string, any>
  multiple?: boolean
  accept?: string
  drag?: boolean
  // 限制配置
  limit?: number
  maxSize?: number
  minSize?: number
  // 按钮配置
  buttonText?: string
  buttonType?: string
  // 显示配置
  showFileList?: boolean
  showClear?: boolean
  showCopyUrl?: boolean
  tip?: string
  size?: 'large' | 'default' | 'small'
  disabled?: boolean
  // 预览配置
  previewWidth?: string
  previewFullscreen?: boolean
  // 自定义请求
  httpRequest?: UploadProps['httpRequest']
  // 状态
  autoUpload?: boolean
}

const props = withDefaults(defineProps<UploadPropsExtends>(), {
  modelValue: () => [],
  action: '',
  headers: () => ({}),
  data: () => ({}),
  multiple: true,
  accept: '',
  drag: false,
  limit: 0,
  maxSize: 10 * 1024 * 1024, // 10MB
  minSize: 0,
  buttonText: '点击上传',
  buttonType: 'primary',
  showFileList: true,
  showClear: true,
  showCopyUrl: true,
  tip: '',
  size: 'default',
  disabled: false,
  previewWidth: '800px',
  previewFullscreen: false,
  autoUpload: true
})

const emit = defineEmits<{
  'update:modelValue': [files: UploadFile[]]
  'change': [files: UploadFile[], fileList: UploadFile[]]
  'remove': [file: UploadFile, fileList: UploadFile[]]
  'success': [response: any, file: UploadFile, fileList: UploadFile[]]
  'error': [error: Error, file: UploadFile, fileList: UploadFile[]]
  'exceed': [files: File[], fileList: UploadFile[]]
  'preview': [file: UploadFile]
  'download': [file: UploadFile]
  'clear': []
}>()

const uploadRef = ref<InstanceType<typeof ElUpload>>()
const fileList = ref<UploadFile[]>([...props.modelValue])
const previewVisible = ref(false)
const previewFile = ref<UploadFile | null>(null)

// 上传组件属性
const uploadProps = computed(() => {
  const baseProps: UploadProps = {
    action: props.action,
    headers: props.headers,
    data: props.data,
    multiple: props.multiple,
    accept: props.accept,
    drag: props.drag,
    limit: props.limit,
    autoUpload: props.autoUpload,
    disabled: props.disabled,
    listType: 'text',
    showFileList: false,
    beforeUpload: beforeUpload
  }

  if (props.httpRequest) {
    baseProps.httpRequest = props.httpRequest
  }

  return baseProps
})

// 文件图标映射
const fileIconMap: Record<string, any> = {
  'image': Picture,
  'pdf': Document,
  'video': VideoCamera,
  'audio': Music,
  'text': Files,
  'default': Document
}

// 格式化文件大小
const formatSize = (size?: number) => {
  if (!size) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB']
  let index = 0
  let formattedSize = size
  
  while (formattedSize >= 1024 && index < units.length - 1) {
    formattedSize /= 1024
    index++
  }
  
  return `${formattedSize.toFixed(2)} ${units[index]}`
}

// 获取文件图标
const getFileIcon = (file: UploadFile) => {
  const type = getFileType(file)
  return fileIconMap[type] || fileIconMap.default
}

// 获取文件类型
const getFileType = (file: UploadFile) => {
  const name = file.name || ''
  const type = file.raw?.type || ''
  
  if (type.startsWith('image/')) return 'image'
  if (type === 'application/pdf') return 'pdf'
  if (type.startsWith('video/')) return 'video'
  if (type.startsWith('audio/')) return 'audio'
  if (type.startsWith('text/')) return 'text'
  
  const extension = name.split('.').pop()?.toLowerCase()
  const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp']
  const videoExtensions = ['mp4', 'avi', 'mov', 'wmv', 'flv']
  const audioExtensions = ['mp3', 'wav', 'ogg', 'flac']
  const textExtensions = ['txt', 'json', 'xml', 'csv', 'md']
  
  if (imageExtensions.includes(extension || '')) return 'image'
  if (videoExtensions.includes(extension || '')) return 'video'
  if (audioExtensions.includes(extension || '')) return 'audio'
  if (textExtensions.includes(extension || '')) return 'text'
  if (extension === 'pdf') return 'pdf'
  
  return 'default'
}

// 获取文件扩展名
const getFileExtension = (file: UploadFile) => {
  const name = file.name || ''
  const extension = name.split('.').pop()?.toUpperCase()
  return extension || 'FILE'
}

// 判断是否为图片
const isImage = (file: UploadFile) => {
  return getFileType(file) === 'image'
}

// 判断是否为PDF
const isPDF = (file: UploadFile) => {
  return getFileType(file) === 'pdf'
}

// 判断是否为文本文件
const isText = (file: UploadFile) => {
  return getFileType(file) === 'text'
}

// 判断是否可预览
const isPreviewable = (file: UploadFile) => {
  const type = getFileType(file)
  return ['image', 'pdf', 'text'].includes(type)
}

// 获取状态标签类型
const getStatusTag = (status: string) => {
  const map: Record<string, string> = {
    'ready': 'info',
    'uploading': 'primary',
    'success': 'success',
    'fail': 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    'ready': '待上传',
    'uploading': '上传中',
    'success': '成功',
    'fail': '失败'
  }
  return map[status] || status
}

// 文件预览处理
const handlePreview = (file: UploadFile) => {
  if (!isPreviewable(file)) {
    ElMessage.info('该文件类型暂不支持预览')
    return
  }
  
  previewFile.value = file
  previewVisible.value = true
  emit('preview', file)
}

// 文件删除处理
const handleRemove = (file: UploadFile, fileList: UploadFile[]) => {
  emit('remove', file, fileList)
  emit('update:modelValue', [...fileList])
}

// 删除前确认
const beforeRemove = (file: UploadFile) => {
  return new Promise<boolean>((resolve) => {
    ElMessageBox.confirm(
      `确定删除文件 ${file.name} 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
      .then(() => resolve(true))
      .catch(() => resolve(false))
  })
}

// 超出限制处理
const handleExceed = (files: File[], fileList: UploadFile[]) => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`)
  emit('exceed', files, fileList)
}

// 上传成功处理
const handleSuccess = (response: any, file: UploadFile, fileList: UploadFile[]) => {
  emit('success', response, file, fileList)
  emit('update:modelValue', [...fileList])
}

// 上传错误处理
const handleError = (error: Error, file: UploadFile, fileList: UploadFile[]) => {
  console.error('上传失败:', error)
  ElMessage.error(`文件 ${file.name} 上传失败`)
  emit('error', error, file, fileList)
}

// 上传进度处理
const handleProgress = (event: ProgressEvent, file: UploadFile, fileList: UploadFile[]) => {
  // 可以在这里处理进度显示
}

// 文件变化处理
const handleChange = (file: UploadFile, fileList: UploadFile[]) => {
  emit('change', file, fileList)
  emit('update:modelValue', [...fileList])
}

// 上传前验证
const beforeUpload = (rawFile: UploadRawFile) => {
  // 大小验证
  if (props.maxSize && rawFile.size > props.maxSize) {
    ElMessage.error(`文件大小不能超过 ${formatSize(props.maxSize)}`)
    return false
  }
  
  if (props.minSize && rawFile.size < props.minSize) {
    ElMessage.error(`文件大小不能小于 ${formatSize(props.minSize)}`)
    return false
  }
  
  // 类型验证
  if (props.accept) {
    const acceptTypes = props.accept.split(',').map(type => type.trim())
    const fileType = rawFile.type || ''
    const fileExtension = rawFile.name.split('.').pop()?.toLowerCase() || ''
    
    const isTypeValid = acceptTypes.some(type => {
      if (type.startsWith('.')) {
        return `.${fileExtension}` === type
      }
      return fileType === type || fileType.startsWith(type.replace('/*', '/'))
    })
    
    if (!isTypeValid) {
      ElMessage.error(`仅支持 ${props.accept} 格式的文件`)
      return false
    }
  }
  
  return true
}

// 文件下载
const handleDownload = (file: UploadFile) => {
  if (!file.url) {
    ElMessage.warning('文件链接不存在')
    return
  }
  
  const link = document.createElement('a')
  link.href = file.url
  link.download = file.name || 'download'
  link.style.display = 'none'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  emit('download', file)
}

// 复制文件链接
const handleCopyUrl = async (file: UploadFile) => {
  if (!file.url) {
    ElMessage.warning('文件链接不存在')
    return
  }
  
  try {
    await navigator.clipboard.writeText(file.url)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

// 清空文件列表
const handleClear = () => {
  ElMessageBox.confirm(
    '确定要清空所有文件吗？',
    '清空确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(() => {
      fileList.value = []
      emit('update:modelValue', [])
      emit('clear')
    })
    .catch(() => {})
}

// 手动上传
const submit = () => {
  if (uploadRef.value) {
    uploadRef.value.submit()
  }
}

// 清空已选择的文件
const clearFiles = () => {
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// 获取内部文件列表
const getFileList = () => {
  return [...fileList.value]
}

// 添加文件
const addFile = (file: File) => {
  if (uploadRef.value) {
    const uploadFile: UploadFile = {
      uid: Date.now().toString(),
      name: file.name,
      size: file.size,
      raw: file,
      status: 'ready'
    }
    fileList.value.push(uploadFile)
    emit('update:modelValue', [...fileList.value])
  }
}

// 暴露的方法
defineExpose({
  getUploadRef: () => uploadRef.value,
  submit,
  clearFiles,
  getFileList,
  addFile,
  download: handleDownload,
  preview: handlePreview
})

// 监听外部值变化
watch(() => props.modelValue, (value) => {
  fileList.value = [...value]
}, { deep: true })

// 初始化文件状态
onMounted(() => {
  // 确保所有文件都有正确的状态
  fileList.value.forEach(file => {
    if (!file.status && file.url) {
      file.status = 'success'
    }
  })
})
</script>

<style scoped lang="scss">
.app-upload {
  width: 100%;
  
  .upload-main {
    width: 100%;
    
    &.drag-upload {
      .drag-area {
        padding: 40px 20px;
        border: 2px dashed #dcdfe6;
        border-radius: 8px;
        text-align: center;
        background: #fafafa;
        transition: all 0.3s;
        cursor: pointer;
        
        &:hover {
          border-color: #409eff;
          background: #f0f9ff;
        }
        
        .upload-icon {
          font-size: 48px;
          color: #c0c4cc;
          margin-bottom: 16px;
        }
        
        .drag-text {
          .primary-text {
            margin: 0 0 8px 0;
            font-size: 16px;
            color: #606266;
            
            em {
              font-style: normal;
              color: #409eff;
              margin: 0 4px;
            }
          }
          
          .tip-text {
            margin: 0 0 8px 0;
            font-size: 14px;
            color: #909399;
          }
          
          .limit-text {
            margin: 0;
            font-size: 12px;
            color: #c0c4cc;
          }
        }
      }
    }
    
    .button-tip {
      margin-top: 8px;
      font-size: 12px;
      color: #909399;
    }
  }
  
  .file-list {
    margin-top: 20px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    overflow: hidden;
    
    .list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      background: #f5f7fa;
      border-bottom: 1px solid #ebeef5;
      
      .list-title {
        font-weight: 600;
        color: #303133;
      }
      
      .list-count {
        font-size: 14px;
        color: #606266;
      }
      
      .clear-btn {
        padding: 0;
        height: auto;
      }
    }
    
    .list-content {
      max-height: 400px;
      overflow-y: auto;
      
      .file-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 16px;
        border-bottom: 1px solid #ebeef5;
        transition: all 0.3s;
        
        &:hover {
          background: #f5f7fa;
        }
        
        &.uploading {
          background: #f0f9ff;
        }
        
        &:last-child {
          border-bottom: none;
        }
        
        .file-info {
          display: flex;
          align-items: center;
          gap: 12px;
          flex: 1;
          min-width: 0;
          
          .file-icon {
            flex-shrink: 0;
            width: 40px;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 4px;
            background: #f5f7fa;
            color: #409eff;
            
            .file-thumbnail {
              width: 100%;
              height: 100%;
              object-fit: cover;
              border-radius: 4px;
            }
            
            .file-extension {
              font-size: 12px;
              font-weight: 600;
              color: #606266;
            }
          }
          
          .file-details {
            flex: 1;
            min-width: 0;
            
            .file-name {
              font-weight: 500;
              color: #303133;
              margin-bottom: 4px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
            
            .file-meta {
              display: flex;
              align-items: center;
              gap: 12px;
              font-size: 12px;
              color: #909399;
              
              .file-status {
                :deep(.el-tag) {
                  height: 20px;
                  line-height: 18px;
                  padding: 0 6px;
                }
              }
            }
          }
        }
        
        .file-actions {
          display: flex;
          align-items: center;
          gap: 12px;
          flex-shrink: 0;
          
          .action-icon {
            font-size: 18px;
            cursor: pointer;
            transition: all 0.3s;
            
            &.preview {
              color: #409eff;
              
              &:hover {
                color: #66b1ff;
              }
            }
            
            &.download {
              color: #67c23a;
              
              &:hover {
                color: #85ce61;
              }
            }
            
            &.delete {
              color: #f56c6c;
              
              &:hover {
                color: #f78989;
              }
            }
            
            &.copy {
              color: #e6a23c;
              
              &:hover {
                color: #ebb563;
              }
            }
            
            &:hover {
              transform: scale(1.1);
            }
          }
        }
      }
    }
  }
  
  .preview-content {
    width: 100%;
    height: 60vh;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: auto;
    
    .image-preview {
      max-width: 100%;
      max-height: 100%;
      
      .preview-image {
        max-width: 100%;
        max-height: 100%;
        object-fit: contain;
      }
    }
    
    .pdf-preview {
      width: 100%;
      height: 100%;
      
      .preview-pdf {
        width: 100%;
        height: 100%;
      }
    }
    
    .text-preview {
      width: 100%;
      height: 100%;
      padding: 20px;
      background: #f5f7fa;
      border-radius: 4px;
      overflow: auto;
      
      .preview-text {
        margin: 0;
        white-space: pre-wrap;
        word-break: break-all;
        font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
        font-size: 14px;
        line-height: 1.5;
        color: #333;
      }
    }
    
    .other-preview {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  
  .preview-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .file-name {
      flex: 1;
      font-size: 14px;
      color: #606266;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .footer-actions {
      display: flex;
      gap: 10px;
    }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .app-upload {
    .file-item {
      flex-direction: column;
      align-items: flex-start !important;
      gap: 12px;
      
      .file-actions {
        align-self: flex-end;
      }
    }
  }
}
</style>