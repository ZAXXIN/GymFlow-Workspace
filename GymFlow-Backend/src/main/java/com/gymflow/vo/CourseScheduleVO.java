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
    private Integer courseType;
    private String courseTypeDesc;
    private Long coachId;
    private String coachName;
    private LocalDate scheduleDate;
    private LocalTime startTime;
    private LocalTime endTime;
    // 移除 location 字段
    private Integer maxCapacity;
    private Integer currentEnrollment;
    private Integer remainingSlots;
    private Integer status;
    private String statusDesc;
    private String notes;

    // 预约信息 - 返回完整的预约列表
    private List<CourseBookingVO> bookings;

    // 计算剩余名额
    public Integer getRemainingSlots() {
        if (maxCapacity == null || currentEnrollment == null) return 0;
        return maxCapacity - currentEnrollment;
    }

    // 获取状态描述
    public String getStatusDesc() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "已取消";
            case 1: return "正常";
            default: return "未知";
        }
    }
}