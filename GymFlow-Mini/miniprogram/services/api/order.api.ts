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
 * 更新订单状态（直接设置状态）
 * PUT /order/updateStatus/{orderId}
 */
export const updateOrderStatus = (orderId: number, status: string, remark?: string) => {
  return wxRequest.put(`/order/updateStatus/${orderId}`, {
    status,
    remark
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
 * 支付订单（同步完成权益激活）
 * POST /order/pay/{orderId}
 */
export const payOrder = ({ orderId, paymentMethod }: PayOrderParams) => {
  return wxRequest.post<boolean>(`/order/pay/${orderId}`, null, {
    params: { paymentMethod: paymentMethod || '微信支付' },
    showLoading: true,
    loadingText: '支付中...'
  })
}

/**
 * 重试激活订单权益（仅限 PAID 状态）
 * POST /order/retry-activate/{orderId}
 */
export const retryActivateOrder = (orderId: number) => {
  return wxRequest.post<boolean>(`/order/retry-activate/${orderId}`, null, {
    showLoading: true,
    loadingText: '激活中...'
  })
}

/**
 * 获取会员订单列表
 * POST /order/member/{memberId}
 */
export const getMemberOrders = (memberId: number, params: OrderQueryParams) => {
  if (!memberId) {
    return Promise.reject(new Error('用户未登录'))
  }
  return wxRequest.post<PageResult<Order>>(`/order/member/${memberId}`, params)
}