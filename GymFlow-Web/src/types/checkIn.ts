// src/types/checkIn.ts

// 签到查询参数
export interface CheckInQueryParams {
  pageNum?: number
  pageSize?: number
  memberId?: number
  memberName?: string
  checkinMethod?: number
  startDate?: string
  endDate?: string
  hasCourseBooking?: boolean
}

// 签到列表项VO
export interface CheckInListVO {
  id: number
  memberId: number
  memberName: string
  memberPhone: string
  memberNo: string
  checkinTime: string
  checkinMethod: number
  checkinMethodDesc: string
  notes?: string
  createTime: string
  
  // 课程签到相关字段
  courseCheckIn: boolean
  courseBookingId?: number
  courseName?: string
  courseType?: number
  courseTypeDesc?: string
  coachName?: string
}

// 签到详情VO
export interface CheckInDetailVO {
  id: number
  memberId: number
  memberName: string
  memberPhone: string
  memberNo: string
  checkinTime: string
  checkinMethod: number
  checkinMethodDesc: string
  notes?: string
  createTime: string
  
  // 会员信息
  gender?: number
  genderDesc?: string
  personalCoachName?: string
  
  // 课程预约信息（如果有关联）
  courseBookingId?: number
  bookingTime?: string
  bookingStatus?: number
  bookingStatusDesc?: string
  
  // 课程信息
  courseId?: number
  courseName?: string
  courseType?: number
  courseTypeDesc?: string
  courseDate?: string
  startTime?: string
  endTime?: string
  location?: string
  
  // 教练信息
  coachName?: string
  coachPhone?: string
}

// 签到统计DTO
export interface CheckInStatsDTO {
  totalCheckIns: number
  courseCheckIns: number
  freeCheckIns: number
  coachCheckIns: number
  frontDeskCheckIns: number
  uniqueMembers: number
  checkInRate: number
}

// 签到报表DTO
export interface CheckInReportDTO {
  totalCheckIns: number
  courseCheckIns: number
  freeCheckIns: number
  coachCheckIns: number
  frontDeskCheckIns: number
  uniqueMembers: number
  avgCheckInsPerMember: number
  dailyTrend: Record<string, number>
  memberRanking: Array<{
    rank: number
    memberId: number
    memberName: string
    memberPhone: string
    memberNo: string
    checkinCount: number
  }>
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