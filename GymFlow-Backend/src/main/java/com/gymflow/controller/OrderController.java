package com.gymflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.OrderDTO;
import com.gymflow.entity.Order;
import com.gymflow.service.OrderService;
import com.gymflow.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 获取订单列表（分页）
     */
    @GetMapping("/list")
    public Result getOrderList(@RequestParam(defaultValue = "1") Integer pageNum,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @RequestParam(required = false) String orderType,
                               @RequestParam(required = false) String orderStatus,
                               @RequestParam(required = false) String paymentStatus,
                               @RequestParam(required = false) String keyword,
                               HttpServletRequest request) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        return orderService.getOrderList(page, orderType, orderStatus, paymentStatus, keyword, request);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/detail/{id}")
    public Result getOrderDetail(@PathVariable Long id, HttpServletRequest request) {
        return orderService.getOrderDetail(id, request);
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result createOrder(@Validated @RequestBody OrderDTO orderDTO, HttpServletRequest request) {
        return orderService.createOrder(orderDTO, request);
    }

    /**
     * 更新订单信息
     */
    @PutMapping("/update/{id}")
    public Result updateOrder(@PathVariable Long id, @RequestBody Order order, HttpServletRequest request) {
        return orderService.updateOrder(id, order, request);
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteOrder(@PathVariable Long id, HttpServletRequest request) {
        return orderService.deleteOrder(id, request);
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/status/{id}")
    public Result updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String orderStatus,
                                    HttpServletRequest request) {
        return orderService.updateOrderStatus(id, orderStatus, request);
    }

    /**
     * 更新支付状态
     */
    @PutMapping("/payment/{id}")
    public Result updatePaymentStatus(@PathVariable Long id,
                                      @RequestParam String paymentStatus,
                                      @RequestParam(required = false) String paymentMethod,
                                      HttpServletRequest request) {
        return orderService.updatePaymentStatus(id, paymentStatus, paymentMethod, request);
    }

    /**
     * 获取会员订单列表
     */
    @GetMapping("/member")
    public Result getMemberOrders(@RequestParam(required = false) String orderType,
                                  HttpServletRequest request) {
        return orderService.getMemberOrders(orderType, request);
    }

    /**
     * 订单支付
     */
    @PostMapping("/pay/{id}")
    public Result payOrder(@PathVariable Long id,
                           @RequestParam(required = false) String paymentMethod,
                           HttpServletRequest request) {
        return orderService.payOrder(id, paymentMethod, request);
    }

    /**
     * 订单退款
     */
    @PostMapping("/refund/{id}")
    public Result refundOrder(@PathVariable Long id,
                              @RequestParam(required = false) String reason,
                              HttpServletRequest request) {
        return orderService.refundOrder(id, reason, request);
    }

    /**
     * 导出订单
     */
    @GetMapping("/export")
    public Result exportOrders(@RequestParam(required = false) String startDate,
                               @RequestParam(required = false) String endDate,
                               HttpServletRequest request) {
        return orderService.exportOrders(startDate, endDate, request);
    }
}