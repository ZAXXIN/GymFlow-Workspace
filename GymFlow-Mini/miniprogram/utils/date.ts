// 日期处理工具 - 原生实现

/**
 * 格式化日期
 */
export const formatDate = (date, format = 'YYYY-MM-DD') => {
  if (!date) return ''
  
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second)
}

/**
 * 格式化时间
 */
export const formatTime = (date, format = 'HH:mm') => {
  return formatDate(date, format)
}

/**
 * 格式化日期时间
 */
export const formatDateTime = (date, format = 'YYYY-MM-DD HH:mm') => {
  return formatDate(date, format)
}

/**
 * 获取相对时间
 */
export const getRelativeTime = (date) => {
  if (!date) return ''
  
  const now = new Date()
  const target = new Date(date)
  const diffMs = now - target
  const diffMinutes = Math.floor(diffMs / (1000 * 60))
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  
  if (diffMinutes < 1) {
    return '刚刚'
  } else if (diffMinutes < 60) {
    return `${diffMinutes}分钟前`
  } else if (diffHours < 24) {
    return `${diffHours}小时前`
  } else if (diffDays < 7) {
    return `${diffDays}天前`
  } else {
    return formatDate(date)
  }
}

/**
 * 判断是否为今天
 */
export const isToday = (date) => {
  if (!date) return false
  
  const d = new Date(date)
  const today = new Date()
  
  return d.getFullYear() === today.getFullYear() &&
         d.getMonth() === today.getMonth() &&
         d.getDate() === today.getDate()
}

/**
 * 判断是否为明天
 */
export const isTomorrow = (date) => {
  if (!date) return false
  
  const d = new Date(date)
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  
  return d.getFullYear() === tomorrow.getFullYear() &&
         d.getMonth() === tomorrow.getMonth() &&
         d.getDate() === tomorrow.getDate()
}

/**
 * 判断是否为昨天
 */
export const isYesterday = (date) => {
  if (!date) return false
  
  const d = new Date(date)
  const yesterday = new Date()
  yesterday.setDate(yesterday.getDate() - 1)
  
  return d.getFullYear() === yesterday.getFullYear() &&
         d.getMonth() === yesterday.getMonth() &&
         d.getDate() === yesterday.getDate()
}

/**
 * 获取日期范围
 */
export const getDateRange = (period) => {
  const now = new Date()
  const start = new Date(now)
  const end = new Date(now)
  
  switch (period) {
    case 'day':
      start.setHours(0, 0, 0, 0)
      end.setHours(23, 59, 59, 999)
      break
    case 'week':
      const day = start.getDay()
      start.setDate(start.getDate() - day + (day === 0 ? -6 : 1)) // 周一
      start.setHours(0, 0, 0, 0)
      end.setDate(start.getDate() + 6)
      end.setHours(23, 59, 59, 999)
      break
    case 'month':
      start.setDate(1)
      start.setHours(0, 0, 0, 0)
      end.setMonth(end.getMonth() + 1)
      end.setDate(0)
      end.setHours(23, 59, 59, 999)
      break
    case 'year':
      start.setMonth(0, 1)
      start.setHours(0, 0, 0, 0)
      end.setMonth(11, 31)
      end.setHours(23, 59, 59, 999)
      break
    default:
      start.setHours(0, 0, 0, 0)
      end.setHours(23, 59, 59, 999)
  }
  
  const formatDateTime = (date) => {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    const h = String(date.getHours()).padStart(2, '0')
    const min = String(date.getMinutes()).padStart(2, '0')
    const s = String(date.getSeconds()).padStart(2, '0')
    return `${y}-${m}-${d} ${h}:${min}:${s}`
  }
  
  return [formatDateTime(start), formatDateTime(end)]
}

/**
 * 判断是否在签到时间内
 */
export const isCheckinTime = (courseDate, startTime, checkinStartMinutes, checkinEndMinutes) => {
  if (!courseDate || !startTime) return false
  
  const now = new Date()
  const courseStart = new Date(`${courseDate} ${startTime}`)
  
  const checkinStart = new Date(courseStart.getTime() - checkinStartMinutes * 60 * 1000)
  const checkinEnd = new Date(courseStart.getTime() + checkinEndMinutes * 60 * 1000)
  
  return now >= checkinStart && now <= checkinEnd
}

/**
 * 获取课程状态
 */
export const getCourseStatus = (courseDate, startTime, endTime) => {
  if (!courseDate || !startTime || !endTime) return 'completed'
  
  const now = new Date()
  const start = new Date(`${courseDate} ${startTime}`)
  const end = new Date(`${courseDate} ${endTime}`)
  
  if (now < start) {
    return 'upcoming'
  } else if (now > end) {
    return 'completed'
  } else {
    return 'ongoing'
  }
}