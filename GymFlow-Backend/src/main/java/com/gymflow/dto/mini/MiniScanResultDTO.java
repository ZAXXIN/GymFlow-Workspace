package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "扫码核销结果DTO")
public class MiniScanResultDTO {

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "结果消息")
    private String message;

    @Schema(description = "预约ID")
    private Long bookingId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "会员手机号")
    private String memberPhone;

    @Schema(description = "预约时间")
    private String bookingTime;

    @Schema(description = "签到时间")
    private String checkinTime;
}