package com.gymflow.controller.mini;

import com.gymflow.common.Result;
import com.gymflow.dto.course.CourseFullDTO;
import com.gymflow.dto.mini.MiniAvailableCourseDTO;
import com.gymflow.dto.mini.MiniBookingDetailDTO;
import com.gymflow.dto.mini.MiniBookingDTO;
import com.gymflow.dto.mini.MiniCreateBookingDTO;
import com.gymflow.service.mini.MiniBookingService;
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
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mini/booking")
@Tag(name = "小程序-预约管理", description = "预约相关接口")
@Validated
@RequiredArgsConstructor
public class MiniBookingController {

    private final MiniBookingService miniBookingService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 从token获取当前用户ID和类型
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtTokenUtil.getUserIdFromToken(token);
    }

    @GetMapping("/available-courses")
    @Operation(summary = "获取可预约课程列表")
    public Result<List<MiniAvailableCourseDTO>> getAvailableCourses(
            HttpServletRequest request,
            @RequestParam(required = false) Integer courseType,
            @RequestParam(required = false) String keyword) {
        // 会员端查看可预约课程
        Long memberId = getCurrentUserId(request);
        List<MiniAvailableCourseDTO> courses = miniBookingService.getAvailableCourses(memberId, courseType, keyword);
        return Result.success("获取成功", courses);
    }

    @GetMapping("/course-detail/{courseId}")
    @Operation(summary = "获取课程详情（用于预约）")
    public Result<CourseFullDTO> getCourseDetail(
            HttpServletRequest request,
            @PathVariable @NotNull Long courseId) {
        CourseFullDTO detail = miniBookingService.getCourseDetailForBooking(courseId);
        return Result.success("获取成功", detail);
    }

    @PostMapping("/create")
    @Operation(summary = "创建预约")
    public Result<Long> createBooking(
            HttpServletRequest request,
            @Valid @RequestBody MiniCreateBookingDTO createDTO) {
        Long memberId = getCurrentUserId(request);
        Long bookingId = miniBookingService.createBooking(memberId, createDTO);
        return Result.success("预约成功", bookingId);
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "获取我的预约列表")
    public Result<List<MiniBookingDTO>> getMyBookings(
            HttpServletRequest request,
            @RequestParam(required = false) Integer status) {
        Long memberId = getCurrentUserId(request);
        List<MiniBookingDTO> bookings = miniBookingService.getMyBookings(memberId, status);
        return Result.success("获取成功", bookings);
    }

    @GetMapping("/detail/{bookingId}")
    @Operation(summary = "获取预约详情")
    public Result<MiniBookingDetailDTO> getBookingDetail(
            HttpServletRequest request,
            @PathVariable @NotNull Long bookingId) {
        Long userId = getCurrentUserId(request);
        MiniBookingDetailDTO detail = miniBookingService.getBookingDetail(userId, bookingId);
        return Result.success("获取成功", detail);
    }

    @PostMapping("/cancel/{bookingId}")
    @Operation(summary = "取消预约")
    public Result<Void> cancelBooking(
            HttpServletRequest request,
            @PathVariable @NotNull Long bookingId,
            @RequestParam String reason) {
        Long memberId = getCurrentUserId(request);
        miniBookingService.cancelBooking(memberId, bookingId, reason);
        return Result.success("取消成功");
    }
}