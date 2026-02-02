package com.gymflow.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Schema(description = "订单查询DTO")
public class OrderQueryDTO {

    // 分页参数
    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码（默认1）", example = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页大小不能小于1")
    @Max(value = 100, message = "页大小不能超过100")
    @Schema(description = "页大小（默认10）", example = "10")
    private Integer pageSize = 10;

    // 查询条件
    @Size(max = 32, message = "订单编号长度不能超过32")
    @Schema(description = "订单编号（模糊查询）", example = "ORDER2024")
    private String orderNo;

    @Schema(description = "会员ID", example = "1001")
    private Long memberId;

    @Schema(description = "订单类型：0-会籍卡，1-私教课，2-团课，3-相关产品")
    private Integer orderType;

    @Schema(description = "支付状态：0-待支付，1-已支付")
    private Integer paymentStatus;

    @Size(max = 20, message = "订单状态长度不能超过20")
    @Schema(description = "订单状态", example = "COMPLETED")
    private String orderStatus;

    @Schema(description = "创建开始日期", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "创建结束日期", example = "2024-01-31")
    private LocalDate endDate;

    @Schema(description = "是否包含删除的订单", example = "false")
    private Boolean includeDeleted = false;
}