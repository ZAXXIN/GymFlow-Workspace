package com.gymflow.controller.auth;

import com.gymflow.common.Result;
import com.gymflow.dto.permission.PermissionResponseDTO;
import com.gymflow.service.auth.PermissionService;
import com.gymflow.utils.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/auth")
@Tag(name = "权限管理", description = "用户权限相关接口")
@RequiredArgsConstructor
public class PermissionsController {

    private final PermissionService permissionService;
    private final JwtTokenUtil jwtTokenUtil;

    @GetMapping("/permissions")
    @Operation(summary = "获取用户权限列表")
    public Result<PermissionResponseDTO> getUserPermissions(HttpServletRequest request) {
        try {
            // 从请求头获取token
            String token = extractToken(request);
            if (token == null) {
                return Result.error(401, "未登录或token无效");
            }

            // 从token中获取用户ID
            Long userId = jwtTokenUtil.getUserIdFromToken(token);

            // 获取用户权限和菜单
            PermissionResponseDTO response = permissionService.getUserPermissions(userId);

            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取用户权限失败: {}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 从请求头中提取token
     */
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }
}