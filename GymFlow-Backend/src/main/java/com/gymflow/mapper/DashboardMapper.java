package com.gymflow.mapper;

import com.gymflow.entity.DashboardStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    /**
     * 获取仪表盘统计数据
     */
    DashboardStats selectDashboardStats();

    /**
     * 查询今日收入
     */
    @Select("SELECT COALESCE(SUM(actual_amount), 0) as amount FROM `order` " +
            "WHERE DATE(payment_time) = CURDATE() AND payment_status = 'PAID'")
    Double selectTodayIncome();

    /**
     * 查询今日新增会员
     */
    @Select("SELECT COUNT(*) as count FROM member WHERE DATE(create_time) = CURDATE()")
    Integer selectTodayNewMembers();

    /**
     * 查询今日签到数
     */
    @Select("SELECT COUNT(*) as count FROM check_in WHERE DATE(checkin_time) = CURDATE() AND status = 'SUCCESS'")
    Integer selectTodayCheckIns();

    /**
     * 查询今日预约数
     */
    @Select("SELECT COUNT(*) as count FROM course WHERE course_date = CURDATE() AND status = 'UPCOMING'")
    Integer selectTodayBookings();

    /**
     * 查询本周收入
     */
    @Select("SELECT COALESCE(SUM(actual_amount), 0) as amount FROM `order` " +
            "WHERE YEARWEEK(payment_time) = YEARWEEK(NOW()) AND payment_status = 'PAID'")
    Double selectWeekIncome();

    /**
     * 查询本周新增会员
     */
    @Select("SELECT COUNT(*) as count FROM member WHERE YEARWEEK(create_time) = YEARWEEK(NOW())")
    Integer selectWeekNewMembers();

    /**
     * 查询本月收入
     */
    @Select("SELECT COALESCE(SUM(actual_amount), 0) as amount FROM `order` " +
            "WHERE DATE_FORMAT(payment_time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') AND payment_status = 'PAID'")
    Double selectMonthIncome();

    /**
     * 查询本月新增会员
     */
    @Select("SELECT COUNT(*) as count FROM member WHERE DATE_FORMAT(create_time, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m')")
    Integer selectMonthNewMembers();

    /**
     * 查询总会员数
     */
    @Select("SELECT COUNT(*) as count FROM member")
    Integer selectTotalMembers();

    /**
     * 查询总教练数
     */
    @Select("SELECT COUNT(*) as count FROM coach WHERE status = 1")
    Integer selectTotalCoaches();

    /**
     * 查询总课程数
     */
    @Select("SELECT COUNT(*) as count FROM course")
    Integer selectTotalCourses();

    /**
     * 查询活跃课程数（今日及未来的）
     */
    @Select("SELECT COUNT(*) as count FROM course WHERE course_date >= CURDATE() AND status = 'UPCOMING'")
    Integer selectActiveCourses();

    /**
     * 查询收入趋势（最近30天）
     */
    List<Map<String, Object>> selectIncomeTrend(@Param("days") Integer days);

    /**
     * 查询会员增长趋势（最近30天）
     */
    List<Map<String, Object>> selectMemberGrowthTrend(@Param("days") Integer days);

    /**
     * 查询教练收入排名
     */
    List<Map<String, Object>> selectCoachIncomeRanking(@Param("limit") Integer limit);

    /**
     * 查询热门课程类型
     */
    List<Map<String, Object>> selectHotCourseTypes(@Param("limit") Integer limit);

    /**
     * 查询会员活跃度分布
     */
    List<Map<String, Object>> selectMemberActivityDistribution();
}