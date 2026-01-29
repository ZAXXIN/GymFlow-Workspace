package com.gymflow.dto.login;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LoginResultDTO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "角色：0-老板，1-前台，2-教练，3-会员", example = "0")
    private Integer role;

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
}