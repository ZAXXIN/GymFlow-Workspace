import request from '@/utils/request'
import type { ApiResponse } from '@/types/common'

// 仪表盘统计数据DTO
export interface DashboardStatsDTO {
  totalMembers: number
  totalCoaches: number
  totalCourses: number
  todayRevenue: number
  yesterdayRevenue: number
  todayCheckIns: number
  yesterdayCheckIns: number
  monthRevenue: number
  lastMonthRevenue: number
  monthNewMembers: number
  lastMonthNewMembers: number
  monthCheckIns: number
}

// 营收数据点DTO
export interface RevenueDataPointDTO {
  date: string
  revenue: number
  label: string
}

// 营收趋势DTO
export interface RevenueTrendDTO {
  period: string
  startDate: string
  endDate: string
  categories: string[]
  values: number[]
  dataPoints: RevenueDataPointDTO[]
}

// 课程分类统计DTO
export interface CourseCategoryStatsDTO {
  category: string
  value: number
  percentage: number
  color: string
}

// 今日课程DTO
export interface TodayCourseDTO {
  id: number
  courseNo: string
  name: string
  coachId: number
  coachName: string
  startTime: string
  endTime: string
  capacity: number
  currentBookings: number
  status: string
  statusText: string
}

// 快速统计数据DTO
export interface QuickStatsDTO {
  pendingOrders: number
  pendingCheckIns: number
  expiringMembers: number
  lowStockProducts: number
}

// 仪表盘API
export const dashboardApi = {
  /**
   * 获取仪表盘统计数据
   */
  getDashboardStats(): Promise<ApiResponse<DashboardStatsDTO>> {
    return request({
      url: '/dashboard/stats',
      method: 'GET'
    })
  },

  /**
   * 获取营收趋势数据
   */
  getRevenueTrend(period: string, startDate?: string, endDate?: string): Promise<ApiResponse<RevenueTrendDTO>> {
    return request({
      url: '/dashboard/revenue/trend',
      method: 'GET',
      params: { period, startDate, endDate }
    })
  },

  /**
   * 获取课程分类分布
   */
  getCourseCategoryStats(): Promise<ApiResponse<CourseCategoryStatsDTO[]>> {
    return request({
      url: '/dashboard/courses/category',
      method: 'GET'
    })
  },

  /**
   * 获取今日课程列表
   */
  getTodayCourses(): Promise<ApiResponse<TodayCourseDTO[]>> {
    return request({
      url: '/dashboard/courses/today',
      method: 'GET'
    })
  },

  /**
   * 获取快速统计数据
   */
  getQuickStats(): Promise<ApiResponse<QuickStatsDTO>> {
    return request({
      url: '/dashboard/quick-stats',
      method: 'GET'
    })
  }
}