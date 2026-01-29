package com.gymflow.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Schema(description = "会员列表查询DTO")
public class MemberQueryDTO {

    // 分页参数
    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码（默认1）", example = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页大小不能小于1")
    @Max(value = 100, message = "页大小不能超过100")
    @Schema(description = "页大小（默认10）", example = "10")
    private Integer pageSize = 10;

    // 查询条件
    @Size(max = 20, message = "会员编号长度不能超过20")
    @Schema(description = "会员编号（模糊查询）", example = "GYM2026")
    private String memberNo;

    @Size(max = 50, message = "真实姓名长度不能超过50")
    @Schema(description = "真实姓名（模糊查询）", example = "张")
    private String realName;

    @Size(max = 11, message = "手机号长度不能超过11")
    @Schema(description = "手机号（模糊查询）", example = "138")
    private String phone;

//    @Schema(description = "注册开始时间", example = "2026-01-01")
//    private LocalDate startDate;
//
//    @Schema(description = "注册结束时间", example = "2026-01-31")
//    private LocalDate endDate;
}