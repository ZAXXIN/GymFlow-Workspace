package com.gymflow.mapper.mini;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.mini.MiniCheckinCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface MiniCheckinCodeMapper extends BaseMapper<MiniCheckinCode> {

    /**
     * 根据预约ID查询签到码
     */
    @Select("SELECT * FROM mini_checkin_code WHERE booking_id = #{bookingId}")
    MiniCheckinCode selectByBookingId(@Param("bookingId") Long bookingId);

    /**
     * 根据数字码查询签到码
     */
    @Select("SELECT * FROM mini_checkin_code WHERE digital_code = #{digitalCode}")
    MiniCheckinCode selectByDigitalCode(@Param("digitalCode") String digitalCode);

    /**
     * 查询过期的签到码
     */
    @Select("SELECT * FROM mini_checkin_code WHERE status = 0 AND valid_end_time < #{now}")
    List<MiniCheckinCode> selectExpiredCodes(@Param("now") LocalDateTime now);

    /**
     * 批量更新过期状态
     */
    @Update("UPDATE mini_checkin_code SET status = 2 WHERE id IN (${ids})")
    int batchUpdateExpired(@Param("ids") String ids);
}