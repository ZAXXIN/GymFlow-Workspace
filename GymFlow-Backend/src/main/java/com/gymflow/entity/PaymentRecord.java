package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment_record")
public class PaymentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    @TableField("payment_no")
    private String paymentNo;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("payment_amount")
    private BigDecimal paymentAmount;

    @TableField("payment_status")
    private Integer paymentStatus;

    @TableField("transaction_id")
    private String transactionId;

    @TableField("payer_account")
    private String payerAccount;

    @TableField("payee_account")
    private String payeeAccount;

    @TableField("payment_time")
    private LocalDateTime paymentTime;

    @TableField("refund_amount")
    private BigDecimal refundAmount;

    @TableField("refund_time")
    private LocalDateTime refundTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}