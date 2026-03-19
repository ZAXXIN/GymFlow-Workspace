// 消息列表页面
import { messageStore } from '../../../stores/message.store'
import { markMessageAsRead, markAllAsRead, deleteMessage } from '../../../services/api/message.api'
import { formatTime } from '../../../utils/date'
import { showToast, showModal, showLoading, hideLoading } from '../../../utils/wx-util'

Page({
  data: {
    // 当前选中Tab 0-全部 1-预约 2-签到 3-系统
    activeTab: 0,
    
    // 消息列表
    messages: [],
    
    // 各类型未读数
    unreadCount: {
      total: 0,
      BOOKING: 0,
      CHECKIN: 0,
      SYSTEM: 0
    },
    
    // 是否有未读消息
    hasUnread: false,
    
    // 分页
    pageNum: 1,
    pageSize: 20,
    hasMore: true,
    
    // 加载状态
    loading: false,
    
    // 错误状态
    error: null
  },

  onLoad: function() {
    this.loadMessages(true)
    this.updateUnreadCount()
  },

  onShow: function() {
    this.updateUnreadCount()
  },

  onPullDownRefresh: function() {
    this.loadMessages(true)
  },

  /**
   * 加载消息列表
   */
  loadMessages: function(refresh) {
    var that = this
    var data = this.data
    var loading = data.loading
    var hasMore = data.hasMore
    var pageNum = data.pageNum
    var pageSize = data.pageSize
    var activeTab = data.activeTab
    
    if (loading) return
    if (!refresh && !hasMore) return
    
    this.setData({ loading: true, error: null })
    
    // 根据tab筛选类型
    var type = undefined
    if (activeTab === 1) {
      type = 'BOOKING'
    } else if (activeTab === 2) {
      type = 'CHECKIN'
    } else if (activeTab === 3) {
      type = 'SYSTEM'
    }
    
    messageStore.loadMessages(refresh).then(function() {
      // 获取所有消息，确保是数组
      var allMessages = messageStore.messages || []
      
      // 根据类型筛选
      var filteredList = allMessages
      if (type) {
        filteredList = []
        for (var i = 0; i < allMessages.length; i++) {
          var msg = allMessages[i]
          if (msg.type === type) {
            filteredList.push(msg)
          }
        }
      }
      
      that.setData({
        messages: filteredList,
        hasMore: messageStore.hasMore,
        loading: false,
        error: null
      })
      
      that.updateUnreadCount()
      
      if (refresh) {
        wx.stopPullDownRefresh()
      }
      
    }).catch(function(error) {
      console.error('加载消息失败:', error)
      that.setData({ 
        loading: false,
        error: error.message || '加载失败'
      })
      if (refresh) {
        wx.stopPullDownRefresh()
      }
    })
  },

  /**
   * 更新未读数
   */
  updateUnreadCount: function() {
    var messages = messageStore.messages || []
    var total = 0
    var booking = 0
    var checkin = 0
    var system = 0
    
    for (var i = 0; i < messages.length; i++) {
      var msg = messages[i]
      if (msg.isRead === 0) {
        total++
        if (msg.type === 'BOOKING') {
          booking++
        } else if (msg.type === 'CHECKIN') {
          checkin++
        } else if (msg.type === 'SYSTEM') {
          system++
        }
      }
    }
    
    this.setData({
      unreadCount: {
        total: total,
        BOOKING: booking,
        CHECKIN: checkin,
        SYSTEM: system
      },
      hasUnread: total > 0
    })
  },

  /**
   * Tab切换
   */
  onTabChange: function(e) {
    var index = e.currentTarget.dataset.index
    this.setData({ 
      activeTab: parseInt(index),
      pageNum: 1,
      hasMore: true
    }, function() {
      this.loadMessages(true)
    })
  },

  /**
   * 点击消息
   */
  onMessageTap: function(e) {
    var message = e.currentTarget.dataset.message
    
    // 标记已读
    if (message.isRead === 0) {
      this.markAsRead(message.id)
    }
    
    // 根据消息类型跳转
    if (message.type === 'BOOKING' && message.data && message.data.bookingId) {
      wx.navigateTo({
        url: '/pages/common/booking-detail/index?id=' + message.data.bookingId
      })
    } else if (message.type === 'CHECKIN' && message.data && message.data.bookingId) {
      wx.navigateTo({
        url: '/pages/member/checkin-code/index?bookingId=' + message.data.bookingId
      })
    } else if (message.type === 'ORDER' && message.data && message.data.orderId) {
      wx.navigateTo({
        url: '/pages/common/order-detail/index?id=' + message.data.orderId
      })
    }
  },

  /**
   * 标记已读
   */
  markAsRead: function(messageId) {
    var that = this
    markMessageAsRead(messageId).then(function() {
      messageStore.markAsRead(messageId)
      
      // 更新列表显示
      var messages = that.data.messages
      for (var i = 0; i < messages.length; i++) {
        if (messages[i].id === messageId) {
          messages[i].isRead = 1
          break
        }
      }
      
      that.setData({ messages: messages })
      that.updateUnreadCount()
      
    }).catch(function(error) {
      console.error('标记已读失败:', error)
    })
  },

  /**
   * 全部标记已读
   */
  onMarkAllRead: function() {
    var that = this
    showModal({
      title: '提示',
      content: '确定要将所有消息标记为已读吗？'
    }).then(function(confirm) {
      if (!confirm) return
      
      showLoading()
      
      markAllAsRead().then(function() {
        messageStore.clearUnread()
        
        // 更新列表
        var messages = that.data.messages
        for (var i = 0; i < messages.length; i++) {
          messages[i].isRead = 1
        }
        
        that.setData({ messages: messages })
        that.updateUnreadCount()
        
        hideLoading()
        showToast('操作成功', 'success')
        
      }).catch(function(error) {
        hideLoading()
        showToast(error.message || '操作失败', 'none')
      })
    })
  },

  /**
   * 删除消息
   */
  onDeleteMessage: function(e) {
    var that = this
    var id = e.currentTarget.dataset.id
    
    showModal({
      title: '提示',
      content: '确定要删除该消息吗？'
    }).then(function(confirm) {
      if (!confirm) return
      
      showLoading()
      
      deleteMessage(id).then(function() {
        // 从列表中移除
        var messages = that.data.messages
        var newMessages = []
        for (var i = 0; i < messages.length; i++) {
          if (messages[i].id !== id) {
            newMessages.push(messages[i])
          }
        }
        
        that.setData({ messages: newMessages })
        that.updateUnreadCount()
        
        hideLoading()
        showToast('删除成功', 'success')
        
      }).catch(function(error) {
        hideLoading()
        showToast(error.message || '删除失败', 'none')
      })
    })
  },

  /**
   * 格式化时间
   */
  formatTime: function(time) {
    return formatTime(time)
  },

  /**
   * 重试加载
   */
  onRetry: function() {
    this.loadMessages(true)
  },

  /**
   * 上拉加载更多
   */
  onReachBottom: function() {
    this.loadMessages(false)
  }
})