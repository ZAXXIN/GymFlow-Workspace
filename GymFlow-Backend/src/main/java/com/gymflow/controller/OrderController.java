package com.gymflow.controller;

import com.gymflow.common.Result;
import com.gymflow.common.annotation.PreAuthorize;
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
    @Operation(summary = "分页查询订单列表")
    @PreAuthorize("order:view")  // 查看权限（老板和前台都有）
    public Result<PageResultVO<OrderListVO>> getOrderList(@Valid @RequestBody OrderQueryDTO queryDTO) {
        PageResultVO<OrderListVO> result = orderService.getOrderList(queryDTO);
        return Result.success("查询成功", result);
    }

    @GetMapping("/detail/{orderId}")
    @Operation(summary = "获取订单详情")
    @PreAuthorize("order:detail")  // 查看详情权限（老板和前台都有）
    public Result<OrderDetailVO> getOrderDetail(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId) {
        OrderFullDTO fullDTO = orderService.getOrderDetail(orderId);
        OrderDetailVO detailVO = convertToOrderDetailVO(fullDTO);
        return Result.success("查询成功", detailVO);
    }

    @PostMapping("/create")
    @Operation(summary = "创建订单")
    @PreAuthorize("order:add")  // 创建订单权限（老板和前台都有）
    public Result<Long> createOrder(@Valid @RequestBody OrderBasicDTO orderDTO) {
        Long orderId = orderService.createOrder(orderDTO);
        return Result.success("创建订单成功", orderId);
    }

    @PutMapping("/update/{orderId}")
    @Operation(summary = "更新订单信息")
    @PreAuthorize("order:edit")  // 编辑权限（只有老板有）
    public Result<Void> updateOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId,
            @Valid @RequestBody OrderBasicDTO orderDTO) {
        orderService.updateOrder(orderId, orderDTO);
        return Result.success("更新订单成功");
    }

    @PutMapping("/updateStatus/{orderId}")
    @Operation(summary = "更新订单状态")
    @PreAuthorize("order:edit")  // 编辑权限（只有老板有）
    public Result<Void> updateOrderStatus(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId,
            @Valid @RequestBody OrderStatusDTO statusDTO) {
        orderService.updateOrderStatus(orderId, statusDTO);
        return Result.success("更新订单状态成功");
    }

    @PostMapping("/cancel/{orderId}")
    @Operation(summary = "取消订单")
    @PreAuthorize("order:cancel")  // 取消权限（老板和前台都有）
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId,
            @RequestParam(required = false) String reason) {
        orderService.cancelOrder(orderId, reason);
        return Result.success("取消订单成功");
    }

    @PostMapping("/complete/{orderId}")
    @Operation(summary = "完成订单")
    @PreAuthorize("order:edit")  // 完成权限（老板和前台都有）
    public Result<Void> completeOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId) {
        orderService.completeOrder(orderId);
        return Result.success("完成订单成功");
    }

    @DeleteMapping("/delete/{orderId}")
    @Operation(summary = "删除订单")
    @PreAuthorize("order:delete")  // 删除权限（只有老板有）
    public Result<Void> deleteOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId) {
        orderService.deleteOrder(orderId);
        return Result.success("删除订单成功");
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除订单")
    @PreAuthorize("order:delete")  // 批量删除权限（只有老板有）
    public Result<Void> batchDeleteOrders(@RequestBody List<Long> orderIds) {
        orderService.batchDeleteOrders(orderIds);
        return Result.success("批量删除成功");
    }

    @PostMapping("/pay/{orderId}")
    @Operation(summary = "订单支付")
    @PreAuthorize("order:pay")  // 支付权限（老板和前台都有）
    public Result<Void> payOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId,
            @RequestParam(required = false) String paymentMethod) {
        orderService.payOrder(orderId, paymentMethod != null ? paymentMethod : "现金");
        return Result.success("订单支付成功");
    }

    @PostMapping("/refund/{orderId}")
    @Operation(summary = "订单退款")
    @PreAuthorize("order:refund")  // 退款权限（只有老板有）
    public Result<Void> refundOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable @NotNull Long orderId,
            @RequestParam BigDecimal refundAmount,
            @RequestParam(required = false) String reason) {
        orderService.refundOrder(orderId, refundAmount, reason);
        return Result.success("订单退款成功");
    }

    @PostMapping("/member/{memberId}")
    @Operation(summary = "获取会员订单列表")
    @PreAuthorize("order:view")  // 查看权限（老板和前台都有）
    public Result<PageResultVO<OrderListVO>> getMemberOrders(
            @Parameter(description = "会员ID", required = true)
            @PathVariable @NotNull Long memberId,
            @Valid @RequestBody OrderQueryDTO queryDTO) {
        PageResultVO<OrderListVO> result = orderService.getMemberOrders(memberId, queryDTO);
        return Result.success("查询成功", result);
    }

    /**
     * 将OrderFullDTO转换为OrderDetailVO
     */
    private OrderDetailVO convertToOrderDetailVO(OrderFullDTO fullDTO) {
        OrderDetailVO vo = new OrderDetailVO();
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