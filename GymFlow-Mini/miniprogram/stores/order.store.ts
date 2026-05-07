// 订单状态管理

import {
  getOrderList,
  cancelOrder as cancelOrderApi,
  updateOrderStatus as updateOrderStatusApi,
  payOrder as payOrderApi,
  retryActivateOrder as retryActivateOrderApi
} from '../services/api/order.api'
import { Order, OrderQueryParams, OrderStatus } from '../services/types/order.types'

class OrderStore {
  private _orders: Order[] = []
  private _currentOrder: Order | null = null
  private _loading: boolean = false
  private _hasMore: boolean = true
  private _currentPage: number = 1
  private _pageSize: number = 10

  // ========== 状态文本映射 ==========
  private readonly _statusTextMap: Record<OrderStatus, string> = {
    'WAIT_PAY': '待支付',
    'PAID': '已支付',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'REFUNDED': '已退款'
  }

  // ========== 公共方法 ==========

  /**
   * 加载订单列表
   */
  async loadOrders(params?: OrderQueryParams, refresh: boolean = false) {
    if (this._loading) return
    
    if (refresh) {
      this._currentPage = 1
      this._hasMore = true
      this._orders = []
    }
    
    if (!this._hasMore) return
    
    this._loading = true
    
    try {
      const result = await getOrderList({
        pageNum: this._currentPage,
        pageSize: this._pageSize,
        ...params
      })
      
      if (refresh) {
        this._orders = result.list
      } else {
        this._orders = [...this._orders, ...result.list]
      }
      
      this._hasMore = this._currentPage < result.pages
      this._currentPage++
      
      return result
    } catch (error) {
      console.error('加载订单列表失败:', error)
      throw error
    } finally {
      this._loading = false
    }
  }

  /**
   * 获取订单详情（从本地缓存获取，如果没有则返回null）
   */
  getOrderById(orderId: number): Order | null {
    return this._orders.find(o => o.id === orderId) || null
  }

  /**
   * 设置当前订单
   */
  setCurrentOrder(order: Order) {
    this._currentOrder = order
  }

  /**
   * 获取当前订单
   */
  getCurrentOrder(): Order | null {
    return this._currentOrder
  }

  // ========== 订单操作（通用） ==========

  /**
   * 更新订单状态（通用方法）
   */
  async updateOrderStatus(orderId: number, status: OrderStatus, remark?: string): Promise<boolean> {
    try {
      await updateOrderStatusApi(orderId, status, remark)
      this._updateLocalOrder(orderId, status)
      return true
    } catch (error) {
      console.error('更新订单状态失败:', error)
      throw error
    }
  }

  /**
   * 取消订单（仅限 WAIT_PAY）
   */
  async cancelOrder(orderId: number, reason?: string): Promise<boolean> {
    try {
      await cancelOrderApi({ orderId, reason })
      this._updateLocalOrder(orderId, 'CANCELLED')
      return true
    } catch (error) {
      console.error('取消订单失败:', error)
      throw error
    }
  }

  /**
   * 支付订单（调用后端支付接口，同步完成权益激活）
   * @returns 支付后订单状态（通常为 COMPLETED）
   */
  async payOrder(orderId: number, paymentMethod?: string): Promise<boolean> {
    try {
      const activated = await payOrderApi({ orderId, paymentMethod })
      if (activated) {
        // 支付并激活成功，订单状态变为 COMPLETED
        this._updateLocalOrder(orderId, 'COMPLETED')
        return true
      } else {
        // 支付成功但激活失败，状态变为 PAID
        this._updateLocalOrder(orderId, 'PAID')
        throw new Error('支付成功，但权益激活失败，请稍后重试激活')
      }
    } catch (error) {
      console.error('支付失败:', error)
      throw error
    }
  }

  /**
   * 重试激活订单权益（仅限 PAID 状态）
   */
  async retryActivateOrder(orderId: number): Promise<boolean> {
    try {
      const activated = await retryActivateOrderApi(orderId)
      if (activated) {
        this._updateLocalOrder(orderId, 'COMPLETED')
        return true
      } else {
        throw new Error('激活失败，请稍后重试')
      }
    } catch (error) {
      console.error('重试激活失败:', error)
      throw error
    }
  }

