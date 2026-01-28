package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coach")
public class Coach {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    @TableField("real_name")
    private String realName;

    private String phone;

    private String specialty;

    private String certifications;

    @TableField("years_of_experience")
    private Integer yearsOfExperience;

    @TableField("hourly_rate")
    private BigDecimal hourlyRate;

    @TableField("commission_rate")
    private BigDecimal commissionRate;

    private Integer status;

    @TableField("total_students")
    private Integer totalStudents;

    @TableField("total_courses")
    private Integer totalCourses;

    @TableField("total_income")
    private BigDecimal totalIncome;

    private BigDecimal rating;

    private String introduction;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}