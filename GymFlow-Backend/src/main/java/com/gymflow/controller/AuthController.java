package com.gymflow.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.gymflow.dto.login.LoginDTO;
import com.gymflow.dto.login.LoginResultDTO;
import com.gymflow.service.AuthService;
import com.gymflow.common.Result;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户认证相关接口")
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "用户登录", description = "使用用户名和密码登录系统")
    @PostMapping("/login")
    public Result<LoginResultDTO> login(
            @Parameter(description = "登录参数", required = true)
            @Valid @RequestBody LoginDTO loginDTO) {
        try {
            LoginResultDTO result = authService.login(loginDTO);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "用户登出", description = "用户登出系统")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                authService.logout(token);
            }
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage());
            return Result.error("登出失败");
        }
    }

    @Operation(summary = "刷新Token", description = "使用旧Token刷新新Token")
    @PostMapping("/refresh-token")
    public Result<LoginResultDTO> refreshToken(
            @Parameter(description = "Authorization请求头", required = true)
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return Result.error("token格式错误");
            }

            String oldToken = authorizationHeader.substring(7);
            LoginResultDTO result = authService.refreshToken(oldToken);
            return Result.success("token刷新成功", result);
        } catch (Exception e) {
            log.error("刷新token失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}