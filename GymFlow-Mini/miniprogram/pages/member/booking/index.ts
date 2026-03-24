// pages/member/booking/index.ts
// 会员端预约页面逻辑

import { TabBarHelper } from '../../../utils/tabbar-helper'
import { userStore } from '../../../stores/user.store'
import { bookingStore, MEMBER_COURSE_TYPE, COURSE_TYPE } from '../../../stores/booking.store'
import { formatTime, formatDate } from '../../../utils/date'
import { showToast, showModal, showLoading, hideLoading } from '../../../utils/wx-util'
import { configStore } from '../../../stores/config.store'

Page({
  data: {
    selectedTab: 0,

    // 主Tab 0-可预约 1-我的预约
    activeTab: 0,
    tabName: ['可预约', '我的预约'],

    // ========== 可预约课程相关 ==========
    courseTypeOptions: [
      { label: '私教课', value: 0 },
      { label: '团课', value: 1 }
    ],
    selectedCourseTypeIndex: 0,
    showCourseTypeSelect: false,

    // 时间选择器
    selectedDate: '',
    selectedDateDisplay: '',
    minDate: '',
    maxDate: '',

    // 可预约课程列表
    displayCourses: [] as any[],
    availableLoading: false,

    // ========== 我的预约相关 ==========
    myBookings: [] as any[],
    myLoading: false,
    myHasMore: true,
    bookingStatus: -1,

    statusTabs: [
      { label: '全部', value: -1 },
      { label: '待上课', value: 0 },
      { label: '已签到', value: 1 },
      { label: '已完成', value: 2 },
      { label: '已取消', value: 3 },
      { label: '已过期', value: 4 }
    ],

    // ========== 预约弹窗 ==========
    showBookingModal: false,
    selectedCourse: null as any,
    bookingLoading: false,

    // ========== 签到码弹窗 ==========
    showCheckinModal: false,
    checkinBooking: null as any,  // 直接存储预约数据
    checkinLoading: false,
    showCancelBtn: false
  },

  onLoad() {
    this.updateSelectedTab()
    this.initDatePicker()
    this.initData()
  },

  onShow() {
    if (this.data.activeTab === 0) {
      this.loadAvailableCourses()
    } else {
      this.loadMyBookings(true)
    }
  },

  updateSelectedTab() {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const pagePath = '/' + currentPage.route
    this.setData({
      selectedTab: TabBarHelper.getSelectedIndex(pagePath)
    })
  },

  onTabChange(e: any) {
    const index = e.detail.index
    this.setData({ selectedTab: index })
  },

  initDatePicker() {
    const today = new Date()
    const year = today.getFullYear()
    const month = String(today.getMonth() + 1).padStart(2, '0')
    const day = String(today.getDate()).padStart(2, '0')
    const todayStr = `${year}-${month}-${day}`

    const maxDate = new Date(today)
    maxDate.setMonth(maxDate.getMonth() + 3)
    const maxYear = maxDate.getFullYear()
    const maxMonth = String(maxDate.getMonth() + 1).padStart(2, '0')
    const maxDay = String(maxDate.getDate()).padStart(2, '0')
    const maxDateStr = `${maxYear}-${maxMonth}-${maxDay}`

    this.setData({
      selectedDate: todayStr,
      selectedDateDisplay: '今天',
      minDate: todayStr,
      maxDate: maxDateStr
    })
  },

  async initData() {
    await bookingStore.initMemberCourseType()

    const showSelect = bookingStore.currentCourseType === 'BOTH'
    this.setData({ showCourseTypeSelect: showSelect })

    if (bookingStore.currentCourseType === 'PRIVATE_ONLY') {
      this.setData({ selectedCourseTypeIndex: 0 })
    } else if (bookingStore.currentCourseType === 'GROUP_ONLY') {
      this.setData({ selectedCourseTypeIndex: 1 })
    }

    this.loadAvailableCourses()
  },

  onMainTabChange(e: any) {
    const index = e.detail.index
    this.setData({ activeTab: index }, () => {
      if (index === 0) {
        this.loadAvailableCourses()
      } else {
        this.loadMyBookings(true)
      }
    })
  },

  // ========== 可预约课程相关 ==========

  onCourseTypeChange(e: any) {
    const index = e.detail.value
    this.setData({ selectedCourseTypeIndex: index }, () => {
      this.loadAvailableCourses()
    })
  },

  onDateChange(e: any) {
    const date = e.detail.value
    const display = this.getDateDisplay(date)

    this.setData({
      selectedDate: date,
      selectedDateDisplay: display
    }, () => {
      this.loadAvailableCourses()
    })
  },

  getDateDisplay(dateStr: string): string {
    const today = new Date()
    const todayStr = formatDate(today, 'YYYY-MM-DD')
    const tomorrow = new Date(today)
    tomorrow.setDate(tomorrow.getDate() + 1)
    const tomorrowStr = formatDate(tomorrow, 'YYYY-MM-DD')

    if (dateStr === todayStr) {
      return '今天'
    } else if (dateStr === tomorrowStr) {
      return '明天'
    }
    return formatDate(dateStr, 'MM月DD日')
  },

  async loadAvailableCourses() {
    if (this.data.availableLoading) return

    this.setData({ availableLoading: true })

    try {
      const { selectedDate, selectedCourseTypeIndex, courseTypeOptions, showCourseTypeSelect } = this.data

      let targetCourseType: number | undefined = undefined
      if (!showCourseTypeSelect) {
        const currentType = bookingStore.currentCourseType
        if (currentType === 'PRIVATE_ONLY') {
          targetCourseType = 0
        } else if (currentType === 'GROUP_ONLY') {
          targetCourseType = 1
        }
      } else {
        targetCourseType = courseTypeOptions[selectedCourseTypeIndex].value
      }

      await bookingStore.loadAvailableCourses({
        date: selectedDate,
        courseType: targetCourseType
      })

      let displayCourses: any[] = []
      if (targetCourseType === 0 || bookingStore.currentCourseType === 'PRIVATE_ONLY') {
        displayCourses = bookingStore.availablePrivateCourses
      } else if (targetCourseType === 1 || bookingStore.currentCourseType === 'GROUP_ONLY') {
        displayCourses = bookingStore.availableGroupCourses
      } else {
        displayCourses = [...bookingStore.availablePrivateCourses, ...bookingStore.availableGroupCourses]
      }

      this.setData({
        displayCourses,
        availableLoading: false
      })
    } catch (error: any) {
      console.error('加载可预约课程失败:', error)
      showToast(error.message || '加载失败', 'none')
      this.setData({ availableLoading: false })
    }
  },

  onCourseTap(e: any) {
    const course = e.currentTarget.dataset.course
    if (!course.canBook) {
      showToast('课程已满员', 'none')
      return
    }

    this.setData({
      selectedCourse: course,
      showBookingModal: true
    })
  },

  onCloseBookingModal() {
    this.setData({
      showBookingModal: false,
      selectedCourse: null
    })
  },

  async onConfirmBooking() {
    const { selectedCourse } = this.data
    const memberId = userStore.memberId

    if (!memberId) {
      showToast('请先登录', 'none')
      return
    }

    if (selectedCourse.currentEnrollment >= selectedCourse.maxCapacity) {
      showToast('课程已满员', 'none')
      this.onCloseBookingModal()
      return
    }

    this.setData({ bookingLoading: true })

    try {
      const result = await bookingStore.createBooking(memberId, selectedCourse)

      showToast('预约成功', 'success')
      this.onCloseBookingModal()

      this.loadAvailableCourses()
      this.loadMyBookings(true)

    } catch (error: any) {
      showToast(error.message || '预约失败', 'none')
    } finally {
      this.setData({ bookingLoading: false })
    }
  },

  // ========== 我的预约相关 ==========

  async loadMyBookings(refresh: boolean) {
    if (this.data.myLoading) return
    if (!refresh && !this.data.myHasMore) return
  
    this.setData({ myLoading: true })
  
    try {
      // 全部时传 null，store 中会判断不添加参数
      const status = this.data.bookingStatus === -1 ? null : this.data.bookingStatus
      console.log('调用 getMemberBookings，status:', status)
      
      const result = await bookingStore.loadMyBookings(refresh, status)  // 直接传 status
  
      this.setData({
        myBookings: bookingStore.myBookings,
        myHasMore: bookingStore.hasMore,
        myLoading: false
      })
    } catch (error: any) {
      console.error('加载我的预约失败:', error)
      showToast(error.message || '加载失败', 'none')
      this.setData({ myLoading: false })
    }
  },

  onStatusTabTap(e: any) {
    const value = parseInt(e.currentTarget.dataset.value)
    this.setData({
      bookingStatus: value,
      myHasMore: true
    }, () => {
      this.loadMyBookings(true)
    })
  },

  onBookingTap(e: any) {
    const booking = e.detail?.booking

    if (!booking || !booking.bookingId) {
      console.error('无法获取预约信息', e.detail)
      showToast('数据错误', 'none')
      return
    }

    wx.navigateTo({
      url: `/pages/common/booking-detail/index?id=${booking.bookingId}`
    })
  },

  async onCancelBooking(e: any) {
    const booking = e.detail?.booking

    if (!booking || !booking.bookingId) {
      console.error('无法获取预约信息', e.detail)
      showToast('数据错误', 'none')
      return
    }

    const confirm = await showModal({
      title: '提示',
      content: '确定要取消该预约吗？'
    })

    if (!confirm) return

    showLoading('取消中...')

    try {
      await bookingStore.cancelBooking(booking.bookingId)

      hideLoading()
      showToast('取消成功', 'success')

      this.loadMyBookings(true)
      this.loadAvailableCourses()

    } catch (error: any) {
      hideLoading()
      showToast(error.message || '取消失败', 'none')
    }
  },

  /**
   * 查看签到码 - 打开弹窗，直接使用预约数据
   */
  onViewCheckinCode(e: any) {
    const booking = e.detail?.booking

    if (!booking || !booking.bookingId) {
      console.error('无法获取预约信息', e.detail)
      showToast('数据错误', 'none')
      return
    }

    // 检查是否有签到码
    if (!booking.signCode) {
      showToast('签到码未生成', 'none')
      return
    }

    // 打印二维码内容
    const qrContent = `gymflow://checkin?bookingId=${booking.bookingId}&code=${booking.signCode}`
    console.log('二维码内容:', qrContent)

    // 直接使用预约数据打开弹窗
    this.setData({
      showCheckinModal: true,
      checkinBooking: booking,
      checkinLoading: false,
      showCancelBtn: this.checkCanCancel(booking.scheduleDate, booking.startTime)
    })
    console.log(this.data.showCheckinModal)
  },

  /**
 * 检查是否可取消
 */
  checkCanCancel(courseDate: string, startTime: string): boolean {
    if (!courseDate || !startTime) return false

    const cancelHours = configStore.courseCancelHours || 2

    const now = new Date()

    // 将 "2026-03-24" 和 "14:00:00" 转换为 iOS 支持的格式
    // 使用 "2026/03/24 14:00:00" 格式，iOS 支持
    const dateStr = courseDate.replace(/-/g, '/')
    const courseStart = new Date(`${dateStr} ${startTime}`)

    // 检查日期是否有效
    if (isNaN(courseStart.getTime())) {
      console.error('无效的日期格式', courseDate, startTime)
      return false
    }

    const hoursUntilCourse = (courseStart.getTime() - now.getTime()) / (1000 * 60 * 60)

    return hoursUntilCourse >= cancelHours
  },

  /**
   * 格式化过期时间
   */
  formatExpireTime(expireTime: string): string {
    if (!expireTime) return ''

    // expireTime 格式: "2026-03-24T14:15:00" 这是 iOS 支持的格式
    const date = new Date(expireTime)

    // 检查日期是否有效
    if (isNaN(date.getTime())) {
      console.error('无效的过期时间格式', expireTime)
      return ''
    }

    const hour = date.getHours().toString().padStart(2, '0')
    const minute = date.getMinutes().toString().padStart(2, '0')

    return `${hour}:${minute}`
  },

  /**
   * 格式化过期时间
   */
  formatExpireTime(expireTime: string): string {
    if (!expireTime) return ''

    const date = new Date(expireTime)
    const hour = date.getHours().toString().padStart(2, '0')
    const minute = date.getMinutes().toString().padStart(2, '0')

    return `${hour}:${minute}`
  },

  /**
   * 关闭签到码弹窗
   */
  onCloseCheckinModal() {
    this.setData({
      showCheckinModal: false,
      checkinBooking: null,
      showCancelBtn: false
    })
  },

  /**
   * 弹窗中的取消预约
   */
  async onCheckinCancelBooking() {
    const { checkinBooking } = this.data

    if (!checkinBooking || !checkinBooking.bookingId) {
      showToast('数据错误', 'none')
      return
    }

    const confirm = await showModal({
      title: '提示',
      content: '确定要取消该预约吗？'
    })

    if (!confirm) return

    showLoading('取消中...')

    try {
      await bookingStore.cancelBooking(checkinBooking.bookingId)

      hideLoading()
      showToast('取消成功', 'success')

      this.onCloseCheckinModal()

      // 刷新列表
      this.loadMyBookings(true)
      this.loadAvailableCourses()

    } catch (error: any) {
      hideLoading()
      showToast(error.message || '取消失败', 'none')
    }
  },

  /**
   * 获取剩余名额显示文本
   */
  getRemainingText(remaining: number): string {
    if (remaining <= 0) return '已满'
    if (remaining <= 5) return `仅剩${remaining}席`
    return `${remaining}席`
  },

  formatTime(time: string): string {
    return formatTime(time)
  },

  onPullDownRefresh() {
    if (this.data.activeTab === 0) {
      this.loadAvailableCourses()
    } else {
      this.loadMyBookings(true)
    }
    wx.stopPullDownRefresh()
  },

  onReachBottom() {
    if (this.data.activeTab === 1 && !this.data.myLoading && this.data.myHasMore) {
      this.loadMyBookings(false)
    }
  }
})