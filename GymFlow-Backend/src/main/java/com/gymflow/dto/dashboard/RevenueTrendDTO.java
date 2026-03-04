package com.gymflow.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "营收趋势DTO")
public class RevenueTrendDTO {

    @Schema(description = "统计周期")
    private String period;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "分类标签")
    private List<String> categories;

    @Schema(description = "营收数值")
    private List<BigDecimal> values;

    @Schema(description = "详细数据点")
    private List<RevenueDataPointDTO> dataPoints;
}