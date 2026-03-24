// stores/user.store.ts
// 用户状态管理

import { logout as logoutApi } from '../services/api/auth.api'

class UserStore {
  private _userInfo: any = null
  private _isLogin: boolean = false

  constructor() {
    // 初始化时从存储加载用户信息
    this.loadFromStorage()
  }

  /**
   * 从存储加载用户信息
   */
  private loadFromStorage() {
    try {
      const userInfo = wx.getStorageSync('userInfo')
      if (userInfo) {
        this._userInfo = userInfo
        this._isLogin = true
      }
    } catch (error) {
      console.error('加载用户信息失败:', error)
    }
  }

  /**
   * 设置用户信息（登录时使用）
   */
  setUserInfo(userInfo: any) {
    // 处理角色转换 - 根据后端返回的 userType
    // userType: 0-会员, 1-教练
    const role = userInfo.userType === 0 ? 'MEMBER' : userInfo.userType === 1 ? 'COACH' : null

    // 构造统一的数据结构
    const processedUserInfo = {
      ...userInfo,
      role: role,
      // 会员字段 - 从 userId 获取 memberId
      memberId: userInfo.userType === 0 ? userInfo.userId : undefined,
      memberNo: userInfo.memberNo,
      // 教练字段
      coachId: userInfo.userType === 1 ? userInfo.userId : undefined,

      // 其他会员信息
      membershipStartDate: undefined,
      membershipEndDate: undefined,
      totalCheckins: 0,
      totalCourseHours: 0,
      totalSpent: 0,
      personalCoachName: undefined,
      height: undefined,
      weight: undefined,
      cards: []
    }

    this._userInfo = processedUserInfo
    this._isLogin = true

    // 保存到存储
    wx.setStorageSync('userInfo', processedUserInfo)
  }

  /**
   * 更新用户信息（会员或教练通用）
   * 用于在页面中刷新用户信息时调用
   */
  updateUserInfo(data: any) {
    if (!this._userInfo) return

    // 合并数据
    this._userInfo = {
      ...this._userInfo,
      ...data
    }

    wx.setStorageSync('userInfo', this._userInfo)
  }

  /**
   * 退出登录
   */
  async logout() {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      this._userInfo = null
      this._isLogin = false

      wx.removeStorageSync('userInfo')
      wx.removeStorageSync('token')

      // 重置 TabBar 为默认会员配置
      const app = getApp() as any
      if (app && app.setMemberTabBar) {
        app.setMemberTabBar()
      }
    }
  }

  // ========== Getters ==========

  get userInfo() {
    return this._userInfo
  }

  get isLogin() {
    return this._isLogin
  }

  get role() {
    return this._userInfo?.role
  }

  // 会员相关
  get memberId() {
    return this._userInfo?.memberId
  }

  get memberNo() {
    return this._userInfo?.memberNo
  }

  // 教练相关
  get coachId() {
    return this._userInfo?.coachId
  }

  // 通用
  get userId() {
    return this._userInfo?.userId
  }

  get phone() {
    return this._userInfo?.phone
  }

  get realName() {
    return this._userInfo?.realName
  }

  // 会员特有字段
  get membershipStartDate() {
    return this._userInfo?.membershipStartDate
  }

  get membershipEndDate() {
    return this._userInfo?.membershipEndDate
  }

  get totalCheckins() {
    return this._userInfo?.totalCheckins || 0
  }

  get totalCourseHours() {
    return this._userInfo?.totalCourseHours || 0
  }

  get totalSpent() {
    return this._userInfo?.totalSpent || 0
  }

  get personalCoachId() {
    return this._userInfo?.personalCoachId
  }

  get personalCoachName() {
    return this._userInfo?.personalCoachName
  }

  get height() {
    return this._userInfo?.height
  }

  get weight() {
    return this._userInfo?.weight
  }

  get birthday() {
    return this._userInfo?.birthday
  }

  get gender() {
    return this._userInfo?.gender
  }

  get idCard() {
    return this._userInfo?.idCard
  }

  get address() {
    return this._userInfo?.address
  }

  get cards() {
    return this._userInfo?.cards || []
  }
}

// 导出单例
export const userStore = new UserStore()