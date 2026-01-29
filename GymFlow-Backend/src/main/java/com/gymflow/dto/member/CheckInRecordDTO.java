package com.gymflow.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Schema(description = "会员签到记录DTO")
public class CheckInRecordDTO {

    @NotNull(message = "签到时间不能为空")
    @Schema(description = "签到时间", example = "2026-01-28T09:30:00", required = true)
    private LocalDateTime checkinTime;

    @NotNull(message = "签到方式不能为空")
    @Min(value = 0, message = "签到方式值只能是0或1")
    @Max(value = 1, message = "签到方式值只能是0或1")
    @Schema(description = "签到方式：0-教练签到，1-前台签到", example = "1", required = true)
    private Integer checkinMethod;

    @Size(max = 100, message = "课程名称长度不能超过100")
    @Schema(description = "签到关联课程名称", example = "私教-胸肌训练")
    private String courseName;

    @Size(max = 50, message = "教练姓名长度不能超过50")
    @Schema(description = "授课教练姓名", example = "李教练")
    private String coachName;

    @Size(max = 500, message = "备注长度不能超过500")
    @Schema(description = "签到备注信息", example = "会员迟到10分钟")
    private String notes;
}