// pages/coach/home/index.ts
import { TabBarHelper } from '../../../utils/tabbar-helper'
import { userStore } from '../../../stores/user.store'
import { messageStore } from '../../../stores/message.store'
import { getMyCoachInfo, getMySchedule } from '../../../services/api/coach.api'
import { getCurrentReminder, verifyCodeCheckin } from '../../../services/api/checkin.api'
import { formatDate, isToday, isTomorrow } from '../../../utils/date'
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
    selectedDateDisplay: '',
    minDate: '',
    maxDate: '',

    // 课表列表
    scheduleList: [] as any[],

    // 加载状态
    loading: true,

    // 签到弹窗相关
    showCheckinModal: false,
    checkinCode: '',
    checkinLoading: false,
    checkinMethod: 0 // 0-教练签到，1-前台签到，默认为教练签到
  },

  onLoad() {
    // 获取当前页面在 TabBar 中的索引
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const pagePath = '/' + currentPage.route
    this.setData({
      selectedTab: TabBarHelper.getSelectedIndex(pagePath)
    })

    // 初始化日期选择器
    this.initDatePicker()

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
   * 初始化日期选择器
   */
  initDatePicker() {
    const today = new Date()
    const year = today.getFullYear()
    const month = (today.getMonth() + 1).toString().padStart(2, '0')
    const day = today.getDate().toString().padStart(2, '0')
    const todayStr = `${year}-${month}-${day}`

    const maxDate = new Date(today)
    maxDate.setMonth(maxDate.getMonth() + 3)
    const maxYear = maxDate.getFullYear()
    const maxMonth = (maxDate.getMonth() + 1).toString().padStart(2, '0')
    const maxDay = maxDate.getDate().toString().padStart(2, '0')
    const maxDateStr = `${maxYear}-${maxMonth}-${maxDay}`

    this.setData({
      minDate: todayStr,
      maxDate: maxDateStr,
      selectedDate: todayStr,
      selectedDateDisplay: '今天'
    })
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

      this.setData({ coachInfo: info })
    } catch (error) {
      console.error('加载教练信息失败:', error)
    }
  },

  /**
   * 加载课表
   */
  async loadSchedule() {
    const { selectedDate } = this.data

    if (!selectedDate) {
      console.warn('selectedDate 为空')
      return
    }

    try {
      const scheduleList = await getMySchedule(selectedDate)

      // 预处理，为每个课程添加状态字段
      const processedList = scheduleList.map((course: any) => {
        const now = new Date()

        // 获取当前时间的 iOS 兼容格式
        const nowStr = `${now.getFullYear()}/${now.getMonth() + 1}/${now.getDate()} ${now.getHours()}:${now.getMinutes()}:${now.getSeconds()}`
        const nowIOS = new Date(nowStr)

        // iOS 兼容：将日期格式从 "yyyy-MM-dd HH:mm" 转换为 "yyyy/MM/dd HH:mm"
        const scheduleDateTimeStr = `${course.scheduleDate} ${course.startTime}`.replace(/-/g, '/')
        const courseEndDateTimeStr = `${course.scheduleDate} ${course.endTime}`.replace(/-/g, '/')

        const scheduleDate = new Date(scheduleDateTimeStr)
        const courseEnd = new Date(courseEndDateTimeStr)

        if (nowIOS < scheduleDate) {
          return { ...course, statusClass: 'upcoming', statusText: '待上课' }
        } else if (nowIOS > courseEnd) {
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
    const display = this.getDateDisplay(value)
    this.setData({
      selectedDate: value,
      selectedDateDisplay: display
    }, () => {
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

    this.setData({
      selectedDate: todayStr,
      selectedDateDisplay: '今天'
    }, () => {
      this.loadSchedule()
    })
  },

  /**
   * 获取日期显示文本
   */
  getDateDisplay(dateStr: string): string {
    if (!dateStr) return ''
    if (isToday(dateStr)) {
      return '今天'
    } else if (isTomorrow(dateStr)) {
      return '明天'
    }
    return formatDate(dateStr)
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
   * 打开签到弹窗
   */
  goToCheckIn() {
    console.log('goToCheckIn 被调用，当前 showCheckinModal:', this.data.showCheckinModal)
    this.setData({
      showCheckinModal: true,
      checkinCode: '',
      checkinLoading: false,
      checkinMethod: 0
    }, () => {
      console.log('setData 完成，showCheckinModal:', this.data.showCheckinModal)
    })
  },

  /**
   * 关闭签到弹窗
   */
  onCloseCheckinModal() {
    this.setData({
      showCheckinModal: false,
      checkinCode: ''
    })
  },

  /**
   * 输入签到码
   */
  onCheckinCodeInput(e: any) {
    this.setData({
      checkinCode: e.detail.value
    })
  },

  /**
   * 提交签到
   */
  async onSubmitCheckin() {
    const { checkinCode, checkinMethod } = this.data

    if (!checkinCode || checkinCode.trim() === '') {
      showToast('请输入签到码', 'none')
      return
    }

    if (!/^\d{6}$/.test(checkinCode.trim())) {
      showToast('签到码必须是6位数字', 'none')
      return
    }

    this.setData({ checkinLoading: true })

    try {
      const result = await verifyCodeCheckin({
        digitalCode: checkinCode.trim(),
        checkinMethod: checkinMethod,
        notes: '教练签到'
      })

      if (result && result.code === 200) {
        showToast('签到成功', 'success')
        this.setData({
          showCheckinModal: false,
          checkinCode: ''
        })
        // 刷新课表（可能会有状态变化）
        this.loadSchedule()
        // 刷新提醒
        this.loadCurrentReminder()
      } else {
        showToast(result?.message || '签到失败', 'none')
      }
    } catch (error: any) {
      console.error('签到失败:', error)
      showToast(error?.message || '签到失败，请重试', 'none')
    } finally {
      this.setData({ checkinLoading: false })
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
   * 获取日期标题
   */
  getDateTitle(): string {
    const { selectedDate } = this.data
    if (!selectedDate) return '今日'

    if (isToday(selectedDate)) {
      return '今日'
    } else if (isTomorrow(selectedDate)) {
      return '明日'
    } else {
      return formatDate(selectedDate)
    }
  },
})