import request from '@/utils/request'
import type { LoginDTO, LoginResultDTO, ApiResponse } from '@/types/auth'
import { ApiPaths } from '@/utils/constants'

export const authApi = {
   /**
   * 用户登录
   */
  login(data: LoginDTO): Promise<ApiResponse<LoginResultDTO>>{
    return request({
      url: '/auth/login',
      method: 'POST',
      data
    })
  },

  /**
   * 用户登出
   */
  logout(): Promise<ApiResponse>{
    return request({
      url: '/auth/logout',
      method: 'POST'
    })
  },

  /**
   * 获取用户信息
   */
  getUserInfo(): Promise<ApiResponse>{
    return request({
      url: '/user/info',
      method: 'GET'
    })
  }
}