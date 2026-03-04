package com.gymflow.service.impl.auth;

import com.gymflow.dto.permission.PermissionResponseDTO;
import com.gymflow.entity.auth.Permission;
import com.gymflow.entity.auth.Role;
import com.gymflow.entity.settings.WebUser;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.auth.PermissionMapper;
import com.gymflow.mapper.auth.RoleMapper;
import com.gymflow.mapper.settings.WebUserMapper;
import com.gymflow.service.auth.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final WebUserMapper webUserMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public PermissionResponseDTO getUserPermissions(Long userId) {
        // 1. 获取用户信息
        WebUser user = webUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        PermissionResponseDTO response = new PermissionResponseDTO();

        // 2. 如果用户没有角色（role_id为空），返回空权限
        if (user.getRoleId() == null) {
            response.setPermissions(List.of());
            response.setMenus(List.of());
            return response;
        }

        // 3. 获取角色信息（仅用于日志，不使用其字段）
        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null) {
            log.warn("用户角色不存在，roleId: {}", user.getRoleId());
            response.setPermissions(List.of());
            response.setMenus(List.of());
            return response;
        }

        log.info("用户角色: {}", role.getRoleCode());

        // 4. 获取用户权限编码列表
        List<String> permissions = roleMapper.selectPermissionsByUserId(userId);
        response.setPermissions(permissions);

        // 5. 获取用户菜单
        List<Permission> menus = permissionMapper.selectUserMenus(userId);

        // 6. 构建树形菜单
        List<Permission> treeMenus = buildMenuTree(menus);
        response.setMenus(treeMenus);

        log.info("获取用户权限成功，用户ID：{}，权限数量：{}，菜单数量：{}",
                userId, permissions.size(), treeMenus.size());

        return response;
    }

    /**
     * 构建树形菜单
     */
    private List<Permission> buildMenuTree(List<Permission> menus) {
        // 获取所有顶级菜单（parentId == 0）
        List<Permission> topMenus = menus.stream()
                .filter(menu -> menu.getParentId() != null && menu.getParentId() == 0L)
                .collect(Collectors.toList());

        // 为每个顶级菜单设置子菜单
        for (Permission menu : topMenus) {
            List<Permission> children = menus.stream()
                    .filter(m -> m.getParentId() != null && m.getParentId().equals(menu.getId()))
                    .collect(Collectors.toList());
            menu.setChildren(children);
        }

        return topMenus;
    }
}