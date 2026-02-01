package com.gymflow.controller;

import com.gymflow.common.Result;
import com.gymflow.dto.course.*;
import com.gymflow.service.CourseService;
import com.gymflow.vo.CourseScheduleVO;
import com.gymflow.vo.PageResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Tag(name = "课程管理", description = "课程管理相关接口")
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
@Validated
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "分页查询课程列表")
    @GetMapping("/list")
    public Result<PageResultVO<com.gymflow.vo.CourseListVO>> getCourseList(@Valid CourseQueryDTO queryDTO) {
        PageResultVO<com.gymflow.vo.CourseListVO> result = courseService.getCourseList(queryDTO);
        return Result.success("查询成功", result);
    }

    @Operation(summary = "获取课程详情")
    @GetMapping("/detail/{courseId}")
    public Result<CourseFullDTO> getCourseDetail(@PathVariable Long courseId) {
        CourseFullDTO courseDetail = courseService.getCourseDetail(courseId);
        return Result.success("查询成功", courseDetail);
    }

    @Operation(summary = "新增课程")
    @PostMapping("/add")
    public Result<Long> addCourse(@Valid @RequestBody CourseBasicDTO courseDTO) {
        Long courseId = courseService.addCourse(courseDTO);
        return Result.success("新增课程成功", courseId);
    }

    @Operation(summary = "编辑课程")
    @PutMapping("/update/{courseId}")
    public Result<Void> updateCourse(@PathVariable Long courseId,
                                     @Valid @RequestBody CourseBasicDTO courseDTO) {
        courseService.updateCourse(courseId, courseDTO);
        return Result.success("编辑课程成功");
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/delete/{courseId}")
    public Result<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return Result.success("删除课程成功");
    }

    @Operation(summary = "更新课程状态")
    @PutMapping("/updateStatus/{courseId}")
    public Result<Void> updateCourseStatus(@PathVariable Long courseId,
                                           @RequestParam Integer status) {
        courseService.updateCourseStatus(courseId, status);
        return Result.success("更新课程状态成功");
    }

    @Operation(summary = "课程排课（团课）")
    @PostMapping("/schedule")
    public Result<Void> scheduleCourse(@Valid @RequestBody CourseScheduleDTO scheduleDTO) {
        courseService.scheduleCourse(scheduleDTO);
        return Result.success("排课成功");
    }

    @Operation(summary = "获取课程排课列表")
    @GetMapping("/schedules/{courseId}")
    public Result<List<CourseScheduleVO>> getCourseSchedules(@PathVariable Long courseId) {
        List<CourseScheduleVO> schedules = courseService.getCourseSchedules(courseId);
        return Result.success("查询成功", schedules);
    }

    @Operation(summary = "获取课程表")
    @GetMapping("/timetable")
    public Result<List<CourseScheduleVO>> getCourseTimetable(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<CourseScheduleVO> timetable = courseService.getCourseTimetable(startDate, endDate);
        return Result.success("查询成功", timetable);
    }

    @Operation(summary = "会员预约私教课")
    @PostMapping("/book/private")
    public Result<Void> bookPrivateCourse(@RequestParam Long memberId,
                                          @RequestParam Long coachId,
                                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate courseDate,
                                          @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime) {
        courseService.bookPrivateCourse(memberId, coachId, courseDate, startTime);
        return Result.success("预约成功");
    }

    @Operation(summary = "会员预约团课")
    @PostMapping("/book/group")
    public Result<Void> bookGroupCourse(@RequestParam Long memberId,
                                        @RequestParam Long scheduleId) {
        courseService.bookGroupCourse(memberId, scheduleId);
        return Result.success("预约成功");
    }

    @Operation(summary = "核销课程预约")
    @PostMapping("/verify/{bookingId}")
    public Result<Void> verifyCourseBooking(@PathVariable Long bookingId,
                                            @RequestParam Integer checkinMethod) {
        courseService.verifyCourseBooking(bookingId, checkinMethod);
        return Result.success("核销成功");
    }

    @Operation(summary = "取消课程预约")
    @PostMapping("/cancel/{bookingId}")
    public Result<Void> cancelCourseBooking(@PathVariable Long bookingId,
                                            @RequestParam String reason) {
        courseService.cancelCourseBooking(bookingId, reason);
        return Result.success("取消预约成功");
    }
}