package com.gymflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.RegisterDTO;
import com.gymflow.entity.Coach;
import com.gymflow.entity.Member;
import com.gymflow.service.CoachService;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/coach")
public class CoachController {

    @Autowired
    private CoachService coachService;

    /**
     * 获取教练列表（分页）
     */
    @GetMapping("/list")
    public Result getCoachList(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @RequestParam(required = false) String keyword,
                               HttpServletRequest request) {
        Page<Coach> page = new Page<>(pageNum, pageSize);
        return coachService.getCoachList(page, keyword, request);
    }

    /**
     * 获取所有在职教练
     */
    @GetMapping("/active")
    public Result getActiveCoaches(HttpServletRequest request) {
        return coachService.getActiveCoaches(request);
    }

    /**
     * 获取教练详情
     */
    @GetMapping("/detail/{id}")
    public Result getCoachDetail(@PathVariable Long id, HttpServletRequest request) {
        return coachService.getCoachDetail(id, request);
    }

    /**
     * 添加教练
     */
    @PostMapping("/add")
    public Result addCoach(@Validated @RequestBody RegisterDTO registerDTO, HttpServletRequest request) {
        return coachService.addCoach(registerDTO, request);
    }

    /**
     * 更新教练信息
     */
    @PutMapping("/update/{id}")
    public Result updateCoach(@PathVariable Long id, @RequestBody Coach coach, HttpServletRequest request) {
        return coachService.updateCoach(id, coach, request);
    }

    /**
     * 删除教练
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteCoach(@PathVariable Long id, HttpServletRequest request) {
        return coachService.deleteCoach(id, request);
    }

    /**
     * 更新教练状态
     */
    @PutMapping("/status/{id}")
    public Result updateCoachStatus(@PathVariable Long id,
                                    @RequestParam Integer status,
                                    HttpServletRequest request) {
        return coachService.updateCoachStatus(id, status, request);
    }

    /**
     * 获取教练的学员列表
     */
    @GetMapping("/students/{id}")
    public Result getCoachStudents(@PathVariable Long id,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   HttpServletRequest request) {
        Page<Member> page = new Page<>(pageNum, pageSize);
        return coachService.getCoachStudents(id, page, request);
    }

    /**
     * 获取教练的课程
     */
    @GetMapping("/courses/{id}")
    public Result getCoachCourses(@PathVariable Long id,
                                  @RequestParam(required = false) String date,
                                  @RequestParam(required = false) String status,
                                  HttpServletRequest request) {
        return coachService.getCoachCourses(id, date, status, request);
    }

    /**
     * 获取教练的业绩统计
     */
    @GetMapping("/stats/{id}")
    public Result getCoachStats(@PathVariable Long id,
                                @RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate,
                                HttpServletRequest request) {
        return coachService.getCoachStats(id, startDate, endDate, request);
    }
}