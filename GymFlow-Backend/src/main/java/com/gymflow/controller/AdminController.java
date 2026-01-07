package com.gymflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.RegisterDTO;
import com.gymflow.entity.Admin;
import com.gymflow.service.AdminService;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 获取管理员列表（分页）
     */
    @GetMapping("/list")
    public Result getAdminList(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @RequestParam(required = false) String keyword,
                               HttpServletRequest request) {
        Page<Admin> page = new Page<>(pageNum, pageSize);
        return adminService.getAdminList(page, keyword, request);
    }

    /**
     * 获取管理员详情
     */
    @GetMapping("/detail/{id}")
    public Result getAdminDetail(@PathVariable Long id, HttpServletRequest request) {
        return adminService.getAdminDetail(id, request);
    }

    /**
     * 添加管理员
     */
    @PostMapping("/add")
    public Result addAdmin(@Validated @RequestBody RegisterDTO registerDTO, HttpServletRequest request) {
        return adminService.addAdmin(registerDTO, request);
    }

    /**
     * 更新管理员信息
     */
    @PutMapping("/update/{id}")
    public Result updateAdmin(@PathVariable Long id, @RequestBody Admin admin, HttpServletRequest request) {
        return adminService.updateAdmin(id, admin, request);
    }

    /**
     * 删除管理员
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteAdmin(@PathVariable Long id, HttpServletRequest request) {
        return adminService.deleteAdmin(id, request);
    }

    /**
     * 更新管理员权限
     */
    @PutMapping("/permissions/{id}")
    public Result updateAdminPermissions(@PathVariable Long id,
                                         @RequestParam String permissions,
                                         HttpServletRequest request) {
        return adminService.updateAdminPermissions(id, permissions, request);
    }

    /**
     * 获取系统日志
     */
    @GetMapping("/logs")
    public Result getSystemLogs(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(required = false) String type,
                                @RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate,
                                HttpServletRequest request) {
        Page<Object> page = new Page<>(pageNum, pageSize);
        return adminService.getSystemLogs(page, type, startDate, endDate, request);
    }

    /**
     * 系统设置
     */
    @GetMapping("/settings")
    public Result getSystemSettings(HttpServletRequest request) {
        return adminService.getSystemSettings(request);
    }

    /**
     * 更新系统设置
     */
    @PutMapping("/settings")
    public Result updateSystemSettings(@RequestBody String settings, HttpServletRequest request) {
        return adminService.updateSystemSettings(settings, request);
    }
}