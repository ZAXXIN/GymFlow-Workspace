package com.gymflow.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "今日课程DTO")
public class TodayCourseDTO {

    @Schema(description = "课程ID")
    private Long id;

    @Schema(description = "课程编号")
    private String courseNo;

    @Schema(description = "课程名称")
    private String name;

    @Schema(description = "教练ID")
    private Long coachId;

    @Schema(description = "教练姓名")
    private String coachName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "课程容量")
    private Integer capacity;

    @Schema(description = "当前预约数")
    private Integer currentBookings;

    @Schema(description = "课程状态编码")
    private String status;

    @Schema(description = "课程状态描述")
    private String statusText;
}