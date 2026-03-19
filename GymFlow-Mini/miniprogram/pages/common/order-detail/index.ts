// 订单详情页面
import { getOrderDetail, payOrder, cancelOrder, completeOrder } from '../../../services/api/order.api'
import { orderStore } from '../../../stores/order.store'
import { userStore } from '../../../stores/user.store'
import { formatDateTime } from '../../../utils/date'
import { showToast, showModal, showLoading, hideLoading } from '../../../utils/wx-util'

Page({
  data: {
    // 订单ID
    orderId: 0,
    
    // 订单信息
    order: {
      id: 0,
      orderNo: '',
      orderType: 0,
      orderTypeDesc: '',
      totalAmount: 0,
      actualAmount: 0,
      paymentMethod: '',
      paymentStatus: 0,
      paymentStatusDesc: '',
      paymentTime: '',
      orderStatus: '',
      orderStatusDesc: '',
      remark: '',
      createTime: '',
      items: []
    },
    
    // 加载状态
    loading: true,
    loadError: false,
    errorMessage: '',
    needLogin: false,  // 新增：是否需要登录
    
    // 空状态
    isEmpty: false
  },

  onLoad: function(options) {
    var id = options.id
    
    if (!id) {
      this.setData({
        loadError: true,
        errorMessage: '参数错误',
        loading: false,
        isEmpty: true
      })
      showToast('参数错误', 'none')
      return
    }
    
    this.setData({ orderId: parseInt(id) })
    
    // 检查登录状态
    var memberId = userStore.memberId
    if (!memberId) {
      // 不自动跳转，显示需要登录的提示
      this.setData({
        loading: false,
        needLogin: true,
        loadError: true,
        errorMessage: '请先登录'
      })
      return
    }
    
    this.loadOrderDetail()
  },

  /**
   * 加载订单详情
   */
  loadOrderDetail: function() {
    var that = this
    this.setData({ loading: true, loadError: false, needLogin: false })
    
    var orderId = this.data.orderId
    
    getOrderDetail(orderId).then(function(detail) {
      if (!detail) {
        that.setData({
          loading: false,
          isEmpty: true,
          loadError: true,
          errorMessage: '订单不存在'
        })
        return
      }
      
      that.setData({
        order: detail,
        loading: false,
        isEmpty: false
      })
      
    }).catch(function(error) {
      console.error('加载订单详情失败:', error)
      
      // 如果是401未授权错误
      if (error.message && error.message.indexOf('401') !== -1) {
        that.setData({
          loading: false,
          needLogin: true,
          loadError: true,
          errorMessage: '登录已过期，请重新登录'
        })
      } else {
        that.setData({
          loading: false,
          loadError: true,
          isEmpty: true,
          errorMessage: error.message || '加载失败，请重试'
        })
      }
      showToast(error.message || '加载失败', 'none')
    })
  },

  /**
   * 重试加载
   */
  onRetry: function() {
    var memberId = userStore.memberId
    if (!memberId) {
      this.setData({
        needLogin: true,
        errorMessage: '请先登录'
      })
      return
    }
    this.loadOrderDetail()
  },

  /**
   * 去登录
   */
  onGoToLogin: function() {
    wx.navigateTo({
      url: '/pages/common/login/index'
    })
  },

  /**
   * 支付订单
   */
  onPayOrder: function() {
    var that = this
    var order = this.data.order
    
    showModal({
      title: '提示',
      content: '确认支付 ¥' + order.actualAmount + ' 吗？'
    }).then(function(confirm) {
      if (!confirm) return
      
      showLoading('支付中...')
      
      payOrder({
        orderId: order.id,
        paymentMethod: '微信支付'
      }).then(function() {
        hideLoading()
        showToast('支付成功', 'success')
        
        // 更新订单状态
        orderStore.updatePaymentStatus(order.id, 1)
        orderStore.updateOrderStatus(order.id, 'PROCESSING')
        
        // 刷新页面
        setTimeout(function() {
          that.loadOrderDetail()
        }, 1500)
        
      }).catch(function(error) {
        hideLoading()
        showToast(error.message || '支付失败', 'none')
      })
    })
  },

  /**
   * 取消订单
   */
  onCancelOrder: function() {
    var that = this
    var order = this.data.order
    
    showModal({
      title: '提示',
      content: '确定要取消该订单吗？'
    }).then(function(confirm) {
      if (!confirm) return
      
      showLoading('取消中...')
      
      cancelOrder({
        orderId: order.id,
        reason: '用户取消'
      }).then(function() {
        hideLoading()
        showToast('取消成功', 'success')
        
        // 更新订单状态
        orderStore.updateOrderStatus(order.id, 'CANCELLED')
        
        // 刷新页面
        setTimeout(function() {
          that.loadOrderDetail()
        }, 1500)
        
      }).catch(function(error) {
        hideLoading()
        showToast(error.message || '取消失败', 'none')
      })
    })
  },

  /**
   * 确认收货
   */
  onConfirmReceive: function() {
    var that = this
    var order = this.data.order
    
    showModal({
      title: '提示',
      content: '确认已收到商品/完成服务？'
    }).then(function(confirm) {
      if (!confirm) return
      
      showLoading('提交中...')
      
      completeOrder(order.id).then(function() {
        hideLoading()
        showToast('操作成功', 'success')
        
        // 更新订单状态
        orderStore.updateOrderStatus(order.id, 'COMPLETED')
        
        // 刷新页面
        setTimeout(function() {
          that.loadOrderDetail()
        }, 1500)
        
      }).catch(function(error) {
        hideLoading()
        showToast(error.message || '操作失败', 'none')
      })
    })
  },

  /**
   * 返回
   */
  onBack: function() {
    wx.navigateBack()
  },

  /**
   * 获取状态图标
   */
  getStatusIcon: function(orderStatus, paymentStatus) {
    if (paymentStatus === 0) {
      return 'pending'
    }
    
    var map = {
      'PROCESSING': 'processing',
      'COMPLETED': 'completed',
      'CANCELLED': 'cancelled',
      'REFUNDED': 'refunded'
    }
    return map[orderStatus] || 'pending'
  },

  /**
   * 获取商品类型文本
   */
  getProductTypeText: function(type) {
    var map = {
      0: '会籍卡',
      1: '私教课',
      2: '团课',
      3: '相关产品'
    }
    return map[type] || '未知'
  },

  /**
   * 格式化日期时间
   */
  formatDateTime: function(time) {
    return formatDateTime(time)
  }
})