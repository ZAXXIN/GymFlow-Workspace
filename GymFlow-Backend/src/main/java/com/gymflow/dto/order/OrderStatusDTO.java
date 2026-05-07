package com.gymflow.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "订单状态更新DTO（仅用于管理员直接修改状态）")
public class OrderStatusDTO {

    @NotBlank(message = "订单状态不能为空")
    @Schema(description = "订单状态：WAIT_PAY, PAID, COMPLETED, CANCELLED, REFUNDED", required = true)
    private String status;

    @Size(max = 500, message = "备注长度不能超过500")
    @Schema(description = "备注信息")
    private String remark;
}