package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Schema(description = "小程序登录DTO")
public class MiniLoginDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000", required = true)
    private String phone;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456", required = true)
    private String password;
}