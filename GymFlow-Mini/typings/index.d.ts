// 全局类型定义

/// <reference types="miniprogram-api-typings" />

declare interface IAppOption {
  globalData: {
    systemInfo: wx.SystemInfo | null
  }
  userStore?: UserStore
  configStore?: ConfigStore
  messageStore?: MessageStore
}

// 用户相关类型
declare namespace GymFlow {
  // 用户类型
  type UserRole = 'MEMBER' | 'COACH'
  
  // 用户基本信息
  interface UserInfo {
    id: number
    phone: string
    realName: string
    role: UserRole
    token: string
  }
  
  // 会员信息
  interface MemberInfo extends UserInfo {
    role: 'MEMBER'
    memberNo: string
    memberId: number
    membershipStartDate?: string
    membershipEndDate?: string
    personalCoachId?: number
    personalCoachName?: string
    totalCheckins?: number
    totalCourseHours?: number
    totalSpent?: number
    height?: number
    weight?: number
    birthday?: string
    gender?: 0 | 1
    idCard?: string
    address?: string
  }
  
  // 教练信息
  interface CoachInfo extends UserInfo {
    role: 'COACH'
    coachId: number
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
  }
  
  // 登录参数
  interface LoginParams {
    phone: string
    password: string
  }
  
  // 登录响应
  interface LoginResult {
    userId: number
    username: string
    realName: string
    phone: string
    role: number
    token: string
    loginTime: string
    memberId?: number
    coachId?: number
  }
  
  // 小程序登录响应
  export interface MiniLoginResult {
    userId: number
    userType: number  // 0-会员，1-教练
    phone: string
    realName: string
    memberNo?: string
    token: string
    loginTime: string
    role?: 'MEMBER' | 'COACH'  // 前端转换后的角色
    memberInfo?: MemberInfo
    coachInfo?: CoachInfo
  }
  
  // 分页参数
  interface PageParams {
    pageNum?: number
    pageSize?: number
  }
  
  // 分页响应
  interface PageResult<T> {
    list: T[]
    total: number
    pageNum: number
    pageSize: number
    pages: number
  }
  
  // 商品类型
  type ProductType = 0 | 1 | 2 | 3 // 0-会籍卡 1-私教课 2-团课 3-相关产品
  
  // 商品
  interface Product {
    id: number
    productName: string
    productType: ProductType
    productTypeDesc: string
    categoryId?: number
    categoryName?: string
    description?: string
    images: string[]
    originalPrice: number
    currentPrice: number
    stockQuantity: number
    salesVolume?: number
    unit?: string
    validityDays?: number
    specifications?: string
    status: 0 | 1
    statusDesc: string
    createTime: string
    updateTime: string
    detail?: ProductDetail
  }
  
  // 商品详情
  interface ProductDetail {
    membershipType?: number
    coachId?: number
    courseDuration?: number
    totalSessions?: number
    availableSessions?: number
    courseLevel?: string
    membershipBenefits?: string[]
    validityDays?: number
    defaultTotalSessions?: number
    maxPurchaseQuantity?: number
    refundPolicy?: string
    usageRules?: string
    coachIds?: number[]
  }
  
  // 商品分类
  interface ProductCategory {
    id: number
    categoryName: string
    parentId: number
    status: 0 | 1
    statusDesc: string
    createTime: string
    updateTime: string
    children?: ProductCategory[]
  }
  
  // 课程
  interface Course {
    id: number
    courseType: 0 | 1
    courseTypeDesc: string
    courseName: string
    description?: string
    coachId: number
    coachName?: string
    maxCapacity: number
    currentEnrollment: number
    courseDate: string
    startTime: string
    endTime: string
    duration: number
    price: number
    location?: string
    status: 0 | 1
    statusDesc: string
    createTime: string
    updateTime: string
  }
  
  // 课程排课
  interface CourseSchedule {
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
  
