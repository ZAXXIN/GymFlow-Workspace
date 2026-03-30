// 预约相关类型定义

// 预约状态
export type BookingStatus = 0 | 1 | 2 | 3 // 0-待上课 1-已签到 2-已完成 3-已取消

// 前端类型定义
export interface MiniAvailableCourseDTO {
  scheduleId: number;           // 排课ID（新增）
  courseId: number;             // 课程ID
  courseName: string;           // 课程名称
  courseType: number;           // 课程类型
  coachId: number;              // 教练ID
  coachName: string;            // 教练姓名
  scheduleDate: string;         // 上课日期
  startTime: string;            // 开始时间
  endTime: string;              // 结束时间
  maxCapacity: number;          // 最大容量
  currentEnrollment: number;    // 当前报名人数
  remainingSlots: number;       // 剩余名额
  price: number;                // 价格
}

// 课程
export interface Course {
  id: number
  courseType: 0 | 1
  courseTypeDesc: string
  courseName: string
  description?: string
  coachId: number
  coachName?: string
  maxCapacity: number
  currentEnrollment: number
  scheduleDate: string
  startTime: string
  endTime: string
  duration: number
  price: number
  status: 0 | 1
  statusDesc: string
  createTime: string
  updateTime: string
}

// 课程排课
export interface CourseSchedule {
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
  bookings?: CourseBooking[]
}

// 课程预约
export interface CourseBooking {
  id: number
  memberId: number
  memberName?: string
  memberPhone?: string
  courseId: number
  courseName?: string
  coachId?: number
  coachName?: string
  bookingTime: string
  bookingStatus: BookingStatus
  bookingStatusDesc: string
  checkinTime?: string
  cancellationReason?: string
  cancellationTime?: string
  scheduleDate?: string
  startTime?: string
  endTime?: string
  checkinCode?: string // 签到码（6位数字）
}

// 可预约课程查询参数
export interface AvailableCourseParams {
  pageNum?: number
  pageSize?: number
  courseName?: string
  coachId?: number
  startDate?: string
  endDate?: string
}

// 我的预约查询参数
export interface MyBookingParams {
  pageNum?: number
  pageSize?: number
  status?: BookingStatus
}

// 创建预约参数
export interface CreateBookingParams {
  memberId: number
  scheduleId: number
}

// 取消预约参数
export interface CancelBookingParams {
  bookingId: number
  reason: string
}