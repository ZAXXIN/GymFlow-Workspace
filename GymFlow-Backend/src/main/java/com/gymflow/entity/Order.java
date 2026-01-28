package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    private Long memberId;

    @TableField("order_type")
    private Integer orderType;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("actual_amount")
    private BigDecimal actualAmount;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("payment_status")
    private Integer paymentStatus;

    @TableField("payment_time")
    private LocalDateTime paymentTime;

    @TableField("order_status")
    private String orderStatus;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}