package com.gymflow.dto.permission;

import com.gymflow.entity.auth.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "权限响应DTO")
public class PermissionResponseDTO {

    @Schema(description = "权限编码列表")
    private List<String> permissions;

    @Schema(description = "菜单列表")
    private List<Permission> menus;
}