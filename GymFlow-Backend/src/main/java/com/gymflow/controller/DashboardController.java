package com.gymflow.controller;

import com.gymflow.service.DashboardService;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取仪表盘核心统计数据
     */
    @GetMapping("/stats")
    public Result getDashboardStats(HttpServletRequest request) {
        return dashboardService.getDashboardStats(request);
    }

    /**
     * 获取收入趋势
     */
    @GetMapping("/income-trend")
    public Result getIncomeTrend(@RequestParam(defaultValue = "30") Integer days,
                                 HttpServletRequest request) {
        return dashboardService.getIncomeTrend(days, request);
    }

    /**
     * 获取会员增长趋势
     */
    @GetMapping("/member-growth")
    public Result getMemberGrowthTrend(@RequestParam(defaultValue = "30") Integer days,
                                       HttpServletRequest request) {
        return dashboardService.getMemberGrowthTrend(days, request);
    }

    /**
     * 获取签到趋势
     */
    @GetMapping("/checkin-trend")
    public Result getCheckInTrend(@RequestParam(defaultValue = "30") Integer days,
                                  HttpServletRequest request) {
        return dashboardService.getCheckInTrend(days, request);
    }

    /**
     * 获取教练收入排名
     */
    @GetMapping("/coach-ranking")
    public Result getCoachIncomeRanking(@RequestParam(defaultValue = "10") Integer limit,
                                        HttpServletRequest request) {
        return dashboardService.getCoachIncomeRanking(limit, request);
    }

    /**
     * 获取热门课程类型
     */
    @GetMapping("/hot-courses")
    public Result getHotCourseTypes(@RequestParam(defaultValue = "5") Integer limit,
                                    HttpServletRequest request) {
        return dashboardService.getHotCourseTypes(limit, request);
    }

    /**
     * 获取会员活跃度分布
     */
    @GetMapping("/member-activity")
    public Result getMemberActivityDistribution(HttpServletRequest request) {
        return dashboardService.getMemberActivityDistribution(request);
    }

    /**
     * 获取今日实时数据
     */
    @GetMapping("/realtime")
    public Result getRealtimeData(HttpServletRequest request) {
        return dashboardService.getRealtimeData(request);
    }

    /**
     * 获取月度统计报告
     */
    @GetMapping("/monthly-report")
    public Result getMonthlyReport(@RequestParam(required = false) String month,
                                   HttpServletRequest request) {
        return dashboardService.getMonthlyReport(month, request);
    }

    /**
     * 获取销售排行榜
     */
    @GetMapping("/sales-ranking")
    public Result getSalesRanking(@RequestParam(defaultValue = "10") Integer limit,
                                  @RequestParam(required = false) String startDate,
                                  @RequestParam(required = false) String endDate,
                                  HttpServletRequest request) {
        return dashboardService.getSalesRanking(limit, startDate, endDate, request);
    }
}