import { http } from '@/utils/request'
import type { Coach, QueryParams, PaginatedResponse, ApiResponse } from '@/types'
import { ApiPaths } from '@/utils/constants'

export const coachApi = {
  /**
   * 获取教练列表
   */
  getCoaches(params?: QueryParams): Promise<PaginatedResponse<Coach>> {
    return http.get(ApiPaths.COACHES, { params })
  },

  /**
   * 获取教练详情
   */
  getCoachById(id: number): Promise<Coach> {
    return http.get(ApiPaths.COACH_DETAIL(id))
  },

  /**
   * 创建教练
   */
  createCoach(data: Partial<Coach>): Promise<Coach> {
    return http.post(ApiPaths.COACHES, data)
  },

  /**
   * 更新教练
   */
  updateCoach(id: number, data: Partial<Coach>): Promise<Coach> {
    return http.put(ApiPaths.COACH_DETAIL(id), data)
  },

  /**
   * 删除教练
   */
  deleteCoach(id: number): Promise<void> {
    return http.delete(ApiPaths.COACH_DETAIL(id))
  },

  /**
   * 获取教练统计
   */
  getStatistics(): Promise<any> {
    return http.get(ApiPaths.COACH_STATISTICS)
  },

  /**
   * 获取教练的课程列表
   */
  getCoachCourses(coachId: number, params?: QueryParams): Promise<PaginatedResponse<any>> {
    return http.get(`/coaches/${coachId}/courses`, { params })
  },

  /**
   * 获取教练的学员列表
   */
  getCoachMembers(coachId: number, params?: QueryParams): Promise<PaginatedResponse<any>> {
    return http.get(`/coaches/${coachId}/members`, { params })
  },

  /**
   * 获取教练的业绩统计
   */
  getCoachPerformance(coachId: number, params?: QueryParams): Promise<any> {
    return http.get(`/coaches/${coachId}/performance`, { params })
  },

  /**
   * 导出教练数据
   */
  exportCoaches(params?: QueryParams): Promise<Blob> {
    return http.get('/coaches/export', {
      params,
      responseType: 'blob'
    })
  }
}