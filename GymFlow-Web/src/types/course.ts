import type { CoachBasicDTO } from './coach'

// 课程基础信息DTO
export interface CourseBasicDTO {
  courseType: number
  courseName: string
  description?: string
  coachIds: number[]
  maxCapacity: number
  duration: number
  price: number
  location?: string
  notice?: string
}

// 课程查询DTO
export interface CourseQueryParams {
  pageNum?: number
  pageSize?: number
  courseType?: number
  courseName?: string
  coachId?: number
  startDate?: string
  endDate?: string
  status?: number
}

// 课程排课DTO
export interface CourseScheduleDTO {
  courseId: number
  coachId: number
  courseDate: string
  startTime: string
  endTime: string
  maxParticipants?: number
  notes?: string
}

// 课程列表VO
export interface CourseListVO {
  id: number
  courseType: number
  courseTypeDesc: string
  courseName: string
  description?: string
  coachNames: string[]
  maxCapacity: number
  currentEnrollment: number
  enrollmentRate: number
  courseDate?: string
  startTime?: string
  endTime?: string
  duration: number
  price: number
  location?: string
  status: number
  statusDesc: string
  createTime?: string
  updateTime?: string
}

// 课程详情
export interface CourseDetail {
  id: number
  courseType: number
  courseTypeDesc: string
  courseName: string
  description?: string
  coaches: CoachBasicDTO[]
  maxCapacity: number
  currentEnrollment: number
  courseDate?: string
  startTime?: string
  endTime?: string
  duration: number
  price: number
  location?: string
  status: number
  statusDesc: string
  notice?: string
  createTime?: string
  updateTime?: string
  bookings: CourseBookingDTO[]
}

// 课程预约DTO
export interface CourseBookingDTO {
  id: number
  memberId: number
  memberName: string
  memberPhone: string
  bookingTime: string
  bookingStatus: number
  bookingStatusDesc: string
  checkinTime?: string
  cancellationReason?: string
  cancellationTime?: string
}

// 课程排课VO
export interface CourseScheduleVO {
  scheduleId: number
  courseId: number
  courseName: string
  courseType: string
  coachId: number
  coachName: string
  scheduleDate: string
  startTime: string
  endTime: string
  maxParticipants: number
  currentParticipants: number
  remainingSlots: number
  status: string
  notes?: string
  bookings: CourseBookingVO[]
}

// 课程预约VO
export interface CourseBookingVO {
  id: number
  memberName: string
  memberPhone: string
  bookingStatusDesc: string
  bookingTime: string
  checkinTime?: string
}

// 分页结果
export interface PageResultVO<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages?: number
}

// API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: string
}