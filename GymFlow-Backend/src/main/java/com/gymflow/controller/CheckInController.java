package com.gymflow.controller;

import com.gymflow.common.Result;
import com.gymflow.common.annotation.PreAuthorize;
import com.gymflow.dto.checkin.CheckInQueryDTO;
import com.gymflow.dto.checkin.CheckInStatsDTO;
import com.gymflow.dto.checkin.CheckInReportDTO;
import com.gymflow.service.CheckInService;
import com.gymflow.vo.CheckInListVO;
import com.gymflow.vo.CheckInDetailVO;
import com.gymflow.vo.PageResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/checkin")
@Tag(name = "签到管理", description = "签到管理相关接口")
@Validated
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping("/list")
    @Operation(summary = "分页查询签到列表")
    @PreAuthorize("checkIn:view")  // 查看权限（老板和前台都有）
    public Result<PageResultVO<CheckInListVO>> getCheckInList(@Valid @RequestBody CheckInQueryDTO queryDTO) {
        PageResultVO<CheckInListVO> result = checkInService.getCheckInList(queryDTO);
        return Result.success("查询成功", result);
    }

    @GetMapping("/detail/{checkInId}")
    @Operation(summary = "获取签到详情")
    @PreAuthorize("checkIn:detail")  // 查看详情权限（老板和前台都有）
    public Result<CheckInDetailVO> getCheckInDetail(
            @Parameter(description = "签到ID", required = true)
            @PathVariable @NotNull Long checkInId) {
        CheckInDetailVO detail = checkInService.getCheckInDetail(checkInId);
        return Result.success("查询成功", detail);
    }

    @PostMapping("/member/{memberId}")
    @Operation(summary = "会员签到")
    @PreAuthorize("checkIn:member:add")  // 会员签到权限（老板和前台都有）
    public Result<Void> memberCheckIn(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId,
            @RequestParam @NotNull Integer checkinMethod,
            @RequestParam(required = false) String notes) {
        checkInService.memberCheckIn(memberId, checkinMethod, notes);
        return Result.success("签到成功");
    }

    @PostMapping("/course/{bookingId}")
    @Operation(summary = "课程签到（支持扫码和数字码）")
    @PreAuthorize("checkIn:course:add")
    public Result<Void> courseCheckIn(
            @Parameter(description = "预约ID", required = true)
            @PathVariable @NotNull Long bookingId,
            @Parameter(description = "签到方式：0-教练签到，1-前台签到", required = true)
            @RequestParam @NotNull Integer checkinMethod,
            @Parameter(description = "备注")
            @RequestParam(required = false) String notes,
            @Parameter(description = "数字码（可选，用于PC端核销）")
            @RequestParam(required = false) String checkinCode) {

        // 如果有数字码，优先验证数字码
        if (checkinCode != null && !checkinCode.isEmpty()) {
            checkInService.courseCheckInByCode(bookingId, checkinCode, checkinMethod, notes);
        } else {
            checkInService.courseCheckIn(bookingId, checkinMethod, notes);
        }
        return Result.success("课程签到成功");
    }

    // ========== 通过数字码核销接口（PC端专用） ==========
    @PostMapping("/verify-code")
    @Operation(summary = "通过数字码核销课程")
    @PreAuthorize("checkIn:verify")
    public Result<Void> verifyByCode(
            @Parameter(description = "数字码", required = true)
            @RequestParam @NotNull String checkinCode,
            @Parameter(description = "签到方式", required = true)
            @RequestParam @NotNull Integer checkinMethod,
            @Parameter(description = "备注")
            @RequestParam(required = false) String notes) {

        checkInService.verifyByCode(checkinCode, checkinMethod, notes);
        return Result.success("核销成功");
    }

    @PutMapping("/update/{checkInId}")
    @Operation(summary = "更新签到信息")
    @PreAuthorize("checkIn:edit")  // 编辑权限（老板和前台都有）
    public Result<Void> updateCheckIn(
            @Parameter(description = "签到ID", required = true)
            @PathVariable @NotNull Long checkInId,
            @RequestParam String notes) {
        checkInService.updateCheckIn(checkInId, notes);
        return Result.success("更新签到信息成功");
    }

    @DeleteMapping("/delete/{checkInId}")
    @Operation(summary = "删除签到记录")
    @PreAuthorize("checkIn:delete")  // 删除权限（只有老板有）
    public Result<Void> deleteCheckIn(
            @Parameter(description = "签到ID", required = true)
            @PathVariable @NotNull Long checkInId) {
        checkInService.deleteCheckIn(checkInId);
        return Result.success("删除签到记录成功");
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除签到记录")
    @PreAuthorize("checkIn:delete")  // 删除权限（只有老板有）
    public Result<Void> batchDeleteCheckIns(@RequestBody List<Long> checkInIds) {
        checkInService.batchDeleteCheckIns(checkInIds);
        return Result.success("批量删除成功");
    }

    @PostMapping("/member-list/{memberId}")
    @Operation(summary = "获取会员签到记录")
    @PreAuthorize("checkIn:view")  // 查看权限（老板和前台都有）
    public Result<PageResultVO<CheckInListVO>> getMemberCheckIns(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId,
            @Valid @RequestBody CheckInQueryDTO queryDTO) {
        PageResultVO<CheckInListVO> result = checkInService.getMemberCheckIns(memberId, queryDTO);
        return Result.success("查询成功", result);
    }

    @GetMapping("/stats/today")
    @Operation(summary = "获取今日签到统计")
    @PreAuthorize("checkIn:view")  // 查看权限（老板和前台都有）
    public Result<CheckInStatsDTO> getTodayCheckInStats() {
        CheckInStatsDTO stats = checkInService.getTodayCheckInStats();
        return Result.success("查询成功", stats);
    }

    @PostMapping("/report")
    @Operation(summary = "获取签到统计报表")
    @PreAuthorize("checkIn:view")  // 查看权限（老板和前台都有）
    public Result<CheckInReportDTO> getCheckInReport(@Valid @RequestBody CheckInQueryDTO queryDTO) {
        CheckInReportDTO report = checkInService.getCheckInReport(queryDTO);
        return Result.success("查询成功", report);
    }
}