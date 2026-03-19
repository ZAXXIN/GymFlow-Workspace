package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "未读消息数量DTO")
public class MiniUnreadCountDTO {

    @Schema(description = "总未读数量")
    private Integer total;

    @Schema(description = "预约相关未读数量")
    private Integer booking;

    @Schema(description = "签到相关未读数量")
    private Integer checkin;

    @Schema(description = "系统通知未读数量")
    private Integer system;
}