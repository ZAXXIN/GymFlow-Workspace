// 订单相关类型定义

// 订单状态（新）
export type OrderStatus = 'WAIT_PAY' | 'PAID' | 'COMPLETED' | 'CANCELLED' | 'REFUNDED'
export type OrderType = 0 | 1 | 2 | 3 // 0-会籍卡 1-私教课 2-团课 3-相关产品

// 订单
export interface Order {
  id: number
  orderNo: string
  memberId: number
  memberName?: string
  memberPhone?: string
  orderType: OrderType
  orderTypeDesc: string
  totalAmount: number
  actualAmount: number
  paymentMethod?: string
  paymentTime?: string
  status: OrderStatus          // 订单状态
  statusDesc: string           // 订单状态描述
  remark?: string
  createTime: string
  updateTime: string
  items?: OrderItem[]
}

// 订单项
export interface OrderItem {
  id: number
  productId: number
  productName: string
  productType: number
  quantity: number
  unitPrice: number
  totalPrice: number
  validityStartDate?: string
  validityEndDate?: string
  totalSessions?: number
  remainingSessions?: number
  status: string
  productImage?: string
}

// 订单查询参数
export interface OrderQueryParams {
  pageNum?: number
  pageSize?: number
  orderNo?: string
  orderType?: OrderType
  status?: OrderStatus          // 使用合并后的状态
  startDate?: string
  endDate?: string
}

// 创建订单参数
export interface CreateOrderParams {
  memberId: number
  orderType: OrderType
  items: CreateOrderItemParams[]
  remark?: string
}

// 创建订单项参数
export interface CreateOrderItemParams {
  productId: number
  productName: string
  productType: number
  quantity: number
  unitPrice: number
}

// 支付订单参数（仅需订单ID，支付方式可选）
export interface PayOrderParams {
  orderId: number
  paymentMethod?: string
}

// 取消订单参数
export interface CancelOrderParams {
  orderId: number
  reason?: string
}