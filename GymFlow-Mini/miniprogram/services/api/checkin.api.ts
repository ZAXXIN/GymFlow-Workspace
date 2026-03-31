// 签到相关API

import { wxRequest } from '../../utils/request'
import { 
  CheckinRecord,
  CheckinCode,
  ScanCheckinParams,
  VerifyCodeParams,
  CurrentReminder,
  PageResult
} from '../types/checkin.types'
import { CheckinRules } from '../types/common.types'

/**
 * 获取签到码
 * GET /mini/checkin/code/{bookingId}
 */
// export const getCheckinCode = (bookingId: number) => {
//   return wxRequest.get<CheckinCode>(`/mini/checkin/code/${bookingId}`, null, {
//     showLoading: true
//   })
// }

/**
 * 扫码核销
 * POST /mini/checkin/scan
 */
export const scanCheckin = (params: ScanCheckinParams) => {
  return wxRequest.post('/mini/checkin/scan', params, {
    showLoading: true,
    loadingText: '核销中...'
  })
}

/**
 * 数字码核销
 * POST /mini/checkin/verify-code
 */
export const verifyCodeCheckin = (params: VerifyCodeParams) => {
  return wxRequest.post('/mini/checkin/verify-code', params, {
    showLoading: true,
    loadingText: '核销中...'
  })
}

/**
 * 获取当前时段提醒信息
 * GET /mini/checkin/current-reminder
 */
export const getCurrentReminder = () => {
  return wxRequest.get<CurrentReminder>('/mini/checkin/current-reminder')
}

/**
 * 获取签到规则配置
 * GET /mini/config/checkin-rules
 */
export const getCheckinRules = () => {
  return wxRequest.get<CheckinRules>('/mini/config/checkin-rules')
}

/**
 * 获取签到列表
 * POST /checkin/list
 */
export const getCheckinList = (params: any) => {
  return wxRequest.post<PageResult<CheckinRecord>>('/checkin/list', params)
}

/**
 * 获取今日签到统计
 * GET /checkin/stats/today
 */
export const getTodayCheckinStats = () => {
  return wxRequest.get('/checkin/stats/today')
}