  /**
   * 确认收货/完成订单（一般由管理端调用，会员端可保留）
   */
  async completeOrder(orderId: number): Promise<boolean> {
    return this.updateOrderStatus(orderId, 'COMPLETED', '用户确认收货')
  }

  /**
   * 申请退款（将订单状态改为已退款）
   */
  async refundOrder(orderId: number, reason?: string): Promise<boolean> {
    return this.updateOrderStatus(orderId, 'REFUNDED', reason || '用户申请退款')
  }

  // ========== 本地数据更新 ==========

  private _updateLocalOrder(orderId: number, status: OrderStatus) {
    // 更新列表中的订单
    const index = this._orders.findIndex(o => o.id === orderId)
    if (index !== -1) {
      this._orders[index].status = status
      this._orders[index].statusDesc = this.getStatusText(status)
      // 如果订单变为已完成，自动设置支付时间和支付方式（如果尚未设置）
      if (status === 'COMPLETED') {
        if (!this._orders[index].paymentTime) {
          this._orders[index].paymentTime = new Date().toISOString()
        }
        if (!this._orders[index].paymentMethod) {
          this._orders[index].paymentMethod = '微信支付'
        }
      }
    }
    
    // 更新当前订单
    if (this._currentOrder?.id === orderId) {
      this._currentOrder.status = status
      this._currentOrder.statusDesc = this.getStatusText(status)
      if (status === 'COMPLETED') {
        if (!this._currentOrder.paymentTime) {
          this._currentOrder.paymentTime = new Date().toISOString()
        }
        if (!this._currentOrder.paymentMethod) {
          this._currentOrder.paymentMethod = '微信支付'
        }
      }
    }
  }

  // ========== 筛选方法 ==========

  /**
   * 按状态筛选订单
   */
  getOrdersByStatus(status?: OrderStatus | OrderStatus[]): Order[] {
    if (!status) return this._orders
    if (Array.isArray(status)) {
      return this._orders.filter(o => status.includes(o.status))
    }
    return this._orders.filter(o => o.status === status)
  }

  /**
   * 获取待支付订单（status === 'WAIT_PAY'）
   */
  getPendingOrders(): Order[] {
    return this._orders.filter(o => o.status === 'WAIT_PAY')
  }

  /**
   * 获取已完成订单
   */
  getCompletedOrders(): Order[] {
    return this._orders.filter(o => o.status === 'COMPLETED')
  }

  /**
   * 获取已取消订单
   */
  getCancelledOrders(): Order[] {
    return this._orders.filter(o => o.status === 'CANCELLED')
  }

  // ========== 工具方法 ==========

  /**
   * 获取订单状态文本
   */
  getStatusText(status: OrderStatus): string {
    return this._statusTextMap[status] || status
  }

  /**
   * 判断订单是否可以支付
   */
  canPay(order: Order): boolean {
    return order.status === 'WAIT_PAY'
  }

  /**
   * 判断订单是否可以取消
   */
  canCancel(order: Order): boolean {
    return order.status === 'WAIT_PAY'
  }

  /**
   * 判断订单是否可以确认收货/完成
   */
  canComplete(order: Order): boolean {
    return order.status === 'PAID'
  }

  /**
   * 判断订单是否可以重试激活
   */
  canRetryActivate(order: Order): boolean {
    return order.status === 'PAID'
  }

  /**
   * 重置状态
   */
  reset() {
    this._orders = []
    this._currentOrder = null
    this._hasMore = true
    this._currentPage = 1
    this._loading = false
  }

  // ========== Getters ==========
  get orders() {
    return this._orders
  }

  get currentOrder() {
    return this._currentOrder
  }

  get loading() {
    return this._loading
  }

  get hasMore() {
    return this._hasMore
  }
}

// 导出单例
export const orderStore = new OrderStore()