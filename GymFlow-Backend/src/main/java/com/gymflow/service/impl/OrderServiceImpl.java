package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.OrderDTO;
import com.gymflow.entity.Order;
import com.gymflow.mapper.OrderMapper;
import com.gymflow.service.OrderService;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Result getOrderList(Page<Order> page, String orderType, String orderStatus, String paymentStatus, String keyword, HttpServletRequest request) {
        try {
            QueryWrapper<Order> queryWrapper = new QueryWrapper<>();

            if (orderType != null && !orderType.isEmpty()) {
                queryWrapper.eq("order_type", orderType);
            }

            if (orderStatus != null && !orderStatus.isEmpty()) {
                queryWrapper.eq("order_status", orderStatus);
            }

            if (paymentStatus != null && !paymentStatus.isEmpty()) {
                queryWrapper.eq("payment_status", paymentStatus);
            }

            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.like("order_no", keyword)
                        .or()
                        .like("member_name", keyword)
                        .or()
                        .like("product_name", keyword);
            }

            queryWrapper.orderByDesc("create_time");

            Page<Order> result = orderMapper.selectPage(page, queryWrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result getOrderDetail(Long id, HttpServletRequest request) {
        try {
            Order order = orderMapper.selectById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }
            return Result.success(order);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result createOrder(OrderDTO orderDTO, HttpServletRequest request) {
        try {
            Order order = new Order();
            order.setOrderNo("O" + System.currentTimeMillis() + (int)(Math.random() * 1000));
            order.setMemberId(orderDTO.getMemberId());
            order.setOrderType(orderDTO.getOrderType());
            order.setProductId(orderDTO.getProductId());
            order.setProductName(orderDTO.getProductName());
            order.setQuantity(orderDTO.getQuantity());
            order.setUnitPrice(orderDTO.getUnitPrice());

            // 计算总金额
            BigDecimal totalAmount = orderDTO.getUnitPrice().multiply(new BigDecimal(orderDTO.getQuantity()));
            order.setTotalAmount(totalAmount);

            // 计算实付金额
            BigDecimal discountAmount = orderDTO.getDiscountAmount() != null ? orderDTO.getDiscountAmount() : BigDecimal.ZERO;
            order.setDiscountAmount(discountAmount);
            order.setActualAmount(totalAmount.subtract(discountAmount));

            order.setPaymentMethod(orderDTO.getPaymentMethod());
            order.setPaymentStatus("UNPAID");
            order.setOrderStatus(com.gymflow.enums.OrderStatus.PENDING);
            order.setRemark(orderDTO.getRemark());
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            // TODO: 需要查询会员姓名并设置
            order.setMemberName("会员");

            int result = orderMapper.insert(order);
            if (result > 0) {
                return Result.success("订单创建成功", order);
            } else {
                return Result.error("订单创建失败");
            }
        } catch (Exception e) {
            throw new RuntimeException("创建订单失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateOrder(Long id, Order order, HttpServletRequest request) {
        try {
            Order existingOrder = orderMapper.selectById(id);
            if (existingOrder == null) {
                return Result.error("订单不存在");
            }

            order.setId(id);
            order.setUpdateTime(LocalDateTime.now());
            int result = orderMapper.updateById(order);

            if (result > 0) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result deleteOrder(Long id, HttpServletRequest request) {
        try {
            int result = orderMapper.deleteById(id);
            if (result > 0) {
                return Result.success("订单删除成功");
            } else {
                return Result.error("订单删除失败");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @Override
    public Result updateOrderStatus(Long id, String orderStatus, HttpServletRequest request) {
        try {
            int result = orderMapper.updateOrderStatus(id, orderStatus);
            if (result > 0) {
                return Result.success("订单状态更新成功");
            } else {
                return Result.error("订单状态更新失败");
            }
        } catch (Exception e) {
            return Result.error("状态更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result updatePaymentStatus(Long id, String paymentStatus, String paymentMethod, HttpServletRequest request) {
        try {
            int result = orderMapper.updatePaymentStatus(id, paymentStatus, paymentMethod);
            if (result > 0) {
                // 如果支付成功，更新订单状态为已完成
                if ("PAID".equals(paymentStatus)) {
                    orderMapper.updateOrderStatus(id, "COMPLETED");
                }
                return Result.success("支付状态更新成功");
            } else {
                return Result.error("支付状态更新失败");
            }
        } catch (Exception e) {
            return Result.error("支付状态更新失败：" + e.getMessage());
        }
    }

    @Override
    public Result getMemberOrders(String orderType, HttpServletRequest request) {
        try {
            // TODO: 需要获取当前会员ID
            Long memberId = 1L; // 临时写死，实际应从token获取

            return Result.success(orderMapper.selectMemberOrders(memberId, orderType));
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @Override
    public Result payOrder(Long id, String paymentMethod, HttpServletRequest request) {
        try {
            Order order = orderMapper.selectById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }

            if (!"UNPAID".equals(order.getPaymentStatus())) {
                return Result.error("订单已支付或已退款");
            }

            int result = orderMapper.updatePaymentStatus(id, "PAID", paymentMethod != null ? paymentMethod : "WECHAT");
            if (result > 0) {
                orderMapper.updateOrderStatus(id, "COMPLETED");
                return Result.success("支付成功");
            } else {
                return Result.error("支付失败");
            }
        } catch (Exception e) {
            return Result.error("支付失败：" + e.getMessage());
        }
    }

    @Override
    public Result refundOrder(Long id, String reason, HttpServletRequest request) {
        try {
            Order order = orderMapper.selectById(id);
            if (order == null) {
                return Result.error("订单不存在");
            }

            if (!"PAID".equals(order.getPaymentStatus())) {
                return Result.error("订单未支付，无法退款");
            }

            int result = orderMapper.updatePaymentStatus(id, "REFUNDED", order.getPaymentMethod());
            if (result > 0) {
                orderMapper.updateOrderStatus(id, "CANCELLED");
                return Result.success("退款成功");
            } else {
                return Result.error("退款失败");
            }
        } catch (Exception e) {
            return Result.error("退款失败：" + e.getMessage());
        }
    }

    @Override
    public Result exportOrders(String startDate, String endDate, HttpServletRequest request) {
        // TODO: 实现导出订单逻辑
        return Result.success("导出订单功能待实现");
    }
}