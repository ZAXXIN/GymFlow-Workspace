package com.gymflow.service.mini;

import com.gymflow.dto.mini.MiniLoginDTO;
import com.gymflow.dto.mini.MiniLoginResultDTO;

public interface MiniAuthService {

    /**
     * 小程序登录（手机号+密码）
     * 自动识别用户类型（会员/教练）
     */
    MiniLoginResultDTO login(MiniLoginDTO loginDTO);

    /**
     * 小程序登出
     */
    void logout(String token);
}