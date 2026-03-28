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

    // 团课时间选择器
    selectedDate: '',
    selectedDateDisplay: '',
    minDate: '',
    maxDate: '',

    // 私教课教练选择器
    privateCoachList: [] as any[],
    selectedCoachId: null as number | null,
    selectedCoachName: '',

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

    // ========== 团课预约弹窗 ==========
    showBookingModal: false,
    selectedCourse: null as any,
    bookingLoading: false,

    // ========== 私教课预约弹窗 ==========
    showPrivateBookingModal: false,
    selectedCourseDuration: 60,
    privateScheduleDate: '',
    privateStartTime: '',
    privateEndTime: '',
    privateMinDate: '',
    privateMaxDate: '',
    privateMinStartTime: '',  // 最小可选开始时间
    privateMaxStartTime: '',  // 最大可选开始时间
    timeOptions: [] as string[],
    privateBookingLoading: false,

    // ========== 签到码弹窗 ==========
    showCheckinModal: false,
    checkinBooking: null as any,
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
    console.log('initData 开始')

    // 先加载会员卡信息，确定课程类型
    await bookingStore.initMemberCourseType()
    console.log('会员卡类型:', bookingStore.currentCourseType)

    // 显示/隐藏下拉框（两种卡都有时才显示）
    const showSelect = bookingStore.currentCourseType === 'BOTH'
    this.setData({ showCourseTypeSelect: showSelect })

    // 设置默认选中的类型
    if (bookingStore.currentCourseType === 'PRIVATE_ONLY') {
      this.setData({ selectedCourseTypeIndex: 0 })
    } else if (bookingStore.currentCourseType === 'GROUP_ONLY') {
      this.setData({ selectedCourseTypeIndex: 1 })
    }

    // 初始加载数据（使用 loadAvailableCourses）
    await this.loadAvailableCourses()
    console.log('loadAvailableCourses 完成')

    // 更新显示
    this.updateDisplayCourses()
  },

  /**
   * 根据当前选择更新显示课程列表
   */
  updateDisplayCourses() {
    const { selectedCourseTypeIndex, courseTypeOptions, showCourseTypeSelect, selectedDate, selectedCoachId } = this.data

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

    // 直接从 store 获取已加载的数据
    let allPrivateCourses = bookingStore.availablePrivateCourses || []
    let allGroupCourses = bookingStore.availableGroupCourses || []

    // 私教课：如果选择了具体教练才筛选
    let filteredPrivate = allPrivateCourses
    if (targetCourseType === 0 && selectedCoachId !== null && selectedCoachId !== undefined) {
      filteredPrivate = allPrivateCourses.filter(c => c.coachId === selectedCoachId)
    }

    // 团课：按日期筛选（但数据已经是按日期请求的，不需要再过滤）
    let filteredGroup = allGroupCourses

    let displayCourses: any[] = []
    if (targetCourseType === 0) {
      displayCourses = filteredPrivate
      // 提取教练列表（用于筛选器）
      this.extractPrivateCoachList(allPrivateCourses)
    } else if (targetCourseType === 1) {
      displayCourses = filteredGroup
    } else {
      displayCourses = [...filteredPrivate, ...filteredGroup]
    }

    this.setData({ displayCourses })
  },

  /**
 * 从私教课列表中提取教练列表（现在每个课程已对应一个教练）
 */
  extractPrivateCoachList(privateCourses: any[]) {
    // 由于每个课程已经只对应一个教练，直接从课程中提取去重
    const coachMap = new Map()
    for (const course of privateCourses) {
      if (course.coachId && !coachMap.has(course.coachId)) {
        coachMap.set(course.coachId, {
          id: course.coachId,
          name: course.coachName,
          specialty: course.coachSpecialty,
          rating: course.coachRating,
          yearsOfExperience: course.coachYearsOfExperience
        })
      }
    }
    const coachList = Array.from(coachMap.values())

    // 在列表开头添加"全部教练"选项
    const fullList = [
      { id: null, name: '全部教练', specialty: '', rating: null, yearsOfExperience: null },
      ...coachList
    ]

    // 更新教练列表，并保持当前选中的教练
    const currentCoachId = this.data.selectedCoachId
    let newSelectedCoachId = currentCoachId
    let newSelectedCoachName = ''

    if (currentCoachId && coachList.some(c => c.id === currentCoachId)) {
      // 当前选中的教练还在列表中，保持选中
      const coach = coachList.find(c => c.id === currentCoachId)
      newSelectedCoachName = coach?.name || ''
    } else {
      // 当前选中的教练不在列表中，清空选中
      newSelectedCoachId = null
      newSelectedCoachName = '全部教练'
    }

    this.setData({
      privateCoachList: coachList,
      selectedCoachId: newSelectedCoachId,
      selectedCoachName: newSelectedCoachName
    })
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
    this.setData({
      selectedCourseTypeIndex: index
    }, () => {
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

  /**
   * 私教课教练选择变化
   */
  onPrivateCoachChange(e: any) {
    const index = e.detail.value
    const coach = this.data.privateCoachList[index]
    if (coach) {
      this.setData({
        selectedCoachId: coach.id,
        selectedCoachName: coach.name
      }, () => {
        this.updateDisplayCourses()
      })
    }
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
  
      // ✅ 根据课程类型调用不同的方法
      if (targetCourseType === 0) {
        await bookingStore.loadPrivateCourses()
      } else if (targetCourseType === 1) {
        await bookingStore.loadGroupCourses({ date: selectedDate })
      } else {
        // 两者都有，同时加载
        await Promise.all([
          bookingStore.loadPrivateCourses(),
          bookingStore.loadGroupCourses({ date: selectedDate })
        ])
      }
  
      // 更新显示
      this.updateDisplayCourses()
      this.setData({ availableLoading: false })
    } catch (error: any) {
      console.error('加载可预约课程失败:', error)
      showToast(error.message || '加载失败', 'none')
      this.setData({ availableLoading: false })
    }
  },

  /**
 * 点击课程 - 打开预约弹窗
 */
  onCourseTap(e: any) {
    const course = e.currentTarget.dataset.course

    if (course.courseType === 0) {
      // 私教课：直接使用当前课程的信息（已经包含教练）
      this.openPrivateBookingModal(course)
    } else {
      // 团课：检查是否满员
      if (!course.canBook) {
        showToast('课程已满员', 'none')
        return
      }
      this.setData({
        selectedCourse: course,
        showBookingModal: true
      })
    }
  },

  /**
   * 打开私教课预约弹窗
   */
  openPrivateBookingModal(course: any) {
    // 初始化日期选择器
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

    // 获取课程时长
    const duration = course.duration || 60

    // 获取营业开始时间
    const businessStart = configStore.businessStartTime || '08:00'

    // 计算当天的最小开始时间
    const minStartTime = configStore.getMinStartTime(todayStr, duration)
    const maxStartTime = configStore.getMaxStartTime(duration)

    this.setData({
      showPrivateBookingModal: true,
      selectedCourse: course,
      selectedCourseDuration: duration,
      privateScheduleDate: todayStr,
      privateStartTime: minStartTime,
      privateEndTime: configStore.calculateEndTime(minStartTime, duration),
      privateMinDate: todayStr,
      privateMaxDate: maxDateStr,
      privateMinStartTime: minStartTime,
      privateMaxStartTime: maxStartTime,
      privateBookingLoading: false
    })
  },

  /**
   * 私教课日期变化 - 重新计算开始时间和可选范围
   */
  onPrivateDateChange(e: any) {
    const newDate = e.detail.value
    const duration = this.data.selectedCourseDuration

    // 判断选择的日期是否是当天
    const today = new Date()
    const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
    const isToday = newDate === todayStr

    // 获取营业开始时间
    const businessStart = configStore.businessStartTime || '08:00'

    let minStartTime = ''
    let defaultStartTime = ''

    if (isToday) {
      // 当天：使用动态计算的最小开始时间
      minStartTime = configStore.getMinStartTime(newDate, duration)
      defaultStartTime = minStartTime
    } else {
      // 未来日期：最小开始时间为营业开始时间，默认开始时间也是营业开始时间
      minStartTime = businessStart
      defaultStartTime = businessStart
    }

    // 获取最大开始时间
    const maxStartTime = configStore.getMaxStartTime(duration)

    console.log('日期变化:', newDate, '是否当天:', isToday)
    console.log('minStartTime:', minStartTime)
    console.log('maxStartTime:', maxStartTime)
    console.log('defaultStartTime:', defaultStartTime)

    this.setData({
      privateScheduleDate: newDate,
      privateMinStartTime: minStartTime,
      privateMaxStartTime: maxStartTime,
      privateStartTime: defaultStartTime,
      privateEndTime: configStore.calculateEndTime(defaultStartTime, duration)
    })
  },

  /**
   * 私教课开始时间变化 - 自动计算结束时间
   */
  onPrivateTimeChange(e: any) {
    const startTime = e.detail.value
    const duration = this.data.selectedCourseDuration

    if (startTime && duration) {
      const [hours, minutes] = startTime.split(':').map(Number)
      const durationHours = Math.floor(duration / 60)
      const durationMinutes = duration % 60

      let endHour = hours + durationHours
      let endMinute = minutes + durationMinutes

      if (endMinute >= 60) {
        endMinute -= 60
        endHour += 1
      }

      const endTime = `${endHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')}`

      this.setData({
        privateStartTime: startTime,
        privateEndTime: endTime
      })
    } else {
      this.setData({
        privateStartTime: startTime,
        privateEndTime: ''
      })
    }
  },

  /**
   * 关闭私教课预约弹窗
   */
  onClosePrivateBookingModal() {
    this.setData({
      showPrivateBookingModal: false,
      selectedCourse: null,
      privateScheduleDate: '',
      privateStartTime: '',
      privateEndTime: '',
      privateBookingLoading: false
    })
  },

  /**
 * 确认私教课预约
 */
  async onConfirmPrivateBooking() {
    const { selectedCourse, privateScheduleDate, privateStartTime } = this.data
    const memberId = userStore.memberId

    if (!memberId) {
      showToast('请先登录', 'none')
      return
    }

    if (!selectedCourse || !selectedCourse.coachId) {
      showToast('请选择教练', 'none')
      return
    }

    if (!privateScheduleDate) {
      showToast('请选择日期', 'none')
      return
    }

    if (!privateStartTime) {
      showToast('请选择开始时间', 'none')
      return
    }

    this.setData({ privateBookingLoading: true })

    try {
      await bookingStore.bookPrivateCourse(
        memberId,
        selectedCourse.coachId,
        privateScheduleDate,
        privateStartTime
      )

      showToast('预约成功', 'success')
      this.onClosePrivateBookingModal()

      // 刷新列表
      this.loadAvailableCourses()
      this.loadMyBookings(true)

    } catch (error: any) {
      showToast(error.message || '预约失败', 'none')
    } finally {
      this.setData({ privateBookingLoading: false })
    }
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
      await bookingStore.createBooking(memberId, selectedCourse)

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
      const status = this.data.bookingStatus === -1 ? null : this.data.bookingStatus

      await bookingStore.loadMyBookings(refresh, status)

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

    if (!booking.signCode) {
      showToast('签到码未生成', 'none')
      return
    }

    this.setData({
      showCheckinModal: true,
      checkinBooking: booking,
      checkinLoading: false,
      showCancelBtn: this.checkCanCancel(booking.scheduleDate, booking.startTime)
    })
  },

  /**
   * 检查是否可取消
   */
  checkCanCancel(scheduleDate: string, startTime: string): boolean {
    if (!scheduleDate || !startTime) return false

    const cancelHours = configStore.courseCancelHours || 2

    const now = new Date()

    const dateStr = scheduleDate.replace(/-/g, '/')
    const courseStart = new Date(`${dateStr} ${startTime}`)

    if (isNaN(courseStart.getTime())) {
      console.error('无效的日期格式', scheduleDate, startTime)
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

    const date = new Date(expireTime)

    if (isNaN(date.getTime())) {
      console.error('无效的过期时间格式', expireTime)
      return ''
    }

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
      // 下拉刷新时重新加载数据
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