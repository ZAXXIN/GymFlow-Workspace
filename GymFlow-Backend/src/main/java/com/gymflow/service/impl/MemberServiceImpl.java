package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.member.*;
import com.gymflow.dto.mini.MiniMemberCardDTO;
import com.gymflow.dto.order.OrderItemDTO;
import com.gymflow.entity.*;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.gymflow.service.MemberService;
import com.gymflow.utils.BCryptUtil;
import com.gymflow.utils.SystemConfigValidator;
import com.gymflow.vo.MemberListVO;
import com.gymflow.vo.PageResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final CoachMapper coachMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final CourseBookingMapper courseBookingMapper;
    private final CourseMapper courseMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    private final BCryptUtil bCryptUtil;
    private final SystemConfigValidator configValidator;

    private static final DateTimeFormatter MEMBER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 订单状态常量（与 OrderServiceImpl 保持一致）
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_WAIT_PAY = "WAIT_PAY";
    private static final String STATUS_PAID = "PAID";

    @Override
    public PageResultVO<MemberListVO> getMemberList(MemberQueryDTO queryDTO) {
        log.info("查询会员列表，查询条件：{}", queryDTO);

        Page<Member> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(queryDTO.getMemberNo())) {
            queryWrapper.like(Member::getMemberNo, queryDTO.getMemberNo());
        }
        if (StringUtils.hasText(queryDTO.getPhone())) {
            queryWrapper.like(Member::getPhone, queryDTO.getPhone());
        }
        if (StringUtils.hasText(queryDTO.getRealName())) {
            queryWrapper.like(Member::getRealName, queryDTO.getRealName());
        }

        queryWrapper.orderByDesc(Member::getCreateTime);
        IPage<Member> memberPage = memberMapper.selectPage(page, queryWrapper);

        List<MemberListVO> voList = memberPage.getRecords().stream()
                .map(this::convertToMemberListVO)
                .collect(Collectors.toList());

        return new PageResultVO<>(voList, memberPage.getTotal(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public MemberFullDTO getMemberDetail(Long memberId) {
        log.info("获取会员详情，会员ID：{}", memberId);

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        MemberFullDTO fullDTO = new MemberFullDTO();
        BeanUtils.copyProperties(member, fullDTO);
        fullDTO.setUsername(member.getPhone());
        fullDTO.setBirthday(member.getBirthday());
        if (member.getBirthday() != null) {
            fullDTO.setAge(Period.between(member.getBirthday(), LocalDate.now()).getYears());
        }

        fullDTO.setHealthRecords(getHealthRecords(memberId));
        List<MemberCardDTO> pcCards = getMemberCards(memberId);
        List<MiniMemberCardDTO> miniCards = convertToMiniMemberCards(pcCards);
        fullDTO.setMemberCards(miniCards);
        fullDTO.setCourseRecords(getCourseRecords(memberId));
        fullDTO.setCheckinRecords(getCheckinRecords(memberId));

        return fullDTO;
    }

    private List<MiniMemberCardDTO> convertToMiniMemberCards(List<MemberCardDTO> pcCards) {
        if (pcCards == null) return new ArrayList<>();
        return pcCards.stream().map(pcCard -> {
            MiniMemberCardDTO miniCard = new MiniMemberCardDTO();
            BeanUtils.copyProperties(pcCard, miniCard);
            miniCard.setCardType(pcCard.getProductType() != null ? pcCard.getProductType() : pcCard.getCardType());
            return miniCard;
        }).collect(Collectors.toList());
    }

    private List<MemberCardDTO> getMemberCards(Long memberId) {
        List<MemberCardDTO> cardList = new ArrayList<>();
        log.info("开始获取会员 {} 的会员卡列表", memberId);

        // 修改：只查询已完成订单（权益已激活）
        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getMemberId, memberId)
                .eq(Order::getStatus, STATUS_COMPLETED);

        List<Order> orders = orderMapper.selectList(orderQuery);
        log.info("查询到 {} 个已完成订单", orders.size());

        if (CollectionUtils.isEmpty(orders)) {
            return cardList;
        }

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());

        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.in(OrderItem::getOrderId, orderIds)
                .in(OrderItem::getProductType, 0, 1, 2)
                .orderByDesc(OrderItem::getCreateTime);

        List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);
        log.info("查询到 {} 个订单项", orderItems.size());

        for (OrderItem item : orderItems) {
            log.info("订单项 ID: {}, 产品类型: {}, 有效期开始: {}, 有效期结束: {}, 总课时: {}, 剩余课时: {}, 状态: {}",
                    item.getId(), item.getProductType(), item.getValidityStartDate(),
                    item.getValidityEndDate(), item.getTotalSessions(),
                    item.getRemainingSessions(), item.getStatus());

            MemberCardDTO card = new MemberCardDTO();
            card.setProductId(item.getProductId());
            card.setProductName(item.getProductName());
            card.setProductType(item.getProductType());
            card.setCardType(item.getProductType());
            card.setStartDate(item.getValidityStartDate());
            card.setEndDate(item.getValidityEndDate());
            card.setTotalSessions(item.getTotalSessions());
            card.setRemainingSessions(item.getRemainingSessions());
            card.setUsedSessions(item.getTotalSessions() != null && item.getRemainingSessions() != null ?
                    item.getTotalSessions() - item.getRemainingSessions() : 0);
            card.setAmount(item.getTotalPrice());

            if (item.getStatus() != null) {
                card.setStatus(item.getStatus());
            } else if (item.getValidityEndDate() != null && LocalDate.now().isAfter(item.getValidityEndDate())) {
                card.setStatus("EXPIRED");
            } else if (item.getRemainingSessions() != null && item.getRemainingSessions() <= 0) {
                card.setStatus("USED_UP");
            } else {
                card.setStatus("ACTIVE");
            }

            cardList.add(card);
            log.info("添加会员卡: 产品名称={}, 状态={}, 剩余课时={}",
                    card.getProductName(), card.getStatus(), card.getRemainingSessions());
        }

        return cardList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addMember(MemberBasicDTO basicDTO, HealthRecordDTO healthRecordDTO, MemberCardDTO cardDTO) {
        log.info("开始添加会员，手机号：{}", basicDTO.getPhone());

        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(Member::getPhone, basicDTO.getPhone());
        Long memberCount = memberMapper.selectCount(memberQuery);
        if (memberCount > 0) {
            throw new BusinessException("该手机号已注册为会员");
        }

        if (cardDTO == null || cardDTO.getProductId() == null) {
            throw new BusinessException("请选择会员卡或课程包");
        }

        Member member = new Member();
        member.setMemberNo(generateMemberNo());
        member.setPhone(basicDTO.getPhone());
        member.setPassword(bCryptUtil.encodePassword("123456"));
        member.setRealName(basicDTO.getRealName());
        member.setGender(basicDTO.getGender());
        member.setBirthday(basicDTO.getBirthday());
        member.setTotalSpent(BigDecimal.ZERO);
        calculateMembershipDates(member, cardDTO);
        if (cardDTO.getAmount() != null) {
            member.setTotalSpent(member.getTotalSpent().add(cardDTO.getAmount()));
        }
        member.setTotalCheckins(0);
        member.setTotalCourseHours(0);

        int result = memberMapper.insert(member);
        if (result <= 0) {
            throw new BusinessException("添加会员失败");
        }

        log.info("添加会员成功，ID：{}，会员编号：{}，总消费：{}",
                member.getId(), member.getMemberNo(), member.getTotalSpent());

        if (healthRecordDTO != null) {
            addHealthRecord(member.getId(), healthRecordDTO);
        }

        createOrderForNewMember(member.getId(), cardDTO);

        return member.getId();
    }

    private void createOrderForNewMember(Long memberId, MemberCardDTO cardDTO) {
        log.info("为新会员创建订单，会员ID：{}", memberId);

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setMemberId(memberId);
        order.setOrderType(cardDTO.getCardType());
        order.setTotalAmount(cardDTO.getAmount() != null ? cardDTO.getAmount() : BigDecimal.ZERO);
        order.setActualAmount(cardDTO.getAmount() != null ? cardDTO.getAmount() : BigDecimal.ZERO);
        order.setStatus(STATUS_COMPLETED);   // 新会员开卡直接设为已完成
        order.setPaymentTime(LocalDateTime.now());
        order.setRemark("新会员开卡");

        orderMapper.insert(order);
        log.info("订单创建成功，订单ID：{}，订单号：{}", order.getId(), order.getOrderNo());

        createOrderItem(order.getId(), cardDTO);
    }

    private void createOrderItem(Long orderId, MemberCardDTO cardDTO) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = null;

        Product product = productMapper.selectById(cardDTO.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        if (product.getProductType() == 0 && product.getValidityDays() != null) {
            endDate = today.plusDays(product.getValidityDays());
            log.info("会籍卡有效期天数：{}，结束日期：{}", product.getValidityDays(), endDate);
        } else {
            endDate = today.plusDays(30);
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(cardDTO.getProductId());
        orderItem.setProductName(cardDTO.getProductName());
        orderItem.setProductType(cardDTO.getCardType());
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(cardDTO.getAmount() != null ? cardDTO.getAmount() : BigDecimal.ZERO);
        orderItem.setTotalPrice(cardDTO.getAmount() != null ? cardDTO.getAmount() : BigDecimal.ZERO);
        orderItem.setValidityStartDate(today);
        orderItem.setValidityEndDate(endDate);
        if (product.getProductType() == 1 || product.getProductType() == 2) {
            orderItem.setTotalSessions(product.getTotalSessions());
            orderItem.setRemainingSessions(product.getTotalSessions());
        }
        orderItem.setStatus("ACTIVE");

        orderItemMapper.insert(orderItem);
        log.info("订单项创建成功，订单项ID：{}，有效期至：{}", orderItem.getId(), endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMember(Long memberId, MemberBasicDTO basicDTO, HealthRecordDTO healthRecordDTO) {
        log.info("开始更新会员，ID：{}", memberId);

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        if (!member.getPhone().equals(basicDTO.getPhone())) {
            LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Member::getPhone, basicDTO.getPhone());
            queryWrapper.ne(Member::getId, memberId);
            Long count = memberMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("该手机号已被其他会员使用");
            }
        }

        member.setPhone(basicDTO.getPhone());
        member.setRealName(basicDTO.getRealName());
        member.setGender(basicDTO.getGender());
        member.setBirthday(basicDTO.getBirthday());
        member.setUpdateTime(LocalDateTime.now());

        int result = memberMapper.updateById(member);
        if (result <= 0) {
            throw new BusinessException("更新会员失败");
        }

        if (healthRecordDTO != null) {
            if (healthRecordDTO.getId() != null) {
                HealthRecord existingRecord = healthRecordMapper.selectById(healthRecordDTO.getId());
                if (existingRecord == null) {
                    throw new BusinessException("健康记录不存在");
                }
                if (!existingRecord.getMemberId().equals(memberId)) {
                    throw new BusinessException("无权修改其他会员的健康记录");
                }
                BeanUtils.copyProperties(healthRecordDTO, existingRecord, "id", "memberId", "createTime");
                existingRecord.setUpdateTime(LocalDateTime.now());
                if (existingRecord.getHeight() != null && existingRecord.getWeight() != null) {
                    BigDecimal heightInM = existingRecord.getHeight().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    BigDecimal bmi = existingRecord.getWeight().divide(
                            heightInM.multiply(heightInM), 1, RoundingMode.HALF_UP);
                    existingRecord.setBmi(bmi);
                }
                healthRecordMapper.updateById(existingRecord);
                log.info("更新健康档案成功，记录ID：{}", existingRecord.getId());
            } else {
                LambdaQueryWrapper<HealthRecord> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(HealthRecord::getMemberId, memberId)
                        .eq(HealthRecord::getRecordDate, healthRecordDTO.getRecordDate());
                HealthRecord existingRecord = healthRecordMapper.selectOne(queryWrapper);
                if (existingRecord != null) {
                    throw new BusinessException("该日期已存在健康记录，请编辑已有记录");
                }
                HealthRecord newRecord = new HealthRecord();
                BeanUtils.copyProperties(healthRecordDTO, newRecord);
                newRecord.setMemberId(memberId);
                if (newRecord.getHeight() != null && newRecord.getWeight() != null) {
                    BigDecimal heightInM = newRecord.getHeight().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    BigDecimal bmi = newRecord.getWeight().divide(
                            heightInM.multiply(heightInM), 1, RoundingMode.HALF_UP);
                    newRecord.setBmi(bmi);
                }
                healthRecordMapper.insert(newRecord);
                log.info("新增健康档案成功，记录ID：{}", newRecord.getId());
            }
        }

        log.info("更新会员成功，ID：{}", memberId);
    }

    private void calculateMembershipDates(Member member, MemberCardDTO cardDTO) {
        if (cardDTO == null || cardDTO.getProductId() == null) return;
        LocalDate today = LocalDate.now();
        member.setMembershipStartDate(today);
        // 实际有效期激活时会根据订单项设置
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMember(Long memberId) {
        log.info("开始删除会员，ID：{}", memberId);

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        checkUnfinishedOrders(memberId);
        checkUnfinishedCourses(memberId);

        int result = memberMapper.deleteById(memberId);
        if (result <= 0) {
            throw new BusinessException("删除会员失败");
        }

        deleteRelatedRecords(memberId);
        log.info("删除会员成功，ID：{}", memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteMember(List<Long> memberIds) {
        log.info("开始批量删除会员，ID列表：{}", memberIds);
        if (CollectionUtils.isEmpty(memberIds)) return;

        int successCount = 0;
        for (Long memberId : memberIds) {
            try {
                deleteMember(memberId);
                successCount++;
            } catch (Exception e) {
                log.error("删除会员失败，ID：{}，错误：{}", memberId, e.getMessage());
            }
        }
        log.info("批量删除完成，成功：{}，失败：{}", successCount, memberIds.size() - successCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMemberCard(Long memberId, MemberCardDTO cardDTO) {
        log.info("为会员添加新卡，会员ID：{}", memberId);

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }
        if (cardDTO == null || cardDTO.getProductId() == null) {
            throw new BusinessException("请选择会员卡或课程包");
        }

        // 会籍卡限制：未过期不能添加
        if (cardDTO.getCardType() == 0) {
            List<MemberCardDTO> currentCards = getMemberCards(memberId);
            MemberCardDTO activeMembershipCard = currentCards.stream()
                    .filter(card -> card.getCardType() == 0 && "ACTIVE".equals(card.getStatus()))
                    .findFirst().orElse(null);
            if (activeMembershipCard != null && activeMembershipCard.getEndDate() != null &&
                    activeMembershipCard.getEndDate().isAfter(LocalDate.now())) {
                throw new BusinessException("当前有未过期的会籍卡（有效期至：" +
                        activeMembershipCard.getEndDate() + "），不能添加新的会籍卡");
            }
        }

        // 课程包限制：剩余课时>0不能添加
        if (cardDTO.getCardType() == 1 || cardDTO.getCardType() == 2) {
            List<MemberCardDTO> currentCards = getMemberCards(memberId);
            MemberCardDTO activeCourseCard = currentCards.stream()
                    .filter(card -> card.getCardType() == cardDTO.getCardType() && "ACTIVE".equals(card.getStatus()))
                    .findFirst().orElse(null);
            if (activeCourseCard != null && activeCourseCard.getRemainingSessions() != null
                    && activeCourseCard.getRemainingSessions() > 0) {
                throw new BusinessException(String.format(
                        "您当前已有的「%s」还有 %d 课时未使用，用完后再购买新卡",
                        activeCourseCard.getProductName(), activeCourseCard.getRemainingSessions()));
            }
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setMemberId(memberId);
        order.setOrderType(cardDTO.getCardType());
        order.setTotalAmount(cardDTO.getAmount() != null ? cardDTO.getAmount() : BigDecimal.ZERO);
        order.setActualAmount(cardDTO.getAmount() != null ? cardDTO.getAmount() : BigDecimal.ZERO);
        order.setStatus(STATUS_COMPLETED);   // 添加新卡直接设为已完成
        order.setPaymentTime(LocalDateTime.now());
        order.setRemark("添加新卡");

        orderMapper.insert(order);
        log.info("订单创建成功，订单ID：{}", order.getId());

        createOrderItem(order.getId(), cardDTO);

        if (cardDTO.getAmount() != null) {
            member.setTotalSpent(member.getTotalSpent().add(cardDTO.getAmount()));
            memberMapper.updateById(member);
        }

        if (cardDTO.getCardType() == 0) {
            Product product = productMapper.selectById(cardDTO.getProductId());
            if (product != null && product.getValidityDays() != null) {
                LocalDate newEndDate = LocalDate.now().plusDays(product.getValidityDays());
                if (member.getMembershipEndDate() != null && member.getMembershipEndDate().isAfter(LocalDate.now())) {
                    member.setMembershipEndDate(member.getMembershipEndDate().plusDays(product.getValidityDays()));
                } else {
                    member.setMembershipStartDate(LocalDate.now());
                    member.setMembershipEndDate(newEndDate);
                }
                memberMapper.updateById(member);
            }
        }

        log.info("为会员添加新卡成功，会员ID：{}", memberId);
    }

    @Override
    public List<HealthRecordDTO> getHealthRecords(Long memberId) {
        LambdaQueryWrapper<HealthRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HealthRecord::getMemberId, memberId);
        queryWrapper.orderByDesc(HealthRecord::getRecordDate);
        List<HealthRecord> healthRecords = healthRecordMapper.selectList(queryWrapper);
        return healthRecords.stream().map(this::convertToHealthRecordDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addHealthRecord(Long memberId, HealthRecordDTO healthRecordDTO) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) throw new BusinessException("会员不存在");

        LambdaQueryWrapper<HealthRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HealthRecord::getMemberId, memberId)
                .eq(HealthRecord::getRecordDate, healthRecordDTO.getRecordDate());
        Long count = healthRecordMapper.selectCount(queryWrapper);
        if (count > 0) throw new BusinessException("该日期已存在健康记录");

        HealthRecord healthRecord = new HealthRecord();
        BeanUtils.copyProperties(healthRecordDTO, healthRecord);
        healthRecord.setMemberId(memberId);
        healthRecord.setRecordedBy("系统");

        if (healthRecord.getHeight() != null && healthRecord.getWeight() != null) {
            BigDecimal heightInM = healthRecord.getHeight().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal bmi = healthRecord.getWeight().divide(heightInM.multiply(heightInM), 1, RoundingMode.HALF_UP);
            healthRecord.setBmi(bmi);
        }

        int result = healthRecordMapper.insert(healthRecord);
        if (result <= 0) throw new BusinessException("添加健康档案失败");
        log.info("添加健康档案成功，会员ID：{}，记录ID：{}", memberId, healthRecord.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateHealthRecord(Long recordId, HealthRecordDTO healthRecordDTO) {
        HealthRecord existingRecord = healthRecordMapper.selectById(recordId);
        if (existingRecord == null) throw new BusinessException("健康记录不存在");

        BeanUtils.copyProperties(healthRecordDTO, existingRecord, "id", "memberId", "createTime");
        existingRecord.setUpdateTime(LocalDateTime.now());

        if (existingRecord.getHeight() != null && existingRecord.getWeight() != null) {
            BigDecimal heightInM = existingRecord.getHeight().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal bmi = existingRecord.getWeight().divide(heightInM.multiply(heightInM), 1, RoundingMode.HALF_UP);
            existingRecord.setBmi(bmi);
        }

        int result = healthRecordMapper.updateById(existingRecord);
        if (result <= 0) throw new BusinessException("更新健康记录失败");
        log.info("更新健康记录成功，记录ID：{}", recordId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHealthRecord(Long recordId) {
        HealthRecord record = healthRecordMapper.selectById(recordId);
        if (record == null) throw new BusinessException("健康记录不存在");
        int result = healthRecordMapper.deleteById(recordId);
        if (result <= 0) throw new BusinessException("删除健康记录失败");
        log.info("删除健康记录成功，记录ID：{}", recordId);
    }

    // ========== 私有辅助方法 ==========

    private MemberListVO convertToMemberListVO(Member member) {
        MemberListVO vo = new MemberListVO();
        vo.setId(member.getId());
        vo.setMemberNo(member.getMemberNo());
        vo.setPhone(member.getPhone());
        vo.setRealName(member.getRealName());
        vo.setGender(member.getGender());
        vo.setCreateTime(member.getCreateTime());

        if (member.getBirthday() != null) {
            vo.setAge(Period.between(member.getBirthday(), LocalDate.now()).getYears());
        }

        List<MemberCardDTO> allCards = getMemberCards(member.getId());
        Set<Integer> cardTypeSet = allCards.stream()
                .map(MemberCardDTO::getCardType)
                .collect(Collectors.toSet());
        vo.setCardTypes(new ArrayList<>(cardTypeSet));

        boolean hasActiveCard = allCards.stream().anyMatch(card -> "ACTIVE".equals(card.getStatus()));
        if (hasActiveCard) {
            vo.setCardStatus("ACTIVE");
            vo.setCardStatusDesc("有效");
        } else {
            vo.setCardStatus("EXPIRED");
            vo.setCardStatusDesc("过期");
        }

        vo.setTotalCheckins(member.getTotalCheckins() != null ? member.getTotalCheckins() : 0);
        vo.setTotalCourseHours(member.getTotalCourseHours() != null ? member.getTotalCourseHours() : 0);
        vo.setTotalSpent(member.getTotalSpent() != null ? member.getTotalSpent() : BigDecimal.ZERO);
        return vo;
    }

    private List<CourseRecordDTO> getCourseRecords(Long memberId) {
        List<CourseRecordDTO> courseRecords = new ArrayList<>();
        try {
            LambdaQueryWrapper<CourseBooking> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CourseBooking::getMemberId, memberId);
            queryWrapper.orderByDesc(CourseBooking::getBookingTime);
            List<CourseBooking> bookings = courseBookingMapper.selectList(queryWrapper);
            for (CourseBooking booking : bookings) {
                CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
                if (schedule == null) continue;
                Course course = courseMapper.selectById(schedule.getCourseId());
                if (course == null) continue;

                CourseRecordDTO recordDTO = new CourseRecordDTO();
                recordDTO.setCourseId(booking.getCourseId());
                recordDTO.setCourseName(course.getCourseName());
                recordDTO.setSessionCost(course.getSessionCost());

                Coach coach = coachMapper.selectById(schedule.getCoachId());
                if (coach != null) recordDTO.setCoachName(coach.getRealName());

                recordDTO.setScheduleDate(schedule.getScheduleDate());
                recordDTO.setStartTime(schedule.getStartTime());
                recordDTO.setEndTime(schedule.getEndTime());
                recordDTO.setBookingStatus(booking.getBookingStatus());
                recordDTO.setCheckinTime(booking.getCheckinTime());
                courseRecords.add(recordDTO);
            }
        } catch (Exception e) {
            log.error("获取课程记录失败，会员ID：{}", memberId, e);
        }
        return courseRecords;
    }

    private List<CheckInRecordDTO> getCheckinRecords(Long memberId) {
        List<CheckInRecordDTO> checkinRecords = new ArrayList<>();
        try {
            LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CheckinRecord::getMemberId, memberId);
            queryWrapper.orderByDesc(CheckinRecord::getCheckinTime);
            List<CheckinRecord> records = checkInRecordMapper.selectList(queryWrapper);
            for (CheckinRecord record : records) {
                CheckInRecordDTO dto = new CheckInRecordDTO();
                dto.setCheckinTime(record.getCheckinTime());
                dto.setCheckinMethod(record.getCheckinMethod());
                dto.setNotes(record.getNotes());

                if (record.getCourseBookingId() != null) {
                    CourseBooking booking = courseBookingMapper.selectById(record.getCourseBookingId());
                    if (booking != null) {
                        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
                        if (schedule != null) {
                            Course course = courseMapper.selectById(schedule.getCourseId());
                            if (course != null) {
                                dto.setCourseName(course.getCourseName());
                                Coach coach = coachMapper.selectById(schedule.getCoachId());
                                if (coach != null) dto.setCoachName(coach.getRealName());
                            }
                        }
                    }
                }
                checkinRecords.add(dto);
            }
        } catch (Exception e) {
            log.error("获取签到记录失败，会员ID：{}", memberId, e);
        }
        return checkinRecords;
    }

    private HealthRecordDTO convertToHealthRecordDTO(HealthRecord healthRecord) {
        HealthRecordDTO dto = new HealthRecordDTO();
        BeanUtils.copyProperties(healthRecord, dto);
        return dto;
    }

    private String generateMemberNo() {
        String dateStr = LocalDate.now().format(MEMBER_NO_FORMATTER);
        String randomStr = String.format("%04d", (int)(Math.random() * 10000));
        return "MF" + dateStr + randomStr;
    }

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        return "ORD" + dateStr + randomStr;
    }

    private void checkUnfinishedOrders(Long memberId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getMemberId, memberId);
        queryWrapper.eq(Order::getStatus, STATUS_WAIT_PAY);   // 待支付订单
        Long count = orderMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("会员存在未完成的订单，不能删除");
        }
    }

    private void checkUnfinishedCourses(Long memberId) {
        LambdaQueryWrapper<CourseBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseBooking::getMemberId, memberId);
        queryWrapper.in(CourseBooking::getBookingStatus, 0, 1);
        Long count = courseBookingMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("会员有未完成的课程，不能删除");
        }
    }

    private void deleteRelatedRecords(Long memberId) {
        LambdaQueryWrapper<HealthRecord> healthQuery = new LambdaQueryWrapper<>();
        healthQuery.eq(HealthRecord::getMemberId, memberId);
        healthRecordMapper.delete(healthQuery);
        LambdaQueryWrapper<CheckinRecord> checkinQuery = new LambdaQueryWrapper<>();
        checkinQuery.eq(CheckinRecord::getMemberId, memberId);
        checkInRecordMapper.delete(checkinQuery);
        log.info("已删除会员 {} 的相关记录", memberId);
    }
}