// 配置相关API

import { wxRequest } from '../../utils/request'
import { CheckinRules } from '../types/common.types'

/**
 * 获取签到规则配置
 * GET /mini/config/checkin-rules
 */
export const getCheckinRules = () => {
  return wxRequest.get<CheckinRules>('/mini/config/checkin-rules', null, {
    showLoading: false
  })
}

/**
 * 获取系统配置
 * GET /settings/systemConfig
 */
export const getSystemConfig = () => {
  return wxRequest.get('/settings/systemConfig')
}