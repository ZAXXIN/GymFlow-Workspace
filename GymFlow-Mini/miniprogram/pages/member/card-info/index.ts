// 会员端会员卡详情页面
import { userStore } from '../../../stores/user.store'
import { getMyMemberInfo } from '../../../services/api/member.api'
import { formatDate } from '../../../utils/date'
import { showToast } from '../../../utils/wx-util'

Page({
  data: {
    // 当前卡片索引（用于轮播）
    currentCardIndex: 0,

    // 当前有效卡片列表（用于轮播）
    activeCards: [],

    // 历史卡片
    historyCards: [],

    // 是否显示历史卡片
    showHistory: false,

    // 加载状态
    loading: true,

    // 错误状态
    loadError: false,
    errorMessage: '',

    // 会员信息（用于显示会员姓名、编号等）
    memberInfo: {}
  },

  onLoad: function () {
    this.loadCardsFromRemote()
  },

  onShow: function () {
    // 每次显示页面都重新加载（防止其他页面更新了会员卡状态）
    this.loadCardsFromRemote()
  },

  /**
   * 从远程加载会员卡信息，对比并更新 store
   */
  loadCardsFromRemote: function () {
    var that = this

    this.setData({ loading: true, loadError: false })

    var memberId = userStore.memberId
    if (!memberId) {
      wx.navigateTo({ url: '/pages/common/login/index' })
      return
    }

    // 调用接口获取最新会员信息
    getMyMemberInfo()
      .then(function (response) {
        console.log('获取会员信息成功:', response)

        var remoteMemberInfo = response.data || response
        var remoteCards = remoteMemberInfo.memberCards || remoteMemberInfo.cards || []

        // 直接更新 store
        userStore.updateUserInfo({
          ...remoteMemberInfo,
          memberCards: remoteCards
        })

        // 设置会员信息
        that.setData({
          memberInfo: {
            ...remoteMemberInfo,
            memberCards: remoteCards
          }
        })

        // 渲染页面
        that.renderCards()
      })
      .catch(function (error) {
        console.error('获取会员信息失败:', error)

        // 如果接口调用失败，尝试从 store 读取缓存数据
        var localCards = userStore.cards || []
        var localMemberInfo = userStore.userInfo || {}

        that.setData({
          memberInfo: localMemberInfo
        })

        if (localCards.length > 0) {
          // 有缓存数据，使用缓存
          console.log('接口失败，使用缓存数据')
          that.renderCards()
          that.setData({ loadError: false })
        } else {
          // 无缓存数据，显示错误
          that.setData({
            loading: false,
            loadError: true,
            errorMessage: error?.message || '加载失败，请重试'
          })
        }
      })
  },

  /**
   * 对比两个卡片列表是否有差异
   * @param localCards 本地卡片列表
   * @param remoteCards 远程卡片列表
   * @returns true-有变化，false-无变化
   */
  compareCards: function (localCards, remoteCards) {
    // 数量不同，有变化
    if (localCards.length !== remoteCards.length) {
      return true
    }

    // 创建映射便于对比
    var localMap = {}
    var remoteMap = {}

    for (var i = 0; i < localCards.length; i++) {
      var card = localCards[i]
      // 用 productId 作为唯一标识
      localMap[card.productId] = card
    }

    for (var j = 0; j < remoteCards.length; j++) {
      var card = remoteCards[j]
      remoteMap[card.productId] = card
    }

    // 检查是否有新增或删除的卡片
    for (var productId in remoteMap) {
      if (!localMap[productId]) {
        return true // 新增卡片
      }
    }

    for (var productId in localMap) {
      if (!remoteMap[productId]) {
        return true // 删除卡片
      }
    }

    // 检查每个卡片的关键字段是否有变化
    for (var productId in remoteMap) {
      var localCard = localMap[productId]
      var remoteCard = remoteMap[productId]

      if (localCard.status !== remoteCard.status ||
        localCard.endDate !== remoteCard.endDate ||
        localCard.remainingSessions !== remoteCard.remainingSessions) {
        return true // 状态、有效期或剩余课时有变化
      }
    }

    return false // 无变化
  },

/**
 * 渲染卡片（从 store 读取数据）
 */
renderCards: function () {
  var cards = userStore._userInfo.memberCards || []

  console.log('渲染卡片，store数据:', cards)

  // 分离有效卡片和历史卡片
  var activeCards = []
  var historyCards = []
  var now = new Date()

  for (var i = 0; i < cards.length; i++) {
    var card = cards[i]
    var endDate = card.endDate ? new Date(card.endDate) : null
    var isActive = false

    // 根据卡片类型判断有效性
    if (card.cardType === 0) {
      // 会籍卡：只判断状态和有效期，不判断剩余课时
      isActive = card.status == 'ACTIVE' && (!endDate || endDate > now)
    } else {
      // 课程卡（私教课、团课）：判断状态、有效期和剩余课时
      isActive = card.status == 'ACTIVE' &&
        (!endDate || endDate > now) &&
        (card.remainingSessions !== undefined && card.remainingSessions > 0)
    }

    if (isActive) {
      activeCards.push(card)
    } else {
      historyCards.push(card)
    }
  }

  console.log('有效卡片:', activeCards)
  console.log('历史卡片:', historyCards)

  this.setData({
    activeCards: activeCards,
    historyCards: historyCards,
    loading: false,
    loadError: false,
    currentCardIndex: 0
  })
},

  /**
   * 卡片切换事件
   */
  onCardChange: function (e) {
    var index = e.detail.current
    this.setData({
      currentCardIndex: index
    })
  },

  /**
   * 点击指示点切换卡片
   */
  onDotTap: function (e) {
    var index = e.currentTarget.dataset.index
    this.setData({
      currentCardIndex: index
    })
  },

  /**
   * 根据卡片类型获取渐变背景
   */
  getCardGradient: function (cardType) {
    console.group(cardType)
    var gradients = {
      0: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',   // 会籍卡
      1: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',   // 私教课
      2: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',   // 团课
      3: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'    // 相关产品
    }
    return gradients[cardType] || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },

  /**
   * 重试加载
   */
  onRetry: function () {
    this.loadCardsFromRemote()
  },

  /**
   * 切换历史卡片显示
   */
  toggleHistory: function () {
    this.setData({
      showHistory: !this.data.showHistory
    })
  },

  /**
   * 点击教练
   */
  onCoachTap: function () {
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

  // /**
  //  * 续费会员卡（续费当前显示的卡片）
  //  */
  // onRenewCard: function () {
  //   var activeCards = this.data.activeCards
  //   var currentIndex = this.data.currentCardIndex
  //   var activeCard = activeCards.length > 0 ? activeCards[currentIndex] : null

  //   if (!activeCard) {
  //     showToast('暂无有效会员卡', 'none')
  //     return
  //   }

  //   // 跳转到商品详情页进行续费
  //   wx.navigateTo({
  //     url: '/pages/common/product-detail/index?id=' + activeCard.productId + '&action=renew'
  //   })
  // },

  /**
   * 购买新卡
   */
  onBuyCard: function () {
    // 跳转到商品列表页
    wx.switchTab({
      url: '/pages/member/home/index'
    })
  },

  /**
   * 获取卡片状态样式类
   */
  getCardStatusClass: function (status) {
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
  getCardStatusText: function (status) {
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
  formatDate: function (date) {
    return formatDate(date)
  },

  /**
   * 格式化金额
   */
  formatMoney: function (amount) {
    if (amount == undefined || amount == null) return '¥0.00'
    return '¥' + amount.toFixed(2)
  },

  /**
   * 格式化剩余课时
   */
  formatRemainingSessions: function (card) {
    if (card.cardType == 0) {
      // 会籍卡
      return '不限次数'
    }
    if (card.remainingSessions == undefined) {
      return '--'
    }
    return '剩余 ' + card.remainingSessions + ' 课时'
  }
})