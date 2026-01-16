import { http } from '@/utils/request'
import type { Member, QueryParams, PaginatedResponse, HealthRecord, ApiResponse } from '@/types'
import { ApiPaths } from '@/utils/constants'

export const memberApi = {
  /**
   * 获取会员列表
   */
  getMembers(params?: QueryParams): Promise<PaginatedResponse<Member>> {
    return http.get(ApiPaths.MEMBERS, { params })
  },

  /**
   * 获取会员详情
   */
  getMemberById(id: number): Promise<Member> {
    return http.get(ApiPaths.MEMBER_DETAIL(id))
  },

  /**
   * 创建会员
   */
  createMember(data: Partial<Member>): Promise<Member> {
    return http.post(ApiPaths.MEMBERS, data)
  },

  /**
   * 更新会员
   */
  updateMember(id: number, data: Partial<Member>): Promise<Member> {
    return http.put(ApiPaths.MEMBER_DETAIL(id), data)
  },

  /**
   * 删除会员
   */
  deleteMember(id: number): Promise<void> {
    return http.delete(ApiPaths.MEMBER_DETAIL(id))
  },

  /**
   * 获取会员统计
   */
  getStatistics(): Promise<any> {
    return http.get(ApiPaths.MEMBER_STATISTICS)
  },

  /**
   * 导出会员数据
   */
  exportMembers(params?: QueryParams): Promise<Blob> {
    return http.get('/members/export', {
      params,
      responseType: 'blob'
    })
  },

  /**
   * 获取会员健康档案
   */
  getHealthRecords(memberId: number): Promise<HealthRecord[]> {
    return http.get(`/members/${memberId}/health-records`)
  },

  /**
   * 添加健康记录
   */
  addHealthRecord(memberId: number, data: Partial<HealthRecord>): Promise<HealthRecord> {
    return http.post(`/members/${memberId}/health-records`, data)
  },

  /**
   * 更新健康记录
   */
  updateHealthRecord(memberId: number, recordId: number, data: Partial<HealthRecord>): Promise<HealthRecord> {
    return http.put(`/members/${memberId}/health-records/${recordId}`, data)
  },

  /**
   * 删除健康记录
   */
  deleteHealthRecord(memberId: number, recordId: number): Promise<void> {
    return http.delete(`/members/${memberId}/health-records/${recordId}`)
  },

  /**
   * 获取会员训练计划
   */
  getTrainingPlans(memberId: number): Promise<any[]> {
    return http.get(`/members/${memberId}/training-plans`)
  },

  /**
   * 续费会籍
   */
  renewMembership(memberId: number, months: number): Promise<Member> {
    return http.post(`/members/${memberId}/renew`, { months })
  },

  /**
   * 暂停会籍
   */
  suspendMembership(memberId: number, days: number, reason: string): Promise<Member> {
    return http.post(`/members/${memberId}/suspend`, { days, reason })
  },

  /**
   * 恢复会籍
   */
  resumeMembership(memberId: number): Promise<Member> {
    return http.post(`/members/${memberId}/resume`)
  }
}