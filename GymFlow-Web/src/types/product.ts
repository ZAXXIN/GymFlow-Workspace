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
  categoryId?: number
  status?: number
  includeZeroStock?: boolean
}

// 商品基础信息DTO
export interface ProductBasicDTO {
  productName: string
  productType: number
  categoryId?: number
  description?: string
  images?: string[]
  originalPrice: number
  currentPrice: number
  costPrice?: number
  stockQuantity: number
  unit?: string
  specifications?: string
  status: number
  detailDTO?: ProductDetailDTO
}

// 商品详情DTO
export interface ProductDetailDTO {
  membershipType?: number
  coachId?: number
  courseDuration?: number
  totalSessions?: number
  availableSessions?: number
  courseLevel?: string
  membershipBenefits?: string[]
  validityDays?: number
  defaultTotalSessions?: number
  maxPurchaseQuantity?: number
  refundPolicy?: string
  usageRules?: string
  coachIds?: number[]
}

// 商品分类DTO
export interface ProductCategoryDTO {
  id?: number
  categoryName: string
  parentId?: number
  status: number
  statusDesc?: string
  createTime?: string
  updateTime?: string
  children?: ProductCategoryDTO[]
}

// 商品完整信息DTO
export interface ProductFullDTO {
  id: number
  productName: string
  productType: number
  productTypeDesc: string
  categoryId?: number
  categoryName?: string
  description?: string
  images: string[]
  originalPrice: number
  currentPrice: number
  costPrice?: number
  stockQuantity: number
  salesVolume: number
  unit?: string
  validityDays?: number
  specifications?: string
  status: number
  statusDesc: string
  createTime: string
  updateTime?: string
  detailDTO?: ProductDetailDTO
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
  status: number
  statusDesc: string
  images: string[]
  createTime: string
}

// 商品类型枚举
export enum ProductType {
  MEMBERSHIP = 0,    // 会籍卡
  PRIVATE_COURSE = 1, // 私教课
  GROUP_COURSE = 2,   // 团课
  RELATED_PRODUCT = 3 // 相关产品
}

// 商品状态枚举
export enum ProductStatus {
  OFF_SHELF = 0, // 下架
  ON_SALE = 1    // 在售
}

// 分类状态枚举
export enum CategoryStatus {
  DISABLED = 0, // 禁用
  ENABLED = 1   // 启用
}