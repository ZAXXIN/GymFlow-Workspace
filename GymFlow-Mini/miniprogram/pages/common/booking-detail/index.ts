// 预约详情页面
import { getBookingDetail } from '../../../services/api/booking.api'
import { cancelBooking } from '../../../services/api/booking.api'
import { userStore } from '../../../stores/user.store'
import { bookingStore } from '../../../stores/booking.store'
import { configStore } from '../../../stores/config.store'
import { formatTime, formatDateTime } from '../../../utils/date'
import { showToast, showModal, showLoading, hideLoading } from '../../../utils/wx-util'

Page({
  data: {
    // 预约ID
    bookingId: 0,
    
    // 预约信息
    booking: {
      id: 0,
      bookingId: 0,
      courseName: '',
      courseType: 0,
      coachName: '',
      courseDate: '',
      startTime: '',
      endTime: '',
      bookingTime: '',
      bookingStatus: 0,
      bookingStatusDesc: '',
      checkinTime: '',
      cancellationTime: '',
      cancellationReason: '',
      checkinCode: '',
      memberId: 0,
      memberName: '',
      memberPhone: ''
    },
    
    // 用户角色
    userRole: '',
    
    // 是否可签到
    canCheckin: false,
    
    // 是否显示操作按钮
    showActions: false,
    
    // 加载状态
    loading: true,
    loadError: false,
    errorMessage: '',
    
    // 空状态
    isEmpty: false
  },

  onLoad: function(options) {
    var id = options.id
    
    if (!id) {
      this.setData({
        loadError: true,
        errorMessage: '参数错误',
        loading: false,
        isEmpty: true
      })
      showToast('参数错误', 'none')
      return
    }
    
    this.setData({ 
      bookingId: parseInt(id),
      userRole: userStore.role
    })
    
    this.loadBookingDetail()
  },

  /**
   * 加载预约详情
   */
  loadBookingDetail: function() {
    var that = this
    this.setData({ loading: true, loadError: false })
    
    var bookingId = this.data.bookingId
    
    getBookingDetail(bookingId).then(function(detail) {
      if (!detail) {
        that.setData({
          loading: false,
          isEmpty: true,
          loadError: true,
          errorMessage: '预约不存在'
        })
        return
      }
      
      that.setData({
        booking: detail,
        loading: false,
        isEmpty: false
      })
      
      that.checkActions()
      that.checkCheckinStatus()
      
    }).catch(function(error) {
      console.error('加载预约详情失败:', error)
      that.setData({
        loading: false,
        loadError: true,
        isEmpty: true,
        errorMessage: error.message || '加载失败，请重试'
      })
      showToast(error.message || '加载失败', 'none')
    })
  },

  /**
   * 重试加载
   */
  onRetry: function() {
    this.loadBookingDetail()
  },

  /**
   * 检查是否显示操作按钮
   */
  checkActions: function() {
    var booking = this.data.booking
    var userRole = this.data.userRole
    
    // 只有待上课状态且是会员本人或教练才显示操作按钮
    var showActions = booking.bookingStatus === 0
    
    this.setData({ showActions: showActions })
  },

  /**
   * 检查是否可签到
   */
  checkCheckinStatus: function() {
    var booking = this.data.booking
    
    if (!booking || booking.bookingStatus !== 0) {
      this.setData({ canCheckin: false })
      return
    }
    
    var canCheckin = configStore.canCheckin(
      booking.courseDate,
      booking.startTime
    )
    
    this.setData({ canCheckin: canCheckin })
  },

  /**
   * 取消预约
   */
  onCancelBooking: function() {
    var that = this
    var booking = this.data.booking
    
    showModal({
      title: '提示',
      content: '确定要取消该预约吗？'
    }).then(function(confirm) {
      if (!confirm) return
      
      showLoading('取消中...')
      
      cancelBooking({
        bookingId: booking.id,
        reason: '用户取消'
      }).then(function() {
        hideLoading()
        showToast('取消成功', 'success')
        
        // 更新本地状态
        that.setData({
          'booking.bookingStatus': 3,
          'booking.bookingStatusDesc': '已取消',
          'booking.cancellationTime': new Date().toISOString()
        })
        
        // 更新store
        bookingStore.updateBookingStatus(booking.id, 3)
        
        // 刷新操作按钮
        that.checkActions()
        
      }).catch(function(error) {
        hideLoading()
        showToast(error.message || '取消失败', 'none')
      })
    })
  },

  /**
   * 查看签到码
   */
  onViewCheckinCode: function() {
    var booking = this.data.booking
    
    wx.navigateTo({
      url: '/pages/member/checkin-code/index?bookingId=' + booking.id
    })
  },

  /**
   * 点击会员（教练端）
   */
  onMemberTap: function(e) {
    var memberid = e.currentTarget.dataset.memberid
    
    wx.navigateTo({
      url: '/pages/coach/member-detail/index?memberId=' + memberid
    })
  },

  /**
   * 返回
   */
  onBack: function() {
    wx.navigateBack()
  },

  /**
   * 获取状态图标
   */
  getStatusIcon: function(status) {
    var map = {
      0: 'pending',
      1: 'checkin',
      2: 'completed',
      3: 'cancelled'
    }
    console.log(map[status])
    return map[status] || 'pending'
  },

  /**
   * 获取课程类型文本
   */
  getCourseTypeText: function(type) {
    return type === 0 ? '私教课' : '团课'
  },

  /**
   * 格式化时间
   */
  formatTime: function(time) {
    return formatTime(time)
  },

  /**
   * 格式化日期时间
   */
  formatDateTime: function(time) {
    console.log(time)
    return formatDateTime(time)
  }
})