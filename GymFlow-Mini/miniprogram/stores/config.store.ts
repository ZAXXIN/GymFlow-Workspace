// stores/config.store.ts
// 系统配置状态管理

import { getCheckinRules, getSystemConfig } from '../services/api/config.api'
import { CheckinRules, SystemConfig } from '../services/types/common.types'

export class ConfigStore {
  private _checkinRules: CheckinRules | null = null
  private _systemConfig: SystemConfig | null = null
  private _systemName: string = 'GymFlow健身'
  private _systemLogo: string = '/assets/icons/logo.jpg'
  private _checkinRulesLoaded: boolean = false
  private _systemConfigLoaded: boolean = false

  constructor() {
    // 不在构造函数中自动加载，改为按需加载
  }

  /**
   * 加载配置（兼容旧代码）
   */
  async loadConfig(force: boolean = false) {
    // 同时加载系统配置和签到规则
    await Promise.all([
      this.loadSystemConfig(force),
      this.loadCheckinRules(force)
    ])
  }

  /**
   * 加载系统配置（包括基本设置和业务设置）
   */
  async loadSystemConfig(force: boolean = false) {
    // 如果已经加载过且不强制刷新，则返回
    if (this._systemConfigLoaded && !force) {
      return
    }

    try {
      const config = await getSystemConfig()
      this._systemConfig = config
      
      // 更新系统名称和logo
      if (config && config.basic) {
        this._systemName = config.basic.systemName || 'GymFlow健身'
        this._systemLogo = config.basic.systemLogo || '/assets/icons/logo.jpg'
      }
      
      // 如果配置中有签到规则，也一并更新
      if (config && config.business) {
        this._checkinRules = {
          checkinStartMinutes: config.business.checkinStartMinutes || 30,
          checkinEndMinutes: config.business.checkinEndMinutes || 0,
          autoCompleteHours: config.business.autoCompleteHours || 1
        }
        this._checkinRulesLoaded = true
      }
      
      this._systemConfigLoaded = true
      console.log('系统配置加载成功:', config)
    } catch (error) {
      console.error('加载系统配置失败:', error)
      // 使用默认配置
      this._systemConfigLoaded = true
    }
  }

  /**
   * 加载签到规则
   */
  async loadCheckinRules(force: boolean = false) {
    // 如果已经加载过且不强制刷新，则返回
    if (this._checkinRulesLoaded && !force) {
      return
    }

    try {
      // 只有在有token的情况下才调用接口，否则使用默认值
      const token = wx.getStorageSync('token')
      if (!token) {
        this._checkinRules = {
          checkinStartMinutes: 30,
          checkinEndMinutes: 0,
          autoCompleteHours: 1
        }
        this._checkinRulesLoaded = true
        return
      }

      const rules = await getCheckinRules()
      this._checkinRules = rules
      this._checkinRulesLoaded = true
    } catch (error) {
      console.error('加载签到规则失败:', error)
      // 设置默认值
      this._checkinRules = {
        checkinStartMinutes: 30,
        checkinEndMinutes: 0,
        autoCompleteHours: 1
      }
      this._checkinRulesLoaded = true
    }
  }

  /**
   * 设置签到规则
   */
  setCheckinRules(rules: CheckinRules) {
    this._checkinRules = rules
    this._checkinRulesLoaded = true
  }

  /**
   * 判断是否可签到
   */
  canCheckin(scheduleDate: string, startTime: string): boolean {
    if (!this._checkinRules) return false
    
    const now = new Date()
    const courseStart = new Date(`${scheduleDate} ${startTime}`)
    const checkinStart = new Date(courseStart.getTime() - this._checkinRules.checkinStartMinutes * 60000)
    
    if (this._checkinRules.checkinEndMinutes > 0) {
      const checkinEnd = new Date(courseStart.getTime() + this._checkinRules.checkinEndMinutes * 60000)
      return now >= checkinStart && now <= checkinEnd
    } else {
      // checkinEndMinutes = 0 表示课程开始后不可签到
      return now >= checkinStart && now <= courseStart
    }
  }

