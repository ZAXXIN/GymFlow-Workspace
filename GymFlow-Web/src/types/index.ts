// 分页参数
export interface PaginationParams {
  page: number
  pageSize: number
  total?: number
}

// 排序参数
export interface SortParams {
  field?: string
  order?: 'asc' | 'desc'
}

// 通用查询参数
export interface QueryParams extends PaginationParams, SortParams {
  keyword?: string
  [key: string]: any
}

// 通用响应格式
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

// 分页数据
export interface PageResult<T = any> {
  list: T[]
  total: number
  page: number
  pageSize: number
}