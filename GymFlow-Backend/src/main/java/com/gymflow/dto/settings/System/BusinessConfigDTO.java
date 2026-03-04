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

    @NotNull(message = "课程提前续约时间不能为空")
    @Min(value = 0, message = "提前续约时间不能小于0天")
    @Max(value = 365, message = "提前续约时间不能超过365天")
    @Schema(description = "课程提前续约时间（天）", example = "7", required = true)
    private Integer courseRenewalDays;

    @NotNull(message = "课程取消时间限制不能为空")
    @Min(value = 0, message = "取消时间限制不能小于0小时")
    @Max(value = 72, message = "取消时间限制不能超过72小时")
    @Schema(description = "课程取消时间限制（小时）", example = "2", required = true)
    private Integer courseCancelHours;

    @NotNull(message = "最低开课人数不能为空")
    @Min(value = 1, message = "最低开课人数不能小于1")
    @Max(value = 50, message = "最低开课人数不能超过50")
    @Schema(description = "最低开课人数（团课）", example = "5", required = true)
    private Integer minClassSize;

    @NotNull(message = "最大课程容量不能为空")
    @Min(value = 1, message = "最大课程容量不能小于1")
    @Max(value = 200, message = "最大课程容量不能超过200")
    @Schema(description = "最大课程容量（团课最多参与人数）", example = "30", required = true)
    private Integer maxClassCapacity;
}