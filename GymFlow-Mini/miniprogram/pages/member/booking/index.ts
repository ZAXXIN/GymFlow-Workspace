// 会员端预约页面逻辑
import { TabBarHelper } from '../../../utils/tabbar-helper'
import { userStore } from '../../../stores/user.store'
import { bookingStore } from '../../../stores/booking.store'
import { getAvailableCourses, getMyBookings, createBooking, cancelBooking } from '../../../services/api/booking.api'
import { formatDate, isToday, isTomorrow } from '../../../utils/date'
import { showToast, showModal } from '../../../utils/wx-util'

Page({
  data: {
    selectedTab: 0,

    // 用户信息
    userInfo: null as any,
    
    // Tab切换
    activeTab: 0, // 0-可预约 1-我的预约
    
    // 可预约课程
    availableCourses: [] as any[],
    availableLoading: false,
    availableHasMore: true,
    availablePageNum: 1,
    availablePageSize: 10,
    availableTotalPages: 0,
    
    // 我的预约
    myBookings: [] as any[],
    myLoading: false,
    myHasMore: true,
    myPageNum: 1,
    myPageSize: 10,
    myTotalPages: 0,
    bookingStatus: -1, // 预约状态筛选
    
    // 状态Tab选项（预约状态：0-待上课，1-已签到，2-已完成，3-已取消）
    statusTabs: [
      { label: '全部', value: -1 },
      { label: '待上课', value: 0 },
      { label: '已签到', value: 1 },
      { label: '已完成', value: 2 },
      { label: '已取消', value: 3 }
    ],
    
    // 搜索关键字
    keyword: '',
    
    // 选中的课程
    selectedCourse: null as any,
    showBookingModal: false,
    bookingLoading: false
  },

  onLoad(options: any) {
    // 获取当前页面在 TabBar 中的索引
    this.updateSelectedTab()
    this.initData()
  },

  onShow() {
    // 每次显示时刷新数据
    if (this.data.activeTab === 0) {
      this.loadAvailableCourses(true)
    } else {
      this.loadMyBookings(true)
    }
  },

  /**
   * 更新选中的 Tab 索引
   */
  updateSelectedTab() {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const pagePath = '/' + currentPage.route
    this.setData({
      selectedTab: TabBarHelper.getSelectedIndex(pagePath)
    })
  },

  /**
   * TabBar 切换事件
   */
  onTabChange(e: any) {
    const { index } = e.detail
    this.setData({ selectedTab: index })
  },

  /**
   * 初始化数据
   */
  initData() {
    this.setData({ userInfo: userStore.userInfo })
    this.loadAvailableCourses(true)
  },

  /**
   * 加载可预约课程
   */
  loadAvailableCourses(refresh: boolean) {
    if (this.data.availableLoading) return
    if (!refresh && !this.data.availableHasMore) return
    
    this.setData({ availableLoading: true })
    
    const params: any = {
      pageNum: refresh ? 1 : this.data.availablePageNum,
      pageSize: this.data.availablePageSize
    }
    
    if (this.data.keyword) {
      params.courseName = this.data.keyword
    }
    
    getAvailableCourses(params)
      .then((result: any) => {
        const newCourses = refresh ? result.list : [...this.data.availableCourses, ...result.list]
        
        this.setData({
          availableCourses: newCourses,
          availableHasMore: result.pageNum < result.pages,
          availableTotalPages: result.pages,
          availablePageNum: (refresh ? 1 : this.data.availablePageNum) + 1,
          availableLoading: false
        })
      })
      .catch((error: any) => {
        console.error('加载可预约课程失败:', error)
        showToast(error.message || '加载课程失败', 'none')
        this.setData({ availableLoading: false })
      })
  },

  /**
   * 加载我的预约
   */
  loadMyBookings(refresh: boolean) {
    if (this.data.myLoading) return
    if (!refresh && !this.data.myHasMore) return
    
    this.setData({ myLoading: true })
    
    const params: any = {
      pageNum: refresh ? 1 : this.data.myPageNum,
      pageSize: this.data.myPageSize
    }
    
    // 只有当 bookingStatus 不是 -1 时才传递 status 参数
    if (this.data.bookingStatus !== -1) {
      params.status = this.data.bookingStatus
    }
    
    getMyBookings(params)
      .then((result: any) => {
        const newBookings = refresh ? result.list : [...this.data.myBookings, ...result.list]
        
        this.setData({
          myBookings: newBookings,
          myHasMore: result.pageNum < result.pages,
          myTotalPages: result.pages,
          myPageNum: (refresh ? 1 : this.data.myPageNum) + 1,
          myLoading: false
        })
      })
      .catch((error: any) => {
        console.error('加载我的预约失败:', error)
        showToast(error.message || '加载预约失败', 'none')
        this.setData({ myLoading: false })
      })
  },

  /**
   * 切换主Tab
   */
  onMainTabChange(e: any) {
    const index = e.detail.index
    this.setData({ activeTab: index }, () => {
      if (index === 0) {
        this.loadAvailableCourses(true)
      } else {
        this.loadMyBookings(true)
      }
    })
  },

  /**
   * 状态Tab点击
   */
  onStatusTabTap(e: any) {
    const value = e.currentTarget.dataset.value
    
    this.setData({ 
      bookingStatus: value,
      myPageNum: 1,
      myHasMore: true
    }, () => {
      this.loadMyBookings(true)
    })
  },

  /**
   * 搜索输入
   */
  onSearchInput(e: any) {
    this.setData({ keyword: e.detail.value })
  },

  /**
   * 执行搜索
   */
  onSearch(e: any) {
    const value = e.detail.value
    this.setData({ keyword: value }, () => {
      this.loadAvailableCourses(true)
    })
  },

  /**
   * 清除搜索
   */
  onSearchClear() {
    this.setData({ keyword: '' }, () => {
      this.loadAvailableCourses(true)
    })
  },

  /**
   * 点击可预约课程
   */
  onCourseTap(e: any) {
    const course = e.currentTarget.dataset.course
    this.setData({ 
      selectedCourse: course,
      showBookingModal: true
    })
  },

  /**
   * 关闭预约弹窗
   */
  onCloseBookingModal() {
    this.setData({ 
      showBookingModal: false,
      selectedCourse: null
    })
  },

  /**
   * 确认预约
   */
  async onConfirmBooking() {
    const selectedCourse = this.data.selectedCourse
    const userInfo = this.data.userInfo
    
    if (!userInfo || !userInfo.memberId) {
      showToast('请先登录', 'none')
      return
    }
    
    this.setData({ bookingLoading: true })
    
    try {
      await createBooking({
        memberId: userInfo.memberId,
        scheduleId: selectedCourse.scheduleId
      })
      
      showToast('预约成功', 'success')
      this.onCloseBookingModal()
      
      // 刷新数据
      this.loadAvailableCourses(true)
      this.loadMyBookings(true)
      
    } catch (error: any) {
      showToast(error.message || '预约失败', 'none')
      this.setData({ bookingLoading: false })
    }
  },

  /**
   * 点击我的预约
   */
  onBookingTap(e: any) {
    const booking = e.currentTarget.dataset.booking
    wx.navigateTo({
      url: `/pages/common/booking-detail/index?id=${booking.id}`
    })
  },

  /**
   * 取消预约
   */
  async onCancelBooking(e: any) {
    const booking = e.currentTarget.dataset.booking
    
    const confirm = await showModal({
      title: '提示',
      content: '确定要取消该预约吗？'
    })
    
    if (!confirm) return
    
    try {
      await cancelBooking({
        bookingId: booking.id,
        reason: '用户取消'
      })
      
      showToast('取消成功', 'success')
      
      // 刷新数据
      this.loadMyBookings(true)
      if (this.data.activeTab === 0) {
        this.loadAvailableCourses(true)
      }
      
    } catch (error: any) {
      showToast(error.message || '取消失败', 'none')
    }
  },

  /**
   * 查看签到码
   */
  onViewCheckinCode(e: any) {
    const booking = e.currentTarget.dataset.booking
    wx.navigateTo({
      url: `/pages/member/checkin-code/index?bookingId=${booking.id}`
    })
  },

  /**
   * 获取状态文本
   */
  getStatusText(status: number): string {
    const map: Record<number, string> = {
      0: '待上课',
      1: '已签到',
      2: '已完成',
      3: '已取消'
    }
    return map[status] || '未知'
  },

  /**
   * 获取状态类名
   */
  getStatusClass(status: number): string {
    const map: Record<number, string> = {
      0: 'status-pending',
      1: 'status-success',
      2: 'status-completed',
      3: 'status-warning'
    }
    return map[status] || ''
  },

  /**
   * 格式化时间
   */
  formatTime(time: string): string {
    if (!time) return ''
    return time.substring(11, 16)
  },

  /**
   * 获取剩余名额显示
   */
  getRemainingText(remaining: number): string {
    if (remaining <= 0) return '已满'
    if (remaining <= 5) return `仅剩${remaining}席`
    return `${remaining}席`
  },

  /**
   * 格式化日期
   */
  formatDate(dateStr: string): string {
    if (!dateStr) return ''
    const date = new Date(dateStr)
    if (isToday(date)) return '今天'
    if (isTomorrow(date)) return '明天'
    return formatDate(date, 'MM月DD日')
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh() {
    if (this.data.activeTab === 0) {
      this.loadAvailableCourses(true)
    } else {
      this.loadMyBookings(true)
    }
    wx.stopPullDownRefresh()
  },

  /**
   * 上拉加载更多
   */
  onReachBottom() {
    if (!this.data.loading) {
      if (this.data.activeTab === 0) {
        if (this.data.availableHasMore) {
          this.loadAvailableCourses(false)
        }
      } else {
        if (this.data.myHasMore) {
          this.loadMyBookings(false)
        }
      }
    }
  },

  /**
   * 页面分享
   */
  onShareAppMessage() {
    return {
      title: '课程预约 - GymFlow健身',
      path: '/pages/member/booking/index'
    }
  }
})