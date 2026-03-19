// 消息相关API

import { wxRequest } from '../../utils/request'
import { Message, MessageQueryParams, PageResult } from '../types/message.types'

/**
 * 获取消息列表
 * GET /mini/message/list
 */
export const getMessageList = (params: MessageQueryParams) => {
  return wxRequest.get<PageResult<Message>>('/mini/message/list', params, {
    showLoading: false
  })
}

/**
 * 获取未读消息数
 * GET /mini/message/unread-count
 */
export const getUnreadCount = () => {
  return wxRequest.get<number>('/mini/message/unread-count')
}

/**
 * 标记消息为已读
 * PUT /mini/message/read/{messageId}
 */
export const markMessageAsRead = (messageId: number) => {
  return wxRequest.put(`/mini/message/read/${messageId}`)
}

/**
 * 标记所有消息为已读
 * PUT /mini/message/read-all
 */
export const markAllAsRead = () => {
  return wxRequest.put('/mini/message/read-all')
}

/**
 * 删除消息
 * DELETE /mini/message/delete/{messageId}
 */
export const deleteMessage = (messageId: number) => {
  return wxRequest.delete(`/mini/message/delete/${messageId}`)
}