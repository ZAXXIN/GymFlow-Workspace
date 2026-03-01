package com.gymflow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "签到列表视图对象")
public class CheckInListVO {

    @Schema(description = "签到ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "会员手机号")
    private String memberPhone;

    @Schema(description = "会员编号")
    private String memberNo;

    @Schema(description = "签到时间")
    private LocalDateTime checkinTime;

    @Schema(description = "签到方式：0-教练签到，1-前台签到")
    private Integer checkinMethod;

    @Schema(description = "签到方式描述")
    private String checkinMethodDesc;

    @Schema(description = "备注信息")
    private String notes;

    @Schema(description = "是否关联课程预约")
    private Boolean courseCheckIn;

    @Schema(description = "课程预约ID")
    private Long courseBookingId;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程类型：0-私教课，1-团课")
    private Integer courseType;

    @Schema(description = "课程类型描述")
    private String courseTypeDesc;

    @Schema(description = "教练姓名")
    private String coachName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 获取签到方式描述
     */
    public String getCheckinMethodDesc() {
        if (checkinMethod == null) return "未知";
        switch (checkinMethod) {
            case 0: return "教练签到";
            case 1: return "前台签到";
            default: return "未知";
        }
    }

    /**
     * 获取课程类型描述
     */
    public String getCourseTypeDesc() {
        if (courseType == null) return "未知";
        switch (courseType) {
            case 0: return "私教课";
            case 1: return "团课";
            default: return "未知";
        }
    }
}