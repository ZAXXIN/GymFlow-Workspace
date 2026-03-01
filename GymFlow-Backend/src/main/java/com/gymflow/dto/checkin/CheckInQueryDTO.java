package com.gymflow.dto.checkin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Schema(description = "签到查询DTO")
public class CheckInQueryDTO {

    // 分页参数
    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码（默认1）", example = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页大小不能小于1")
    @Max(value = 100, message = "页大小不能超过100")
    @Schema(description = "页大小（默认10）", example = "10")
    private Integer pageSize = 10;

    // 查询条件
    @Schema(description = "会员ID", example = "1001")
    private Long memberId;

    @Size(max = 50, message = "会员姓名长度不能超过50")
    @Schema(description = "会员姓名（模糊查询）", example = "张三")
    private String memberName;

    @Size(max = 11, message = "手机号长度不能超过11")
    @Schema(description = "会员手机号（模糊查询）", example = "138")
    private String memberPhone;

    @Min(value = 0, message = "签到方式值只能是0或1")
    @Max(value = 1, message = "签到方式值只能是0或1")
    @Schema(description = "签到方式：0-教练签到，1-前台签到")
    private Integer checkinMethod;

    @Schema(description = "是否关联课程预约")
    private Boolean hasCourseBooking;

    @Schema(description = "签到开始日期", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "签到结束日期", example = "2024-01-31")
    private LocalDate endDate;
}