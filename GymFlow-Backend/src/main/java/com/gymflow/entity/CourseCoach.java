package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("course_coach")
public class CourseCoach {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long coachId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}