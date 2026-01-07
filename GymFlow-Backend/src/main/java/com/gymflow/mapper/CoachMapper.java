package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.entity.Coach;
import com.gymflow.entity.Course;  // 添加导入
import com.gymflow.vo.CoachVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface CoachMapper extends BaseMapper<Coach> {

    /**
     * 根据用户ID查询教练
     */
    @Select("SELECT * FROM coach WHERE user_id = #{userId}")
    Coach selectByUserId(@Param("userId") Long userId);

    /**
     * 分页查询教练列表（带条件）
     */
    IPage<CoachVO> selectCoachPage(Page<CoachVO> page,
                                   @Param("keyword") String keyword,
                                   @Param("status") Integer status);

    /**
     * 查询所有在职教练
     */
    @Select("SELECT * FROM coach WHERE status = 1 ORDER BY real_name")
    List<Coach> selectActiveCoaches();

    /**
     * 查询所有教练（包含用户信息）
     */
    List<CoachVO> selectAllCoachesWithUserInfo();

    /**
     * 更新教练状态
     */
    @Update("UPDATE coach SET status = #{status}, update_time = NOW() WHERE id = #{coachId}")
    int updateStatus(@Param("coachId") Long coachId, @Param("status") Integer status);

    /**
     * 更新教练统计信息
     */
    @Update("UPDATE coach SET total_students = total_students + #{students}, " +
            "total_courses = total_courses + #{courses}, " +
            "total_income = total_income + #{income}, update_time = NOW() WHERE id = #{coachId}")
    int updateStats(@Param("coachId") Long coachId,
                    @Param("students") Integer students,
                    @Param("courses") Integer courses,
                    @Param("income") Double income);

    /**
     * 增加学员数
     */
    @Update("UPDATE coach SET total_students = total_students + 1, update_time = NOW() WHERE id = #{coachId}")
    int incrementStudents(@Param("coachId") Long coachId);

    /**
     * 增加课程数
     */
    @Update("UPDATE coach SET total_courses = total_courses + 1, update_time = NOW() WHERE id = #{coachId}")
    int incrementCourses(@Param("coachId") Long coachId);

    /**
     * 增加收入
     */
    @Update("UPDATE coach SET total_income = total_income + #{income}, update_time = NOW() WHERE id = #{coachId}")
    int addIncome(@Param("coachId") Long coachId, @Param("income") Double income);

    /**
     * 根据手机号查询教练
     */
    @Select("SELECT c.* FROM coach c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "WHERE u.phone = #{phone} AND u.deleted = 0")
    Coach selectByPhone(@Param("phone") String phone);

    /**
     * 根据专业特长查询教练
     */
    @Select("SELECT * FROM coach WHERE specialty LIKE CONCAT('%', #{specialty}, '%') AND status = 1")
    List<Coach> selectBySpecialty(@Param("specialty") String specialty);

    /**
     * 更新教练资质信息
     */
    @Update("UPDATE coach SET certifications = #{certifications}, years_of_experience = #{years}, " +
            "specialty = #{specialty}, update_time = NOW() WHERE id = #{coachId}")
    int updateQualifications(@Param("coachId") Long coachId,
                             @Param("certifications") String certifications,
                             @Param("years") Integer years,
                             @Param("specialty") String specialty);

    /**
     * 更新教练费率
     */
    @Update("UPDATE coach SET hourly_rate = #{hourlyRate}, commission_rate = #{commissionRate}, " +
            "update_time = NOW() WHERE id = #{coachId}")
    int updateRates(@Param("coachId") Long coachId,
                    @Param("hourlyRate") Double hourlyRate,
                    @Param("commissionRate") Double commissionRate);

    /**
     * 查询教练详情（包含用户信息）
     */
    CoachVO selectCoachDetail(@Param("coachId") Long coachId);

    /**
     * 查询教练的学员数统计
     */
    @Select("SELECT COUNT(*) as student_count FROM member WHERE personal_coach_id = #{coachId}")
    Integer countStudents(@Param("coachId") Long coachId);

    /**
     * 查询教练的课程数统计
     */
    @Select("SELECT COUNT(*) as course_count FROM course WHERE coach_id = #{coachId}")
    Integer countCourses(@Param("coachId") Long coachId);

    /**
     * 查询教练收入统计（按时间段）
     */
    Map<String, Object> selectIncomeStats(@Param("coachId") Long coachId,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate);

    /**
     * 查询教练排名（按收入）
     */
    List<Map<String, Object>> selectCoachRankingByIncome(@Param("limit") Integer limit);

    /**
     * 查询教练排名（按学员数）
     */
    List<Map<String, Object>> selectCoachRankingByStudents(@Param("limit") Integer limit);

    /**
     * 查询教练的课程（新增方法）
     */
    @Select("SELECT * FROM course WHERE coach_id = #{coachId} " +
            "AND (#{date} IS NULL OR course_date = #{date}) " +
            "AND (#{status} IS NULL OR status = #{status}) " +
            "ORDER BY course_date DESC, start_time")
    List<Course> selectCoachCourses(@Param("coachId") Long coachId,
                                    @Param("date") String date,
                                    @Param("status") String status);
}