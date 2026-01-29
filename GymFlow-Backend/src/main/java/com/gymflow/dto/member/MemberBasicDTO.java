package com.gymflow.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "会员基础信息DTO")
public class MemberBasicDTO {

    // 用户信息
//    @NotBlank(message = "用户名不能为空")
//    @Size(max = 50, message = "用户名长度不能超过50")
//    @Schema(description = "用户名", example = "zhangsan", required = true)
//    private String username;
//
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需在6-20之间")
    @Schema(description = "密码", example = "123456", required = true)
    private String password;

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

    @Size(max = 100, message = "部门长度不能超过100")
    @Schema(description = "所属部门/公司", example = "技术部")
    private String department;

    @Size(max = 100, message = "职位长度不能超过100")
    @Schema(description = "职位", example = "工程师")
    private String position;

    // 会员信息
    @NotBlank(message = "会员编号不能为空")
    @Size(max = 20, message = "会员编号长度不能超过20")
    @Schema(description = "会员编号", example = "GYM20260128001", required = true)
    private String memberNo;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$",
            message = "身份证号格式不正确")
    @Schema(description = "身份证号", example = "110101199001011234", required = true)
    private String idCard;

    @DecimalMin(value = "50.00", message = "身高不能小于50cm")
    @DecimalMax(value = "250.00", message = "身高不能大于250cm")
    @Schema(description = "身高（cm）", example = "175.0", required = true)
    private BigDecimal height;

    @DecimalMin(value = "0.01", message = "体重不能小于0.01kg")
    @Schema(description = "体重（kg）", example = "65.5", required = true)
    private BigDecimal weight;

    @NotNull(message = "会员开始日期不能为空")
    @Schema(description = "会员有效期开始日期", example = "2026-01-01", required = true)
    private LocalDate membershipStartDate;

    @NotNull(message = "会员结束日期不能为空")
    @Schema(description = "会员有效期结束日期", example = "2027-01-01", required = true)
    private LocalDate membershipEndDate;

    @Positive(message = "私教ID必须为正数")
    @Schema(description = "专属私教ID", example = "1001")
    private Long personalCoachId;

    @Size(max = 500, message = "地址长度不能超过500")
    @Schema(description = "居住地址", example = "北京市朝阳区XX小区")
    private String address;
}