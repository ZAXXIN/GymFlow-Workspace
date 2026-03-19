// 订单相关自定义Hook

import { orderStore } from '../stores/order.store'
import { 
  createOrder, 
  payOrder as payOrderApi,
  cancelOrder as cancelOrderApi,
  getOrderDetail 
} from '../services/api/order.api'
import { showModal, showSuccess, showError, showLoading, hideLoading } from '../utils/wx-util'
import { CreateOrderParams, OrderStatus } from '../services/types/order.types'

export const useOrder = () => {
  /**
   * 加载订单列表
   */
  const loadOrders = async (params?: any, refresh: boolean = false) => {
    try {
      return await orderStore.loadOrders(params, refresh)
    } catch (error: any) {
      showError(error.message || '加载订单失败')
      throw error
    }
  }

  /**
   * 创建新订单
   */
  const createNewOrder = async (params: CreateOrderParams) => {
    try {
      showLoading('创建订单中...')
      
      const result = await createOrder(params)
      
      hideLoading()
      showSuccess('订单创建成功')
      
      return result.orderId
    } catch (error: any) {
      hideLoading()
      showError(error.message || '创建订单失败')
      throw error
    }
  }

  /**
   * 支付订单
   */
  const payOrder = async (orderId: number) => {
    try {
      const confirm = await showModal({
        title: '提示',
        content: '确认支付该订单吗？'
      })
      
      if (!confirm) return false
      
      showLoading('支付中...')
      
      await payOrderApi({ orderId, paymentMethod: '微信支付' })
      
      // 更新订单状态
      orderStore.updatePaymentStatus(orderId, 1)
      orderStore.updateOrderStatus(orderId, 'PROCESSING')
      
      hideLoading()
      showSuccess('支付成功')
      
      return true
    } catch (error: any) {
      hideLoading()
      showError(error.message || '支付失败')
      throw error
    }
  }

  /**
   * 取消订单
   */
  const cancelUserOrder = async (orderId: number, reason?: string) => {
    try {
      const confirm = await showModal({
        title: '提示',
        content: '确定要取消该订单吗？'
      })
      
      if (!confirm) return false
      
      showLoading('取消中...')
      
      await orderStore.cancelOrder(orderId, reason || '用户取消')
      
      hideLoading()
      showSuccess('取消成功')
      
      return true
    } catch (error: any) {
      hideLoading()
      showError(error.message || '取消失败')
      throw error
    }
  }

  /**
   * 获取订单详情
   */
  const fetchOrderDetail = async (orderId: number) => {
    try {
      showLoading()
      const detail = await getOrderDetail(orderId)
      orderStore.setCurrentOrder(detail)
      hideLoading()
      return detail
    } catch (error: any) {
      hideLoading()
      showError(error.message || '获取详情失败')
      throw error
    }
  }

  /**
   * 获取订单按状态分组
   */
  const getOrdersGroupedByStatus = () => {
    const all = orderStore.orders
    
    return {
      pending: all.filter(o => o.paymentStatus === 0),
      paid: all.filter(o => o.paymentStatus === 1 && o.orderStatus === 'PROCESSING'),
      completed: all.filter(o => o.orderStatus === 'COMPLETED'),
      cancelled: all.filter(o => o.orderStatus === 'CANCELLED'),
      refunded: all.filter(o => o.orderStatus === 'REFUNDED')
    }
  }

  /**
   * 获取待支付订单总数
   */
  const getPendingOrderCount = (): number => {
    return orderStore.orders.filter(o => o.paymentStatus === 0).length
  }

  return {
    orders: orderStore.orders,
    currentOrder: orderStore.currentOrder,
    loading: orderStore.loading,
    hasMore: orderStore.hasMore,
    loadOrders,
    createOrder: createNewOrder,
    payOrder,
    cancelOrder: cancelUserOrder,
    getOrderDetail: fetchOrderDetail,
    getOrdersGroupedByStatus,
    getPendingOrderCount
  }
}