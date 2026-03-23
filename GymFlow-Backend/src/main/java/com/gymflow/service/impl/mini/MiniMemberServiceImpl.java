package com.gymflow.service.impl.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymflow.dto.member.HealthRecordDTO;
import com.gymflow.dto.member.MemberFullDTO;
import com.gymflow.dto.mini.MiniCheckinCodeDTO;
import com.gymflow.dto.mini.MiniMemberCardDTO;
import com.gymflow.dto.mini.MiniReminderDTO;
import com.gymflow.dto.mini.MyCourseDTO;
import com.gymflow.entity.*;
import com.gymflow.entity.mini.MiniCheckinCode;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.gymflow.mapper.mini.MiniCheckinCodeMapper;
import com.gymflow.service.mini.MiniMemberService;
import com.gymflow.utils.BCryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiniMemberServiceImpl implements MiniMemberService {

    private final MemberMapper memberMapper;
    private final CoachMapper coachMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final CourseBookingMapper courseBookingMapper;
    private final CourseMapper courseMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final MiniCheckinCodeMapper miniCheckinCodeMapper;
    private final BCryptUtil bCryptUtil;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public MemberFullDTO getMyInfo(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        MemberFullDTO dto = new MemberFullDTO();
        BeanUtils.copyProperties(member, dto);
        dto.setUsername(member.getPhone());

        // 获取会员卡信息
        List<MiniMemberCardDTO> cards = getMemberCards(memberId);
        dto.setMemberCards(cards);

        // 获取健康档案列表
        List<HealthRecordDTO> healthRecords = getHealthRecords(memberId);
        dto.setHealthRecords(healthRecords);

        // 获取课程包列表
        List<MyCourseDTO> courses = getMyCourses(memberId);
        dto.setCourses(courses);

        return dto;
    }

    /**
     * 获取会员卡列表
     */
    private List<MiniMemberCardDTO> getMemberCards(Long memberId) {
        List<MiniMemberCardDTO> cardList = new ArrayList<>();

        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getMemberId, memberId)
                .eq(Order::getPaymentStatus, 1)
                .in(Order::getOrderStatus, "COMPLETED", "PROCESSING");

        List<Order> orders = orderMapper.selectList(orderQuery);
        if (CollectionUtils.isEmpty(orders)) {
            return cardList;
        }

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());

        // 查询订单项
        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.in(OrderItem::getOrderId, orderIds)
                .in(OrderItem::getProductType, 0, 1, 2)
                .orderByDesc(OrderItem::getCreateTime);

        List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);

        for (OrderItem item : orderItems) {
            MiniMemberCardDTO card = new MiniMemberCardDTO();
            card.setProductId(item.getProductId());
            card.setProductName(item.getProductName());
            card.setCardType(item.getProductType());
            card.setStartDate(item.getValidityStartDate());
            card.setEndDate(item.getValidityEndDate());
            card.setTotalSessions(item.getTotalSessions());
            card.setRemainingSessions(item.getRemainingSessions());
            card.setUsedSessions(item.getTotalSessions() != null && item.getRemainingSessions() != null ?
                    item.getTotalSessions() - item.getRemainingSessions() : 0);

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

    /**
     * 获取课程包列表
     */
    private List<MyCourseDTO> getMyCourses(Long memberId) {
        List<MyCourseDTO> courseList = new ArrayList<>();

        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getMemberId, memberId)
                .eq(Order::getPaymentStatus, 1)
                .in(Order::getOrderStatus, "COMPLETED", "PROCESSING");

        List<Order> orders = orderMapper.selectList(orderQuery);
        if (CollectionUtils.isEmpty(orders)) {
            return courseList;
        }

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());

        // 查询课程类订单项
        LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.in(OrderItem::getOrderId, orderIds)
                .in(OrderItem::getProductType, 1, 2)
                .gt(OrderItem::getRemainingSessions, 0)
                .and(wrapper -> wrapper
                        .isNull(OrderItem::getValidityEndDate)
                        .or()
                        .ge(OrderItem::getValidityEndDate, LocalDate.now()))
                .orderByDesc(OrderItem::getCreateTime);

        List<OrderItem> orderItems = orderItemMapper.selectList(itemQuery);

        for (OrderItem item : orderItems) {
            MyCourseDTO course = new MyCourseDTO();
            course.setOrderItemId(item.getId());
            course.setProductId(item.getProductId());
            course.setProductName(item.getProductName());
            course.setProductType(item.getProductType());
            course.setTotalSessions(item.getTotalSessions());
            course.setRemainingSessions(item.getRemainingSessions());
            course.setValidityStartDate(item.getValidityStartDate());
            course.setValidityEndDate(item.getValidityEndDate());

            // 判断状态
            if (item.getValidityEndDate() != null && LocalDate.now().isAfter(item.getValidityEndDate())) {
                course.setStatus("EXPIRED");
            } else if (item.getRemainingSessions() == null || item.getRemainingSessions() <= 0) {
                course.setStatus("USED_UP");
            } else {
                course.setStatus("ACTIVE");
            }

            courseList.add(course);
        }

        return courseList;
    }

    /**
     * 获取健康档案列表
     */
    private List<HealthRecordDTO> getHealthRecords(Long memberId) {
        LambdaQueryWrapper<HealthRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HealthRecord::getMemberId, memberId)
                .orderByDesc(HealthRecord::getRecordDate);

        List<HealthRecord> records = healthRecordMapper.selectList(queryWrapper);
        return records.stream().map(record -> {
            HealthRecordDTO dto = new HealthRecordDTO();
            BeanUtils.copyProperties(record, dto);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public MiniReminderDTO getCurrentReminder(Long memberId) {
        MiniReminderDTO reminder = new MiniReminderDTO();
        reminder.setHasReminder(false);

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        // 查询会员当前时段的预约（正在上课或即将上课）
        LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
        bookingQuery.eq(CourseBooking::getMemberId, memberId)
                .in(CourseBooking::getBookingStatus, 0, 1); // 待上课或已签到

        List<CourseBooking> bookings = courseBookingMapper.selectList(bookingQuery);
        if (CollectionUtils.isEmpty(bookings)) {
            return reminder;
        }

        for (CourseBooking booking : bookings) {
            CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
            if (schedule == null || !schedule.getScheduleDate().equals(today)) {
                continue;
            }

            LocalDateTime courseStart = LocalDateTime.of(today, schedule.getStartTime());
            LocalDateTime courseEnd = LocalDateTime.of(today, schedule.getEndTime());

            // 如果是当前时段（课程开始前30分钟到课程结束）
            if (now.isAfter(courseStart.minusMinutes(30)) && now.isBefore(courseEnd)) {
                Course course = courseMapper.selectById(schedule.getCourseId());

                reminder.setHasReminder(true);
                reminder.setType("MEMBER");
                reminder.setCourseName(course != null ? course.getCourseName() : "");
                reminder.setBookingTime(courseStart);

                // 获取教练姓名
                Coach coach = coachMapper.selectById(schedule.getCoachId());
                if (coach != null) {
                    reminder.setCoachName(coach.getRealName());
                }

                reminder.setBookingId(booking.getId());

                // 获取签到码
                MiniCheckinCode checkinCode = miniCheckinCodeMapper.selectByBookingId(booking.getId());
                if (checkinCode != null) {
                    MiniCheckinCodeDTO codeDTO = new MiniCheckinCodeDTO();
                    BeanUtils.copyProperties(checkinCode, codeDTO);
                    if (course != null) {
                        codeDTO.setCourseName(course.getCourseName());
                    }
                    codeDTO.setCourseStartTime(courseStart);
                    if (coach != null) {
                        codeDTO.setCoachName(coach.getRealName());
                    }
                    reminder.setCheckinCode(codeDTO);
                }
                break;
            }
        }

        return reminder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addHealthRecord(Long memberId, HealthRecordDTO healthRecordDTO) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 检查是否已存在相同日期的记录
        LambdaQueryWrapper<HealthRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HealthRecord::getMemberId, memberId)
                .eq(HealthRecord::getRecordDate, healthRecordDTO.getRecordDate());

        Long count = healthRecordMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("该日期已存在健康记录");
        }

        HealthRecord record = new HealthRecord();
        BeanUtils.copyProperties(healthRecordDTO, record);
        record.setMemberId(memberId);
        record.setRecordedBy("会员本人");

        int result = healthRecordMapper.insert(record);
        if (result <= 0) {
            throw new BusinessException("添加健康档案失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyPassword(Long memberId, String oldPassword, String newPassword) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 验证旧密码
        if (!bCryptUtil.matches(oldPassword, member.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新新密码
        member.setPassword(bCryptUtil.encodePassword(newPassword));
        memberMapper.updateById(member);
        log.info("会员密码修改成功，会员ID：{}", memberId);
    }
}