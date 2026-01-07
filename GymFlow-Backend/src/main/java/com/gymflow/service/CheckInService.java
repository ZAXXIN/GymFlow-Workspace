package com.gymflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.entity.CheckIn;
import com.gymflow.utils.Result;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface CheckInService {

    Result getCheckInList(Page<CheckIn> page, Long memberId, Long courseId, String status, HttpServletRequest request);

    Result checkIn(String signCode, HttpServletRequest request);

    Result batchCheckIn(Long courseId, String memberIds, HttpServletRequest request);

    Result getMemberCheckIns(String startDate, String endDate, HttpServletRequest request);

    Result getCourseCheckIns(Long id, HttpServletRequest request);

    Result updateCheckInStatus(Long id, String status, String notes, HttpServletRequest request);

    Result getTodayCheckInStats(HttpServletRequest request);

    Result getCheckInTrend(Integer days, HttpServletRequest request);

    Result getActiveMembers(Integer days, HttpServletRequest request);

    Result exportCheckIns(String startDate, String endDate, HttpServletRequest request);
}