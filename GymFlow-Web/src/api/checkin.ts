import { http } from '@/utils/request'
import type { CheckIn, QueryParams, PaginatedResponse, ApiResponse } from '@/types'
import { ApiPaths } from '@/utils/constants'

export const checkInApi = {
  /**
   * 获取签到记录
   */
  getCheckIns(params?: QueryParams): Promise<PaginatedResponse<CheckIn>> {
    return http.get(ApiPaths.CHECK_INS, { params })
  },

  /**
   * 创建签到记录
   */
  createCheckIn(data: Partial<CheckIn>): Promise<CheckIn> {
    return http.post(ApiPaths.CHECK_INS, data)
  },

  /**
   * 更新签到记录
   */
  updateCheckIn(id: number, data: Partial<CheckIn>): Promise<CheckIn> {
    return http.put(`/check-ins/${id}`, data)
  },

  /**
   * 删除签到记录
   */
  deleteCheckIn(id: number): Promise<void> {
    return http.delete(`/check-ins/${id}`)
  },

  /**
   * 获取签到统计
   */
  getStatistics(params?: QueryParams): Promise<any> {
    return http.get(ApiPaths.CHECK_IN_STATISTICS, { params })
  },

  /**
   * 获取今日签到
   */
  getTodayCheckIns(params?: QueryParams): Promise<PaginatedResponse<CheckIn>> {
    return http.get('/check-ins/today', { params })
  },

  /**
   * 批量签到
   */
  batchCheckIn(data: { memberIds: number[]; type: string }): Promise<void> {
    return http.post('/check-ins/batch', data)
  },

  /**
   * 导出签到数据
   */
  exportCheckIns(params?: QueryParams): Promise<Blob> {
    return http.get('/check-ins/export', {
      params,
      responseType: 'blob'
    })
  },

  /**
   * 生成签到码
   */
  generateCheckInCode(memberId: number): Promise<{ code: string; expiresAt: string }> {
    return http.post(`/check-ins/${memberId}/generate-code`)
  },

  /**
   * 验证签到码
   */
  verifyCheckInCode(code: string): Promise<CheckIn> {
    return http.post('/check-ins/verify-code', { code })
  }
}