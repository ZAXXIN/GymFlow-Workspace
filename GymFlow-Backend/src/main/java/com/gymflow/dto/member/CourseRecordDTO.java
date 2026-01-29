package com.gymflow.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CourseRecordDTO {

    private Long courseId;
    private String courseName;
    private String coachName;
    private LocalDateTime courseDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Integer bookingStatus; // 0-待上课，1-已签到，2-已完成，3-已取消
    private LocalDateTime checkinTime;
}