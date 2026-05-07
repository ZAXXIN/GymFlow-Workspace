// 会员端订单列表页面
import { userStore } from '../../../stores/user.store'
import { getMemberOrders } from '../../../services/api/order.api'
import { showModal, showToast } from '../../../utils/wx-util'
import { orderStore } from '../../../stores/order.store'

Page({
  data: {
    orders: [],
    loading: false,
    hasMore: true,
    pageNum: 1,
    pageSize: 10,
    statusFilter: '',        // 空字符串表示全部，可选值：WAIT_PAY, COMPLETED, CANCELLED, REFUNDED
    stats: {
      pending: 0,            // WAIT_PAY 数量
      completed: 0,          // COMPLETED 数量
      cancelled: 0,          // CANCELLED 数量
      refunded: 0,           // REFUNDED 数量
      total: 0
    },
    error: null,
    checkedLogin: false
  },

  onLoad: function() {
    this.checkLoginAndLoad()
  },

  onShow: function() {
    if (wx.hideHomeButton) {
      wx.hideHomeButton({
        success: (res) => console.log('Home键隐藏成功', res),
        fail: (err) => console.error('Home键隐藏失败', err)
      })
    }
    if (this.data.orders.length > 0 && this.data.checkedLogin) {
      this.loadOrders(true)
    } else if (!this.data.checkedLogin) {
      this.checkLoginAndLoad()
    }
  },

  checkLoginAndLoad: function() {
    const memberId = userStore.memberId
    console.log('订单页 memberId:', memberId)
    
    if (!memberId) {
      this.setData({ error: '请先登录', checkedLogin: true, loading: false })
      return
    }
    
    this.setData({ checkedLogin: true, error: null })
    this.loadOrders(true)
  },

  calculateStats: function(orders) {
    const stats = { pending: 0, completed: 0, cancelled: 0, refunded: 0, total: orders.length }
    for (const order of orders) {
      switch (order.status) {
        case 'WAIT_PAY': stats.pending++; break
        case 'COMPLETED': stats.completed++; break
        case 'CANCELLED': stats.cancelled++; break
        case 'REFUNDED': stats.refunded++; break
      }
    }
    return stats
  },

  loadOrders: function(refresh) {
    if (this.data.loading) return
    if (!refresh && !this.data.hasMore) return
    
    const memberId = userStore.memberId
    if (!memberId) {
      this.setData({ error: '用户未登录', loading: false })
      return
    }
    
    this.setData({ loading: true, error: null })
    
    const params = {
      pageNum: refresh ? 1 : this.data.pageNum,
      pageSize: this.data.pageSize
    }
    if (this.data.statusFilter) {
      params.status = this.data.statusFilter
    }
    
    console.log('请求订单列表:', memberId, params)
    
    getMemberOrders(memberId, params).then(result => {
      console.log('订单列表返回:', result)
      const orders = result.list || []
      const newOrders = refresh ? orders : [...this.data.orders, ...orders]
      
      // 仅在刷新且筛选条件为全部时重新计算统计，否则保留原统计
      let newStats = this.data.stats
      if (refresh && !this.data.statusFilter) {
        newStats = this.calculateStats(newOrders)
      }
      
      this.setData({
        orders: newOrders,
        stats: newStats,
        hasMore: result.pageNum < result.pages,
        pageNum: (refresh ? 1 : this.data.pageNum) + 1,
        loading: false,
        error: null
      })
    }).catch(error => {
      console.error('加载订单失败:', error)
      this.setData({ loading: false, error: error.message || '加载失败' })
    })
  },

  onStatusFilter: function(e) {
    const status = e.currentTarget.dataset.status   // 可能为空字符串
    this.setData({ statusFilter: status, pageNum: 1, orders: [] }, () => {
      this.loadOrders(true)
    })
  },

  onOrderTap: function(e) {
    const order = e.currentTarget.dataset.order
    wx.navigateTo({ url: '/pages/common/order-detail/index?id=' + order.id })
  },

  onPayOrder: function(e) {
    const order = e.currentTarget.dataset.order
    e.stopPropagation()
    
    showModal({ title: '提示', content: '确认支付该订单吗？' }).then(confirm => {
      if (!confirm) return
      
      orderStore.payOrder(order.id, '微信支付')
        .then(() => {
          showToast('支付成功', 'success')
          this.loadOrders(true)
        })
        .catch(error => {
          showToast(error.message || '支付失败', 'none')
        })
    })
  },

  onCancelOrder: function(e) {
    const order = e.currentTarget.dataset.order
    showModal({ title: '提示', content: '确定要取消该订单吗？' }).then(confirm => {
      if (!confirm) return
      orderStore.cancelOrder(order.id, '用户取消')
        .then(() => {
          showToast('取消成功', 'success')
          this.loadOrders(true)
        })
        .catch(error => {
          showToast(error.message || '取消失败', 'none')
        })
    })
  },

  onConfirmReceive: function(e) {
    const order = e.currentTarget.dataset.order
    e.stopPropagation()
    showModal({ title: '提示', content: '确认已收到商品/完成服务？' }).then(confirm => {
      if (!confirm) return
      orderStore.completeOrder(order.id)
        .then(() => {
          showToast('操作成功', 'success')
          this.loadOrders(true)
        })
        .catch(error => {
          showToast(error.message || '操作失败', 'none')
        })
    })
  },

  getStatusClass: function(status) {
    const map = {
      'WAIT_PAY': 'status-pending',
      'PAID': 'status-info',
      'COMPLETED': 'status-success',
      'CANCELLED': 'status-warning',
      'REFUNDED': 'status-warning'
    }
    return map[status] || ''
  },

  onRetry: function() {
    this.checkLoginAndLoad()
  },

  onPullDownRefresh: function() {
    this.loadOrders(true)
    wx.stopPullDownRefresh()
  },

  onReachBottom: function() {
    this.loadOrders(false)
  }
})