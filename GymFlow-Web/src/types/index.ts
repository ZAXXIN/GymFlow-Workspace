// 用户相关类型
export interface User {
  id: number
  username: string
  email: string
  phone: string
  avatar?: string
  role: UserRole
  status: number
  createdAt: string
  updatedAt: string
}

export enum UserRole {
  MEMBER = 'MEMBER',
  COACH = 'COACH',
  ADMIN = 'ADMIN'
}

// 会员相关类型
export interface Member {
  id: number
  userId: number
  memberNo: string
  realName: string
  gender: number
  birthDate?: string
  height?: number
  weight?: number
  bmi?: number
  fitnessGoal?: string
  medicalHistory?: string
  emergencyContact?: string
  emergencyPhone?: string
  status: MemberStatus
  remainingSessions: number
  totalSessions: number
  membershipStartDate: string
  membershipEndDate: string
  createdAt: string
  updatedAt: string
  
  // 关联数据
  user?: User
}

export enum MemberStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  EXPIRED = 'EXPIRED'
}

// 会员相关类型
export interface MemberFormData {
  id?: string | number
  name: string
  phone: string
  gender: 'MALE' | 'FEMALE'
  birthday: string
  email: string
  status: MemberStatus
  address: string
  height: number
  weight: number
  bmi: string
  bodyFatRate: number
  healthNote: string
  memberCard: MemberCard | null
}

export interface MemberCard {
  cardType: 'YEAR' | 'QUARTER' | 'MONTH' | 'TIMES' | 'PERSONAL'
  cardNumber: string
  startDate: string
  endDate: string
  remainingTimes: number
  totalTimes: number
  note: string
}

export type MemberStatus = 'ACTIVE' | 'FROZEN' | 'EXPIRED' | 'CANCELLED'

// 教练相关类型
export interface Coach {
  id: number
  userId: number
  coachNo: string
  realName: string
  gender: number
  birthDate?: string
  specialization: string
  certification?: string
  yearsOfExperience: number
  hourlyRate: number
  commissionRate: number
  totalSessions: number
  totalSales: number
  rating: number
  status: CoachStatus
  createdAt: string
  updatedAt: string
  
  // 关联数据
  user?: User
}

export enum CoachStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  ON_LEAVE = 'ON_LEAVE'
}

// 教练相关类型
export interface CoachFormData {
  id?: string | number
  name: string
  englishName: string
  phone: string
  email: string
  gender: 'MALE' | 'FEMALE'
  birthday: string
  introduction: string
  avatar: string
  status: CoachStatus
  level: CoachLevel
  experienceYears: number
  certificates: string[]
  specialties: string[]
  teachingPhilosophy: string
  hourlyRate: number
  commissionRate: number
  maxStudents: number
  schedule: ScheduleData
}

export type CoachStatus = 'ACTIVE' | 'ON_LEAVE' | 'RESIGNED'
export type CoachLevel = 'JUNIOR' | 'INTERMEDIATE' | 'SENIOR' | 'STAR' | 'DIRECTOR'

export interface ScheduleData {
  morning: boolean[]
  afternoon: boolean[]
  evening: boolean[]
}

// 课程相关类型
export interface Course {
  id: number
  courseNo: string
  name: string
  description?: string
  category: CourseCategory
  difficulty: CourseDifficulty
  duration: number
  capacity: number
  currentBookings: number
  price: number
  coachId: number
  coachName: string
  location: string
  startTime: string
  endTime: string
  status: CourseStatus
  createdAt: string
  updatedAt: string
  
  // 关联数据
  coach?: Coach
  bookings?: CourseBooking[]
}

export enum CourseCategory {
  YOGA = 'YOGA',
  PILATES = 'PILATES',
  CROSSFIT = 'CROSSFIT',
  SPINNING = 'SPINNING',
  BODYBUILDING = 'BODYBUILDING',
  AEROBICS = 'AEROBICS',
  PERSONAL_TRAINING = 'PERSONAL_TRAINING'
}

export enum CourseDifficulty {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED'
}

export enum CourseStatus {
  SCHEDULED = 'SCHEDULED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

// 课程预约
export interface CourseBooking {
  id: number
  bookingNo: string
  courseId: number
  memberId: number
  memberName: string
  status: BookingStatus
  checkInTime?: string
  checkOutTime?: string
  notes?: string
  createdAt: string
  updatedAt: string
  
