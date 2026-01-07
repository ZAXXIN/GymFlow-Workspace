package com.gymflow.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CourseDTO {

    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    @NotBlank(message = "课程类型不能为空")
    private String courseType;

    private String description;

    @NotNull(message = "教练不能为空")
    private Long coachId;

    private Integer maxCapacity;

    @NotNull(message = "课程日期不能为空")
    private LocalDate courseDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    private Integer duration;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private String location;
}