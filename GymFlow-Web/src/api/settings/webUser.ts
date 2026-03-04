import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'

// 用户查询参数
export interface WebUserQueryParams {
  pageNum?: number
  pageSize?: number
  username?: string
  realName?: string
  role?: number
  status?: number
}

// 用户基础信息DTO
export interface WebUserBasicDTO {
  username: string
  password?: string
  realName: string
  role: number
  status: number
}

// 用户详情DTO
export interface WebUserDetailDTO {
  id: number
  username: string
  realName: string
  role: number
  roleDesc: string
  status: number
  statusDesc: string
  createTime: string
  updateTime: string
}

// 用户列表VO
export interface WebUserListVO {
  id: number
  username: string
  realName: string
  role: number
  roleDesc: string
  status: number
  statusDesc: string
  createTime: string
  updateTime: string
}

// 分页结果
export interface PageResultVO<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages?: number
}

// 用户管理API
export const webUserApi = {
  /**
   * 分页查询用户列表
   */
  getUserList(params: WebUserQueryParams): Promise<ApiResponse<PageResultVO<WebUserListVO>>> {
    return request({
      url: '/settings/webUser/list',
      method: 'POST',
      data: params
    })
  },

  /**
   * 获取用户详情
   */
  getUserDetail(userId: number): Promise<ApiResponse<WebUserDetailDTO>> {
    return request({
      url: `/settings/webUser/detail/${userId}`,
      method: 'GET'
    })
  },

  /**
   * 添加用户
   */
  addUser(data: WebUserBasicDTO): Promise<ApiResponse<number>> {
    return request({
      url: '/settings/webUser/add',
      method: 'POST',
      data
    })
  },

  /**
   * 更新用户
   */
  updateUser(userId: number, data: WebUserBasicDTO): Promise<ApiResponse> {
    return request({
      url: `/settings/webUser/update/${userId}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 删除用户
   */
  deleteUser(userId: number): Promise<ApiResponse> {
    return request({
      url: `/settings/webUser/delete/${userId}`,
      method: 'DELETE'
    })
  },

  /**
   * 更新用户状态
   */
  updateUserStatus(userId: number, status: number): Promise<ApiResponse> {
    return request({
      url: `/settings/webUser/updateStatus/${userId}`,
      method: 'PUT',
      params: { status }
    })
  },

  /**
   * 重置密码
   */
  resetPassword(userId: number): Promise<ApiResponse> {
    return request({
      url: `/settings/webUser/resetPassword/${userId}`,
      method: 'PUT'
    })
  },

  /**
   * 检查用户名是否存在
   */
  checkUsernameExists(username: string, excludeUserId?: number): Promise<ApiResponse<boolean>> {
    return request({
      url: '/settings/webUser/check-username',
      method: 'GET',
      params: { username, excludeUserId }
    })
  }
}