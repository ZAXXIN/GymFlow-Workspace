package com.gymflow.service.impl.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.mini.MiniMessageDTO;
import com.gymflow.dto.mini.MiniUnreadCountDTO;
import com.gymflow.entity.mini.MiniMessage;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.mini.MiniMessageMapper;
import com.gymflow.service.mini.MiniMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiniMessageServiceImpl implements MiniMessageService {

    private final MiniMessageMapper miniMessageMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    @Override
    public List<MiniMessageDTO> getMessageList(Long userId, Integer userType, Integer pageNum, Integer pageSize) {
        // 计算分页偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询消息列表
        List<MiniMessage> messages = miniMessageMapper.selectUserMessages(userId, userType, offset, pageSize);

        // 转换为DTO并设置时间描述
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MiniUnreadCountDTO getUnreadCount(Long userId, Integer userType) {
        Integer totalUnread = miniMessageMapper.selectUnreadCount(userId, userType);

        MiniUnreadCountDTO dto = new MiniUnreadCountDTO();
        dto.setTotal(totalUnread);

        // 按类型统计（如果有需要）
        // 这里简单处理，全部归入总未读
        dto.setBooking(0);
        dto.setCheckin(0);
        dto.setSystem(0);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long messageId) {
        MiniMessage message = miniMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 验证是否是本人的消息
        if (!message.getUserId().equals(userId)) {
            throw new BusinessException("无权操作他人的消息");
        }

        if (message.getIsRead() == 0) {
            message.setIsRead(1);
            miniMessageMapper.updateById(message);
            log.info("消息标记为已读，消息ID：{}", messageId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId, Integer userType) {
        int count = miniMessageMapper.markAllAsRead(userId, userType);
        log.info("全部消息标记为已读，用户ID：{}，用户类型：{}，数量：{}", userId, userType, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long userId, Long messageId) {
        MiniMessage message = miniMessageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }

        // 验证是否是本人的消息
        if (!message.getUserId().equals(userId)) {
            throw new BusinessException("无权删除他人的消息");
        }

        int result = miniMessageMapper.deleteById(messageId);
        if (result > 0) {
            log.info("消息删除成功，消息ID：{}", messageId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Long userId, Integer userType, String messageType,
                            String title, String content, Long relatedId) {
        MiniMessage message = new MiniMessage();
        message.setUserId(userId);
        message.setUserType(userType);
        message.setMessageType(messageType);
        message.setTitle(title);
        message.setContent(content);
        message.setRelatedId(relatedId);
        message.setIsRead(0); // 未读

        int result = miniMessageMapper.insert(message);
        if (result > 0) {
            log.info("消息发送成功，用户ID：{}，消息类型：{}", userId, messageType);
        } else {
            log.error("消息发送失败，用户ID：{}", userId);
        }
    }

    /**
     * 将实体转换为DTO
     */
    private MiniMessageDTO convertToDTO(MiniMessage message) {
        MiniMessageDTO dto = new MiniMessageDTO();
        dto.setId(message.getId());
        dto.setMessageType(message.getMessageType());
        dto.setTitle(message.getTitle());
        dto.setContent(message.getContent());
        dto.setRelatedId(message.getRelatedId());
        dto.setIsRead(message.getIsRead());
        dto.setCreateTime(message.getCreateTime());
        dto.setTimeDesc(getTimeDesc(message.getCreateTime()));

        // 设置类型描述
        if (StringUtils.hasText(message.getMessageType())) {
            switch (message.getMessageType()) {
                case "BOOKING_SUCCESS":
                    dto.setTypeDesc("预约成功");
                    break;
                case "BOOKING_CANCEL":
                    dto.setTypeDesc("预约取消");
                    break;
                case "CHECKIN_SUCCESS":
                    dto.setTypeDesc("签到成功");
                    break;
                case "COURSE_REMINDER":
                    dto.setTypeDesc("课程提醒");
                    break;
                default:
                    dto.setTypeDesc("系统通知");
            }
        } else {
            dto.setTypeDesc("系统通知");
        }

        return dto;
    }

    /**
     * 获取时间描述（刚刚、几分钟前等）
     */
    private String getTimeDesc(LocalDateTime time) {
        if (time == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(time, now);
        long hours = ChronoUnit.HOURS.between(time, now);
        long days = ChronoUnit.DAYS.between(time.toLocalDate(), now.toLocalDate());

        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (hours < 24) {
            return hours + "小时前";
        } else if (days < 7) {
            return days + "天前";
        } else {
            return time.format(DATE_FORMATTER);
        }
    }
}