package com.gymflow.controller.mini;

import com.gymflow.common.Result;
import com.gymflow.dto.coach.CoachFullDTO;
import com.gymflow.dto.member.HealthRecordDTO;
import com.gymflow.dto.member.MemberFullDTO;
import com.gymflow.dto.mini.*;
import com.gymflow.service.mini.MiniCoachService;
import com.gymflow.utils.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mini/coach")
@Tag(name = "小程序-教练端", description = "教练端专用接口")
@Validated
@RequiredArgsConstructor
public class MiniCoachController {

    private final MiniCoachService miniCoachService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 从token获取当前教练ID
     */
    private Long getCurrentCoachId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtTokenUtil.getUserIdFromToken(token);
    }

    @GetMapping("/my-info")
    @Operation(summary = "获取当前教练信息")
    public Result<CoachFullDTO> getMyInfo(HttpServletRequest request) {
        Long coachId = getCurrentCoachId(request);
        log.info("获取教练信息，当前登录用户ID：{}", coachId);
        CoachFullDTO coachInfo = miniCoachService.getMyInfo(coachId);
        return Result.success("获取成功", coachInfo);
    }

    @GetMapping("/my-schedule")
    @Operation(summary = "获取我的课表")
    public Result<List<MiniScheduleDTO>> getMySchedule(
            HttpServletRequest request,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        Long coachId = getCurrentCoachId(request);
        List<MiniScheduleDTO> schedule = miniCoachService.getMySchedule(coachId, date);
        return Result.success("获取成功", schedule);
    }

    @GetMapping("/course-students/{scheduleId}")
    @Operation(summary = "获取课程学员列表")
    public Result<List<MiniCourseStudentDTO>> getCourseStudents(
            HttpServletRequest request,
            @PathVariable @NotNull Long scheduleId) {
        Long coachId = getCurrentCoachId(request);
        List<MiniCourseStudentDTO> students = miniCoachService.getCourseStudents(coachId, scheduleId);
        return Result.success("获取成功", students);
    }

    @GetMapping("/member-detail/{memberId}")
    @Operation(summary = "获取会员详情（教练视角）")
    public Result<MemberFullDTO> getMemberDetail(
            HttpServletRequest request,
            @PathVariable @NotNull Long memberId) {
        Long coachId = getCurrentCoachId(request);
        MemberFullDTO memberDetail = miniCoachService.getMemberDetail(coachId, memberId);
        return Result.success("获取成功", memberDetail);
    }

    @PostMapping("/add-health-record/{memberId}")
    @Operation(summary = "为会员添加健康档案")
    public Result<Void> addHealthRecord(
            HttpServletRequest request,
            @PathVariable @NotNull Long memberId,
            @Valid @RequestBody HealthRecordDTO healthRecordDTO) {
        Long coachId = getCurrentCoachId(request);
        miniCoachService.addHealthRecord(coachId, memberId, healthRecordDTO);
        return Result.success("添加成功");
    }

    @GetMapping("/finance/stats")
    @Operation(summary = "获取财务统计数据")
    public Result<MiniFinanceStatsDTO> getFinanceStats(
            HttpServletRequest request,
            @RequestParam(defaultValue = "month") String period) {
        Long coachId = getCurrentCoachId(request);
        MiniFinanceStatsDTO stats = miniCoachService.getFinanceStats(coachId, period);
        return Result.success("获取成功", stats);
    }

    @GetMapping("/current-reminder")
    @Operation(summary = "获取当前时段提醒")
    public Result<MiniReminderDTO> getCurrentReminder(HttpServletRequest request) {
        Long coachId = getCurrentCoachId(request);
        MiniReminderDTO reminder = miniCoachService.getCurrentReminder(coachId);
        return Result.success("获取成功", reminder);
    }

    @PostMapping("/modify-password")
    @Operation(summary = "修改密码")
    public Result<Void> modifyPassword(HttpServletRequest request,
                                       @RequestParam @NotNull String oldPassword,
                                       @RequestParam @NotNull String newPassword) {
        Long coachId = getCurrentCoachId(request);
        miniCoachService.modifyPassword(coachId, oldPassword, newPassword);
        return Result.success("修改成功");
    }
}