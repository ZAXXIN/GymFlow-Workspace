package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("course_type")
    private Integer courseType;

    @TableField("course_name")
    private String courseName;

    private String description;

    @TableField("coach_id")
    private Long coachId;

    @TableField("max_capacity")
    private Integer maxCapacity;

    @TableField("current_enrollment")
    private Integer currentEnrollment;

    @TableField("course_date")
    private LocalDate courseDate;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    private Integer duration;

    private BigDecimal price;

    private String location;

    private Integer status;

    @TableField("sign_code")
    private String signCode;

    @TableField("sign_code_expire_time")
    private LocalDateTime signCodeExpireTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}