package com.gymflow.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Schema(description = "课程查询DTO")
public class CourseQueryDTO {

//    @NotNull(message = "会员ID不能为空")
//    @Schema(description = "会员ID", required = true)
//    private Long memberId;

    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码（默认1）", example = "1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页大小不能小于1")
    @Schema(description = "页大小（默认10）", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "预约状态：0-待上课，1-已签到，2-已完成，3-已取消，4-已过期")
    private Integer bookingStatus;

    @Schema(description = "课程类型：0-私教课，1-团课")
    private Integer courseType;

    @Size(max = 100, message = "课程名称长度不能超过100")
    @Schema(description = "课程名称（模糊查询）", example = "瑜伽")
    private String courseName;

    @Schema(description = "教练ID")
    private Long coachId;

    @Schema(description = "课程开始日期")
    private LocalDate startDate;

    @Schema(description = "课程结束日期")
    private LocalDate endDate;

    @Schema(description = "状态：0-禁用，1-正常")
    private Integer status;
}