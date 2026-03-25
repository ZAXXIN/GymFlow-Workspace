// src/types/coach.ts

// 教练列表项
export interface CoachListVO {
  id: number
  realName: string
  phone: string
  gender?: number // 新增
  genderDesc?: string // 新增
  specialty?: string
  certifications?: string[]
  yearsOfExperience?: number
  status?: number
  statusDesc?: string
  totalCourses?: number
  // rating?: number
  createTime?: string
}

// 教练基础信息DTO
export interface CoachBasicDTO {
  id?: number
  realName: string
  phone: string
  password?: string
  gender: number // 新增，必填
  specialty?: string
  certificationList?: string[]
  yearsOfExperience?: number
  introduction?: string
}

// 教练详情
export interface CoachDetail {
  id: number
  realName: string
  phone: string
  gender: number // 新增
  genderDesc?: string // 新增
  specialty?: string
  certificationList?: string[]
  yearsOfExperience?: number
  status?: number
  statusDesc?: string
  totalCourses?: number
  // rating?: number
  introduction?: string
  createTime?: string
  updateTime?: string
  schedules?: CoachScheduleList[]
  courses?: CoachCourseList[]
}