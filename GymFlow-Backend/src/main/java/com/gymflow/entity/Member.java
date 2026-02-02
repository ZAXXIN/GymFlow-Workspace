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
    private Long id;  // 主键，作为唯一标识

    @TableField("member_no")
    private String memberNo;

    @TableField("phone")
    private String phone;

    @TableField("password")
    private String password;

    @TableField("id_card")
    private String idCard;

    @TableField("real_name")
    private String realName;

    @TableField("gender")
    private Integer gender;

    @TableField("birthday")
    private LocalDate birthday;

    @TableField("height")
    private BigDecimal height;

    @TableField("weight")
    private BigDecimal weight;

    @TableField("membership_start_date")
    private LocalDate membershipStartDate;

    @TableField("membership_end_date")
    private LocalDate membershipEndDate;

    @TableField("personal_coach_id")
    private Long personalCoachId;

    @TableField("total_checkins")
    private Integer totalCheckins;

    @TableField("total_course_hours")
    private Integer totalCourseHours;

    @TableField("total_spent")
    private BigDecimal totalSpent;

    @TableField("address")
    private String address;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}