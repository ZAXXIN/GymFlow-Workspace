import request from '@/utils/request'
import type {
  CourseQueryParams,
  CourseBasicDTO,
  CourseScheduleDTO,
  CourseDetail,
  CourseListVO,
  PageResultVO,
  CourseScheduleVO
} from '@/types/course'
import type { ApiResponse } from '@/types/auth'

// 课程管理API
export const courseApi = {
  /**
   * 分页查询课程列表
   */
  getCourseList(params: CourseQueryParams): Promise<ApiResponse<PageResultVO<CourseListVO>>> {
    return request({
      url: '/course/list',
      method: 'GET',
      params
    })
  },

  /**
   * 获取课程详情
   */
  getCourseDetail(courseId: number): Promise<ApiResponse<CourseDetail>> {
    return request({
      url: `/course/detail/${courseId}`,
      method: 'GET'
    })
  },

  /**
   * 添加课程
   */
  addCourse(data: CourseBasicDTO): Promise<ApiResponse<number>> {
    return request({
      url: '/course/add',
      method: 'POST',
      data
    })
  },

  /**
   * 更新课程
   */
  updateCourse(courseId: number, data: CourseBasicDTO): Promise<ApiResponse> {
    return request({
      url: `/course/update/${courseId}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 删除课程
   */
  deleteCourse(courseId: number): Promise<ApiResponse> {
    return request({
      url: `/course/delete/${courseId}`,
      method: 'DELETE'
    })
  },

  /**
   * 更新课程状态
   */
  updateCourseStatus(courseId: number, status: number): Promise<ApiResponse> {
    return request({
      url: `/course/updateStatus/${courseId}`,
      method: 'PUT',
      params: { status }
    })
  },

  /**
   * 课程排课（团课）
   */
  scheduleCourse(data: CourseScheduleDTO): Promise<ApiResponse> {
    return request({
      url: '/course/schedule',
      method: 'POST',
      data
    })
  },

  /**
   * 删除排课
   */
  deleteCourseSchedule(scheduleId: number): Promise<ApiResponse> {
    return request({
      url: `/course/schedule/${scheduleId}`,
      method: 'DELETE'
    })
  },

  /**
   * 获取课程排课列表
   */
  getCourseSchedules(courseId: number): Promise<ApiResponse<CourseScheduleVO[]>> {
    return request({
      url: `/course/schedules/${courseId}`,
      method: 'GET'
    })
  },

  /**
   * 获取课程表
   */
  getCourseTimetable(startDate?: string, endDate?: string): Promise<ApiResponse<CourseScheduleVO[]>> {
    return request({
      url: '/course/timetable',
      method: 'GET',
      params: { startDate, endDate }
    })
  },

  /**
   * 会员预约私教课
   */
  bookPrivateCourse(memberId: number, coachId: number, courseDate: string, startTime: string): Promise<ApiResponse> {
    return request({
      url: '/course/book/private',
      method: 'POST',
      params: { memberId, coachId, courseDate, startTime }
    })
  },

  /**
   * 会员预约团课
   */
  bookGroupCourse(memberId: number, scheduleId: number): Promise<ApiResponse> {
    return request({
      url: '/course/book/group',
      method: 'POST',
      params: { memberId, scheduleId }
    })
  },

  /**
   * 核销课程预约
   */
  verifyCourseBooking(bookingId: number, checkinMethod: number): Promise<ApiResponse> {
    return request({
      url: `/course/verify/${bookingId}`,
      method: 'POST',
      params: { checkinMethod }
    })
  },

  /**
   * 取消课程预约
   */
  cancelCourseBooking(bookingId: number, reason: string): Promise<ApiResponse> {
    return request({
      url: `/course/cancel/${bookingId}`,
      method: 'POST',
      params: { reason }
    })
  }
}