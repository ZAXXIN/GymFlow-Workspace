package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    /**
     * 根据用户ID查询管理员
     */
    @Select("SELECT * FROM admin WHERE user_id = #{userId}")
    Admin selectByUserId(@Param("userId") Long userId);

    /**
     * 根据手机号查询管理员
     */
    @Select("SELECT a.* FROM admin a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE u.phone = #{phone} AND u.deleted = 0")
    Admin selectByPhone(@Param("phone") String phone);

    /**
     * 根据部门查询管理员列表
     */
    @Select("SELECT * FROM admin WHERE department = #{department} ORDER BY create_time DESC")
    List<Admin> selectByDepartment(@Param("department") String department);

    /**
     * 更新管理员最后登录IP
     */
    @Update("UPDATE admin SET last_login_ip = #{ip}, login_count = login_count + 1 WHERE id = #{adminId}")
    int updateLastLoginInfo(@Param("adminId") Long adminId, @Param("ip") String ip);

    /**
     * 更新管理员权限
     */
    @Update("UPDATE admin SET permissions = #{permissions}, update_time = NOW() WHERE id = #{adminId}")
    int updatePermissions(@Param("adminId") Long adminId, @Param("permissions") String permissions);

    /**
     * 更新管理员职位信息
     */
    @Update("UPDATE admin SET department = #{department}, position = #{position}, update_time = NOW() " +
            "WHERE id = #{adminId}")
    int updatePosition(@Param("adminId") Long adminId,
                       @Param("department") String department,
                       @Param("position") String position);

    /**
     * 查询所有有权限的管理员
     */
    @Select("SELECT * FROM admin WHERE permissions IS NOT NULL AND permissions != '[]' ORDER BY create_time DESC")
    List<Admin> selectAdminsWithPermissions();

    /**
     * 根据权限查询管理员
     */
    @Select("SELECT * FROM admin WHERE permissions LIKE CONCAT('%', #{permission}, '%') ORDER BY create_time DESC")
    List<Admin> selectByPermission(@Param("permission") String permission);
}