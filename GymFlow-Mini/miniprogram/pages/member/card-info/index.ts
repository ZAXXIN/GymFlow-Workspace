// 会员端会员卡详情页面
import { userStore } from '../../../stores/user.store'
import { formatDate } from '../../../utils/date'
import { showToast } from '../../../utils/wx-util'

Page({
  data: {
    // 当前有效卡片
    activeCard: null,
    
    // 历史卡片
    historyCards: [],
    
    // 是否显示历史卡片
    showHistory: false,
    
    // 加载状态
    loading: true,
    
    // 错误状态
    loadError: false,
    errorMessage: ''
  },

  onLoad: function() {
    this.loadCards()
  },

  onShow: function() {
    this.loadCards()
  },

  /**
   * 加载会员卡信息（从 userStore 获取）
   */
  loadCards: function() {
    this.setData({ loading: true, loadError: false })
    
    var memberId = userStore.memberId
    if (!memberId) {
      wx.navigateTo({ url: '/pages/common/login/index' })
      return
    }
    
    // 从 userStore 获取会员卡信息
    var userInfo = userStore.userInfo
    var cards = userInfo?.memberCards || []
    
    console.log('会员卡从userInfo获取:', cards)
    
    // 分离有效卡片和历史卡片
    var activeCards = []
    var historyCards = []
    var now = new Date()
    
    for (var i = 0; i < cards.length; i++) {
      var card = cards[i]
      var endDate = card.endDate ? new Date(card.endDate) : null
      
      // 判断是否为有效卡片
      var isActive = card.status === 'ACTIVE' && 
                      (!endDate || endDate > now) &&
                      (card.remainingSessions === undefined || card.remainingSessions > 0)
      
      if (isActive) {
        activeCards.push(card)
      } else {
        historyCards.push(card)
      }
    }
    
    // 取第一个有效卡片作为当前卡片
    var activeCard = activeCards.length > 0 ? activeCards[0] : null
    
    this.setData({
      activeCard: activeCard,
      historyCards: historyCards,
      loading: false,
      loadError: false
    })
  },

  /**
   * 重试加载
   */
  onRetry: function() {
    this.loadCards()
  },

  /**
   * 切换历史卡片显示
   */
  toggleHistory: function() {
    this.setData({
      showHistory: !this.data.showHistory
    })
  },

  /**
   * 点击教练
   */
  onCoachTap: function() {
    var personalCoachName = userStore.personalCoachName
    var personalCoachId = userStore.personalCoachId
    
    if (!personalCoachId) {
      showToast('暂无专属教练', 'none')
      return
    }
    
    // 跳转到教练详情
    wx.navigateTo({
      url: '/pages/coach/detail/index?id=' + personalCoachId
    })
  },

  /**
   * 续费会员卡
   */
  onRenewCard: function() {
    var activeCard = this.data.activeCard
    
    if (!activeCard) {
      showToast('暂无有效会员卡', 'none')
      return
    }
    
    // 跳转到商品详情页进行续费
    wx.navigateTo({
      url: '/pages/common/product-detail/index?id=' + activeCard.productId + '&action=renew'
    })
  },

  /**
   * 购买新卡
   */
  onBuyCard: function() {
    // 跳转到商品列表页
    wx.switchTab({
      url: '/pages/member/home/index'
    })
  },

  /**
   * 获取卡片状态样式类
   */
  getCardStatusClass: function(status) {
    var map = {
      'ACTIVE': 'card-info-card-status-active',
      'EXPIRED': 'card-info-card-status-expired',
      'USED_UP': 'card-info-card-status-used-up',
      'UNPAID': 'card-info-card-status-expired'
    }
    return map[status] || 'card-info-card-status-expired'
  },

  /**
   * 获取卡片状态文本
   */
  getCardStatusText: function(status) {
    var map = {
      'ACTIVE': '有效',
      'EXPIRED': '已过期',
      'USED_UP': '已用完',
      'UNPAID': '未支付'
    }
    return map[status] || '未知'
  },

  /**
   * 格式化日期
   */
  formatDate: function(date) {
    return formatDate(date)
  },

  /**
   * 格式化金额
   */
  formatMoney: function(amount) {
    if (amount === undefined || amount === null) return '¥0.00'
    return '¥' + amount.toFixed(2)
  },

  /**
   * 格式化剩余课时
   */
  formatRemainingSessions: function(card) {
    if (card.cardType === 0) {
      // 会籍卡
      return '不限次数'
    }
    if (card.remainingSessions === undefined) {
      return '--'
    }
    return '剩余 ' + card.remainingSessions + ' 课时'
  }
})