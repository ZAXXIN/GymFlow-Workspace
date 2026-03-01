package com.gymflow.dto.checkin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "签到报表DTO")
public class CheckInReportDTO {

    @Schema(description = "总签到数")
    private Integer totalCheckIns;

    @Schema(description = "课程签到数")
    private Integer courseCheckIns;

    @Schema(description = "自由训练签到数")
    private Integer freeCheckIns;

    @Schema(description = "教练签到数")
    private Integer coachCheckIns;

    @Schema(description = "前台签到数")
    private Integer frontDeskCheckIns;

    @Schema(description = "签到会员数（去重）")
    private Integer uniqueMembers;

    @Schema(description = "人均签到次数")
    private Double avgCheckInsPerMember;

    @Schema(description = "每日签到趋势")
    private Map<String, Integer> dailyTrend;

    @Schema(description = "会员活跃度排名")
    private List<Map<String, Object>> memberRanking;

    @Schema(description = "报表生成时间")
    private String reportTime;
}