package com.gymflow.controller.settings;

import com.gymflow.common.Result;
import com.gymflow.common.annotation.PreAuthorize;
import com.gymflow.dto.settings.rolePermission.PermissionTreeDTO;
import com.gymflow.dto.settings.rolePermission.RoleDTO;
import com.gymflow.dto.settings.rolePermission.RolePermissionDetailDTO;
import com.gymflow.dto.settings.rolePermission.RolePermissionUpdateDTO;
import com.gymflow.service.settings.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/settings")
@Tag(name = "角色权限管理", description = "角色和权限管理相关接口")
@Validated
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @GetMapping("/roles")
    @Operation(summary = "获取角色列表")
    @PreAuthorize("settings:role:view")
    public Result<List<RoleDTO>> getRoleList() {
        List<RoleDTO> roleList = rolePermissionService.getRoleList();
        return Result.success("获取成功", roleList);
    }

    @GetMapping("/roles/{roleId}")
    @Operation(summary = "获取角色详情")
    @PreAuthorize("settings:role:view")
    public Result<RoleDTO> getRoleDetail(@PathVariable Long roleId) {
        RoleDTO role = rolePermissionService.getRoleDetail(roleId);
        return Result.success("获取成功", role);
    }

    @PostMapping("/roles")
    @Operation(summary = "新增角色")
    @PreAuthorize("settings:role:add")
    public Result<Long> addRole(@Valid @RequestBody RoleDTO roleDTO) {
        Long roleId = rolePermissionService.addRole(roleDTO);
        return Result.success("添加成功", roleId);
    }

    @PutMapping("/roles/{roleId}")
    @Operation(summary = "更新角色")
    @PreAuthorize("settings:role:edit")
    public Result<Void> updateRole(@PathVariable Long roleId, @Valid @RequestBody RoleDTO roleDTO) {
        rolePermissionService.updateRole(roleId, roleDTO);
        return Result.success("更新成功");
    }

    @DeleteMapping("/roles/{roleId}")
    @Operation(summary = "删除角色")
    @PreAuthorize("settings:role:delete")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        rolePermissionService.deleteRole(roleId);
        return Result.success("删除成功");
    }

    @GetMapping("/permissions/tree")
    @Operation(summary = "获取权限树")
    @PreAuthorize("settings:role:view")
    public Result<List<PermissionTreeDTO>> getPermissionTree() {
        List<PermissionTreeDTO> permissionTree = rolePermissionService.getPermissionTree();
        return Result.success("获取成功", permissionTree);
    }

    @GetMapping("/roles/{roleId}/permissions")
    @Operation(summary = "获取角色的权限详情列表")
    @PreAuthorize("settings:role:view")
    public Result<List<RolePermissionDetailDTO>> getRolePermissions(@PathVariable Long roleId) {
        List<RolePermissionDetailDTO> permissionDetails = rolePermissionService.getRolePermissionDetails(roleId);
        return Result.success("获取成功", permissionDetails);
    }

    @GetMapping("/roles/{roleId}/permission-ids")
    @Operation(summary = "获取角色的权限ID列表")
    @PreAuthorize("settings:role:view")
    public Result<List<Long>> getRolePermissionIds(@PathVariable Long roleId) {
        List<Long> permissionIds = rolePermissionService.getRolePermissionIds(roleId);
        return Result.success("获取成功", permissionIds);
    }

    @PutMapping("/roles/{roleId}/permissions")
    @Operation(summary = "更新角色的权限")
    @PreAuthorize("settings:role:edit")
    public Result<Void> updateRolePermissions(
            @PathVariable Long roleId,
            @Valid @RequestBody RolePermissionUpdateDTO updateDTO) {
        rolePermissionService.updateRolePermissions(roleId, updateDTO.getPermissionIds());
        return Result.success("更新成功");
    }
}