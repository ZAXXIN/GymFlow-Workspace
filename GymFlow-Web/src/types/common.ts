// 统一响应格式
export interface ApiResponse<T = any> {
  code: number
  message: string
  timestamp: number
}

// 分页结果
export interface PageResultVO<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages?: number
}

export interface UserInfo {
  userId: number
  username: string
  realName?: string
  phone?: string
  role: number
  department?: string
  position?: string
  gender?: number
  birthday?: string
  status: number
}