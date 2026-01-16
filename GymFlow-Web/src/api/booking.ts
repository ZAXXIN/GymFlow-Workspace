import { http } from '@/utils/request'
import type { CourseBooking, QueryParams, PaginatedResponse, ApiResponse } from '@/types'

export const bookingApi = {
  /**
   * 获取预约列表
   */
  getBookings(params?: QueryParams): Promise<PaginatedResponse<CourseBooking>> {
    return http.get('/bookings', { params })
  },

  /**
   * 获取预约详情
   */
  getBookingById(id: number): Promise<CourseBooking> {
    return http.get(`/bookings/${id}`)
  },

  /**
   * 创建预约
   */
  createBooking(data: Partial<CourseBooking>): Promise<CourseBooking> {
    return http.post('/bookings', data)
  },

  /**
   * 更新预约
   */
  updateBooking(id: number, data: Partial<CourseBooking>): Promise<CourseBooking> {
    return http.put(`/bookings/${id}`, data)
  },

  /**
   * 删除预约
   */
  deleteBooking(id: number): Promise<void> {
    return http.delete(`/bookings/${id}`)
  },

  /**
   * 签到
   */
  checkIn(bookingId: number): Promise<CourseBooking> {
    return http.post(`/bookings/${bookingId}/check-in`)
  },

  /**
   * 签退
   */
  checkOut(bookingId: number): Promise<CourseBooking> {
    return http.post(`/bookings/${bookingId}/check-out`)
  },

  /**
   * 批量签到
   */
  batchCheckIn(bookingIds: number[]): Promise<void> {
    return http.post('/bookings/batch-check-in', { bookingIds })
  },

  /**
   * 获取今日预约
   */
  getTodayBookings(params?: QueryParams): Promise<PaginatedResponse<CourseBooking>> {
    return http.get('/bookings/today', { params })
  },

  /**
   * 导出预约数据
   */
  exportBookings(params?: QueryParams): Promise<Blob> {
    return http.get('/bookings/export', {
      params,
      responseType: 'blob'
    })
  }
}