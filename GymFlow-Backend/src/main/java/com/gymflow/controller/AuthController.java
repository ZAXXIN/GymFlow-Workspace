package com.gymflow.controller;

import com.gymflow.dto.LoginDTO;
import com.gymflow.dto.RegisterDTO;
import com.gymflow.service.AuthService;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result login(@Validated @RequestBody LoginDTO loginDTO) {
        return authService.login(loginDTO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register(@Validated @RequestBody RegisterDTO registerDTO) {
        return authService.register(registerDTO);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        return authService.logout(request);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result getCurrentUserInfo(HttpServletRequest request) {
        return authService.getCurrentUserInfo(request);
    }

    /**
     * 修改密码
     */
    @PostMapping("/password/change")
    public Result changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 HttpServletRequest request) {
        return authService.changePassword(oldPassword, newPassword, request);
    }

    /**
     * 重置密码（管理员用）
     */
    @PostMapping("/password/reset")
    public Result resetPassword(@RequestParam Long userId,
                                @RequestParam String newPassword,
                                HttpServletRequest request) {
        return authService.resetPassword(userId, newPassword, request);
    }
}