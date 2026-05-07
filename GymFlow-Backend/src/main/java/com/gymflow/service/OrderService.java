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
     * 完成订单
     */
    void completeOrder(Long orderId);

    /**
     * 删除订单（软删除）
     */
    void deleteOrder(Long orderId);

    /**
     * 获取会员订单列表
     */
    PageResultVO<OrderListVO> getMemberOrders(Long memberId, OrderQueryDTO queryDTO);

    /**
     * 订单支付（同步完成权益激活）
     * @param orderId 订单ID
     * @param paymentMethod 支付方式（默认"前台支付"）
     * @return true-支付并激活成功，false-支付成功但激活失败（状态为PAID）
     */
    boolean payOrder(Long orderId, String paymentMethod);

    /**
     * 重试激活订单权益（仅适用于状态为PAID的订单）
     * @param orderId 订单ID
     * @return true-激活成功，false-激活失败
     */
    boolean retryActivateOrder(Long orderId);
}