package com.gymflow.config;

import com.gymflow.interceptor.PermissionInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC配置类
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    /**
     * 配置静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadPath).toAbsolutePath().normalize().toString().replace("\\", "/");
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + absolutePath + "/");

        log.info("==========================================");
        log.info("静态资源映射配置:");
        log.info("  访问URL: http://localhost:8080/api/files/**");
        log.info("  本地路径: file:" + absolutePath + "/");
        log.info("==========================================");
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 不需要拦截的路径（登录和静态资源）
                .excludePathPatterns(
                        // 注意：/mini/** 路径不再排除，因为我们要在拦截器内部判断
                        // 这样我们可以区分小程序端和PC端的权限需求
                );

        log.info("==========================================");
        log.info("拦截器配置:");
        log.info("  拦截路径: /**");
        log.info("  小程序端请求: /mini/** 自动放行（无需权限）");
        log.info("  PC端白名单: /auth/login, /auth/refresh-token, /common/upload/**, /swagger-ui/**");
        log.info("==========================================");
    }
}