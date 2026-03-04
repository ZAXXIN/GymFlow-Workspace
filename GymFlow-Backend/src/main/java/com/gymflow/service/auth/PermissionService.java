package com.gymflow.service.auth;

import com.gymflow.dto.permission.PermissionResponseDTO;

public interface PermissionService {

    /**
     * 获取用户权限和菜单
     */
    PermissionResponseDTO getUserPermissions(Long userId);
}