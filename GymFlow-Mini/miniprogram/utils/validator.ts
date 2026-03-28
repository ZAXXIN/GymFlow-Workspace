// 校验工具

/**
 * 校验手机号
 */
export const isValidPhone = (phone: string): boolean => {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

/**
 * 校验密码强度
 */
export const isValidPassword = (password: string): boolean => {
  // 6-20位，至少包含字母和数字
  const reg = /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,20}$/
  return reg.test(password)
}

/**
 * 校验验证码
 */
export const isValidCode = (code: string): boolean => {
  const reg = /^\d{6}$/
  return reg.test(code)
}

/**
 * 校验身份证号
 */
export const isValidIdCard = (idCard: string): boolean => {
  const reg = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/
  return reg.test(idCard)
}

/**
 * 校验邮箱
 */
export const isValidEmail = (email: string): boolean => {
  const reg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return reg.test(email)
}

/**
 * 校验金额
 */
export const isValidMoney = (money: number): boolean => {
  return !isNaN(money) && money >= 0 && money <= 999999.99
}

/**
 * 校验整数
 */
export const isValidInteger = (num: number, min?: number, max?: number): boolean => {
  if (!Number.isInteger(num)) return false
  if (min !== undefined && num < min) return false
  if (max !== undefined && num > max) return false
  return true
}

/**
 * 校验小数
 */
export const isValidDecimal = (num: number, decimalPlaces: number = 2, min?: number, max?: number): boolean => {
  if (isNaN(num)) return false
  const decimalStr = num.toString().split('.')[1] || ''
  if (decimalStr.length > decimalPlaces) return false
  if (min !== undefined && num < min) return false
  if (max !== undefined && num > max) return false
  return true
}

/**
 * 校验非空
 */
export const isNotEmpty = (value: any): boolean => {
  if (value === null || value === undefined) return false
  if (typeof value === 'string') return value.trim() !== ''
  if (Array.isArray(value)) return value.length > 0
  if (typeof value === 'object') return Object.keys(value).length > 0
  return true
}

/**
 * 校验长度
 */
export const isValidLength = (value: string, min?: number, max?: number): boolean => {
  const length = value.length
  if (min !== undefined && length < min) return false
  if (max !== undefined && length > max) return false
  return true
}

/**
 * 校验URL
 */
export const isValidUrl = (url: string): boolean => {
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}

/**
 * 校验日期
 */
export const isValidDate = (date: string): boolean => {
  const d = new Date(date)
  return d instanceof Date && !isNaN(d.getTime())
}

/**
 * 校验时间范围
 */
export const isValidTimeRange = (startTime: string, endTime: string): boolean => {
  return startTime < endTime
}

/**
 * 校验签到码是否在有效期内
 */
export const isValidCheckinTime = (
  scheduleDate: string,
  startTime: string,
  checkinStartMinutes: number
): boolean => {
  const now = new Date()
  const courseStart = new Date(`${scheduleDate} ${startTime}`)
  const checkinStart = new Date(courseStart.getTime() - checkinStartMinutes * 60000)
  
  return now >= checkinStart && now <= courseStart
}