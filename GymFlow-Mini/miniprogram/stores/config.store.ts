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
  canCheckin(courseDate: string, startTime: string): boolean {
    if (!this._checkinRules) return false
    
    const now = new Date()
    const courseStart = new Date(`${courseDate} ${startTime}`)
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
  getCourseStatus(courseDate: string, startTime: string, endTime: string): 'upcoming' | 'ongoing' | 'completed' {
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

  /**
   * 设置系统信息
   */
  setSystemInfo(name: string, logo: string) {
    this._systemName = name
    this._systemLogo = logo
  }

  // Getters
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
}

// 导出单例
export const configStore = new ConfigStore()