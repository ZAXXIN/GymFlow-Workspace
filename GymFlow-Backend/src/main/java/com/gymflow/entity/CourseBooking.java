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

    @TableField("schedule_id")  // 新增：关联排课ID
    private Long scheduleId;

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

    @TableField("sign_code")  // 新增：6位签到码
    private String signCode;

    @TableField("sign_code_expire_time")  // 新增：签到码过期时间
    private LocalDateTime signCodeExpireTime;

    @TableField("checkin_code_id")
    private Long checkinCodeId;

    @TableField("auto_complete_time")
    private LocalDateTime autoCompleteTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}