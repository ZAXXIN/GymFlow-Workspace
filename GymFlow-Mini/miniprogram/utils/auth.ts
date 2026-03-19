// 认证工具

import { wxRequest } from './request'
import { MiniLoginResult, LoginParams } from '../services/types/user.types'

/**
 * 小程序登录
 */
export const miniLogin = async (params: LoginParams): Promise<MiniLoginResult> => {
  try {
    const result = await wxRequest.post<MiniLoginResult>('/mini/auth/login', params, {
      showLoading: true,
      loadingText: '登录中...'
    })
    
    // 保存token
    wxRequest.setToken(result.token)
    
    // 保存用户信息
    wx.setStorageSync('userInfo', result)
    
    return result
  } catch (error) {
    throw error
  }
}

/**
 * 退出登录
 */
export const logout = () => {
  wxRequest.clearToken()
  wx.removeStorageSync('userInfo')
  wx.removeStorageSync('token')
}

/**
 * 检查是否已登录
 */
export const isLoggedIn = (): boolean => {
  return !!wx.getStorageSync('token')
}

/**
 * 获取当前用户信息
 */
export const getCurrentUser = (): MiniLoginResult | null => {
  const userInfo = wx.getStorageSync('userInfo')
  return userInfo || null
}

/**
 * 获取用户角色
 */
export const getUserRole = (): 'MEMBER' | 'COACH' | null => {
  const userInfo = getCurrentUser()
  return userInfo?.role || null
}

/**
 * 获取会员ID
 */
export const getMemberId = (): number | null => {
  const userInfo = getCurrentUser()
  return userInfo?.memberInfo?.memberId || null
}

/**
 * 获取教练ID
 */
export const getCoachId = (): number | null => {
  const userInfo = getCurrentUser()
  return userInfo?.coachInfo?.coachId || null
}

/**
 * 修改密码
 */
export const changePassword = async (params: {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}) => {
  // 验证密码
  if (params.newPassword !== params.confirmPassword) {
    throw new Error('两次输入的新密码不一致')
  }
  
  if (params.newPassword.length < 6) {
    throw new Error('密码长度不能小于6位')
  }
  
  const userInfo = getCurrentUser()
  if (!userInfo) {
    throw new Error('用户未登录')
  }
  
  // 根据角色调用不同接口
  if (userInfo.role === 'MEMBER') {
    return wxRequest.put(`/member/update/${userInfo.memberInfo?.memberId}`, {
      password: params.newPassword
    })
  } else {
    return wxRequest.put(`/coach/update/${userInfo.coachInfo?.coachId}`, {
      password: params.newPassword
    })
  }
}