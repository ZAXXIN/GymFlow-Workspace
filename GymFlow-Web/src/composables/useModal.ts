import { ref, reactive, computed, watch } from 'vue'
import type { Ref } from 'vue'
import type { Action } from '@/types'

interface ModalOptions<T = any> {
  // 弹窗配置
  title?: string
  width?: string
  fullscreen?: boolean
  top?: string
  modal?: boolean
  lockScroll?: boolean
  closeOnClickModal?: boolean
  closeOnPressEscape?: boolean
  showClose?: boolean
  beforeClose?: (done: () => void) => void
  
  // 操作按钮
  showCancel?: boolean
  showConfirm?: boolean
  cancelText?: string
  confirmText?: string
  cancelProps?: Record<string, any>
  confirmProps?: Record<string, any>
  
  // 自定义操作
  customActions?: Action[]
  
  // 事件回调
  onOpen?: () => void
  onClose?: () => void
  onConfirm?: (data?: T) => Promise<boolean> | boolean | void
  onCancel?: () => void
  onActionClick?: (action: string, data?: T) => void
}

/**
 * 弹窗管理组合式函数
 * @param options 弹窗配置选项
 * @returns 弹窗相关的状态和方法
 */
export function useModal<T = any>(options: ModalOptions<T> = {}) {
  // 弹窗状态
  const visible = ref(false)
  const loading = ref(false)
  const modalData = ref<T | null>(null) as Ref<T | null>
  
  // 弹窗配置
  const modalConfig = reactive({
    title: options.title || '弹窗',
    width: options.width || '50%',
    fullscreen: options.fullscreen || false,
    top: options.top || '15vh',
    modal: options.modal ?? true,
    lockScroll: options.lockScroll ?? true,
    closeOnClickModal: options.closeOnClickModal ?? true,
    closeOnPressEscape: options.closeOnPressEscape ?? true,
    showClose: options.showClose ?? true
  })
  
  // 操作按钮配置
  const actionConfig = reactive({
    showCancel: options.showCancel ?? true,
    showConfirm: options.showConfirm ?? true,
    cancelText: options.cancelText || '取消',
    confirmText: options.confirmText || '确定',
    cancelProps: options.cancelProps || {},
    confirmProps: options.confirmProps || {}
  })
  
  // 自定义操作
  const customActions = computed(() => options.customActions || [])
  
  // 打开弹窗
  const openModal = (data?: T) => {
    visible.value = true
    modalData.value = data || null
    
    // 回调函数
    options.onOpen?.()
  }
  
  // 关闭弹窗
  const closeModal = () => {
    if (options.beforeClose) {
      options.beforeClose(() => {
        doClose()
      })
    } else {
      doClose()
    }
  }
  
  // 执行关闭
  const doClose = () => {
    visible.value = false
    
    // 延迟清空数据，避免动画过程中数据丢失
    setTimeout(() => {
      modalData.value = null
      loading.value = false
    }, 300)
    
    // 回调函数
    options.onClose?.()
  }
  
  // 确认操作
  const handleConfirm = async () => {
    if (options.onConfirm) {
      try {
        loading.value = true
        const result = await options.onConfirm(modalData.value as T)
        
        if (result !== false) {
          closeModal()
        }
      } catch (error) {
        console.error('确认操作失败:', error)
      } finally {
        loading.value = false
      }
    } else {
      closeModal()
    }
  }
  
  // 取消操作
  const handleCancel = () => {
    options.onCancel?.()
    closeModal()
  }
  
  // 处理操作按钮点击
  const handleActionClick = (action: string) => {
    options.onActionClick?.(action, modalData.value as T)
  }
  
  // 更新弹窗数据
  const updateModalData = (data: Partial<T>) => {
    if (modalData.value) {
      modalData.value = { ...modalData.value, ...data }
    } else {
      modalData.value = data as T
    }
  }
  
  // 设置弹窗标题
  const setModalTitle = (title: string) => {
    modalConfig.title = title
  }
  
  // 设置弹窗宽度
  const setModalWidth = (width: string) => {
    modalConfig.width = width
  }
  
  // 切换全屏
  const toggleFullscreen = () => {
    modalConfig.fullscreen = !modalConfig.fullscreen
  }
  
  // 显示确认弹窗
  const showConfirmModal = (
    title: string,
    message: string,
    options?: {
      type?: 'success' | 'warning' | 'info' | 'error'
      confirmButtonText?: string
      cancelButtonText?: string
      onConfirm?: () => Promise<void> | void
      onCancel?: () => void
    }
  ) => {
    return ElMessageBox.confirm(message, title, {
      type: options?.type || 'warning',
      confirmButtonText: options?.confirmButtonText || '确定',
      cancelButtonText: options?.cancelButtonText || '取消',
      beforeClose: async (action, instance, done) => {
        if (action === 'confirm') {
          if (options?.onConfirm) {
            instance.confirmButtonLoading = true
            try {
              await options.onConfirm()
              done()
            } catch (error) {
              console.error('确认操作失败:', error)
            } finally {
              instance.confirmButtonLoading = false
            }
          } else {
            done()
          }
        } else {
          options?.onCancel?.()
          done()
        }
      }
    })
  }
  
  // 显示提示弹窗
  const showMessageModal = (
    title: string,
    message: string,
    options?: {
      type?: 'success' | 'warning' | 'info' | 'error'
      confirmButtonText?: string
      onClose?: () => void
    }
  ) => {
    return ElMessageBox.alert(message, title, {
      type: options?.type || 'info',
      confirmButtonText: options?.confirmButtonText || '确定',
      callback: options?.onClose
    })
  }
  
  // 显示输入弹窗
  const showInputModal = (
    title: string,
    message: string,
    options?: {
      inputPlaceholder?: string
      inputValue?: string
      inputPattern?: RegExp
      inputErrorMessage?: string
      confirmButtonText?: string
      cancelButtonText?: string
      onConfirm?: (value: string) => Promise<void> | void
      onCancel?: () => void
    }
  ) => {
    return ElMessageBox.prompt(message, title, {
      inputPlaceholder: options?.inputPlaceholder,
      inputValue: options?.inputValue,
      inputPattern: options?.inputPattern,
      inputErrorMessage: options?.inputErrorMessage,
      confirmButtonText: options?.confirmButtonText || '确定',
      cancelButtonText: options?.cancelButtonText || '取消',
      beforeClose: async (action, instance, done) => {
        if (action === 'confirm') {
          const value = instance.inputValue
          if (options?.onConfirm) {
            instance.confirmButtonLoading = true
            try {
              await options.onConfirm(value)
              done()
            } catch (error) {
              console.error('确认操作失败:', error)
            } finally {
              instance.confirmButtonLoading = false
            }
          } else {
            done()
          }
        } else {
          options?.onCancel?.()
          done()
        }
      }
    })
  }
  
  // 监听可见性变化
  watch(visible, (newVal) => {
    if (!newVal) {
      // 弹窗关闭时清空数据
      setTimeout(() => {
        modalData.value = null
      }, 300)
    }
  })
  
  return {
    // 状态
    visible,
    loading,
    modalData,
    modalConfig,
    actionConfig,
    customActions,
    
    // 方法
    openModal,
    closeModal,
    handleConfirm,
    handleCancel,
    handleActionClick,
    updateModalData,
    setModalTitle,
    setModalWidth,
    toggleFullscreen,
    showConfirmModal,
    showMessageModal,
    showInputModal
  }
}