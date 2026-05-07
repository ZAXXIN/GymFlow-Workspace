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

    @TableField("payment_time")
    private LocalDateTime paymentTime;

    private String remark;

    /**
     * 订单状态：WAIT_PAY, PAID, COMPLETED, CANCELLED, REFUNDED
     */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}