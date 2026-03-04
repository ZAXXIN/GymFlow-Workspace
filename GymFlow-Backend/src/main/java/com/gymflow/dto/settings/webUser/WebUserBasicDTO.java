package com.gymflow.dto.settings.webUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Schema(description = "用户基础信息DTO")
public class WebUserBasicDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50")
    @Schema(description = "用户名", required = true)
    private String username;

    @Size(max = 255, message = "密码长度不能超过255")
    @Schema(description = "密码（新增时必填，编辑时如果changePassword为true则必填）")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50")
    @Schema(description = "真实姓名", required = true)
    private String realName;

    @NotNull(message = "角色不能为空")
    @Schema(description = "角色：0-老板，1-前台", required = true)
    private Integer role;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-禁用，1-正常", required = true)
    private Integer status;

    @Schema(description = "是否修改密码（编辑模式使用）")
    private Boolean changePassword;

    @Schema(description = "新密码（changePassword为true时必填）")
    private String newPassword;
}