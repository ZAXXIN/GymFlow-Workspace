import request from '@/utils/request'
import type { Course, QueryParams, PaginatedResponse, CourseBooking, ApiResponse } from '@/types'
import { ApiPaths } from '@/utils/constants'

export const courseApi = {
  /**
   * 获取课程列表
   */
  getCourses(params?: QueryParams): Promise<PaginatedResponse<Course>> {
    return http.get(ApiPaths.COURSES, { params })
  },

  /**
   * 获取课程详情
   */
  getCourseById(id: number): Promise<Course> {
    return http.get(ApiPaths.COURSE_DETAIL(id))
  },

  /**
   * 创建课程
   */
  createCourse(data: Partial<Course>): Promise<Course> {
    return http.post(ApiPaths.COURSES, data)
  },

  /**
   * 更新课程
   */
  updateCourse(id: number, data: Partial<Course>): Promise<Course> {
    return http.put(ApiPaths.COURSE_DETAIL(id), data)
  },

  /**
   * 删除课程
   */
  deleteCourse(id: number): Promise<void> {
    return http.delete(ApiPaths.COURSE_DETAIL(id))
  },

  /**
   * 获取课程安排
   */
  getSchedule(params?: QueryParams): Promise<any[]> {
    return http.get(ApiPaths.COURSE_SCHEDULE, { params })
  },

  /**
   * 获取课程预约列表
   */
  getCourseBookings(courseId: number): Promise<CourseBooking[]> {
    return http.get(`/courses/${courseId}/bookings`)
  },

  /**
   * 预约课程
   */
  bookCourse(courseId: number, memberId: number): Promise<CourseBooking> {
    return http.post(`/courses/${courseId}/book`, { memberId })
  },

  /**
   * 取消预约
   */
  cancelBooking(bookingId: number): Promise<void> {
    return http.delete(`/bookings/${bookingId}`)
  },

  /**
   * 导出课程数据
   */
  exportCourses(params?: QueryParams): Promise<Blob> {
    return http.get('/courses/export', {
      params,
      responseType: 'blob'
    })
  },

  /**
   * 批量创建课程
   */
  batchCreateCourses(data: Partial<Course>[]): Promise<Course[]> {
    return http.post('/courses/batch', data)
  }
}