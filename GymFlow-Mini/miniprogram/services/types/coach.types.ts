// 教练端相关类型定义

// 教练信息
export interface CoachInfo {
  id: number
  realName: string
  phone: string
  specialty?: string
  yearsOfExperience?: number
  hourlyRate?: number
  commissionRate?: number
  rating?: number
  introduction?: string
  certifications?: string[]
  totalStudents?: number
  totalCourses?: number
  totalIncome?: number
  status: 0 | 1
  createTime: string
  updateTime: string
}

// 教练排班
export interface CoachSchedule {
  scheduleId: number
  scheduleDate: string
  startTime: string
  endTime: string
  scheduleType: 0 | 1
  scheduleTypeDesc: string
  status: string
  notes?: string
}

// 教练课程
export interface CoachCourse {
  id: number
  courseName: string
  courseType: 0 | 1
  courseDate: string
  startTime: string
  endTime: string
  duration: number
  location: string
  maxCapacity: number
  currentEnrollment: number
  status: 0 | 1
  students?: CourseStudent[]
}

// 课程学员
export interface CourseStudent {
  memberId: number
  memberName: string
  memberPhone: string
  memberNo: string
  bookingId: number
  bookingStatus: number
  checkinTime?: string
}

// 教练端会员详情
export interface CoachMemberDetail {
  memberId: number
  memberName: string
  memberPhone: string
  memberNo: string
  membershipStartDate?: string
  membershipEndDate?: string
  memberCard?: MemberCard
  healthRecords?: HealthRecord[]
  courseRecords?: MemberCourseRecord[]
}

// 会员课程记录（教练视角）
export interface MemberCourseRecord {
  courseId: number
  courseName: string
  courseDate: string
  startTime: string
  endTime: string
  bookingId: number
  bookingStatus: number
  checkinTime?: string
  statusDesc: string
}

// 财务统计参数
export interface FinanceStatsParams {
  period: 'day' | 'month' | 'year'
  date?: string // 指定日期，默认当前
}

// 财务统计数据点
export interface FinanceDataPoint {
  date: string
  label: string
  sessions: number
  revenue: number
  members: number
}

// 财务统计响应
export interface FinanceStatsResult {
  period: 'day' | 'month' | 'year'
  startDate: string
  endDate: string
  totalSessions: number
  totalRevenue: number
  totalMembers: number
  dataPoints: FinanceDataPoint[]
}