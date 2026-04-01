// pages/member/card-detail/index.ts
import { userStore } from '../../../stores/user.store'
import { memberStatisticsStore } from '../../../stores/memberStatistics.store'

Page({
  data: {
    currentCard: null as any,
    memberInfo: {} as any,
    orderItemId: null as number | null,
    statistics: {
      totalCheckins: 0,
      recentCheckins: [],
      totalConsumed: 0,
      totalReturned: 0,
      sessionDetails: []
    },
    loading: true
  },

  async onLoad(options: any) {
    console.log('card-detail onLoad, options:', options)
    
    // 获取 orderItemId
    const orderItemId = options.orderItemId ? parseInt(options.orderItemId) : null
    if (!orderItemId) {
      console.error('缺少 orderItemId 参数')
      wx.showToast({ title: '参数错误', icon: 'none' })
      return
    }
    
    this.setData({ orderItemId })
    
    // 获取卡片数据（优先使用传入的 card，否则从 store 查找）
    if (options.card) {
      try {
        const card = JSON.parse(decodeURIComponent(options.card))
        this.setData({ currentCard: card })
        console.log('从参数获取卡片数据:', card)
      } catch (e) {
        console.error('解析卡片数据失败:', e)
        // 如果解析失败，从 store 查找
        this.findCardFromStore(orderItemId)
      }
    } else {
      // 没有传 card，从 store 查找
      this.findCardFromStore(orderItemId)
    }
    
    // 获取会员信息
    this.setData({
      memberInfo: userStore.userInfo || {}
    })
    
    // 加载统计数据
    await this.loadStatistics()
  },

  /**
   * 从 store 查找卡片
   */
  findCardFromStore(orderItemId: number) {
    const cards = userStore.cards || []
    const card = cards.find((c: any) => c.orderItemId === orderItemId)
    if (card) {
      this.setData({ currentCard: card })
      console.log('从 store 获取卡片数据:', card)
    } else {
      console.error('未找到对应卡片, orderItemId:', orderItemId)
    }
  },

  /**
   * 加载统计数据
   */
  async loadStatistics() {
    const { orderItemId } = this.data
    if (!orderItemId) return
    
    console.log('开始加载统计数据, orderItemId:', orderItemId)
    this.setData({ loading: true })

    try {
      // 调用 store 加载数据，传入 orderItemId
      await memberStatisticsStore.loadStatistics(true, orderItemId)

      // 格式化时间
      const formattedCheckins = this.formatTimes(memberStatisticsStore.recentCheckins, 'checkinTime')
      const formattedSessions = this.formatTimes(memberStatisticsStore.sessionDetails, 'time')

      this.setData({
        statistics: {
          totalCheckins: memberStatisticsStore.totalCheckins,
          recentCheckins: formattedCheckins,
          totalConsumed: memberStatisticsStore.totalConsumed,
          totalReturned: memberStatisticsStore.totalReturned,
          sessionDetails: formattedSessions
        },
        loading: false
      })
      
      console.log('统计数据设置完成:', this.data.statistics)
    } catch (error) {
      console.error('加载统计数据失败:', error)
      this.setData({ loading: false })
    }
  },

  /**
   * 格式化列表中的时间字段
   */
  formatTimes(list: any[], timeField: string) {
    if (!list || !Array.isArray(list)) return []
    return list.map(item => ({
      ...item,
      [timeField]: this.formatDate(item[timeField])
    }))
  },

  /**
   * 格式化日期时间
   */
  formatDate(dateStr: string) {
    if (!dateStr) return ''
    try {
      const date = new Date(dateStr)
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      const hour = date.getHours().toString().padStart(2, '0')
      const minute = date.getMinutes().toString().padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}`
    } catch (e) {
      return dateStr
    }
  },

  /**
   * 下拉刷新
   */
  async onPullDownRefresh() {
    await this.loadStatistics()
    wx.stopPullDownRefresh()
  },

  onUnload() {
    memberStatisticsStore.reset()
  }
})