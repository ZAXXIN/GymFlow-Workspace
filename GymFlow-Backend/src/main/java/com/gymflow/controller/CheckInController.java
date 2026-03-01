package com.gymflow.controller;

import com.gymflow.common.Result;
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
    @Operation(summary = "分页查询签到列表", description = "根据条件分页查询签到列表")
    public Result<PageResultVO<CheckInListVO>> getCheckInList(@Valid @RequestBody CheckInQueryDTO queryDTO) {
        try {
            PageResultVO<CheckInListVO> result = checkInService.getCheckInList(queryDTO);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询签到列表失败：{}", e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/detail/{checkInId}")
    @Operation(summary = "获取签到详情", description = "根据签到ID获取签到详细信息")
    public Result<CheckInDetailVO> getCheckInDetail(
            @Parameter(description = "签到ID", required = true)
            @PathVariable @NotNull Long checkInId) {
        try {
            CheckInDetailVO detail = checkInService.getCheckInDetail(checkInId);
            return Result.success("查询成功", detail);
        } catch (Exception e) {
            log.error("获取签到详情失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @PostMapping("/member/{memberId}")
    @Operation(summary = "会员签到", description = "会员进行自由训练签到")
    public Result<Void> memberCheckIn(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId,
            @RequestParam @NotNull Integer checkinMethod,
            @RequestParam(required = false) String notes) {
        try {
            checkInService.memberCheckIn(memberId, checkinMethod, notes);
            return Result.success("签到成功");
        } catch (Exception e) {
            log.error("会员签到失败：{}", e.getMessage(), e);
            return Result.error("签到失败：" + e.getMessage());
        }
    }

    @PostMapping("/course/{bookingId}")
    @Operation(summary = "课程签到", description = "会员进行课程签到")
    public Result<Void> courseCheckIn(
            @Parameter(description = "预约ID", required = true)
            @PathVariable @NotNull Long bookingId,
            @RequestParam @NotNull Integer checkinMethod,
            @RequestParam(required = false) String notes) {
        try {
            checkInService.courseCheckIn(bookingId, checkinMethod, notes);
            return Result.success("课程签到成功");
        } catch (Exception e) {
            log.error("课程签到失败：{}", e.getMessage(), e);
            return Result.error("签到失败：" + e.getMessage());
        }
    }

    @PutMapping("/update/{checkInId}")
    @Operation(summary = "更新签到信息", description = "更新签到备注信息")
    public Result<Void> updateCheckIn(
            @Parameter(description = "签到ID", required = true)
            @PathVariable @NotNull Long checkInId,
            @RequestParam String notes) {
        try {
            checkInService.updateCheckIn(checkInId, notes);
            return Result.success("更新签到信息成功");
        } catch (Exception e) {
            log.error("更新签到信息失败：{}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{checkInId}")
    @Operation(summary = "删除签到记录", description = "删除签到记录（仅限自由训练签到）")
    public Result<Void> deleteCheckIn(
            @Parameter(description = "签到ID", required = true)
            @PathVariable @NotNull Long checkInId) {
        try {
            checkInService.deleteCheckIn(checkInId);
            return Result.success("删除签到记录成功");
        } catch (Exception e) {
            log.error("删除签到记录失败：{}", e.getMessage(), e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除签到记录", description = "批量删除多个签到记录")
    public Result<Void> batchDeleteCheckIns(@RequestBody List<Long> checkInIds) {
        try {
            checkInService.batchDeleteCheckIns(checkInIds);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            log.error("批量删除签到记录失败：{}", e.getMessage(), e);
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/member-list/{memberId}")
    @Operation(summary = "获取会员签到记录", description = "获取指定会员的签到记录列表")
    public Result<PageResultVO<CheckInListVO>> getMemberCheckIns(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId,
            @Valid @RequestBody CheckInQueryDTO queryDTO) {
        try {
            PageResultVO<CheckInListVO> result = checkInService.getMemberCheckIns(memberId, queryDTO);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("获取会员签到记录失败：{}", e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/stats/today")
    @Operation(summary = "获取今日签到统计", description = "获取今日签到统计数据")
    public Result<CheckInStatsDTO> getTodayCheckInStats() {
        try {
            CheckInStatsDTO stats = checkInService.getTodayCheckInStats();
            return Result.success("查询成功", stats);
        } catch (Exception e) {
            log.error("获取今日签到统计失败：{}", e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/report")
    @Operation(summary = "获取签到统计报表", description = "获取签到统计报表数据")
    public Result<CheckInReportDTO> getCheckInReport(@Valid @RequestBody CheckInQueryDTO queryDTO) {
        try {
            CheckInReportDTO report = checkInService.getCheckInReport(queryDTO);
            return Result.success("查询成功", report);
        } catch (Exception e) {
            log.error("获取签到统计报表失败：{}", e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}