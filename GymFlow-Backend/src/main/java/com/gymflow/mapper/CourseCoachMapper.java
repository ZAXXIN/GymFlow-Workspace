package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.CourseCoach;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseCoachMapper extends BaseMapper<CourseCoach> {

    /**
     * 根据课程ID查询绑定的教练ID列表
     */
    @Select("SELECT coach_id FROM course_coach WHERE course_id = #{courseId}")
    List<Long> selectCoachIdsByCourseId(Long courseId);
}