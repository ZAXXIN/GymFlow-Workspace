package com.gymflow.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "营收数据点DTO")
public class RevenueDataPointDTO {

    @Schema(description = "日期")
    private String date;

    @Schema(description = "营收金额")
    private BigDecimal revenue;

    @Schema(description = "显示标签")
    private String label;
}