  /**
   * 获取课程状态
   */
  getCourseStatus(scheduleDate: string, startTime: string, endTime: string): 'upcoming' | 'ongoing' | 'completed' {
    const now = new Date()
    const start = new Date(`${scheduleDate} ${startTime}`)
    const end = new Date(`${scheduleDate} ${endTime}`)
    
    if (now < start) {
      return 'upcoming'
    } else if (now > end) {
      return 'completed'
    } else {
      return 'ongoing'
    }
  }

  /**
   * 设置系统信息
   */
  setSystemInfo(name: string, logo: string) {
    this._systemName = name
    this._systemLogo = logo
  }

  // ========== Getters ==========

  get checkinRules() {
    return this._checkinRules
  }

  get systemConfig() {
    return this._systemConfig
  }

  get systemName() {
    return this._systemName
  }

  get systemLogo() {
    return this._systemLogo
  }

  get checkinStartMinutes() {
    return this._checkinRules?.checkinStartMinutes || 30
  }

  get checkinEndMinutes() {
    return this._checkinRules?.checkinEndMinutes || 0
  }

  get autoCompleteHours() {
    return this._checkinRules?.autoCompleteHours || 1
  }

  get isSystemConfigLoaded() {
    return this._systemConfigLoaded
  }

  // ========== 业务配置 Getters ==========

  /**
   * 获取营业开始时间
   */
  get businessStartTime(): string {
    if (this._systemConfig?.business?.businessStartTime) {
      return this._systemConfig.business.businessStartTime.substring(0, 5)
    }
    return ''
  }

  /**
   * 获取营业结束时间
   */
  get businessEndTime(): string {
    if (this._systemConfig?.business?.businessEndTime) {
      return this._systemConfig.business.businessEndTime.substring(0, 5)
    }
    return ''
  }

  /**
   * 获取提前预约小时数（最早可预约时间）
   */
  get advanceBookingHours(): number {
    return this._systemConfig?.business?.courseAdvanceBookingHours || 4
  }

  /**
   * 获取课程取消小时数
   */
  get courseCancelHours(): number {
    return this._systemConfig?.business?.courseCancelHours || 2
  }

  /**
   * 获取最低开课人数
   */
  get minClassSize(): number {
    return this._systemConfig?.business?.minClassSize || 5
  }

  /**
   * 获取最大课程容量
   */
  get maxClassCapacity(): number {
    return this._systemConfig?.business?.maxClassCapacity || 30
  }

  /**
   * 获取课程续约天数
   */
  get courseRenewalDays(): number {
    return this._systemConfig?.business?.courseRenewalDays || 7
  }

