// 课程相关API（复用PC接口）

import { wxRequest } from '../../utils/request'
import { Course, CourseSchedule, PageResult } from '../types/booking.types'

/**
 * 获取课程列表
 * GET /course/list
 */
export const getCourseList = (params: any) => {
  return wxRequest.get<PageResult<Course>>('/course/list', params)
}

/**
 * 获取课程详情
 * GET /course/detail/{courseId}
 */
export const getCourseDetail = (courseId: number) => {
  return wxRequest.get(`/course/detail/${courseId}`)
}

/**
 * 获取课程排课列表
 * GET /course/schedules/{courseId}
 */
export const getCourseSchedules = (courseId: number) => {
  return wxRequest.get<CourseSchedule[]>(`/course/schedules/${courseId}`)
}

/**
 * 获取课程表
 * GET /course/timetable
 */
export const getCourseTimetable = (startDate?: string, endDate?: string) => {
  return wxRequest.get<CourseSchedule[]>('/course/timetable', { startDate, endDate })
}