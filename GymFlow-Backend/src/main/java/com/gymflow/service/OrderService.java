package com.gymflow.service;

import com.gymflow.dto.order.OrderBasicDTO;
import com.gymflow.dto.order.OrderFullDTO;
import com.gymflow.dto.order.OrderQueryDTO;
import com.gymflow.dto.order.OrderStatusDTO;
import com.gymflow.vo.OrderListVO;
import com.gymflow.vo.PageResultVO;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    /**
     * 分页查询订单列表
     */
    PageResultVO<OrderListVO> getOrderList(OrderQueryDTO queryDTO);

    /**
     * 获取订单详情
     */
    OrderFullDTO getOrderDetail(Long orderId);

    /**
     * 创建订单
     */
    Long createOrder(OrderBasicDTO orderDTO);

    /**
     * 更新订单信息
     */
    void updateOrder(Long orderId, OrderBasicDTO orderDTO);

    /**
     * 更新订单状态
     */
    void updateOrderStatus(Long orderId, OrderStatusDTO statusDTO);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, String reason);

    /**
     * 完成订单（确认收货/完成服务）
     */
    void completeOrder(Long orderId);

    /**
     * 删除订单（软删除）
     */
    void deleteOrder(Long orderId);

    /**
     * 批量删除订单
     */
    void batchDeleteOrders(List<Long> orderIds);

    /**
     * 获取会员订单列表
     */
    PageResultVO<OrderListVO> getMemberOrders(Long memberId, OrderQueryDTO queryDTO);

    /**
     * 订单支付
     */
    void payOrder(Long orderId, String paymentMethod);

    /**
     * 订单退款
     */
    void refundOrder(Long orderId, BigDecimal refundAmount, String reason);
}