// 预约相关自定义Hook

import { bookingStore } from '../stores/booking.store'
import { configStore } from '../stores/config.store'
import { 
  createBooking, 
  cancelBooking as cancelBookingApi,
  getBookingDetail 
} from '../services/api/booking.api'
import { getCheckinCode } from '../services/api/checkin.api'
import { showModal, showSuccess, showError, showLoading, hideLoading } from '../utils/wx-util'
import { BookingStatus } from '../services/types/booking.types'

export const useBooking = () => {
  /**
   * 加载我的预约
   */
  const loadMyBookings = async (status?: BookingStatus, refresh: boolean = false) => {
    try {
      return await bookingStore.loadMyBookings(status, refresh)
    } catch (error: any) {
      showError(error.message || '加载预约失败')
      throw error
    }
  }

  /**
   * 加载可预约课程
   */
  const loadAvailableCourses = async (params?: any, refresh: boolean = false) => {
    try {
      return await bookingStore.loadAvailableCourses(params, refresh)
    } catch (error: any) {
      showError(error.message || '加载课程失败')
      throw error
    }
  }

  /**
   * 创建预约
   */
  const createNewBooking = async (params: {
    memberId: number
    scheduleId: number
  }) => {
    try {
      showLoading('预约中...')
      
      await createBooking(params)
      
      hideLoading()
      showSuccess('预约成功')
      
      // 刷新预约列表
      await bookingStore.loadMyBookings(undefined, true)
      
      return true
    } catch (error: any) {
      hideLoading()
      showError(error.message || '预约失败')
      throw error
    }
  }

  /**
   * 取消预约
   */
  const cancelUserBooking = async (bookingId: number) => {
    try {
      const confirm = await showModal({
        title: '提示',
        content: '确定要取消该预约吗？'
      })
      
      if (!confirm) return false
      
      showLoading('取消中...')
      
      await bookingStore.cancelBooking(bookingId, '用户取消')
      
      hideLoading()
      showSuccess('取消成功')
      
      return true
    } catch (error: any) {
      hideLoading()
      showError(error.message || '取消失败')
      throw error
    }
  }

  /**
   * 获取预约详情
   */
  const fetchBookingDetail = async (bookingId: number) => {
    try {
      showLoading()
      const detail = await getBookingDetail(bookingId)
      bookingStore.setCurrentBooking(detail)
      hideLoading()
      return detail
    } catch (error: any) {
      hideLoading()
      showError(error.message || '获取详情失败')
      throw error
    }
  }

  /**
   * 获取签到码
   */
  const fetchCheckinCode = async (bookingId: number) => {
    try {
      showLoading('生成签到码中...')
      const code = await getCheckinCode(bookingId)
      hideLoading()
      return code
    } catch (error: any) {
      hideLoading()
      showError(error.message || '获取签到码失败')
      throw error
    }
  }

  /**
   * 检查是否可签到
   */
  const canCheckin = (courseDate: string, startTime: string): boolean => {
    return configStore.canCheckin(courseDate, startTime)
  }

  /**
   * 获取预约按状态分组
   */
  const getBookingsGroupedByStatus = () => {
    const all = bookingStore.myBookings
    
    return {
      pending: all.filter(b => b.bookingStatus === 0),
      checked: all.filter(b => b.bookingStatus === 1),
      completed: all.filter(b => b.bookingStatus === 2),
      cancelled: all.filter(b => b.bookingStatus === 3)
    }
  }

  /**
   * 获取今日即将开始的课程
   */
  const getTodayUpcomingBookings = () => {
    const today = new Date().toISOString().split('T')[0]
    
    return bookingStore.myBookings.filter(b => {
      return b.bookingStatus === 0 && 
             b.courseDate === today && 
             canCheckin(b.courseDate!, b.startTime!)
    })
  }

  return {
    myBookings: bookingStore.myBookings,
    availableCourses: bookingStore.availableCourses,
    currentBooking: bookingStore.currentBooking,
    loading: bookingStore.loading,
    hasMore: bookingStore.hasMore,
    loadMyBookings,
    loadAvailableCourses,
    createBooking: createNewBooking,
    cancelBooking: cancelUserBooking,
    getBookingDetail: fetchBookingDetail,
    getCheckinCode: fetchCheckinCode,
    canCheckin,
    getBookingsGroupedByStatus,
    getTodayUpcomingBookings
  }
}