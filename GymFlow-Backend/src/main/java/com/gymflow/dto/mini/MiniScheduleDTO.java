package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Schema(description = "教练课表DTO")
public class MiniScheduleDTO {

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程类型：0-私教课，1-团课")
    private Integer courseType;

    @Schema(description = "课程类型描述")
    private String courseTypeDesc;

    @Schema(description = "课程日期")
    private LocalDate courseDate;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "上课地点")
    private String location;

    @Schema(description = "最大容量")
    private Integer maxCapacity;

    @Schema(description = "当前预约人数")
    private Integer currentEnrollment;

    @Schema(description = "剩余名额")
    private Integer remainingSlots;

    @Schema(description = "学员列表")
    private List<MiniCourseStudentDTO> students;
}