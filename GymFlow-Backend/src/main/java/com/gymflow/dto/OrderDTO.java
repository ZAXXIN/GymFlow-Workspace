package com.gymflow.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class OrderDTO {

    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    @NotBlank(message = "订单类型不能为空")
    private String orderType;

    private Long productId;

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    @NotNull(message = "数量不能为空")
    private Integer quantity;

    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    private BigDecimal discountAmount;

    private String paymentMethod;

    private String remark;
}