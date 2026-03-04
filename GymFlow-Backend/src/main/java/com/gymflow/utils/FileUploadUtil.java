package com.gymflow.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
public class FileUploadUtil {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${file.access.url:http://localhost:8080/files}")
    private String accessUrl;

    //创建上传目录
    @PostConstruct
    public void init() {
        try {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("创建上传根目录: {}", uploadDir);
            }
        } catch (IOException e) {
            log.error("初始化上传目录失败", e);
        }
    }

    /**
     * 上传文件
     * @param file 文件
     * @param module 模块名称（如：images、avatars、logos等）
     * @return 可访问的文件URL
     */
    public String upload(MultipartFile file, String module) throws IOException {
        // 生成存储路径：uploads/模块/yyyy/MM/dd/
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = module + "/" + datePath;

        // 获取绝对路径
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path targetDir = uploadDir.resolve(relativePath);

        // 创建目录
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
            log.info("创建上传目录: {}", targetDir);
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + extension;

        // 保存文件
        Path filePath = targetDir.resolve(filename);
        file.transferTo(filePath.toFile());

        log.info("文件上传成功: {}", filePath);

        // 返回访问URL
        return accessUrl + "/" + relativePath.replace("\\", "/") + "/" + filename;
    }

    /**
     * 删除文件
     */
    public void delete(String url) {
        try {
            String relativePath = url.replace(accessUrl + "/", "");
            Path filePath = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(relativePath);

            File file = filePath.toFile();
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功: {}", url);
                    deleteEmptyDirectories(file.getParentFile());
                }
            }
        } catch (Exception e) {
            log.error("删除文件异常", e);
        }
    }

    /**
     * 递归删除空目录
     */
    private void deleteEmptyDirectories(File directory) {
        if (directory == null || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            if (directory.delete()) {
                log.info("删除空目录: {}", directory.getPath());
                deleteEmptyDirectories(directory.getParentFile());
            }
        }
    }
}