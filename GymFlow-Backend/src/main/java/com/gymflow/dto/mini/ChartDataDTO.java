package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "图表数据点DTO")
public class ChartDataDTO {

    @Schema(description = "日期/周期")
    private String date;

    @Schema(description = "数值")
    private BigDecimal value;

    @Schema(description = "显示标签")
    private String label;
}