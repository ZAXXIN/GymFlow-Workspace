package com.gymflow.controller.mini;

import com.gymflow.common.Result;
import com.gymflow.dto.mini.MiniLoginDTO;
import com.gymflow.dto.mini.MiniLoginResultDTO;
import com.gymflow.service.mini.MiniAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/mini/auth")
@Tag(name = "小程序-认证管理", description = "小程序专用认证接口")
@Validated
@RequiredArgsConstructor
public class MiniAuthController {

    private final MiniAuthService miniAuthService;

    @PostMapping("/login")
    @Operation(summary = "小程序登录", description = "使用手机号+密码登录，自动识别用户类型")
    public Result<MiniLoginResultDTO> login(@Valid @RequestBody MiniLoginDTO loginDTO) {
        try {
            MiniLoginResultDTO result = miniAuthService.login(loginDTO);
            return Result.success("登录成功", result);
        } catch (Exception e) {
            log.error("小程序登录失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "小程序登出")
    public Result<Void> logout(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                miniAuthService.logout(token);
            }
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("小程序登出失败: {}", e.getMessage());
            return Result.error("登出失败");
        }
    }
}