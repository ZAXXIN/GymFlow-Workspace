// 教练相关类型定义
export interface CoachQueryParams {
  pageNum?: number
  pageSize?: number
  realName?: string
  phone?: string
  specialty?: string
  status?: number
  minExperience?: number
  maxExperience?: number
}

export interface CoachBasicDTO {
  realName: string
  phone: string
  password?: string
  specialty?: string
  certificationList?: string[]
  yearsOfExperience: number
  hourlyRate: number
  commissionRate: number
  introduction?: string
}

export interface CoachScheduleDTO {
  scheduleDate: string // YYYY-MM-DD
  startTime: string // HH:mm:ss
  endTime: string // HH:mm:ss
  scheduleType: number // 0-私教课，1-团课
  notes?: string
  scheduleTypeDesc?: string
  status?: string
}

export interface CoachListVO {
  id: number
  realName: string
  phone: string
  specialty?: string
  certifications?: string[]
  yearsOfExperience: number
  hourlyRate: number
  status: number
  statusDesc: string
  totalStudents: number
  totalCourses: number
  totalIncome: number
  rating: number
  createTime: string
}

export interface CoachDetail {
  id: number
  realName: string
  phone: string
  specialty?: string
  certificationList?: string[]
  yearsOfExperience: number
  hourlyRate: number
  commissionRate: number
  status: number
  totalStudents: number
  totalCourses: number
  totalIncome: number
  rating: number
  introduction?: string
  createTime: string
  updateTime?: string
  schedules?: CoachScheduleList[]
  courses?: CoachCourseList[]
}

export interface CoachScheduleList {
  id: number
  scheduleDate: string
  startTime: string
  endTime: string
  scheduleType: number
  scheduleTypeDesc: string
  status: string
  notes?: string
}

export interface CoachCourseList {
  id: number
  courseName: string
  courseType: number
  courseDate: string
  startTime: string
  endTime: string
  duration: number
  price: number
  location: string
  maxCapacity: number
  currentEnrollment: number
  enrollmentRate: number
  status: number
}

export interface PageResultVO<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}