package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("product_detail")
public class ProductDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    @TableField("membership_type")
    private Integer membershipType;

    @TableField("coach_id")
    private Long coachId;

    @TableField("course_duration")
    private Integer courseDuration;

    @TableField("total_sessions")
    private Integer totalSessions;

    @TableField("available_sessions")
    private Integer availableSessions;

    @TableField("course_level")
    private String courseLevel;

    @TableField("membership_benefits")
    private String membershipBenefits;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
