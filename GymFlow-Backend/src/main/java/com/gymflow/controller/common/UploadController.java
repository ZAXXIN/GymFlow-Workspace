package com.gymflow.controller.common;

import com.gymflow.common.Result;
import com.gymflow.utils.FileUploadUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/common/upload")
@Tag(name = "通用上传接口", description = "文件上传相关接口")
@RequiredArgsConstructor
public class UploadController {

    private final FileUploadUtil fileUploadUtil;

    @PostMapping("/image")
    @Operation(summary = "上传图片", description = "上传图片文件，返回图片URL")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件类型
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return Result.error("文件名为空");
            }

            // 获取文件扩展名
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

            // 允许的图片格式
            String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg"};
            boolean isValid = false;
            for (String ext : allowedExtensions) {
                if (ext.equals(extension)) {
                    isValid = true;
                    break;
                }
            }

            if (!isValid) {
                return Result.error("不支持的文件格式，请上传 JPG、PNG、GIF、BMP、WEBP 或 SVG 格式的图片");
            }

            // 验证文件大小（限制5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("文件大小不能超过5MB");
            }

            // 上传文件并获取URL
            String fileUrl = fileUploadUtil.upload(file, "images");

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", originalFilename);

            return Result.success("上传成功", result);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/file")
    @Operation(summary = "上传文件", description = "上传通用文件，返回文件URL")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件大小（限制20MB）
            if (file.getSize() > 20 * 1024 * 1024) {
                return Result.error("文件大小不能超过20MB");
            }

            // 上传文件并获取URL
            String fileUrl = fileUploadUtil.upload(file, "files");

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", file.getOriginalFilename());
            result.put("size", String.valueOf(file.getSize()));

            return Result.success("上传成功", result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @DeleteMapping
    @Operation(summary = "删除文件", description = "根据URL删除已上传的文件")
    public Result<Void> deleteFile(@RequestParam String url) {
        try {
            fileUploadUtil.delete(url);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}