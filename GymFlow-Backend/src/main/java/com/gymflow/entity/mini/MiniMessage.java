package com.gymflow.entity.mini;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 小程序消息通知实体类
 * 用于存储会员和教练的消息通知
 */
@Data
@TableName("mini_message")
public class MiniMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（会员ID或教练ID）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户类型：0-会员，1-教练
     */
    @TableField("user_type")
    private Integer userType;

    /**
     * 消息类型：
     * BOOKING_SUCCESS - 预约成功
     * BOOKING_CANCEL - 预约取消
     * CHECKIN_SUCCESS - 签到成功
     * COURSE_REMINDER - 课程提醒
     */
    @TableField("message_type")
    private String messageType;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 关联ID（预约ID、课程ID等）
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 是否已读：0-未读，1-已读
     */
    @TableField("is_read")
    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ========== 非数据库字段 ==========

    @TableField(exist = false)
    private String timeDesc; // 时间描述（刚刚、几分钟前等）

    @TableField(exist = false)
    private String typeDesc; // 类型描述

    public String getTypeDesc() {
        if (messageType == null) return "系统通知";
        switch (messageType) {
            case "BOOKING_SUCCESS": return "预约成功";
            case "BOOKING_CANCEL": return "预约取消";
            case "CHECKIN_SUCCESS": return "签到成功";
            case "COURSE_REMINDER": return "课程提醒";
            default: return "系统通知";
        }
    }
}