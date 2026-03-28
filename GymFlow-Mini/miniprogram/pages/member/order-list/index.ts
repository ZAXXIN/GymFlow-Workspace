// 会员端订单列表页面
import { userStore } from '../../../stores/user.store'
import { getMemberOrders } from '../../../services/api/order.api'
import { showModal, showToast } from '../../../utils/wx-util'

Page({
  data: {
    // 订单列表
    orders: [],
    loading: false,
    hasMore: true,
    pageNum: 1,
    pageSize: 10,
    
    // 筛选状态（空字符串表示全部）
    statusFilter: '',
    
    // 统计信息（增加 total 字段）
    stats: {
      pending: 0,
      processing: 0,
      completed: 0,
      cancelled: 0,
      total: 0
    },
    
    // 错误状态
    error: null,
    
    // 是否已检查登录
    checkedLogin: false
  },

  onLoad: function() {
    this.checkLoginAndLoad()
  },

  onShow: function() {
    if (this.data.orders.length > 0) {
      this.loadOrders(true)
      this.loadStats()
    } else {
      this.checkLoginAndLoad()
    }
  },

  /**
   * 检查登录并加载数据
   */
  checkLoginAndLoad: function() {
    var memberId = userStore.memberId
    console.log('订单页 memberId:', memberId)
    
    if (!memberId) {
      this.setData({ 
        error: '请先登录',
        checkedLogin: true,
        loading: false
      })
      return
    }
    
    this.setData({ checkedLogin: true, error: null })
    this.loadOrders(true)
    this.loadStats()
  },

  /**
   * 加载订单列表
   */
  loadOrders: function(refresh) {
    var that = this
    var data = this.data
    var loading = data.loading
    var hasMore = data.hasMore
    var pageNum = data.pageNum
    var pageSize = data.pageSize
    var statusFilter = data.statusFilter
    
    if (loading) return
    if (!refresh && !hasMore) return
    
    var memberId = userStore.memberId
    if (!memberId) {
      this.setData({ 
        error: '用户未登录',
        loading: false 
      })
      return
    }
    
    this.setData({ loading: true, error: null })
    
    var params = {
      pageNum: refresh ? 1 : pageNum,
      pageSize: pageSize
    }
    
    // 如果 statusFilter 不为空，则添加订单状态筛选
    if (statusFilter) {
      params.orderStatus = statusFilter
    }
    
    console.log('请求订单列表:', memberId, params)
    
    getMemberOrders(memberId, params).then(function(result) {
      console.log('订单列表返回:', result)
      
      var orders = result.list || []
      
      that.setData({
        orders: refresh ? orders : [...that.data.orders, ...orders],
        hasMore: result.pageNum < result.pages,
        pageNum: (refresh ? 1 : pageNum) + 1,
        loading: false,
        error: null
      })
    }).catch(function(error) {
      console.error('加载订单失败:', error)
      that.setData({ 
        loading: false,
        error: error.message || '加载失败'
      })
    })
  },

  /**
   * 加载统计信息
   */
  loadStats: function() {
    var that = this
    var memberId = userStore.memberId
    if (!memberId) return
    
    this.setData({ error: null })
    
    var getPending = getMemberOrders(memberId, { orderStatus: 'PENDING', pageSize: 1 })
    var getProcessing = getMemberOrders(memberId, { orderStatus: 'PROCESSING', pageSize: 1 })
    var getCompleted = getMemberOrders(memberId, { orderStatus: 'COMPLETED', pageSize: 1 })
    var getCancelled = getMemberOrders(memberId, { orderStatus: 'CANCELLED', pageSize: 1 })
    
    Promise.all([getPending, getProcessing, getCompleted, getCancelled]).then(function(results) {
      var pending = results[0].total || 0
      var processing = results[1].total || 0
      var completed = results[2].total || 0
      var cancelled = results[3].total || 0
      var total = pending + processing + completed + cancelled
      
      that.setData({
        stats: {
          pending: pending,
          processing: processing,
          completed: completed,
          cancelled: cancelled,
          total: total
        }
      })
    }).catch(function(error) {
      console.error('加载统计失败:', error)
    })
  },

  /**
   * 点击统计卡片进行筛选
   */
  onStatusFilter: function(e) {
    var status = e.currentTarget.dataset.status  // 可能为空字符串（全部）
    
    this.setData({ 
      statusFilter: status,
      pageNum: 1,
      orders: []
    }, function() {
      this.loadOrders(true)
    })
  },

  /**
   * 点击订单
   */
  onOrderTap: function(e) {
    var order = e.currentTarget.dataset.order
    wx.navigateTo({
      url: '/pages/common/order-detail/index?id=' + order.id
    })
  },

  /**
   * 支付订单
   */
  onPayOrder: function(e) {
    var that = this
    var order = e.currentTarget.dataset.order
    e.stopPropagation()
    
    showModal({
      title: '提示',
      content: '确认支付该订单吗？'
    }).then(function(confirm) {
      if (!confirm) return
      
      var payOrder = require('../../../services/api/order.api').payOrder
      
      payOrder({ orderId: order.id, paymentMethod: '微信支付' }).then(function() {
        showToast('支付成功', 'success')
        that.loadOrders(true)
        that.loadStats()
      }).catch(function(error) {
        showToast(error.message || '支付失败', 'none')
      })
    })
  },

  /**
   * 取消订单
   */
  onCancelOrder: function(e) {
    var that = this
    var order = e.currentTarget.dataset.order
    e.stopPropagation()
    
    showModal({
      title: '提示',
      content: '确定要取消该订单吗？'
    }).then(function(confirm) {
      if (!confirm) return
      
      var cancelOrder = require('../../../services/api/order.api').cancelOrder
      
      cancelOrder({ orderId: order.id, reason: '用户取消' }).then(function() {
        showToast('取消成功', 'success')
        that.loadOrders(true)
        that.loadStats()
      }).catch(function(error) {
        showToast(error.message || '取消失败', 'none')
      })
    })
  },

  /**
   * 确认收货
   */
  onConfirmReceive: function(e) {
    var that = this
    var order = e.currentTarget.dataset.order
    e.stopPropagation()
    
    showModal({
      title: '提示',
      content: '确认已收到商品/完成服务？'
    }).then(function(confirm) {
      if (!confirm) return
      
      var completeOrder = require('../../../services/api/order.api').completeOrder
      
      completeOrder(order.id).then(function() {
        showToast('操作成功', 'success')
        that.loadOrders(true)
        that.loadStats()
      }).catch(function(error) {
        showToast(error.message || '操作失败', 'none')
      })
    })
  },

  /**
   * 获取状态类名
   */
  getStatusClass: function(status) {
    var map = {
      'PENDING': 'status-pending',
      'PROCESSING': 'status-info',
      'COMPLETED': 'status-success',
      'CANCELLED': 'status-warning',
      'REFUNDED': 'status-warning',
      'DELETED': 'status-completed'
    }
    return map[status] || ''
  },

  /**
   * 重试加载
   */
  onRetry: function() {
    this.checkLoginAndLoad()
  },

  /**
   * 去首页
   */
  // goToHome: function() {
  //   wx.switchTab({
  //     url: '/pages/member/home/index'
  //   })
  // },

  /**
   * 下拉刷新
   */
  onPullDownRefresh: function() {
    this.loadOrders(true)
    this.loadStats()
    wx.stopPullDownRefresh()
  },

  /**
   * 上拉加载更多
   */
  onReachBottom: function() {
    this.loadOrders(false)
  }
})