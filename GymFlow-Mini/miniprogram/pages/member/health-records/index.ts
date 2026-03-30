// 会员端健康档案列表页面
import { userStore } from '../../../stores/user.store'
import { formatDate } from '../../../utils/date'
import { showToast } from '../../../utils/wx-util'
import { getHealthRecords } from '../../../services/api/member.api'

Page({
  data: {
    // 健康记录列表
    records: [],
    loading: false,
    loadError: false,
    errorMessage: '',
    
    // 统计信息
    stats: {
      latestWeight: 0,
      weightChange: 0,
      latestBodyFat: 0,
      bodyFatChange: 0,
      recordCount: 0
    }
  },

  onLoad: function() {
    this.loadRecords()
  },

  onShow: function() {
    this.loadRecords()
  },

  /**
   * 加载健康记录（从接口获取）
   */
  loadRecords: function() {
    var that = this
    this.setData({ loading: true, loadError: false })
    
    var memberId = userStore.memberId
    if (!memberId) {
      wx.navigateTo({ url: '/pages/common/login/index' })
      return
    }
    
    // 调用接口获取健康记录
    getHealthRecords(memberId)
      .then((response: any) => {
        var records = response || []
        console.log('健康记录从接口获取:', records)
        
        that.setData({ 
          records: records,
          loading: false
        })
        
        that.calculateStats(records)
      })
      .catch((error: any) => {
        console.error('获取健康记录失败:', error)
        that.setData({ 
          loading: false,
          loadError: true,
          errorMessage: error.message || '加载失败，请稍后重试'
        })
        showToast('加载失败', 'none')
      })
  },

  /**
   * 计算统计信息
   */
  calculateStats: function(records) {
    console.log(records)
    if (!records || records.length == 0) {
      this.setData({
        stats: {
          latestWeight: 0,
          weightChange: 0,
          latestBodyFat: 0,
          bodyFatChange: 0,
          recordCount: 0
        }
      })
      return
    }
    
    var latest = records[0]
    var previous = records.length > 1 ? records[1] : null
    
    var stats = {
      latestWeight: latest.weight || 0,
      weightChange: previous ? (latest.weight - previous.weight) : 0,
      latestBodyFat: latest.bodyFatPercentage || 0,
      bodyFatChange: previous ? (latest.bodyFatPercentage - previous.bodyFatPercentage) : 0,
      recordCount: records.length
    }
    
    this.setData({ stats: stats })

    console.log(stats)
  },

  /**
   * 重试加载
   */
  onRetry: function() {
    this.loadRecords()
  },

  /**
   * 查看记录详情
   */
  onRecordTap: function(e) {
    var record = e.currentTarget.dataset.record
    if (!record || !record.id) return
    
    wx.navigateTo({
      url: '/pages/member/health-record-detail/index?id=' + record.id
    })
  },

  /**
   * 格式化日期
   */
  formatDate: function(date) {
    return formatDate(date)
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh: function() {
    this.loadRecords()
    wx.stopPullDownRefresh()
  }
})