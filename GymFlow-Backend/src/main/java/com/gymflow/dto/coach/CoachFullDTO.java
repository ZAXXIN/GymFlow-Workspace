package com.gymflow.dto.coach;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CoachFullDTO {

    // 基础信息
    private Long id;
    private String realName;
    private String phone;
    private String specialty;
    private List<String> certificationList;
    private Integer yearsOfExperience;
    private BigDecimal hourlyRate;
    private BigDecimal commissionRate;
    private Integer status;
    private Integer totalStudents;
    private Integer totalCourses;
    private BigDecimal totalIncome;
    private BigDecimal rating;
    private String introduction;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 扩展信息
    private List<CoachScheduleDTO> schedules;
    private List<CoachCourseDTO> courses;
}