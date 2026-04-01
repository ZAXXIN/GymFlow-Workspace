// stores/member-statistics.store.ts
import { getMemberStatistics } from '../services/api/member.api'
import type { MemberStatistics } from '../services/types/statistics.types'

class MemberStatisticsStore {
  private _statistics: MemberStatistics | null = null
  private _loading: boolean = false

  get statistics() {
    return this._statistics
  }

  get loading() {
    return this._loading
  }

  get totalCheckins() {
    return this._statistics?.checkinStats?.totalCheckins || 0
  }

  get recentCheckins() {
    return this._statistics?.checkinStats?.recentCheckins || []
  }

  get totalConsumed() {
    return this._statistics?.sessionStats?.totalConsumed || 0
  }

  get totalReturned() {
    return this._statistics?.sessionStats?.totalReturned || 0
  }

  get sessionDetails() {
    return this._statistics?.sessionStats?.details || []
  }

  /**
   * 加载会员统计数据
   * @param force 是否强制刷新
   * @param orderItemId 订单项ID
   */
  async loadStatistics(force: boolean = false, orderItemId?: number) {
    if (this._loading && !force) {
      console.log('正在加载中，跳过')
      return
    }

    if (!orderItemId) {
      console.warn('orderItemId 不能为空')
      return
    }

    this._loading = true
    console.log('开始加载会员统计数据，orderItemId:', orderItemId)

    try {
      const response = await getMemberStatistics(orderItemId)
      console.log('会员统计数据接口返回:', response)
      
      let data = null
      if (response && response.code === 200) {
        data = response.data
      } else if (response && response.checkinStats) {
        data = response
      } else {
        console.error('加载会员统计数据失败: 响应格式异常')
        return
      }
      
      if (data) {
        this._statistics = {
          checkinStats: {
            totalCheckins: data.checkinStats?.totalCheckins || 0,
            recentCheckins: data.checkinStats?.recentCheckins || []
          },
          sessionStats: {
            totalConsumed: data.sessionStats?.totalConsumed || 0,
            totalReturned: data.sessionStats?.totalReturned || 0,
            details: data.sessionStats?.details || []
          }
        }
        console.log('会员统计数据加载成功:', this._statistics)
      }
    } catch (error) {
      console.error('加载会员统计数据失败:', error)
    } finally {
      this._loading = false
    }
  }

  reset() {
    this._statistics = null
    this._loading = false
  }
}

export const memberStatisticsStore = new MemberStatisticsStore()