package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long productId;

    @TableField("product_name")
    private String productName;

    @TableField("product_type")
    private Integer productType;

    private Integer quantity;

    @TableField("unit_price")
    private BigDecimal unitPrice;

    @TableField("total_price")
    private BigDecimal totalPrice;

    @TableField("validity_start_date")
    private LocalDate validityStartDate;

    @TableField("validity_end_date")
    private LocalDate validityEndDate;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
