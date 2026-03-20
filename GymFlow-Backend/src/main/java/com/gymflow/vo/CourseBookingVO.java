package com.gymflow.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseBookingVO {

    private Long bookingId;
    private Long memberId;
    private String memberName;
    private String memberPhone;
    private String memberNo;
    private String memberAvatar;
    private LocalDateTime bookingTime;
    private Integer bookingStatus;
    private String bookingStatusDesc;
    private LocalDateTime checkinTime;
    private Integer checkinMethod;
    private String checkinMethodDesc;
    private String signCode;  // 签到码
    private LocalDateTime signCodeExpireTime;  // 签到码过期时间
    private String cancellationReason;
    private LocalDateTime cancellationTime;

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
            default: return "未知";
        }
    }
}