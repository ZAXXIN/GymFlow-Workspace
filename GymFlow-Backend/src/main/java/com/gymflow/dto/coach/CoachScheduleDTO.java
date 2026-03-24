package com.gymflow.dto.coach;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "教练排班DTO")
public class CoachScheduleDTO {

    @Schema(description = "排班ID（对应课程排课ID）")
    private Long id;

    @Schema(description = "教练ID")
    private Long coachId;

    @Schema(description = "排班日期")
    private LocalDate scheduleDate;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "排班类型：0-私教课，1-团课")
    private Integer scheduleType;

    @Schema(description = "排班类型描述")
    private String scheduleTypeDesc;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "最大人数")
    private Integer maxCapacity;

    @Schema(description = "当前报名人数")
    private Integer currentEnrollment;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String notes;

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) return "未知";
        switch (status) {
            case "SCHEDULED": return "已排班";
            case "CANCELLED": return "已取消";
            case "COMPLETED": return "已完成";
            default: return status;
        }
    }
}