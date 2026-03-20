package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gymflow.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 查询会员可用的课时
     */
    @Select("SELECT * FROM order_item WHERE member_id = #{memberId} " +
            "AND product_type = 1 AND status = 'ACTIVE' " +
            "AND remaining_sessions > 0 ORDER BY validity_end_date ASC")
    List<OrderItem> selectAvailableSessions(Long memberId);

    /**
     * 扣减课时
     */
    @Update("UPDATE order_item SET remaining_sessions = remaining_sessions - 1 " +
            "WHERE id = #{id} AND remaining_sessions > 0")
    int deductSession(Long id);

    /**
     * 恢复课时（取消预约时）
     */
    @Update("UPDATE order_item SET remaining_sessions = remaining_sessions + 1 " +
            "WHERE id = #{id}")
    int restoreSession(Long id);
}