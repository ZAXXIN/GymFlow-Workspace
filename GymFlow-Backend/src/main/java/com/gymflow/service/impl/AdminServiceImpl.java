package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.RegisterDTO;
import com.gymflow.entity.Admin;
import com.gymflow.entity.User;
import com.gymflow.enums.UserRole;
import com.gymflow.mapper.AdminMapper;
import com.gymflow.mapper.UserMapper;
import com.gymflow.service.AdminService;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result getAdminList(Page<Admin> page, String keyword, HttpServletRequest request) {
        try {
            Page<Admin> result = adminMapper.selectPage(page, new QueryWrapper<Admin>()
                    .like(keyword != null, "real_name", keyword)
                    .orderByDesc("create_time"));
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result getAdminDetail(Long id, HttpServletRequest request) {
        try {
            Admin admin = adminMapper.selectById(id);
            if (admin == null) {
                return Result.error("管理员不存在");
            }
            return Result.success(admin);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result addAdmin(RegisterDTO registerDTO, HttpServletRequest request) {
        try {
            // 检查用户名是否已存在
            if (userMapper.countByUsername(registerDTO.getUsername()) > 0) {
                return Result.error("用户名已存在");
            }

            // 检查手机号是否已存在
            if (userMapper.countByPhone(registerDTO.getPhone()) > 0) {
                return Result.error("手机号已存在");
            }

            // 创建用户
            User user = new User();
            user.setUsername(registerDTO.getUsername());
            user.setPassword(registerDTO.getPassword()); // 实际应该加密
            user.setPhone(registerDTO.getPhone());
            user.setEmail(registerDTO.getEmail());
            user.setRole(UserRole.ADMIN);
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            int userResult = userMapper.insert(user);
            if (userResult <= 0) {
                throw new RuntimeException("创建用户失败");
            }

            // 创建管理员
            Admin admin = new Admin();
            admin.setUserId(user.getId());
            admin.setRealName(registerDTO.getRealName());
            admin.setDepartment("运营部");
            admin.setPosition("管理员");
            admin.setPermissions("[]");
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());

            int adminResult = adminMapper.insert(admin);
            if (adminResult <= 0) {
                throw new RuntimeException("创建管理员失败");
            }

            return Result.success("管理员添加成功");
        } catch (Exception e) {
            throw new RuntimeException("添加管理员失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateAdmin(Long id, Admin admin, HttpServletRequest request) {
        try {
            Admin existingAdmin = adminMapper.selectById(id);
            if (existingAdmin == null) {
                return Result.error("管理员不存在");
            }

            admin.setId(id);
            admin.setUpdateTime(LocalDateTime.now());
            int result = adminMapper.updateById(admin);

            if (result > 0) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result deleteAdmin(Long id, HttpServletRequest request) {
        try {
            Admin admin = adminMapper.selectById(id);
            if (admin == null) {
                return Result.error("管理员不存在");
            }

            // 删除管理员记录
            int adminResult = adminMapper.deleteById(id);
            if (adminResult <= 0) {
                return Result.error("删除管理员失败");
            }

            // 删除对应的用户记录
            int userResult = userMapper.deleteById(admin.getUserId());
            if (userResult <= 0) {
                throw new RuntimeException("删除用户失败");
            }

            return Result.success("删除成功");
        } catch (Exception e) {
            throw new RuntimeException("删除失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateAdminPermissions(Long id, String permissions, HttpServletRequest request) {
        try {
            Admin admin = new Admin();
            admin.setId(id);
            admin.setPermissions(permissions);
            admin.setUpdateTime(LocalDateTime.now());

            int result = adminMapper.updateById(admin);
            if (result > 0) {
                return Result.success("权限更新成功");
            } else {
                return Result.error("权限更新失败");
            }
        } catch (Exception e) {
            return Result.error("权限更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result getSystemLogs(Page<Object> page, String type, String startDate, String endDate, HttpServletRequest request) {
        // TODO: 实现获取系统日志逻辑
        return Result.success("获取系统日志功能待实现");
    }

    @Override
    public Result getSystemSettings(HttpServletRequest request) {
        // TODO: 实现获取系统设置逻辑
        return Result.success("获取系统设置功能待实现");
    }

    @Override
    public Result updateSystemSettings(String settings, HttpServletRequest request) {
        // TODO: 实现更新系统设置逻辑
        return Result.success("更新系统设置功能待实现");
    }
}