  // 预约状态
  type BookingStatus = 0 | 1 | 2 | 3 // 0-待上课 1-已签到 2-已完成 3-已取消
  
  // 课程预约
  interface CourseBooking {
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
    courseDate?: string
    startTime?: string
    endTime?: string
    location?: string
    checkinCode?: string // 签到码（6位数字）
  }
  
  // 订单状态
  type OrderStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'CANCELLED' | 'REFUNDED' | 'DELETED'
  type PaymentStatus = 0 | 1 // 0-待支付 1-已支付
  type OrderType = 0 | 1 | 2 | 3 // 0-会籍卡 1-私教课 2-团课 3-相关产品
  
  // 订单
  interface Order {
    id: number
    orderNo: string
    memberId: number
    memberName?: string
    memberPhone?: string
    orderType: OrderType
    orderTypeDesc: string
    totalAmount: number
    actualAmount: number
    paymentMethod?: string
    paymentStatus: PaymentStatus
    paymentStatusDesc: string
    paymentTime?: string
    orderStatus: OrderStatus
    orderStatusDesc: string
    remark?: string
    createTime: string
    updateTime: string
    items?: OrderItem[]
  }
  
  // 订单项
  interface OrderItem {
    id: number
    productId: number
    productName: string
    productType: ProductType
    quantity: number
    unitPrice: number
    totalPrice: number
    validityStartDate?: string
    validityEndDate?: string
    totalSessions?: number
    remainingSessions?: number
    status: string
    productImage?: string
  }
  
  // 签到记录
  interface CheckinRecord {
    id: number
    memberId: number
    memberName?: string
    memberPhone?: string
    memberNo?: string
    checkinTime: string
    checkinMethod: 0 | 1
    checkinMethodDesc: string
    courseBookingId?: number
    courseName?: string
    coachName?: string
    notes?: string
    createTime: string
  }
  
  // 健康档案
  interface HealthRecord {
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
  interface MemberCard {
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
  
  // 消息
  interface Message {
    id: number
    type: 'BOOKING' | 'CHECKIN' | 'SYSTEM'
    title: string
    content: string
    isRead: 0 | 1
    createTime: string
    data?: any
  }
  
  // 签到规则配置
  interface CheckinRules {
    checkinStartMinutes: number // 课程开始前多少分钟可签到
    checkinEndMinutes: number // 课程开始后多少分钟截止签到
    autoCompleteHours: number // 课程结束后多少小时自动变更为已完成
  }
  
  // 财务统计数据点
  interface FinanceDataPoint {
    date: string
    label: string
    sessions: number
    revenue: number
    members: number
  }
  
  // 财务统计
  interface FinanceStats {
    period: 'day' | 'month' | 'year'
    startDate: string
    endDate: string
    totalSessions: number
    totalRevenue: number
    totalMembers: number
    dataPoints: FinanceDataPoint[]
  }
}

// 存储类声明
declare class UserStore {
  userInfo: GymFlow.UserInfo | null
  memberInfo: GymFlow.MemberInfo | null
  coachInfo: GymFlow.CoachInfo | null
  isLogin: boolean
  role: GymFlow.UserRole | null
  
  setUserInfo(userInfo: GymFlow.UserInfo): void
  setMemberInfo(info: GymFlow.MemberInfo): void
  setCoachInfo(info: GymFlow.CoachInfo): void
  logout(): void
  getToken(): string | null
}

declare class ConfigStore {
  checkinRules: GymFlow.CheckinRules | null
  
  setCheckinRules(rules: GymFlow.CheckinRules): void
  canCheckin(courseDate: string, startTime: string): boolean
}

declare class MessageStore {
  unreadCount: number
  messages: GymFlow.Message[]
  
  setUnreadCount(count: number): void
  incrementUnread(): void
  clearUnread(): void
  setMessages(messages: GymFlow.Message[]): void
  markAsRead(messageId: number): void
}