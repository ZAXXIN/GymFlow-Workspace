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

    @TableField("validity_days")
    private Integer validityDays; // 新增：有效期天数

    @TableField("default_total_sessions")
    private Integer defaultTotalSessions; // 新增：默认总课时数

    @TableField("max_purchase_quantity")
    private Integer maxPurchaseQuantity; // 新增：最大购买数量

    @TableField("refund_policy")
    private String refundPolicy; // 新增：退款政策

    @TableField("usage_rules")
    private String usageRules; // 新增：使用规则

    @TableField("coach_ids")
    private String coachIds; // 新增：适用教练ID列表（JSON格式）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}