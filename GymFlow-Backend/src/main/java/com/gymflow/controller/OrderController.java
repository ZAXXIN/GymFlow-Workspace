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
    @PreAuthorize("order:menu")
    public Result<PageResultVO<OrderListVO>> getOrderList(@Valid @RequestBody OrderQueryDTO queryDTO) {
        PageResultVO<OrderListVO> result = orderService.getOrderList(queryDTO);
        return Result.success("查询成功", result);
    }

    @GetMapping("/detail/{orderId}")
    @Operation(summary = "获取订单详情")
    @PreAuthorize("order:detail")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable @NotNull Long orderId) {
        OrderFullDTO fullDTO = orderService.getOrderDetail(orderId);
        OrderDetailVO detailVO = convertToOrderDetailVO(fullDTO);
        return Result.success("查询成功", detailVO);
    }

    @PostMapping("/create")
    @Operation(summary = "创建订单")
    @PreAuthorize("order:add")
    public Result<Long> createOrder(@Valid @RequestBody OrderBasicDTO orderDTO) {
        Long orderId = orderService.createOrder(orderDTO);
        return Result.success("创建订单成功", orderId);
    }

    @PutMapping("/update/{orderId}")
    @Operation(summary = "更新订单信息")
    @PreAuthorize("order:edit")
    public Result<Void> updateOrder(@PathVariable @NotNull Long orderId,
                                    @Valid @RequestBody OrderBasicDTO orderDTO) {
        orderService.updateOrder(orderId, orderDTO);
        return Result.success("更新订单成功");
    }

    @PutMapping("/updateStatus/{orderId}")
    @Operation(summary = "更新订单状态")
    @PreAuthorize("order:edit")
    public Result<Void> updateOrderStatus(@PathVariable @NotNull Long orderId,
                                          @Valid @RequestBody OrderStatusDTO statusDTO) {
        orderService.updateOrderStatus(orderId, statusDTO);
        return Result.success("更新订单状态成功");
    }

    @PostMapping("/cancel/{orderId}")
    @Operation(summary = "取消订单（仅限待支付订单）")
    @PreAuthorize("order:cancel")
    public Result<Void> cancelOrder(@PathVariable @NotNull Long orderId,
                                    @RequestParam(required = false) String reason) {
        orderService.cancelOrder(orderId, reason);
        return Result.success("取消订单成功");
    }

    @PostMapping("/complete/{orderId}")
    @Operation(summary = "完成订单")
    @PreAuthorize("order:edit")
    public Result<Void> completeOrder(@PathVariable @NotNull Long orderId) {
        orderService.completeOrder(orderId);
        return Result.success("完成订单成功");
    }

    @DeleteMapping("/delete/{orderId}")
    @Operation(summary = "删除订单")
    @PreAuthorize("order:delete")
    public Result<Void> deleteOrder(@PathVariable @NotNull Long orderId) {
        orderService.deleteOrder(orderId);
        return Result.success("删除订单成功");
    }

    @PostMapping("/pay/{orderId}")
    @Operation(summary = "订单支付（同步完成权益激活）")
    @PreAuthorize("order:edit")
    public Result<Boolean> payOrder(@PathVariable @NotNull Long orderId,
                                    @RequestParam(required = false) String paymentMethod) {
        boolean activated = orderService.payOrder(orderId, paymentMethod);
        if (activated) {
            return Result.success("支付成功，订单已完成", true);
        } else {
            return Result.error("支付成功，但权益激活失败，请稍后重试激活");
        }
    }

    @PostMapping("/retry-activate/{orderId}")
    @Operation(summary = "重试激活订单权益（仅限已支付状态）")
    @PreAuthorize("order:edit")
    public Result<Boolean> retryActivateOrder(@PathVariable @NotNull Long orderId) {
        boolean activated = orderService.retryActivateOrder(orderId);
        if (activated) {
            return Result.success("激活成功", true);
        } else {
            return Result.error("激活失败，请联系管理员");
        }
    }

    @PostMapping("/member/{memberId}")
    @Operation(summary = "获取会员订单列表")
    @PreAuthorize("order:view")
    public Result<PageResultVO<OrderListVO>> getMemberOrders(@PathVariable @NotNull Long memberId,
                                                             @Valid @RequestBody OrderQueryDTO queryDTO) {
        PageResultVO<OrderListVO> result = orderService.getMemberOrders(memberId, queryDTO);
        return Result.success("查询成功", result);
    }

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
        vo.setTotalAmount(fullDTO.getTotalAmount());
        vo.setActualAmount(fullDTO.getActualAmount());
        vo.setPaymentMethod(fullDTO.getPaymentMethod());
        vo.setPaymentTime(fullDTO.getPaymentTime());
        vo.setStatus(fullDTO.getStatus());
        vo.setRemark(fullDTO.getRemark());
        vo.setCreateTime(fullDTO.getCreateTime());
        vo.setUpdateTime(fullDTO.getUpdateTime());
        vo.setOrderItems(fullDTO.getOrderItems());
        return vo;
    }
}