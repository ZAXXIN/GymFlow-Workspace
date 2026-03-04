package com.gymflow.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DashboardStats {

    // 总会员数
    private Integer totalMembers;

    // 总教练数
    private Integer totalCoaches;

    // 总课程数
    private Integer totalCourses;

    // 今日营收
    private BigDecimal todayRevenue;

    // 今日签到数
    private Integer todayCheckIns;

    // 本月营收
    private BigDecimal monthRevenue;

    // 本月新增会员
    private Integer monthNewMembers;

    // 本月签到数
    private Integer monthCheckIns;

    // 上月营收
    private BigDecimal lastMonthRevenue;

    // 上月新增会员
    private Integer lastMonthNewMembers;

    // 昨日营收
    private BigDecimal yesterdayRevenue;

    // 昨日签到数
    private Integer yesterdayCheckIns;
}