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
   * 设置用户信息
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
      memberNo: userInfo.memberNo, // 直接使用返回的 memberNo
      // 教练字段（预留）
      coachId: userInfo.userType === 1 ? userInfo.userId : undefined,
      
      // 其他会员信息（这些可能需要从后续的 /mini/member/my-info 接口获取）
      // 暂时设为 undefined，等加载 my-info 后再更新
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
   * 更新用户信息（从 /mini/member/my-info 接口获取后更新）
   */
  updateMemberInfo(memberInfo: any) {
    if (this._userInfo) {
      this._userInfo = {
        ...this._userInfo,
        ...memberInfo,
        // 保留原有的 memberId 和 memberNo
        memberId: this._userInfo.memberId,
        memberNo: this._userInfo.memberNo
      }
      wx.setStorageSync('userInfo', this._userInfo)
    }
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
    }
  }

  // Getters
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

  // 会员特有字段（从 my-info 接口获取）
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

  // 会员卡信息
  get cards() {
    return this._userInfo?.cards || []
  }
}

// 导出单例
export const userStore = new UserStore()