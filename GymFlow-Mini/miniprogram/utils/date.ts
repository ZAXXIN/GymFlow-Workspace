// utils/date.ts

/**
 * 格式化日期
 * @param date 日期对象或日期字符串
 * @param format 格式，如 'YYYY-MM-DD'
 */
export const formatDate = (date: Date | string, format: string = 'YYYY-MM-DD'): string => {
  if (!date) return ''
  
  let d: Date
  if (typeof date === 'string') {
    // iOS 兼容：将 "yyyy-MM-dd" 转换为 "yyyy/MM/dd"
    const iosCompatible = date.replace(/-/g, '/')
    d = new Date(iosCompatible)
  } else {
    d = date
  }
  
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  
  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
}

/**
 * 格式化时间
 * @param time 时间字符串，如 '14:30:00'
 */
export const formatTime = (time: string): string => {
  if (!time) return ''
  return time.substring(0, 5)
}

/**
 * 判断是否为今天
 * @param dateStr 日期字符串，格式 YYYY-MM-DD
 */
export const isToday = (dateStr: string): boolean => {
  if (!dateStr) return false
  
  const today = new Date()
  const [year, month, day] = dateStr.split('-').map(Number)
  
  return year === today.getFullYear() && 
         month === today.getMonth() + 1 && 
         day === today.getDate()
}

/**
 * 判断是否为明天
 * @param dateStr 日期字符串，格式 YYYY-MM-DD
 */
export const isTomorrow = (dateStr: string): boolean => {
  if (!dateStr) return false
  
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  
  const [year, month, day] = dateStr.split('-').map(Number)
  
  return year === tomorrow.getFullYear() && 
         month === tomorrow.getMonth() + 1 && 
         day === tomorrow.getDate()
}

/**
 * 获取日期显示文本（今天/明天/MM月DD日）
 * @param dateStr 日期字符串
 */
export const getDateDisplay = (dateStr: string): string => {
  if (!dateStr) return ''
  if (isToday(dateStr)) return '今天'
  if (isTomorrow(dateStr)) return '明天'
  
  const [, month, day] = dateStr.split('-')
  return `${parseInt(month)}月${parseInt(day)}日`
}

/**
 * 获取课程状态
 */
export const getCourseStatus = (course: any) => {
  if (!course || !course.scheduleDate || !course.startTime || !course.endTime) {
    return { class: '', text: '' }
  }
  
  const now = new Date()
  // iOS 兼容
  const scheduleDateTime = new Date(`${course.scheduleDate.replace(/-/g, '/')} ${course.startTime}`)
  const courseEnd = new Date(`${course.scheduleDate.replace(/-/g, '/')} ${course.endTime}`)
  
  if (now < scheduleDateTime) {
    return { class: 'upcoming', text: '待上课' }
  } else if (now > courseEnd) {
    return { class: 'completed', text: '已结束' }
  } else {
    return { class: 'ongoing', text: '进行中' }
  }
}

/**
 * 获取预约状态文本
 */
export const getBookingStatusText = (status: number): string => {
  const statusMap: Record<number, string> = {
    0: '待上课',
    1: '已签到',
    2: '已完成',
    3: '已取消',
    4: '已过期'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取预约状态样式类
 */
export const getBookingStatusClass = (status: number): string => {
  const classMap: Record<number, string> = {
    0: 'pending',
    1: 'checked',
    2: 'completed',
    3: 'cancelled',
    4: 'expired'
  }
  return classMap[status] || 'unknown'
}

/**
 * 获取剩余名额显示文本
 */
export const getRemainingText = (remaining: number): string => {
  if (remaining <= 0) return '已满'
  if (remaining <= 5) return `仅剩${remaining}席`
  return `${remaining}席`
}

/**
 * 获取剩余名额样式类
 */
export const getRemainingClass = (remaining: number): string => {
  if (remaining <= 0) return 'full'
  if (remaining <= 5) return 'low'
  return 'normal'
}

/**
 * 获取课程类型文本
 */
export const getCourseTypeText = (type: number): string => {
  return type === 0 ? '私教' : '团课'
}

/**
 * 获取课程类型样式类
 */
export const getCourseTypeClass = (type: number): string => {
  return type === 0 ? 'private' : 'group'
}

/**
 * 格式化金额
 */
export const formatMoney = (amount: number): string => {
  if (!amount && amount !== 0) return '¥0.00'
  return `¥${amount.toFixed(2)}`
}

/**
 * 获取性别文本
 */
export const getGenderText = (gender: number): string => {
  if (gender === 0) return '女'
  if (gender === 1) return '男'
  return '未知'
}