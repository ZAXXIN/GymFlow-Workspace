import request from '@/utils/request'
import type {
  OrderQueryParams,
  OrderBasicDTO,
  OrderStatusDTO,
  OrderListVO,
  OrderDetailVO,
  PageResultVO,
  ApiResponse
} from '@/types/order'

// 订单管理API
export const orderApi = {
  /**
   * 分页查询订单列表
   */
  getOrderList(params: OrderQueryParams): Promise<ApiResponse<PageResultVO<OrderListVO>>> {
    return request({
      url: '/order/list',
      method: 'POST',
      data: params
    })
  },

  /**
   * 获取订单详情
   */
  getOrderDetail(orderId: number): Promise<ApiResponse<OrderDetailVO>> {
    return request({
      url: `/order/detail/${orderId}`,
      method: 'GET'
    })
  },

  /**
   * 创建订单
   */
  createOrder(data: OrderBasicDTO): Promise<ApiResponse<number>> {
    return request({
      url: '/order/create',
      method: 'POST',
      data
    })
  },

  /**
   * 更新订单信息
   */
  updateOrder(orderId: number, data: OrderBasicDTO): Promise<ApiResponse> {
    return request({
      url: `/order/update/${orderId}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 更新订单状态
   */
  updateOrderStatus(orderId: number, data: OrderStatusDTO): Promise<ApiResponse> {
    return request({
      url: `/order/updateStatus/${orderId}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 取消订单
   */
  cancelOrder(orderId: number, reason?: string): Promise<ApiResponse> {
    return request({
      url: `/order/cancel/${orderId}`,
      method: 'POST',
      params: { reason }
    })
  },

  /**
   * 完成订单
   */
  completeOrder(orderId: number): Promise<ApiResponse> {
    return request({
      url: `/order/complete/${orderId}`,
      method: 'POST'
    })
  },

  /**
   * 删除订单
   */
  deleteOrder(orderId: number): Promise<ApiResponse> {
    return request({
      url: `/order/delete/${orderId}`,
      method: 'DELETE'
    })
  },

  /**
   * 批量删除订单
   */
  batchDeleteOrder(ids: number[]): Promise<ApiResponse> {
    return request({
      url: '/order/batch-delete',
      method: 'POST',
      data: ids
    })
  },

  /**
   * 订单支付
   */
  payOrder(orderId: number, paymentMethod?: string): Promise<ApiResponse> {
    return request({
      url: `/order/pay/${orderId}`,
      method: 'POST',
      params: { paymentMethod }
    })
  },

  /**
   * 订单退款
   */
  refundOrder(orderId: number, refundAmount: number, reason?: string): Promise<ApiResponse> {
    return request({
      url: `/order/refund/${orderId}`,
      method: 'POST',
      params: { refundAmount, reason }
    })
  },

  /**
   * 获取会员订单列表
   */
  getMemberOrders(memberId: number, params: OrderQueryParams): Promise<ApiResponse<PageResultVO<OrderListVO>>> {
    return request({
      url: `/order/member/${memberId}`,
      method: 'POST',
      data: params
    })
  }
}