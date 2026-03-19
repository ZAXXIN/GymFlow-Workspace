// 消息相关自定义Hook

import { messageStore } from '../stores/message.store'
import { 
  markMessageAsRead,
  markAllAsRead,
  deleteMessage 
} from '../services/api/message.api'
import { showModal, showSuccess, showError, showLoading, hideLoading } from '../utils/wx-util'

export const useMessage = () => {
  /**
   * 加载消息列表
   */
  const loadMessages = async (refresh: boolean = false) => {
    try {
      return await messageStore.loadMessages(refresh)
    } catch (error: any) {
      showError(error.message || '加载消息失败')
      throw error
    }
  }

  /**
   * 刷新未读消息数
   */
  const refreshUnreadCount = async () => {
    try {
      await messageStore.refreshUnreadCount()
    } catch (error: any) {
      console.error('刷新未读消息数失败:', error)
    }
  }

  /**
   * 标记消息为已读
   */
  const markAsRead = async (messageId: number) => {
    try {
      await markMessageAsRead(messageId)
      messageStore.markAsRead(messageId)
    } catch (error: any) {
      console.error('标记已读失败:', error)
    }
  }

  /**
   * 标记所有为已读
   */
  const markAllRead = async () => {
    try {
      showLoading()
      await markAllAsRead()
      messageStore.clearUnread()
      // 更新所有消息为已读
      messageStore['_messages'].forEach(msg => msg.isRead = 1)
      hideLoading()
      showSuccess('已全部标记为已读')
    } catch (error: any) {
      hideLoading()
      showError(error.message || '操作失败')
      throw error
    }
  }

  /**
   * 删除消息
   */
  const removeMessage = async (messageId: number) => {
    try {
      const confirm = await showModal({
        title: '提示',
        content: '确定要删除该消息吗？'
      })
      
      if (!confirm) return false
      
      showLoading()
      await deleteMessage(messageId)
      
      // 从列表中移除
      const index = messageStore['_messages'].findIndex(m => m.id === messageId)
      if (index !== -1) {
        const message = messageStore['_messages'][index]
        messageStore['_messages'].splice(index, 1)
        
        // 如果删除的是未读消息，更新计数
        if (message.isRead === 0) {
          messageStore.setUnreadCount(messageStore.unreadCount - 1)
        }
      }
      
      hideLoading()
      showSuccess('删除成功')
      
      return true
    } catch (error: any) {
      hideLoading()
      showError(error.message || '删除失败')
      throw error
    }
  }

  /**
   * 获取未读消息列表
   */
  const getUnreadMessages = () => {
    return messageStore.messages.filter(m => m.isRead === 0)
  }

  /**
   * 按类型获取消息
   */
  const getMessagesByType = (type: string) => {
    return messageStore.messages.filter(m => m.type === type)
  }

  return {
    messages: messageStore.messages,
    unreadCount: messageStore.unreadCount,
    hasMore: messageStore.hasMore,
    loading: messageStore.loading,
    loadMessages,
    refreshUnreadCount,
    markAsRead,
    markAllRead,
    deleteMessage: removeMessage,
    getUnreadMessages,
    getMessagesByType
  }
}