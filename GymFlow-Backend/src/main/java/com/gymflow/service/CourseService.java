package com.gymflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.CourseDTO;
import com.gymflow.entity.Course;
import com.gymflow.utils.Result;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface CourseService {

    Result getCourseList(Page<Course> page, String courseType, String status, String keyword, HttpServletRequest request);

    Result getCourseDetail(Long id, HttpServletRequest request);

    Result addCourse(CourseDTO courseDTO, HttpServletRequest request);

    Result updateCourse(Long id, CourseDTO courseDTO, HttpServletRequest request);

    Result deleteCourse(Long id, HttpServletRequest request);

    Result updateCourseStatus(Long id, String status, HttpServletRequest request);

    Result getAvailableCourses(String courseType, String date, HttpServletRequest request);

    Result getTodayCourses(HttpServletRequest request);

    Result bookCourse(Long id, HttpServletRequest request);

    Result cancelBooking(Long id, String reason, HttpServletRequest request);

    Result getCourseBookings(Long id, Page<Object> page, HttpServletRequest request);

    Result generateSignCode(Long id, HttpServletRequest request);

    Result verifySignCode(String signCode, HttpServletRequest request);
}