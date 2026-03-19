// 认证相关API

import { wxRequest } from '../../utils/request'
import { LoginParams, MiniLoginResult } from '../types/user.types'

/**
 * 小程序登录
 * POST /mini/auth/login
 */
export const miniLogin = async (params: LoginParams): Promise<MiniLoginResult> => {
  try {
    const result = await wxRequest.post<MiniLoginResult>('/mini/auth/login', params, {
      showLoading: true,
      loadingText: '登录中...'
    })
    
    // 保存token
    wxRequest.setToken(result.token)
    
    // 转换角色
    const role = result.userType === 0 ? 'MEMBER' : result.userType === 1 ? 'COACH' : null
    
    // 构造统一的数据结构
    const userInfo = {
      ...result,
      role: role,
      memberId: result.userType === 0 ? result.userId : undefined,
      memberNo: result.userType === 0 ? result.memberNo : undefined,
      coachId: result.userType === 1 ? result.userId : undefined
    }
    
    // 保存用户信息
    wx.setStorageSync('userInfo', userInfo)
    
    return result
  } catch (error) {
    throw error
  }
}

/**
 * 退出登录
 * POST /auth/logout
 */
export const logout = () => {
  return wxRequest.post('/auth/logout')
}

/**
 * 刷新token
 * POST /auth/refresh-token
 */
export const refreshToken = () => {
  return wxRequest.post('/auth/refresh-token')
}