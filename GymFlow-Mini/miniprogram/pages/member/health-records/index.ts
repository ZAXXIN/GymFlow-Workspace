// 会员端健康档案列表页面
import { userStore } from '../../../stores/user.store'
import { formatDate } from '../../../utils/date'
import { showToast } from '../../../utils/wx-util'

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
   * 加载健康记录（从 userStore 获取）
   */
  loadRecords: function() {
    var that = this
    this.setData({ loading: true, loadError: false })
    
    var memberId = userStore.memberId
    if (!memberId) {
      wx.navigateTo({ url: '/pages/common/login/index' })
      return
    }
    
    // 从 userStore 获取健康记录
    var userInfo = userStore.userInfo
    var records = userInfo?.healthRecords || []
    
    console.log('健康记录从userInfo获取:', records)
    
    this.setData({ 
      records: records,
      loading: false
    })
    
    this.calculateStats(records)
  },

  /**
   * 计算统计信息
   */
  calculateStats: function(records) {
    if (!records || records.length === 0) {
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
  },

  /**
   * 重试加载
   */
  onRetry: function() {
    this.loadRecords()
  },

  /**
   * 添加记录
   */
  onAddRecord: function() {
    wx.navigateTo({
      url: '/pages/member/add-health-record/index'
    })
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
   * 格式化数值
   */
  formatNumber: function(num, decimals) {
    if (decimals === undefined) decimals = 1
    if (num === undefined || num === null) return '--'
    return num.toFixed(decimals)
  },

  /**
   * 下拉刷新
   */
  onPullDownRefresh: function() {
    this.loadRecords()
    wx.stopPullDownRefresh()
  }
})