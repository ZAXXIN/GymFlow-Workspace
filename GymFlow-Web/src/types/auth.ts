// 登录请求参数
export interface LoginDTO {
  username: string
  password: string
}

// 登录返回结果
export interface LoginResultDTO {
  userId: number
  username: string
  realName?: string
  phone?: string
  role: number
  token: string
  loginTime: string
  avatar?: string
}

// 用户信息
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
  avatar?: string
}

// 统一响应格式
export interface ApiResponse<T = any> {
  code: number
  message: string
  data?: T
  timestamp: number
}