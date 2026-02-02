package com.gymflow.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "会员登录结果DTO")
public class MemberLoginResultDTO {

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "会员编号")
    private String memberNo;

    @Schema(description = "Token")
    private String token;

    @Schema(description = "Token过期时间")
    private Long tokenExpireTime;
}