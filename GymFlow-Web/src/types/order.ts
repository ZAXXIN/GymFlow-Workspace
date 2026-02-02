// 分页查询参数
export interface OrderQueryParams {
  pageNum?: number
  pageSize?: number
  orderNo?: string
  memberId?: number
  orderType?: number
  paymentStatus?: number
  orderStatus?: string
  startDate?: string // YYYY-MM-DD
  endDate?: string   // YYYY-MM-DD
}

// 订单基础信息DTO
export interface OrderBasicDTO {
  memberId: number
  orderType: number
  paymentMethod?: string
  remark?: string
  orderItems: OrderItemDTO[]
}

// 订单项DTO
export interface OrderItemDTO {
  productId: number
  productName?: string
  productType?: number
  quantity: number
  unitPrice: number
  totalPrice?: number
  totalSessions?: number
  remainingSessions?: number
  validityStartDate?: string
  validityEndDate?: string
  status?: string
  productImage?: string
}

// 订单状态DTO
export interface OrderStatusDTO {
  orderStatus: string
  remark?: string
}

// 订单列表VO
export interface OrderListVO {
  id: number
  orderNo: string
  memberId: number
  memberName?: string
  memberPhone?: string
  orderType?: number
  orderTypeDesc?: string
  totalAmount?: number
  actualAmount?: number
  paymentStatus: number
  paymentStatusDesc: string
  paymentMethod?: string
  paymentTime?: string
  orderStatus: string
  orderStatusDesc: string
  itemCount: number
  createTime: string
}

// 订单详情VO
export interface OrderDetailVO {
  id: number
  orderNo: string
  memberId: number
  memberName?: string
  memberPhone?: string
  orderType: number
  orderTypeDesc: string
  totalAmount: number
  actualAmount: number
  paymentMethod?: string
  paymentStatus: number
  paymentStatusDesc: string
  paymentTime?: string
  orderStatus: string
  orderStatusDesc: string
  remark?: string
  createTime: string
  updateTime?: string
  orderItems: OrderItemVO[]
}

// 订单项VO
export interface OrderItemVO {
  id: number
  productId: number
  productName: string
  productType: number
  productTypeDesc?: string
  productImage?: string
  quantity: number
  unitPrice: number
  totalPrice: number
  totalSessions?: number
  remainingSessions?: number
  validityStartDate?: string
  validityEndDate?: string
  status: string
  statusDesc?: string
}

// 订单完整DTO
export interface OrderFullDTO {
  id: number
  orderNo: string
  memberId: number
  memberInfo?: {
    id: number
    realName: string
    phone: string
  }
  orderType: number
  orderTypeDesc: string
  totalAmount: number
  actualAmount: number
  paymentMethod?: string
  paymentStatus: number
  paymentStatusDesc: string
  paymentTime?: string
  orderStatus: string
  orderStatusDesc: string
  remark?: string
  createTime: string
  updateTime?: string
  orderItems: OrderItemDTO[]
  paymentRecords?: any[]
}

// 分页结果
export interface PageResultVO<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages?: number
}

// API响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: string
}