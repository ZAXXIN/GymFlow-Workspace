package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "预约详情DTO")
public class MiniBookingDetailDTO {

    @Schema(description = "预约ID")
    private Long bookingId;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程类型：0-私教课，1-团课")
    private Integer courseType;

    @Schema(description = "课程类型描述")
    private String courseTypeDesc;

    @Schema(description = "课程描述")
    private String courseDescription;

    @Schema(description = "教练ID")
    private Long coachId;

    @Schema(description = "教练姓名")
    private String coachName;

    @Schema(description = "教练手机号")
    private String coachPhone;

    @Schema(description = "课程日期")
    private String courseDate;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "上课地点")
    private String location;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "预约时间")
    private LocalDateTime bookingTime;

    @Schema(description = "预约状态")
    private Integer bookingStatus;

    @Schema(description = "预约状态描述")
    private String bookingStatusDesc;

    @Schema(description = "签到时间")
    private LocalDateTime checkinTime;

    @Schema(description = "签到码信息")
    private MiniCheckinCodeDTO checkinCode;

    @Schema(description = "取消原因")
    private String cancellationReason;

    @Schema(description = "取消时间")
    private LocalDateTime cancellationTime;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "会员手机号")
    private String memberPhone;

    @Schema(description = "会员编号")
    private String memberNo;
}