package com.gymflow.dto.coach;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "教练基础信息DTO")
public class CoachBasicDTO {

    @Schema(description = "教练ID")
    private Long id;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50")
    @Schema(description = "真实姓名", required = true)
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", required = true)
    private String phone;

    @Schema(description = "密码（新增时必填）")
    private String password;

    // 新增性别字段
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别值只能是0、1")
    @Max(value = 1, message = "性别值只能是0、1")
    @Schema(description = "性别：0-女，1-男", required = true)
    private Integer gender;

    @Size(max = 500, message = "专业特长长度不能超过500")
    @Schema(description = "专长领域")
    private String specialty;

    @Schema(description = "资格证书列表")
    private List<String> certificationList;

    @Min(value = 0, message = "从业年限不能小于0")
    @Schema(description = "经验年限")
    private Integer yearsOfExperience;

    @Schema(description = "状态：0-离职，1-在职")
    private Integer status;

    @Schema(description = "评分")
    private BigDecimal rating;

    @Size(max = 1000, message = "个人简介长度不能超过1000")
    @Schema(description = "个人简介")
    private String introduction;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}