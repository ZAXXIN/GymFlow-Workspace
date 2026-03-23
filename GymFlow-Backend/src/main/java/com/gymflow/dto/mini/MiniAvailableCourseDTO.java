package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "可预约课程DTO")
public class MiniAvailableCourseDTO {

    @Schema(description = "排课ID")
    private Long scheduleId;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程类型：0-私教课，1-团课")
    private Integer courseType;

    @Schema(description = "课程类型描述")
    private String courseTypeDesc;

    @Schema(description = "教练ID")
    private Long coachId;

    @Schema(description = "教练姓名")
    private String coachName;

    @Schema(description = "课程日期")
    private LocalDate courseDate;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "最大容量")
    private Integer maxCapacity;

    @Schema(description = "当前预约人数")
    private Integer currentEnrollment;

    @Schema(description = "剩余名额")
    private Integer remainingSlots;

    @Schema(description = "消耗课时数")
    private Integer sessionCost;

    @Schema(description = "课程描述")
    private String description;

    @Schema(description = "是否可预约")
    private Boolean canBook;

    @Schema(description = "不可预约原因")
    private String cannotBookReason;
}