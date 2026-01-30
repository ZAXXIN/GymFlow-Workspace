import request from '@/utils/request'
import type {
  CoachQueryParams,
  CoachBasicDTO,
  CoachScheduleDTO,
  CoachDetail,
  CoachListVO,
  PageResultVO,
  CoachScheduleList,
  CoachCourseList
} from '@/types/coach'
import type { ApiResponse } from '@/types/auth' // 复用通用响应类型

// 教练管理API
export const coachApi = {
  /**
   * 分页查询教练列表
   */
  getCoachList(params: CoachQueryParams): Promise<ApiResponse<PageResultVO<CoachListVO>>> {
    return request({
      url: '/coach/list',
      method: 'GET',
      params
    })
  },

  /**
   * 获取教练详情
   */
  getCoachDetail(coachId: number): Promise<ApiResponse<CoachDetail>> {
    return request({
      url: `/coach/detail/${coachId}`,
      method: 'GET'
    })
  },

  /**
   * 添加教练
   */
  addCoach(data: CoachBasicDTO): Promise<ApiResponse<number>> {
    return request({
      url: '/coach/add',
      method: 'POST',
      data
    })
  },

  /**
   * 更新教练信息
   */
  updateCoach(coachId: number, data: CoachBasicDTO): Promise<ApiResponse> {
    return request({
      url: `/coach/update/${coachId}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 删除教练
   */
  deleteCoach(coachId: number): Promise<ApiResponse> {
    return request({
      url: `/coach/delete/${coachId}`,
      method: 'DELETE'
    })
  },

  /**
   * 批量删除教练
   */
  // batchDeleteCoach(ids: number[]): Promise<ApiResponse> {
  //   return request({
  //     url: '/coach/batchDelete',
  //     method: 'DELETE',
  //     data: ids // DELETE请求传递请求体
  //   })
  // },

  /**
   * 更新教练状态
   */
  updateCoachStatus(coachId: number, status: number): Promise<ApiResponse> {
    return request({
      url: `/coach/updateStatus/${coachId}`,
      method: 'PUT',
      params: { status }
    })
  },

  /**
   * 获取教练排班列表
   */
  getCoachSchedules(coachId: number): Promise<ApiResponse<CoachScheduleList[]>> {
    return request({
      url: `/coach/schedules/${coachId}`,
      method: 'GET'
    })
  },

  /**
   * 添加教练排班
   */
  addCoachSchedule(coachId: number, data: CoachScheduleDTO): Promise<ApiResponse> {
    return request({
      url: `/coach/schedule/add/${coachId}`,
      method: 'POST',
      data
    })
  },

  /**
   * 更新教练排班
   */
  updateCoachSchedule(scheduleId: number, data: CoachScheduleDTO): Promise<ApiResponse> {
    return request({
      url: `/coach/schedule/update/${scheduleId}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 删除教练排班
   */
  deleteCoachSchedule(scheduleId: number): Promise<ApiResponse> {
    return request({
      url: `/coach/schedule/delete/${scheduleId}`,
      method: 'DELETE'
    })
  },

  /**
   * 获取教练课程列表
   */
  getCoachCourses(coachId: number): Promise<ApiResponse<CoachCourseList[]>> {
    return request({
      url: `/coach/courses/${coachId}`,
      method: 'GET'
    })
  }
}