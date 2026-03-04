package com.gymflow.dto.settings.rolePermission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "权限树DTO")
public class PermissionTreeDTO {

    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "权限名称")
    private String permissionName;

    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "父权限ID")
    private Long parentId;

    @Schema(description = "所属模块")
    private String module;

    @Schema(description = "权限类型：1-菜单，2-页面，3-按钮")
    private Integer type;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "子权限列表")
    private List<PermissionTreeDTO> children;
}
