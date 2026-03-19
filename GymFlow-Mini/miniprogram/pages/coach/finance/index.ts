// 教练端财务页面逻辑
import { userStore } from '../../../stores/user.store'
import { getFinanceStats } from '../../../services/api/coach.api'
import { showToast } from '../../../utils/wx-util'
import { formatDate } from '../../../utils/date'

Page({
  data: {
    // 当前选中周期 0-日 1-月 2-年
    activePeriod: 1,
    
    // 统计数据
    stats: {
      totalSessions: 0,
      totalRevenue: 0,
      totalMembers: 0,
      sessionsTrend: 0,
      revenueTrend: 0,
      membersTrend: 0
    },
    
    // 平均课时费
    avgHourlyRate: 0,
    
    // 图表数据点
    dataPoints: [] as any[],
    
    // 图表数据
    sessionsChartData: [] as any[],
    revenueChartData: [] as any[],
    membersChartData: [] as any[],
    
    // 日期范围
    startDate: '',
    endDate: '',
    
    // 加载状态
    loading: true
  },

  onLoad() {
    this.loadFinanceStats()
  },

  onPullDownRefresh() {
    this.loadFinanceStats(true)
  },

  /**
   * 加载财务统计
   */
  async loadFinanceStats(showPullRefresh: boolean = false) {
    if (!showPullRefresh) {
      this.setData({ loading: true })
    }
    
    try {
      const { activePeriod } = this.data
      
      // 根据周期获取对应日期
      const date = this.getDateByPeriod(activePeriod)
      
      const result = await getFinanceStats({
        period: this.getPeriodType(activePeriod),
        date
      })
      
      // 计算平均课时费
      const avgHourlyRate = result.totalSessions > 0 
        ? (result.totalRevenue / result.totalSessions).toFixed(2) 
        : 0
      
      // 准备图表数据
      const sessionsData: any[] = []
      const revenueData: any[] = []
      const membersData: any[] = []
      
      for (let i = 0; i < result.dataPoints.length; i++) {
        const point = result.dataPoints[i]
        sessionsData.push({
          name: point.label,
          value: point.sessions,
          color: '#07c160'
        })
        revenueData.push({
          name: point.label,
          value: point.revenue,
          color: '#f56c6c'
        })
        membersData.push({
          name: point.label,
          value: point.members,
          color: '#409EFF'
        })
      }
      
      this.setData({
        stats: {
          totalSessions: result.totalSessions,
          totalRevenue: result.totalRevenue,
          totalMembers: result.totalMembers,
          sessionsTrend: this.calculateTrend(result.dataPoints, 'sessions'),
          revenueTrend: this.calculateTrend(result.dataPoints, 'revenue'),
          membersTrend: this.calculateTrend(result.dataPoints, 'members')
        },
        avgHourlyRate,
        dataPoints: result.dataPoints,
        sessionsChartData: sessionsData,
        revenueChartData: revenueData,
        membersChartData: membersData,
        startDate: result.startDate,
        endDate: result.endDate,
        loading: false
      })
      
      if (showPullRefresh) {
        wx.stopPullDownRefresh()
      }
      
    } catch (error: any) {
      console.error('加载财务统计失败:', error)
      this.setData({ loading: false })
      if (showPullRefresh) {
        wx.stopPullDownRefresh()
      }
      showToast(error.message || '加载失败', 'none')
    }
  },

  /**
   * 周期切换
   */
  onPeriodChange(e: any) {
    const { index } = e.detail
    this.setData({ activePeriod: index }, () => {
      this.loadFinanceStats()
    })
  },

  /**
   * 获取周期类型
   */
  getPeriodType(period: number): 'day' | 'month' | 'year' {
    const map: Record<number, 'day' | 'month' | 'year'> = {
      0: 'day',
      1: 'month',
      2: 'year'
    }
    return map[period] || 'month'
  },

  /**
   * 根据周期获取日期
   */
  getDateByPeriod(period: number): string {
    const now = new Date()
    const year = now.getFullYear()
    const month = (now.getMonth() + 1).toString().padStart(2, '0')
    const day = now.getDate().toString().padStart(2, '0')
    
    if (period === 0) {
      // 日：返回今天日期
      return `${year}-${month}-${day}`
    } else if (period === 1) {
      // 月：返回年月
      return `${year}-${month}`
    } else {
      // 年：返回年份
      return `${year}`
    }
  },

  /**
   * 计算趋势（环比）
   */
  calculateTrend(dataPoints: any[], field: string): number {
    if (dataPoints.length < 2) {
      return 0
    }
    
    const current = dataPoints[dataPoints.length - 1][field]
    const previous = dataPoints[0][field]
    
    if (previous === 0) {
      return current > 0 ? 100 : 0
    }
    
    return Math.round(((current - previous) / previous) * 100)
  },

  /**
   * 获取趋势对比文本
   */
  getTrendText(): string {
    const { activePeriod } = this.data
    
    const map: Record<number, string> = {
      0: '昨日',
      1: '上月',
      2: '去年'
    }
    return map[activePeriod] || '上周期'
  }
})