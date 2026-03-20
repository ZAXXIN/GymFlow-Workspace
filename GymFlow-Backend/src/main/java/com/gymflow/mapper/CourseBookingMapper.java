package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.CourseBooking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CourseBookingMapper extends BaseMapper<CourseBooking> {

    /**
     * 根据签到码查询预约
     */
    @Select("SELECT * FROM course_booking WHERE sign_code = #{signCode}")
    CourseBooking selectBySignCode(String signCode);

    /**
     * 查询需要自动完成的预约
     */
    @Select("SELECT * FROM course_booking WHERE booking_status = 1 " +
            "AND auto_complete_time <= #{now}")
    List<CourseBooking> selectNeedAutoComplete(LocalDateTime now);

    /**
     * 批量更新预约状态为已完成
     */
    @Update("UPDATE course_booking SET booking_status = 2 " +
            "WHERE id IN (${ids})")
    int batchComplete(String ids);
}