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

    private String phone;

    private String password;

    @TableField("id_card")
    private String idCard;

    @TableField("real_name")
    private String realName;

    private Integer gender;

    private LocalDate birthday;

    private BigDecimal height;

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

    private String address;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}