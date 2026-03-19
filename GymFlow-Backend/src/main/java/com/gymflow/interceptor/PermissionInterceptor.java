package com.gymflow.interceptor;

import com.gymflow.exception.BusinessException;
import com.gymflow.utils.JwtTokenUtil;
import com.gymflow.utils.PermissionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 权限拦截器
 * 只拦截PC端请求，小程序端请求（/mini/**）跳过权限验证
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PermissionUtil permissionUtil;

    // 小程序端路径前缀，这些路径跳过权限验证
    private static final List<String> MINI_PATH_PREFIXES = Arrays.asList(
            "/mini/",      // 所有小程序专用接口
            "/api/mini/"   // 带/api前缀的小程序接口
    );

    // PC端不需要验证的路径
    private static final List<String> PC_WHITE_LIST = Arrays.asList(
            "/auth/login",
            "/api/auth/login",
            "/auth/refresh-token",
            "/api/auth/refresh-token",
            "/common/upload/",
            "/api/common/upload/",
            "/settings/systemConfig",
            "/api/settings/systemConfig",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/swagger-resources/",
            "/webjars/",
            "/files/",
            "/api/files/"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 如果是OPTIONS请求，直接放行（CORS预检）
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        log.debug("请求路径: {}", path);

        // 1. 检查是否是小程序端请求 - 直接放行，不需要任何权限验证
        if (isMiniRequest(path)) {
            log.debug("小程序端请求，跳过权限验证: {}", path);
            return true;
        }

        // 2. 检查是否是PC端白名单路径
        if (isPcWhiteList(path)) {
            log.debug("PC端白名单路径，跳过权限验证: {}", path);
            return true;
        }

        // 3. PC端请求需要验证token和权限
        String token = extractToken(request);
        if (token == null) {
            log.warn("PC端请求未携带token: {}", path);
            throw new BusinessException(401, "未登录或token无效");
        }

        // 验证token
        if (!jwtTokenUtil.validateToken(token)) {
            log.warn("PC端请求token无效: {}", path);
            throw new BusinessException(401, "token无效或已过期");
        }

        // 设置用户上下文
        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        Integer role = jwtTokenUtil.getRoleFromToken(token);

        UserContext.setCurrentUser(userId, username, role);

        log.debug("PC端请求通过权限验证: {}, 用户: {}", path, username);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除用户上下文
        UserContext.clear();
    }

    /**
     * 判断是否是小程序端请求
     */
    private boolean isMiniRequest(String path) {
        for (String prefix : MINI_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否是PC端白名单路径
     */
    private boolean isPcWhiteList(String path) {
        for (String whitePath : PC_WHITE_LIST) {
            if (path.startsWith(whitePath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从请求头中提取token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}