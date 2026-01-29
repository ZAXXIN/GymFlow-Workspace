package com.gymflow.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Schema(description = "会员课程记录DTO")
public class CourseRecordDTO {

    @NotNull(message = "课程ID不能为空")
    @Positive(message = "课程ID必须为正数")
    @Schema(description = "课程ID", example = "1001", required = true)
    private Long courseId;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过100")
    @Schema(description = "课程名称", example = "团课-瑜伽基础", required = true)
    private String courseName;

    @Size(max = 50, message = "教练姓名长度不能超过50")
    @Schema(description = "授课教练姓名", example = "王教练")
    private String coachName;

    @NotNull(message = "课程日期不能为空")
    @Schema(description = "课程日期", example = "2026-01-28T00:00:00", required = true)
    private LocalDateTime courseDate;

    @NotNull(message = "课程开始时间不能为空")
    @Schema(description = "课程开始时间", example = "2026-01-28T14:00:00", required = true)
    private LocalDateTime startTime;

    @NotNull(message = "课程结束时间不能为空")
    @Schema(description = "课程结束时间", example = "2026-01-28T15:30:00", required = true)
    private LocalDateTime endTime;

    @Size(max = 200, message = "上课地点长度不能超过200")
    @Schema(description = "上课地点", example = "健身房2楼瑜伽室")
    private String location;

    @NotNull(message = "预约状态不能为空")
    @Min(value = 0, message = "预约状态值只能是0-3")
    @Max(value = 3, message = "预约状态值只能是0-3")
    @Schema(description = "预约状态：0-待上课，1-已签到，2-已完成，3-已取消", example = "1", required = true)
    private Integer bookingStatus;

    @Schema(description = "课程签到时间（未签到则为null）", example = "2026-01-28T13:55:00")
    private LocalDateTime checkinTime;
}