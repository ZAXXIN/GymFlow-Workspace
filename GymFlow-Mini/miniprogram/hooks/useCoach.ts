// 教练端相关自定义Hook

import { coachStore } from '../stores/coach.store' // 需要创建
import { 
  getMySchedule,
  getCourseStudents,
  getCoachMemberDetail,
  getFinanceStats,
  coachAddHealthRecord 
} from '../services/api/coach.api'
import { showError, showSuccess, showLoading, hideLoading, showModal } from '../utils/wx-util'
import { FinanceStatsParams } from '../services/types/coach.types'
import { HealthRecord } from '../services/types/common.types'

// 需要创建coach.store.ts，这里先定义接口
interface CoachStore {
  mySchedule: any[]
  currentCourseStudents: any[]
  currentMemberDetail: any
  financeStats: any
  loading: boolean
  loadMySchedule: (date?: string) => Promise<any>
  loadCourseStudents: (courseId: number) => Promise<any>
  loadMemberDetail: (memberId: number) => Promise<any>
  loadFinanceStats: (params: FinanceStatsParams) => Promise<any>
}

export const useCoach = () => {
  /**
   * 加载我的课表
   */
  const loadMySchedule = async (date?: string) => {
    try {
      return await getMySchedule(date)
    } catch (error: any) {
      showError(error.message || '加载课表失败')
      throw error
    }
  }

  /**
   * 加载课程学员
   */
  const loadCourseStudents = async (courseId: number) => {
    try {
      showLoading()
      const students = await getCourseStudents(courseId)
      hideLoading()
      return students
    } catch (error: any) {
      hideLoading()
      showError(error.message || '加载学员失败')
      throw error
    }
  }

  /**
   * 加载会员详情（教练视角）
   */
  const loadMemberDetail = async (memberId: number) => {
    try {
      showLoading()
      const detail = await getCoachMemberDetail(memberId)
      hideLoading()
      return detail
    } catch (error: any) {
      hideLoading()
      showError(error.message || '加载会员信息失败')
      throw error
    }
  }

  /**
   * 加载财务统计
   */
  const loadFinanceStats = async (params: FinanceStatsParams) => {
    try {
      showLoading()
      const stats = await getFinanceStats(params)
      hideLoading()
      return stats
    } catch (error: any) {
      hideLoading()
      showError(error.message || '加载财务数据失败')
      throw error
    }
  }

  /**
   * 教练添加健康档案
   */
  const addHealthRecord = async (memberId: number, data: HealthRecord) => {
    try {
      // 验证数据
      if (!data.recordDate) {
        showError('请选择记录日期')
        return false
      }
      
      if (!data.weight || data.weight <= 0) {
        showError('请输入有效的体重')
        return false
      }
      
      showLoading('保存中...')
      
      await coachAddHealthRecord(memberId, data)
      
      hideLoading()
      showSuccess('添加成功')
      
      return true
    } catch (error: any) {
      hideLoading()
      showError(error.message || '添加失败')
      throw error
    }
  }

  /**
   * 扫码核销
   */
  const scanCheckin = async () => {
    try {
      const { scanCode } = await import('../utils/wx-util')
      const result = await scanCode()
      
      // 解析二维码内容
      // 假设格式为：gymflow://checkin?bookingId=123&code=123456
      const url = new URL(result)
      const params = new URLSearchParams(url.search)
      
      const bookingId = params.get('bookingId')
      const code = params.get('code')
      
      if (!bookingId || !code) {
        showError('无效的二维码')
        return
      }
      
      showLoading('核销中...')
      
      const { scanCheckin } = await import('../services/api/checkin.api')
      await scanCheckin({ code: result })
      
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
   * 获取今日上课提醒
   */
  const getTodayReminders = async () => {
    try {
      const { getCurrentReminder } = await import('../services/api/checkin.api')
      return await getCurrentReminder()
    } catch (error: any) {
      console.error('获取提醒失败:', error)
      return null
    }
  }

  return {
    loadMySchedule,
    loadCourseStudents,
    loadMemberDetail,
    loadFinanceStats,
    addHealthRecord,
    scanCheckin,
    getTodayReminders
  }
}