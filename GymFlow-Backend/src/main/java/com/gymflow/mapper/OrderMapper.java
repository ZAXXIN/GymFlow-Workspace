package com.gymflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 分页查询订单列表
     */
    IPage<Order> selectOrderPage(Page<Order> page,
                                 @Param("orderType") String orderType,
                                 @Param("orderStatus") String orderStatus,
                                 @Param("paymentStatus") String paymentStatus,
                                 @Param("keyword") String keyword);

    /**
     * 查询会员订单
     */
    List<Order> selectMemberOrders(@Param("memberId") Long memberId,
                                   @Param("orderType") String orderType);

    /**
     * 根据订单号查询订单
     */
    @Select("SELECT * FROM `order` WHERE order_no = #{orderNo}")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 更新订单支付状态
     */
    @Update("UPDATE `order` SET payment_status = #{paymentStatus}, payment_time = NOW(), " +
            "payment_method = #{paymentMethod} WHERE id = #{orderId}")
    int updatePaymentStatus(@Param("orderId") Long orderId,
                            @Param("paymentStatus") String paymentStatus,
                            @Param("paymentMethod") String paymentMethod);

    /**
     * 更新订单状态
     */
    @Update("UPDATE `order` SET order_status = #{orderStatus} WHERE id = #{orderId}")
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("orderStatus") String orderStatus);

    /**
     * 查询今日订单统计
     */
    @Select("SELECT COUNT(*) as count, SUM(actual_amount) as amount FROM `order` " +
            "WHERE DATE(create_time) = CURDATE() AND payment_status = 'PAID'")
    Map<String, Object> selectTodayOrderStats();

    /**
     * 查询时间段内的订单统计
     */
    Map<String, Object> selectOrderStatsByDateRange(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * 查询收入统计（按天）
     */
    List<Map<String, Object>> selectIncomeStatsByDay(@Param("days") Integer days);

    /**
     * 查询热销商品
     */
    List<Map<String, Object>> selectHotProducts(@Param("limit") Integer limit);

    /**
     * 查询未支付订单（超过30分钟自动取消）
     */
    @Select("SELECT * FROM `order` WHERE payment_status = 'UNPAID' AND " +
            "TIMESTAMPDIFF(MINUTE, create_time, NOW()) > 30")
    List<Order> selectUnpaidTimeoutOrders();
}