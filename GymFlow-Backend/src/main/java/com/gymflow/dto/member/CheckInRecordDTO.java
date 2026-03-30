package com.gymflow.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "会员签到记录DTO")
public class CheckInRecordDTO {

    @Schema(description = "签到时间")
    private LocalDateTime checkinTime;

    @Schema(description = "签到方式：0-教练签到，1-前台签到")
    private Integer checkinMethod;

    @Schema(description = "签到备注")
    private String notes;

    @Schema(description = "排课ID")
    private Long scheduleId;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "教练ID")
    private Long coachId;

    @Schema(description = "教练姓名")
    private String coachName;
}