package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.member.*;
import com.gymflow.dto.mini.MiniMemberCardDTO;
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
import java.util.List;
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
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final ProductDetailMapper productDetailMapper;

    private final BCryptUtil bCryptUtil;
    private final SystemConfigValidator configValidator;

    private static final DateTimeFormatter MEMBER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

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

        fullDTO.setHealthRecords(getHealthRecords(memberId));

        // 获取会员卡列表并转换为MiniMemberCardDTO
        List<MemberCardDTO> pcCards = getMemberCards(memberId);
        List<MiniMemberCardDTO> miniCards = convertToMiniMemberCards(pcCards);
        fullDTO.setMemberCards(miniCards);

        fullDTO.setCourseRecords(getCourseRecords(memberId));
        fullDTO.setCheckinRecords(getCheckinRecords(memberId));

        return fullDTO;
    }

    /**
     * 将PC端的MemberCardDTO转换为小程序端的MiniMemberCardDTO
     */
    private List<MiniMemberCardDTO> convertToMiniMemberCards(List<MemberCardDTO> pcCards) {
        if (pcCards == null) {
            return new ArrayList<>();
        }

        return pcCards.stream().map(pcCard -> {
            MiniMemberCardDTO miniCard = new MiniMemberCardDTO();
            BeanUtils.copyProperties(pcCard, miniCard);

            if (pcCard.getProductType() != null) {
                miniCard.setCardType(pcCard.getProductType());
            } else {
                miniCard.setCardType(pcCard.getCardType());
            }

            return miniCard;
        }).collect(Collectors.toList());
    }

    /**
     * 获取会员卡列表（PC端使用）
     */
    private List<MemberCardDTO> getMemberCards(Long memberId) {
        List<MemberCardDTO> cardList = new ArrayList<>();
        log.info("开始获取会员 {} 的会员卡列表", memberId);

        // 查询会员已支付的订单
        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getMemberId, memberId)
                .eq(Order::getPaymentStatus, 1)
                .in(Order::getOrderStatus, "COMPLETED", "PROCESSING");

        List<Order> orders = orderMapper.selectList(orderQuery);
        log.info("查询到 {} 个订单", orders.size());

        if (CollectionUtils.isEmpty(orders)) {
            return cardList;
        }

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        log.info("订单ID列表: {}", orderIds);

        // 查询订单项（会籍卡、私教课、团课）
        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.in(OrderItem::getOrderId, orderIds)
                .in(OrderItem::getProductType, 0, 1, 2)
                .orderByDesc(OrderItem::getCreateTime);

        List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);

        for (OrderItem item : orderItems) {
            log.info("订单项 ID: {}, 产品类型: {}, 有效期开始: {}, 有效期结束: {}, 金额: {}",
                    item.getId(), item.getProductType(), item.getValidityStartDate(),
                    item.getValidityEndDate(), item.getTotalPrice());

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

            // 判断状态
            if (item.getStatus() != null) {
                card.setStatus(item.getStatus());
            } else if (item.getValidityEndDate() != null &&
                    LocalDate.now().isAfter(item.getValidityEndDate())) {
                card.setStatus("EXPIRED");
            } else if (item.getRemainingSessions() != null &&
                    item.getRemainingSessions() <= 0) {
                card.setStatus("USED_UP");
            } else {
                card.setStatus("ACTIVE");
            }

            cardList.add(card);
        }

        return cardList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addMember(MemberBasicDTO basicDTO, HealthRecordDTO healthRecordDTO, MemberCardDTO cardDTO) {
        log.info("开始添加会员，手机号：{}", basicDTO.getPhone());

        // 检查手机号
        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(Member::getPhone, basicDTO.getPhone());
        Long memberCount = memberMapper.selectCount(memberQuery);
        if (memberCount > 0) {
            throw new BusinessException("该手机号已注册为会员");
        }

        // 创建会员记录
        Member member = new Member();
        member.setMemberNo(generateMemberNo()); // 后端自动生成会员编号
        member.setPhone(basicDTO.getPhone());
        member.setPassword(bCryptUtil.encodePassword(basicDTO.getPassword()));
        member.setRealName(basicDTO.getRealName());
        member.setGender(basicDTO.getGender());
        member.setBirthday(basicDTO.getBirthday());

        // 初始总消费为0
        member.setTotalSpent(BigDecimal.ZERO);

        // 会员卡购买时才设置会籍时间，否则为空
        if (cardDTO != null) {
            // 根据会员卡类型计算会籍时间
            calculateMembershipDates(member, cardDTO);

            // 累加会员卡金额到总消费
            if (cardDTO.getAmount() != null) {
                member.setTotalSpent(member.getTotalSpent().add(cardDTO.getAmount()));
            }
        } else {
            member.setMembershipStartDate(null);
            member.setMembershipEndDate(null);
        }

        member.setTotalCheckins(0);
        member.setTotalCourseHours(0);

        LocalDateTime now = LocalDateTime.now();
        member.setCreateTime(now);
        member.setUpdateTime(now);

        int result = memberMapper.insert(member);
        if (result <= 0) {
            throw new BusinessException("添加会员失败");
        }

        log.info("添加会员成功，ID：{}，会员编号：{}，总消费：{}",
                member.getId(), member.getMemberNo(), member.getTotalSpent());

        if (healthRecordDTO != null) {
            addHealthRecord(member.getId(), healthRecordDTO);
        }

        // 如果有会员卡，创建订单
        if (cardDTO != null) {
            createOrderForNewMember(member.getId(), cardDTO);
        }

        return member.getId();
    }

    /**
     * 为新会员创建订单
     */
    private void createOrderForNewMember(Long memberId, MemberCardDTO cardDTO) {
        log.info("为新会员创建订单，会员ID：{}", memberId);

        // 创建新订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setMemberId(memberId);
        order.setOrderType(cardDTO.getCardType() <= 1 ? 1 : 0); // 0-会籍卡，1-课程卡
        order.setTotalAmount(cardDTO.getAmount() != null ? cardDTO.getAmount() : BigDecimal.ZERO);
        order.setActualAmount(cardDTO.getAmount() != null ? cardDTO.getAmount() : BigDecimal.ZERO);
        order.setPaymentStatus(1); // 已支付
        order.setPaymentTime(LocalDateTime.now());
        order.setOrderStatus("COMPLETED");
        order.setRemark("新会员开卡");

        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);

        orderMapper.insert(order);
        log.info("订单创建成功，订单ID：{}，订单号：{}", order.getId(), order.getOrderNo());

        // 创建订单项
        createOrderItem(order.getId(), cardDTO);
    }

    /**
     * 创建订单项
     */
    private void createOrderItem(Long orderId, MemberCardDTO cardDTO) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = null;

        // 根据卡类型计算结束日期
        switch (cardDTO.getCardType()) {
            case 0: // 会籍卡 - 年卡
            case 3: // 年卡
                endDate = today.plusYears(1);
                break;
            case 2: // 月卡
                endDate = today.plusMonths(1);
                break;
            case 4: // 周卡
                endDate = today.plusWeeks(1);
                break;
            case 1: // 私教课
            case 5: // 团课
                // 课程卡，从商品详情获取有效期
                if (cardDTO.getProductId() != null) {
                    ProductDetail productDetail = productDetailMapper.selectOne(
                            new LambdaQueryWrapper<ProductDetail>()
                                    .eq(ProductDetail::getProductId, cardDTO.getProductId())
                    );
                    if (productDetail != null && productDetail.getValidityDays() != null) {
                        endDate = today.plusDays(productDetail.getValidityDays());
                    } else {
                        endDate = today.plusMonths(6); // 默认6个月
                    }
                } else {
                    endDate = today.plusMonths(6);
                }
                break;
            default:
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

        // 设置课时数（用于课程卡）
        if (cardDTO.getCardType() == 1 || cardDTO.getCardType() == 5) {
            orderItem.setTotalSessions(cardDTO.getTotalSessions() != null ?
                    cardDTO.getTotalSessions() : 10);
            orderItem.setRemainingSessions(cardDTO.getTotalSessions() != null ?
                    cardDTO.getTotalSessions() : 10);
        }

        orderItem.setStatus("ACTIVE");

        LocalDateTime now = LocalDateTime.now();
        orderItem.setCreateTime(now);
        orderItem.setUpdateTime(now);

        orderItemMapper.insert(orderItem);
        log.info("订单项创建成功，订单项ID：{}，有效期至：{}", orderItem.getId(), endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMember(Long memberId, MemberBasicDTO basicDTO, MemberCardDTO cardDTO) {
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

        if (StringUtils.hasText(basicDTO.getPassword())) {
            member.setPassword(bCryptUtil.encodePassword(basicDTO.getPassword()));
        }

        member.setRealName(basicDTO.getRealName());
        member.setGender(basicDTO.getGender());
        member.setBirthday(basicDTO.getBirthday());

        // 如果有会员卡信息，更新会籍时间和总消费
        if (cardDTO != null) {
            calculateMembershipDates(member, cardDTO);

            // 累加新卡金额到总消费
            if (cardDTO.getAmount() != null) {
                BigDecimal currentTotal = member.getTotalSpent() != null ?
                        member.getTotalSpent() : BigDecimal.ZERO;
                member.setTotalSpent(currentTotal.add(cardDTO.getAmount()));
            }

            // 创建新订单
            createOrderForNewMember(memberId, cardDTO);
        }

        member.setUpdateTime(LocalDateTime.now());

        int result = memberMapper.updateById(member);
        if (result <= 0) {
            throw new BusinessException("更新会员失败");
        }

        log.info("更新会员成功，ID：{}，新总消费：{}", memberId, member.getTotalSpent());
    }

    /**
     * 根据会员卡类型计算会籍开始和结束日期
     */
    private void calculateMembershipDates(Member member, MemberCardDTO cardDTO) {
        if (cardDTO == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        member.setMembershipStartDate(today);

        // 根据卡类型计算结束日期
        switch (cardDTO.getCardType()) {
            case 0: // 会籍卡 - 年卡
            case 3: // 年卡
                member.setMembershipEndDate(today.plusYears(1));
                break;
            case 2: // 月卡
                member.setMembershipEndDate(today.plusMonths(1));
                break;
            case 4: // 周卡
                member.setMembershipEndDate(today.plusWeeks(1));
                break;
            case 1: // 私教课
            case 5: // 团课
                // 课程卡不设置结束日期，由课时数控制
                member.setMembershipEndDate(null);
                break;
            default:
                // 其他类型默认30天
                member.setMembershipEndDate(today.plusDays(30));
        }
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

        if (CollectionUtils.isEmpty(memberIds)) {
            log.warn("批量删除会员ID列表为空");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (Long memberId : memberIds) {
            try {
                deleteMember(memberId);
                successCount++;
            } catch (Exception e) {
                log.error("删除会员失败，ID：{}，错误：{}", memberId, e.getMessage());
                failCount++;
            }
        }

        log.info("批量删除完成，成功：{}，失败：{}", successCount, failCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renewMemberCard(Long memberId, MemberCardDTO cardDTO) {
        log.info("开始续费会员卡，会员ID：{}", memberId);

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 获取会员当前有效的会员卡
        List<MemberCardDTO> currentCards = getMemberCards(memberId);

        // 检查续费类型一致性
        if (!CollectionUtils.isEmpty(currentCards)) {
            // 获取最近一张有效卡
            MemberCardDTO latestCard = currentCards.stream()
                    .filter(card -> "ACTIVE".equals(card.getStatus()))
                    .findFirst()
                    .orElse(null);

            if (latestCard != null) {
                // 续费类型必须和当前有效卡一致
                if (!latestCard.getCardType().equals(cardDTO.getCardType())) {
                    throw new BusinessException("续费类型必须与当前会员卡类型一致");
                }
            }
        }

        // 创建订单
        createOrderForNewMember(memberId, cardDTO);

        // 累加金额到总消费
        if (cardDTO.getAmount() != null) {
            BigDecimal currentTotal = member.getTotalSpent() != null ?
                    member.getTotalSpent() : BigDecimal.ZERO;
            member.setTotalSpent(currentTotal.add(cardDTO.getAmount()));
        }

        // 更新会籍时间（如果是时间卡）
        if (cardDTO.getCardType() == 0 || cardDTO.getCardType() == 2 ||
                cardDTO.getCardType() == 3 || cardDTO.getCardType() == 4) {

            LocalDate today = LocalDate.now();
            LocalDate newEndDate = null;

            switch (cardDTO.getCardType()) {
                case 0: // 会籍卡 - 年卡
                case 3: // 年卡
                    newEndDate = today.plusYears(1);
                    break;
                case 2: // 月卡
                    newEndDate = today.plusMonths(1);
                    break;
                case 4: // 周卡
                    newEndDate = today.plusWeeks(1);
                    break;
            }

            if (member.getMembershipEndDate() != null &&
                    member.getMembershipEndDate().isAfter(today)) {
                // 如果会员卡未过期，在原有效期上增加天数
                long daysToAdd = Period.between(today, newEndDate).getDays();
                member.setMembershipEndDate(member.getMembershipEndDate().plusDays(daysToAdd));
            } else {
                // 如果已过期，直接设置为新日期
                member.setMembershipStartDate(today);
                member.setMembershipEndDate(newEndDate);
            }
        }

        member.setUpdateTime(LocalDateTime.now());
        memberMapper.updateById(member);

        log.info("续费会员卡成功，会员ID：{}，新总消费：{}", memberId, member.getTotalSpent());
    }

    @Override
    public List<HealthRecordDTO> getHealthRecords(Long memberId) {
        log.info("获取会员健康档案，会员ID：{}", memberId);

        LambdaQueryWrapper<HealthRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HealthRecord::getMemberId, memberId);
        queryWrapper.orderByDesc(HealthRecord::getRecordDate);

        List<HealthRecord> healthRecords = healthRecordMapper.selectList(queryWrapper);

        return healthRecords.stream()
                .map(this::convertToHealthRecordDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addHealthRecord(Long memberId, HealthRecordDTO healthRecordDTO) {
        log.info("开始添加健康档案，会员ID：{}", memberId);

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        LambdaQueryWrapper<HealthRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HealthRecord::getMemberId, memberId);
        queryWrapper.eq(HealthRecord::getRecordDate, healthRecordDTO.getRecordDate());

        Long count = healthRecordMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("该日期已存在健康记录");
        }

        HealthRecord healthRecord = new HealthRecord();
        BeanUtils.copyProperties(healthRecordDTO, healthRecord);
        healthRecord.setMemberId(memberId);
        healthRecord.setRecordedBy("系统");

        // 计算BMI
        if (healthRecord.getHeight() != null && healthRecord.getWeight() != null) {
            BigDecimal heightInM = healthRecord.getHeight().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal bmi = healthRecord.getWeight().divide(
                    heightInM.multiply(heightInM), 1, RoundingMode.HALF_UP);
            healthRecord.setBmi(bmi);
        }

        LocalDateTime now = LocalDateTime.now();
        healthRecord.setCreateTime(now);
        healthRecord.setUpdateTime(now);

        int result = healthRecordMapper.insert(healthRecord);
        if (result <= 0) {
            throw new BusinessException("添加健康档案失败");
        }

        log.info("添加健康档案成功，会员ID：{}，记录ID：{}", memberId, healthRecord.getId());
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

        MiniMemberCardDTO currentCard = getCurrentMemberCard(member.getId());
        if (currentCard != null) {
            vo.setCardType(currentCard.getCardType());
            vo.setCardStatus(currentCard.getStatus());

            // 只有会籍卡（type=0）才设置结束日期
            if (currentCard.getCardType() == 0 && currentCard.getEndDate() != null) {
                vo.setCardEndDate(currentCard.getEndDate().atStartOfDay());
            } else {
                // 非会籍卡不设置结束日期
                vo.setCardEndDate(null);
            }

            // 如果是课程卡，设置剩余课时
            if (currentCard.getCardType() == 1 || currentCard.getCardType() == 2) {
                vo.setRemainingSessions(currentCard.getRemainingSessions());
            }
        } else {
            // 如果没有当前卡，但有会员的会籍结束日期（可能是旧数据）
            if (member.getMembershipEndDate() != null) {
                vo.setCardEndDate(member.getMembershipEndDate().atStartOfDay());
                if (member.getMembershipEndDate().isBefore(LocalDate.now())) {
                    vo.setCardStatus("EXPIRED");
                } else {
                    vo.setCardStatus("ACTIVE");
                }
            }
        }

        vo.setTotalCheckins(member.getTotalCheckins() != null ? member.getTotalCheckins() : 0);
        vo.setTotalCourseHours(member.getTotalCourseHours() != null ? member.getTotalCourseHours() : 0);
        vo.setTotalSpent(member.getTotalSpent() != null ? member.getTotalSpent() : BigDecimal.ZERO);

        return vo;
    }

    private MiniMemberCardDTO getCurrentMemberCard(Long memberId) {
        try {
            List<MemberCardDTO> pcCards = getMemberCards(memberId);
            List<MiniMemberCardDTO> miniCards = convertToMiniMemberCards(pcCards);

            MiniMemberCardDTO card = miniCards.stream()
                    .filter(c -> "ACTIVE".equals(c.getStatus()))
                    .findFirst()
                    .orElse(null);

            if (card != null) {
                log.info("会员ID: {}, 卡类型: {}, 结束日期: {}", memberId, card.getCardType(), card.getEndDate());
            }

            return card;
        } catch (Exception e) {
            log.error("获取会员卡信息失败，会员ID：{}", memberId, e);
        }
        return null;
    }

    private List<CourseRecordDTO> getCourseRecords(Long memberId) {
        List<CourseRecordDTO> courseRecords = new ArrayList<>();

        try {
            LambdaQueryWrapper<CourseBooking> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CourseBooking::getMemberId, memberId);
            queryWrapper.orderByDesc(CourseBooking::getBookingTime);

            List<CourseBooking> bookings = courseBookingMapper.selectList(queryWrapper);

            for (CourseBooking booking : bookings) {
                Course course = courseMapper.selectById(booking.getCourseId());
                if (course == null) {
                    continue;
                }

                CourseRecordDTO recordDTO = new CourseRecordDTO();
                recordDTO.setCourseId(booking.getCourseId());
                recordDTO.setCourseName(course.getCourseName());

                if (course.getCoachId() != null) {
                    Coach coach = coachMapper.selectById(course.getCoachId());
                    if (coach != null) {
                        recordDTO.setCoachName(coach.getRealName());
                    }
                }

                recordDTO.setCourseDate(course.getCourseDate().atStartOfDay());
                recordDTO.setStartTime(course.getCourseDate().atTime(course.getStartTime()));
                recordDTO.setEndTime(course.getCourseDate().atTime(course.getEndTime()));
                recordDTO.setLocation(course.getLocation());
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
                        Course course = courseMapper.selectById(booking.getCourseId());
                        if (course != null) {
                            dto.setCourseName(course.getCourseName());

                            if (course.getCoachId() != null) {
                                Coach coach = coachMapper.selectById(course.getCoachId());
                                if (coach != null) {
                                    dto.setCoachName(coach.getRealName());
                                }
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
        queryWrapper.eq(Order::getPaymentStatus, 0);
        queryWrapper.in(Order::getOrderStatus, "PENDING", "PROCESSING");

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