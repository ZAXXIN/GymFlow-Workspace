package com.gymflow.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "课程预约DTO")
public class CourseBookingDTO {

    @Schema(description = "预约ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "会员手机号")
    private String memberPhone;

    @Schema(description = "预约时间")
    private LocalDateTime bookingTime;

    @Schema(description = "预约状态：0-待上课，1-已签到，2-已完成，3-已取消")
    private Integer bookingStatus;

    @Schema(description = "预约状态描述")
    private String bookingStatusDesc;

    @Schema(description = "签到时间")
    private LocalDateTime checkinTime;

    @Schema(description = "取消原因")
    private String cancellationReason;

    @Schema(description = "取消时间")
    private LocalDateTime cancellationTime;
}