// 订单相关API

import { wxRequest } from '../../utils/request'
import { 
  Order, 
  OrderQueryParams, 
  PageResult,
  CreateOrderParams,
  PayOrderParams,
  CancelOrderParams
} from '../types/order.types'

/**
 * 获取订单列表
 * POST /order/list
 */
export const getOrderList = (params: OrderQueryParams) => {
  return wxRequest.post<PageResult<Order>>('/order/list', params, {
    showLoading: false
  })
}

/**
 * 获取订单详情
 * GET /order/detail/{orderId}
 */
export const getOrderDetail = (orderId: number) => {
  return wxRequest.get<Order>(`/order/detail/${orderId}`, null, {
    showLoading: true
  })
}

/**
 * 创建订单
 * POST /order/create
 */
export const createOrder = (params: CreateOrderParams) => {
  return wxRequest.post<{ orderId: number }>('/order/create', params, {
    showLoading: true,
    loadingText: '创建订单中...'
  })
}

/**
 * 支付订单
 * POST /order/pay/{orderId}
 */
export const payOrder = ({ orderId, paymentMethod }: PayOrderParams) => {
  return wxRequest.post(`/order/pay/${orderId}`, null, {
    params: { paymentMethod },
    showLoading: true,
    loadingText: '支付中...'
  })
}

/**
 * 取消订单
 * POST /order/cancel/{orderId}
 */
export const cancelOrder = ({ orderId, reason }: CancelOrderParams) => {
  return wxRequest.post(`/order/cancel/${orderId}`, null, {
    params: { reason },
    showLoading: true
  })
}

/**
 * 获取会员订单列表
 * POST /order/member/{memberId}
 */
export const getMemberOrders = (memberId, params) => {
  if (!memberId) {
    return Promise.reject(new Error('用户未登录'))
  }
  return wxRequest.post(`/order/member/${memberId}`, params)
}