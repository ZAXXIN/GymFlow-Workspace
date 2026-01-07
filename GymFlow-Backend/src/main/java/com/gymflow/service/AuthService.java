package com.gymflow.service;

import com.gymflow.dto.LoginDTO;
import com.gymflow.dto.RegisterDTO;
import com.gymflow.utils.Result;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface AuthService {

    Result login(LoginDTO loginDTO);

    Result register(RegisterDTO registerDTO);

    Result logout(HttpServletRequest request);

    Result getCurrentUserInfo(HttpServletRequest request);

    Result changePassword(String oldPassword, String newPassword, HttpServletRequest request);

    Result resetPassword(Long userId, String newPassword, HttpServletRequest request);
}