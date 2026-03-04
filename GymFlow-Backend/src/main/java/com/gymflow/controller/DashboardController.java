package com.gymflow.controller;

import com.gymflow.common.Result;
import com.gymflow.common.annotation.PreAuthorize;
import com.gymflow.dto.dashboard.*;
import com.gymflow.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/dashboard")
@Tag(name = "仪表盘管理", description = "仪表盘数据统计相关接口")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "获取仪表盘统计数据")
    @PreAuthorize("dashboard:view")  // 仪表盘查看权限
    public Result<DashboardStatsDTO> getDashboardStats() {
        try {
            DashboardStatsDTO stats = dashboardService.getDashboardStats();
            return Result.success("获取成功", stats);
        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/revenue/trend")
    @Operation(summary = "获取营收趋势数据")
    @PreAuthorize("dashboard:view")
    public Result<RevenueTrendDTO> getRevenueTrend(
            @RequestParam(defaultValue = "week") String period,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            RevenueTrendDTO trend = dashboardService.getRevenueTrend(period, startDate, endDate);
            return Result.success("获取成功", trend);
        } catch (Exception e) {
            log.error("获取营收趋势数据失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/courses/category")
    @Operation(summary = "获取课程分类分布")
    @PreAuthorize("dashboard:view")
    public Result<List<CourseCategoryStatsDTO>> getCourseCategoryStats() {
        try {
            List<CourseCategoryStatsDTO> stats = dashboardService.getCourseCategoryStats();
            return Result.success("获取成功", stats);
        } catch (Exception e) {
            log.error("获取课程分类分布失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/courses/today")
    @Operation(summary = "获取今日课程列表")
    @PreAuthorize("dashboard:view")
    public Result<List<TodayCourseDTO>> getTodayCourses() {
        try {
            List<TodayCourseDTO> courses = dashboardService.getTodayCourses();
            return Result.success("获取成功", courses);
        } catch (Exception e) {
            log.error("获取今日课程列表失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/quick-stats")
    @Operation(summary = "获取快速统计数据（用于快速操作区域）")
    @PreAuthorize("dashboard:view")
    public Result<QuickStatsDTO> getQuickStats() {
        try {
            QuickStatsDTO stats = dashboardService.getQuickStats();
            return Result.success("获取成功", stats);
        } catch (Exception e) {
            log.error("获取快速统计数据失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }
}