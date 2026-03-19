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

    @TableField("booking_time")
    private LocalDateTime bookingTime;

    @TableField("booking_status")
    private Integer bookingStatus;

    @TableField("checkin_time")
    private LocalDateTime checkinTime;

    @TableField("cancellation_reason")
    private String cancellationReason;

    @TableField("cancellation_time")
    private LocalDateTime cancellationTime;


    // ========== 签到码相关字段 ==========

    /**
     * 签到码ID（关联mini_checkin_code表）
     */
    @TableField("checkin_code_id")
    private Long checkinCodeId;

    /**
     * 自动完成时间（用于定时任务）
     */
    @TableField("auto_complete_time")
    private LocalDateTime autoCompleteTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}