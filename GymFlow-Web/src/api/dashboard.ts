import request from '@/utils/request'
import type { DashboardStats, ApiResponse } from '@/types'
import { ApiPaths } from '@/utils/constants'

export const dashboardApi = {
  /**
   * 获取仪表盘统计
   */
  getStats(): Promise<DashboardStats> {
    return http.get(ApiPaths.DASHBOARD_STATS)
  },

  /**
   * 获取图表数据
   */
  getChartData(): Promise<any> {
    return http.get(ApiPaths.DASHBOARD_CHARTS)
  },

  /**
   * 获取实时数据
   */
  getRealtimeData(): Promise<any> {
    return http.get('/dashboard/realtime')
  },

  /**
   * 获取公告通知
   */
  getNotifications(): Promise<any[]> {
    return http.get('/dashboard/notifications')
  },

  /**
   * 获取待办事项
   */
  getTodos(): Promise<any[]> {
    return http.get('/dashboard/todos')
  },

  /**
   * 标记待办为完成
   */
  completeTodo(id: number): Promise<void> {
    return http.post(`/dashboard/todos/${id}/complete`)
  }
}