  // ========== 时间计算方法 ==========

/**
 * 获取最晚可开始时间（营业结束时间减去课程时长）
 * @param duration 课程时长（分钟）
 */
getMaxStartTime(duration: number): string {
  const endTime = this.businessEndTime
  if (!endTime) return '21:00'
  
  const [endHour, endMinute] = endTime.split(':').map(Number)
  const durationHours = Math.floor(duration / 60)
  const durationMinutes = duration % 60

  let maxHour = endHour - durationHours
  let maxMinute = endMinute - durationMinutes

  if (maxMinute < 0) {
    maxMinute += 60
    maxHour -= 1
  }

  if (maxHour < 0) {
    maxHour = 0
  }

  return `${String(maxHour).padStart(2, '0')}:${String(maxMinute).padStart(2, '0')}`
}

/**
 * 获取最早可开始时间（根据当前时间和提前预约小时数）
 * @param selectedDate 选择的日期（YYYY-MM-DD）
 * @param duration 课程时长（分钟）
 */
getMinStartTime(selectedDate: string, duration: number): string {
  const now = new Date()
  const advanceHours = this.advanceBookingHours
  const earliestTime = new Date(now.getTime() + advanceHours * 60 * 60 * 1000)
  
  // 判断选择的日期是否是当天
  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
  const isToday = selectedDate === todayStr
  
  // 如果不是当天，返回营业开始时间
  if (!isToday) {
    return this.businessStartTime
  }
  
  // 当天：根据提前预约小时数计算
  let hour = earliestTime.getHours()
  let minute = earliestTime.getMinutes()
  
  const startTime = this.businessStartTime
  if (!startTime) {
    // 如果没有营业开始时间，直接返回计算出的时间（向上取整到10分钟）
    const totalMinutes = hour * 60 + minute
    const step = 10
    const roundedMinutes = Math.ceil(totalMinutes / step) * step
    const roundedHour = Math.floor(roundedMinutes / 60)
    const roundedMinute = roundedMinutes % 60
    return `${roundedHour.toString().padStart(2, '0')}:${roundedMinute.toString().padStart(2, '0')}`
  }
  
  const [startHour, startMinute] = startTime.split(':').map(Number)
  
  // 如果计算出的时间早于营业开始时间，返回营业开始时间
  if (hour * 60 + minute < startHour * 60 + startMinute) {
    return startTime
  }
  
  // 向上取整到10分钟
  const step = 10
  const totalMinutes = hour * 60 + minute
  const roundedMinutes = Math.ceil(totalMinutes / step) * step
  const roundedHour = Math.floor(roundedMinutes / 60)
  const roundedMinute = roundedMinutes % 60
  return `${roundedHour.toString().padStart(2, '0')}:${roundedMinute.toString().padStart(2, '0')}`
}

  /**
   * 计算结束时间
   * @param startTime 开始时间（HH:MM）
   * @param duration 课程时长（分钟）
   */
  calculateEndTime(startTime: string, duration: number): string {
    if (!startTime || !duration) return ''

    const [hours, minutes] = startTime.split(':').map(Number)
    const durationHours = Math.floor(duration / 60)
    const durationMinutes = duration % 60

    let endHour = hours + durationHours
    let endMinute = minutes + durationMinutes

    if (endMinute >= 60) {
      endMinute -= 60
      endHour += 1
    }

    const endTime = this.businessEndTime
    if (endTime) {
      const [maxHour, maxMinute] = endTime.split(':').map(Number)
      if (endHour > maxHour || (endHour === maxHour && endMinute > maxMinute)) {
        endHour = maxHour
        endMinute = maxMinute
      }
    }

    return `${String(endHour).padStart(2, '0')}:${String(endMinute).padStart(2, '0')}`
  }

  /**
   * 判断是否在营业时间内
   * @param startTime 开始时间
   * @param endTime 结束时间
   */
  isWithinBusinessHours(startTime: string, endTime: string): boolean {
    const businessStart = this.businessStartTime
    const businessEnd = this.businessEndTime
    
    if (!businessStart || !businessEnd) return true
    
    return startTime >= businessStart && endTime <= businessEnd
  }

  /**
   * 判断是否可以预约（根据提前预约小时数）
   * @param scheduleDate 课程日期
   * @param startTime 开始时间
   */
  canBook(scheduleDate: string, startTime: string): boolean {
    const now = new Date()
    const courseStart = new Date(`${scheduleDate} ${startTime}`)
    const earliestBookTime = new Date(now.getTime() + this.advanceBookingHours * 60 * 60 * 1000)
    
    return courseStart >= earliestBookTime
  }

  /**
   * 判断是否可以取消（根据取消小时数）
   * @param scheduleDate 课程日期
   * @param startTime 开始时间
   */
  canCancel(scheduleDate: string, startTime: string): boolean {
    const now = new Date()
    const courseStart = new Date(`${scheduleDate} ${startTime}`)
    const hoursUntilCourse = (courseStart.getTime() - now.getTime()) / (1000 * 60 * 60)
    
    return hoursUntilCourse >= this.courseCancelHours
  }
}

// 导出单例
export const configStore = new ConfigStore()