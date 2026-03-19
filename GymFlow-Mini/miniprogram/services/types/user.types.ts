// 用户相关类型定义

// 用户类型
export type UserRole = 'MEMBER' | 'COACH'

// 小程序登录响应（后端返回）
export interface MiniLoginResult {
  userId: number
  userType: 0 | 1  // 0-会员，1-教练
  phone: string
  realName: string
  memberNo?: string  // 会员才有
  token: string
  loginTime?: string
}

// 前端使用的用户信息（扩展后）
export interface UserInfo extends MiniLoginResult {
  role: UserRole
  memberId?: number      // 会员ID（等于 userId）
  coachId?: number       // 教练ID（等于 userId）
  memberNo?: string      // 会员编号
}

// 登录参数
export interface LoginParams {
  phone: string
  password: string
}

// 修改密码参数
export interface ChangePasswordParams {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}