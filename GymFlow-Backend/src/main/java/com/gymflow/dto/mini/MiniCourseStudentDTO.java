package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "课程学员DTO")
public class MiniCourseStudentDTO {

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "会员手机号")
    private String memberPhone;

    @Schema(description = "会员编号")
    private String memberNo;

    @Schema(description = "预约ID")
    private Long bookingId;

    @Schema(description = "预约状态：0-待上课，1-已签到，2-已完成，3-已取消，4-已过期")
    private Integer bookingStatus;

    @Schema(description = "预约状态描述")
    private String bookingStatusDesc;

    @Schema(description = "预约时间")
    private LocalDateTime bookingTime;

    @Schema(description = "签到时间")
    private LocalDateTime checkinTime;

    @Schema(description = "剩余课时数")
    private Integer remainingSessions;

    @Schema(description = "会员卡有效期")
    private String membershipEndDate;

    public String getBookingStatusDesc() {
        if (bookingStatus == null) return "未知";
        switch (bookingStatus) {
            case 0: return "待上课";
            case 1: return "已签到";
            case 2: return "已完成";
            case 3: return "已取消";
            case 4: return "已过期";
            default: return "未知";
        }
    }
}