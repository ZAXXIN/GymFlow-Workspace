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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}