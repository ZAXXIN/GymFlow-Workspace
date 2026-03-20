package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "创建预约DTO")
public class MiniCreateBookingDTO {

    @NotNull(message = "排课ID不能为空")
    @Schema(description = "排课ID", required = true)
    private Long scheduleId;  // 改为 scheduleId，移除其他字段

    @Schema(description = "备注")
    private String remark;
}