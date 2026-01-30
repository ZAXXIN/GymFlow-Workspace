package com.gymflow.controller;

import com.gymflow.common.Result;
import com.gymflow.dto.coach.*;
import com.gymflow.dto.member.MemberQueryDTO;
import com.gymflow.service.CoachService;
import com.gymflow.vo.CoachListVO;
import com.gymflow.vo.MemberListVO;
import com.gymflow.vo.PageResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "教练管理", description = "教练管理相关接口")
@RestController
@RequestMapping("/coach")
@RequiredArgsConstructor
@Validated
public class CoachController {
    private final CoachService coachService;

    @Operation(summary = "分页查询教练列表")
    @GetMapping("/list")
    public Result<PageResultVO<CoachListVO>> getCoachList(@Valid CoachQueryDTO queryDTO) {
        PageResultVO<CoachListVO> result = coachService.getCoachList(queryDTO);
        return Result.success("查询成功", result);
    }

    @Operation(summary = "获取教练详情")
    @GetMapping("/detail/{coachId}")
    public Result<CoachFullDTO> getCoachDetail(@PathVariable Long coachId) {
        CoachFullDTO coachDetail = coachService.getCoachDetail(coachId);
        return Result.success("查询成功", coachDetail);
    }

    @Operation(summary = "添加教练")
    @PostMapping("/add")
    public Result<Long> addCoach(@Valid @RequestBody CoachBasicDTO basicDTO) {
        Long coachId = coachService.addCoach(basicDTO);
        return Result.success("添加教练成功", coachId);
    }

    @Operation(summary = "更新教练")
    @PutMapping("/update/{coachId}")
    public Result<Void> updateCoach(@PathVariable Long coachId,
                                    @Valid @RequestBody CoachBasicDTO basicDTO) {
        coachService.updateCoach(coachId, basicDTO);
        return Result.success("更新教练成功");
    }

    @Operation(summary = "删除教练")
    @DeleteMapping("/delete/{coachId}")
    public Result<Void> deleteCoach(@PathVariable Long coachId) {
        coachService.deleteCoach(coachId);
        return Result.success("删除教练成功");
    }

    @Operation(summary = "批量删除教练")
    @DeleteMapping("/batchDelete")
    public Result<Void> batchDeleteCoach(@RequestBody List<Long> coachIds) {
        coachService.batchDeleteCoach(coachIds);
        return Result.success("批量删除教练成功");
    }

    @Operation(summary = "更新教练状态")
    @PutMapping("/updateStatus/{coachId}")
    public Result<Void> updateCoachStatus(@PathVariable Long coachId,
                                          @RequestParam Integer status) {
        coachService.updateCoachStatus(coachId, status);
        return Result.success("更新教练状态成功");
    }

    @Operation(summary = "获取教练排班列表")
    @GetMapping("/schedules/{coachId}")
    public Result<List<CoachScheduleDTO>> getCoachSchedules(@PathVariable Long coachId) {
        List<CoachScheduleDTO> schedules = coachService.getCoachSchedules(coachId);
        return Result.success("查询成功", schedules);
    }

    @Operation(summary = "添加教练排班")
    @PostMapping("/schedule/add/{coachId}")
    public Result<Void> addCoachSchedule(@PathVariable Long coachId,
                                         @Valid @RequestBody CoachScheduleDTO scheduleDTO) {
        coachService.addCoachSchedule(coachId, scheduleDTO);
        return Result.success("添加排班成功");
    }

    @Operation(summary = "更新教练排班")
    @PutMapping("/schedule/update/{scheduleId}")
    public Result<Void> updateCoachSchedule(@PathVariable Long scheduleId,
                                            @Valid @RequestBody CoachScheduleDTO scheduleDTO) {
        coachService.updateCoachSchedule(scheduleId, scheduleDTO);
        return Result.success("更新排班成功");
    }

    @Operation(summary = "删除教练排班")
    @DeleteMapping("/schedule/delete/{scheduleId}")
    public Result<Void> deleteCoachSchedule(@PathVariable Long scheduleId) {
        coachService.deleteCoachSchedule(scheduleId);
        return Result.success("删除排班成功");
    }

    @Operation(summary = "获取教练课程列表")
    @GetMapping("/courses/{coachId}")
    public Result<List<CoachCourseDTO>> getCoachCourses(@PathVariable Long coachId) {
        List<CoachCourseDTO> courses = coachService.getCoachCourses(coachId);
        return Result.success("查询成功", courses);
    }
}