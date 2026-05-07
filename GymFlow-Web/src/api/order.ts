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
   * 取消订单（仅限待支付状态）
   */
  cancelOrder(orderId: number, reason?: string): Promise<ApiResponse> {
    return request({
      url: `/order/cancel/${orderId}`,
      method: 'POST',
      params: { reason }
    })
  },

  /**
   * 完成订单（一般由前端调用完成）
   */
  completeOrder(orderId: number): Promise<ApiResponse> {
    return request({
      url: `/order/complete/${orderId}`,
      method: 'POST'
    })
  },

  /**
   * 删除订单（软删除）
   */
  deleteOrder(orderId: number): Promise<ApiResponse> {
    return request({
      url: `/order/delete/${orderId}`,
      method: 'DELETE'
    })
  },

  /**
   * 订单支付（同步完成权益激活）
   */
  payOrder(orderId: number, paymentMethod?: string): Promise<ApiResponse<boolean>> {
    return request({
      url: `/order/pay/${orderId}`,
      method: 'POST',
      params: { paymentMethod }
    })
  },

  /**
   * 重试激活订单权益（仅限已支付状态）
   */
  retryActivateOrder(orderId: number): Promise<ApiResponse<boolean>> {
    return request({
      url: `/order/retry-activate/${orderId}`,
      method: 'POST'
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