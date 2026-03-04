package com.gymflow.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.auth.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID查询角色编码
     */
    @Select("SELECT r.role_code FROM role r " +
            "LEFT JOIN web_user w ON w.role_id = r.id " +
            "WHERE w.id = #{userId}")
    String selectRoleCodeByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询权限编码列表
     */
    @Select("SELECT DISTINCT p.permission_code FROM permission p " +
            "LEFT JOIN role_permission rp ON rp.permission_id = p.id " +
            "LEFT JOIN web_user w ON w.role_id = rp.role_id " +
            "WHERE w.id = #{userId} AND p.status = 1")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
}