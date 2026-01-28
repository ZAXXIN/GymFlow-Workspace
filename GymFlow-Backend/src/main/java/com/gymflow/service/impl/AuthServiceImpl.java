package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.gymflow.dto.LoginDTO;
import com.gymflow.dto.LoginResultDTO;
import com.gymflow.entity.User;
import com.gymflow.mapper.UserMapper;
import com.gymflow.service.AuthService;
import com.gymflow.utils.JwtTokenUtil;
import com.gymflow.utils.PasswordUtil;
import com.gymflow.exception.BusinessException;
import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    // BCrypt密码编码器
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Override
    public LoginResultDTO login(LoginDTO loginDTO) {
        // 1. 参数校验
        if (!StringUtils.hasText(loginDTO.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(loginDTO.getPassword())) {
            throw new BusinessException("密码不能为空");
        }

        // 2. 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            log.warn("登录失败，用户名不存在: {}", loginDTO.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("用户已被禁用，请联系管理员");
        }

        // 4. 使用BCrypt验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            log.warn("登录失败，密码错误，用户名: {}", loginDTO.getUsername());
            throw new BusinessException("用户名或密码错误");
        }

        // 5. 生成token
        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        // 6. 构造返回结果
        LoginResultDTO result = new LoginResultDTO();
        result.setUserId(user.getId());
        result.setUsername(user.getUsername());
        result.setRealName(user.getRealName());
        result.setPhone(user.getPhone());
        result.setRole(user.getRole());
        result.setToken(token);
        result.setLoginTime(LocalDateTime.now());

        log.info("用户登录成功: {}", user.getUsername());

        return result;
    }

    @Override
    public void logout(String token) {
        // 实际项目中，这里可以将token加入黑名单或从Redis中删除
        // 暂时只做日志记录
        String username = jwtTokenUtil.getUsernameFromToken(token);
        log.info("用户登出: {}", username);
    }

    @Override
    public LoginResultDTO refreshToken(String oldToken) {
        if (!jwtTokenUtil.validateToken(oldToken)) {
            throw new BusinessException("token无效");
        }

        Long userId = jwtTokenUtil.getUserIdFromToken(oldToken);
        String username = jwtTokenUtil.getUsernameFromToken(oldToken);
        Integer role = jwtTokenUtil.getRoleFromToken(oldToken);

        // 生成新token
        String newToken = jwtTokenUtil.generateToken(userId, username, role);

        LoginResultDTO result = new LoginResultDTO();
        result.setUserId(userId);
        result.setUsername(username);
        result.setRole(role);
        result.setToken(newToken);
        result.setLoginTime(LocalDateTime.now());

        return result;
    }
}