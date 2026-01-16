import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { DashboardStats } from '@/types'
import { dashboardApi } from '@/api/dashboard'

export const useDashboardStore = defineStore('dashboard', () => {
  // 状态
  const stats = ref<DashboardStats | null>(null)
  const loading = ref(false)

  // Actions
  const fetchStats = async () => {
    try {
      loading.value = true
      const response = await dashboardApi.getStats()
      stats.value = response
      return response
    } catch (error) {
      console.error('Fetch dashboard stats failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchChartData = async () => {
    try {
      loading.value = true
      const response = await dashboardApi.getChartData()
      return response
    } catch (error) {
      console.error('Fetch chart data failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const refreshData = async () => {
    try {
      loading.value = true
      const [statsData, chartData] = await Promise.all([
        dashboardApi.getStats(),
        dashboardApi.getChartData()
      ])
      stats.value = statsData
      return { stats: statsData, charts: chartData }
    } catch (error) {
      console.error('Refresh data failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const resetDashboardState = () => {
    stats.value = null
  }

  return {
    // 状态
    stats,
    loading,
    
    // Actions
    fetchStats,
    fetchChartData,
    refreshData,
    resetDashboardState
  }
})