package com.gymflow.dto.settings.rolePermission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色权限详情DTO")
public class RolePermissionDetailDTO {

    @Schema(description = "权限ID")
    private Long permissionId;

    @Schema(description = "权限名称")
    private String permissionName;

    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "权限类型：1-菜单，2-页面，3-按钮")
    private Integer type;

    @Schema(description = "所属模块")
    private String module;
}
