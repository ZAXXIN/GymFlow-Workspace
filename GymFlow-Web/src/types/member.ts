// 会员相关类型定义

// 分页查询参数
export interface MemberQueryDTO {
  pageNum?: number
  pageSize?: number
  memberNo?: string
  realName?: string
  phone?: string
  // status?: number
  startDate?: string // YYYY-MM-DD
  endDate?: string   // YYYY-MM-DD
}

// 分页结果
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

// 会员列表项
export interface MemberListVO {
  id: number
  memberNo: string
  username: string
  phone: string
  realName: string
  gender: number
  genderDesc: string
  age?: number
  birthday?: string
  personalCoachName?: string
  membershipStartDate?: string
  membershipEndDate?: string
  // status: number
  // statusDesc: string
  totalCheckins: number
  totalCourseHours: number
  totalSpent: number
  createTime: string
}

// 会员基本信息
export interface MemberBasicDTO {
  username: string
  password: string
  phone: string
  realName: string
  gender: number
  birthday?: string
  idCard?: string
  height?: number
  weight?: number
  address?: string
  department?: string
  position?: string
  personalCoachId?: number
}

// 健康档案
export interface HealthRecordDTO {
  recordDate: string
  weight: number
  bodyFatPercentage?: number
  muscleMass?: number
  bmi?: number
  chestCircumference?: number
  waistCircumference?: number
  hipCircumference?: number
  bloodPressure?: string
  heartRate?: number
  notes?: string
}

// 会员卡信息
export interface MemberCardDTO {
  productId?: number
  cardName: string
  cardType: number // 0-私教课，1-团课，2-月卡，3-年卡
  startDate: string
  endDate: string
  totalSessions?: number
  usedSessions?: number
  remainingSessions?: number
  amount: number
  quantity?: number
}

// 课程记录
export interface CourseRecordDTO {
  courseId: number
  courseName?: string
  coachName?: string
  courseDate?: string
  startTime?: string
  endTime?: string
  location?: string
  bookingStatus: number // 0-待上课，1-已签到，2-已完成，3-已取消
  checkinTime?: string
}

// 签到记录
export interface CheckinRecordDTO {
  checkinTime: string
  checkinMethod: number // 0-教练，1-前台
  courseName?: string
  coachName?: string
  notes?: string
}

// 完整会员信息
export interface MemberFullDTO {
  // 会员基本信息
  id: number
  userId: number
  memberNo: string
  username: string
  phone: string
  realName: string
  gender: number
  createTime: string
  // status: number
  
  // 扩展信息
  idCard?: string
  height?: number
  weight?: number
  membershipStartDate?: string
  membershipEndDate?: string
  personalCoachName?: string
  totalCheckins: number
  totalCourseHours: number
  totalSpent: number
  address?: string
  department?: string
  position?: string
  
  // 关联信息
  healthRecords: HealthRecordDTO[]
  memberCards: MemberCardDTO[]
  courseRecords: CourseRecordDTO[]
  checkinRecords: CheckinRecordDTO[]
}

// 添加会员请求
export interface MemberAddRequest {
  basicDTO: MemberBasicDTO
  healthRecordDTO?: HealthRecordDTO
  cardDTO?: MemberCardDTO
}

// 更新会员请求
export interface MemberUpdateRequest {
  basicDTO: MemberBasicDTO
  healthRecordDTO?: HealthRecordDTO
  cardDTO?: MemberCardDTO
}

// 会员统计数据
export interface MemberStats {
  totalMembers: number
  activeMembers: number
  newMembersToday: number
  expiringMembers: number
  genderDistribution: Array<{
    gender: string
    count: number
  }>
  ageDistribution: Array<{
    range: string
    count: number
  }>
  membershipDistribution: Array<{
    type: string
    count: number
  }>
}