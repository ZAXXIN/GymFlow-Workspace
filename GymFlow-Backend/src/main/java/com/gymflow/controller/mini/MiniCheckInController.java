package com.gymflow.controller.mini;

import com.gymflow.common.Result;
import com.gymflow.dto.mini.MiniCheckinCodeDTO;
import com.gymflow.dto.mini.MiniReminderDTO;
import com.gymflow.dto.mini.MiniScanResultDTO;
import com.gymflow.service.mini.MiniCheckInService;
import com.gymflow.utils.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/mini/checkin")
@Tag(name = "小程序-签到核销", description = "签到码和核销相关接口")
@Validated
@RequiredArgsConstructor
public class MiniCheckInController {

    private final MiniCheckInService miniCheckInService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 从token获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtTokenUtil.getUserIdFromToken(token);
    }

    /**
     * 从token获取当前用户类型
     * 根据后端定义：0-会员，1-教练，2-PC端用户等
     */
    private Integer getUserType(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtTokenUtil.getRoleFromToken(token);
    }

    /**
     * 从请求头中提取token
     */
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    @GetMapping("/code/{bookingId}")
    @Operation(summary = "获取签到码（会员端）")
    public Result<MiniCheckinCodeDTO> getCheckinCode(
            HttpServletRequest request,
            @PathVariable @NotNull Long bookingId) {
        Long memberId = getCurrentUserId(request);
        MiniCheckinCodeDTO code = miniCheckInService.getCheckinCode(memberId, bookingId);
        return Result.success("获取成功", code);
    }

    @PostMapping("/scan")
    @Operation(summary = "扫码核销（教练端）")
    public Result<MiniScanResultDTO> scanCheckin(
            HttpServletRequest request,
            @RequestParam @NotNull String qrCode) {
        Long coachId = getCurrentUserId(request);
        MiniScanResultDTO result = miniCheckInService.scanCheckin(coachId, qrCode);
        return Result.success("核销成功", result);
    }

    @PostMapping("/verify-code")
    @Operation(summary = "数字码核销（PC端调用）")
    public Result<MiniScanResultDTO> verifyCode(
            @RequestParam @NotNull String digitalCode,
            @RequestParam @NotNull Integer checkinMethod,
            @RequestParam(required = false) String notes) {
        MiniScanResultDTO result = miniCheckInService.verifyCode(digitalCode, checkinMethod, notes);
        return Result.success("核销成功", result);
    }

    @GetMapping("/check-valid/{bookingId}")
    @Operation(summary = "检查签到码是否有效")
    public Result<Boolean> checkCodeValid(
            HttpServletRequest request,
            @PathVariable @NotNull Long bookingId) {
        Long userId = getCurrentUserId(request);
        Boolean isValid = miniCheckInService.checkCodeValid(userId, bookingId);
        return Result.success("查询成功", isValid);
    }

    @GetMapping("/current-reminder")
    @Operation(summary = "获取当前时段提醒")
    public Result<MiniReminderDTO> getCurrentReminder(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Integer userType = getUserType(request);
        MiniReminderDTO reminder = miniCheckInService.getCurrentReminder(userId, userType);
        return Result.success("获取成功", reminder);
    }
}