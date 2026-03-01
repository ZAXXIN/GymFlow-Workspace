package com.gymflow.dto.checkin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Schema(description = "签到详情DTO")
public class CheckInDetailDTO {

    @Schema(description = "签到ID")
    private Long id;

    @NotNull(message = "会员ID不能为空")
    @Schema(description = "会员ID", required = true)
    private Long memberId;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "会员手机号")
    private String memberPhone;

    @Schema(description = "会员编号")
    private String memberNo;

    @NotNull(message = "签到时间不能为空")
    @Schema(description = "签到时间", required = true)
    private LocalDateTime checkinTime;

    @NotNull(message = "签到方式不能为空")
    @Schema(description = "签到方式：0-教练签到，1-前台签到", required = true)
    private Integer checkinMethod;

    @Schema(description = "签到方式描述")
    private String checkinMethodDesc;

    @Size(max = 200, message = "备注长度不能超过200")
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
}