package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@TableName("course_schedule")
public class CourseSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("course_id")
    private Long courseId;

    @TableField("coach_id")
    private Long coachId;

    @TableField("schedule_date")
    private LocalDate scheduleDate;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    @TableField("max_capacity")
    private Integer maxCapacity;

    @TableField("current_enrollment")
    private Integer currentEnrollment;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ========== 非数据库字段 ==========
    @TableField(exist = false)
    private List<CourseBooking> bookings;
}