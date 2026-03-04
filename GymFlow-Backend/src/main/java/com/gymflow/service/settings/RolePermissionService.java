package com.gymflow.service.settings;

import com.gymflow.dto.settings.rolePermission.PermissionTreeDTO;
import com.gymflow.dto.settings.rolePermission.RoleDTO;
import com.gymflow.dto.settings.rolePermission.RolePermissionDetailDTO;

import java.util.List;

public interface RolePermissionService {

    /**
     * 获取角色列表
     */
    List<RoleDTO> getRoleList();

    /**
     * 获取角色详情
     */
    RoleDTO getRoleDetail(Long roleId);

    /**
     * 新增角色
     */
    Long addRole(RoleDTO roleDTO);

    /**
     * 更新角色
     */
    void updateRole(Long roleId, RoleDTO roleDTO);

    /**
     * 删除角色
     */
    void deleteRole(Long roleId);

    /**
     * 获取权限树
     */
    List<PermissionTreeDTO> getPermissionTree();

    /**
     * 获取角色的权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);

    /**
     * 获取角色的权限详情列表
     */
    List<RolePermissionDetailDTO> getRolePermissionDetails(Long roleId);

    /**
     * 更新角色的权限
     */
    void updateRolePermissions(Long roleId, List<Long> permissionIds);
}