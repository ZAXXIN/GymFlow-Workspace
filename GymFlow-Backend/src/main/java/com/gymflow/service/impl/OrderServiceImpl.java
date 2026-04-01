// service/impl/OrderServiceImpl.java
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
    private final PaymentRecordMapper paymentRecordMapper;

    // 注入系统配置验证器
    private final SystemConfigValidator configValidator;

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

        // 是否包含已删除的订单
        if (!queryDTO.getIncludeDeleted()) {
            queryWrapper.ne(Order::getOrderStatus, "DELETED");
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

        // 设置类型描述
        fullDTO.setOrderTypeDesc(getOrderTypeDesc(order.getOrderType()));
        fullDTO.setPaymentStatusDesc(getPaymentStatusDesc(order.getPaymentStatus()));
        fullDTO.setOrderStatusDesc(getOrderStatusDesc(order.getOrderStatus()));

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

        // ========== 新增：购买限制检查 ==========
        checkPurchaseLimit(orderDTO.getMemberId(), orderDTO.getOrderItems());

        // 根据订单项的商品类型确定订单类型
        // 如果所有订单项都是同一类型，则使用该类型；否则设置为3（相关产品）或混合类型
        Integer orderType = determineOrderType(orderDTO.getOrderItems());
        log.info("根据订单项确定订单类型：{}", orderType);

        // 计算总金额
        BigDecimal totalAmount = orderDTO.getOrderItems().stream()
                .map(item -> {
                    if (item.getUnitPrice() != null && item.getQuantity() != null) {
                        return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 验证实付金额
        if (orderDTO.getActualAmount() != null &&
                orderDTO.getActualAmount().compareTo(totalAmount) > 0) {
            throw new BusinessException("实付金额不能大于总金额");
        }

        // 验证课程类商品的容量是否符合系统配置
        for (OrderItemDTO item : orderDTO.getOrderItems()) {
            if (item.getProductType() == 2) { // 团课
                configValidator.validateClassCapacity(0, item.getTotalSessions());
            }
        }

        // 生成订单编号
        String orderNo = generateOrderNo();

        // 创建订单记录
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setMemberId(orderDTO.getMemberId());
        order.setOrderType(orderType);  // 使用计算出的订单类型
        order.setTotalAmount(totalAmount);
        order.setActualAmount(orderDTO.getActualAmount() != null ?
                orderDTO.getActualAmount() : totalAmount);
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
        if (orderType == 0 || orderType == 1 || orderType == 2) {
            setOrderItemsValidity(orderId, orderType);
        }

        // 发送订单创建通知
        sendOrderCreatedNotification(orderId, member);

        return orderId;
    }

    /**
     * 根据订单项的商品类型确定订单类型
     */
    private Integer determineOrderType(List<OrderItemDTO> orderItems) {
        if (CollectionUtils.isEmpty(orderItems)) {
            return 3; // 默认相关产品
        }

        // 获取第一个订单项的商品类型
        Integer firstProductType = orderItems.get(0).getProductType();

        // 检查所有订单项是否都是同一类型
        boolean allSameType = orderItems.stream()
                .allMatch(item -> item.getProductType().equals(firstProductType));

        if (allSameType) {
            // 所有订单项类型相同，直接使用该类型
            return firstProductType;
        } else {
            // 混合类型，设为相关产品
            return 3;
        }
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

        // 验证实付金额
        if (orderDTO.getActualAmount() != null &&
                orderDTO.getActualAmount().compareTo(totalAmount) > 0) {
            throw new BusinessException("实付金额不能大于总金额");
        }

        // 验证课程类商品的容量是否符合系统配置
        for (OrderItemDTO item : orderDTO.getOrderItems()) {
            if (item.getProductType() == 2) { // 团课
                configValidator.validateClassCapacity(0, item.getTotalSessions());
            }
        }

        // 更新订单信息
        order.setTotalAmount(totalAmount);
        order.setActualAmount(orderDTO.getActualAmount() != null ?
                orderDTO.getActualAmount() : totalAmount);
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

        // ✅ 如果状态变为已完成，自动更新支付状态为已支付
        if ("COMPLETED".equals(statusDTO.getOrderStatus())) {
            order.setPaymentStatus(1);
            order.setPaymentTime(LocalDateTime.now());
            // 设置支付方式（如果没有则默认现金）
            if (order.getPaymentMethod() == null) {
                order.setPaymentMethod("在线支付");
            }

            // ✅ 新增：更新商品销量
            updateProductSalesVolume(orderId);
        }

        // 如果状态变为已取消或已退款，保持支付状态不变
        if ("CANCELLED".equals(statusDTO.getOrderStatus()) || "REFUNDED".equals(statusDTO.getOrderStatus())) {
            // 支付状态保持不变，但可以记录退款信息
            order.setRemark(statusDTO.getRemark());
        }

        if (StringUtils.hasText(statusDTO.getRemark())) {
            order.setRemark(statusDTO.getRemark());
        }

        int result = orderMapper.updateById(order);
        if (result <= 0) {
            throw new BusinessException("更新订单状态失败");
        }

        // ✅ 如果订单状态变为已完成，激活订单项（添加会籍卡/课程包）
        if ("COMPLETED".equals(statusDTO.getOrderStatus())) {
            activateOrderProducts(orderId);
        }

        // 记录状态变更日志
        logOrderStatusChange(orderId, order.getOrderStatus(), statusDTO.getOrderStatus(), statusDTO.getRemark());

        log.info("更新订单状态成功，订单ID：{}，新状态：{}", orderId, statusDTO.getOrderStatus());
    }

    /**
     * 更新商品销量
     */
    private void updateProductSalesVolume(Long orderId) {
        // 查询订单的所有订单项
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);

        for (OrderItem item : orderItems) {
            Product product = productMapper.selectById(item.getProductId());
            if (product != null) {
                // 增加销量
                int newSalesVolume = (product.getSalesVolume() != null ? product.getSalesVolume() : 0) + item.getQuantity();
                product.setSalesVolume(newSalesVolume);
                productMapper.updateById(product);
                log.info("更新商品销量，商品ID：{}，商品名称：{}，新增销量：{}，总销量：{}",
                        product.getId(), product.getProductName(), item.getQuantity(), newSalesVolume);
            }
        }
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
        vo.setOrderType(order.getOrderType());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setActualAmount(order.getActualAmount());
        vo.setPaymentStatus(order.getPaymentStatus());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setCreateTime(order.getCreateTime());

        // 获取会员信息
        Member member = memberMapper.selectById(order.getMemberId());
        if (member != null) {
            vo.setMemberName(member.getRealName());
            vo.setMemberPhone(member.getPhone());
        }

        // 设置类型描述
        vo.setOrderTypeDesc(getOrderTypeDesc(order.getOrderType()));
        vo.setPaymentStatusDesc(getPaymentStatusDesc(order.getPaymentStatus()));
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

            // 从商品表获取课时数和有效期天数
            Product product = productMapper.selectById(itemDTO.getProductId());
            if (product != null) {
                // 设置课时数
                if (product.getTotalSessions() != null) {
                    orderItem.setTotalSessions(product.getTotalSessions());
                    orderItem.setRemainingSessions(product.getTotalSessions());
                    log.debug("设置课时数：{}", product.getTotalSessions());
                }

                // 设置有效期天数（仅用于会籍卡，实际有效期在激活时设置）
                if (product.getValidityDays() != null) {
                    log.debug("商品有效期天数：{}", product.getValidityDays());
                }
            } else {
                // 如果没有商品信息，使用默认值
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
                // 从商品表中获取有效期天数
                Product product = productMapper.selectById(orderItem.getProductId());
                Integer validityDays = product != null ? product.getValidityDays() : 30;

                orderItem.setValidityStartDate(LocalDate.now());
                orderItem.setValidityEndDate(LocalDate.now().plusDays(validityDays));

                log.debug("会籍卡有效期设置，商品ID：{}，有效期天数：{}，结束日期：{}",
                        orderItem.getProductId(), validityDays, orderItem.getValidityEndDate());

            } else if (orderType == 1 || orderType == 2) { // 私教课、团课
                // 课程包默认365天有效期
                orderItem.setValidityStartDate(LocalDate.now());
                orderItem.setValidityEndDate(LocalDate.now().plusDays(365));

                log.debug("课程包有效期设置，订单项ID：{}，商品类型：{}，结束日期：{}",
                        orderItem.getId(), orderType, orderItem.getValidityEndDate());
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
        // 可以根据业务需求完善
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
     * 激活订单产品（会籍卡、课程包等）
     */
    private void activateOrderProducts(Long orderId) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getOrderId, orderId);

        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);
        log.info("激活订单产品，订单ID：{}，共 {} 个订单项", orderId, orderItems.size());

        for (OrderItem orderItem : orderItems) {
            // 更新订单项状态为已激活
            orderItem.setStatus("ACTIVE");
            orderItemMapper.updateById(orderItem);
            log.info("订单项状态更新为 ACTIVE，订单项ID：{}", orderItem.getId());

            // 根据商品类型处理
            if (orderItem.getProductType() == 0) {
                // 会籍卡：更新会员有效期
                activateMembershipCard(orderItem);
            } else if (orderItem.getProductType() == 1) {
                // 私教课：激活私教课程包
                activatePrivateCoursePackage(orderItem);
            } else if (orderItem.getProductType() == 2) {
                // 团课：激活团课课程包
                activateGroupCoursePackage(orderItem);
            }
            // 产品类（productType == 3）不需要激活
        }
    }

    /**
     * 激活会籍卡 - 更新会员有效期
     */
    private void activateMembershipCard(OrderItem orderItem) {
        log.info("激活会籍卡，订单项ID：{}，商品ID：{}", orderItem.getId(), orderItem.getProductId());

        // 获取订单信息
        Order order = orderMapper.selectById(orderItem.getOrderId());
        if (order == null) {
            log.error("订单不存在，订单ID：{}", orderItem.getOrderId());
            return;
        }

        Member member = memberMapper.selectById(order.getMemberId());
        if (member == null) {
            log.error("会员不存在，会员ID：{}", order.getMemberId());
            return;
        }

        // 获取商品信息（有效期天数）
        Product product = productMapper.selectById(orderItem.getProductId());
        if (product == null) {
            log.error("商品不存在，商品ID：{}", orderItem.getProductId());
            return;
        }

        LocalDate today = LocalDate.now();
        Integer validityDays = product.getValidityDays();
        if (validityDays == null) {
            validityDays = 30; // 默认30天
        }

        LocalDate newEndDate = today.plusDays(validityDays);
        log.info("会籍卡有效期天数：{}，新结束日期：{}", validityDays, newEndDate);

        // 更新会员会籍时间
        if (member.getMembershipEndDate() != null && member.getMembershipEndDate().isAfter(today)) {
            // 如果会员卡未过期，在原有效期上增加天数
            member.setMembershipEndDate(member.getMembershipEndDate().plusDays(validityDays));
            log.info("会员卡未过期，在原有效期上增加 {} 天，新结束日期：{}",
                    validityDays, member.getMembershipEndDate());
        } else {
            // 如果已过期或首次，设置新日期
            member.setMembershipStartDate(today);
            member.setMembershipEndDate(newEndDate);
            log.info("会员卡首次激活或已过期，设置有效期：{} - {}", today, newEndDate);
        }

        memberMapper.updateById(member);
        log.info("会籍卡激活成功，会员ID：{}，新有效期至：{}", member.getId(), member.getMembershipEndDate());
    }

    /**
     * 激活私教课程包
     */
    private void activatePrivateCoursePackage(OrderItem orderItem) {
        log.info("激活私教课程包，订单项ID：{}，商品ID：{}", orderItem.getId(), orderItem.getProductId());

        // 获取商品信息
        Product product = productMapper.selectById(orderItem.getProductId());
        if (product == null) {
            log.error("商品不存在，商品ID：{}", orderItem.getProductId());
            return;
        }

        Integer totalSessions = product.getTotalSessions();
        if (totalSessions == null) {
            throw new BusinessException("商品未配置课时数，商品ID：" + orderItem.getProductId());
        }

        // 设置订单项的课时数
        orderItem.setTotalSessions(totalSessions);
        orderItem.setRemainingSessions(totalSessions);

        // 设置有效期（课程包默认365天）
        LocalDate today = LocalDate.now();
        orderItem.setValidityStartDate(today);
        orderItem.setValidityEndDate(today.plusDays(365));

        orderItemMapper.updateById(orderItem);

        log.info("私教课程包激活成功，订单项ID：{}，总课时：{}，有效期至：{}",
                orderItem.getId(), totalSessions, orderItem.getValidityEndDate());
    }

    /**
     * 激活团课课程包
     */
    private void activateGroupCoursePackage(OrderItem orderItem) {
        log.info("激活团课课程包，订单项ID：{}，商品ID：{}", orderItem.getId(), orderItem.getProductId());

        // 获取商品信息
        Product product = productMapper.selectById(orderItem.getProductId());
        if (product == null) {
            log.error("商品不存在，商品ID：{}", orderItem.getProductId());
            return;
        }

        Integer totalSessions = product.getTotalSessions();
        if (totalSessions == null) {
            throw new BusinessException("商品未配置课时数，商品ID：" + orderItem.getProductId());
        }

        // 设置订单项的课时数
        orderItem.setTotalSessions(totalSessions);
        orderItem.setRemainingSessions(totalSessions);

        // 设置有效期（课程包默认365天）
        LocalDate today = LocalDate.now();
        orderItem.setValidityStartDate(today);
        orderItem.setValidityEndDate(today.plusDays(365));

        orderItemMapper.updateById(orderItem);

        log.info("团课课程包激活成功，订单项ID：{}，总课时：{}，有效期至：{}",
                orderItem.getId(), totalSessions, orderItem.getValidityEndDate());
    }

    /**
     * 购买限制检查
     */
    private void checkPurchaseLimit(Long memberId, List<OrderItemDTO> orderItems) {
        // 查询会员当前有效的订单项
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getMemberId, memberId)
                .eq(Order::getPaymentStatus, 1)
                .in(Order::getOrderStatus, "COMPLETED", "PROCESSING");

        List<Order> orders = orderMapper.selectList(orderWrapper);
        if (orders.isEmpty()) {
            return;
        }

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getOrderId, orderIds)
                .eq(OrderItem::getStatus, "ACTIVE");

        List<OrderItem> activeItems = orderItemMapper.selectList(itemWrapper);

        for (OrderItemDTO newItem : orderItems) {
            Integer productType = newItem.getProductType();

            List<OrderItem> sameTypeItems = activeItems.stream()
                    .filter(item -> item.getProductType().equals(productType))
                    .collect(Collectors.toList());

            if (sameTypeItems.isEmpty()) {
                continue;
            }

            switch (productType) {
                case 0: // 会籍卡
                    checkMembershipLimit(sameTypeItems);
                    break;
                case 1: // 私教课
                case 2: // 团课
                    checkCoursePackageLimit(sameTypeItems);
                    break;
            }
        }
    }

    /**
     * 会籍卡购买限制检查
     */
    private void checkMembershipLimit(List<OrderItem> existingItems) {
        LocalDate today = LocalDate.now();

        for (OrderItem item : existingItems) {
            if (item.getValidityEndDate() != null && item.getValidityEndDate().isAfter(today)) {
                throw new BusinessException(String.format(
                        "您当前已有有效的会籍卡「%s」，有效期至 %s，到期后才能购买新卡",
                        item.getProductName(), item.getValidityEndDate()));
            }
        }
    }

    /**
     * 课程包购买限制检查（私教课/团课）
     */
    private void checkCoursePackageLimit(List<OrderItem> existingItems) {
        for (OrderItem item : existingItems) {
            Integer remainingSessions = item.getRemainingSessions();
            if (remainingSessions != null && remainingSessions > 0) {
                throw new BusinessException(String.format(
                        "您当前已有的「%s」还有 %d 课时未使用，用完后再购买新卡",
                        item.getProductName(), remainingSessions));
            }
        }
    }

    /**
     * 记录订单状态变更日志
     */
    private void logOrderStatusChange(Long orderId, String oldStatus, String newStatus, String remark) {
        log.info("订单状态变更，订单ID：{}，从 {} 变更为 {}，备注：{}",
                orderId, oldStatus, newStatus, remark);
    }

    /**
     * 发送订单创建通知
     */
    private void sendOrderCreatedNotification(Long orderId, Member member) {
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
     * 获取订单类型描述
     */
    private String getOrderTypeDesc(Integer orderType) {
        if (orderType == null) return "未知";
        switch (orderType) {
            case 0: return "会籍卡";
            case 1: return "私教课";
            case 2: return "团课";
            case 3: return "相关产品";
            default: return "未知";
        }
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
            case "DELETED": return "已删除";
            default: return status;
        }
    }
}