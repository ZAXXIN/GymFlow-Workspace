package com.gymflow.interceptor;

import com.gymflow.common.annotation.PreAuthorize;
import com.gymflow.exception.BusinessException;
import com.gymflow.utils.JwtTokenUtil;
import com.gymflow.mapper.auth.RoleMapper;
import com.gymflow.entity.settings.WebUser;
import com.gymflow.mapper.settings.WebUserMapper;
import com.gymflow.interceptor.UserContext.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private WebUserMapper webUserMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是方法请求，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 获取并验证token
        String token = extractToken(request);
        if (token == null) {
            throw new BusinessException(401, "未登录或token无效");
        }

        // 验证token有效性
        if (!jwtTokenUtil.validateToken(token)) {
            throw new BusinessException(401, "token已过期或无效");
        }

        // 获取用户信息
        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        WebUser user = webUserMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(401, "用户已被禁用");
        }

        // 获取用户角色编码
        String roleCode = null;
        if (user.getRoleId() != null) {
            roleCode = roleMapper.selectRoleCodeByUserId(userId);
        }

        // 将用户信息存入线程上下文 - 修改这里：user.getRoleId() 是 Long 类型，直接传递
        UserContext.setCurrentUser(new UserInfo(
                userId,
                user.getUsername(),
                roleCode,
                user.getRoleId()  // 这里已经是 Long 类型，不需要转换
        ));

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        PreAuthorize preAuthorize = handlerMethod.getMethodAnnotation(PreAuthorize.class);

        // 如果没有权限注解，表示不需要权限验证
        if (preAuthorize == null) {
            return true;
        }

        // 获取用户权限列表
        List<String> userPermissions = roleMapper.selectPermissionsByUserId(userId);

        // 验证权限
        String[] requiredPermissions = preAuthorize.value().split(",");
        boolean hasPermission = false;

        if (preAuthorize.logical() == PreAuthorize.Logical.AND) {
            // 需要所有权限
            hasPermission = Arrays.stream(requiredPermissions)
                    .allMatch(userPermissions::contains);
        } else {
            // 满足一个即可
            hasPermission = Arrays.stream(requiredPermissions)
                    .anyMatch(userPermissions::contains);
        }

        if (!hasPermission) {
            log.warn("用户{}没有权限访问：{}，所需权限：{}",
                    userId, request.getRequestURI(), preAuthorize.value());
            throw new BusinessException(403, "没有操作权限");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清除用户上下文
        UserContext.clear();
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