package com.gymflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.entity.CheckIn;
import com.gymflow.service.CheckInService;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    /**
     * 获取签到记录列表（分页）
     */
    @GetMapping("/list")
    public Result getCheckInList(@RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam(required = false) Long memberId,
                                 @RequestParam(required = false) Long courseId,
                                 @RequestParam(required = false) String status,
                                 HttpServletRequest request) {
        Page<CheckIn> page = new Page<>(pageNum, pageSize);
        return checkInService.getCheckInList(page, memberId, courseId, status, request);
    }

    /**
     * 会员签到
     */
    @PostMapping("/sign")
    public Result checkIn(@RequestParam String signCode, HttpServletRequest request) {
        return checkInService.checkIn(signCode, request);
    }

    /**
     * 教练批量签到
     */
    @PostMapping("/batch")
    public Result batchCheckIn(@RequestParam Long courseId,
                               @RequestBody String memberIds,
                               HttpServletRequest request) {
        return checkInService.batchCheckIn(courseId, memberIds, request);
    }

    /**
     * 获取会员签到记录
     */
    @GetMapping("/member")
    public Result getMemberCheckIns(@RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate,
                                    HttpServletRequest request) {
        return checkInService.getMemberCheckIns(startDate, endDate, request);
    }

    /**
     * 获取课程签到记录
     */
    @GetMapping("/course/{id}")
    public Result getCourseCheckIns(@PathVariable Long id, HttpServletRequest request) {
        return checkInService.getCourseCheckIns(id, request);
    }

    /**
     * 更新签到状态
     */
    @PutMapping("/status/{id}")
    public Result updateCheckInStatus(@PathVariable Long id,
                                      @RequestParam String status,
                                      @RequestParam(required = false) String notes,
                                      HttpServletRequest request) {
        return checkInService.updateCheckInStatus(id, status, notes, request);
    }

    /**
     * 获取今日签到统计
     */
    @GetMapping("/today/stats")
    public Result getTodayCheckInStats(HttpServletRequest request) {
        return checkInService.getTodayCheckInStats(request);
    }

    /**
     * 获取签到趋势统计
     */
    @GetMapping("/trend")
    public Result getCheckInTrend(@RequestParam(defaultValue = "7") Integer days,
                                  HttpServletRequest request) {
        return checkInService.getCheckInTrend(days, request);
    }

    /**
     * 获取活跃会员
     */
    @GetMapping("/active-members")
    public Result getActiveMembers(@RequestParam(defaultValue = "7") Integer days,
                                   HttpServletRequest request) {
        return checkInService.getActiveMembers(days, request);
    }

    /**
     * 导出签到记录
     */
    @GetMapping("/export")
    public Result exportCheckIns(@RequestParam(required = false) String startDate,
                                 @RequestParam(required = false) String endDate,
                                 HttpServletRequest request) {
        return checkInService.exportCheckIns(startDate, endDate, request);
    }
}