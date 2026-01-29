package com.gymflow.service;

import com.gymflow.dto.login.LoginDTO;
import com.gymflow.dto.login.LoginResultDTO;

//登录服务接口
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResultDTO login(LoginDTO loginDTO);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 刷新token
     */
    LoginResultDTO refreshToken(String oldToken);
}