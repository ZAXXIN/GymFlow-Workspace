package com.gymflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
//通过 URL 访问上传的文件
// 配置前：
//       文件存储位置：./uploads/images/2026/03/04/uuid.jpg
//       访问方式：无法直接通过浏览器访问
// 配置后：
//       文件存储位置：./uploads/images/2026/03/04/uuid.jpg
//       访问方式：http://localhost:8080/files/images/2026/03/04/uuid.jpg
// config/WebMvcConfig.java

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取绝对路径
        String absolutePath = Paths.get(uploadPath).toAbsolutePath().normalize().toString().replace("\\", "/");

        // 关键点：URL路径不要加 /api，因为这是静态资源，不是API
        // 但访问时需要通过 /api 进来，所以这里配置的是 /files/**
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + absolutePath + "/");

        System.out.println("==========================================");
        System.out.println("静态资源映射配置:");
        System.out.println("  访问URL: http://localhost:8080/api/files/**");
        System.out.println("  本地路径: file:" + absolutePath + "/");
        System.out.println("==========================================");
    }
}