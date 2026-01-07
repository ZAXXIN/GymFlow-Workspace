package com.gymflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.RegisterDTO;
import com.gymflow.entity.Admin;
import com.gymflow.utils.Result;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface AdminService {

    Result getAdminList(Page<Admin> page, String keyword, HttpServletRequest request);

    Result getAdminDetail(Long id, HttpServletRequest request);

    Result addAdmin(RegisterDTO registerDTO, HttpServletRequest request);

    Result updateAdmin(Long id, Admin admin, HttpServletRequest request);

    Result deleteAdmin(Long id, HttpServletRequest request);

    Result updateAdminPermissions(Long id, String permissions, HttpServletRequest request);

    Result getSystemLogs(Page<Object> page, String type, String startDate, String endDate, HttpServletRequest request);

    Result getSystemSettings(HttpServletRequest request);

    Result updateSystemSettings(String settings, HttpServletRequest request);
}