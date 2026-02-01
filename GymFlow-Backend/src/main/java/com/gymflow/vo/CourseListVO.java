package com.gymflow.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class CourseListVO {

    private Long id;
    private Integer courseType;
    private String courseTypeDesc;
    private String courseName;
    private String description;
    private List<String> coachNames;  // 绑定教练姓名列表
    private Integer maxCapacity;
    private Integer currentEnrollment;
    private BigDecimal enrollmentRate;
    private LocalDate courseDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer duration;
    private BigDecimal price;
    private String location;
    private Integer status;
    private String statusDesc;
}