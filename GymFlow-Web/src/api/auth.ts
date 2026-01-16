import { http } from '@/utils/request'
import type { LoginDTO, LoginVO, User, ApiResponse } from '@/types'
import { ApiPaths } from '@/utils/constants'

export const authApi = {
  /**
   * 用户登录
   */
  login(data: LoginDTO): Promise<LoginVO> {
    return http.post(ApiPaths.LOGIN, data)
  },

  /**
   * 用户登出
   */
  logout(): Promise<void> {
    return http.post(ApiPaths.LOGOUT)
  },

  /**
   * 刷新token
   */
  refreshToken(): Promise<{ token: string }> {
    return http.post(ApiPaths.REFRESH_TOKEN)
  },

  /**
   * 获取当前用户信息
   */
  getUserInfo(): Promise<User> {
    return http.get(ApiPaths.USER_INFO)
  },

  /**
   * 修改密码
   */
  changePassword(oldPassword: string, newPassword: string): Promise<void> {
    return http.post('/auth/change-password', { oldPassword, newPassword })
  },

  /**
   * 重置密码
   */
  resetPassword(username: string, email: string): Promise<void> {
    return http.post('/auth/reset-password', { username, email })
  }
}