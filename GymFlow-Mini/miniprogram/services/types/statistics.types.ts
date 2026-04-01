// 会员统计相关类型定义

// 签到记录
export interface CheckinRecordDTO {
  checkinTime: string
  checkinMethod: 0 | 1
  checkinMethodDesc: string
}

// 签到统计
export interface CheckinStatistics {
  totalCheckins: number
  recentCheckins: CheckinRecordDTO[]
}

// 课时明细记录
export interface SessionRecordDTO {
  type: 'CONSUME' | 'RETURN'
  time: string
  courseName: string
  sessions: number
  bookingId: number
  reason?: string
}

// 课时统计
export interface SessionStatistics {
  totalConsumed: number
  totalReturned: number
  details: SessionRecordDTO[]
}

// 会员统计数据
export interface MemberStatistics {
  checkinStats: CheckinStatistics
  sessionStats: SessionStatistics
}