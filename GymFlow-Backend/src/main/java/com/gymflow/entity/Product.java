package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("product_name")
    private String productName;

    @TableField("product_type")
    private Integer productType;

    private String description;

    private String images;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("current_price")
    private BigDecimal currentPrice;

    @TableField("stock_quantity")
    private Integer stockQuantity;

    @TableField("sales_volume")
    private Integer salesVolume;

    private String specifications;

    private Integer status;

    @TableField("validity_days")
    private Integer validityDays;

    @TableField("total_sessions")
    private Integer totalSessions;

    @TableField("membership_benefits")
    private String membershipBenefits;  // JSON格式

    @TableField("max_purchase_quantity")
    private Integer maxPurchaseQuantity;

    @TableField("refund_policy")
    private String refundPolicy;

    @TableField("usage_rules")
    private String usageRules;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}