package com.gymflow.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.auth.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 获取用户菜单权限（树形结构）
     */
    @Select("SELECT DISTINCT p.* FROM permission p " +
            "LEFT JOIN role_permission rp ON rp.permission_id = p.id " +
            "LEFT JOIN web_user w ON w.role_id = rp.role_id " +
            "WHERE w.id = #{userId} AND p.type = 1 AND p.status = 1 " +
            "ORDER BY p.sort_order")
    List<Permission> selectUserMenus(@Param("userId") Long userId);

    /**
     * 获取用户按钮权限
     */
    @Select("SELECT DISTINCT p.permission_code FROM permission p " +
            "LEFT JOIN role_permission rp ON rp.permission_id = p.id " +
            "LEFT JOIN web_user w ON w.role_id = rp.role_id " +
            "WHERE w.id = #{userId} AND p.type = 3 AND p.status = 1")
    List<String> selectUserButtons(@Param("userId") Long userId);

    /**
     * 统计角色拥有的权限数量
     */
    @Select("SELECT COUNT(*) FROM role_permission WHERE role_id = #{roleId}")
    Integer countPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 插入角色权限关联
     */
    @Insert("INSERT INTO role_permission(role_id, permission_id) VALUES(#{roleId}, #{permissionId})")
    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 删除角色的所有权限
     */
    @Delete("DELETE FROM role_permission WHERE role_id = #{roleId}")
    int deleteRolePermissions(@Param("roleId") Long roleId);
}