import { http } from '@/utils/request'
import type { Order, QueryParams, PaginatedResponse, ApiResponse } from '@/types'
import { ApiPaths } from '@/utils/constants'

export const orderApi = {
  /**
   * 获取订单列表
   */
  getOrders(params?: QueryParams): Promise<PaginatedResponse<Order>> {
    return http.get(ApiPaths.ORDERS, { params })
  },

  /**
   * 获取订单详情
   */
  getOrderById(id: number): Promise<Order> {
    return http.get(ApiPaths.ORDER_DETAIL(id))
  },

  /**
   * 创建订单
   */
  createOrder(data: Partial<Order>): Promise<Order> {
    return http.post(ApiPaths.ORDERS, data)
  },

  /**
   * 更新订单
   */
  updateOrder(id: number, data: Partial<Order>): Promise<Order> {
    return http.put(ApiPaths.ORDER_DETAIL(id), data)
  },

  /**
   * 删除订单
   */
  deleteOrder(id: number): Promise<void> {
    return http.delete(ApiPaths.ORDER_DETAIL(id))
  },

  /**
   * 获取订单统计
   */
  getStatistics(params?: QueryParams): Promise<any> {
    return http.get(ApiPaths.ORDER_STATISTICS, { params })
  },

  /**
   * 确认订单
   */
  confirmOrder(id: number): Promise<Order> {
    return http.post(`/orders/${id}/confirm`)
  },

  /**
   * 取消订单
   */
  cancelOrder(id: number, reason?: string): Promise<Order> {
    return http.post(`/orders/${id}/cancel`, { reason })
  },

  /**
   * 退款
   */
  refundOrder(id: number, reason?: string): Promise<Order> {
    return http.post(`/orders/${id}/refund`, { reason })
  },

  /**
   * 完成订单
   */
  completeOrder(id: number): Promise<Order> {
    return http.post(`/orders/${id}/complete`)
  },

  /**
   * 导出订单数据
   */
  exportOrders(params?: QueryParams): Promise<Blob> {
    return http.get('/orders/export', {
      params,
      responseType: 'blob'
    })
  },

  /**
   * 获取订单商品项
   */
  getOrderItems(orderId: number): Promise<any[]> {
    return http.get(`/orders/${orderId}/items`)
  }
}