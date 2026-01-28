package com.gymflow.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

//密码加密工具类
@Component
public class PasswordUtil {

    /**
     * 生成盐值
     */
    public String generateSalt() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 加密密码（MD5 + 盐）
     */
    public String encryptPassword(String password, String salt) {
        String str = salt + password + salt;
        return DigestUtils.md5DigestAsHex(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(String inputPassword, String storedPassword, String salt) {
        String encryptedPassword = encryptPassword(inputPassword, salt);
        return encryptedPassword.equals(storedPassword);
    }
}
