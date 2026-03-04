package com.gymflow.dto.settings.webUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@Schema(description = "用户查询DTO")
public class WebUserQueryDTO {

    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码（默认1）", example = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页大小不能小于1")
    @Max(value = 100, message = "页大小不能超过100")
    @Schema(description = "页大小（默认10）", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "用户名（模糊查询）")
    private String username;

    @Schema(description = "真实姓名（模糊查询）")
    private String realName;

    @Schema(description = "角色：0-老板，1-前台")
    private Integer role;

    @Schema(description = "状态：0-禁用，1-正常")
    private Integer status;
}