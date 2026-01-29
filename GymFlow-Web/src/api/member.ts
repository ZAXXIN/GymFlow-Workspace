import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'
import type { 
  MemberQueryDTO,
  MemberListVO,
  PageResult,
  MemberFullDTO,
  MemberBasicDTO,
  HealthRecordDTO,
  MemberCardDTO,
  MemberAddRequest,
  MemberUpdateRequest
} from '@/types/member'

export const memberApi = {
  /**
   * 分页查询会员列表
   */
  getMemberList(data: MemberQueryDTO): Promise<ApiResponse<PageResult<MemberListVO>>> {
    return request({
      url: '/member/list',
      method: 'POST',
      data
    })
  },

  /**
   * 获取会员详情
   */
  getMemberDetail(memberId: number): Promise<ApiResponse<MemberFullDTO>> {
    return request({
      url: `/member/detail/${memberId}`,
      method: 'GET'
    })
  },

  /**
   * 添加会员
   */
  addMember(data: MemberAddRequest): Promise<ApiResponse<number>> {
    return request({
      url: '/member/add',
      method: 'POST',
      data
    })
  },

  /**
   * 更新会员信息
   */
  updateMember(memberId: number, data: MemberUpdateRequest): Promise<ApiResponse> {
    return request({
      url: `/member/update/${memberId}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 删除会员（软删除）
   */
  deleteMember(memberId: number): Promise<ApiResponse> {
    return request({
      url: `/member/delete/${memberId}`,
      method: 'DELETE'
    })
  },

  /**
   * 批量删除会员
   */
  batchDeleteMember(memberIds: number[]): Promise<ApiResponse> {
    return request({
      url: '/member/batch-delete',
      method: 'POST',
      data: memberIds
    })
  },

  /**
   * 启用/禁用会员
   */
  // changeMemberStatus(memberId: number, status: number): Promise<ApiResponse> {
  //   return request({
  //     url: `/member/change-status/${memberId}`,
  //     method: 'PUT',
  //     params: { status }
  //   })
  // },

  /**
   * 续费会员卡
   */
  renewMemberCard(memberId: number, data: MemberCardDTO): Promise<ApiResponse> {
    return request({
      url: `/member/renew-card/${memberId}`,
      method: 'POST',
      data
    })
  },

  /**
   * 获取健康档案列表
   */
  getHealthRecords(memberId: number): Promise<ApiResponse<HealthRecordDTO[]>> {
    return request({
      url: `/member/health-records/${memberId}`,
      method: 'GET'
    })
  },

  /**
   * 添加健康档案
   */
  addHealthRecord(memberId: number, data: HealthRecordDTO): Promise<ApiResponse> {
    return request({
      url: `/member/add-health-record/${memberId}`,
      method: 'POST',
      data
    })
  },

  /**
   * 检查用户名是否存在
   */
  checkUsernameExists(username: string): Promise<ApiResponse<boolean>> {
    return request({
      url: `/member/check-username/${username}`,
      method: 'GET'
    })
  },

  /**
   * 检查手机号是否存在
   */
  // checkPhoneExists(phone: string): Promise<ApiResponse<boolean>> {
  //   return request({
  //     url: `/api/member/check-phone/${phone}`,
  //     method: 'GET'
  //   })
  // },

  /**
   * 导出会员列表
   */
  // exportMembers(data: MemberQueryDTO): Promise<Blob> {
  //   return request({
  //     url: '/api/member/export',
  //     method: 'POST',
  //     data,
  //     responseType: 'blob'
  //   })
  // },

  /**
   * 导入会员数据
   */
  // importMembers(file: File): Promise<ApiResponse> {
  //   const formData = new FormData()
  //   formData.append('file', file)
    
  //   return request({
  //     url: '/api/member/import',
  //     method: 'POST',
  //     data: formData,
  //     headers: {
  //       'Content-Type': 'multipart/form-data'
  //     }
  //   })
  // },

  /**
   * 获取会员统计数据
   */
  // getMemberStats(): Promise<ApiResponse<{
  //   totalMembers: number
  //   activeMembers: number
  //   newMembersToday: number
  //   expiringMembers: number
  // }>> {
  //   return request({
  //     url: '/api/member/stats',
  //     method: 'GET'
  //   })
  // }
}