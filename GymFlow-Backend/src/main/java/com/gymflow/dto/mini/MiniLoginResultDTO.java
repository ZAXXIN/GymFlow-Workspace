package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "小程序登录结果DTO")
public class MiniLoginResultDTO {

    @Schema(description = "用户ID（会员ID或教练ID）")
    private Long userId;

    @Schema(description = "用户类型：0-会员，1-教练")
    private Integer userType;

    @Schema(description = "用户类型描述")
    private String userTypeDesc;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "会员编号（仅会员有）")
    private String memberNo;

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    public String getUserTypeDesc() {
        if (userType == null) return "未知";
        switch (userType) {
            case 0: return "会员";
            case 1: return "教练";
            default: return "未知";
        }
    }
}