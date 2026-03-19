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

    private final BCryptUtil bCryptUtil;
    private final SystemConfigValidator configValidator;

    private static final DateTimeFormatter MEMBER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

        if (member.getPersonalCoachId() != null) {
            Coach coach = coachMapper.selectById(member.getPersonalCoachId());
            if (coach != null) {
                fullDTO.setPersonalCoachName(coach.getRealName());
            }
        }

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

            // 设置cardType（如果MemberCardDTO中有productType，可以用作cardType）
            if (pcCard.getProductType() != null) {
                miniCard.setCardType(pcCard.getProductType());
            } else {
                miniCard.setCardType(pcCard.getCardType());
            }

            // cardTypeDesc和statusDesc会在MiniMemberCardDTO的getter中自动生成
            return miniCard;
        }).collect(Collectors.toList());
    }

    /**
     * 获取会员卡列表（PC端使用）
     */
    private List<MemberCardDTO> getMemberCards(Long memberId) {
        List<MemberCardDTO> cardList = new ArrayList<>();

        // 查询会员已支付的订单
        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getMemberId, memberId)
                .eq(Order::getPaymentStatus, 1)
                .in(Order::getOrderStatus, "COMPLETED", "PROCESSING");

        List<Order> orders = orderMapper.selectList(orderQuery);
        if (CollectionUtils.isEmpty(orders)) {
            return cardList;
        }

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());

        // 查询订单项（会籍卡、私教课、团课）
        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.in(OrderItem::getOrderId, orderIds)
                .in(OrderItem::getProductType, 0, 1, 2)
                .orderByDesc(OrderItem::getCreateTime);

        List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);

        for (OrderItem item : orderItems) {
            MemberCardDTO card = new MemberCardDTO();
            card.setProductId(item.getProductId());
            card.setProductName(item.getProductName());
            card.setProductType(item.getProductType());

            // 根据productType设置cardType
            if (item.getProductType() == 0) {
                // 会籍卡，需要判断是月卡/季卡/年卡
                // 这里简化处理，可以根据validityDays判断
                card.setCardType(2); // 默认月卡
            } else {
                card.setCardType(item.getProductType()); // 私教课或团课
            }

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

        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(Member::getPhone, basicDTO.getPhone());
        Long memberCount = memberMapper.selectCount(memberQuery);
        if (memberCount > 0) {
            throw new BusinessException("该手机号已注册为会员");
        }

        if (StringUtils.hasText(basicDTO.getMemberNo())) {
            LambdaQueryWrapper<Member> memberNoQuery = new LambdaQueryWrapper<>();
            memberNoQuery.eq(Member::getMemberNo, basicDTO.getMemberNo());
            Long memberNoCount = memberMapper.selectCount(memberNoQuery);
            if (memberNoCount > 0) {
                throw new BusinessException("会员编号已存在");
            }
        }

        Member member = new Member();
        if (!StringUtils.hasText(basicDTO.getMemberNo())) {
            member.setMemberNo(generateMemberNo());
        } else {
            member.setMemberNo(basicDTO.getMemberNo());
        }

        member.setPhone(basicDTO.getPhone());
        member.setPassword(bCryptUtil.encodePassword(basicDTO.getPassword()));
        member.setRealName(basicDTO.getRealName());
        member.setIdCard(basicDTO.getIdCard());
        member.setGender(basicDTO.getGender());
        member.setBirthday(basicDTO.getBirthday());
        member.setHeight(basicDTO.getHeight());
        member.setWeight(basicDTO.getWeight());
        member.setMembershipStartDate(basicDTO.getMembershipStartDate());
        member.setMembershipEndDate(basicDTO.getMembershipEndDate());
        member.setPersonalCoachId(basicDTO.getPersonalCoachId());
        member.setAddress(basicDTO.getAddress());

        member.setTotalCheckins(0);
        member.setTotalCourseHours(0);
        member.setTotalSpent(BigDecimal.ZERO);

        int result = memberMapper.insert(member);
        if (result <= 0) {
            throw new BusinessException("添加会员失败");
        }

        log.info("添加会员成功，ID：{}，会员编号：{}", member.getId(), member.getMemberNo());

        if (healthRecordDTO != null) {
            addHealthRecord(member.getId(), healthRecordDTO);
        }

        if (cardDTO != null) {
            activateMemberCard(member.getId(), cardDTO);
        }

        return member.getId();
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

        if (!member.getMemberNo().equals(basicDTO.getMemberNo())) {
            LambdaQueryWrapper<Member> memberNoQuery = new LambdaQueryWrapper<>();
            memberNoQuery.eq(Member::getMemberNo, basicDTO.getMemberNo());
            memberNoQuery.ne(Member::getId, memberId);
            Long memberNoCount = memberMapper.selectCount(memberNoQuery);
            if (memberNoCount > 0) {
                throw new BusinessException("会员编号已被其他会员使用");
            }
        }

        member.setMemberNo(basicDTO.getMemberNo());
        member.setPhone(basicDTO.getPhone());

        if (StringUtils.hasText(basicDTO.getPassword())) {
            member.setPassword(bCryptUtil.encodePassword(basicDTO.getPassword()));
        }

        member.setRealName(basicDTO.getRealName());
        member.setIdCard(basicDTO.getIdCard());
        member.setGender(basicDTO.getGender());
        member.setBirthday(basicDTO.getBirthday());
        member.setHeight(basicDTO.getHeight());
        member.setWeight(basicDTO.getWeight());
        member.setMembershipStartDate(basicDTO.getMembershipStartDate());
        member.setMembershipEndDate(basicDTO.getMembershipEndDate());
        member.setPersonalCoachId(basicDTO.getPersonalCoachId());
        member.setAddress(basicDTO.getAddress());

        int result = memberMapper.updateById(member);
        if (result <= 0) {
            throw new BusinessException("更新会员失败");
        }

        log.info("更新会员成功，ID：{}", memberId);

        if (cardDTO != null) {
            updateMemberCard(memberId, cardDTO);
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

        if (member.getMembershipEndDate() != null) {
            configValidator.validateCourseRenewal(member.getMembershipEndDate());
        }

        createRenewalOrder(memberId, cardDTO);

        if (cardDTO.getEndDate() != null) {
            if (member.getMembershipEndDate() != null &&
                    member.getMembershipEndDate().isAfter(LocalDate.now())) {
                long daysToAdd = Period.between(LocalDate.now(), cardDTO.getEndDate()).getDays();
                member.setMembershipEndDate(member.getMembershipEndDate().plusDays(daysToAdd));
            } else {
                member.setMembershipEndDate(cardDTO.getEndDate());
            }
        }

        if (cardDTO.getAmount() != null) {
            BigDecimal currentTotal = member.getTotalSpent() != null ?
                    member.getTotalSpent() : BigDecimal.ZERO;
            member.setTotalSpent(currentTotal.add(cardDTO.getAmount()));
        }

        int result = memberMapper.updateById(member);
        if (result <= 0) {
            throw new BusinessException("续费会员卡失败");
        }

        log.info("续费会员卡成功，会员ID：{}，新有效期至：{}",
                memberId, member.getMembershipEndDate());
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

        vo.setPersonalCoachName(getCoachName(member.getPersonalCoachId()));

        MiniMemberCardDTO currentCard = getCurrentMemberCard(member.getId());
        if (currentCard != null) {
            vo.setCardType(currentCard.getCardType());
            vo.setCardStatus(currentCard.getStatus());

            if (currentCard.getEndDate() != null) {
                vo.setCardEndDate(currentCard.getEndDate().atStartOfDay());
            }
        } else {
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

            return miniCards.stream()
                    .filter(card -> "ACTIVE".equals(card.getStatus()))
                    .findFirst()
                    .orElse(null);
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
                recordDTO.setCoachName(getCoachName(course.getCoachId()));
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
                            dto.setCoachName(getCoachName(course.getCoachId()));
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

    private String getCoachName(Long coachId) {
        if (coachId == null) {
            return null;
        }
        try {
            Coach coach = coachMapper.selectById(coachId);
            return coach != null ? coach.getRealName() : null;
        } catch (Exception e) {
            log.error("获取教练姓名失败，教练ID：{}", coachId, e);
            return null;
        }
    }

    private String generateMemberNo() {
        String dateStr = LocalDate.now().format(MEMBER_NO_FORMATTER);
        String randomStr = String.format("%04d", (int)(Math.random() * 10000));
        return "MF" + dateStr + randomStr;
    }

    private void activateMemberCard(Long memberId, MemberCardDTO cardDTO) {
        log.info("激活会员卡，会员ID：{}", memberId);
    }

    private void updateMemberCard(Long memberId, MemberCardDTO cardDTO) {
        log.info("更新会员卡，会员ID：{}", memberId);
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

    private void createRenewalOrder(Long memberId, MemberCardDTO cardDTO) {
        log.info("创建续费订单，会员ID：{}", memberId);
    }
}