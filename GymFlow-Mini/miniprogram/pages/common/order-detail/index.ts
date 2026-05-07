// 订单详情页面
import { getOrderDetail } from '../../../services/api/order.api'
import { orderStore } from '../../../stores/order.store'
import { userStore } from '../../../stores/user.store'
import { showToast, showModal, showLoading, hideLoading } from '../../../utils/wx-util'

Page({
  data: {
    orderId: 0,
    order: {
      id: 0,
      orderNo: '',
      orderType: 0,
      orderTypeDesc: '',
      totalAmount: 0,
      actualAmount: 0,
      paymentMethod: '',
      paymentTime: '',
      status: '',
      statusDesc: '',
      remark: '',
      createTime: '',
      items: []
    },
    loading: true,
    loadError: false,
    errorMessage: '',
    needLogin: false,
    isEmpty: false
  },

  onLoad: function (options) {
    const id = options.id
    if (!id) {
      this.setData({ loadError: true, errorMessage: '参数错误', loading: false, isEmpty: true })
      showToast('参数错误', 'none')
      return
    }
    this.setData({ orderId: parseInt(id) })
    const memberId = userStore.memberId
    if (!memberId) {
      this.setData({ loading: false, needLogin: true, loadError: true, errorMessage: '请先登录' })
      return
    }
    this.loadOrderDetail()
  },

  loadOrderDetail: function () {
    this.setData({ loading: true, loadError: false, needLogin: false })
    const orderId = this.data.orderId
    getOrderDetail(orderId).then(detail => {
      console.log(detail)
      if (!detail) {
        this.setData({ loading: false, isEmpty: true, loadError: true, errorMessage: '订单不存在' })
        return
      }
      this.setData({ order: detail, loading: false, isEmpty: false })
      console.log(this.data.order)
    }).catch(error => {
      console.error('加载订单详情失败:', error)
      if (error.message && error.message.indexOf('401') !== -1) {
        this.setData({ loading: false, needLogin: true, loadError: true, errorMessage: '登录已过期，请重新登录' })
      } else {
        this.setData({ loading: false, loadError: true, isEmpty: true, errorMessage: error.message || '加载失败，请重试' })
      }
      showToast(error.message || '加载失败', 'none')
    })
  },

  onRetry: function () {
    const memberId = userStore.memberId
    if (!memberId) {
      this.setData({ needLogin: true, errorMessage: '请先登录' })
      return
    }
    this.loadOrderDetail()
  },

  onGoToLogin: function () {
    wx.navigateTo({ url: '/pages/common/login/index' })
  },

  onPayOrder: function () {
    const order = this.data.order
    if (!orderStore.canPay(order)) {
      showToast('当前订单状态无法支付', 'none')
      return
    }
    showModal({
      title: '提示',
      content: `确认支付 ¥${order.actualAmount} 吗？`,
      confirmText: '确认支付',
      cancelText: '取消'
    }).then(confirm => {
      if (!confirm) return
      showLoading('支付中...')
      orderStore.payOrder(order.id)
        .then(() => {
          hideLoading()
          showToast('支付成功', 'success')
          setTimeout(() => this.loadOrderDetail(), 1500)
        })
        .catch(error => {
          hideLoading()
          showToast(error.message || '支付失败', 'none')
        })
    })
  },

  onCancelOrder: function () {
    const order = this.data.order
    if (!orderStore.canCancel(order)) {
      showToast('当前订单状态无法取消', 'none')
      return
    }
    showModal({
      title: '提示',
      content: '确定要取消该订单吗？取消后订单将无法恢复。',
      confirmText: '确认取消',
      cancelText: '再想想',
      confirmColor: '#f56c6c'
    }).then(confirm => {
      if (!confirm) return
      showLoading('取消中...')
      orderStore.cancelOrder(order.id, '用户主动取消')
        .then(() => {
          hideLoading()
          showToast('取消成功', 'success')
          setTimeout(() => this.loadOrderDetail(), 1500)
        })
        .catch(error => {
          hideLoading()
          showToast(error.message || '取消失败', 'none')
        })
    })
  },

  // 重试激活（仅当状态为 PAID 时显示此按钮）
  onRetryActivate: function () {
    const order = this.data.order
    if (!orderStore.canRetryActivate(order)) {
      showToast('当前订单状态无法重试激活', 'none')
      return
    }
    showLoading('激活中...')
    orderStore.retryActivateOrder(order.id)
      .then(() => {
        hideLoading()
        showToast('激活成功', 'success')
        this.loadOrderDetail()
      })
      .catch(error => {
        hideLoading()
        showToast(error.message || '激活失败', 'none')
      })
  },

  onBack: function () {
    wx.navigateBack()
  },

  getProductTypeText: function (type) {
    const map = { 0: '会籍卡', 1: '私教课', 2: '团课', 3: '相关产品' }
    return map[type] || '未知'
  }
})