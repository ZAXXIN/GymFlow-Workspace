// 教练端首页逻辑
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
      totalStudents: 0,
      totalCourses: 0,
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
      const coachInfo = await getMyCoachInfo()
      userStore.setUserInfo(coachInfo)
      
      this.setData({ coachInfo })
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
      this.setData({ scheduleList })
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
   * 点击课程
   */
  onCourseTap(e: any) {
    const { course } = e.currentTarget.dataset
    
    wx.navigateTo({
      url: `/pages/coach/student-list/index?courseId=${course.id}&courseName=${course.courseName}`
    })
  },

  /**
   * 查看学员
   */
  onViewStudents(e: any) {
    const { course } = e.currentTarget.dataset
    
    wx.navigateTo({
      url: `/pages/coach/student-list/index?courseId=${course.id}&courseName=${course.courseName}`
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
   * 获取课程状态样式类
   */
  getCourseStatusClass(course: any): string {
    const now = new Date()
    const courseDate = new Date(`${course.courseDate} ${course.startTime}`)
    const courseEnd = new Date(`${course.courseDate} ${course.endTime}`)
    
    if (now < courseDate) {
      return 'upcoming'
    } else if (now > courseEnd) {
      return 'completed'
    } else {
      return 'ongoing'
    }
  },

  /**
   * 获取课程状态文本
   */
  getCourseStatusText(course: any): string {
    const now = new Date()
    const courseDate = new Date(`${course.courseDate} ${course.startTime}`)
    const courseEnd = new Date(`${course.courseDate} ${course.endTime}`)
    
    if (now < courseDate) {
      return '待上课'
    } else if (now > courseEnd) {
      return '已结束'
    } else {
      return '进行中'
    }
  },

  /**
   * 格式化时间
   */
  formatTime(time: string): string {
    return formatTime(time)
  }
})