// 教练端专用API

import { wxRequest } from '../../utils/request'
import { 
  CoachInfo,
  CoachSchedule,
  CoachCourse,
  CourseStudent,
  CoachMemberDetail,
  // FinanceStatsResult,
  // FinanceStatsParams
} from '../types/coach.types'
import { HealthRecord } from '../types/common.types'

/**
 * 获取当前登录教练信息
 * GET /mini/coach/my-info
 */
export const getMyCoachInfo = () => {
  return wxRequest.get<CoachInfo>('/mini/coach/my-info', null, {
    showLoading: true
  })
}

/**
 * 获取教练详情
 * GET /coach/detail/{coachId}
 */
export const getCoachDetail = (coachId: number) => {
  return wxRequest.get(`/coach/detail/${coachId}`)
}

/**
 * 获取我的课表
 * GET /mini/coach/my-schedule
 */
export const getMySchedule = (date?: string) => {
  return wxRequest.get<CoachCourse[]>('/mini/coach/my-schedule', { date })
}

/**
 * 获取课程学员列表
 * GET /mini/coach/course-students/{courseId}
 */
export const getCourseStudents = (courseId: number) => {
  return wxRequest.get<CourseStudent[]>(`/mini/coach/course-students/${courseId}`, null, {
    showLoading: true
  })
}

/**
 * 获取教练视角的会员详情
 * GET /mini/coach/member-detail/{memberId}
 */
export const getCoachMemberDetail = (memberId: number) => {
  return wxRequest.get<CoachMemberDetail>(`/mini/coach/member-detail/${memberId}`, null, {
    showLoading: true
  })
}

/**
 * 获取教练排班列表
 * GET /coach/schedules/{coachId}
 */
export const getCoachSchedules = (coachId: number) => {
  return wxRequest.get<CoachSchedule[]>(`/coach/schedules/${coachId}`)
}

/**
 * 获取教练课程列表
 * GET /coach/courses/{coachId}
 */
export const getCoachCourses = (coachId: number) => {
  return wxRequest.get(`/coach/courses/${coachId}`)
}

/**
 * 获取财务统计数据
 * GET /mini/coach/finance/stats
 */
// export const getFinanceStats = (params: FinanceStatsParams) => {
//   return wxRequest.get<FinanceStatsResult>('/mini/coach/finance/stats', params, {
//     showLoading: true
//   })
// }

/**
 * 教练添加会员健康档案
 * POST /mini/coach/add-health-record/{memberId}
 */
export const coachAddHealthRecord = (memberId: number, data: HealthRecord) => {
  return wxRequest.post(`/mini/coach/add-health-record/${memberId}`, data, {
    showLoading: true,
    loadingText: '保存中...'
  })
}