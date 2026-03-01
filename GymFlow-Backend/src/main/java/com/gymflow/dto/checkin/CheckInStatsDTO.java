package com.gymflow.dto.checkin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "签到统计DTO")
public class CheckInStatsDTO {

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

    @Schema(description = "签到率（%）")
    private Double checkInRate;

    @Schema(description = "统计日期")
    private String statDate;
}