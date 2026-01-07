package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gymflow.enums.CourseStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("course")
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("course_type")
    private String courseType;

    @TableField("course_name")
    private String courseName;

    @TableField("description")
    private String description;

    @TableField("coach_id")
    private Long coachId;

    @TableField("coach_name")
    private String coachName;

    @TableField("max_capacity")
    private Integer maxCapacity;

    @TableField("current_enrollment")
    private Integer currentEnrollment;

    @TableField("course_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate courseDate;

    @TableField("start_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @TableField("end_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @TableField("duration")
    private Integer duration;

    @TableField("price")
    private BigDecimal price;

    @TableField("location")
    private String location;

    @TableField("status")
    private CourseStatus status;

    @TableField("sign_code")
    private String signCode;

    @TableField("sign_code_expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signCodeExpireTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Coach coach;

    @TableField(exist = false)
    private Integer bookedCount;
}