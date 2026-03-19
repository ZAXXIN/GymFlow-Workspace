package com.gymflow.service.mini;

import com.gymflow.dto.course.CourseFullDTO;
import com.gymflow.dto.mini.MiniAvailableCourseDTO;
import com.gymflow.dto.mini.MiniBookingDetailDTO;
import com.gymflow.dto.mini.MiniBookingDTO;
import com.gymflow.dto.mini.MiniCreateBookingDTO;

import java.util.List;

public interface MiniBookingService {

    /**
     * 获取可预约课程列表
     */
    List<MiniAvailableCourseDTO> getAvailableCourses(Long memberId, Integer courseType, String keyword);

    /**
     * 获取课程详情（用于预约）
     */
    CourseFullDTO getCourseDetailForBooking(Long courseId);

    /**
     * 创建预约
     */
    Long createBooking(Long memberId, MiniCreateBookingDTO createDTO);

    /**
     * 获取我的预约列表
     */
    List<MiniBookingDTO> getMyBookings(Long memberId, Integer status);

    /**
     * 获取预约详情
     */
    MiniBookingDetailDTO getBookingDetail(Long userId, Long bookingId);

    /**
     * 取消预约
     */
    void cancelBooking(Long memberId, Long bookingId, String reason);
}