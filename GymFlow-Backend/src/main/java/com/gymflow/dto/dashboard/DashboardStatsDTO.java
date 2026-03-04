package com.gymflow.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "仪表盘统计数据DTO")
public class DashboardStatsDTO {

    @Schema(description = "总会员数")
    private Integer totalMembers;

    @Schema(description = "总教练数")
    private Integer totalCoaches;

    @Schema(description = "总课程数")
    private Integer totalCourses;

    @Schema(description = "今日营收")
    private BigDecimal todayRevenue;

    @Schema(description = "昨日营收")
    private BigDecimal yesterdayRevenue;

    @Schema(description = "今日签到数")
    private Integer todayCheckIns;

    @Schema(description = "昨日签到数")
    private Integer yesterdayCheckIns;

    @Schema(description = "本月营收")
    private BigDecimal monthRevenue;

    @Schema(description = "上月营收")
    private BigDecimal lastMonthRevenue;

    @Schema(description = "本月新增会员")
    private Integer monthNewMembers;

    @Schema(description = "上月新增会员")
    private Integer lastMonthNewMembers;

    @Schema(description = "本月签到数")
    private Integer monthCheckIns;
}