// 签到相关自定义Hook

import { configStore } from '../stores/config.store'
import { 
  getCheckinCode,
  verifyCodeCheckin,
  getCurrentReminder 
} from '../services/api/checkin.api'
import { showSuccess, showError, showLoading, hideLoading, showModal } from '../utils/wx-util'

export const useCheckin = () => {
  /**
   * 获取签到码
   */
  const getCheckinCodeForBooking = async (bookingId: number) => {
    try {
      showLoading('生成签到码中...')
      const code = await getCheckinCode(bookingId)
      hideLoading()
      return code
    } catch (error: any) {
      hideLoading()
      showError(error.message || '获取签到码失败')
      throw error
    }
  }

  /**
   * 数字码核销
   */
  const verifyCode = async (bookingId: number, checkinCode: string) => {
    try {
      // 验证码格式
      if (!/^\d{6}$/.test(checkinCode)) {
        showError('请输入6位数字签到码')
        return false
      }
      
      showLoading('核销中...')
      
      await verifyCodeCheckin({ bookingId, checkinCode })
      
      hideLoading()
      showSuccess('核销成功')
      
      return true
    } catch (error: any) {
      hideLoading()
      showError(error.message || '核销失败')
      throw error
    }
  }

  /**
   * 获取当前时段提醒
   */
  const getCurrentReminderInfo = async () => {
    try {
      return await getCurrentReminder()
    } catch (error: any) {
      console.error('获取提醒失败:', error)
      return null
    }
  }

  /**
   * 检查是否可签到
   */
  const canCheckin = (courseDate: string, startTime: string): boolean => {
    return configStore.canCheckin(courseDate, startTime)
  }

  /**
   * 获取签到剩余时间（分钟）
   */
  const getCheckinRemainingMinutes = (courseDate: string, startTime: string): number => {
    const now = new Date()
    const courseStart = new Date(`${courseDate} ${startTime}`)
    const checkinStart = new Date(courseStart.getTime() - configStore.checkinStartMinutes * 60000)
    
    if (now < checkinStart) {
      // 签到未开始
      return Math.ceil((checkinStart.getTime() - now.getTime()) / 60000)
    } else if (now > courseStart && configStore.checkinEndMinutes === 0) {
      // 已过签到时间
      return 0
    } else if (configStore.checkinEndMinutes > 0) {
      const checkinEnd = new Date(courseStart.getTime() + configStore.checkinEndMinutes * 60000)
      if (now > checkinEnd) {
        return 0
      }
      return Math.ceil((checkinEnd.getTime() - now.getTime()) / 60000)
    } else {
      // 签到进行中
      return Math.ceil((courseStart.getTime() - now.getTime()) / 60000)
    }
  }

  /**
   * 生成二维码内容
   */
  const generateQRCodeContent = (bookingId: number, code: string): string => {
    // 格式：gymflow://checkin?bookingId=123&code=123456
    return `gymflow://checkin?bookingId=${bookingId}&code=${code}`
  }

  return {
    getCheckinCode: getCheckinCodeForBooking,
    verifyCode,
    getCurrentReminder: getCurrentReminderInfo,
    canCheckin,
    getCheckinRemainingMinutes,
    generateQRCodeContent
  }
}