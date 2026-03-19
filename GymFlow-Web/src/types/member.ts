// 会员相关类型定义

// 分页查询参数
export interface MemberQueryDTO {
  pageNum?: number
  pageSize?: number
  memberNo?: string
  realName?: string
  phone?: string
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

// 会员基本信息
export interface MemberBasicDTO {
  id?: number
  phone: string
  password?: string
  realName: string
  gender: number
  birthday: string
  memberNo: string
  membershipStartDate: string
  membershipEndDate: string
}

// 健康档案
export interface HealthRecordDTO {
  recordDate: string
  height: number
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

// 商品选项（用于会员卡选择）
export interface ProductOption {
  id: number
  productName: string
  productType: number
  productTypeDesc: string
  currentPrice: number
  originalPrice: number
  validityDays?: number
  totalSessions?: number  // 课程卡的总课时数
  description?: string
}

// 会员卡信息
export interface MemberCardDTO {
  productId?: number
  productName?: string
  cardType: number // 0-私教课，1-团课，2-月卡，3-年卡，4-周卡，5-其他
  cardTypeDesc?: string
  startDate: string
  endDate: string
  totalSessions?: number    // 总课时数（仅私教课、团课）
  usedSessions?: number      // 已用课时数（仅编辑模式显示）
  remainingSessions?: number // 剩余课时数（仅私教课、团课）
  amount: number
  status?: string
  statusDesc?: string
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

// 完整会员信息
export interface MemberFullDTO {
  id: number
  memberNo: string
  username: string
  phone: string
  realName: string
  gender: number
  createTime: string
  membershipStartDate?: string
  membershipEndDate?: string
  totalCheckins: number
  totalCourseHours: number
  totalSpent: number
  
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