package com.gymflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gymflow.enums.CourseStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CourseVO {

    private Long id;

    private String courseType;

    private String courseName;

    private String description;

    private Long coachId;

    private String coachName;

    private String coachAvatar;

    private Integer maxCapacity;

    private Integer currentEnrollment;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate courseDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private Integer duration;

    private BigDecimal price;

    private String location;

    private CourseStatus status;

    private String signCode;

    private Boolean isBooked;

    private Integer remainingSlots;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;
}