// 通用响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp?: string
}

// 分页结果
export interface PageResultVO<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

// 商品查询DTO
export interface ProductQueryDTO {
  pageNum?: number
  pageSize?: number
  productType?: number
  productName?: string
  status?: number
  includeZeroStock?: boolean
}

// 商品基础信息DTO
export interface ProductBasicDTO {
  productName: string
  productType: number
  description?: string
  images?: string[]
  originalPrice: number
  currentPrice: number
  stockQuantity: number
  specifications?: string
  status: number
  validityDays?: number          // 有效期天数（仅会籍卡）
  totalSessions?: number          // 总课时数（仅私教课/团课）
  membershipBenefits?: string[]   // 会籍权益（仅会籍卡）
  maxPurchaseQuantity?: number    // 最大购买数量
  refundPolicy?: string           // 退款政策
  usageRules?: string             // 使用规则
}

// 商品完整信息DTO
export interface ProductFullDTO {
  id: number
  productName: string
  productType: number
  productTypeDesc: string
  description?: string
  images: string[]
  originalPrice: number
  currentPrice: number
  stockQuantity: number
  salesVolume: number
  specifications?: string
  status: number
  statusDesc: string
  validityDays?: number
  totalSessions?: number
  membershipBenefits?: string[]
  maxPurchaseQuantity?: number
  refundPolicy?: string
  usageRules?: string
  createTime: string
  updateTime?: string
}

// 商品列表VO
export interface ProductListVO {
  id: number
  productName: string
  productType: number
  productTypeDesc: string
  currentPrice: number
  originalPrice: number
  discount?: number
  stockQuantity: number
  salesVolume: number
  totalSessions?: number
  validityDays?: number
  status: number
  statusDesc: string
  images: string[]
  createTime: string
}

// 商品类型枚举
export enum ProductType {
  MEMBERSHIP = 0,      // 会籍卡
  PRIVATE_COURSE = 1,  // 私教课
  GROUP_COURSE = 2,    // 团课
  RELATED_PRODUCT = 3  // 相关产品
}

// 商品状态枚举
export enum ProductStatus {
  OFF_SHELF = 0,  // 下架
  ON_SALE = 1     // 在售
}