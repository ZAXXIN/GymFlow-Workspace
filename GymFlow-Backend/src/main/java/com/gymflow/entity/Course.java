package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("course_type")
    private Integer courseType;

    @TableField("course_name")
    private String courseName;

    private String notice;

    private String description;

    private Integer duration;

    @TableField("session_cost")
    private Integer sessionCost;  // 预约消耗课时数

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}