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

    private Long userId;

    @TableField("member_no")
    private String memberNo;

    @TableField("id_card")
    private String idCard;

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