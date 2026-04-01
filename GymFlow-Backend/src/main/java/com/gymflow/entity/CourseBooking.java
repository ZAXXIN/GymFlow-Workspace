package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("course_booking")
public class CourseBooking {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private Long courseId;

    @TableField("schedule_id")
    private Long scheduleId;

    @TableField("booking_time")
    private LocalDateTime bookingTime;

    // 0-待上课，1-已签到，2-已完成，3-已取消，4-已过期
    @TableField("booking_status")
    private Integer bookingStatus;

    @TableField("checkin_time")
    private LocalDateTime checkinTime;

    @TableField("cancellation_reason")
    private String cancellationReason;

    @TableField("cancellation_time")
    private LocalDateTime cancellationTime;

    // ========== 签到码相关字段 ==========

    @TableField("sign_code")
    private String signCode;

    @TableField("sign_code_expire_time")
    private LocalDateTime signCodeExpireTime;

    @TableField("checkin_code_id")
    private Long checkinCodeId;

    @TableField("auto_complete_time")
    private LocalDateTime autoCompleteTime;

    // ========== 新增：订单项ID（记录使用的是哪个课时包） ==========
    @TableField("order_item_id")
    private Long orderItemId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}