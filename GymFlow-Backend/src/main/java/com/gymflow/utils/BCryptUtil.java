package com.gymflow.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptUtil {

    private final BCryptPasswordEncoder passwordEncoder;

    public BCryptUtil() {
        // 设置强度为10（默认）
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * 加密密码
     */
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 生成加密密码（用于初始化数据）
     */
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 生成123456的BCrypt加密
        String encodedPassword = encoder.encode("123456");
        System.out.println("123456 的 BCrypt 加密结果: " + encodedPassword);

        // 验证
        boolean matches = encoder.matches("123456", encodedPassword);
        System.out.println("密码验证结果: " + matches);
    }
}