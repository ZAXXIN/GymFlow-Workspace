// 会员相关类型定义

// 分页查询参数
export interface MemberQueryDTO {
  pageNum?: number
  pageSize?: number
  memberNo?: string
  realName?: string
  phone?: string
  startDate?: string
  endDate?: string
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
  phone: string
  realName: string
  gender: number
  age?: number
  birthday?:string
  membershipStartDate?: string
  membershipEndDate?: string
  totalCheckins: number
  totalCourseHours: number
  totalSpent: number
  createTime: string
  
  // 会员卡信息
  cardType?: number
  cardTypeDesc?: string
  cardStatus?: string
  cardStatusDesc?: string
  cardEndDate?: string
  remainingSessions?: number
}

// 小程序端会员卡DTO（后端返回的格式）
export interface MiniMemberCardDTO {
  productId: number
  productName: string
  cardType: number
  cardTypeDesc: string
  startDate?: string
  endDate?: string
  totalSessions?: number
  usedSessions?: number
  remainingSessions?: number
  status: string
  statusDesc: string
}

// 健康档案（从 health_record 表）
export interface HealthRecordDTO {
  recordDate: string
  height?: number
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

// 课程记录
export interface CourseRecordDTO {
  courseId: number
  courseName?: string
  coachName?: string
  courseDate?: string
  startTime?: string
  endTime?: string
  location?: string
  bookingStatus: number
  checkinTime?: string
}

// 签到记录
export interface CheckinRecordDTO {
  checkinTime: string
  checkinMethod: number
  courseName?: string
  coachName?: string
  notes?: string
}

// 完整会员信息（与后端 MemberFullDTO 对应）
export interface MemberFullDTO {
  id: number
  memberNo: string
  username: string
  phone: string
  realName: string
  gender: number
  birthday?: string  // 添加 birthday
  createTime: string
  membershipStartDate?: string
  membershipEndDate?: string
  totalCheckins: number
  totalCourseHours: number
  totalSpent: number
  healthRecords: HealthRecordDTO[]
  memberCards: MiniMemberCardDTO[]
  courses: MyCourseDTO[]
  courseRecords: CourseRecordDTO[]
  checkinRecords: CheckInRecordDTO[]
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

// 会员基本信息（用于新增/编辑表单）
export interface MemberBasicDTO {
  phone: string
  password?: string
  realName: string
  gender: number
  birthday?: string
}

// 会员卡信息（用于新增/编辑表单）
export interface MemberCardDTO {
  productId?: number
  productName?: string
  cardType: number  // 0-私教课,1-团课,2-月卡,3-年卡,4-周卡,5-其他
  startDate?: string
  endDate?: string
  totalSessions?: number
  remainingSessions?: number
  amount?: number
}

// 会员统计数据
export interface MemberStats {
  totalMembers: number
  activeMembers: number
  newMembersToday: number
  expiringMembers: number
}