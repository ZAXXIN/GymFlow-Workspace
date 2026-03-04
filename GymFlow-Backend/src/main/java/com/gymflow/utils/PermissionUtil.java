package com.gymflow.utils;

import com.gymflow.exception.BusinessException;
import com.gymflow.interceptor.UserContext;
import com.gymflow.mapper.auth.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限验证工具类
 */
@Component
public class PermissionUtil {

    @Autowired
    private RoleMapper roleMapper;

    /**
     * 获取当前登录用户ID
     */
    public Long getCurrentUserId() {
        return UserContext.getCurrentUserId();
    }

    /**
     * 获取当前用户角色编码
     */
    public String getCurrentUserRole() {
        return UserContext.getCurrentUserRole();
    }

    /**
     * 判断是否为老板
     */
    public boolean isBoss() {
        return "BOSS".equals(getCurrentUserRole());
    }

    /**
     * 判断是否为前台
     */
    public boolean isReceptionist() {
        return "RECEPTIONIST".equals(getCurrentUserRole());
    }

    /**
     * 验证当前用户是否有指定权限
     */
    public boolean hasPermission(String permissionCode) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return false;
        }
        List<String> permissions = roleMapper.selectPermissionsByUserId(userId);
        return permissions.contains(permissionCode);
    }

    /**
     * 验证当前用户是否有指定权限，没有则抛出异常
     */
    public void checkPermission(String permissionCode) {
        if (!hasPermission(permissionCode)) {
            throw new BusinessException(403, "没有操作权限");
        }
    }

    /**
     * 验证当前用户是否为老板，不是则抛出异常
     */
    public void checkBoss() {
        if (!isBoss()) {
            throw new BusinessException(403, "只有老板可以进行此操作");
        }
    }

    /**
     * 验证当前用户是否为前台，不是则抛出异常
     */
    public void checkReceptionist() {
        if (!isReceptionist()) {
            throw new BusinessException(403, "只有前台可以进行此操作");
        }
    }
}