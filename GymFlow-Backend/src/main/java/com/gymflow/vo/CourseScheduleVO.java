package com.gymflow.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class CourseScheduleVO {

    private Long scheduleId;
    private Long courseId;
    private String courseName;
    private String courseType;
    private Long coachId;
    private String coachName;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private Integer remainingSlots;
    private String status;
    private String notes;

    // 预约信息
    private List<CourseBookingVO> bookings;
}