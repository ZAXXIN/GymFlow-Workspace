package com.gymflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CoachVO {

    private Long id;

    private Long userId;

    private String coachNo;

    private String username;

    private String phone;

    private String email;

    private String avatar;

    private String realName;

    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private String specialty;

    private String certifications;

    private Integer yearsOfExperience;

    private BigDecimal hourlyRate;

    private BigDecimal commissionRate;

    private Integer status;

    private Integer totalStudents;

    private Integer totalCourses;

    private BigDecimal totalIncome;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 额外统计字段
    private Integer currentStudents;      // 当前学员数
    private Integer currentCourses;       // 当前课程数
    private BigDecimal monthIncome;       // 本月收入
}