package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.CheckinRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface CheckInRecordMapper extends BaseMapper<CheckinRecord> {

    /**
     * 根据会员ID查询签到记录
     */
    @Select("SELECT * FROM checkin_record WHERE member_id = #{memberId} ORDER BY checkin_time DESC")
    List<CheckinRecord> selectByMemberId(@Param("memberId") Long memberId);

    /**
     * 查询指定时间段的签到统计
     */
    @Select("SELECT DATE(checkin_time) as checkin_date, COUNT(*) as checkin_count " +
            "FROM checkin_record " +
            "WHERE checkin_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY DATE(checkin_time) " +
            "ORDER BY checkin_date")
    List<Map<String, Object>> selectCheckInStatsByDateRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询会员签到排名
     */
    @Select("SELECT member_id, COUNT(*) as checkin_count " +
            "FROM checkin_record " +
            "WHERE checkin_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY member_id " +
            "ORDER BY checkin_count DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectMemberRanking(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") Integer limit);

    /**
     * 查询会员最近一次签到时间
     */
    @Select("SELECT MAX(checkin_time) as last_checkin_time FROM checkin_record WHERE member_id = #{memberId}")
    LocalDateTime selectLastCheckInTime(@Param("memberId") Long memberId);

    /**
     * 查询今日是否已签到
     */
    @Select("SELECT COUNT(*) FROM checkin_record " +
            "WHERE member_id = #{memberId} " +
            "AND DATE(checkin_time) = CURDATE() " +
            "AND course_booking_id IS NULL")
    Integer selectTodayFreeCheckIn(@Param("memberId") Long memberId);
}