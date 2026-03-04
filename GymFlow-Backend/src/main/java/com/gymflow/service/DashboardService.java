package com.gymflow.service;

import com.gymflow.dto.dashboard.*;
import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    /**
     * 获取仪表盘统计数据
     */
    DashboardStatsDTO getDashboardStats();

    /**
     * 获取营收趋势数据
     * @param period 统计周期：week-周，month-月，year-年
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     */
    RevenueTrendDTO getRevenueTrend(String period, LocalDate startDate, LocalDate endDate);

    /**
     * 获取课程分类分布
     */
    List<CourseCategoryStatsDTO> getCourseCategoryStats();

    /**
     * 获取今日课程列表
     */
    List<TodayCourseDTO> getTodayCourses();

    /**
     * 获取快速统计数据
     */
    QuickStatsDTO getQuickStats();
}