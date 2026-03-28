package com.gymflow.dto.settings.System;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;
import java.time.LocalTime;

@Data
@Schema(description = "业务设置DTO")
public class BusinessConfigDTO {

    @NotNull(message = "营业开始时间不能为空")
    @DateTimeFormat(pattern = "HH:mm:ss")
    @Schema(description = "营业开始时间", example = "08:00:00", required = true)
    private LocalTime businessStartTime;

    @NotNull(message = "营业结束时间不能为空")
    @DateTimeFormat(pattern = "HH:mm:ss")
    @Schema(description = "营业结束时间", example = "22:00:00", required = true)
    private LocalTime businessEndTime;

    @NotNull(message = "课程提前预约时间不能为空")
    @Min(value = 0, message = "提前预约时间不能小于0小时")
    @Max(value = 12, message = "提前预约时间不能超过12小时")
    @Schema(description = "课程提前预约时间（小时）", example = "7", required = true)
    private Integer courseAdvanceBookingHours;

    @NotNull(message = "课程取消时间限制不能为空")
    @Min(value = 0, message = "取消时间限制不能小于0小时")
    @Max(value = 4, message = "取消时间限制不能超过4小时")
    @Schema(description = "课程取消时间限制（小时）", example = "2", required = true)
    private Integer courseCancelHours;

    @NotNull(message = "最低开课人数不能为空")
    @Min(value = 1, message = "最低开课人数不能小于1")
    @Max(value = 20, message = "最低开课人数不能超过50")
    @Schema(description = "最低开课人数（团课）", example = "5", required = true)
    private Integer minClassSize;

    @NotNull(message = "最大课程容量不能为空")
    @Min(value = 1, message = "最大课程容量不能小于1")
    @Max(value = 100, message = "最大课程容量不能超过200")
    @Schema(description = "最大课程容量（团课最多参与人数）", example = "30", required = true)
    private Integer maxClassCapacity;

    @NotNull(message = "签到开始时间不能为空")
    @Min(value = 0, message = "签到开始时间不能小于0分钟")
    @Max(value = 60, message = "签到开始时间不能超过120分钟")
    @Schema(description = "课程开始前多少分钟可签到", example = "30", required = true)
    private Integer checkinStartMinutes;

    @NotNull(message = "签到截止时间不能为空")
    @Min(value = 0, message = "签到截止时间不能小于0分钟")
    @Max(value = 60, message = "签到截止时间不能超过60分钟")
    @Schema(description = "课程开始后多少分钟截止签到（0表示开始后不可签到）", example = "0", required = true)
    private Integer checkinEndMinutes;

    @NotNull(message = "自动完成时间不能为空")
    @Min(value = 0, message = "自动完成时间不能小于0小时")
    @Max(value = 24, message = "自动完成时间不能超过24小时")
    @Schema(description = "课程结束后多少小时自动变更为已完成", example = "1", required = true)
    private Integer autoCompleteHours;
}