package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.member.*;
import com.gymflow.entity.*;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.gymflow.service.MemberService;
import com.gymflow.utils.BCryptUtil;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final UserMapper userMapper;
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

    // 日期格式化器
    private static final DateTimeFormatter MEMBER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public PageResultVO<MemberListVO> getMemberList(MemberQueryDTO queryDTO) {
        // 创建分页对象
        Page<Member> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();

        // 会员编号查询
        if (StringUtils.hasText(queryDTO.getMemberNo())) {
            queryWrapper.like(Member::getMemberNo, queryDTO.getMemberNo());
        }

        // 手机号查询
        if (StringUtils.hasText(queryDTO.getPhone())) {
            queryWrapper.like(Member::getPhone, queryDTO.getPhone());
        }

        // 真实姓名查询
        if (StringUtils.hasText(queryDTO.getRealName())) {
            queryWrapper.like(Member::getRealName, queryDTO.getRealName());
        }

        // 注册时间范围查询
//        if (queryDTO.getStartDate() != null) {
//            queryWrapper.ge(Member::getCreateTime, queryDTO.getStartDate().atStartOfDay());
//        }
//        if (queryDTO.getEndDate() != null) {
//            queryWrapper.le(Member::getCreateTime, queryDTO.getEndDate().atTime(23, 59, 59));
//        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Member::getCreateTime);

        // 执行分页查询
        IPage<Member> memberPage = memberMapper.selectPage(page, queryWrapper);

        // 转换为VO列表
        List<MemberListVO> voList = memberPage.getRecords().stream()
                .map(this::convertToMemberListVO)
                .collect(Collectors.toList());

        // 构建返回结果
        return new PageResultVO<>(voList, memberPage.getTotal(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public MemberFullDTO getMemberDetail(Long memberId) {
        // 获取会员基本信息
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 构建完整DTO
        MemberFullDTO fullDTO = new MemberFullDTO();

        // 设置会员基本信息
        fullDTO.setId(member.getId());
        fullDTO.setMemberNo(member.getMemberNo());
        fullDTO.setPhone(member.getPhone());
        fullDTO.setRealName(member.getRealName());
        fullDTO.setGender(member.getGender());
        fullDTO.setIdCard(member.getIdCard());
        fullDTO.setHeight(member.getHeight());
        fullDTO.setWeight(member.getWeight());
        fullDTO.setMembershipStartDate(member.getMembershipStartDate());
        fullDTO.setMembershipEndDate(member.getMembershipEndDate());
        fullDTO.setPersonalCoachName(getCoachName(member.getPersonalCoachId()));
        fullDTO.setTotalCheckins(member.getTotalCheckins());
        fullDTO.setTotalCourseHours(member.getTotalCourseHours());
        fullDTO.setTotalSpent(member.getTotalSpent());
        fullDTO.setAddress(member.getAddress());
        fullDTO.setCreateTime(member.getCreateTime());

        // 设置用户名（手机号作为用户名）
        fullDTO.setUsername(member.getPhone());

        // 获取健康档案列表
        fullDTO.setHealthRecords(getHealthRecords(memberId));

        // 获取会员卡列表
        fullDTO.setMemberCards(getMemberCards(memberId));

        // 获取课程记录列表
        fullDTO.setCourseRecords(getCourseRecords(memberId));

        // 获取签到记录列表
        fullDTO.setCheckinRecords(getCheckinRecords(memberId));

        return fullDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addMember(MemberBasicDTO basicDTO, HealthRecordDTO healthRecordDTO, MemberCardDTO cardDTO) {
        log.info("开始添加会员，手机号：{}", basicDTO.getPhone());

        // 1. 检查手机号是否已存在
        LambdaQueryWrapper<Member> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(Member::getPhone, basicDTO.getPhone());
        Long memberCount = memberMapper.selectCount(memberQuery);
        if (memberCount > 0) {
            throw new BusinessException("该手机号已注册为会员");
        }

        // 2. 检查会员编号是否已存在
        if (StringUtils.hasText(basicDTO.getMemberNo())) {
            LambdaQueryWrapper<Member> memberNoQuery = new LambdaQueryWrapper<>();
            memberNoQuery.eq(Member::getMemberNo, basicDTO.getMemberNo());
            Long memberNoCount = memberMapper.selectCount(memberNoQuery);
            if (memberNoCount > 0) {
                throw new BusinessException("会员编号已存在");
            }
        }

        // 3. 创建会员记录
        Member member = new Member();

        // 设置基本信息
        if (!StringUtils.hasText(basicDTO.getMemberNo())) {
            // 自动生成会员编号
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

        // 初始化统计信息
        member.setTotalCheckins(0);
        member.setTotalCourseHours(0);
        member.setTotalSpent(BigDecimal.ZERO);

        // 4. 保存会员
        int result = memberMapper.insert(member);
        if (result <= 0) {
            throw new BusinessException("添加会员失败");
        }

        log.info("添加会员成功，ID：{}，会员编号：{}", member.getId(), member.getMemberNo());

        // 5. 保存健康记录（如果有）
        if (healthRecordDTO != null) {
            addHealthRecord(member.getId(), healthRecordDTO);
        }

        // 6. 处理会员卡信息（如果有）
        if (cardDTO != null) {
            activateMemberCard(member.getId(), cardDTO);
        }

        return member.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMember(Long memberId, MemberBasicDTO basicDTO, MemberCardDTO cardDTO) {
        log.info("开始更新会员，ID：{}", memberId);

        // 1. 检查会员是否存在
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 2. 检查手机号是否已被其他会员使用
        if (!member.getPhone().equals(basicDTO.getPhone())) {
            LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Member::getPhone, basicDTO.getPhone());
            queryWrapper.ne(Member::getId, memberId);
            Long count = memberMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("该手机号已被其他会员使用");
            }
        }

        // 3. 检查会员编号是否已被其他会员使用
        if (!member.getMemberNo().equals(basicDTO.getMemberNo())) {
            LambdaQueryWrapper<Member> memberNoQuery = new LambdaQueryWrapper<>();
            memberNoQuery.eq(Member::getMemberNo, basicDTO.getMemberNo());
            memberNoQuery.ne(Member::getId, memberId);
            Long memberNoCount = memberMapper.selectCount(memberNoQuery);
            if (memberNoCount > 0) {
                throw new BusinessException("会员编号已被其他会员使用");
            }
        }

        // 4. 更新会员基本信息
        member.setMemberNo(basicDTO.getMemberNo());
        member.setPhone(basicDTO.getPhone());

        // 只有密码不为空时才更新密码
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

        // 5. 更新会员记录
        int result = memberMapper.updateById(member);
        if (result <= 0) {
            throw new BusinessException("更新会员失败");
        }

        log.info("更新会员成功，ID：{}", memberId);

        // 6. 更新会员卡信息（如果需要）
        if (cardDTO != null) {
            updateMemberCard(memberId, cardDTO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMember(Long memberId) {
        log.info("开始删除会员，ID：{}", memberId);

        // 1. 检查会员是否存在
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 2. 检查会员是否有未完成的订单
        checkUnfinishedOrders(memberId);

        // 3. 检查会员是否有未完成的课程
        checkUnfinishedCourses(memberId);

        // 4. 删除会员
        int result = memberMapper.deleteById(memberId);
        if (result <= 0) {
            throw new BusinessException("删除会员失败");
        }

        // 5. 删除相关记录（可选，根据业务需求决定）
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

        // 1. 检查会员是否存在
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 2. 创建续费订单
        createRenewalOrder(memberId, cardDTO);

        // 3. 更新会员有效期
        if (cardDTO.getEndDate() != null) {
            member.setMembershipEndDate(cardDTO.getEndDate());
        }

        // 4. 更新统计信息
        if (cardDTO.getAmount() != null) {
            BigDecimal currentTotal = member.getTotalSpent() != null ?
                    member.getTotalSpent() : BigDecimal.ZERO;
            member.setTotalSpent(currentTotal.add(cardDTO.getAmount()));
        }

        // 5. 保存更新
        int result = memberMapper.updateById(member);
        if (result <= 0) {
            throw new BusinessException("续费会员卡失败");
        }

        log.info("续费会员卡成功，会员ID：{}", memberId);
    }

    @Override
    public List<HealthRecordDTO> getHealthRecords(Long memberId) {
        log.info("获取会员健康档案，会员ID：{}", memberId);

        // 查询健康记录
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

        // 1. 检查会员是否存在
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 2. 检查是否已存在相同日期的记录
        LambdaQueryWrapper<HealthRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HealthRecord::getMemberId, memberId);
        queryWrapper.eq(HealthRecord::getRecordDate, healthRecordDTO.getRecordDate());

        Long count = healthRecordMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("该日期已存在健康记录");
        }

        // 3. 创建健康记录
        HealthRecord healthRecord = new HealthRecord();
        healthRecord.setMemberId(memberId);
        healthRecord.setRecordDate(healthRecordDTO.getRecordDate());
        healthRecord.setWeight(healthRecordDTO.getWeight());
        healthRecord.setBodyFatPercentage(healthRecordDTO.getBodyFatPercentage());
        healthRecord.setMuscleMass(healthRecordDTO.getMuscleMass());
        healthRecord.setBmi(healthRecordDTO.getBmi());
        healthRecord.setChestCircumference(healthRecordDTO.getChestCircumference());
        healthRecord.setWaistCircumference(healthRecordDTO.getWaistCircumference());
        healthRecord.setHipCircumference(healthRecordDTO.getHipCircumference());
        healthRecord.setBloodPressure(healthRecordDTO.getBloodPressure());
        healthRecord.setHeartRate(healthRecordDTO.getHeartRate());
        healthRecord.setNotes(healthRecordDTO.getNotes());
        healthRecord.setRecordedBy("系统"); // 可根据实际情况设置记录人

        // 4. 保存健康记录
        int result = healthRecordMapper.insert(healthRecord);
        if (result <= 0) {
            throw new BusinessException("添加健康档案失败");
        }

        log.info("添加健康档案成功，会员ID：{}，记录ID：{}", memberId, healthRecord.getId());
    }

    /**
     * 将Member实体转换为MemberListVO
     */
    private MemberListVO convertToMemberListVO(Member member) {
        MemberListVO vo = new MemberListVO();

        // 设置基本信息
        vo.setId(member.getId());
        vo.setMemberNo(member.getMemberNo());
        vo.setPhone(member.getPhone());
        vo.setRealName(member.getRealName());
        vo.setGender(member.getGender());
        vo.setCreateTime(member.getCreateTime());

        // 计算年龄
        if (member.getBirthday() != null) {
            vo.setAge(Period.between(member.getBirthday(), LocalDate.now()).getYears());
        }

        // 设置教练姓名
        vo.setPersonalCoachName(getCoachName(member.getPersonalCoachId()));

        // 获取会员卡信息
        MemberCardDTO currentCard = getCurrentMemberCard(member.getId());
        if (currentCard != null) {
            vo.setCardType(currentCard.getCardType());
            vo.setCardTypeDesc(vo.getCardTypeDesc()); // 调用VO自己的方法
            vo.setCardStatus(currentCard.getStatus());
            vo.setCardStatusDesc(vo.getCardStatusDesc()); // 调用VO自己的方法
            vo.setRemainingSessions(currentCard.getRemainingSessions());

            if (currentCard.getEndDate() != null) {
                vo.setCardEndDate(currentCard.getEndDate().atStartOfDay());
            }
        } else {
            // 设置默认值
            vo.setCardType(2); // 默认月卡
            vo.setCardTypeDesc(vo.getCardTypeDesc());
            vo.setCardStatus("ACTIVE");
            vo.setCardStatusDesc(vo.getCardStatusDesc());

            if (member.getMembershipEndDate() != null) {
                vo.setCardEndDate(member.getMembershipEndDate().atStartOfDay());
            }
        }

        // 设置统计信息
        vo.setTotalCheckins(member.getTotalCheckins() != null ? member.getTotalCheckins() : 0);
        vo.setTotalCourseHours(member.getTotalCourseHours() != null ? member.getTotalCourseHours() : 0);
        vo.setTotalSpent(member.getTotalSpent() != null ? member.getTotalSpent() : BigDecimal.ZERO);

        return vo;
    }

    /**
     * 获取当前有效的会员卡信息
     */
    private MemberCardDTO getCurrentMemberCard(Long memberId) {
        try {
            // 查询会员最近的有效订单项
            LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
            orderQuery.eq(Order::getMemberId, memberId);
            orderQuery.eq(Order::getOrderStatus, "COMPLETED");
            orderQuery.orderByDesc(Order::getCreateTime);

            List<Order> orders = orderMapper.selectList(orderQuery);

            for (Order order : orders) {
                LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
                itemQuery.eq(OrderItem::getOrderId, order.getId());
                itemQuery.in(OrderItem::getProductType, 0, 1, 2); // 会籍卡、私教课、团课
                itemQuery.eq(OrderItem::getStatus, "ACTIVE");
                itemQuery.ge(OrderItem::getValidityEndDate, LocalDate.now()); // 未过期

                List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);

                if (!CollectionUtils.isEmpty(orderItems)) {
                    // 取第一个有效的订单项
                    OrderItem orderItem = orderItems.get(0);

                    // 查询商品信息
                    Product product = productMapper.selectById(orderItem.getProductId());

                    MemberCardDTO cardDTO = new MemberCardDTO();
                    cardDTO.setProductId(orderItem.getProductId());
                    cardDTO.setProductName(orderItem.getProductName());
                    cardDTO.setProductType(orderItem.getProductType());
                    cardDTO.setCardType(orderItem.getProductType()); // 产品类型映射为卡类型
                    cardDTO.setStartDate(orderItem.getValidityStartDate());
                    cardDTO.setEndDate(orderItem.getValidityEndDate());
                    cardDTO.setTotalSessions(orderItem.getTotalSessions());
                    cardDTO.setRemainingSessions(orderItem.getRemainingSessions());
                    cardDTO.setUsedSessions(orderItem.getTotalSessions() - orderItem.getRemainingSessions());

                    if (orderItem.getTotalPrice() != null) {
                        cardDTO.setAmount(orderItem.getTotalPrice());
                    }

                    // 判断状态
                    if (LocalDate.now().isAfter(orderItem.getValidityEndDate())) {
                        cardDTO.setStatus("EXPIRED");
                    } else if (orderItem.getRemainingSessions() != null && orderItem.getRemainingSessions() <= 0) {
                        cardDTO.setStatus("USED_UP");
                    } else {
                        cardDTO.setStatus("ACTIVE");
                    }

                    return cardDTO;
                }
            }
        } catch (Exception e) {
            log.error("获取会员卡信息失败，会员ID：{}", memberId, e);
        }

        return null;
    }

    /**
     * 获取会员卡列表
     */
    private List<MemberCardDTO> getMemberCards(Long memberId) {
        List<MemberCardDTO> memberCards = new ArrayList<>();

        try {
            // 查询所有订单
            LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
            orderQuery.eq(Order::getMemberId, memberId);
            orderQuery.in(Order::getOrderType, 0, 1, 2); // 会籍卡、私教课、团课
            orderQuery.eq(Order::getOrderStatus, "COMPLETED");
            orderQuery.orderByDesc(Order::getCreateTime);

            List<Order> orders = orderMapper.selectList(orderQuery);

            for (Order order : orders) {
                LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
                itemQuery.eq(OrderItem::getOrderId, order.getId());
                itemQuery.in(OrderItem::getProductType, 0, 1, 2);

                List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);

                for (OrderItem orderItem : orderItems) {
                    MemberCardDTO cardDTO = new MemberCardDTO();
                    cardDTO.setProductId(orderItem.getProductId());
                    cardDTO.setProductName(orderItem.getProductName());
                    cardDTO.setProductType(orderItem.getProductType());
                    cardDTO.setCardType(orderItem.getProductType());
                    cardDTO.setStartDate(orderItem.getValidityStartDate());
                    cardDTO.setEndDate(orderItem.getValidityEndDate());
                    cardDTO.setTotalSessions(orderItem.getTotalSessions());
                    cardDTO.setRemainingSessions(orderItem.getRemainingSessions());
                    cardDTO.setUsedSessions(orderItem.getTotalSessions() - orderItem.getRemainingSessions());

                    if (orderItem.getTotalPrice() != null) {
                        cardDTO.setAmount(orderItem.getTotalPrice());
                    }

                    // 判断状态
                    if (orderItem.getStatus() != null) {
                        cardDTO.setStatus(orderItem.getStatus());
                    } else if (orderItem.getValidityEndDate() != null &&
                            LocalDate.now().isAfter(orderItem.getValidityEndDate())) {
                        cardDTO.setStatus("EXPIRED");
                    } else if (orderItem.getRemainingSessions() != null &&
                            orderItem.getRemainingSessions() <= 0) {
                        cardDTO.setStatus("USED_UP");
                    } else {
                        cardDTO.setStatus("ACTIVE");
                    }

                    memberCards.add(cardDTO);
                }
            }
        } catch (Exception e) {
            log.error("获取会员卡列表失败，会员ID：{}", memberId, e);
        }

        return memberCards;
    }

    /**
     * 获取课程记录
     */
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

    /**
     * 获取签到记录
     */
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

                // 如果有课程预约，获取课程信息
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

    /**
     * 健康记录实体转DTO
     */
    private HealthRecordDTO convertToHealthRecordDTO(HealthRecord healthRecord) {
        HealthRecordDTO dto = new HealthRecordDTO();
        BeanUtils.copyProperties(healthRecord, dto);
        return dto;
    }

    /**
     * 根据教练ID获取教练姓名
     */
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

    /**
     * 生成会员编号
     */
    private String generateMemberNo() {
        String dateStr = LocalDate.now().format(MEMBER_NO_FORMATTER);
        String randomStr = String.format("%04d", (int)(Math.random() * 10000));
        return "GYM" + dateStr + randomStr;
    }

    /**
     * 激活会员卡
     */
    private void activateMemberCard(Long memberId, MemberCardDTO cardDTO) {
        log.info("激活会员卡，会员ID：{}", memberId);

        // 这里可以创建订单、更新订单项等操作
        // 具体实现根据业务需求
    }

    /**
     * 更新会员卡
     */
    private void updateMemberCard(Long memberId, MemberCardDTO cardDTO) {
        log.info("更新会员卡，会员ID：{}", memberId);

        // 这里可以更新订单项等操作
        // 具体实现根据业务需求
    }

    /**
     * 检查未完成的订单
     */
    private void checkUnfinishedOrders(Long memberId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getMemberId, memberId);
        queryWrapper.in(Order::getPaymentStatus, 0); // 待支付
        queryWrapper.in(Order::getOrderStatus, "PENDING", "PROCESSING");

        Long count = orderMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("会员存在未完成的订单，不能删除");
        }
    }

    /**
     * 检查未完成的课程
     */
    private void checkUnfinishedCourses(Long memberId) {
        LambdaQueryWrapper<CourseBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseBooking::getMemberId, memberId);
        queryWrapper.in(CourseBooking::getBookingStatus, 0, 1); // 待上课、已签到

        Long count = courseBookingMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("会员有未完成的课程，不能删除");
        }
    }

    /**
     * 删除相关记录
     */
    private void deleteRelatedRecords(Long memberId) {
        // 删除健康记录
        LambdaQueryWrapper<HealthRecord> healthQuery = new LambdaQueryWrapper<>();
        healthQuery.eq(HealthRecord::getMemberId, memberId);
        healthRecordMapper.delete(healthQuery);

        // 删除签到记录
        LambdaQueryWrapper<CheckinRecord> checkinQuery = new LambdaQueryWrapper<>();
        checkinQuery.eq(CheckinRecord::getMemberId, memberId);
        checkInRecordMapper.delete(checkinQuery);

        // 注意：订单、课程预约等重要记录通常不删除，只做关联处理
        // 这里可以根据业务需求决定是否删除或标记
    }

    /**
     * 创建续费订单
     */
    private void createRenewalOrder(Long memberId, MemberCardDTO cardDTO) {
        log.info("创建续费订单，会员ID：{}", memberId);

        // 这里实现创建续费订单的逻辑
        // 包括创建Order和OrderItem记录

        // 示例：
        /*
        Order order = new Order();
        order.setMemberId(memberId);
        order.setOrderNo(generateOrderNo());
        order.setOrderType(cardDTO.getProductType());
        order.setTotalAmount(cardDTO.getAmount());
        order.setActualAmount(cardDTO.getAmount());
        order.setOrderStatus("COMPLETED");
        order.setPaymentStatus(1);
        order.setPaymentTime(LocalDateTime.now());
        order.setPaymentMethod("现金");
        order.setRemark("会员卡续费");

        orderMapper.insert(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setProductId(cardDTO.getProductId());
        orderItem.setProductName(cardDTO.getProductName());
        orderItem.setProductType(cardDTO.getProductType());
        orderItem.setQuantity(1);
        orderItem.setUnitPrice(cardDTO.getAmount());
        orderItem.setTotalPrice(cardDTO.getAmount());
        orderItem.setValidityStartDate(LocalDate.now());
        orderItem.setValidityEndDate(cardDTO.getEndDate());
        orderItem.setTotalSessions(cardDTO.getTotalSessions());
        orderItem.setRemainingSessions(cardDTO.getRemainingSessions());
        orderItem.setStatus("ACTIVE");

        orderItemMapper.insert(orderItem);
        */
    }
}