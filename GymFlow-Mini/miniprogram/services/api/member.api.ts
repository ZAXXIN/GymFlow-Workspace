// 会员端专用API

import { wxRequest } from '../../utils/request'
import { 
  MemberInfo,
  HealthRecord,
  MemberCard,
  PageResult
} from '../types/common.types'
import { Order } from '../types/order.types'
import { CourseBooking } from '../types/booking.types'
import { userStore } from '../../stores/user.store'

/**
 * 获取当前登录会员信息
 * GET /mini/member/my-info
 */
export const getMyMemberInfo = () => {
  return wxRequest.get<MemberInfo>('/mini/member/my-info', null, {
    showLoading: true
  })
}

/**
 * 获取会员详情
 * GET /member/detail/{memberId}  废弃my-info返回
 */
// export const getMemberDetail = (memberId) => {
//   return wxRequest.get(`/member/detail/${memberId}`)
// }

/**
 * 获取健康档案列表
 * GET /member/health-records/{memberId}
 */
export const getHealthRecords = (memberId) => {
  return wxRequest.get<HealthRecord[]>(`/member/health-records/${memberId}`)
}

/**
 * 添加健康档案
 * POST /member/add-health-record/{memberId}
 */
export const addHealthRecord = (memberId, data) => {
  return wxRequest.post(`/member/add-health-record/${memberId}`, data, {
    showLoading: true,
    loadingText: '保存中...'
  })
}

/**
 * 获取会员卡信息 - 已废弃，由my-info接口返回
 * 保留方法但返回空数组，避免报错
 */
export const getMyCards = () => {
  console.warn('getMyCards接口已废弃，请使用my-info接口获取会员卡信息')
  return Promise.resolve([])
}

/**
 * 获取我的预约列表
 * GET /mini/member/my-bookings
 */
export const getMyBookings = (params) => {
  return wxRequest.get<PageResult<CourseBooking>>('/mini/member/my-bookings', params)
}

/**
 * 获取我的课程记录
 * GET /mini/member/my-courses
 */
export const getMyCourses = () => {
  return wxRequest.get('/mini/member/my-courses')
}

/**
 * 续费会员卡
 * POST /member/renew-card/{memberId}
 */
// export const renewMemberCard = (memberId, data) => {
//   return wxRequest.post(`/member/renew-card/${memberId}`, data)
// }