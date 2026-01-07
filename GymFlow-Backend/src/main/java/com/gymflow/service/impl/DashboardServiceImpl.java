package com.gymflow.service.impl;

import com.gymflow.entity.DashboardStats;
import com.gymflow.mapper.DashboardMapper;
import com.gymflow.service.DashboardService;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DashboardMapper dashboardMapper;

    @Override
    public Result getDashboardStats(HttpServletRequest request) {
        try {
            DashboardStats stats = new DashboardStats();

            // 今日数据
            Double todayIncome = dashboardMapper.selectTodayIncome();
            Integer todayNewMembers = dashboardMapper.selectTodayNewMembers();
            Integer todayCheckins = dashboardMapper.selectTodayCheckIns();
            Integer todayBookings = dashboardMapper.selectTodayBookings();

            stats.setTodayIncome(todayIncome != null ? BigDecimal.valueOf(todayIncome) : BigDecimal.ZERO);
            stats.setTodayNewMembers(todayNewMembers != null ? todayNewMembers : 0);
            stats.setTodayCheckins(todayCheckins != null ? todayCheckins : 0);
            stats.setTodayBookings(todayBookings != null ? todayBookings : 0);

            // 本周数据
            Double weekIncome = dashboardMapper.selectWeekIncome();
            Integer weekNewMembers = dashboardMapper.selectWeekNewMembers();

            stats.setWeekIncome(weekIncome != null ? BigDecimal.valueOf(weekIncome) : BigDecimal.ZERO);
            stats.setWeekNewMembers(weekNewMembers != null ? weekNewMembers : 0);

            // 本月数据
            Double monthIncome = dashboardMapper.selectMonthIncome();
            Integer monthNewMembers = dashboardMapper.selectMonthNewMembers();

            stats.setMonthIncome(monthIncome != null ? BigDecimal.valueOf(monthIncome) : BigDecimal.ZERO);
            stats.setMonthNewMembers(monthNewMembers != null ? monthNewMembers : 0);

            // 总览数据
            Integer totalMembers = dashboardMapper.selectTotalMembers();
            Integer totalCoaches = dashboardMapper.selectTotalCoaches();
            Integer totalCourses = dashboardMapper.selectTotalCourses();
            Integer activeCourses = dashboardMapper.selectActiveCourses();

            stats.setTotalMembers(totalMembers != null ? totalMembers : 0);
            stats.setTotalCoaches(totalCoaches != null ? totalCoaches : 0);
            stats.setTotalCourses(totalCourses != null ? totalCourses : 0);
            stats.setActiveCourses(activeCourses != null ? activeCourses : 0);

            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("获取统计失败：" + e.getMessage());
        }
    }

    @Override
    public Result getIncomeTrend(Integer days, HttpServletRequest request) {
        try {
            return Result.success(dashboardMapper.selectIncomeTrend(days));
        } catch (Exception e) {
            return Result.error("获取收入趋势失败：" + e.getMessage());
        }
    }

    @Override
    public Result getMemberGrowthTrend(Integer days, HttpServletRequest request) {
        try {
            return Result.success(dashboardMapper.selectMemberGrowthTrend(days));
        } catch (Exception e) {
            return Result.error("获取会员增长趋势失败：" + e.getMessage());
        }
    }

    @Override
    public Result getCheckInTrend(Integer days, HttpServletRequest request) {
        try {
            // 使用CheckInMapper中的方法
            // return Result.success(checkInMapper.selectCheckInStatsByDay(days));
            return Result.success("获取签到趋势功能待实现");
        } catch (Exception e) {
            return Result.error("获取签到趋势失败：" + e.getMessage());
        }
    }

    @Override
    public Result getCoachIncomeRanking(Integer limit, HttpServletRequest request) {
        try {
            return Result.success(dashboardMapper.selectCoachIncomeRanking(limit));
        } catch (Exception e) {
            return Result.error("获取教练排名失败：" + e.getMessage());
        }
    }

    @Override
    public Result getHotCourseTypes(Integer limit, HttpServletRequest request) {
        try {
            return Result.success(dashboardMapper.selectHotCourseTypes(limit));
        } catch (Exception e) {
            return Result.error("获取热门课程失败：" + e.getMessage());
        }
    }

    @Override
    public Result getMemberActivityDistribution(HttpServletRequest request) {
        try {
            return Result.success(dashboardMapper.selectMemberActivityDistribution());
        } catch (Exception e) {
            return Result.error("获取会员活跃度失败：" + e.getMessage());
        }
    }

    @Override
    public Result getRealtimeData(HttpServletRequest request) {
        try {
            // 获取实时数据：今日新增、今日收入、在线人数等
            DashboardStats realtimeStats = new DashboardStats();

            Double todayIncome = dashboardMapper.selectTodayIncome();
            Integer todayNewMembers = dashboardMapper.selectTodayNewMembers();
            Integer todayCheckins = dashboardMapper.selectTodayCheckIns();

            realtimeStats.setTodayIncome(todayIncome != null ? BigDecimal.valueOf(todayIncome) : BigDecimal.ZERO);
            realtimeStats.setTodayNewMembers(todayNewMembers != null ? todayNewMembers : 0);
            realtimeStats.setTodayCheckins(todayCheckins != null ? todayCheckins : 0);

            return Result.success(realtimeStats);
        } catch (Exception e) {
            return Result.error("获取实时数据失败：" + e.getMessage());
        }
    }

    @Override
    public Result getMonthlyReport(String month, HttpServletRequest request) {
        // TODO: 实现月度报告逻辑
        return Result.success("月度报告功能待实现");
    }

    @Override
    public Result getSalesRanking(Integer limit, String startDate, String endDate, HttpServletRequest request) {
        // TODO: 实现销售排行榜逻辑
        return Result.success("销售排行榜功能待实现");
    }
}