import request from '@/utils/request'
import type { Coach, QueryParams, PaginatedResponse, ApiResponse } from '@/types'
import { ApiPaths } from '@/utils/constants'

export const coachApi = {
  /**
   * 获取教练列表
   */
  getCoaches(params?: QueryParams): Promise<PaginatedResponse<Coach>> {
    return request({
      url: ApiPaths.COACHES,
      method: 'GET',
      params
    })
  },

  /**
   * 获取教练详情
   */
  getCoachById(id: number): Promise<Coach> {
    return request({
      url: ApiPaths.COACH_DETAIL(id),
      method: 'GET'
    })
  },

  /**
   * 创建教练
   */
  createCoach(data: Partial<Coach>): Promise<Coach> {
    return request({
      url: ApiPaths.COACHES,
      method: 'POST',
      data
    })
  },

  /**
   * 更新教练
   */
  updateCoach(id: number, data: Partial<Coach>): Promise<Coach> {
    return request({
      url: ApiPaths.COACH_DETAIL(id),
      method: 'PUT',
      data
    })
  },

  /**
   * 删除教练
   */
  deleteCoach(id: number): Promise<void> {
    return request({
      url: ApiPaths.COACH_DETAIL(id),
      method: 'DELETE'
    })
  },

  /**
   * 获取教练统计
   */
  getStatistics(): Promise<any> {
    return request({
      url: ApiPaths.COACH_STATISTICS,
      method: 'GET'
    })
  },

  /**
   * 获取教练的课程列表
   */
  getCoachCourses(coachId: number, params?: QueryParams): Promise<PaginatedResponse<any>> {
    return request({
      url: `/coaches/${coachId}/courses`,
      method: 'GET',
      params
    })
  },

  /**
   * 获取教练的学员列表
   */
  getCoachMembers(coachId: number, params?: QueryParams): Promise<PaginatedResponse<any>> {
    return request({
      url: `/coaches/${coachId}/members`,
      method: 'GET',
      params
    })
  },

  /**
   * 获取教练的业绩统计
   */
  getCoachPerformance(coachId: number, params?: QueryParams): Promise<any> {
    return request({
      url: `/coaches/${coachId}/performance`,
      method: 'GET',
      params
    })
  },

  /**
   * 导出教练数据
   */
  exportCoaches(params?: QueryParams): Promise<Blob> {
    return request({
      url: '/coaches/export',
      method: 'GET',
      params,
      responseType: 'blob'
    })
  }
}