package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("checkin_record")
public class CheckinRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    @TableField("course_booking_id")
    private Long courseBookingId;

    @TableField("checkin_time")
    private LocalDateTime checkinTime;

    @TableField("checkin_method")
    private Integer checkinMethod;

    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}