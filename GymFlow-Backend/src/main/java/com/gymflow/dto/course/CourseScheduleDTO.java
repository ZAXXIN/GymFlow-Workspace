package com.gymflow.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "课程排课DTO")
public class CourseScheduleDTO {

    @NotNull(message = "课程ID不能为空")
    @Schema(description = "课程ID", required = true)
    private Long courseId;

//    @NotNull(message = "课程名称不能为空")
//    @Schema(description = "课程名称", required = true)
//    private String courseName;

    @NotNull(message = "教练ID不能为空")
    @Schema(description = "教练ID", required = true)
    private Long coachId;

    @NotNull(message = "课程日期不能为空")
    @Schema(description = "课程日期", required = true)
    private LocalDate scheduleDate;

    @NotNull(message = "开始时间不能为空")
    @Schema(description = "开始时间", required = true)
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Schema(description = "结束时间", required = true)
    private LocalTime endTime;

    @Schema(description = "最大容量")
    private Integer maxCapacity;

    @Schema(description = "备注")
    private String notes;
}