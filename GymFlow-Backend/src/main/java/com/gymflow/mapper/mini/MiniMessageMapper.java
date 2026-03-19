package com.gymflow.mapper.mini;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.mini.MiniMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MiniMessageMapper extends BaseMapper<MiniMessage> {

    /**
     * 查询用户的消息列表（按时间倒序）
     */
    @Select("SELECT * FROM mini_message WHERE user_id = #{userId} AND user_type = #{userType} " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<MiniMessage> selectUserMessages(@Param("userId") Long userId,
                                         @Param("userType") Integer userType,
                                         @Param("offset") Integer offset,
                                         @Param("limit") Integer limit);

    /**
     * 获取用户未读消息数量
     */
    @Select("SELECT COUNT(*) FROM mini_message WHERE user_id = #{userId} " +
            "AND user_type = #{userType} AND is_read = 0")
    Integer selectUnreadCount(@Param("userId") Long userId,
                              @Param("userType") Integer userType);

    /**
     * 批量标记消息为已读
     */
    @Update("UPDATE mini_message SET is_read = 1 WHERE id IN (${ids})")
    int batchMarkAsRead(@Param("ids") String ids);

    /**
     * 标记所有消息为已读
     */
    @Update("UPDATE mini_message SET is_read = 1 WHERE user_id = #{userId} AND user_type = #{userType}")
    int markAllAsRead(@Param("userId") Long userId,
                      @Param("userType") Integer userType);
}