package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "财务统计数据DTO")
public class MiniFinanceStatsDTO {

    @Schema(description = "统计周期：day-日，month-月，year-年")
    private String period;

    @Schema(description = "本月课时数")
    private Integer monthCourseHours;

    @Schema(description = "本月服务会员数")
    private Integer monthMemberCount;

    @Schema(description = "本月收入")
    private BigDecimal monthIncome;

    @Schema(description = "总课时数")
    private Integer totalCourseHours;

    @Schema(description = "课时数趋势")
    private List<ChartDataDTO> courseTrend;

    @Schema(description = "收入趋势")
    private List<ChartDataDTO> incomeTrend;

    @Schema(description = "会员数趋势")
    private List<ChartDataDTO> memberTrend;
}