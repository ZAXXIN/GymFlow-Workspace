package com.gymflow.dto.coach;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Data
@Schema(description = "教练列表查询DTO")
public class CoachQueryDTO {

    // 分页参数
    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码（默认1）", example = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页大小不能小于1")
    @Max(value = 100, message = "页大小不能超过100")
    @Schema(description = "页大小（默认10）", example = "10")
    private Integer pageSize = 10;

    // 查询条件
    @Size(max = 50, message = "教练姓名长度不能超过50")
    @Schema(description = "教练姓名（模糊查询）", example = "张")
    private String realName;

    @Size(max = 11, message = "手机号长度不能超过11")
    @Schema(description = "手机号（模糊查询）", example = "138")
    private String phone;

    @Size(max = 100, message = "专业特长长度不能超过100")
    @Schema(description = "专业特长（模糊查询）", example = "瑜伽")
    private String specialty;

    @Schema(description = "状态：0-离职，1-在职", example = "1")
    private Integer status;

    @Min(value = 0, message = "从业年限不能小于0")
    @Schema(description = "最小从业年限", example = "3")
    private Integer minExperience;

    @Min(value = 0, message = "从业年限不能小于0")
    @Schema(description = "最大从业年限", example = "10")
    private Integer maxExperience;
}