package com.gymflow.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CoachListVO {

    private Long id;
    private String realName;
    private String phone;
    private String specialty;
    private List<String> certifications;
    private Integer yearsOfExperience;
    private BigDecimal hourlyRate;
    private Integer status;
    private String statusDesc;
    private Integer totalStudents;
    private Integer totalCourses;
    private BigDecimal totalIncome;
    private BigDecimal rating;
    private LocalDateTime createTime;
}