// src/api/checkIn.ts
import request from '@/utils/request'
import type {
  CheckInQueryParams,
  CheckInListVO,
  CheckInDetailVO,
  CheckInStatsDTO,
  CheckInReportDTO,
  PageResultVO,
  ApiResponse
} from '@/types/checkIn'

// 签到管理API
export const checkInApi = {
  /**
   * 分页查询签到列表
   */
  getCheckInList(params: CheckInQueryParams): Promise<ApiResponse<PageResultVO<CheckInListVO>>> {
    return request({
      url: '/checkin/list',
      method: 'POST',
      data: params
    })
  },

  /**
   * 获取签到详情
   */
  getCheckInDetail(checkInId: number): Promise<ApiResponse<CheckInDetailVO>> {
    return request({
      url: `/checkin/detail/${checkInId}`,
      method: 'GET'
    })
  },

  /**
   * 会员签到（自由训练）
   */
  memberCheckIn(memberId: number, checkinMethod: number, notes?: string): Promise<ApiResponse> {
    return request({
      url: `/checkin/member/${memberId}`,
      method: 'POST',
      params: { checkinMethod, notes }
    })
  },

  /**
   * 课程签到
   */
  courseCheckIn(bookingId: number, checkinMethod: number, notes?: string): Promise<ApiResponse> {
    return request({
      url: `/checkin/course/${bookingId}`,
      method: 'POST',
      params: { checkinMethod, notes }
    })
  },

  /**
 * 通过数字码核销课程（PC端专用）
 */
  verifyByCode(checkinCode: string, checkinMethod: number, notes?: string): Promise<ApiResponse> {
    return request({
      url: '/checkin/verify-code',
      method: 'POST',
      params: { checkinCode, checkinMethod, notes }
    })
  },

  /**
   * 更新签到信息
   */
  updateCheckIn(checkInId: number, notes: string): Promise<ApiResponse> {
    return request({
      url: `/checkin/update/${checkInId}`,
      method: 'PUT',
      params: { notes }
    })
  },

  /**
   * 删除签到记录
   */
  deleteCheckIn(checkInId: number): Promise<ApiResponse> {
    return request({
      url: `/checkin/delete/${checkInId}`,
      method: 'DELETE'
    })
  },

  /**
   * 批量删除签到记录
   */
  batchDeleteCheckIns(checkInIds: number[]): Promise<ApiResponse> {
    return request({
      url: '/checkin/batch-delete',
      method: 'POST',
      data: checkInIds
    })
  },

  /**
   * 获取会员签到记录
   */
  getMemberCheckIns(memberId: number, params: CheckInQueryParams): Promise<ApiResponse<PageResultVO<CheckInListVO>>> {
    return request({
      url: `/checkin/member-list/${memberId}`,
      method: 'POST',
      data: params
    })
  },

  /**
   * 获取今日签到统计
   */
  getTodayCheckInStats(): Promise<ApiResponse<CheckInStatsDTO>> {
    return request({
      url: '/checkin/stats/today',
      method: 'GET'
    })
  },

  /**
   * 获取签到统计报表
   */
  getCheckInReport(params: CheckInQueryParams): Promise<ApiResponse<CheckInReportDTO>> {
    return request({
      url: '/checkin/report',
      method: 'POST',
      data: params
    })
  }
}