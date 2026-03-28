// 会员端我的页面逻辑
import { TabBarHelper } from '../../../utils/tabbar-helper'
import { userStore } from '../../../stores/user.store'
import { messageStore } from '../../../stores/message.store'
import { bookingStore } from '../../../stores/booking.store'
import { orderStore } from '../../../stores/order.store'
import { getMyMemberInfo } from '../../../services/api/member.api'
import { logout as logoutApi } from '../../../services/api/auth.api'
import { showModal, showSuccess, showError } from '../../../utils/wx-util'


Page({
  data: {
    selectedTab: 0,
    // 用户信息
    userInfo: null,

    // 统计数据
    stats: {
      totalCheckins: 0,
      totalCourses: 0,
      totalSpent: 0,
      memberDays: 0
    },

    // 会员卡信息（从my-info接口获取）
    memberCard: null,

    // 未读消息数
    unreadCount: 0,

    // 菜单列表
    menuList: [
      {
        id: 'orders',
        icon: '/assets/icons/order.png',
        title: '我的订单',
        url: '/pages/member/order-list/index'
      },
      {
        id: 'health',
        icon: '/assets/icons/health.png',
        title: '健康档案',
        url: '/pages/member/health-records/index'
      },
      {
        id: 'card',
        icon: '/assets/icons/card.png',
        title: '会员卡',
        url: '/pages/member/card-info/index'
      },
      {
        id: 'messages',
        icon: '/assets/icons/message.png',
        title: '消息中心',
        url: '/pages/common/message-list/index',
        badge: 0
      },
      {
        id: 'password',
        icon: '/assets/icons/lock.png',
        title: '修改密码',
        url: '/pages/common/modify-password/index'
      },
      {
        id: 'about',
        icon: '/assets/icons/about.png',
        title: '关于我们',
        url: '/pages/common/about/index'
      }
    ]
  },

  onLoad: function () {
    // 获取当前页面在 TabBar 中的索引
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const pagePath = '/' + currentPage.route
    this.setData({
      selectedTab: TabBarHelper.getSelectedIndex(pagePath)
    })

    this.initData()
  },

  onShow: function () {
    // 每次显示时只刷新用户信息和未读消息数
    // 不自动加载订单列表
    this.loadUserInfo()
    this.updateUnreadCount()
  },

  onTabChange(e: any) {
    const { index } = e.detail
    this.setData({ selectedTab: index })
  },

  /**
   * 初始化数据
   */
  initData: function () {
    var that = this
    this.setData({
      userInfo: userStore.userInfo
    }, function () {
      that.updateUnreadCount()
      // 从userInfo中获取统计数据
      that.updateStatsFromUserInfo()
      // 从userInfo中获取会员卡信息
      that.updateCardFromUserInfo()
    })
  },

  /**
   * 从userInfo更新统计数据
   */
  updateStatsFromUserInfo: function () {
    var userInfo = this.data.userInfo
    if (!userInfo) return

    // 计算会员天数
    var memberDays = 0
    if (userInfo.membershipStartDate) {
      var start = new Date(userInfo.membershipStartDate)
      var now = new Date()
      memberDays = Math.floor((now.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
    }

    this.setData({
      stats: {
        totalCheckins: userInfo.totalCheckins || 0,
        totalCourses: userInfo.totalCourseHours || 0,
        totalSpent: userInfo.totalSpent || 0,
        memberDays: memberDays
      }
    })
  },

  /**
   * 从userInfo更新会员卡信息
   */
  updateCardFromUserInfo: function () {
    var userInfo = this.data.userInfo
    if (!userInfo || !userInfo.cards || userInfo.cards.length === 0) {
      this.setData({ memberCard: null })
      return
    }

    // 获取当前有效的会员卡
    var now = new Date()
    var activeCard = null

    for (var i = 0; i < userInfo.cards.length; i++) {
      var card = userInfo.cards[i]
      if (card.status === 'ACTIVE') {
        if (!card.endDate || new Date(card.endDate) > now) {
          activeCard = card
          break
        }
      }
    }

    this.setData({ memberCard: activeCard })
  },

  /**
   * 加载用户信息
   */
  loadUserInfo: function () {
    var that = this
    getMyMemberInfo().then(function (memberInfo) {
      userStore.updateUserInfo(memberInfo)
      that.setData({ userInfo: userStore.userInfo }, function () {
        that.updateStatsFromUserInfo()
        that.updateCardFromUserInfo()
      })
    }).catch(function (error) {
      console.error('加载用户信息失败:', error)
    })
  },

  /**
   * 更新未读消息数
   */
  updateUnreadCount: function () {
    var that = this
    var unreadCount = messageStore.unreadCount
    this.setData({ unreadCount: unreadCount })

    // 更新菜单徽标
    var menuList = this.data.menuList.map(function (item) {
      if (item.id === 'messages') {
        return { ...item, badge: unreadCount }
      }
      return item
    })
    this.setData({ menuList: menuList })
  },

  /**
   * 点击菜单项
   */
  onMenuTap: function (e) {
    var item = e.currentTarget.dataset.item
    if (item && item.url) {
      console.log('跳转到:', item.url)
      wx.navigateTo({
        url: item.url
      })
    }
  },

  /**
   * 点击会员卡
   */
  onCardTap: function () {
    wx.navigateTo({
      url: '/pages/member/card-info/index'
    })
  },

  /**
   * 点击健康档案
   */
  onHealthTap: function () {
    wx.navigateTo({
      url: '/pages/member/health-records/index'
    })
  },

  /**
   * 查看全部订单
   */
  onViewAllOrders: function () {
    wx.navigateTo({
      url: '/pages/member/order-list/index'
    })
  },

  /**
   * 退出登录
   */
  async onLogout () {
    try {
      const confirm = await showModal({
        title: '提示',
        content: '确定要退出登录吗？'
      })
      if (!confirm) return

      // 调用登出
      await logoutApi()

      // 清除所有store
      userStore.logout()
      messageStore.reset()
      bookingStore.reset()
      orderStore.reset()

      showSuccess('已退出登录')

      // 跳转到登录页
      setTimeout(() => {
        wx.reLaunch({
          url: '/pages/common/login/index'
        })
      }, 1500)
    } catch (error: any) {
      showError(error.message || '退出登录失败')
    }
  }
})