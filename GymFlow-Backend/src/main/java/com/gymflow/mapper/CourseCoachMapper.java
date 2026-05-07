package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.CourseCoach;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CourseCoachMapper extends BaseMapper<CourseCoach> {

    /**
     * 根据课程ID查询绑定的教练ID列表
     */
    @Select("SELECT coach_id FROM course_coach WHERE course_id = #{courseId}")
    List<Long> selectCoachIdsByCourseId(Long courseId);

    /**
     * 根据教练ID查询绑定的课程ID列表
     */
    @Select("SELECT course_id FROM course_coach WHERE coach_id = #{coachId}")
    List<Long> selectCourseIdsByCoachId(@Param("coachId") Long coachId);

    /**
     * 批量根据教练ID列表查询绑定的课程ID列表
     */
    @Select("<script>" +
            "SELECT coach_id, course_id FROM course_coach WHERE coach_id IN " +
            "<foreach collection='coachIds' item='coachId' open='(' separator=',' close=')'>" +
            "#{coachId}" +
            "</foreach>" +
            "</script>")
    List<CourseCoach> selectByCoachIds(@Param("coachIds") List<Long> coachIds);
}