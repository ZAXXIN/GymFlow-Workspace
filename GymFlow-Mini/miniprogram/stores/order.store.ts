// 订单状态管理

import { getOrderList, cancelOrder } from '../services/api/order.api'
import { Order, OrderQueryParams, OrderStatus } from '../services/types/order.types'

class OrderStore {
  private _orders: Order[] = []
  private _currentOrder: Order | null = null
  private _loading: boolean = false
  private _hasMore: boolean = true
  private _currentPage: number = 1
  private _pageSize: number = 10

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
   * 取消订单
   */
  async cancelOrder(orderId: number, reason?: string) {
    try {
      await cancelOrder({ orderId, reason })
      
      // 更新本地数据
      const index = this._orders.findIndex(o => o.id === orderId)
      if (index !== -1) {
        this._orders[index].orderStatus = 'CANCELLED'
        this._orders[index].orderStatusDesc = '已取消'
      }
      
      return true
    } catch (error) {
      console.error('取消订单失败:', error)
      throw error
    }
  }

  /**
   * 设置当前订单
   */
  setCurrentOrder(order: Order) {
    this._currentOrder = order
  }

  /**
   * 更新订单状态
   */
  updateOrderStatus(orderId: number, status: OrderStatus) {
    const order = this._orders.find(o => o.id === orderId)
    if (order) {
      order.orderStatus = status
      order.orderStatusDesc = this.getStatusText(status)
    }
    
    if (this._currentOrder?.id === orderId) {
      this._currentOrder.orderStatus = status
      this._currentOrder.orderStatusDesc = this.getStatusText(status)
    }
  }

  /**
   * 更新订单支付状态
   */
  updatePaymentStatus(orderId: number, paymentStatus: 0 | 1) {
    const order = this._orders.find(o => o.id === orderId)
    if (order) {
      order.paymentStatus = paymentStatus
      order.paymentStatusDesc = paymentStatus === 1 ? '已支付' : '待支付'
      if (paymentStatus === 1) {
        order.paymentTime = new Date().toISOString()
      }
    }
    
    if (this._currentOrder?.id === orderId) {
      this._currentOrder.paymentStatus = paymentStatus
      this._currentOrder.paymentStatusDesc = paymentStatus === 1 ? '已支付' : '待支付'
      if (paymentStatus === 1) {
        this._currentOrder.paymentTime = new Date().toISOString()
      }
    }
  }

  /**
   * 获取状态文本
   */
  private getStatusText(status: OrderStatus): string {
    const statusMap: Record<OrderStatus, string> = {
      'PENDING': '待支付',
      'PROCESSING': '处理中',
      'COMPLETED': '已完成',
      'CANCELLED': '已取消',
      'REFUNDED': '已退款',
      'DELETED': '已删除'
    }
    return statusMap[status] || status
  }

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
   * 获取待支付订单
   */
  getPendingOrders(): Order[] {
    return this._orders.filter(o => o.paymentStatus === 0)
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

  // Getters
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