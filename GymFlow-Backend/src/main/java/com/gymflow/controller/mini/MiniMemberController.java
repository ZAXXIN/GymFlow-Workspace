package com.gymflow.controller.mini;

import com.gymflow.common.Result;
import com.gymflow.dto.member.HealthRecordDTO;
import com.gymflow.dto.member.MemberFullDTO;
import com.gymflow.dto.mini.MiniReminderDTO;
import com.gymflow.service.mini.MiniMemberService;
import com.gymflow.utils.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/mini/member")
@Tag(name = "小程序-会员端", description = "会员端专用接口")
@Validated
@RequiredArgsConstructor
public class MiniMemberController {

    private final MiniMemberService miniMemberService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 从token获取当前会员ID
     */
    private Long getCurrentMemberId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtTokenUtil.getUserIdFromToken(token);
    }

    @GetMapping("/my-info")
    @Operation(summary = "获取当前会员完整信息（包含会员卡、健康档案、课程包）")
    public Result<MemberFullDTO> getMyInfo(HttpServletRequest request) {
        Long memberId = getCurrentMemberId(request);
        MemberFullDTO memberInfo = miniMemberService.getMyInfo(memberId);
        return Result.success("获取成功", memberInfo);
    }

    @GetMapping("/current-reminder")
    @Operation(summary = "获取当前时段提醒")
    public Result<MiniReminderDTO> getCurrentReminder(HttpServletRequest request) {
        Long memberId = getCurrentMemberId(request);
        MiniReminderDTO reminder = miniMemberService.getCurrentReminder(memberId);
        return Result.success("获取成功", reminder);
    }

    @PostMapping("/add-health-record")
    @Operation(summary = "添加健康档案")
    public Result<Void> addHealthRecord(HttpServletRequest request,
                                        @Valid @RequestBody HealthRecordDTO healthRecordDTO) {
        Long memberId = getCurrentMemberId(request);
        miniMemberService.addHealthRecord(memberId, healthRecordDTO);
        return Result.success("添加成功");
    }

    @PostMapping("/modify-password")
    @Operation(summary = "修改密码")
    public Result<Void> modifyPassword(HttpServletRequest request,
                                       @RequestParam @NotNull String oldPassword,
                                       @RequestParam @NotNull String newPassword) {
        Long memberId = getCurrentMemberId(request);
        miniMemberService.modifyPassword(memberId, oldPassword, newPassword);
        return Result.success("修改成功");
    }
}