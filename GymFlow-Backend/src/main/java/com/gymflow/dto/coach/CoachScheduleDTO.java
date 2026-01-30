package com.gymflow.dto.coach;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "教练排班DTO")
public class CoachScheduleDTO {

    @NotNull(message = "排班日期不能为空")
    @Schema(description = "排班日期", example = "2026-01-30", required = true)
    private LocalDate scheduleDate;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", example = "09:00:00", required = true)
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间", example = "12:00:00", required = true)
    private LocalTime endTime;

    @NotNull(message = "排班类型不能为空")
    @Schema(description = "排班类型：0-私教课，1-团课", example = "1", required = true)
    private Integer scheduleType;

    @Size(max = 200, message = "备注长度不能超过200")
    @Schema(description = "备注", example = "瑜伽入门团课")
    private String notes;

    // 仅用于响应
    @Schema(description = "排班类型描述")
    private String scheduleTypeDesc;

    @Schema(description = "状态")
    private String status;
}