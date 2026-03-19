// 会员端预约页面逻辑
import { userStore } from '../../../stores/user.store'
import { bookingStore } from '../../../stores/booking.store'
import { getAvailableCourses, getMyBookings } from '../../../services/api/booking.api'
import { formatDate, isToday, isTomorrow } from '../../../utils/date'
import { showToast, showModal } from '../../../utils/wx-util'

Page({
  data: {
    // 用户信息
    userInfo: null,
    
    // Tab切换
    activeTab: 0, // 0-可预约 1-我的预约
    
    // 可预约课程
    availableCourses: [],
    availableLoading: false,
    availableHasMore: true,
    availablePageNum: 1,
    availablePageSize: 10,
    
    // 我的预约
    myBookings: [],
    myLoading: false,
    myHasMore: true,
    myPageNum: 1,
    myPageSize: 10,
    bookingStatus: -1, // 预约状态筛选
    
    // 状态Tab选项（预约状态：0-待上课，1-已签到，2-已完成，3-已取消）
    statusTabs: [
      { label: '全部', value: -1 },  // 用 -1 代表全部
      { label: '待上课', value: 0 },
      { label: '已签到', value: 1 },
      { label: '已完成', value: 2 },
      { label: '已取消', value: 3 }
    ],
    
    // 搜索关键字
    keyword: '',
    
    // 选中的课程
    selectedCourse: null,
    showBookingModal: false,
    bookingLoading: false
  },

  onLoad: function() {
    this.initData()
  },

  onShow: function() {
    // 每次显示时刷新数据
    var that = this
    if (this.data.activeTab === 0) {
      this.loadAvailableCourses(true)
    } else {
      this.loadMyBookings(true)
    }
  },

  /**
   * 初始化数据
   */
  initData: function() {
    this.setData({ userInfo: userStore.userInfo })
    this.loadAvailableCourses(true)
  },

  /**
   * 加载可预约课程
   */
  loadAvailableCourses: function(refresh) {
    var that = this
    var data = this.data
    var availableLoading = data.availableLoading
    var availableHasMore = data.availableHasMore
    var availablePageNum = data.availablePageNum
    var availablePageSize = data.availablePageSize
    var keyword = data.keyword
    
    if (availableLoading) return
    if (!refresh && !availableHasMore) return
    
    this.setData({ availableLoading: true })
    
    var params = {
      pageNum: refresh ? 1 : availablePageNum,
      pageSize: availablePageSize
    }
    
    if (keyword) {
      params.courseName = keyword
    }
    
    getAvailableCourses(params).then(function(result) {
      that.setData({
        availableCourses: refresh ? result.list : [...that.data.availableCourses, ...result.list],
        availableHasMore: result.pageNum < result.pages,
        availablePageNum: (refresh ? 1 : availablePageNum) + 1,
        availableLoading: false
      })
    }).catch(function(error) {
      console.error('加载可预约课程失败:', error)
      that.setData({ availableLoading: false })
    })
  },

  /**
   * 加载我的预约
   */
  loadMyBookings: function(refresh) {
    var that = this
    var data = this.data
    var myLoading = data.myLoading
    var myHasMore = data.myHasMore
    var myPageNum = data.myPageNum
    var myPageSize = data.myPageSize
    var bookingStatus = data.bookingStatus
    
    if (myLoading) return
    if (!refresh && !myHasMore) return
    
    this.setData({ myLoading: true })
    
    var params = {
      pageNum: refresh ? 1 : myPageNum,
      pageSize: myPageSize
    }
    
    // 只有当 bookingStatus 不是 -1 时才传递 status 参数
    if (bookingStatus !== -1) {
      params.status = bookingStatus
    }
    
    getMyBookings(params).then(function(result) {
      that.setData({
        myBookings: refresh ? result.list : [...that.data.myBookings, ...result.list],
        myHasMore: result.pageNum < result.pages,
        myPageNum: (refresh ? 1 : myPageNum) + 1,
        myLoading: false
      })
    }).catch(function(error) {
      console.error('加载我的预约失败:', error)
      that.setData({ myLoading: false })
    })
  },

  /**
   * 切换Tab
   */
  onTabChange: function(e) {
    var that = this
    var index = e.detail.index
    this.setData({ activeTab: index }, function() {
      if (index === 0) {
        that.loadAvailableCourses(true)
      } else {
        that.loadMyBookings(true)
      }
    })
  },

  /**
   * 状态Tab点击
   */
  onStatusTabTap: function(e) {
    var that = this
    var value = e.currentTarget.dataset.value
    
    this.setData({ 
      bookingStatus: value,
      myPageNum: 1
    }, function() {
      that.loadMyBookings(true)
    })
  },

  /**
   * 搜索输入
   */
  onSearchInput: function(e) {
    this.setData({ keyword: e.detail.value })
  },

  /**
   * 执行搜索
   */
  onSearch: function(e) {
    var that = this
    var value = e.detail.value
    this.setData({ keyword: value }, function() {
      that.loadAvailableCourses(true)
    })
  },

  /**
   * 清除搜索
   */
  onSearchClear: function() {
    var that = this
    this.setData({ keyword: '' }, function() {
      that.loadAvailableCourses(true)
    })
  },

  /**
   * 点击可预约课程
   */
  onCourseTap: function(e) {
    var course = e.currentTarget.dataset.course
    this.setData({ 
      selectedCourse: course,
      showBookingModal: true
    })
  },

  /**
   * 关闭预约弹窗
   */
  onCloseBookingModal: function() {
    this.setData({ 
      showBookingModal: false,
      selectedCourse: null
    })
  },

  /**
   * 确认预约
   */
  onConfirmBooking: function() {
    var that = this
    var selectedCourse = this.data.selectedCourse
    var userInfo = this.data.userInfo
    
    if (!userInfo || !userInfo.memberId) {
      showToast('请先登录', 'none')
      return
    }
    
    this.setData({ bookingLoading: true })
    
    // 调用预约接口
    var createBooking = require('../../../services/api/booking.api').createBooking
    
    createBooking({
      memberId: userInfo.memberId,
      scheduleId: selectedCourse.scheduleId
    }).then(function() {
      showToast('预约成功', 'success')
      that.onCloseBookingModal()
      
      // 刷新数据
      that.loadAvailableCourses(true)
      that.loadMyBookings(true)
      
    }).catch(function(error) {
      showToast(error.message || '预约失败', 'none')
      that.setData({ bookingLoading: false })
    })
  },

  /**
   * 点击我的预约
   */
  onBookingTap: function(e) {
    var booking = e.currentTarget.dataset.booking
    wx.navigateTo({
      url: '/pages/common/booking-detail/index?id=' + booking.id
    })
  },

  /**
   * 取消预约
   */
  onCancelBooking: function(e) {
    var that = this
    var booking = e.currentTarget.dataset.booking
    
    showModal({
      title: '提示',
      content: '确定要取消该预约吗？'
    }).then(function(confirm) {
      if (!confirm) return
      
      var cancelBooking = require('../../../services/api/booking.api').cancelBooking
      
      cancelBooking({
        bookingId: booking.id,
        reason: '用户取消'
      }).then(function() {
        showToast('取消成功', 'success')
        
        // 刷新数据
        that.loadMyBookings(true)
        if (that.data.activeTab === 0) {
          that.loadAvailableCourses(true)
        }
        
      }).catch(function(error) {
        showToast(error.message || '取消失败', 'none')
      })
    })
  },

  /**
   * 查看签到码
   */
  onViewCheckinCode: function(e) {
    var booking = e.currentTarget.dataset.booking
    wx.navigateTo({
      url: '/pages/member/checkin-code/index?bookingId=' + booking.id
    })
  },

  /**
   * 获取状态文本
   */
  getStatusText: function(status) {
    var map = {
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
  getStatusClass: function(status) {
    var map = {
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
  formatTime: function(time) {
    if (!time) return ''
    return time.substring(11, 16)
  },

  /**
   * 获取剩余名额显示
   */
  getRemainingText: function(remaining) {
    if (remaining <= 0) return '已满'
    if (remaining <= 5) return '仅剩' + remaining + '席'
    return remaining + '席'
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh: function() {
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
  onReachBottom: function() {
    if (this.data.activeTab === 0) {
      this.loadAvailableCourses(false)
    } else {
      this.loadMyBookings(false)
    }
  }
})