// 签到相关类型定义

// 签到记录
export interface CheckinRecord {
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

// 签到码
export interface CheckinCode {
  bookingId: number
  numericCode: string // 6位数字
  qrCode: string // 二维码内容（可以是预约ID或URL）
  expireTime: string
}

// 扫码核销参数
export interface ScanCheckinParams {
  code: string // 扫描到的二维码内容
}

// 数字码核销参数
export interface VerifyCodeParams {
  bookingId: number
  checkinCode: string // 6位数字码
}

// 当前时段提醒信息
export interface CurrentReminder {
  // 会员端
  memberReminder?: {
    courseName: string
    coachName: string
    bookingTime: string
    bookingId: number
  }
  // 教练端
  coachReminder?: {
    memberName: string
    courseName: string
    bookingTime: string
    memberId: number
    bookingId: number
  }
}