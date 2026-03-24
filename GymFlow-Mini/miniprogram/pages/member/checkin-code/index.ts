// 会员端签到码页面逻辑
import { getCheckinCode } from '../../../services/api/checkin.api'
import { getBookingDetail } from '../../../services/api/booking.api'
import { bookingStore } from '../../../stores/booking.store'
import { configStore } from '../../../stores/config.store'
import { showToast, showModal, showLoading, hideLoading } from '../../../utils/wx-util'
import { formatTime } from '../../../utils/date'

Page({
  data: {
    // 预约ID
    bookingId: 0,
    
    // 课程信息
    courseInfo: {
      courseName: '',
      courseTypeDesc: '',
      courseDate: '',
      startTime: '',
      endTime: '',
    },
    
    // 签到码信息
    numericCode: '',
    qrCodeContent: '',
    expireTime: '',
    
    // 是否显示取消按钮
    showCancel: false,
    
    // 加载状态
    loading: true
  },

  onLoad(options: any) {
    const { bookingId } = options
    if (!bookingId) {
      showToast('参数错误', 'none')
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
      return
    }
    
    this.setData({ bookingId: parseInt(bookingId) })
    this.initData()
  },

  /**
   * 初始化数据
   */
  async initData() {
    showLoading('加载中...')
    
    try {
      // 并行加载预约详情和签到码
      await Promise.all([
        this.loadBookingDetail(),
        this.loadCheckinCode()
      ])
      
      // 检查是否可取消
      this.checkCancelable()
      
      hideLoading()
      this.setData({ loading: false })
    } catch (error: any) {
      hideLoading()
      showToast(error.message || '加载失败', 'none')
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    }
  },

  /**
   * 加载预约详情
   */
  async loadBookingDetail() {
    const { bookingId } = this.data
    
    try {
      const detail = await getBookingDetail(bookingId)
      
      this.setData({
        'courseInfo.courseName': detail.courseName || '',
        'courseInfo.courseTypeDesc': detail.bookingStatusDesc || '',
        'courseInfo.courseDate': detail.courseDate || '',
        'courseInfo.startTime': detail.startTime || '',
        'courseInfo.endTime': detail.endTime || '',
      })
      
      // 保存到store
      bookingStore.setCurrentBooking(detail)
      
    } catch (error) {
      console.error('加载预约详情失败:', error)
      throw error
    }
  },

  /**
   * 加载签到码
   */
  async loadCheckinCode() {
    const { bookingId } = this.data
    
    try {
      const result = await getCheckinCode(bookingId)
      
      // 生成二维码内容
      const qrContent = `gymflow://checkin?bookingId=${bookingId}&code=${result.numericCode}`
      
      this.setData({
        numericCode: result.numericCode,
        qrCodeContent: qrContent,
        expireTime: this.formatExpireTime(result.expireTime)
      })
      
    } catch (error) {
      console.error('加载签到码失败:', error)
      throw error
    }
  },

  /**
   * 检查是否可取消
   */
  checkCancelable() {
    const { courseInfo } = this.data
    const { courseDate, startTime } = courseInfo
    
    if (!courseDate || !startTime) {
      this.setData({ showCancel: false })
      return
    }
    
    // 获取系统配置的取消时间限制
    const cancelHours = configStore.courseCancelHours || 2
    
    const now = new Date()
    const courseStart = new Date(`${courseDate} ${startTime}`)
    const hoursUntilCourse = (courseStart.getTime() - now.getTime()) / (1000 * 60 * 60)
    
    // 课程开始前 cancelHours 小时内不可取消
    const showCancel = hoursUntilCourse >= cancelHours
    
    this.setData({ showCancel })
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
   * 格式化时间
   */
  formatTime(time: string): string {
    return formatTime(time)
  },

  /**
   * 取消预约
   */
  async onCancelBooking() {
    const { bookingId } = this.data
    
    const confirm = await showModal({
      title: '提示',
      content: '确定要取消该预约吗？'
    })
    
    if (!confirm) return
    
    showLoading('取消中...')
    
    try {
      const { cancelBooking } = await import('../../../services/api/booking.api')
      await cancelBooking({
        bookingId,
        reason: '用户取消'
      })
      
      hideLoading()
      showToast('取消成功', 'success')
      
      // 更新store中的预约状态
      bookingStore.updateBookingStatus(bookingId, 3)
      
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
      
    } catch (error: any) {
      hideLoading()
      showToast(error.message || '取消失败', 'none')
    }
  },

  /**
   * 返回
   */
  onBack() {
    wx.navigateBack()
  }
})