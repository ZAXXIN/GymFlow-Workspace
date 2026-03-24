package com.gymflow.dto.settings.webUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户完整信息DTO")
public class WebUserDetailDTO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "角色：0-老板，1-前台")
    private Integer role;

    @Schema(description = "角色描述")
    private String roleDesc;

    @Schema(description = "状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
