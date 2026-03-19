// 消息状态管理

import { getUnreadCount, getMessageList } from '../services/api/message.api'
import { Message } from '../services/types/message.types'

export class MessageStore {
  private _unreadCount: number = 0
  private _messages: Message[] = []
  private _hasMore: boolean = true
  private _currentPage: number = 1
  private _pageSize: number = 20
  private _loading: boolean = false
  private _initialized: boolean = false // 添加初始化标志

  constructor() {
    // 不在构造函数中自动获取未读消息数
  }

  /**
   * 初始化（在登录后调用）
   */
  async init() {
    if (this._initialized) return
    await this.refreshUnreadCount()
    this._initialized = true
  }

  /**
   * 刷新未读消息数
   */
  async refreshUnreadCount() {
    // 只有在有token的情况下才调用接口
    const token = wx.getStorageSync('token')
    if (!token) {
      this._unreadCount = 0
      this.updateTabBarBadge()
      return
    }

    try {
      const count = await getUnreadCount()
      this._unreadCount = count
      this.updateTabBarBadge()
    } catch (error) {
      console.error('获取未读消息数失败:', error)
    }
  }

  /**
   * 更新TabBar徽标
   */
  private updateTabBarBadge() {
    const app = getApp() as any
    const userStore = app.globalData.userStore
    
    if (!userStore?.isLogin) return
    
    // 根据角色设置不同的TabBar索引
    const tabIndex = userStore.role === 'MEMBER' ? 2 : 2 // 我的tab都在索引2
    
    if (this._unreadCount > 0) {
      wx.setTabBarBadge({
        index: tabIndex,
        text: this._unreadCount > 99 ? '99+' : String(this._unreadCount)
      })
    } else {
      wx.removeTabBarBadge({
        index: tabIndex
      })
    }
  }

  /**
   * 设置未读消息数
   */
  setUnreadCount(count: number) {
    this._unreadCount = count
    this.updateTabBarBadge()
  }

  /**
   * 增加未读消息数
   */
  incrementUnread() {
    this._unreadCount += 1
    this.updateTabBarBadge()
  }

  /**
   * 清除未读消息数
   */
  clearUnread() {
    this._unreadCount = 0
    this.updateTabBarBadge()
  }

  /**
   * 加载消息列表
   */
  async loadMessages(refresh: boolean = false) {
    // 只有在有token的情况下才加载消息列表
    const token = wx.getStorageSync('token')
    if (!token) {
      return { list: [], total: 0, pages: 0 }
    }

    if (this._loading) return
    
    if (refresh) {
      this._currentPage = 1
      this._hasMore = true
      this._messages = []
    }
    
    if (!this._hasMore) return
    
    this._loading = true
    
    try {
      const result = await getMessageList({
        pageNum: this._currentPage,
        pageSize: this._pageSize
      })
      
      if (refresh) {
        this._messages = result.list
      } else {
        this._messages = [...this._messages, ...result.list]
      }
      
      this._hasMore = this._currentPage < result.pages
      this._currentPage++
      
      return result
    } catch (error) {
      console.error('加载消息列表失败:', error)
      throw error
    } finally {
      this._loading = false
    }
  }

  /**
   * 设置消息列表
   */
  setMessages(messages: Message[]) {
    this._messages = messages
  }

  /**
   * 标记消息为已读
   */
  markAsRead(messageId: number) {
    const message = this._messages.find(m => m.id === messageId)
    if (message && message.isRead === 0) {
      message.isRead = 1
      this._unreadCount = Math.max(0, this._unreadCount - 1)
      this.updateTabBarBadge()
    }
  }

  /**
   * 添加新消息
   */
  addMessage(message: Message) {
    this._messages.unshift(message)
    if (message.isRead === 0) {
      this.incrementUnread()
    }
  }

  /**
   * 重置状态
   */
  reset() {
    this._unreadCount = 0
    this._messages = []
    this._hasMore = true
    this._currentPage = 1
    this._loading = false
    this._initialized = false
    this.updateTabBarBadge()
  }

  // Getters
  get unreadCount() {
    return this._unreadCount
  }

  get messages() {
    return this._messages
  }

  get hasMore() {
    return this._hasMore
  }

  get loading() {
    return this._loading
  }
}

// 导出单例
export const messageStore = new MessageStore()