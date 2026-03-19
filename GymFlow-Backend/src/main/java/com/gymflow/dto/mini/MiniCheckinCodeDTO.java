package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "小程序签到码DTO")
public class MiniCheckinCodeDTO {

    @Schema(description = "预约ID")
    private Long bookingId;

    @Schema(description = "6位数字码")
    private String digitalCode;

    @Schema(description = "二维码图片URL")
    private String qrCodeUrl;

    @Schema(description = "状态：0-未使用，1-已使用，2-已过期")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "有效开始时间")
    private LocalDateTime validStartTime;

    @Schema(description = "有效结束时间")
    private LocalDateTime validEndTime;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程开始时间")
    private LocalDateTime courseStartTime;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "教练姓名")
    private String coachName;

    public String getStatusDesc() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "未使用";
            case 1: return "已使用";
            case 2: return "已过期";
            default: return "未知";
        }
    }
}