  // 关联数据
  course?: Course
  member?: Member
}

export enum BookingStatus {
  BOOKED = 'BOOKED',
  CHECKED_IN = 'CHECKED_IN',
  CHECKED_OUT = 'CHECKED_OUT',
  CANCELLED = 'CANCELLED',
  NO_SHOW = 'NO_SHOW'
}

// 订单相关类型
export interface Order {
  id: number
  orderNo: string
  memberId: number
  memberName: string
  type: OrderType
  amount: number
  paymentAmount: number
  discountAmount: number
  paymentMethod: PaymentMethod
  paymentStatus: PaymentStatus
  orderStatus: OrderStatus
  items: OrderItem[]
  notes?: string
  paidAt?: string
  completedAt?: string
  createdAt: string
  updatedAt: string
  
  // 关联数据
  member?: Member
}

export interface OrderItem {
  id: number
  orderId: number
  itemType: OrderItemType
  itemId: number
  itemName: string
  quantity: number
  unitPrice: number
  totalPrice: number
  commissionRate?: number
  commissionAmount?: number
}

export enum OrderType {
  MEMBERSHIP = 'MEMBERSHIP',
  COURSE_PACKAGE = 'COURSE_PACKAGE',
  PERSONAL_TRAINING = 'PERSONAL_TRAINING',
  PRODUCT = 'PRODUCT'
}

export enum OrderItemType {
  MEMBERSHIP_CARD = 'MEMBERSHIP_CARD',
  COURSE_PACKAGE = 'COURSE_PACKAGE',
  PERSONAL_TRAINING_SESSION = 'PERSONAL_TRAINING_SESSION',
  PRODUCT_ITEM = 'PRODUCT_ITEM'
}

export enum PaymentMethod {
  WECHAT = 'WECHAT',
  ALIPAY = 'ALIPAY',
  CASH = 'CASH',
  CARD = 'CARD',
  BANK_TRANSFER = 'BANK_TRANSFER'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  FAILED = 'FAILED',
  REFUNDED = 'REFUNDED'
}

export enum OrderStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}

// 签到相关类型
export interface CheckIn {
  id: number
  checkInNo: string
  memberId: number
  memberName: string
  checkInTime: string
  checkOutTime?: string
  location: string
  type: CheckInType
  status: CheckInStatus
  createdAt: string
  updatedAt: string
  
  // 关联数据
  member?: Member
}

export enum CheckInType {
  COURSE = 'COURSE',
  GYM_ACCESS = 'GYM_ACCESS',
  PERSONAL_TRAINING = 'PERSONAL_TRAINING'
}

export enum CheckInStatus {
  CHECKED_IN = 'CHECKED_IN',
  CHECKED_OUT = 'CHECKED_OUT'
}

// 健康档案
export interface HealthRecord {
  id: number
  memberId: number
  recordDate: string
  height: number
  weight: number
  bmi: number
  bodyFatPercentage?: number
  muscleMass?: number
  bloodPressure?: string
  heartRate?: number
  notes?: string
  createdAt: string
  updatedAt: string
  
  // 关联数据
  member?: Member
}

// 训练计划
export interface TrainingPlan {
  id: number
  planNo: string
  memberId: number
  memberName: string
  coachId: number
  coachName: string
  title: string
  description?: string
  duration: number
  startDate: string
  endDate: string
  status: PlanStatus
  sessions: TrainingSession[]
  createdAt: string
  updatedAt: string
  
  // 关联数据
  member?: Member
  coach?: Coach
}

export interface TrainingSession {
  id: number
  planId: number
  sessionDate: string
  startTime: string
  endTime: string
  content: string
  notes?: string
  status: SessionStatus
  createdAt: string
  updatedAt: string
}

export enum PlanStatus {
  ACTIVE = 'ACTIVE',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

export enum SessionStatus {
  SCHEDULED = 'SCHEDULED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

// 仪表盘统计
export interface DashboardStats {
  totalMembers: number
  activeMembers: number
  totalCoaches: number
  activeCoaches: number
  todayCheckIns: number
  todayCourses: number
  todayBookings: number
  todayRevenue: number
  monthlyRevenue: number
  yearlyRevenue: number
  
  // 图表数据
  revenueTrend: RevenueData[]
  memberGrowth: GrowthData[]
  coursePopularity: PopularityData[]
  coachPerformance: PerformanceData[]
}

export interface RevenueData {
  date: string
  revenue: number
}

export interface GrowthData {
  date: string
  count: number
}

export interface PopularityData {
  name: string
  count: number
}

export interface PerformanceData {
  name: string
  sessions: number
  revenue: number
  rating: number
}

// API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: string
}

export interface PaginatedResponse<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

// 分页参数
export interface PageParams {
  page?: number
  pageSize?: number
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

// 查询参数
export interface QueryParams extends PageParams {
  keyword?: string
  status?: string
  startDate?: string
  endDate?: string
  [key: string]: any
}
