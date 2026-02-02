package com.gymflow.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "订单状态更新DTO")
public class OrderStatusDTO {

    @NotBlank(message = "订单状态不能为空")
    @Size(max = 20, message = "订单状态长度不能超过20")
    @Schema(description = "订单状态", example = "PROCESSING", required = true)
    private String orderStatus;

    @Size(max = 500, message = "备注长度不能超过500")
    @Schema(description = "备注信息", example = "已联系客户确认订单")
    private String remark;
}