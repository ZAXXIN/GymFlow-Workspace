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
import com.gymflow.vo.OrderListVO;
import com.gymflow.vo.PageResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final MemberMapper memberMapper;
    private final ProductMapper productMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final ProductDetailMapper productDetailMapper;

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public PageResultVO<OrderListVO> getOrderList(OrderQueryDTO queryDTO) {
        log.info("查询订单列表，查询条件：{}", queryDTO);

        // 创建分页对象
        Page<Order> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();

        // 订单编号查询
        if (StringUtils.hasText(queryDTO.getOrderNo())) {
            queryWrapper.like(Order::getOrderNo, queryDTO.getOrderNo());
        }

        // 会员ID查询
        if (queryDTO.getMemberId() != null) {
            queryWrapper.eq(Order::getMemberId, queryDTO.getMemberId());
        }

        // 订单类型查询
        if (queryDTO.getOrderType() != null) {
            queryWrapper.eq(Order::getOrderType, queryDTO.getOrderType());
        }

        // 支付状态查询
        if (queryDTO.getPaymentStatus() != null) {
            queryWrapper.eq(Order::getPaymentStatus, queryDTO.getPaymentStatus());
        }

        // 订单状态查询
        if (StringUtils.hasText(queryDTO.getOrderStatus())) {
            queryWrapper.eq(Order::getOrderStatus, queryDTO.getOrderStatus());
        }

        // 创建时间范围查询
        if (queryDTO.getStartDate() != null) {
            queryWrapper.ge(Order::getCreateTime, queryDTO.getStartDate().atStartOfDay());
        }
        if (queryDTO.getEndDate() != null) {
            queryWrapper.le(Order::getCreateTime, queryDTO.getEndDate().atTime(23, 59, 59));
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Order::getCreateTime);

        // 执行分页查询
        IPage<Order> orderPage = orderMapper.selectPage(page, queryWrapper);

        // 转换为VO列表
        List<OrderListVO> voList = orderPage.getRecords().stream()
                .map(this::convertToOrderListVO)
                .collect(Collectors.toList());

        return new PageResultVO<>(voList, orderPage.getTotal(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public OrderFullDTO getOrderDetail(Long orderId) {
        log.info("获取订单详情，订单ID：{}", orderId);

        // 获取订单信息
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 构建完整DTO
        OrderFullDTO fullDTO = new OrderFullDTO();
        BeanUtils.copyProperties(order, fullDTO);

        // 获取会员信息
        Member member = memberMapper.selectById(order.getMemberId());
        if (member != null) {
            MemberBasicDTO memberDTO = new MemberBasicDTO();
            BeanUtils.copyProperties(member, memberDTO);
            fullDTO.setMemberInfo(memberDTO);
        }

        // 获取订单项列表
        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderItem::getOrderId, orderId);
        itemQuery.orderByAsc(OrderItem::getCreateTime);

        List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);
        List<OrderItemDTO> itemDTOs = orderItems.stream()
                .map(this::convertToOrderItemDTO)
                .collect(Collectors.toList());

        fullDTO.setOrderItems(itemDTOs);

        // 获取支付记录
        LambdaQueryWrapper<PaymentRecord> paymentQuery = new LambdaQueryWrapper<>();
        paymentQuery.eq(PaymentRecord::getOrderId, orderId);
        paymentQuery.orderByDesc(PaymentRecord::getCreateTime);

        List<PaymentRecord> paymentRecords = paymentRecordMapper.selectList(paymentQuery);
        fullDTO.setPaymentRecords(paymentRecords);

        return fullDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderBasicDTO orderDTO) {
        log.info("开始创建订单，会员ID：{}", orderDTO.getMemberId());

        // 验证会员信息
        Member member = memberMapper.selectById(orderDTO.getMemberId());
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 验证订单项
        if (CollectionUtils.isEmpty(orderDTO.getOrderItems())) {
            throw new BusinessException("订单项不能为空");
        }

        // 计算总金额
        BigDecimal totalAmount = orderDTO.getOrderItems().stream()
                .map(item -> {
                    if (item.getUnitPrice() != null && item.getQuantity() != null) {
                        return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 生成订单编号
        String orderNo = generateOrderNo();

        // 创建订单记录
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setMemberId(orderDTO.getMemberId());
        order.setOrderType(orderDTO.getOrderType());
        order.setTotalAmount(totalAmount);
        order.setActualAmount(totalAmount); // 默认实际支付金额等于总金额
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setPaymentStatus(0); // 待支付
        order.setOrderStatus("PENDING"); // 待处理
        order.setRemark(orderDTO.getRemark());

        // 保存订单
        int result = orderMapper.insert(order);
        if (result <= 0) {
            throw new BusinessException("创建订单失败");
        }

        Long orderId = order.getId();
        log.info("创建订单成功，订单ID：{}，订单号：{}", orderId, orderNo);

        // 创建订单项
        createOrderItems(orderId, orderDTO.getOrderItems());

        // 如果是会籍卡、私教课、团课，设置有效期
        if (orderDTO.getOrderType() == 0 || orderDTO.getOrderType() == 1 || orderDTO.getOrderType() == 2) {
            setOrderItemsValidity(orderId, orderDTO.getOrderType());
        }

        // 发送订单创建通知（可以异步处理）
        sendOrderCreatedNotification(orderId, member);

        return orderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(Long orderId, OrderBasicDTO orderDTO) {
        log.info("开始更新订单，订单ID：{}", orderId);

        // 验证订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 只能更新待处理的订单
        if (!"PENDING".equals(order.getOrderStatus())) {
            throw new BusinessException("只有待处理的订单可以修改");
        }

        // 验证会员信息
        if (!order.getMemberId().equals(orderDTO.getMemberId())) {
            throw new BusinessException("不能修改订单的会员信息");
        }

        // 验证订单类型
        if (order.getOrderType() != orderDTO.getOrderType()) {
            throw new BusinessException("不能修改订单类型");
        }

        // 重新计算总金额
        BigDecimal totalAmount = orderDTO.getOrderItems().stream()
                .map(item -> {
                    if (item.getUnitPrice() != null && item.getQuantity() != null) {
                        return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 更新订单信息
        order.setTotalAmount(totalAmount);
        order.setActualAmount(totalAmount);
        order.setRemark(orderDTO.getRemark());

        // 更新订单
        int result = orderMapper.updateById(order);
        if (result <= 0) {
            throw new BusinessException("更新订单失败");
        }

        // 删除原有订单项
        LambdaQueryWrapper<OrderItem> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(OrderItem::getOrderId, orderId);
        orderItemMapper.delete(deleteWrapper);

        // 创建新的订单项
        createOrderItems(orderId, orderDTO.getOrderItems());

        // 重新设置有效期
        if (orderDTO.getOrderType() == 0 || orderDTO.getOrderType() == 1 || orderDTO.getOrderType() == 2) {
            setOrderItemsValidity(orderId, orderDTO.getOrderType());
        }

        log.info("更新订单成功，订单ID：{}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long orderId, OrderStatusDTO statusDTO) {
        log.info("更新订单状态，订单ID：{}，新状态：{}", orderId, statusDTO.getOrderStatus());

        // 验证订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 验证状态流转合法性
        validateStatusTransition(order.getOrderStatus(), statusDTO.getOrderStatus());

        // 更新订单状态
        order.setOrderStatus(statusDTO.getOrderStatus());
        if (StringUtils.hasText(statusDTO.getRemark())) {
            order.setRemark(statusDTO.getRemark());
        }

        int result = orderMapper.updateById(order);
        if (result <= 0) {
            throw new BusinessException("更新订单状态失败");
        }

        // 记录状态变更日志
        logOrderStatusChange(orderId, order.getOrderStatus(), statusDTO.getOrderStatus(), statusDTO.getRemark());

        log.info("更新订单状态成功，订单ID：{}，新状态：{}", orderId, statusDTO.getOrderStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String reason) {
        log.info("取消订单，订单ID：{}，原因：{}", orderId, reason);

        // 验证订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 检查订单状态是否可以取消
        if (!canCancelOrder(order)) {
            throw new BusinessException("当前订单状态不能取消");
        }

        // 更新订单状态
        order.setOrderStatus("CANCELLED");
        if (StringUtils.hasText(reason)) {
            order.setRemark(reason);
        }

        int result = orderMapper.updateById(order);
        if (result <= 0) {
            throw new BusinessException("取消订单失败");
        }

        // 如果是已支付订单，需要退款
        if (order.getPaymentStatus() == 1) {
            processOrderRefund(orderId, order.getActualAmount(), reason);
        }

        // 释放库存（如果是产品类订单）
        if (order.getOrderType() == 3) {
            releaseProductStock(orderId);
        }

        log.info("取消订单成功，订单ID：{}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId) {
        log.info("完成订单，订单ID：{}", orderId);

        // 验证订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 检查订单状态是否可以完成
        if (!canCompleteOrder(order)) {
            throw new BusinessException("当前订单状态不能完成");
        }

        // 更新订单状态
        order.setOrderStatus("COMPLETED");

        int result = orderMapper.updateById(order);
        if (result <= 0) {
            throw new BusinessException("完成订单失败");
        }

        // 更新会员消费统计
        updateMemberSpending(order.getMemberId(), order.getActualAmount());

        // 激活相关产品（会籍卡、课程等）
        activateOrderProducts(orderId);

        log.info("完成订单成功，订单ID：{}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long orderId) {
        log.info("删除订单，订单ID：{}", orderId);

        // 验证订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 只能删除已取消或已完成的订单
        if (!"CANCELLED".equals(order.getOrderStatus()) && !"COMPLETED".equals(order.getOrderStatus())) {
            throw new BusinessException("只能删除已取消或已完成的订单");
        }

        // 软删除：标记为已删除状态
        order.setOrderStatus("DELETED");

        int result = orderMapper.updateById(order);
        if (result <= 0) {
            throw new BusinessException("删除订单失败");
        }

        log.info("删除订单成功，订单ID：{}", orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteOrders(List<Long> orderIds) {
        log.info("批量删除订单，ID列表：{}", orderIds);

        if (CollectionUtils.isEmpty(orderIds)) {
            log.warn("批量删除订单ID列表为空");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (Long orderId : orderIds) {
            try {
                deleteOrder(orderId);
                successCount++;
            } catch (Exception e) {
                log.error("删除订单失败，ID：{}，错误：{}", orderId, e.getMessage());
                failCount++;
            }
        }

        log.info("批量删除完成，成功：{}，失败：{}", successCount, failCount);
    }

    @Override
    public PageResultVO<OrderListVO> getMemberOrders(Long memberId, OrderQueryDTO queryDTO) {
        log.info("获取会员订单列表，会员ID：{}", memberId);

        // 验证会员是否存在
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 设置会员ID查询条件
        queryDTO.setMemberId(memberId);

        // 调用通用查询方法
        return getOrderList(queryDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, String paymentMethod) {
        log.info("订单支付，订单ID：{}，支付方式：{}", orderId, paymentMethod);

        // 验证订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 检查订单状态是否可以支付
        if (order.getPaymentStatus() == 1) {
            throw new BusinessException("订单已支付");
        }

        if (!"PENDING".equals(order.getOrderStatus())) {
            throw new BusinessException("当前订单状态不能支付");
        }

        // 生成支付流水号
        String paymentNo = generatePaymentNo();

        // 创建支付记录
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrderId(orderId);
        paymentRecord.setPaymentNo(paymentNo);
        paymentRecord.setPaymentMethod(paymentMethod);
        paymentRecord.setPaymentAmount(order.getActualAmount());
        paymentRecord.setPaymentStatus(1); // 已支付
        paymentRecord.setPaymentTime(LocalDateTime.now());
        paymentRecord.setTransactionId(generateTransactionId(paymentMethod));
        paymentRecord.setRemark("订单支付");

        int result = paymentRecordMapper.insert(paymentRecord);
        if (result <= 0) {
            throw new BusinessException("创建支付记录失败");
        }

        // 更新订单支付状态
        order.setPaymentStatus(1);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentTime(LocalDateTime.now());
        order.setOrderStatus("PROCESSING"); // 处理中

        int updateResult = orderMapper.updateById(order);
        if (updateResult <= 0) {
            throw new BusinessException("更新订单支付状态失败");
        }

        // 扣减库存（如果是产品类订单）
        if (order.getOrderType() == 3) {
            deductProductStock(orderId);
        }

        log.info("订单支付成功，订单ID：{}，支付流水号：{}", orderId, paymentNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundOrder(Long orderId, BigDecimal refundAmount, String reason) {
        log.info("订单退款，订单ID：{}，退款金额：{}", orderId, refundAmount);

        // 验证订单是否存在
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 检查订单状态是否可以退款
        if (order.getPaymentStatus() != 1) {
            throw new BusinessException("订单未支付，不能退款");
        }

        // 检查退款金额是否合法
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("退款金额必须大于0");
        }

        if (refundAmount.compareTo(order.getActualAmount()) > 0) {
            throw new BusinessException("退款金额不能超过实际支付金额");
        }

        // 创建退款记录
        PaymentRecord refundRecord = new PaymentRecord();
        refundRecord.setOrderId(orderId);
        refundRecord.setPaymentNo(generatePaymentNo());
        refundRecord.setPaymentMethod(order.getPaymentMethod());
        refundRecord.setPaymentAmount(refundAmount.negate()); // 负数表示退款
        refundRecord.setPaymentStatus(1);
        refundRecord.setPaymentTime(LocalDateTime.now());
        refundRecord.setRefundAmount(refundAmount);
        refundRecord.setRefundTime(LocalDateTime.now());
        refundRecord.setRemark(reason);

        int result = paymentRecordMapper.insert(refundRecord);
        if (result <= 0) {
            throw new BusinessException("创建退款记录失败");
        }

        // 更新订单状态为已退款
        order.setOrderStatus("REFUNDED");
        if (StringUtils.hasText(reason)) {
            order.setRemark(reason);
        }

        int updateResult = orderMapper.updateById(order);
        if (updateResult <= 0) {
            throw new BusinessException("更新订单状态失败");
        }

        // 更新订单实际金额（减去退款金额）
        BigDecimal newActualAmount = order.getActualAmount().subtract(refundAmount);
        order.setActualAmount(newActualAmount);
        orderMapper.updateById(order);

        log.info("订单退款成功，订单ID：{}，退款金额：{}", orderId, refundAmount);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 将Order实体转换为OrderListVO
     */
    private OrderListVO convertToOrderListVO(Order order) {
        OrderListVO vo = new OrderListVO();

        // 设置基本信息
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setMemberId(order.getMemberId());
//        vo.setOrderType(order.getOrderType());
//        vo.setTotalAmount(order.getTotalAmount());
//        vo.setActualAmount(order.getActualAmount());
//        vo.setPaymentStatus(order.getPaymentStatus());
//        vo.setOrderStatus(order.getOrderStatus());
//        vo.setCreateTime(order.getCreateTime());

        // 获取会员信息
        Member member = memberMapper.selectById(order.getMemberId());
        if (member != null) {
            vo.setMemberName(member.getRealName());
            vo.setMemberPhone(member.getPhone());
        }

        // 设置支付状态描述
        vo.setPaymentStatusDesc(getPaymentStatusDesc(order.getPaymentStatus()));

        // 设置订单状态描述
        vo.setOrderStatusDesc(getOrderStatusDesc(order.getOrderStatus()));

        // 获取订单项数量
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId, order.getId());
        Long itemCount = orderItemMapper.selectCount(queryWrapper);
        vo.setItemCount(itemCount.intValue());

        return vo;
    }

    /**
     * 将OrderItem实体转换为OrderItemDTO
     */
    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        BeanUtils.copyProperties(orderItem, dto);

        // 查询商品信息
        Product product = productMapper.selectById(orderItem.getProductId());
        if (product != null) {
            dto.setProductName(product.getProductName());
            dto.setProductType(product.getProductType());
            dto.setProductImage(getFirstProductImage(product.getImages()));
        }

        return dto;
    }

    /**
     * 创建订单项
     */
    private void createOrderItems(Long orderId, List<OrderItemDTO> itemDTOs) {
        log.info("为订单ID {} 创建订单项，共 {} 个", orderId, itemDTOs.size());

        for (OrderItemDTO itemDTO : itemDTOs) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setProductId(itemDTO.getProductId());
            orderItem.setProductName(itemDTO.getProductName());
            orderItem.setProductType(itemDTO.getProductType());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setUnitPrice(itemDTO.getUnitPrice());
            orderItem.setTotalPrice(itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));

            // 如果是会籍卡、私教课、团课，设置默认课时数
            if (itemDTO.getProductType() == 0 || itemDTO.getProductType() == 1 || itemDTO.getProductType() == 2) {
                setDefaultSessions(orderItem, itemDTO.getProductType());
            }

            orderItem.setStatus("UNPAID"); // 待支付

            int result = orderItemMapper.insert(orderItem);
            if (result <= 0) {
                throw new BusinessException("创建订单项失败");
            }
        }

        log.info("创建订单项完成，订单ID：{}", orderId);
    }

    /**
     * 设置默认课时数
     */
    private void setDefaultSessions(OrderItem orderItem, Integer productType) {
        // 这里根据商品类型设置默认的课时数
        // 实际业务中可以从商品详情或配置中获取
        if (productType == 1) { // 私教课
            orderItem.setTotalSessions(10);
            orderItem.setRemainingSessions(10);
        } else if (productType == 2) { // 团课
            orderItem.setTotalSessions(10);
            orderItem.setRemainingSessions(10);
        }
    }

    /**
     * 设置订单项有效期
     */
    private void setOrderItemsValidity(Long orderId, Integer orderType) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId, orderId);

        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);

        for (OrderItem orderItem : orderItems) {
            if (orderType == 0) { // 会籍卡
                orderItem.setValidityStartDate(LocalDate.now());
                orderItem.setValidityEndDate(LocalDate.now().plusDays(30)); // 默认30天
            } else if (orderType == 1 || orderType == 2) { // 私教课、团课
                orderItem.setValidityStartDate(LocalDate.now());
                orderItem.setValidityEndDate(LocalDate.now().plusMonths(6)); // 默认6个月有效期
            }

            orderItemMapper.updateById(orderItem);
        }
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(ORDER_NO_FORMATTER);
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        return "ORD" + dateStr + randomStr;
    }

    /**
     * 生成支付流水号
     */
    private String generatePaymentNo() {
        String dateStr = LocalDate.now().format(ORDER_NO_FORMATTER);
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        return "PAY" + dateStr + randomStr;
    }

    /**
     * 生成交易ID
     */
    private String generateTransactionId(String paymentMethod) {
        if ("微信支付".equals(paymentMethod)) {
            return "WX" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        } else if ("支付宝".equals(paymentMethod)) {
            return "ALIPAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        } else {
            return "CASH" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        }
    }

    /**
     * 验证状态流转合法性
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        // 这里实现状态流转验证逻辑
        // 例如：只能从PENDING到PROCESSING，不能直接从PENDING到COMPLETED
        // 实际业务中需要根据具体业务规则实现
    }

    /**
     * 检查订单是否可以取消
     */
    private boolean canCancelOrder(Order order) {
        // 待支付、待处理的订单可以取消
        return "PENDING".equals(order.getOrderStatus()) ||
                "PROCESSING".equals(order.getOrderStatus());
    }

    /**
     * 检查订单是否可以完成
     */
    private boolean canCompleteOrder(Order order) {
        // 处理中的订单可以完成
        return "PROCESSING".equals(order.getOrderStatus());
    }

    /**
     * 处理订单退款
     */
    private void processOrderRefund(Long orderId, BigDecimal amount, String reason) {
        // 这里实现退款逻辑
        // 可以调用支付接口进行退款，或记录退款申请
        log.info("处理订单退款，订单ID：{}，金额：{}，原因：{}", orderId, amount, reason);
    }

    /**
     * 释放库存
     */
    private void releaseProductStock(Long orderId) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId, orderId);
        queryWrapper.eq(OrderItem::getProductType, 3); // 产品类

        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);

        for (OrderItem orderItem : orderItems) {
            Product product = productMapper.selectById(orderItem.getProductId());
            if (product != null) {
                int newStock = product.getStockQuantity() + orderItem.getQuantity();
                product.setStockQuantity(newStock);
                productMapper.updateById(product);
            }
        }
    }

    /**
     * 扣减库存
     */
    private void deductProductStock(Long orderId) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId, orderId);
        queryWrapper.eq(OrderItem::getProductType, 3); // 产品类

        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);

        for (OrderItem orderItem : orderItems) {
            Product product = productMapper.selectById(orderItem.getProductId());
            if (product != null) {
                int newStock = product.getStockQuantity() - orderItem.getQuantity();
                if (newStock < 0) {
                    throw new BusinessException("商品库存不足");
                }
                product.setStockQuantity(newStock);
                productMapper.updateById(product);
            }
        }
    }

    /**
     * 更新会员消费统计
     */
    private void updateMemberSpending(Long memberId, BigDecimal amount) {
        Member member = memberMapper.selectById(memberId);
        if (member != null) {
            BigDecimal currentTotal = member.getTotalSpent() != null ?
                    member.getTotalSpent() : BigDecimal.ZERO;
            member.setTotalSpent(currentTotal.add(amount));
            memberMapper.updateById(member);
        }
    }

    /**
     * 激活订单产品
     */
    private void activateOrderProducts(Long orderId) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId, orderId);

        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);

        for (OrderItem orderItem : orderItems) {
            // 更新订单项状态为已激活
            orderItem.setStatus("ACTIVE");
            orderItemMapper.updateById(orderItem);

            // 如果是会籍卡，更新会员有效期
            if (orderItem.getProductType() == 0) {
                activateMembershipCard(orderItem);
            }
        }
    }

    /**
     * 激活会籍卡
     */
    private void activateMembershipCard(OrderItem orderItem) {
        // 这里实现会籍卡激活逻辑
        // 更新会员的有效期等
    }

    /**
     * 记录订单状态变更日志
     */
    private void logOrderStatusChange(Long orderId, String oldStatus, String newStatus, String remark) {
        // 这里记录状态变更日志
        log.info("订单状态变更，订单ID：{}，从 {} 变更为 {}，备注：{}",
                orderId, oldStatus, newStatus, remark);
    }

    /**
     * 发送订单创建通知
     */
    private void sendOrderCreatedNotification(Long orderId, Member member) {
        // 这里实现发送通知的逻辑
        // 可以发送短信、微信通知等
        log.info("发送订单创建通知，订单ID：{}，会员：{}", orderId, member.getRealName());
    }

    /**
     * 获取商品第一张图片
     */
    private String getFirstProductImage(String images) {
        if (!StringUtils.hasText(images)) {
            return "";
        }

        try {
            // 解析JSON数组
            if (images.startsWith("[")) {
                // 简单实现：取第一个URL
                String[] urls = images.replace("[", "").replace("]", "").replace("\"", "").split(",");
                if (urls.length > 0) {
                    return urls[0].trim();
                }
            }
        } catch (Exception e) {
            log.error("解析商品图片失败：{}", images, e);
        }

        return "";
    }

    /**
     * 获取支付状态描述
     */
    private String getPaymentStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            default: return "未知";
        }
    }

    /**
     * 获取订单状态描述
     */
    private String getOrderStatusDesc(String status) {
        if (status == null) return "未知";
        switch (status) {
            case "PENDING": return "待处理";
            case "PROCESSING": return "处理中";
            case "COMPLETED": return "已完成";
            case "CANCELLED": return "已取消";
            case "REFUNDED": return "已退款";
//            case "DELETED": return "已删除";
            default: return "未知";
        }
    }
}