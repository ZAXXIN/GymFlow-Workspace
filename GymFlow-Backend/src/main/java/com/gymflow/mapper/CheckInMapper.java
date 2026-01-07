package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.entity.CheckIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface CheckInMapper extends BaseMapper<CheckIn> {

    /**
     * 分页查询签到记录
     */
    IPage<CheckIn> selectCheckInPage(Page<CheckIn> page,
                                     @Param("memberId") Long memberId,
                                     @Param("courseId") Long courseId,
                                     @Param("status") String status);

    /**
     * 查询会员签到记录
     */
    List<CheckIn> selectMemberCheckIns(@Param("memberId") Long memberId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * 查询课程签到记录
     */
    List<CheckIn> selectCourseCheckIns(@Param("courseId") Long courseId);

    /**
     * 查询今日签到统计
     */
    @Select("SELECT COUNT(*) as count FROM check_in WHERE DATE(checkin_time) = CURDATE()")
    Integer selectTodayCheckInCount();

    /**
     * 查询会员今日是否已签到某课程
     */
    @Select("SELECT COUNT(*) FROM check_in WHERE member_id = #{memberId} AND course_id = #{courseId} " +
            "AND DATE(checkin_time) = CURDATE() AND status = 'SUCCESS'")
    int checkMemberCourseCheckIn(@Param("memberId") Long memberId, @Param("courseId") Long courseId);

    /**
     * 更新签到状态
     */
    @Update("UPDATE check_in SET status = #{status}, notes = #{notes} WHERE id = #{checkInId}")
    int updateCheckInStatus(@Param("checkInId") Long checkInId,
                            @Param("status") String status,
                            @Param("notes") String notes);

    /**
     * 批量签到
     */
    @Update({"<script>",
            "UPDATE check_in SET status = 'SUCCESS', checkin_time = NOW(), notes = #{notes}",
            "WHERE id IN",
            "<foreach collection='checkInIds' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"})
    int batchCheckIn(@Param("checkInIds") List<Long> checkInIds, @Param("notes") String notes);

    /**
     * 查询签到统计（按天）
     */
    List<Map<String, Object>> selectCheckInStatsByDay(@Param("days") Integer days);

    /**
     * 查询活跃会员（最近7天有签到的）
     */
    List<Map<String, Object>> selectActiveMembers(@Param("days") Integer days);

    /**
     * 根据会员和课程查询签到记录
     */
    @Select("SELECT * FROM check_in WHERE member_id = #{memberId} AND course_id = #{courseId}")
    CheckIn selectByMemberAndCourse(@Param("memberId") Long memberId, @Param("courseId") Long courseId);
}