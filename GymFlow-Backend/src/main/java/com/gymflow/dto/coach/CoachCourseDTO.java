package com.gymflow.dto.coach;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Schema(description = "教练课程DTO")
public class CoachCourseDTO {

    @Schema(description = "课程ID")
    private Long id;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程类型：0-私教课，1-团课")
    private Integer courseType;

    @Schema(description = "课程描述")
    private String description;

    @Schema(description = "课程日期")
    private LocalDate courseDate;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "时长（分钟）")
    private Integer duration;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "上课地点")
    private String location;

    @Schema(description = "最大容量")
    private Integer maxCapacity;

    @Schema(description = "当前报名人数")
    private Integer currentEnrollment;

    @Schema(description = "报名率（%）")
    private Double enrollmentRate;

    @Schema(description = "课程状态：0-禁用，1-正常")
    private Integer status;
}