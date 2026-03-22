package com.gymflow.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "会员基础信息DTO")
public class MemberBasicDTO {

    @Schema(description = "会员ID", example = "1001")
    private Long id;

//    @Size(min = 6, max = 20, message = "密码长度需在6-20之间")
//    @Schema(description = "密码", example = "123456", required = true)
//    private String password;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000", required = true)
    private String phone;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50")
    @Schema(description = "真实姓名", example = "张三", required = true)
    private String realName;

    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别值只能是0或1")
    @Max(value = 1, message = "性别值只能是0或1")
    @Schema(description = "性别：0-女，1-男", example = "1", required = true)
    private Integer gender;

    @NotNull(message = "出生日期不能为空")
    @Schema(description = "出生日期", example = "1990-01-01", required = true)
    private LocalDate birthday;

    @Schema(description = "会员编号", example = "GYM20260128001", required = true)
    private String memberNo;

    @Schema(description = "会员有效期开始日期", example = "2026-01-01", required = true)
    private LocalDate membershipStartDate;

    @Schema(description = "会员有效期结束日期", example = "2027-01-01", required = true)
    private LocalDate membershipEndDate;
}