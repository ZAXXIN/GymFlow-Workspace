package com.gymflow.dto.settings.System;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "基本设置DTO")
public class BasicConfigDTO {

    @NotBlank(message = "系统名称不能为空")
    @Size(max = 100, message = "系统名称长度不能超过100")
    @Schema(description = "系统名称", example = "GymFlow健身管理系统", required = true)
    private String systemName;

    @Size(max = 500, message = "系统logo URL长度不能超过500")
    @Schema(description = "系统logo URL", example = "/logo.png")
    private String systemLogo;
}