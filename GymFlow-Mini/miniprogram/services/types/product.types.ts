// 商品相关类型定义

// 商品类型
export type ProductType = 0 | 1 | 2 | 3 // 0-会籍卡 1-私教课 2-团课 3-相关产品

// 商品
export interface Product {
  id: number
  productName: string
  productType: ProductType
  productTypeDesc: string
  categoryId?: number
  categoryName?: string
  description?: string
  images: string[]
  originalPrice: number
  currentPrice: number
  stockQuantity: number
  salesVolume?: number
  unit?: string
  validityDays?: number
  specifications?: string
  status: 0 | 1
  statusDesc: string
  createTime: string
  updateTime: string
  detail?: ProductDetail
}

// 商品详情
export interface ProductDetail {
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

// 商品分类
export interface ProductCategory {
  id: number
  categoryName: string
  parentId: number
  status: 0 | 1
  statusDesc: string
  createTime: string
  updateTime: string
  children?: ProductCategory[]
}

// 商品查询参数
export interface ProductQueryParams {
  pageNum?: number
  pageSize?: number
  productType?: ProductType
  productName?: string
  categoryId?: number
  status?: 0 | 1
  includeZeroStock?: boolean
}

// 分页响应
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}