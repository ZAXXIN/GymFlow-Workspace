package com.gymflow.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "快速统计数据DTO")
public class QuickStatsDTO {

    @Schema(description = "待处理订单数")
    private Integer pendingOrders;

    @Schema(description = "待签到课程数")
    private Integer pendingCheckIns;

    @Schema(description = "即将过期会员数")
    private Integer expiringMembers;

    @Schema(description = "库存预警商品数")
    private Integer lowStockProducts;
}