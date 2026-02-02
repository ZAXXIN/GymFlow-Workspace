package com.gymflow.controller;

import com.gymflow.common.Result;
import com.gymflow.dto.order.*;
import com.gymflow.service.OrderService;
import com.gymflow.vo.OrderListVO;
import com.gymflow.vo.OrderDetailVO;
import com.gymflow.vo.PageResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
@Tag(name = "订单管理", description = "订单管理相关接口")
@Validated
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/list")
    @Operation(summary = "分页查询订单列表", description = "根据条件分页查询订单列表")
    public Result<PageResultVO<OrderListVO>> getOrderList(@Valid @RequestBody OrderQueryDTO queryDTO) {
        try {
            PageResultVO<OrderListVO> result = orderService.getOrderList(queryDTO);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询订单列表失败：{}", e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/detail/{orderId}")
    @Operation(summary = "获取订单详情", description = "根据订单ID获取订单详细信息")
    public Result<OrderDetailVO> getOrderDetail(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId) {
        try {
            OrderFullDTO fullDTO = orderService.getOrderDetail(orderId);
            OrderDetailVO detailVO = convertToOrderDetailVO(fullDTO);
            return Result.success("查询成功", detailVO);
        } catch (Exception e) {
            log.error("获取订单详情失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "创建新订单")
    public Result<Long> createOrder(@Valid @RequestBody OrderBasicDTO orderDTO) {
        try {
            Long orderId = orderService.createOrder(orderDTO);
            return Result.success("创建订单成功", orderId);
        } catch (Exception e) {
            log.error("创建订单失败：{}", e.getMessage(), e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }

    @PutMapping("/update/{orderId}")
    @Operation(summary = "更新订单信息", description = "更新订单信息（仅限待处理订单）")
    public Result<Void> updateOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId,
            @Valid @RequestBody OrderBasicDTO orderDTO) {
        try {
            orderService.updateOrder(orderId, orderDTO);
            return Result.success("更新订单成功");
        } catch (Exception e) {
            log.error("更新订单失败：{}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @PutMapping("/updateStatus/{orderId}")
    @Operation(summary = "更新订单状态", description = "更新订单状态")
    public Result<Void> updateOrderStatus(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId,
            @Valid @RequestBody OrderStatusDTO statusDTO) {
        try {
            orderService.updateOrderStatus(orderId, statusDTO);
            return Result.success("更新订单状态成功");
        } catch (Exception e) {
            log.error("更新订单状态失败：{}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @PostMapping("/cancel/{orderId}")
    @Operation(summary = "取消订单", description = "取消订单")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId,
            @RequestParam(required = false) String reason) {
        try {
            orderService.cancelOrder(orderId, reason);
            return Result.success("取消订单成功");
        } catch (Exception e) {
            log.error("取消订单失败：{}", e.getMessage(), e);
            return Result.error("取消失败：" + e.getMessage());
        }
    }

    @PostMapping("/complete/{orderId}")
    @Operation(summary = "完成订单", description = "完成订单（确认收货/完成服务）")
    public Result<Void> completeOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId) {
        try {
            orderService.completeOrder(orderId);
            return Result.success("完成订单成功");
        } catch (Exception e) {
            log.error("完成订单失败：{}", e.getMessage(), e);
            return Result.error("完成失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{orderId}")
    @Operation(summary = "删除订单", description = "删除订单（软删除）")
    public Result<Void> deleteOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return Result.success("删除订单成功");
        } catch (Exception e) {
            log.error("删除订单失败：{}", e.getMessage(), e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除订单", description = "批量删除多个订单")
    public Result<Void> batchDeleteOrders(@RequestBody List<Long> orderIds) {
        try {
            orderService.batchDeleteOrders(orderIds);
            return Result.success("批量删除成功");
        } catch (Exception e) {
            log.error("批量删除订单失败：{}", e.getMessage(), e);
            return Result.error("批量删除失败：" + e.getMessage());
        }
    }

    @PostMapping("/pay/{orderId}")
    @Operation(summary = "订单支付", description = "订单支付")
    public Result<Void> payOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId,
            @RequestParam(required = false) String paymentMethod) {
        try {
            orderService.payOrder(orderId, paymentMethod != null ? paymentMethod : "现金");
            return Result.success("订单支付成功");
        } catch (Exception e) {
            log.error("订单支付失败：{}", e.getMessage(), e);
            return Result.error("支付失败：" + e.getMessage());
        }
    }

    @PostMapping("/refund/{orderId}")
    @Operation(summary = "订单退款", description = "订单退款")
    public Result<Void> refundOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId,
            @RequestParam BigDecimal refundAmount,
            @RequestParam(required = false) String reason) {
        try {
            orderService.refundOrder(orderId, refundAmount, reason);
            return Result.success("订单退款成功");
        } catch (Exception e) {
            log.error("订单退款失败：{}", e.getMessage(), e);
            return Result.error("退款失败：" + e.getMessage());
        }
    }

    @PostMapping("/member/{memberId}")
    @Operation(summary = "获取会员订单列表", description = "获取指定会员的订单列表")
    public Result<PageResultVO<OrderListVO>> getMemberOrders(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId,
            @Valid @RequestBody OrderQueryDTO queryDTO) {
        try {
            PageResultVO<OrderListVO> result = orderService.getMemberOrders(memberId, queryDTO);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("获取会员订单列表失败：{}", e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 将OrderFullDTO转换为OrderDetailVO
     */
    private OrderDetailVO convertToOrderDetailVO(OrderFullDTO fullDTO) {
        OrderDetailVO vo = new OrderDetailVO();

        // 设置基本信息
        vo.setId(fullDTO.getId());
        vo.setOrderNo(fullDTO.getOrderNo());
        vo.setMemberId(fullDTO.getMemberInfo() != null ? fullDTO.getMemberInfo().getId() : null);

        if (fullDTO.getMemberInfo() != null) {
            vo.setMemberName(fullDTO.getMemberInfo().getRealName());
            vo.setMemberPhone(fullDTO.getMemberInfo().getPhone());
        }

        vo.setOrderType(fullDTO.getOrderType());
        vo.setOrderTypeDesc(fullDTO.getOrderTypeDesc());
        vo.setTotalAmount(fullDTO.getTotalAmount());
        vo.setActualAmount(fullDTO.getActualAmount());
        vo.setPaymentMethod(fullDTO.getPaymentMethod());
        vo.setPaymentStatus(fullDTO.getPaymentStatus());
        vo.setPaymentStatusDesc(fullDTO.getPaymentStatusDesc());
        vo.setPaymentTime(fullDTO.getPaymentTime());
        vo.setOrderStatus(fullDTO.getOrderStatus());
        vo.setOrderStatusDesc(fullDTO.getOrderStatusDesc());
        vo.setRemark(fullDTO.getRemark());
        vo.setCreateTime(fullDTO.getCreateTime());
        vo.setUpdateTime(fullDTO.getUpdateTime());
        vo.setOrderItems(fullDTO.getOrderItems());

        return vo;
    }
}