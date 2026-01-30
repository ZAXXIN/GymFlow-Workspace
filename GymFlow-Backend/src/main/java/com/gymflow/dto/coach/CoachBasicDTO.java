package com.gymflow.dto.coach;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "教练基础信息DTO")
public class CoachBasicDTO {

    @NotBlank(message = "教练姓名不能为空")
    @Size(max = 50, message = "教练姓名长度不能超过50")
    @Schema(description = "教练姓名", example = "张三", required = true)
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000", required = true)
    private String phone;

    @Size(min = 6, max = 20, message = "密码长度需在6-20之间")
    @Schema(description = "密码（为空则不修改）", example = "123456")
    private String password;

    @Size(max = 500, message = "专业特长长度不能超过500")
    @Schema(description = "专业特长", example = "瑜伽、普拉提、体态矫正")
    private String specialty;

    @Schema(description = "资质证书列表")
    private List<String> certificationList;

    @NotNull(message = "从业年限不能为空")
    @Min(value = 0, message = "从业年限不能小于0")
    @Schema(description = "从业年限", example = "5", required = true)
    private Integer yearsOfExperience;

    @NotNull(message = "课时费不能为空")
    @DecimalMin(value = "0.00", message = "课时费不能小于0")
    @Schema(description = "课时费（元/小时）", example = "300.00", required = true)
    private BigDecimal hourlyRate;

    @NotNull(message = "佣金比例不能为空")
    @DecimalMin(value = "0.00", message = "佣金比例不能小于0")
    @DecimalMax(value = "100.00", message = "佣金比例不能大于100")
    @Schema(description = "佣金比例（%）", example = "20.00", required = true)
    private BigDecimal commissionRate;

    @Size(max = 2000, message = "个人介绍长度不能超过2000")
    @Schema(description = "个人介绍", example = "拥有10年瑜伽教学经验...")
    private String introduction;
}