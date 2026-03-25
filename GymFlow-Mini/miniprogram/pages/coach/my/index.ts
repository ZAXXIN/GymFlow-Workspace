// 教练端我的页面逻辑
import { TabBarHelper } from '../../../utils/tabbar-helper'
import { userStore } from '../../../stores/user.store'
import { messageStore } from '../../../stores/message.store'
import { getMyCoachInfo } from '../../../services/api/coach.api'
import { showModal } from '../../../utils/wx-util'

Page({
  data: {
    selectedTab: 0,
    // 教练信息
    coachInfo: {
      realName: '',
      phone: '',
      specialty: '',
      yearsOfExperience: 0,
      // totalCourses: 0,
      // rating: 5.0,
      certifications: [] as string[],
      introduction: ''
    },

    // 未读消息数
    unreadCount: 0
  },

  onLoad() {
    // 获取当前页面在 TabBar 中的索引
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const pagePath = '/' + currentPage.route
    this.setData({
      selectedTab: TabBarHelper.getSelectedIndex(pagePath)
    })

    this.initData()
  },

  onShow() {
    // 每次显示时刷新数据
    // this.loadCoachInfo()
    // this.updateUnreadCount()
  },

  onTabChange(e: any) {
    const { index } = e.detail
    this.setData({ selectedTab: index })
  },

  /**
   * 初始化数据
   */
  async initData() {
    await this.loadCoachInfo()
    this.updateUnreadCount()
  },

  /**
   * 加载教练信息
   */
  async loadCoachInfo() {
    try {
      const info = await getMyCoachInfo()
      console.log(info)
      // 更新 store
      userStore.updateUserInfo(info)
     
      this.setData({ coachInfo:info })
    } catch (error) {
      console.error('加载教练信息失败:', error)
    }
  },

  /**
   * 更新未读消息数
   */
  updateUnreadCount() {
    this.setData({
      unreadCount: messageStore.unreadCount
    })
  },

  /**
   * 点击菜单项
   */
  onMenuTap(e: any) {
    const { url } = e.currentTarget.dataset

    if (url) {
      wx.navigateTo({
        url
      })
    }
  },

  /**
   * 跳转到消息列表
   */
  goToMessage() {
    wx.navigateTo({
      url: '/pages/common/message-list/index'
    })
  },

  /**
   * 退出登录
   */
  async onLogout() {
    const confirm = await showModal({
      title: '提示',
      content: '确定要退出登录吗？'
    })

    if (!confirm) return

    try {
      const { logout } = await import('../../../hooks/useUser')
      await logout()
    } catch (error) {
      console.error('退出登录失败:', error)
    }
  }
})