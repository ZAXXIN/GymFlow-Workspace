package com.gymflow.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardStats {

    // 今日数据
    private BigDecimal todayIncome;
    private Integer todayNewMembers;
    private Integer todayCheckins;
    private Integer todayBookings;

    // 本周数据
    private BigDecimal weekIncome;
    private Integer weekNewMembers;

    // 本月数据
    private BigDecimal monthIncome;
    private Integer monthNewMembers;

    // 总览数据
    private Integer totalMembers;
    private Integer totalCoaches;
    private Integer totalCourses;
    private Integer activeCourses;

    // 教练排名数据（需要单独查询）
    // 会员活跃度数据（需要单独查询）
}