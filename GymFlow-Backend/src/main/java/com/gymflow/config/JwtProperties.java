package com.gymflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//JWT配置类
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secretKey = "gymflow-secret-key-2024"; // 建议在生产环境中修改

    private long expiration = 86400000; // token有效期，默认24小时（毫秒）

    private String tokenHeader = "Authorization";

    private String tokenPrefix = "Bearer ";
}
