// 订单状态管理

import { getOrderList, cancelOrder as cancelOrderApi, updateOrderStatus as updateOrderStatusApi } from '../services/api/order.api'
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
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'REFUNDED': '已退款',
    'DELETED': '已删除'
  }

  private readonly _paymentStatusTextMap: Record<number, string> = {
    0: '待支付',
    1: '已支付'
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
   * @param orderId 订单ID
   * @param orderStatus 新状态
   * @param remark 备注（可选）
   * @returns Promise<boolean>
   */
  async updateOrderStatus(orderId: number, orderStatus: OrderStatus, remark?: string): Promise<boolean> {
    try {
      // 调用接口
      await updateOrderStatusApi(orderId, orderStatus, remark)
      
      // 更新本地数据
      this._updateLocalOrder(orderId, orderStatus)
      
      return true
    } catch (error) {
      console.error('更新订单状态失败:', error)
      throw error
    }
  }

  /**
   * 取消订单
   * @param orderId 订单ID
   * @param reason 取消原因
   * @returns Promise<boolean>
   */
  async cancelOrder(orderId: number, reason?: string): Promise<boolean> {
    try {
      // 调用接口
      await cancelOrderApi({ orderId, reason })
      
      // 更新本地数据
      this._updateLocalOrder(orderId, 'CANCELLED')
      
      return true
    } catch (error) {
      console.error('取消订单失败:', error)
      throw error
    }
  }

  /**
   * 支付订单（快捷方法，直接完成订单）
   * @param orderId 订单ID
   * @param paymentMethod 支付方式（可选）
   * @returns Promise<boolean>
   */
  async payOrder(orderId: number, paymentMethod?: string): Promise<boolean> {
    return this.updateOrderStatus(orderId, 'COMPLETED', `用户支付${paymentMethod ? `（${paymentMethod}）` : ''}`)
  }

  /**
   * 确认收货/完成订单
   * @param orderId 订单ID
   * @returns Promise<boolean>
   */
  async completeOrder(orderId: number): Promise<boolean> {
    return this.updateOrderStatus(orderId, 'COMPLETED', '用户确认收货')
  }

  /**
   * 申请退款（将订单状态改为已退款）
   * @param orderId 订单ID
   * @param reason 退款原因
   * @returns Promise<boolean>
   */
  async refundOrder(orderId: number, reason?: string): Promise<boolean> {
    return this.updateOrderStatus(orderId, 'REFUNDED', reason || '用户申请退款')
  }

  // ========== 本地数据更新 ==========

  /**
   * 更新本地订单数据
   */
  private _updateLocalOrder(orderId: number, orderStatus: OrderStatus) {
    // 更新列表中的订单
    const index = this._orders.findIndex(o => o.id === orderId)
    if (index !== -1) {
      this._orders[index].orderStatus = orderStatus
      this._orders[index].orderStatusDesc = this.getStatusText(orderStatus)
      
      // 如果订单完成，更新支付状态
      if (orderStatus === 'COMPLETED') {
        this._orders[index].paymentStatus = 1
        this._orders[index].paymentStatusDesc = '已支付'
        this._orders[index].paymentTime = new Date().toISOString()
      }
      // 如果订单取消，支付状态保持不变（但订单状态变更为已取消）
      if (orderStatus === 'CANCELLED') {
        // 支付状态不变，但前端需要知道订单已取消
      }
    }
    
    // 更新当前订单
    if (this._currentOrder?.id === orderId) {
      this._currentOrder.orderStatus = orderStatus
      this._currentOrder.orderStatusDesc = this.getStatusText(orderStatus)
      
      if (orderStatus === 'COMPLETED') {
        this._currentOrder.paymentStatus = 1
        this._currentOrder.paymentStatusDesc = '已支付'
        this._currentOrder.paymentTime = new Date().toISOString()
      }
    }
  }

  // ========== 筛选方法 ==========

  /**
   * 按状态筛选订单
   */
  getOrdersByStatus(status?: OrderStatus | OrderStatus[]): Order[] {
    if (!status) {
      return this._orders
    }
    
    if (Array.isArray(status)) {
      return this._orders.filter(o => status.includes(o.orderStatus))
    }
    
    return this._orders.filter(o => o.orderStatus === status)
  }

  /**
   * 获取待支付订单（支付状态为待支付且订单状态为处理中或待处理）
   */
  getPendingOrders(): Order[] {
    return this._orders.filter(o => o.paymentStatus === 0 && ['PENDING', 'PROCESSING'].includes(o.orderStatus))
  }

  /**
   * 获取已完成订单
   */
  getCompletedOrders(): Order[] {
    return this._orders.filter(o => o.orderStatus === 'COMPLETED')
  }

  /**
   * 获取已取消订单
   */
  getCancelledOrders(): Order[] {
    return this._orders.filter(o => o.orderStatus === 'CANCELLED')
  }

  // ========== 工具方法 ==========

  /**
   * 获取订单状态文本
   */
  getStatusText(status: OrderStatus): string {
    return this._statusTextMap[status] || status
  }

  /**
   * 获取支付状态文本
   */
  getPaymentStatusText(status: number): string {
    return this._paymentStatusTextMap[status] || '未知'
  }

  /**
   * 判断订单是否可以支付
   */
  canPay(order: Order): boolean {
    return order.paymentStatus === 0 && ['PENDING', 'PROCESSING'].includes(order.orderStatus)
  }

  /**
   * 判断订单是否可以取消
   */
  canCancel(order: Order): boolean {
    return order.paymentStatus === 0 && ['PENDING', 'PROCESSING'].includes(order.orderStatus)
  }

  /**
   * 判断订单是否可以确认收货
   */
  canComplete(order: Order): boolean {
    return order.paymentStatus === 1 && order.orderStatus === 'PROCESSING'
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