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

    @TableField("category_id")
    private Long categoryId;

    private String description;

    private String images;

    @TableField("original_price")
    private BigDecimal originalPrice;

    @TableField("current_price")
    private BigDecimal currentPrice;

    @TableField("cost_price")
    private BigDecimal costPrice;

    @TableField("stock_quantity")
    private Integer stockQuantity;

    @TableField("sales_volume")
    private Integer salesVolume;

    private String unit;

    @TableField("validity_days")
    private Integer validityDays;

    private String specifications;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
