package com.gymflow.service.mini;

import com.gymflow.dto.mini.MiniMessageDTO;
import com.gymflow.dto.mini.MiniUnreadCountDTO;

import java.util.List;

public interface MiniMessageService {

    /**
     * 获取消息列表
     */
    List<MiniMessageDTO> getMessageList(Long userId, Integer userType, Integer pageNum, Integer pageSize);

    /**
     * 获取未读消息数量
     */
    MiniUnreadCountDTO getUnreadCount(Long userId, Integer userType);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long userId, Long messageId);

    /**
     * 标记所有消息为已读
     */
    void markAllAsRead(Long userId, Integer userType);

    /**
     * 删除消息
     */
    void deleteMessage(Long userId, Long messageId);

    /**
     * 发送消息（内部调用）
     */
    void sendMessage(Long userId, Integer userType, String messageType, String title, String content, Long relatedId);
}