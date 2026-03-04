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
 * 功能：
 * 1. 静态资源映射（文件上传访问）
 * 2. 拦截器配置（权限验证）
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
     * 作用：让上传的文件可以通过URL访问
     *
     * 配置前：
     *   文件存储位置：./uploads/images/2026/03/04/uuid.jpg
     *   访问方式：无法直接通过浏览器访问
     *
     * 配置后：
     *   文件存储位置：./uploads/images/2026/03/04/uuid.jpg
     *   访问方式：http://localhost:8080/api/files/images/2026/03/04/uuid.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取绝对路径
        String absolutePath = Paths.get(uploadPath).toAbsolutePath().normalize().toString().replace("\\", "/");

        // 配置静态资源映射
        // 注意：URL路径不要加 /api，因为这是静态资源，不是API
        // 但访问时需要通过 /api 进来，所以这里配置的是 /files/**
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
     * 作用：权限验证、日志记录等
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                // 需要拦截的路径
                .addPathPatterns("/**")
                // 不需要拦截的路径（白名单）
                .excludePathPatterns(
                        // 认证相关
                        "/auth/login",
                        "/auth/refresh-token",

                        // 文件上传和访问（文件访问是静态资源，已经通过addResourceHandlers配置，不会被拦截器拦截）
                        "/common/upload/**",

                        // Swagger文档相关
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",

                        // 静态资源（除了文件上传外的其他静态资源）
                        "/**.html",
                        "/**.css",
                        "/**.js",
                        "/**.png",
                        "/**.jpg",
                        "/**.jpeg",
                        "/**.gif",
                        "/**.svg",
                        "/**.ico",
                        "/**.json"
                );

        log.info("==========================================");
        log.info("拦截器配置:");
        log.info("  拦截路径: /**");
        log.info("  排除路径: /auth/login, /auth/refresh-token, /common/upload/**, /swagger-ui/**, /v3/api-docs/**");
        log.info("==========================================");
    }
}