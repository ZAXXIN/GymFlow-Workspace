package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@Schema(description = "数字码核销请求")
public class VerifyCodeRequest {

    @NotBlank(message = "数字码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "数字码必须是6位数字")
    @Schema(description = "6位数字签到码", example = "123456", required = true)
    private String digitalCode;

    @NotNull(message = "签到方式不能为空")
    @Schema(description = "签到方式：0-教练签到，1-前台签到", example = "0", required = true)
    private Integer checkinMethod;

    @Schema(description = "备注", example = "教练签到")
    private String notes;
}