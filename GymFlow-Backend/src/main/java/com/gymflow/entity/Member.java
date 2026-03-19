package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("member")
public class Member {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("member_no")
    private String memberNo;

    @TableField("phone")
    private String phone;

    @TableField("password")
    private String password;

    @TableField("real_name")
    private String realName;

    @TableField("gender")
    private Integer gender;

    @TableField("birthday")
    private LocalDate birthday;

    @TableField("membership_start_date")
    private LocalDate membershipStartDate;

    @TableField("membership_end_date")
    private LocalDate membershipEndDate;

    @TableField("total_checkins")
    private Integer totalCheckins;

    @TableField("total_course_hours")
    private Integer totalCourseHours;

    @TableField("total_spent")
    private BigDecimal totalSpent;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}