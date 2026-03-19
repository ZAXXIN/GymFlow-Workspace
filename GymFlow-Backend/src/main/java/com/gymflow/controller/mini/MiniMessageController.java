package com.gymflow.controller.mini;

import com.gymflow.common.Result;
import com.gymflow.dto.mini.MiniMessageDTO;
import com.gymflow.dto.mini.MiniUnreadCountDTO;
import com.gymflow.service.mini.MiniMessageService;
import com.gymflow.utils.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mini/message")
@Tag(name = "小程序-消息通知", description = "消息通知相关接口")
@Validated
@RequiredArgsConstructor
public class MiniMessageController {

    private final MiniMessageService miniMessageService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 从token获取当前用户ID和类型
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtTokenUtil.getUserIdFromToken(token);
    }

    private Integer getUserTypeFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtTokenUtil.getRoleFromToken(token);
    }

    @GetMapping("/list")
    @Operation(summary = "获取消息列表")
    public Result<List<MiniMessageDTO>> getMessageList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = getCurrentUserId(request);
        Integer userType = getUserTypeFromToken(request);
        List<MiniMessageDTO> messages = miniMessageService.getMessageList(userId, userType, pageNum, pageSize);
        return Result.success("获取成功", messages);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数量")
    public Result<MiniUnreadCountDTO> getUnreadCount(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Integer userType = getUserTypeFromToken(request);
        MiniUnreadCountDTO count = miniMessageService.getUnreadCount(userId, userType);
        return Result.success("获取成功", count);
    }

    @PutMapping("/read/{messageId}")
    @Operation(summary = "标记消息为已读")
    public Result<Void> markAsRead(
            HttpServletRequest request,
            @PathVariable @NotNull Long messageId) {
        Long userId = getCurrentUserId(request);
        miniMessageService.markAsRead(userId, messageId);
        return Result.success("操作成功");
    }

    @PutMapping("/read-all")
    @Operation(summary = "标记所有消息为已读")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        Integer userType = getUserTypeFromToken(request);
        miniMessageService.markAllAsRead(userId, userType);
        return Result.success("操作成功");
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "删除消息")
    public Result<Void> deleteMessage(
            HttpServletRequest request,
            @PathVariable @NotNull Long messageId) {
        Long userId = getCurrentUserId(request);
        miniMessageService.deleteMessage(userId, messageId);
        return Result.success("删除成功");
    }
}