package com.gymflow.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourseBookingVO {

    private Long bookingId;
    private Long memberId;
    private String memberName;
    private String memberPhone;
    private String memberAvatar;
    private LocalDateTime bookingTime;
    private Integer bookingStatus;
    private String bookingStatusDesc;
    private LocalDateTime checkinTime;
    private String checkinMethod;
    private String cancellationReason;
}