package com.gymflow.service;

import com.gymflow.utils.Result;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface DashboardService {

    Result getDashboardStats(HttpServletRequest request);

    Result getIncomeTrend(Integer days, HttpServletRequest request);

    Result getMemberGrowthTrend(Integer days, HttpServletRequest request);

    Result getCheckInTrend(Integer days, HttpServletRequest request);

    Result getCoachIncomeRanking(Integer limit, HttpServletRequest request);

    Result getHotCourseTypes(Integer limit, HttpServletRequest request);

    Result getMemberActivityDistribution(HttpServletRequest request);

    Result getRealtimeData(HttpServletRequest request);

    Result getMonthlyReport(String month, HttpServletRequest request);

    Result getSalesRanking(Integer limit, String startDate, String endDate, HttpServletRequest request);
}