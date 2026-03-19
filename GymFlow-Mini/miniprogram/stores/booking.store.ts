// 预约状态管理

import { 
  getMyBookings, 
  getAvailableCourses,
  cancelBooking 
} from '../services/api/booking.api'
import { 
  CourseBooking, 
  CourseSchedule,
  BookingStatus 
} from '../services/types/booking.types'

class BookingStore {
  private _myBookings: CourseBooking[] = []
  private _availableCourses: CourseSchedule[] = []
  private _currentBooking: CourseBooking | null = null
  private _loading: boolean = false
  private _hasMore: boolean = true
  private _currentPage: number = 1
  private _pageSize: number = 10

  /**
   * 加载我的预约
   */
  async loadMyBookings(status?: BookingStatus, refresh: boolean = false) {
    if (this._loading) return
    
    if (refresh) {
      this._currentPage = 1
      this._hasMore = true
      this._myBookings = []
    }
    
    if (!this._hasMore) return
    
    this._loading = true
    
    try {
      const result = await getMyBookings({
        status,
        pageNum: this._currentPage,
        pageSize: this._pageSize
      })
      
      if (refresh) {
        this._myBookings = result.list
      } else {
        this._myBookings = [...this._myBookings, ...result.list]
      }
      
      this._hasMore = this._currentPage < result.pages
      this._currentPage++
      
      return result
    } catch (error) {
      console.error('加载我的预约失败:', error)
      throw error
    } finally {
      this._loading = false
    }
  }

  /**
   * 加载可预约课程
   */
  async loadAvailableCourses(params?: any, refresh: boolean = false) {
    if (this._loading) return
    
    if (refresh) {
      this._currentPage = 1
      this._hasMore = true
      this._availableCourses = []
    }
    
    if (!this._hasMore) return
    
    this._loading = true
    
    try {
      const result = await getAvailableCourses({
        pageNum: this._currentPage,
        pageSize: this._pageSize,
        ...params
      })
      
      if (refresh) {
        this._availableCourses = result.list
      } else {
        this._availableCourses = [...this._availableCourses, ...result.list]
      }
      
      this._hasMore = this._currentPage < result.pages
      this._currentPage++
      
      return result
    } catch (error) {
      console.error('加载可预约课程失败:', error)
      throw error
    } finally {
      this._loading = false
    }
  }

  /**
   * 取消预约
   */
  async cancelBooking(bookingId: number, reason: string) {
    try {
      await cancelBooking({ bookingId, reason })
      
      // 更新本地数据
      const index = this._myBookings.findIndex(b => b.id === bookingId)
      if (index !== -1) {
        this._myBookings[index].bookingStatus = 3
        this._myBookings[index].bookingStatusDesc = '已取消'
        this._myBookings[index].cancellationReason = reason
        this._myBookings[index].cancellationTime = new Date().toISOString()
      }
      
      return true
    } catch (error) {
      console.error('取消预约失败:', error)
      throw error
    }
  }

  /**
   * 设置当前预约
   */
  setCurrentBooking(booking: CourseBooking) {
    this._currentBooking = booking
  }

  /**
   * 更新预约状态
   */
  updateBookingStatus(bookingId: number, status: BookingStatus) {
    const booking = this._myBookings.find(b => b.id === bookingId)
    if (booking) {
      booking.bookingStatus = status
      booking.bookingStatusDesc = this.getStatusText(status)
      
      if (status === 1) {
        booking.checkinTime = new Date().toISOString()
      }
    }
  }

  /**
   * 获取状态文本
   */
  private getStatusText(status: BookingStatus): string {
    const statusMap: Record<BookingStatus, string> = {
      0: '待上课',
      1: '已签到',
      2: '已完成',
      3: '已取消'
    }
    return statusMap[status] || '未知'
  }

  /**
   * 按状态筛选预约
   */
  getBookingsByStatus(status?: BookingStatus): CourseBooking[] {
    if (status === undefined) {
      return this._myBookings
    }
    return this._myBookings.filter(b => b.bookingStatus === status)
  }

  /**
   * 重置状态
   */
  reset() {
    this._myBookings = []
    this._availableCourses = []
    this._currentBooking = null
    this._hasMore = true
    this._currentPage = 1
    this._loading = false
  }

  // Getters
  get myBookings() {
    return this._myBookings
  }

  get availableCourses() {
    return this._availableCourses
  }

  get currentBooking() {
    return this._currentBooking
  }

  get loading() {
    return this._loading
  }

  get hasMore() {
    return this._hasMore
  }
}

// 导出单例
export const bookingStore = new BookingStore()