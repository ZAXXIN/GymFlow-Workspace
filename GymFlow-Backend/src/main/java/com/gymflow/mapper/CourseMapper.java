package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.entity.Course;
import com.gymflow.vo.CourseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 分页查询课程列表
     */
    IPage<CourseVO> selectCoursePage(Page<CourseVO> page,
                                     @Param("courseType") String courseType,
                                     @Param("status") String status,
                                     @Param("keyword") String keyword);

    /**
     * 查询会员可预约的课程
     */
    List<CourseVO> selectAvailableCourses(@Param("memberId") Long memberId,
                                          @Param("courseType") String courseType,
                                          @Param("date") LocalDate date);

    /**
     * 查询教练的课程
     */
    List<CourseVO> selectCoachCourses(@Param("coachId") Long coachId,
                                      @Param("date") LocalDate date,
                                      @Param("status") String status);

    /**
     * 查询今日课程
     */
    @Select("SELECT * FROM course WHERE course_date = CURDATE() AND status IN ('UPCOMING', 'ONGOING')")
    List<Course> selectTodayCourses();

    /**
     * 更新课程状态
     */
    @Update("UPDATE course SET status = #{status} WHERE id = #{courseId}")
    int updateStatus(@Param("courseId") Long courseId, @Param("status") String status);

    /**
     * 更新课程报名人数
     */
    @Update("UPDATE course SET current_enrollment = current_enrollment + #{change} WHERE id = #{courseId}")
    int updateEnrollment(@Param("courseId") Long courseId, @Param("change") Integer change);

    /**
     * 设置签到码
     */
    @Update("UPDATE course SET sign_code = #{signCode}, sign_code_expire_time = #{expireTime} WHERE id = #{courseId}")
    int updateSignCode(@Param("courseId") Long courseId,
                       @Param("signCode") String signCode,
                       @Param("expireTime") String expireTime);

    /**
     * 查询过期课程（自动更新状态）
     */
    @Select("SELECT * FROM course WHERE status = 'UPCOMING' AND course_date < CURDATE()")
    List<Course> selectExpiredCourses();

    /**
     * 根据签到码查询课程
     */
    @Select("SELECT * FROM course WHERE sign_code = #{signCode} AND sign_code_expire_time > NOW()")
    Course selectBySignCode(@Param("signCode") String signCode);

    /**
     * 查询课程详情（包含教练信息）
     */
    CourseVO selectCourseDetail(@Param("courseId") Long courseId);
}