package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "预约列表DTO")
public class MiniBookingDTO {

    @Schema(description = "预约ID")
    private Long bookingId;

    @Schema(description = "排课ID")
    private Long scheduleId;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程类型：0-私教课，1-团课")
    private Integer courseType;

    @Schema(description = "教练姓名")
    private String coachName;

    @Schema(description = "课程日期")
    private String courseDate;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "预约时间")
    private LocalDateTime bookingTime;

    @Schema(description = "预约状态：0-待上课，1-已签到，2-已完成，3-已取消")
    private Integer bookingStatus;

    @Schema(description = "预约状态描述")
    private String bookingStatusDesc;

    @Schema(description = "签到时间")
    private LocalDateTime checkinTime;

    @Schema(description = "是否可取消")
    private Boolean canCancel;

    @Schema(description = "是否可签到")
    private Boolean canCheckin;

    public String getBookingStatusDesc() {
        if (bookingStatus == null) return "未知";
        switch (bookingStatus) {
            case 0: return "待上课";
            case 1: return "已签到";
            case 2: return "已完成";
            case 3: return "已取消";
            default: return "未知";
        }
    }
}