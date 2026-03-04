import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { dashboardApi } from '@/api/dashboard'
import type { 
  DashboardStatsDTO, 
  RevenueTrendDTO, 
  CourseCategoryStatsDTO, 
  TodayCourseDTO,
  QuickStatsDTO 
} from '@/api/dashboard'
import { ElMessage } from 'element-plus'

export const useDashboardStore = defineStore('dashboard', () => {
  // 状态
  const stats = ref<DashboardStatsDTO | null>(null)
  const revenueTrend = ref<RevenueTrendDTO | null>(null)
  const courseCategories = ref<CourseCategoryStatsDTO[]>([])
  const todayCourses = ref<TodayCourseDTO[]>([])
  const quickStats = ref<QuickStatsDTO | null>(null)
  const loading = ref(false)
  const coursesLoading = ref(false)
  const currentPeriod = ref('week')

  // Getter
  const totalMembers = computed(() => stats.value?.totalMembers || 0)
  const totalCoaches = computed(() => stats.value?.totalCoaches || 0)
  const todayRevenue = computed(() => stats.value?.todayRevenue || 0)
  const todayCheckIns = computed(() => stats.value?.todayCheckIns || 0)

  // 会员增长趋势
  const memberTrend = computed(() => {
    if (!stats.value) return 0
    const current = stats.value.monthNewMembers || 0
    const last = stats.value.lastMonthNewMembers || 0
    if (last === 0) return current > 0 ? 100 : 0
    return Number(((current - last) / last * 100).toFixed(1))
  })

  // 教练增长趋势
  const coachTrend = computed(() => {
    // 这里可以根据实际数据计算，暂时返回随机值
    return Math.floor(Math.random() * 20) - 5
  })

  // 营收增长趋势
  const revenueTrendValue = computed(() => {
    if (!stats.value) return 0
    const current = stats.value.todayRevenue || 0
    const last = stats.value.yesterdayRevenue || 0
    if (last === 0) return current > 0 ? 100 : 0
    return Number(((current - last) / last * 100).toFixed(1))
  })

  // 签到增长趋势
  const attendanceTrend = computed(() => {
    if (!stats.value) return 0
    const current = stats.value.todayCheckIns || 0
    const last = stats.value.yesterdayCheckIns || 0
    if (last === 0) return current > 0 ? 100 : 0
    return Number(((current - last) / last * 100).toFixed(1))
  })

  // Actions
  /**
   * 获取仪表盘统计数据
   */
  const fetchDashboardStats = async () => {
    try {
      loading.value = true
      const response = await dashboardApi.getDashboardStats()
      if (response.code === 200) {
        stats.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取仪表盘统计数据失败:', error)
      ElMessage.error('获取统计数据失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取营收趋势数据
   */
  const fetchRevenueTrend = async (period: string = 'week', startDate?: string, endDate?: string) => {
    try {
      const response = await dashboardApi.getRevenueTrend(period, startDate, endDate)
      if (response.code === 200) {
        revenueTrend.value = response.data
        currentPeriod.value = period
      }
      return response.data
    } catch (error) {
      console.error('获取营收趋势数据失败:', error)
      ElMessage.error('获取营收趋势失败')
      throw error
    }
  }

  /**
   * 获取课程分类分布
   */
  const fetchCourseCategoryStats = async () => {
    try {
      const response = await dashboardApi.getCourseCategoryStats()
      if (response.code === 200) {
        courseCategories.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取课程分类分布失败:', error)
      ElMessage.error('获取课程分类失败')
      throw error
    }
  }

  /**
   * 获取今日课程列表
   */
  const fetchTodayCourses = async () => {
    try {
      coursesLoading.value = true
      const response = await dashboardApi.getTodayCourses()
      if (response.code === 200) {
        todayCourses.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取今日课程列表失败:', error)
      ElMessage.error('获取今日课程失败')
      throw error
    } finally {
      coursesLoading.value = false
    }
  }

  /**
   * 获取快速统计数据
   */
  const fetchQuickStats = async () => {
    try {
      const response = await dashboardApi.getQuickStats()
      if (response.code === 200) {
        quickStats.value = response.data
      }
      return response.data
    } catch (error) {
      console.error('获取快速统计数据失败:', error)
      ElMessage.error('获取快速统计失败')
      throw error
    }
  }

  /**
   * 刷新所有数据
   */
  const refreshAllData = async () => {
    try {
      loading.value = true
      await Promise.all([
        fetchDashboardStats(),
        fetchRevenueTrend(currentPeriod.value),
        fetchCourseCategoryStats(),
        fetchTodayCourses(),
        fetchQuickStats()
      ])
      ElMessage.success('数据刷新成功')
    } catch (error) {
      console.error('刷新数据失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 格式化函数
  const formatRevenue = (value: number) => {
    if (value >= 10000) {
      return `¥${(value / 10000).toFixed(1)}万`
    }
    return `¥${value.toFixed(2)}`
  }

  const formatDate = (dateString: string) => {
    return dateString.replace(/-/g, '/')
  }

  const formatTime = (timeString: string) => {
    return timeString.substring(0, 5)
  }

  return {
    // 状态
    stats,
    revenueTrend,
    courseCategories,
    todayCourses,
    quickStats,
    loading,
    coursesLoading,
    currentPeriod,

    // Getter
    totalMembers,
    totalCoaches,
    todayRevenue,
    todayCheckIns,
    memberTrend,
    coachTrend,
    revenueTrend: revenueTrendValue,
    attendanceTrend,

    // Actions
    fetchDashboardStats,
    fetchRevenueTrend,
    fetchCourseCategoryStats,
    fetchTodayCourses,
    fetchQuickStats,
    refreshAllData,

    // 工具函数
    formatRevenue,
    formatDate,
    formatTime
  }
})