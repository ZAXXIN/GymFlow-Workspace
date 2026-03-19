// 消息相关类型定义

// 消息类型
export type MessageType = 'BOOKING' | 'CHECKIN' | 'SYSTEM'

// 消息
export interface Message {
  id: number
  type: MessageType
  title: string
  content: string
  isRead: 0 | 1
  createTime: string
  data?: any
}

// 消息列表查询参数
export interface MessageQueryParams {
  pageNum?: number
  pageSize?: number
  type?: MessageType
  isRead?: 0 | 1
}