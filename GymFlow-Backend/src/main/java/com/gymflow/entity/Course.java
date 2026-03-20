package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
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

    private String description;

    private Integer duration;

    private BigDecimal price;

    private Integer status;

    @TableField("notice")  // 新增：课程须知
    private String notice;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}