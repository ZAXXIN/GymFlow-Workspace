// 会员相关类型定义
// cardType 0-会籍卡，1-私教课，2-团课

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
  birthday?: string
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

// 小程序端会员卡DTO
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

// 健康档案
export interface HealthRecordDTO {
  id?: number
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

// 课程记录（添加 sessionCost）
export interface CourseRecordDTO {
  courseId: number
  courseName?: string
  coachName?: string
  courseDate?: string
  startTime?: string
  endTime?: string
  sessionCost?: number  // 新增：消耗课时数
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

// 完整会员信息
export interface MemberFullDTO {
  id: number
  memberNo: string
  username: string
  phone: string
  realName: string
  gender: number
  birthday?: string
  age?: number  // 新增
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
  checkinRecords: CheckinRecordDTO[]
}

// 我的课程包
export interface MyCourseDTO {
  orderItemId: number
  productId: number
  productName: string
  productType: number
  productTypeDesc: string
  totalSessions: number
  remainingSessions: number
  usedSessions: number
  validityStartDate?: string
  validityEndDate?: string
  status: string
  statusDesc: string
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

// 会员基本信息
export interface MemberBasicDTO {
  phone: string
  password?: string
  realName: string
  gender: number
  birthday?: string
}

// 会员卡信息
export interface MemberCardDTO {
  productId?: number
  productName?: string
  cardType: number
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