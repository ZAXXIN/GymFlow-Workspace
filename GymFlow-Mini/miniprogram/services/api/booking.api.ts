// 预约相关API

import { wxRequest } from '../../utils/request'
import { 
  CourseSchedule,
  CourseBooking,
  AvailableCourseParams,
  MyBookingParams,
  CreateBookingParams,
  CancelBookingParams,
  PageResult
} from '../types/booking.types'

/**
 * 获取可预约课程列表
 * GET /mini/booking/available-courses
 */
export const getAvailableCourses = (params: AvailableCourseParams) => {
  return wxRequest.get<PageResult<CourseSchedule>>('/mini/booking/available-courses', params, {
    showLoading: false
  })
}

/**
 * 获取我的预约列表
 * GET /mini/booking/my-bookings
 */
export const getMyBookings = (params: MyBookingParams) => {
  return wxRequest.get<PageResult<CourseBooking>>('/mini/booking/my-bookings', params, {
    showLoading: false
  })
}

/**
 * 获取预约详情
 * GET /mini/booking/detail/{bookingId}
 */
export const getBookingDetail = (bookingId: number) => {
  return wxRequest.get<CourseBooking>(`/mini/booking/detail/${bookingId}`, null, {
    showLoading: true
  })
}

/**
 * 创建预约
 * POST /mini/booking/create
 */
export const createBooking = (params: CreateBookingParams) => {
  return wxRequest.post('/mini/booking/create', params, {
    showLoading: true,
    loadingText: '预约中...'
  })
}

/**
 * 取消预约
 * POST /course/cancel/{bookingId}
 */
export const cancelBooking = ({ bookingId, reason }: CancelBookingParams) => {
  return wxRequest.post(`/course/cancel/${bookingId}`, null, {
    params: { reason },
    showLoading: true
  })
}

/**
 * 获取可预约时间段
 * GET /mini/booking/available-times/{courseId}
 */
export const getAvailableTimes = (courseId: number, date: string) => {
  return wxRequest.get<string[]>(`/mini/booking/available-times/${courseId}`, {
    date
  })
}

/**
 * 预约私教课
 * POST /course/book/private
 */
export const bookPrivateCourse = (params: {
  memberId: number
  coachId: number
  courseDate: string
  startTime: string
}) => {
  return wxRequest.post('/course/book/private', null, { params })
}

/**
 * 预约团课
 * POST /course/book/group
 */
export const bookGroupCourse = (params: {
  memberId: number
  scheduleId: number
}) => {
  return wxRequest.post('/course/book/group', null, { params })
}