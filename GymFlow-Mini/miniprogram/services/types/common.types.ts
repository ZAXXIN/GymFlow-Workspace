// 通用类型定义

// 分页参数
export interface PageParams {
  pageNum?: number
  pageSize?: number
}

// 分页响应
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

// API响应
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 签到规则配置
export interface CheckinRules {
  checkinStartMinutes: number // 课程开始前多少分钟可签到
  checkinEndMinutes: number // 课程开始后多少分钟截止签到
  autoCompleteHours: number // 课程结束后多少小时自动变更为已完成
}

// 健康档案
export interface HealthRecord {
  id?: number
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

// 会员卡
export interface MemberCard {
  productId: number
  productName: string
  productType: ProductType
  cardType?: number
  startDate?: string
  endDate?: string
  totalSessions?: number
  usedSessions?: number
  remainingSessions?: number
  amount?: number
  status: 'ACTIVE' | 'EXPIRED' | 'USED_UP' | 'UNPAID'
}

// 搜索参数
export interface SearchParams {
  keyword: string
  pageNum?: number
  pageSize?: number
  type?: 'product' | 'course' | 'all'
}