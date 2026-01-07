package com.gymflow.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.OrderDTO;
import com.gymflow.entity.Order;
import com.gymflow.utils.Result;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface OrderService {

    Result getOrderList(Page<Order> page, String orderType, String orderStatus, String paymentStatus, String keyword, HttpServletRequest request);

    Result getOrderDetail(Long id, HttpServletRequest request);

    Result createOrder(OrderDTO orderDTO, HttpServletRequest request);

    Result updateOrder(Long id, Order order, HttpServletRequest request);

    Result deleteOrder(Long id, HttpServletRequest request);

    Result updateOrderStatus(Long id, String orderStatus, HttpServletRequest request);

    Result updatePaymentStatus(Long id, String paymentStatus, String paymentMethod, HttpServletRequest request);

    Result getMemberOrders(String orderType, HttpServletRequest request);

    Result payOrder(Long id, String paymentMethod, HttpServletRequest request);

    Result refundOrder(Long id, String reason, HttpServletRequest request);

    Result exportOrders(String startDate, String endDate, HttpServletRequest request);
}