import { type RouteLocationNormalized } from 'vue-router'
import dayjs from 'dayjs'

/**
 * 格式化日期时间
 */
export function formatDateTime(date: string | Date, format = 'YYYY-MM-DD HH:mm:ss'): string {
  return dayjs(date).format(format)
}

/**
 * 格式化日期
 */
export function formatDate(date: string | Date, format = 'YYYY-MM-DD'): string {
  return dayjs(date).format(format)
}

/**
 * 格式化时间
 */
export function formatTime(date: string | Date, format = 'HH:mm'): string {
  return dayjs(date).format(format)
}

/**
 * 格式化金额
 */
export function formatAmount(amount: number): string {
  return `¥${amount.toFixed(2)}`
}

/**
 * 格式化数量
 */
export function formatNumber(num: number): string {
  return num.toLocaleString()
}

/**
 * 获取文件大小文本
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 防抖函数
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout
  
  return function(...args: Parameters<T>) {
    clearTimeout(timeout)
    timeout = setTimeout(() => func.apply(this, args), wait)
  }
}

/**
 * 节流函数
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout | null = null
  let previous = 0
  
  return function(...args: Parameters<T>) {
    const now = Date.now()
    const remaining = wait - (now - previous)
    
    if (remaining <= 0) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }
      previous = now
      func.apply(this, args)
    } else if (!timeout) {
      timeout = setTimeout(() => {
        previous = Date.now()
        timeout = null
        func.apply(this, args)
      }, remaining)
    }
  }
}

/**
 * 深度克隆对象
 */
export function deepClone<T>(obj: T): T {
  if (obj === null || typeof obj !== 'object') return obj
  if (obj instanceof Date) return new Date(obj.getTime()) as T
  if (obj instanceof Array) return obj.map(item => deepClone(item)) as T
  if (typeof obj === 'object') {
    const cloned: any = {}
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        cloned[key] = deepClone(obj[key])
      }
    }
    return cloned as T
  }
  return obj
}

/**
 * 对象合并
 */
export function mergeObjects<T extends object>(...objects: Partial<T>[]): T {
  const result = {} as T
  
  for (const obj of objects) {
    for (const key in obj) {
      if (obj.hasOwnProperty(key)) {
        if (typeof obj[key] === 'object' && obj[key] !== null && !Array.isArray(obj[key])) {
          result[key] = mergeObjects(result[key] || {}, obj[key])
        } else {
          result[key] = obj[key]
        }
      }
    }
  }
  
  return result
}

/**
 * 获取URL参数
 */
export function getQueryParam(name: string): string | null {
  const urlParams = new URLSearchParams(window.location.search)
  return urlParams.get(name)
}

/**
 * 设置URL参数
 */
export function setQueryParam(name: string, value: string): void {
  const url = new URL(window.location.href)
  url.searchParams.set(name, value)
  window.history.pushState({}, '', url.toString())
}

/**
 * 移除URL参数
 */
export function removeQueryParam(name: string): void {
  const url = new URL(window.location.href)
  url.searchParams.delete(name)
  window.history.pushState({}, '', url.toString())
}

/**
 * 生成UUID
 */
export function generateUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

/**
 * 生成随机字符串
 */
export function generateRandomString(length = 8): string {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

/**
 * 验证手机号
 */
export function validatePhone(phone: string): boolean {
  return /^1[3-9]\d{9}$/.test(phone)
}

/**
 * 验证邮箱
 */
export function validateEmail(email: string): boolean {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

/**
 * 验证身份证号
 */
export function validateIdCard(idCard: string): boolean {
  return /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/.test(idCard)
}

/**
 * 获取枚举选项
 */
export function getEnumOptions(enumObj: any): Array<{ label: string; value: any }> {
  return Object.keys(enumObj)
    .filter(key => isNaN(Number(key)))
    .map(key => ({
      label: key,
      value: enumObj[key]
    }))
}

/**
 * 获取枚举标签
 */
export function getEnumLabel(enumObj: any, value: any): string {
  const key = Object.keys(enumObj).find(k => enumObj[k] === value)
  return key || ''
}

/**
 * 下载文件
 */
export function downloadFile(url: string, filename?: string): void {
  const a = document.createElement('a')
  a.href = url
  a.download = filename || 'download'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

/**
 * 复制到剪贴板
 */
export async function copyToClipboard(text: string): Promise<boolean> {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch (err) {
    console.error('Failed to copy:', err)
    
    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = text
    textArea.style.position = 'fixed'
    textArea.style.left = '-999999px'
    textArea.style.top = '-999999px'
    document.body.appendChild(textArea)
    textArea.focus()
    textArea.select()
    
    try {
      document.execCommand('copy')
      return true
    } catch (err) {
      console.error('Fallback copy failed:', err)
      return false
    } finally {
      document.body.removeChild(textArea)
    }
  }
}

/**
 * 检查路由权限
 */
export function checkRoutePermission(route: RouteLocationNormalized, userRole: string): boolean {
  const requiredRoles = route.meta?.roles as string[] || []
  
  if (requiredRoles.length === 0) {
    return true
  }
  
  return requiredRoles.includes(userRole)
}

/**
 * 格式化持续时间
 */
export function formatDuration(minutes: number): string {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  
  if (hours === 0) {
    return `${mins}分钟`
  } else if (mins === 0) {
    return `${hours}小时`
  } else {
    return `${hours}小时${mins}分钟`
  }
}

/**
 * 计算BMI
 */
export function calculateBMI(height: number, weight: number): number {
  // 身高转换为米
  const heightInMeters = height / 100
  return weight / (heightInMeters * heightInMeters)
}

/**
 * 获取BMI评级
 */
export function getBMIRating(bmi: number): string {
  if (bmi < 18.5) return '偏瘦'
  if (bmi < 24) return '正常'
  if (bmi < 28) return '超重'
  return '肥胖'
}