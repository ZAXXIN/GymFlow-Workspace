import type { CoachBasicDTO } from './coach'

// 课程基础信息DTO
export interface CourseBasicDTO {
  courseType: number
  courseName: string
  description?: string
  coachIds: number[]
  duration: number
  price: number
  notice?: string
  // 移除 maxCapacity 和 location
}

// 课程查询DTO
export interface CourseQueryParams {
  pageNum?: number
  pageSize?: number
  courseType?: number
  courseName?: string
  coachId?: number
  status?: number
  // 移除 startDate 和 endDate
}

// 课程排课DTO
export interface CourseScheduleDTO {
  courseId: number
  coachId: number
  scheduleDate: string      // 改名，从 courseDate 改为 scheduleDate
  startTime: string
  endTime: string
  maxCapacity?: number      // 改名，从 maxParticipants 改为 maxCapacity
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
  duration: number
  price: number
  status: number
  statusDesc: string
  // 统计信息
  totalSchedules: number    // 总排课数
  totalBookings: number     // 总预约数
  // 移除 maxCapacity, currentEnrollment, enrollmentRate, courseDate, startTime, endTime, location, createTime, updateTime
}

// 课程详情
export interface CourseDetail {
  id: number
  courseType: number
  courseTypeDesc: string
  courseName: string
  description?: string
  coaches: CoachBasicDTO[]
  duration: number
  price: number
  status: number
  statusDesc: string
  notice?: string
  createTime?: string
  updateTime?: string
  schedules: CourseScheduleVO[]  // 改为 schedules，包含排课信息
  // 移除 maxCapacity, currentEnrollment, courseDate, startTime, endTime, location, bookings
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
  signCode?: string          // 签到码
  signCodeExpireTime?: string // 签到码过期时间
  cancellationReason?: string
  cancellationTime?: string
}

// 课程排课VO
export interface CourseScheduleVO {
  scheduleId: number
  courseId: number
  courseName: string
  courseType: number
  courseTypeDesc?: string
  coachId: number
  coachName: string
  scheduleDate: string
  startTime: string
  endTime: string
  maxCapacity: number
  currentEnrollment: number
  remainingSlots: number
  status: number
  statusDesc: string
  notes?: string
  bookings: CourseBookingVO[]    // 该排课的预约列表
}

// 课程预约VO（用于排课详情）
export interface CourseBookingVO {
  bookingId: number
  memberId: number
  memberName: string
  memberPhone: string
  memberNo?: string
  bookingTime: string
  bookingStatus: number
  bookingStatusDesc: string
  checkinTime?: string
  signCode?: string
  signCodeExpireTime?: string
  cancellationReason?: string
  cancellationTime?: string
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