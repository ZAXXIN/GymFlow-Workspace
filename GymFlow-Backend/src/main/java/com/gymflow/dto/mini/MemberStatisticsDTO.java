package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "会员统计数据")
public class MemberStatisticsDTO {

    @Schema(description = "签到统计")
    private CheckinStatistics checkinStats;

    @Schema(description = "课时统计")
    private SessionStatistics sessionStats;

    @Data
    @Schema(description = "签到统计")
    public static class CheckinStatistics {
        @Schema(description = "总签到次数")
        private Integer totalCheckins;

        @Schema(description = "最近签到记录")
        private List<CheckinRecordDTO> recentCheckins;
    }

    @Data
    @Schema(description = "签到记录")
    public static class CheckinRecordDTO {
        @Schema(description = "签到日期")
        private LocalDateTime checkinTime;

        @Schema(description = "签到方式：0-教练，1-前台")
        private Integer checkinMethod;

        @Schema(description = "签到方式描述")
        private String checkinMethodDesc;
    }

    @Data
    @Schema(description = "课时统计")
    public static class SessionStatistics {
        @Schema(description = "总消耗课时")
        private Integer totalConsumed;

        @Schema(description = "总归还课时")
        private Integer totalReturned;

        @Schema(description = "课时明细记录")
        private List<SessionRecordDTO> details;
    }

    @Data
    @Schema(description = "课时明细记录")
    public static class SessionRecordDTO {
        @Schema(description = "类型：CONSUME-消耗，RETURN-归还")
        private String type;

        @Schema(description = "时间")
        private LocalDateTime time;

        @Schema(description = "课程名称")
        private String courseName;

        @Schema(description = "课时数")
        private Integer sessions;

        @Schema(description = "预约ID")
        private Long bookingId;

        @Schema(description = "原因（仅归还时）")
        private String reason;
    }
}