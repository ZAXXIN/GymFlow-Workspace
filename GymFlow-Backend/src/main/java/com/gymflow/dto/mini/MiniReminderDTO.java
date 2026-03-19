package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "当前时段提醒DTO")
public class MiniReminderDTO {

    @Schema(description = "提醒类型：MEMBER-会员端提醒，COACH-教练端提醒")
    private String type;

    // ========== 会员端提醒 ==========

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "教练姓名")
    private String coachName;

    @Schema(description = "预约时间")
    private LocalDateTime bookingTime;

    @Schema(description = "预约ID")
    private Long bookingId;

    @Schema(description = "签到码")
    private MiniCheckinCodeDTO checkinCode;

    // ========== 教练端提醒 ==========

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "预约的课程名称")
    private String bookedCourseName;

    @Schema(description = "预约开始时间")
    private LocalDateTime bookedStartTime;

    @Schema(description = "预约结束时间")
    private LocalDateTime bookedEndTime;

    // ========== 通用 ==========

    @Schema(description = "是否有提醒")
    private Boolean hasReminder;
}