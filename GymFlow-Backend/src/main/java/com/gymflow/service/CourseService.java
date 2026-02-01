package com.gymflow.service;

import com.gymflow.dto.course.*;
import com.gymflow.vo.CourseListVO;
import com.gymflow.vo.CourseScheduleVO;
import com.gymflow.vo.PageResultVO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CourseService {

    /**
     * 分页查询课程列表
     */
    PageResultVO<CourseListVO> getCourseList(CourseQueryDTO queryDTO);

    /**
     * 获取课程详情（包含预约情况）
     */
    CourseFullDTO getCourseDetail(Long courseId);

    /**
     * 新增课程
     */
    Long addCourse(CourseBasicDTO courseDTO);

    /**
     * 编辑课程
     */
    void updateCourse(Long courseId, CourseBasicDTO courseDTO);

    /**
     * 删除课程
     */
    void deleteCourse(Long courseId);

    /**
     * 更新课程状态
     */
    void updateCourseStatus(Long courseId, Integer status);

    /**
     * 课程排课（团课）
     */
    void scheduleCourse(CourseScheduleDTO scheduleDTO);

    /**
     * 更新排课
     */
    void updateCourseSchedule(Long scheduleId, CourseScheduleDTO scheduleDTO);

    /**
     * 删除排课
     */
    void deleteCourseSchedule(Long scheduleId);

    /**
     * 获取课程排课列表
     */
    List<CourseScheduleVO> getCourseSchedules(Long courseId);

    /**
     * 获取课程表（所有课程的全部预约信息）
     */
    List<CourseScheduleVO> getCourseTimetable(LocalDate startDate, LocalDate endDate);

    /**
     * 会员预约私教课
     */
    void bookPrivateCourse(Long memberId, Long coachId, LocalDate courseDate, LocalTime startTime);

    /**
     * 会员预约团课
     */
    void bookGroupCourse(Long memberId, Long scheduleId);

    /**
     * 核销课程预约
     */
    void verifyCourseBooking(Long bookingId, Integer checkinMethod);

    /**
     * 取消课程预约
     */
    void cancelCourseBooking(Long bookingId, String reason);
}