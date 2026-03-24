package com.gymflow.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CourseListVO {

    private Long id;
    private Integer courseType;
    private String courseTypeDesc;
    private String courseName;
    private String notice;
    private String description;
    private List<String> coachNames;  // 绑定教练姓名列表
//    private Integer maxCapacity;
    private Integer duration;
    private Integer sessionCost;  // 预约消耗课时数
    private Integer status;
    private String statusDesc;

    // 统计信息
    private Integer totalSchedules;  // 总排课数
    private Integer totalBookings;   // 总预约数

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

    /**
     * 获取状态描述
     */
    public String getStatusDesc() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "禁用";
            case 1: return "正常";
            default: return "未知";
        }
    }
}