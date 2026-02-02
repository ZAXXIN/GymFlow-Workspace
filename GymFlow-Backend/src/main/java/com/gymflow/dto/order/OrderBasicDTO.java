package com.gymflow.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "订单基础信息DTO")
public class OrderBasicDTO {

    @NotNull(message = "会员ID不能为空")
    @Positive(message = "会员ID必须为正数")
    @Schema(description = "会员ID", example = "1001", required = true)
    private Long memberId;

    @NotNull(message = "订单类型不能为空")
    @Min(value = 0, message = "订单类型值只能是0-3")
    @Max(value = 3, message = "订单类型值只能是0-3")
    @Schema(description = "订单类型：0-会籍卡，1-私教课，2-团课，3-相关产品", example = "1", required = true)
    private Integer orderType;

    @DecimalMin(value = "0.01", message = "总金额不能小于0.01")
    @Schema(description = "总金额", example = "4200.00", required = true)
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.00", message = "实付金额不能小于0")
    @Schema(description = "实付金额", example = "4200.00")
    private BigDecimal actualAmount;

    @Size(max = 20, message = "支付方式长度不能超过20")
    @Schema(description = "支付方式", example = "微信支付")
    private String paymentMethod;

    @NotNull(message = "订单项不能为空")
    @Size(min = 1, message = "至少需要一个订单项")
    @Schema(description = "订单项列表", required = true)
    private List<OrderItemDTO> orderItems;

    @Size(max = 500, message = "备注长度不能超过500")
    @Schema(description = "备注信息", example = "客户指定张教练")
    private String remark;
}