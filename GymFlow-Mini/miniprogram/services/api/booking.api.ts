// services/api/booking.api.ts
// 预约相关API - 只负责接口调用

import { wxRequest } from '../../utils/request'

/**
 * 获取课程表（所有排课，包含团课和私教课）
 * GET /course/timetable
 * 用于获取可预约课程列表
 */
export const getCourseTimetable = (params?: { startDate?: string; endDate?: string }) => {
  return wxRequest.get('/course/timetable', params)
}

/**
 * 获取单个排课详情
 * GET /course/schedule/{scheduleId}
 * 用于预约确认页面获取详细信息
 */
export const getCourseScheduleDetail = (scheduleId: number) => {
  return wxRequest.get(`/course/schedule/${scheduleId}`)
}

/**
 * 预约团课
 * POST /course/book/group
 * 后端使用 @RequestParam，所以用 params 传递
 */
export const bookGroupCourse = (memberId: number, scheduleId: number) => {
  return wxRequest.post('/course/book/group', null, {
    params: { memberId, scheduleId }
  })
}

/**
 * 预约私教课
 * POST /course/book/private
 * 后端使用 @RequestParam，所以用 params 传递
 */
export const bookPrivateCourse = (
  memberId: number,
  coachId: number,
  courseDate: string,
  startTime: string
) => {
  return wxRequest.post('/course/book/private', null, {
    params: { memberId, coachId, courseDate, startTime }
  })
}

/**
 * 取消预约
 * POST /course/cancel/{bookingId}
 * 后端使用 @RequestParam，所以用 params 传递
 */
export const cancelBooking = (bookingId: number, reason?: string) => {
  return wxRequest.post(`/course/cancel/${bookingId}`, null, {
    params: { reason }
  })
}

/**
 * 获取会员的预约列表
 * GET /course/booking/member/{memberId}
 */
export const getMemberBookings = (
  memberId: number,
  params?: Record<string, any>
) => {
  return wxRequest.get(`/course/booking/member/${memberId}`, params)
}