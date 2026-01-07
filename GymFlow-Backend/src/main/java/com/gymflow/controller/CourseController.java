package com.gymflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.CourseDTO;
import com.gymflow.entity.Course;
import com.gymflow.service.CourseService;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 获取课程列表（分页）
     */
    @GetMapping("/list")
    public Result getCourseList(@RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "10") Integer pageSize,
                                @RequestParam(required = false) String courseType,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String keyword,
                                HttpServletRequest request) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        return courseService.getCourseList(page, courseType, status, keyword, request);
    }

    /**
     * 获取课程详情
     */
    @GetMapping("/detail/{id}")
    public Result getCourseDetail(@PathVariable Long id, HttpServletRequest request) {
        return courseService.getCourseDetail(id, request);
    }

    /**
     * 添加课程
     */
    @PostMapping("/add")
    public Result addCourse(@Validated @RequestBody CourseDTO courseDTO, HttpServletRequest request) {
        return courseService.addCourse(courseDTO, request);
    }

    /**
     * 更新课程
     */
    @PutMapping("/update/{id}")
    public Result updateCourse(@PathVariable Long id, @RequestBody CourseDTO courseDTO, HttpServletRequest request) {
        return courseService.updateCourse(id, courseDTO, request);
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteCourse(@PathVariable Long id, HttpServletRequest request) {
        return courseService.deleteCourse(id, request);
    }

    /**
     * 更新课程状态
     */
    @PutMapping("/status/{id}")
    public Result updateCourseStatus(@PathVariable Long id,
                                     @RequestParam String status,
                                     HttpServletRequest request) {
        return courseService.updateCourseStatus(id, status, request);
    }

    /**
     * 获取可预约课程（会员端）
     */
    @GetMapping("/available")
    public Result getAvailableCourses(@RequestParam(required = false) String courseType,
                                      @RequestParam(required = false) String date,
                                      HttpServletRequest request) {
        return courseService.getAvailableCourses(courseType, date, request);
    }

    /**
     * 获取今日课程（教练端）
     */
    @GetMapping("/today")
    public Result getTodayCourses(HttpServletRequest request) {
        return courseService.getTodayCourses(request);
    }

    /**
     * 课程预约
     */
    @PostMapping("/book/{id}")
    public Result bookCourse(@PathVariable Long id, HttpServletRequest request) {
        return courseService.bookCourse(id, request);
    }

    /**
     * 取消预约
     */
    @PostMapping("/cancel/{id}")
    public Result cancelBooking(@PathVariable Long id,
                                @RequestParam(required = false) String reason,
                                HttpServletRequest request) {
        return courseService.cancelBooking(id, reason, request);
    }

    /**
     * 获取课程预约列表
     */
    @GetMapping("/bookings/{id}")
    public Result getCourseBookings(@PathVariable Long id,
                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                    HttpServletRequest request) {
        Page<Object> page = new Page<>(pageNum, pageSize);
        return courseService.getCourseBookings(id, page, request);
    }

    /**
     * 生成课程签到码
     */
    @PostMapping("/sign-code/{id}")
    public Result generateSignCode(@PathVariable Long id, HttpServletRequest request) {
        return courseService.generateSignCode(id, request);
    }

    /**
     * 验证签到码
     */
    @PostMapping("/verify-sign-code")
    public Result verifySignCode(@RequestParam String signCode, HttpServletRequest request) {
        return courseService.verifySignCode(signCode, request);
    }
}