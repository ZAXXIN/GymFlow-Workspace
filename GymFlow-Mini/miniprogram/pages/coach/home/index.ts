// pages/coach/home/index.ts
import { TabBarHelper } from '../../../utils/tabbar-helper'
import { userStore } from '../../../stores/user.store'
import { messageStore } from '../../../stores/message.store'
import { getMyCoachInfo, getMySchedule } from '../../../services/api/coach.api'
import { getCurrentReminder } from '../../../services/api/checkin.api'
import { formatDate, formatTime, isToday, isTomorrow } from '../../../utils/date'
import { showToast } from '../../../utils/wx-util'

Page({
  data: {
    selectedTab: 0,
    // 教练信息
    coachInfo: {
      realName: '',
      specialty: '',
      yearsOfExperience: 0
    },
    
    // 未读消息数
    unreadCount: 0,
    
    // 当前提醒
    currentReminder: null as any,
    
    // 选中日期
    selectedDate: '',
    
    // 课表列表
    scheduleList: [] as any[],
    
    // 加载状态
    loading: true
  },

  onLoad() {
    // 获取当前页面在 TabBar 中的索引
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const pagePath = '/' + currentPage.route
    this.setData({
      selectedTab: TabBarHelper.getSelectedIndex(pagePath)
    })

    // 设置默认日期为今天
    const today = new Date()
    const year = today.getFullYear()
    const month = (today.getMonth() + 1).toString().padStart(2, '0')
    const day = today.getDate().toString().padStart(2, '0')
    
    this.setData({
      selectedDate: `${year}-${month}-${day}`
    })
    
    this.initData()
  },

  onShow() {
    // 刷新未读消息数
    this.updateUnreadCount()
    
    // 刷新当前提醒
    this.loadCurrentReminder()
  },

  onPullDownRefresh() {
    this.refreshData()
  },

  onTabChange(e: any) {
    const { index } = e.detail
    this.setData({ selectedTab: index })
  },

  /**
   * 初始化数据
   */
  async initData() {
    this.setData({ loading: true })
    
    try {
      await Promise.all([
        this.loadCoachInfo(),
        this.loadSchedule(),
        this.loadCurrentReminder()
      ])
      
      this.updateUnreadCount()
    } catch (error) {
      console.error('加载数据失败:', error)
    } finally {
      this.setData({ loading: false })
      wx.stopPullDownRefresh()
    }
  },

  /**
   * 刷新数据
   */
  async refreshData() {
    this.setData({ loading: true })
    
    try {
      await Promise.all([
        this.loadCoachInfo(),
        this.loadSchedule(),
        this.loadCurrentReminder()
      ])
      
      this.updateUnreadCount()
    } catch (error) {
      console.error('刷新数据失败:', error)
    } finally {
      this.setData({ loading: false })
      wx.stopPullDownRefresh()
    }
  },

  /**
   * 加载教练信息
   */
  async loadCoachInfo() {
    try {
      const info = await getMyCoachInfo()
      // 更新 store
      userStore.updateUserInfo(info)
     
      this.setData({ coachInfo:info })
    } catch (error) {
      console.error('加载教练信息失败:', error)
    }
  },

  /**
   * 加载课表
   */
  async loadSchedule() {
    const { selectedDate } = this.data
    
    try {
      const scheduleList = await getMySchedule(selectedDate)
      
      // 预处理，为每个课程添加状态字段
      const processedList = scheduleList.map((course: any) => {
        const now = new Date()
        
        const scheduleDateTimeStr = `${course.scheduleDate} ${course.startTime}`.replace(/-/g, '/')
        const courseEndDateTimeStr = `${course.scheduleDate} ${course.endTime}`.replace(/-/g, '/')
        
        const scheduleDate = new Date(scheduleDateTimeStr)
        const courseEnd = new Date(courseEndDateTimeStr)
        
        if (now < scheduleDate) {
          return { ...course, statusClass: 'upcoming', statusText: '待上课' }
        } else if (now > courseEnd) {
          return { ...course, statusClass: 'completed', statusText: '已结束' }
        } else {
          return { ...course, statusClass: 'ongoing', statusText: '进行中' }
        }
      })
      
      this.setData({ scheduleList: processedList })
    } catch (error) {
      console.error('加载课表失败:', error)
    }
  },

  /**
   * 加载当前提醒
   */
  async loadCurrentReminder() {
    try {
      const reminder = await getCurrentReminder()
      this.setData({ currentReminder: reminder?.coachReminder || null })
    } catch (error) {
      console.error('加载提醒失败:', error)
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
   * 日期变化
   */
  onDateChange(e: any) {
    const { value } = e.detail
    this.setData({ selectedDate: value }, () => {
      this.loadSchedule()
    })
  },

  /**
   * 点击今天
   */
  onTodayTap() {
    const today = new Date()
    const year = today.getFullYear()
    const month = (today.getMonth() + 1).toString().padStart(2, '0')
    const day = today.getDate().toString().padStart(2, '0')
    const todayStr = `${year}-${month}-${day}`
    
    this.setData({ selectedDate: todayStr }, () => {
      this.loadSchedule()
    })
  },

  /**
   * 点击提醒
   */
  onReminderTap() {
    const { currentReminder } = this.data
    
    if (currentReminder?.memberId) {
      wx.navigateTo({
        url: `/pages/coach/member-detail/index?memberId=${currentReminder.memberId}`
      })
    }
  },

/**
 * 查看学员
 */
onViewStudents(e: any) {
  const { course } = e.currentTarget.dataset
  
  wx.navigateTo({
    url: `/pages/coach/student-list/index?scheduleId=${course.scheduleId}&courseName=${encodeURIComponent(course.courseName)}&courseType=${course.courseType}&scheduleDate=${course.scheduleDate}&startTime=${course.startTime}&endTime=${course.endTime}`
  })
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
   * 获取日期标题
   */
  getDateTitle(): string {
    const { selectedDate } = this.data
    
    if (isToday(selectedDate)) {
      return '今日'
    } else if (isTomorrow(selectedDate)) {
      return '明日'
    } else {
      return formatDate(selectedDate)
    }
  },

  /**
   * 格式化时间
   */
  formatTime(time: string): string {
    return formatTime(time)
  }
})