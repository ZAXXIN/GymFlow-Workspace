package com.gymflow.dto.settings.System;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统配置响应DTO")
public class SystemConfigResponseDTO {

    @Schema(description = "基本设置")
    private BasicConfigDTO basic;

    @Schema(description = "业务设置")
    private BusinessConfigDTO business;

    @Schema(description = "最后更新时间")
    private String updateTime;
}