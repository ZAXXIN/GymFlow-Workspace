import request from '@/utils/request'
import type { 
  User, 
  UserFormData, 
  QueryParams, 
  PaginatedResponse, 
  ApiResponse,
  ChangePasswordData,
  UpdateProfileData
} from '@/types'
import { ApiPaths } from '@/utils/constants'

export const userApi = {
  /**
   * 获取当前登录用户信息
   */
  getCurrentUser(): Promise<ApiResponse<User>> {
    return http.get(ApiPaths.USER_INFO)
  },

  /**
   * 更新当前用户信息
   */
  updateCurrentUser(data: UpdateProfileData): Promise<ApiResponse<User>> {
    return http.put(ApiPaths.USER_INFO, data)
  },

  /**
   * 修改密码
   */
  changePassword(data: ChangePasswordData): Promise<ApiResponse<void>> {
    return http.put(ApiPaths.USER_PASSWORD, data)
  },

  /**
   * 获取用户列表
   */
  getUsers(params?: QueryParams): Promise<PaginatedResponse<User>> {
    return http.get(ApiPaths.USERS, { params })
  },

  /**
   * 获取用户详情
   */
  getUserById(id: number): Promise<ApiResponse<User>> {
    return http.get(ApiPaths.USER_DETAIL(id))
  },

  /**
   * 创建用户
   */
  createUser(data: UserFormData): Promise<ApiResponse<User>> {
    return http.post(ApiPaths.USERS, data)
  },

  /**
   * 更新用户
   */
  updateUser(id: number, data: UserFormData): Promise<ApiResponse<User>> {
    return http.put(ApiPaths.USER_DETAIL(id), data)
  },

  /**
   * 删除用户
   */
  deleteUser(id: number): Promise<ApiResponse<void>> {
    return http.delete(ApiPaths.USER_DETAIL(id))
  },

  /**
   * 启用用户
   */
  enableUser(id: number): Promise<ApiResponse<User>> {
    return http.put(ApiPaths.USER_ENABLE(id))
  },

  /**
   * 禁用用户
   */
  disableUser(id: number): Promise<ApiResponse<User>> {
    return http.put(ApiPaths.USER_DISABLE(id))
  },

  /**
   * 重置用户密码
   */
  resetUserPassword(id: number): Promise<ApiResponse<{ newPassword: string }>> {
    return http.post(ApiPaths.USER_RESET_PASSWORD(id))
  },

  /**
   * 获取用户角色列表
   */
  getUserRoles(): Promise<ApiResponse<string[]>> {
    return http.get(ApiPaths.USER_ROLES)
  },

  /**
   * 分配用户角色
   */
  assignUserRole(userId: number, roleIds: number[]): Promise<ApiResponse<void>> {
    return http.post(ApiPaths.USER_ASSIGN_ROLES(userId), { roleIds })
  },

  /**
   * 获取用户权限列表
   */
  getUserPermissions(userId: number): Promise<ApiResponse<string[]>> {
    return http.get(ApiPaths.USER_PERMISSIONS(userId))
  },

  /**
   * 获取用户登录日志
   */
  getUserLoginLogs(userId: number, params?: QueryParams): Promise<PaginatedResponse<any>> {
    return http.get(ApiPaths.USER_LOGIN_LOGS(userId), { params })
  },

  /**
   * 获取用户操作日志
   */
  getUserOperationLogs(userId: number, params?: QueryParams): Promise<PaginatedResponse<any>> {
    return http.get(ApiPaths.USER_OPERATION_LOGS(userId), { params })
  },

  /**
   * 更新用户头像
   */
  updateUserAvatar(userId: number, avatarFile: File): Promise<ApiResponse<{ avatarUrl: string }>> {
    const formData = new FormData()
    formData.append('avatar', avatarFile)
    return http.put(ApiPaths.USER_AVATAR(userId), formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 批量删除用户
   */
  batchDeleteUsers(ids: number[]): Promise<ApiResponse<void>> {
    return http.post(ApiPaths.USERS_BATCH_DELETE, { ids })
  },

  /**
   * 批量启用用户
   */
  batchEnableUsers(ids: number[]): Promise<ApiResponse<void>> {
    return http.post(ApiPaths.USERS_BATCH_ENABLE, { ids })
  },

  /**
   * 批量禁用用户
   */
  batchDisableUsers(ids: number[]): Promise<ApiResponse<void>> {
    return http.post(ApiPaths.USERS_BATCH_DISABLE, { ids })
  },

  /**
   * 导出用户数据
   */
  exportUsers(params?: QueryParams): Promise<Blob> {
    return http.get(ApiPaths.USERS_EXPORT, {
      params,
      responseType: 'blob'
    })
  },

  /**
   * 导入用户数据
   */
  importUsers(file: File): Promise<ApiResponse<{ success: number; failure: number }>> {
    const formData = new FormData()
    formData.append('file', file)
    return http.post(ApiPaths.USERS_IMPORT, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  /**
   * 获取用户统计信息
   */
  getUserStatistics(): Promise<ApiResponse<{
    total: number
    active: number
    inactive: number
    todayNew: number
    weekNew: number
    monthNew: number
  }>> {
    return http.get(ApiPaths.USER_STATISTICS)
  },

  /**
   * 获取用户在线状态
   */
  getUserOnlineStatus(userId: number): Promise<ApiResponse<{ isOnline: boolean; lastActiveTime?: string }>> {
    return http.get(ApiPaths.USER_ONLINE_STATUS(userId))
  },

  /**
   * 强制用户下线
   */
  forceUserLogout(userId: number): Promise<ApiResponse<void>> {
    return http.post(ApiPaths.USER_FORCE_LOGOUT(userId))
  },

  /**
   * 解锁用户
   */
  unlockUser(userId: number): Promise<ApiResponse<User>> {
    return http.put(ApiPaths.USER_UNLOCK(userId))
  },

  /**
   * 获取用户会话列表
   */
  getUserSessions(userId: number): Promise<ApiResponse<any[]>> {
    return http.get(ApiPaths.USER_SESSIONS(userId))
  },

  /**
   * 清除用户所有会话
   */
  clearUserSessions(userId: number): Promise<ApiResponse<void>> {
    return http.delete(ApiPaths.USER_SESSIONS(userId))
  },

  /**
   * 发送验证码到用户邮箱/手机
   */
  sendVerificationCode(type: 'email' | 'sms', contact: string): Promise<ApiResponse<void>> {
    return http.post(ApiPaths.USER_SEND_VERIFICATION_CODE, { type, contact })
  },

  /**
   * 验证用户邮箱/手机
   */
  verifyUserContact(type: 'email' | 'sms', contact: string, code: string): Promise<ApiResponse<void>> {
    return http.post(ApiPaths.USER_VERIFY_CONTACT, { type, contact, code })
  }
}