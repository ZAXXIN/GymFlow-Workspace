package com.gymflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Schema(description = "课程预约VO")
public class CourseBookingVO {

    @Schema(description = "预约ID")
    private Long bookingId;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "会员手机号")
    private String memberPhone;

    @Schema(description = "会员编号")
    private String memberNo;

    @Schema(description = "会员头像")
    private String memberAvatar;

    @Schema(description = "排课ID")
    private Long scheduleId;

    @Schema(description = "课程ID")
    private Long courseId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程类型：0-私教课，1-团课")
    private Integer courseType;

    @Schema(description = "教练ID")
    private Long coachId;

    @Schema(description = "教练姓名")
    private String coachName;

    @Schema(description = "上课日期")
    private LocalDate scheduleDate;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "预约时间")
    private LocalDateTime bookingTime;

    @Schema(description = "预约状态：0-待上课，1-已签到，2-已完成，3-已取消，4-已过期")
    private Integer bookingStatus;

    @Schema(description = "签到时间")
    private LocalDateTime checkinTime;

    @Schema(description = "签到方式：0-教练签到，1-前台签到")
    private Integer checkinMethod;

    @Schema(description = "签到码")
    private String signCode;

    @Schema(description = "签到码过期时间")
    private LocalDateTime signCodeExpireTime;

    @Schema(description = "取消原因")
    private String cancellationReason;

    @Schema(description = "取消时间")
    private LocalDateTime cancellationTime;

    @Schema(description = "消耗课时数")
    private Integer sessionCost;

    // 获取签到方式描述
    public String getCheckinMethodDesc() {
        if (checkinMethod == null) return "未签到";
        switch (checkinMethod) {
            case 0: return "教练签到";
            case 1: return "前台签到";
            default: return "未知";
        }
    }

    // 获取预约状态描述
    public String getBookingStatusDesc() {
        if (bookingStatus == null) return "未知";
        switch (bookingStatus) {
            case 0: return "待上课";
            case 1: return "已签到";
            case 2: return "已完成";
            case 3: return "已取消";
            case 4: return "已过期";
            default: return "未知";
        }
    }

    // 获取课程类型描述
    public String getCourseTypeDesc() {
        if (courseType == null) return "未知";
        switch (courseType) {
            case 0: return "私教课";
            case 1: return "团课";
            default: return "未知";
        }
    }
}