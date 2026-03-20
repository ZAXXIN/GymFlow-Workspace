package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.CourseSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CourseScheduleMapper extends BaseMapper<CourseSchedule> {

    /**
     * 查询教练在某时间段是否有排课冲突
     */
    @Select("SELECT COUNT(*) FROM course_schedule WHERE coach_id = #{coachId} " +
            "AND schedule_date = #{date} " +
            "AND ((start_time BETWEEN #{startTime} AND #{endTime}) " +
            "OR (end_time BETWEEN #{startTime} AND #{endTime}) " +
            "OR (start_time <= #{startTime} AND end_time >= #{endTime}))")
    int checkConflict(Long coachId, LocalDate date, String startTime, String endTime);
}