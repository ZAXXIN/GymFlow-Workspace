package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.gymflow.enums.MemberStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("member")
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("member_no")
    private String memberNo;

    @TableField("real_name")
    private String realName;

    @TableField("gender")
    private Integer gender;

    @TableField("birthday")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @TableField("height")
    private BigDecimal height;

    @TableField("weight")
    private BigDecimal weight;

    @TableField("membership_type")
    private String membershipType;

    @TableField("membership_start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate membershipStartDate;

    @TableField("membership_end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate membershipEndDate;

    @TableField("personal_coach_id")
    private Long personalCoachId;

    @TableField("total_checkins")
    private Integer totalCheckins;

    @TableField("total_course_hours")
    private Integer totalCourseHours;

    @TableField("status")
    private MemberStatus status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private String coachName;
}