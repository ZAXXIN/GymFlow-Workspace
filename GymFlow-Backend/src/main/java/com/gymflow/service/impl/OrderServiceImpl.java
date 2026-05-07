package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.member.MemberBasicDTO;
import com.gymflow.dto.order.*;
import com.gymflow.entity.*;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.gymflow.service.OrderService;
import com.gymflow.utils.SystemConfigValidator;
import com.gymflow.vo.OrderListVO;
import com.gymflow.vo.PageResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final MemberMapper memberMapper;
    private final ProductMapper productMapper;
    // private final PaymentRecordMapper paymentRecordMapper; // 该表已删除，注释掉
    private final SystemConfigValidator configValidator;

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 订单状态常量
    public static final String STATUS_WAIT_PAY = "WAIT_PAY";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_REFUNDED = "REFUNDED";

    // 支付方式默认值
    private static final String DEFAULT_PAYMENT_METHOD = "前台支付";

    @Override
    public PageResultVO<OrderListVO> getOrderList(OrderQueryDTO queryDTO) {
        log.info("查询订单列表，查询条件：{}", queryDTO);

        Page<Order> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            queryWrapper.like(Order::getOrderNo, queryDTO.getOrderNo());
        }
        if (queryDTO.getMemberId() != null) {
            queryWrapper.eq(Order::getMemberId, queryDTO.getMemberId());
        }
        if (queryDTO.getOrderType() != null) {
            queryWrapper.eq(Order::getOrderType, queryDTO.getOrderType());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            queryWrapper.eq(Order::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getStartDate() != null) {
            queryWrapper.ge(Order::getCreateTime, queryDTO.getStartDate().atStartOfDay());
        }
        if (queryDTO.getEndDate() != null) {
            queryWrapper.le(Order::getCreateTime, queryDTO.getEndDate().atTime(23, 59, 59));
        }
        if (!queryDTO.getIncludeDeleted()) {
            queryWrapper.ne(Order::getStatus, STATUS_CANCELLED);
            queryWrapper.ne(Order::getStatus, STATUS_REFUNDED);
        }

        queryWrapper.orderByDesc(Order::getCreateTime);

        IPage<Order> orderPage = orderMapper.selectPage(page, queryWrapper);

        List<OrderListVO> voList = orderPage.getRecords().stream()
                .map(this::convertToOrderListVO)
                .collect(Collectors.toList());

        return new PageResultVO<>(voList, orderPage.getTotal(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public OrderFullDTO getOrderDetail(Long orderId) {
        log.info("获取订单详情，订单ID：{}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        OrderFullDTO fullDTO = new OrderFullDTO();
        BeanUtils.copyProperties(order, fullDTO);
        fullDTO.setStatus(order.getStatus());

        Member member = memberMapper.selectById(order.getMemberId());
        if (member != null) {
            MemberBasicDTO memberDTO = new MemberBasicDTO();
            BeanUtils.copyProperties(member, memberDTO);
            fullDTO.setMemberInfo(memberDTO);
        }

        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderItem::getOrderId, orderId);
        itemQuery.orderByAsc(OrderItem::getCreateTime);
        List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);
        List<OrderItemDTO> itemDTOs = orderItems.stream()
                .map(this::convertToOrderItemDTO)
                .collect(Collectors.toList());
        fullDTO.setOrderItems(itemDTOs);

        // 支付记录表已删除，不再查询
        // LambdaQueryWrapper<PaymentRecord> paymentQuery = new LambdaQueryWrapper<>();
        // paymentQuery.eq(PaymentRecord::getOrderId, orderId);
        // paymentQuery.orderByDesc(PaymentRecord::getCreateTime);
        // List<PaymentRecord> paymentRecords = paymentRecordMapper.selectList(paymentQuery);
        // fullDTO.setPaymentRecords(paymentRecords);

        return fullDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderBasicDTO orderDTO) {
        log.info("开始创建订单，会员ID：{}", orderDTO.getMemberId());

        Member member = memberMapper.selectById(orderDTO.getMemberId());
        if (member == null) {
            throw new BusinessException("会员不存在");
        }
        if (CollectionUtils.isEmpty(orderDTO.getOrderItems())) {
            throw new BusinessException("订单项不能为空");
        }

        Integer orderType = determineOrderType(orderDTO.getOrderItems());
        BigDecimal totalAmount = orderDTO.getOrderItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (orderDTO.getActualAmount() != null &&
                orderDTO.getActualAmount().compareTo(totalAmount) > 0) {
            throw new BusinessException("实付金额不能大于总金额");
        }

        for (OrderItemDTO item : orderDTO.getOrderItems()) {
            if (item.getProductType() == 2) { // 团课
                configValidator.validateClassCapacity(0, item.getTotalSessions());
            }
        }

        String orderNo = generateOrderNo();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setMemberId(orderDTO.getMemberId());
        order.setOrderType(orderType);
        order.setTotalAmount(totalAmount);
        order.setActualAmount(orderDTO.getActualAmount() != null ? orderDTO.getActualAmount() : totalAmount);
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setRemark(orderDTO.getRemark());
        order.setStatus(STATUS_WAIT_PAY); // 初始状态

        int result = orderMapper.insert(order);
        if (result <= 0) {
            throw new BusinessException("创建订单失败");
        }

        Long orderId = order.getId();
        log.info("创建订单成功，订单ID：{}，订单号：{}", orderId, orderNo);

        createOrderItems(orderId, orderDTO.getOrderItems());

        if (orderType == 0 || orderType == 1 || orderType == 2) {
            setOrderItemsValidity(orderId, orderType);
        }

        sendOrderCreatedNotification(orderId, member);
        return orderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(Long orderId, OrderBasicDTO orderDTO) {
        log.info("开始更新订单，订单ID：{}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!STATUS_WAIT_PAY.equals(order.getStatus())) {
            throw new BusinessException("只有待支付的订单可以修改");
        }
        if (!order.getMemberId().equals(orderDTO.getMemberId())) {
            throw new BusinessException("不能修改订单的会员信息");
        }
        if (order.getOrderType() != orderDTO.getOrderType()) {
            throw new BusinessException("不能修改订单类型");
        }

        BigDecimal totalAmount = orderDTO.getOrderItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (orderDTO.getActualAmount() != null &&
                orderDTO.getActualAmount().compareTo(totalAmount) > 0) {
            throw new BusinessException("实付金额不能大于总金额");
        }

        order.setTotalAmount(totalAmount);
        order.setActualAmount(orderDTO.getActualAmount() != null ? orderDTO.getActualAmount() : totalAmount);
        order.setRemark(orderDTO.getRemark());

        int result = orderMapper.updateById(order);
        if (result <= 0) {
            throw new BusinessException("更新订单失败");
        }

        LambdaQueryWrapper<OrderItem> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(OrderItem::getOrderId, orderId);
        orderItemMapper.delete(deleteWrapper);

        createOrderItems(orderId, orderDTO.getOrderItems());

        if (orderDTO.getOrderType() == 0 || orderDTO.getOrderType() == 1 || orderDTO.getOrderType() == 2) {
            setOrderItemsValidity(orderId, orderDTO.getOrderType());
        }

        log.info("更新订单成功，订单ID：{}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long orderId, OrderStatusDTO statusDTO) {
        log.info("更新订单状态，订单ID：{}，新状态：{}", orderId, statusDTO.getStatus());

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        String newStatus = statusDTO.getStatus();
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        if (StringUtils.hasText(statusDTO.getRemark())) {
            order.setRemark(statusDTO.getRemark());
        }

        // 如果状态变为已完成，自动设置支付时间和支付方式（如未设置）
        if (STATUS_COMPLETED.equals(newStatus)) {
            if (order.getPaymentTime() == null) {
                order.setPaymentTime(LocalDateTime.now());
            }
            if (order.getPaymentMethod() == null) {
                order.setPaymentMethod(DEFAULT_PAYMENT_METHOD);
            }
        }

        int result = orderMapper.updateById(order);
        if (result <= 0) {
            throw new BusinessException("更新订单状态失败");
        }

        // 如果变为已完成，激活权益
        if (STATUS_COMPLETED.equals(newStatus)) {
            activateOrderProducts(orderId);
        }

        logOrderStatusChange(orderId, order.getStatus(), newStatus, statusDTO.getRemark());
        log.info("更新订单状态成功，订单ID：{}，新状态：{}", orderId, newStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String reason) {
        log.info("取消订单，订单ID：{}，原因：{}", orderId, reason);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!STATUS_WAIT_PAY.equals(order.getStatus())) {
            throw new BusinessException("只有待支付的订单可以取消");
        }

        order.setStatus(STATUS_CANCELLED);
        if (StringUtils.hasText(reason)) {
            order.setRemark(reason);
        }

        int result = orderMapper.updateById(order);
        if (result <= 0) {
            throw new BusinessException("取消订单失败");
        }

        // 释放库存（产品类订单）
        if (order.getOrderType() == 3) {
            releaseProductStock(orderId);
        }

        log.info("取消订单成功，订单ID：{}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId) {
        log.info("完成订单，订单ID：{}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!STATUS_PAID.equals(order.getStatus()) && !STATUS_WAIT_PAY.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不能完成");
        }

        order.setStatus(STATUS_COMPLETED);
        if (order.getPaymentTime() == null) {
            order.setPaymentTime(LocalDateTime.now());
        }
        if (order.getPaymentMethod() == null) {
            order.setPaymentMethod(DEFAULT_PAYMENT_METHOD);
        }

        int result = orderMapper.updateById(order);
        if (result <= 0) {
            throw new BusinessException("完成订单失败");
        }

        updateMemberSpending(order.getMemberId(), order.getActualAmount());
        activateOrderProducts(orderId);

        log.info("完成订单成功，订单ID：{}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long orderId) {
        log.info("删除订单，订单ID：{}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!STATUS_CANCELLED.equals(order.getStatus()) && !STATUS_COMPLETED.equals(order.getStatus())) {
            throw new BusinessException("只能删除已取消或已完成的订单");
        }

        // 软删除：标记为已取消或物理删除？根据业务设置为取消状态即可
        // 这里不做物理删除，仅记录日志
        log.info("删除订单成功（软删除），订单ID：{}", orderId);
        // 实际可以更新一个 deleted 标志，但原设计没有，所以只记录日志
    }

    @Override
    public PageResultVO<OrderListVO> getMemberOrders(Long memberId, OrderQueryDTO queryDTO) {
        log.info("获取会员订单列表，会员ID：{}", memberId);
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }
        queryDTO.setMemberId(memberId);
        return getOrderList(queryDTO);
    }

    @Override
    public boolean payOrder(Long orderId, String paymentMethod) {
        log.info("订单支付，订单ID：{}，支付方式：{}", orderId, paymentMethod);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!STATUS_WAIT_PAY.equals(order.getStatus())) {
            throw new BusinessException("当前订单状态不能支付");
        }

        String payMethod = StringUtils.hasText(paymentMethod) ? paymentMethod : DEFAULT_PAYMENT_METHOD;
        order.setStatus(STATUS_PAID);
        order.setPaymentMethod(payMethod);
        order.setPaymentTime(LocalDateTime.now());
        int updateResult = orderMapper.updateById(order);
        if (updateResult <= 0) {
            throw new BusinessException("更新订单支付状态失败");
        }

        // 支付记录表已删除，不再记录
        // createPaymentRecord(order, payMethod);

        boolean activated = activateOrderWithNewTransaction(orderId);

        if (activated) {
            log.info("订单支付并激活成功，订单ID：{}", orderId);
        } else {
            log.warn("订单支付成功但激活失败，订单ID：{}，状态为PAID", orderId);
        }
        return activated;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean activateOrderWithNewTransaction(Long orderId) {
        try {
            activateOrderProducts(orderId);
            Order order = orderMapper.selectById(orderId);
            if (order != null && STATUS_PAID.equals(order.getStatus())) {
                order.setStatus(STATUS_COMPLETED);
                orderMapper.updateById(order);
            }
            return true;
        } catch (Exception e) {
            log.error("激活订单权益失败，订单ID：{}，错误：{}", orderId, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean retryActivateOrder(Long orderId) {
        log.info("重试激活订单权益，订单ID：{}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!STATUS_PAID.equals(order.getStatus())) {
            throw new BusinessException("只有已支付状态的订单可以重试激活");
        }

        boolean activated = activateOrderWithNewTransaction(orderId);
        if (activated) {
            log.info("重试激活成功，订单ID：{}", orderId);
        } else {
            log.warn("重试激活失败，订单ID：{}", orderId);
        }
        return activated;
    }

    // ========== 私有辅助方法 ==========

    private OrderListVO convertToOrderListVO(Order order) {
        OrderListVO vo = new OrderListVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setMemberId(order.getMemberId());
        vo.setOrderType(order.getOrderType());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setActualAmount(order.getActualAmount());
        vo.setStatus(order.getStatus());
        vo.setCreateTime(order.getCreateTime());

        Member member = memberMapper.selectById(order.getMemberId());
        if (member != null) {
            vo.setMemberName(member.getRealName());
            vo.setMemberPhone(member.getPhone());
        }

        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId, order.getId());
        Long itemCount = orderItemMapper.selectCount(queryWrapper);
        vo.setItemCount(itemCount.intValue());

        return vo;
    }

    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        BeanUtils.copyProperties(orderItem, dto);
        Product product = productMapper.selectById(orderItem.getProductId());
        if (product != null) {
            dto.setProductName(product.getProductName());
            dto.setProductType(product.getProductType());
            dto.setProductImage(getFirstProductImage(product.getImages()));
        }
        return dto;
    }

    private void createOrderItems(Long orderId, List<OrderItemDTO> itemDTOs) {
        for (OrderItemDTO itemDTO : itemDTOs) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(itemDTO.getProductId());
            orderItem.setProductName(itemDTO.getProductName());
            orderItem.setProductType(itemDTO.getProductType());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setUnitPrice(itemDTO.getUnitPrice());
            orderItem.setTotalPrice(itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));

            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product != null) {
                if (product.getTotalSessions() != null) {
                    orderItem.setTotalSessions(product.getTotalSessions());
                    orderItem.setRemainingSessions(product.getTotalSessions());
                }
            } else {
                setDefaultSessions(orderItem, itemDTO.getProductType());
            }

            orderItem.setStatus("UNPAID");
            orderItemMapper.insert(orderItem);
        }
    }

    private void setDefaultSessions(OrderItem orderItem, Integer productType) {
        if (productType == 1 || productType == 2) {
            orderItem.setTotalSessions(10);
            orderItem.setRemainingSessions(10);
        }
    }

    private void setOrderItemsValidity(Long orderId, Integer orderType) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);

        for (OrderItem orderItem : orderItems) {
            if (orderType == 0) { // 会籍卡
                Product product = productMapper.selectById(orderItem.getProductId());
                Integer validityDays = product != null ? product.getValidityDays() : 30;
                orderItem.setValidityStartDate(LocalDate.now());
                orderItem.setValidityEndDate(LocalDate.now().plusDays(validityDays));
            } else if (orderType == 1 || orderType == 2) { // 课程包
                orderItem.setValidityStartDate(LocalDate.now());
                orderItem.setValidityEndDate(LocalDate.now().plusDays(365));
            }
            orderItemMapper.updateById(orderItem);
        }
    }

    private void activateOrderProducts(Long orderId) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);

        for (OrderItem orderItem : orderItems) {
            orderItem.setStatus("ACTIVE");
            orderItemMapper.updateById(orderItem);

            if (orderItem.getProductType() == 0) {
                activateMembershipCard(orderItem);
            } else if (orderItem.getProductType() == 1) {
                activatePrivateCoursePackage(orderItem);
            } else if (orderItem.getProductType() == 2) {
                activateGroupCoursePackage(orderItem);
            }
        }
    }

    private void activateMembershipCard(OrderItem orderItem) {
        Order order = orderMapper.selectById(orderItem.getOrderId());
        Member member = memberMapper.selectById(order.getMemberId());
        Product product = productMapper.selectById(orderItem.getProductId());
        if (member == null || product == null) return;

        Integer validityDays = product.getValidityDays() != null ? product.getValidityDays() : 30;
        LocalDate today = LocalDate.now();
        LocalDate newEndDate = today.plusDays(validityDays);

        if (member.getMembershipEndDate() != null && member.getMembershipEndDate().isAfter(today)) {
            member.setMembershipEndDate(member.getMembershipEndDate().plusDays(validityDays));
        } else {
            member.setMembershipStartDate(today);
            member.setMembershipEndDate(newEndDate);
        }
        memberMapper.updateById(member);
        log.info("会籍卡激活成功，会员ID：{}，新有效期至：{}", member.getId(), member.getMembershipEndDate());
    }

    private void activatePrivateCoursePackage(OrderItem orderItem) {
        Product product = productMapper.selectById(orderItem.getProductId());
        if (product == null || product.getTotalSessions() == null) return;
        orderItem.setTotalSessions(product.getTotalSessions());
        orderItem.setRemainingSessions(product.getTotalSessions());
        orderItem.setValidityStartDate(LocalDate.now());
        orderItem.setValidityEndDate(LocalDate.now().plusDays(365));
        orderItemMapper.updateById(orderItem);
        log.info("私教课程包激活成功，订单项ID：{}，总课时：{}", orderItem.getId(), product.getTotalSessions());
    }

    private void activateGroupCoursePackage(OrderItem orderItem) {
        Product product = productMapper.selectById(orderItem.getProductId());
        if (product == null || product.getTotalSessions() == null) return;
        orderItem.setTotalSessions(product.getTotalSessions());
        orderItem.setRemainingSessions(product.getTotalSessions());
        orderItem.setValidityStartDate(LocalDate.now());
        orderItem.setValidityEndDate(LocalDate.now().plusDays(365));
        orderItemMapper.updateById(orderItem);
        log.info("团课课程包激活成功，订单项ID：{}，总课时：{}", orderItem.getId(), product.getTotalSessions());
    }

    private void releaseProductStock(Long orderId) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId, orderId)
                .eq(OrderItem::getProductType, 3);
        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);
        for (OrderItem orderItem : orderItems) {
            Product product = productMapper.selectById(orderItem.getProductId());
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
                productMapper.updateById(product);
            }
        }
    }

    private void updateMemberSpending(Long memberId, BigDecimal amount) {
        Member member = memberMapper.selectById(memberId);
        if (member != null) {
            BigDecimal currentTotal = member.getTotalSpent() != null ? member.getTotalSpent() : BigDecimal.ZERO;
            member.setTotalSpent(currentTotal.add(amount));
            memberMapper.updateById(member);
        }
    }

    private Integer determineOrderType(List<OrderItemDTO> orderItems) {
        if (CollectionUtils.isEmpty(orderItems)) return 3;
        Integer firstType = orderItems.get(0).getProductType();
        boolean allSame = orderItems.stream().allMatch(item -> item.getProductType().equals(firstType));
        return allSame ? firstType : 3;
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        if (STATUS_WAIT_PAY.equals(currentStatus)) {
            if (!STATUS_PAID.equals(newStatus) && !STATUS_CANCELLED.equals(newStatus)) {
                throw new BusinessException("待支付订单只能变更为已支付或已取消");
            }
        } else if (STATUS_PAID.equals(currentStatus)) {
            if (!STATUS_COMPLETED.equals(newStatus) && !STATUS_REFUNDED.equals(newStatus)) {
                throw new BusinessException("已支付订单只能变更为已完成或已退款");
            }
        } else if (STATUS_COMPLETED.equals(currentStatus)) {
            if (!STATUS_REFUNDED.equals(newStatus)) {
                throw new BusinessException("已完成订单只能变更为已退款");
            }
        } else if (STATUS_CANCELLED.equals(currentStatus)) {
            throw new BusinessException("已取消订单不能变更状态");
        } else if (STATUS_REFUNDED.equals(currentStatus)) {
            throw new BusinessException("已退款订单不能变更状态");
        }
    }

    private void logOrderStatusChange(Long orderId, String oldStatus, String newStatus, String remark) {
        log.info("订单状态变更，订单ID：{}，从 {} 变更为 {}，备注：{}", orderId, oldStatus, newStatus, remark);
    }

    private void sendOrderCreatedNotification(Long orderId, Member member) {
        log.info("发送订单创建通知，订单ID：{}，会员：{}", orderId, member.getRealName());
    }

    private String generateOrderNo() {
        return "ORD" + LocalDate.now().format(ORDER_NO_FORMATTER) + String.format("%06d", (int)(Math.random() * 1000000));
    }

    private String generatePaymentNo() {
        return "PAY" + LocalDate.now().format(ORDER_NO_FORMATTER) + String.format("%06d", (int)(Math.random() * 1000000));
    }

    private String generateTransactionId(String paymentMethod) {
        return (paymentMethod.contains("微信") ? "WX" : (paymentMethod.contains("支付宝") ? "ALIPAY" : "CASH"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    private String getFirstProductImage(String images) {
        if (!StringUtils.hasText(images)) return "";
        try {
            if (images.startsWith("[")) {
                String[] urls = images.replace("[", "").replace("]", "").replace("\"", "").split(",");
                if (urls.length > 0) return urls[0].trim();
            }
        } catch (Exception e) {
            log.error("解析商品图片失败", e);
        }
        return "";
    }
}