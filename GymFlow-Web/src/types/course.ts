import type { CoachBasicDTO } from './coach'

// 课程基础信息DTO
export interface CourseBasicDTO {
  courseType: number
  courseName: string
  description?: string
  coachIds: number[]
  duration: number
  sessionCost: number  // 改为 sessionCost，移除 price
  notice?: string
}

// 课程查询DTO
export interface CourseQueryParams {
  pageNum?: number
  pageSize?: number
  courseType?: number
  courseName?: string
  coachId?: number
  status?: number
}

// 课程排课DTO
export interface CourseScheduleDTO {
  courseId: number
  coachId: number
  scheduleDate: string
  startTime: string
  endTime: string
  maxCapacity?: number
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
  sessionCost: number  // 新增
  status: number
  statusDesc: string
  // 统计信息
  totalSchedules: number    // 总排课数
  totalBookings: number     // 总预约数
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
  sessionCost: number  // 新增
  status: number
  statusDesc: string
  notice?: string
  createTime?: string
  updateTime?: string
  schedules: CourseScheduleVO[]
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
  signCode?: string
  signCodeExpireTime?: string
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
  bookings: CourseBookingVO[]
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