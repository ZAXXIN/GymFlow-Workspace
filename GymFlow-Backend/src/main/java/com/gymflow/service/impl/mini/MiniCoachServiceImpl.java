package com.gymflow.service.impl.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymflow.dto.coach.CoachFullDTO;
import com.gymflow.dto.member.CourseRecordDTO;
import com.gymflow.dto.member.HealthRecordDTO;
import com.gymflow.dto.member.MemberFullDTO;
import com.gymflow.dto.member.CheckInRecordDTO;
import com.gymflow.dto.mini.*;
import com.gymflow.entity.*;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.gymflow.service.mini.MiniCoachService;
import com.gymflow.utils.BCryptUtil;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiniCoachServiceImpl implements MiniCoachService {

    private final CoachMapper coachMapper;
    private final MemberMapper memberMapper;
    private final CourseMapper courseMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final CourseBookingMapper courseBookingMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final BCryptUtil bCryptUtil;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public CoachFullDTO getMyInfo(Long coachId) {
        log.info("获取教练信息，教练ID：{}", coachId);

        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            log.error("教练不存在，ID：{}", coachId);
            throw new BusinessException("教练不存在");
        }

        log.info("查询到的教练信息：{}", coach);
        log.info("证书原始数据：{}", coach.getCertifications());

        CoachFullDTO dto = new CoachFullDTO();

        // 手动设置所有字段
        dto.setId(coach.getId());
        dto.setRealName(coach.getRealName());
        dto.setPhone(coach.getPhone());
        dto.setGender(coach.getGender());
        dto.setSpecialty(coach.getSpecialty());
        dto.setYearsOfExperience(coach.getYearsOfExperience());
        dto.setStatus(coach.getStatus());
        dto.setRating(coach.getRating());
        dto.setIntroduction(coach.getIntroduction());
        dto.setCreateTime(coach.getCreateTime());
        dto.setUpdateTime(coach.getUpdateTime());

        // 解析证书列表
        List<String> certList = new ArrayList<>();
        if (StringUtils.hasText(coach.getCertifications())) {
            try {
                // 证书存储的是 JSON 数组，如 ["瑜伽教练资格证", "普拉提教练证"]
                ObjectMapper objectMapper = new ObjectMapper();
                certList = objectMapper.readValue(coach.getCertifications(),
                        new TypeReference<List<String>>() {});
                log.info("解析后的证书列表：{}", certList);
            } catch (Exception e) {
                log.error("解析证书列表失败", e);
                // 尝试兼容旧格式（用逗号分隔的字符串）
                String certStr = coach.getCertifications();
                certStr = certStr.replace("[", "").replace("]", "").replace("\"", "");
                String[] certs = certStr.split(",");
                for (String cert : certs) {
                    String trimmed = cert.trim();
                    if (!trimmed.isEmpty()) {
                        certList.add(trimmed);
                    }
                }
                log.info("兼容解析后的证书列表：{}", certList);
            }
        }
        dto.setCertificationList(certList);

        // 设置用户类型（用于小程序端）
        dto.setUserType(1);
        dto.setUserTypeDesc("教练");

        // 如果需要返回课程列表，可以在这里查询
        // List<CoachCourseDTO> courses = getCoachCourses(coachId);
        // dto.setCourses(courses);

        log.info("返回的教练信息：id={}, realName={}, phone={}, certifications={}",
                dto.getId(), dto.getRealName(), dto.getPhone(), dto.getCertificationList());

        return dto;
    }

    @Override
    public List<MiniScheduleDTO> getMySchedule(Long coachId, LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();

        // 查询教练的排课
        LambdaQueryWrapper<CourseSchedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSchedule::getCoachId, coachId)
                .eq(CourseSchedule::getScheduleDate, queryDate)
                .eq(CourseSchedule::getStatus, 1) // 只查正常状态的排课
                .orderByAsc(CourseSchedule::getStartTime);

        List<CourseSchedule> schedules = courseScheduleMapper.selectList(queryWrapper);
        List<MiniScheduleDTO> scheduleList = new ArrayList<>();

        for (CourseSchedule schedule : schedules) {
            // 获取课程信息
            Course course = courseMapper.selectById(schedule.getCourseId());
            if (course == null) continue;

            MiniScheduleDTO dto = new MiniScheduleDTO();
            dto.setScheduleId(schedule.getScheduleId());
            dto.setCourseId(course.getCourseId());
            dto.setCourseName(course.getCourseName());
            dto.setCourseType(course.getCourseType());
            dto.setCourseTypeDesc(course.getCourseType() == 0 ? "私教课" : "团课");
            dto.setScheduleDate(schedule.getScheduleDate());
            dto.setStartTime(schedule.getStartTime());
            dto.setEndTime(schedule.getEndTime());
            dto.setMaxCapacity(schedule.getMaxCapacity());
            dto.setCurrentEnrollment(schedule.getCurrentEnrollment());
            dto.setRemainingSlots(schedule.getMaxCapacity() - schedule.getCurrentEnrollment());

            scheduleList.add(dto);
        }

        return scheduleList;
    }

    @Override
    public List<MiniCourseStudentDTO> getCourseStudents(Long coachId, Long scheduleId) {
        // 验证排课是否属于该教练
        CourseSchedule schedule = courseScheduleMapper.selectById(scheduleId);
        if (schedule == null || !schedule.getCoachId().equals(coachId)) {
            throw new BusinessException("无权查看该课程的学员");
        }

        // 查询排课的预约
        LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
        bookingQuery.eq(CourseBooking::getScheduleId, scheduleId)
                .in(CourseBooking::getBookingStatus, 0, 1, 2, 4) // 待上课、已签到、已完成
                .orderByAsc(CourseBooking::getBookingTime);

        List<CourseBooking> bookings = courseBookingMapper.selectList(bookingQuery);
        List<MiniCourseStudentDTO> studentList = new ArrayList<>();

        for (CourseBooking booking : bookings) {
            Member member = memberMapper.selectById(booking.getMemberId());
            if (member == null) continue;

            MiniCourseStudentDTO student = new MiniCourseStudentDTO();
            student.setMemberId(member.getId());
            student.setMemberName(member.getRealName());
            student.setMemberPhone(member.getPhone());
            student.setMemberNo(member.getMemberNo());
            student.setBookingId(booking.getId());
            student.setBookingStatus(booking.getBookingStatus());
            student.setBookingTime(booking.getBookingTime());
            student.setCheckinTime(booking.getCheckinTime());
            student.setMembershipEndDate(member.getMembershipEndDate() != null ?
                    member.getMembershipEndDate().toString() : "");

            // 获取会员剩余课时
            Course course = courseMapper.selectById(schedule.getCourseId());
            student.setRemainingSessions(getMemberRemainingSessions(member.getId(), course.getCourseType()));

            studentList.add(student);
        }

        return studentList;
    }

    @Override
    public List<MiniCourseStudentDTO> getCourseStudentsBySchedule(Long coachId, Long scheduleId) {
        // 查询排课信息
        CourseSchedule schedule = courseScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排课不存在");
        }

        // 验证教练是否有权限查看该排课的学员
        if (!schedule.getCoachId().equals(coachId)) {
            throw new BusinessException("无权查看该课程的学员");
        }

        // 获取课程信息（用于确定课程类型）
        Course course = courseMapper.selectById(schedule.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 查询该排课的所有预约
        LambdaQueryWrapper<CourseBooking> bookingWrapper = new LambdaQueryWrapper<>();
        bookingWrapper.eq(CourseBooking::getScheduleId, scheduleId)
                .in(CourseBooking::getBookingStatus, Arrays.asList(0, 1, 2,4)) // 待上课、已签到、已完成
                .orderByAsc(CourseBooking::getBookingTime);

        List<CourseBooking> bookings = courseBookingMapper.selectList(bookingWrapper);

        List<MiniCourseStudentDTO> result = new ArrayList<>();
        for (CourseBooking booking : bookings) {
            Member member = memberMapper.selectById(booking.getMemberId());
            if (member == null) continue;

            MiniCourseStudentDTO dto = new MiniCourseStudentDTO();
            dto.setMemberId(member.getId());
            dto.setMemberName(member.getRealName());
            dto.setMemberPhone(member.getPhone());
            dto.setMemberNo(member.getMemberNo());
            dto.setBookingId(booking.getId());
            dto.setBookingStatus(booking.getBookingStatus());
            dto.setBookingTime(booking.getBookingTime());
            dto.setCheckinTime(booking.getCheckinTime());

            // 获取会员剩余课时
            dto.setRemainingSessions(getMemberRemainingSessions(member.getId(), course.getCourseType()));

            result.add(dto);
        }

        return result;
    }

    @Override
    public MemberFullDTO getMemberDetail(Long coachId, Long memberId) {
        // 验证教练是否有权查看该会员（通过排课关联）
        if (!canCoachViewMember(coachId, memberId)) {
            throw new BusinessException("无权查看该会员信息");
        }

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        MemberFullDTO dto = new MemberFullDTO();
        BeanUtils.copyProperties(member, dto);
        dto.setUsername(member.getPhone());

        // 计算年龄
        if (member.getBirthday() != null) {
            dto.setAge(java.time.Period.between(member.getBirthday(), LocalDate.now()).getYears());
        }

        // 1. 查询健康档案
        LambdaQueryWrapper<HealthRecord> healthWrapper = new LambdaQueryWrapper<>();
        healthWrapper.eq(HealthRecord::getMemberId, memberId)
                .orderByDesc(HealthRecord::getRecordDate);
        List<HealthRecord> healthRecords = healthRecordMapper.selectList(healthWrapper);

        List<HealthRecordDTO> healthRecordDTOs = healthRecords.stream().map(record -> {
            HealthRecordDTO dtoItem = new HealthRecordDTO();
            BeanUtils.copyProperties(record, dtoItem);
            return dtoItem;
        }).collect(Collectors.toList());
        dto.setHealthRecords(healthRecordDTOs);

        // 2. 查询会员卡
        List<MiniMemberCardDTO> memberCards = getMemberCards(memberId);
        dto.setMemberCards(memberCards);

        // 3. 查询课程记录
        List<CourseRecordDTO> courseRecords = getCourseRecords(memberId);
        dto.setCourseRecords(courseRecords);

        // 4. 查询签到记录
        List<CheckInRecordDTO> checkinRecords = getCheckinRecords(memberId);
        dto.setCheckinRecords(checkinRecords);

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

            if (item.getTotalSessions() != null && item.getRemainingSessions() != null) {
                card.setUsedSessions(item.getTotalSessions() - item.getRemainingSessions());
            }

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
        }

        return cardList;
    }

    /**
     * 获取课程记录
     */
    private List<CourseRecordDTO> getCourseRecords(Long memberId) {
        List<CourseRecordDTO> courseRecords = new ArrayList<>();

        LambdaQueryWrapper<CourseBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseBooking::getMemberId, memberId)
                .orderByDesc(CourseBooking::getBookingTime);

        List<CourseBooking> bookings = courseBookingMapper.selectList(queryWrapper);

        for (CourseBooking booking : bookings) {
            CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
            if (schedule == null) continue;

            Course course = courseMapper.selectById(schedule.getCourseId());
            if (course == null) continue;

            CourseRecordDTO record = new CourseRecordDTO();
            record.setCourseId(course.getCourseId());
            record.setCourseName(course.getCourseName());
            record.setSessionCost(course.getSessionCost());

            Coach coach = coachMapper.selectById(schedule.getCoachId());
            if (coach != null) {
                record.setCoachName(coach.getRealName());
            }

            record.setScheduleDate(schedule.getScheduleDate());
            record.setStartTime(schedule.getStartTime());
            record.setEndTime(schedule.getEndTime());
            record.setBookingStatus(booking.getBookingStatus());
            record.setCheckinTime(booking.getCheckinTime());

            courseRecords.add(record);
        }

        return courseRecords;
    }

    /**
     * 获取签到记录
     */
    private List<CheckInRecordDTO> getCheckinRecords(Long memberId) {
        List<CheckInRecordDTO> checkinRecords = new ArrayList<>();

        LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CheckinRecord::getMemberId, memberId)
                .orderByDesc(CheckinRecord::getCheckinTime);

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
                            dto.setCourseId(course.getCourseId());
                            dto.setScheduleId(schedule.getScheduleId());

                            Coach coach = coachMapper.selectById(schedule.getCoachId());
                            if (coach != null) {
                                dto.setCoachName(coach.getRealName());
                                dto.setCoachId(coach.getId());
                            }
                        }
                    }
                }
            }

            checkinRecords.add(dto);
        }

        return checkinRecords;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addHealthRecord(Long coachId, Long memberId, HealthRecordDTO healthRecordDTO) {
        // 验证教练是否有权为该会员添加健康档案
        if (!canCoachViewMember(coachId, memberId)) {
            throw new BusinessException("无权为该会员添加健康档案");
        }

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new BusinessException("教练不存在");
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
        record.setRecordedBy(coach.getRealName());

        int result = healthRecordMapper.insert(record);
        if (result <= 0) {
            throw new BusinessException("添加健康档案失败");
        }
    }

    @Override
    public MiniReminderDTO getCurrentReminder(Long coachId) {
        MiniReminderDTO reminder = new MiniReminderDTO();
        reminder.setHasReminder(false);

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        // 查询教练当前时段的排课
        LambdaQueryWrapper<CourseSchedule> scheduleQuery = new LambdaQueryWrapper<>();
        scheduleQuery.eq(CourseSchedule::getCoachId, coachId)
                .eq(CourseSchedule::getScheduleDate, today)
                .eq(CourseSchedule::getStatus, 1)
                .le(CourseSchedule::getStartTime, currentTime.plusMinutes(30)) // 即将开始的课程
                .ge(CourseSchedule::getEndTime, currentTime); // 尚未结束的课程

        List<CourseSchedule> schedules = courseScheduleMapper.selectList(scheduleQuery);
        if (CollectionUtils.isEmpty(schedules)) {
            return reminder;
        }

        CourseSchedule schedule = schedules.get(0); // 取第一个
        Course course = courseMapper.selectById(schedule.getCourseId());
        LocalDateTime courseStart = LocalDateTime.of(today, schedule.getStartTime());

        // 查询该排课的预约学员
        LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
        bookingQuery.eq(CourseBooking::getScheduleId, schedule.getScheduleId())
                .eq(CourseBooking::getBookingStatus, 0); // 待上课

        List<CourseBooking> bookings = courseBookingMapper.selectList(bookingQuery);
        if (!CollectionUtils.isEmpty(bookings)) {
            CourseBooking booking = bookings.get(0);
            Member member = memberMapper.selectById(booking.getMemberId());
            if (member != null && course != null) {
                reminder.setHasReminder(true);
                reminder.setType("COACH");
                reminder.setMemberId(member.getId());
                reminder.setMemberName(member.getRealName());
                reminder.setBookedCourseName(course.getCourseName());
                reminder.setBookedStartTime(courseStart);
                reminder.setBookedEndTime(LocalDateTime.of(today, schedule.getEndTime()));
            }
        }

        return reminder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyPassword(Long coachId, String oldPassword, String newPassword) {
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }

        // 验证旧密码
        if (!bCryptUtil.matches(oldPassword, coach.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新新密码
        coach.setPassword(bCryptUtil.encodePassword(newPassword));
        coachMapper.updateById(coach);
        log.info("教练密码修改成功，教练ID：{}", coachId);
    }

    /**
     * 获取教练的所有排课
     */
    private List<CourseSchedule> getCoachSchedules(Long coachId) {
        LambdaQueryWrapper<CourseSchedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSchedule::getCoachId, coachId)
                .ge(CourseSchedule::getScheduleDate, LocalDate.now());
        return courseScheduleMapper.selectList(queryWrapper);
    }

    /**
     * 判断教练是否可以查看会员信息
     */
    private boolean canCoachViewMember(Long coachId, Long memberId) {
        List<CourseSchedule> schedules = getCoachSchedules(coachId);
        if (CollectionUtils.isEmpty(schedules)) {
            return false;
        }

        List<Long> scheduleIds = schedules.stream().map(CourseSchedule::getScheduleId).collect(Collectors.toList());

        LambdaQueryWrapper<CourseBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CourseBooking::getScheduleId, scheduleIds)
                .eq(CourseBooking::getMemberId, memberId);

        Long count = courseBookingMapper.selectCount(queryWrapper);
        return count > 0;
    }

    /**
     * 获取会员剩余课时（修改为按课时类型查询）
     */
    private Integer getMemberRemainingSessions(Long memberId, Integer courseType) {
        // courseType: 0-私教课, 1-团课
        Integer targetProductType = courseType == 0 ? 1 : 2;

        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getProductType, targetProductType)
                .eq(OrderItem::getStatus, "ACTIVE")
                .gt(OrderItem::getRemainingSessions, 0)
                .ge(OrderItem::getValidityEndDate, LocalDate.now());

        List<OrderItem> items = orderItemMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(items)) {
            return 0;
        }

        // 返回总剩余课时
        return items.stream()
                .mapToInt(OrderItem::getRemainingSessions)
                .sum();
    }

    @Override
    public MiniFinanceStatsDTO getFinanceStats(Long coachId, String period) {
        MiniFinanceStatsDTO stats = new MiniFinanceStatsDTO();
        stats.setPeriod(period);

        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            return stats;
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate, endDate;

        switch (period) {
            case "day":
                startDate = now;
                endDate = now;
                break;
            case "month":
                startDate = now.withDayOfMonth(1);
                endDate = now;
                break;
            case "year":
                startDate = now.withDayOfYear(1);
                endDate = now;
                break;
            default:
                startDate = now.withDayOfMonth(1);
                endDate = now;
        }

        // 查询该时间范围内的排课
        LambdaQueryWrapper<CourseSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(CourseSchedule::getCoachId, coachId)
                .between(CourseSchedule::getScheduleDate, startDate, endDate)
                .eq(CourseSchedule::getStatus, 1);

        List<CourseSchedule> schedules = courseScheduleMapper.selectList(scheduleWrapper);

        // 计算本月课时数
        int monthCourseHours = 0;
        for (CourseSchedule schedule : schedules) {
            Course course = courseMapper.selectById(schedule.getCourseId());
            if (course != null && course.getDuration() != null) {
                monthCourseHours += course.getDuration();
            }
        }
        monthCourseHours = monthCourseHours / 60; // 转换为小时

        // 本月服务会员数（去重）
        List<Long> memberIds = new ArrayList<>();
        for (CourseSchedule schedule : schedules) {
            LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
            bookingQuery.eq(CourseBooking::getScheduleId, schedule.getScheduleId())
                    .in(CourseBooking::getBookingStatus, 1, 2); // 已签到或已完成
            List<CourseBooking> bookings = courseBookingMapper.selectList(bookingQuery);
            memberIds.addAll(bookings.stream().map(CourseBooking::getMemberId).collect(Collectors.toList()));
        }
        long monthMemberCount = memberIds.stream().distinct().count();

        // 本月收入（根据课时消耗计算）
        BigDecimal monthIncome = BigDecimal.ZERO;
        for (CourseSchedule schedule : schedules) {
            LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
            bookingQuery.eq(CourseBooking::getScheduleId, schedule.getScheduleId())
                    .in(CourseBooking::getBookingStatus, 1, 2);
            List<CourseBooking> bookings = courseBookingMapper.selectList(bookingQuery);

            Course course = courseMapper.selectById(schedule.getCourseId());
//            if (course != null && course.getSessionCost() != null) {
//                // 收入 = 课时消耗 × 课时单价
//                // 课时单价需要从教练的课程包价格计算，这里简化为使用教练的时薪
//                BigDecimal sessionIncome = coach.getHourlyRate() != null ?
//                        coach.getHourlyRate().multiply(BigDecimal.valueOf(course.getSessionCost())) :
//                        BigDecimal.ZERO;
//                monthIncome = monthIncome.add(sessionIncome.multiply(BigDecimal.valueOf(bookings.size())));
//            }
        }

        stats.setMonthCourseHours(monthCourseHours);
        stats.setMonthMemberCount((int) monthMemberCount);
        stats.setMonthIncome(monthIncome);

        // 生成趋势数据
        stats.setCourseTrend(generateTrendData(startDate, endDate, "course"));
        stats.setIncomeTrend(generateTrendData(startDate, endDate, "income"));
        stats.setMemberTrend(generateTrendData(startDate, endDate, "member"));

        return stats;
    }

    /**
     * 生成趋势数据
     */
    private List<ChartDataDTO> generateTrendData(LocalDate startDate, LocalDate endDate, String type) {
        List<ChartDataDTO> trend = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            ChartDataDTO point = new ChartDataDTO();
            point.setDate(current.format(DATE_FORMATTER));
            point.setLabel(current.format(DateTimeFormatter.ofPattern("MM-dd")));
            point.setValue(BigDecimal.ZERO);
            trend.add(point);
            current = current.plusDays(1);
        }
        return trend;
    }

}