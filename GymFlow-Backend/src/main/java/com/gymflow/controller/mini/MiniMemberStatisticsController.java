package com.gymflow.controller.mini;

import com.gymflow.common.Result;
import com.gymflow.dto.mini.MemberStatisticsDTO;
import com.gymflow.service.mini.MemberStatisticsService;
import com.gymflow.utils.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/mini/member")
@Tag(name = "小程序-会员统计", description = "会员统计数据接口")
@RequiredArgsConstructor
public class MiniMemberStatisticsController {

    private final MemberStatisticsService memberStatisticsService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 从token获取当前会员ID
     */
    private Long getCurrentMemberId(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtTokenUtil.getUserIdFromToken(token);
    }

    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取会员统计数据", description = "获取会员的签到记录和课时消费/归还明细")
    public Result<MemberStatisticsDTO> getMemberStatistics(
            HttpServletRequest request,
            @RequestParam Long orderItemId) {
        try {
            Long memberId = getCurrentMemberId(request);
            MemberStatisticsDTO statistics = memberStatisticsService.getMemberStatistics(memberId, orderItemId);
            return Result.success("获取成功", statistics);
        } catch (Exception e) {
            log.error("获取会员统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }
}