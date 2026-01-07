package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("coach")
public class Coach implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("coach_no")
    private String coachNo;

    @TableField("real_name")
    private String realName;

    @TableField("gender")
    private Integer gender;

    @TableField("birthday")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("specialty")
    private String specialty;

    @TableField("certifications")
    private String certifications;

    @TableField("years_of_experience")
    private Integer yearsOfExperience;

    @TableField("hourly_rate")
    private BigDecimal hourlyRate;

    @TableField("commission_rate")
    private BigDecimal commissionRate;

    @TableField("status")
    private Integer status;

    @TableField("total_students")
    private Integer totalStudents;

    @TableField("total_courses")
    private Integer totalCourses;

    @TableField("total_income")
    private BigDecimal totalIncome;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private User user;
}