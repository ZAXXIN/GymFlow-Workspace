package com.gymflow.service.impl.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymflow.dto.course.CourseFullDTO;
import com.gymflow.dto.mini.*;
import com.gymflow.entity.*;
import com.gymflow.entity.mini.MiniCheckinCode;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.gymflow.mapper.mini.MiniCheckinCodeMapper;
import com.gymflow.service.mini.MiniBookingService;
import com.gymflow.utils.SystemConfigValidator;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiniBookingServiceImpl implements MiniBookingService {

    private final CourseMapper courseMapper;
    private final CoachMapper coachMapper;
    private final MemberMapper memberMapper;
    private final CourseBookingMapper courseBookingMapper;
    private final OrderItemMapper orderItemMapper;
    private final MiniCheckinCodeMapper miniCheckinCodeMapper;
    private final SystemConfigValidator configValidator;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public List<MiniAvailableCourseDTO> getAvailableCourses(Long memberId, Integer courseType, String keyword) {
        // 查询可预约的课程（状态正常、未开始、有名额）
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getStatus, 1) // 正常状态
                .ge(Course::getCourseDate, today); // 今天及以后的课程

        // 修复：不能直接用 lt 比较两个字段，需要分别查询后过滤
        List<Course> allCourses = courseMapper.selectList(queryWrapper);
        List<Course> availableCourses = new ArrayList<>();

        for (Course course : allCourses) {
            if (course.getCurrentEnrollment() < course.getMaxCapacity()) {
                availableCourses.add(course);
            }
        }

        if (courseType != null) {
            availableCourses.removeIf(course -> !course.getCourseType().equals(courseType));
        }

        if (StringUtils.hasText(keyword)) {
            availableCourses.removeIf(course ->
                    !course.getCourseName().contains(keyword) &&
                            (course.getDescription() == null || !course.getDescription().contains(keyword)));
        }

        // 排序
        availableCourses.sort((c1, c2) -> {
            if (!c1.getCourseDate().equals(c2.getCourseDate())) {
                return c1.getCourseDate().compareTo(c2.getCourseDate());
            }
            return c1.getStartTime().compareTo(c2.getStartTime());
        });

        List<MiniAvailableCourseDTO> result = new ArrayList<>();
        for (Course course : availableCourses) {
            MiniAvailableCourseDTO dto = convertToAvailableCourseDTO(course);

            // 检查会员是否可以预约该课程
            dto.setCanBook(checkCanBook(memberId, course));
            if (!dto.getCanBook()) {
                dto.setCannotBookReason(getCannotBookReason(memberId, course));
            }

            result.add(dto);
        }

        return result;
    }

    @Override
    public CourseFullDTO getCourseDetailForBooking(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        CourseFullDTO dto = new CourseFullDTO();
        BeanUtils.copyProperties(course, dto);
        dto.setCourseTypeDesc(course.getCourseType() == 0 ? "私教课" : "团课");

        // 获取教练信息 - 修复匿名内部类问题
        Coach coach = coachMapper.selectById(course.getCoachId());
        if (coach != null) {
            com.gymflow.dto.coach.CoachBasicDTO coachDTO = new com.gymflow.dto.coach.CoachBasicDTO();
            coachDTO.setId(coach.getId());
            coachDTO.setRealName(coach.getRealName());
            coachDTO.setPhone(coach.getPhone());
            coachDTO.setSpecialty(coach.getSpecialty());
            coachDTO.setYearsOfExperience(coach.getYearsOfExperience());
            coachDTO.setHourlyRate(coach.getHourlyRate());
            coachDTO.setRating(coach.getRating());
            coachDTO.setIntroduction(coach.getIntroduction());
            dto.setCoaches(Collections.singletonList(coachDTO));
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBooking(Long memberId, MiniCreateBookingDTO createDTO) {
        log.info("创建预约，会员ID：{}，课程ID：{}", memberId, createDTO.getCourseId());

        // 验证课程是否存在
        Course course = courseMapper.selectById(createDTO.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 验证课程状态
        if (course.getStatus() != 1) {
            throw new BusinessException("课程已下架");
        }

        // 验证课程时间
        LocalDateTime courseDateTime = LocalDateTime.of(createDTO.getCourseDate(), createDTO.getStartTime());
        if (courseDateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("不能预约已开始的课程");
        }

        // 验证名额
        if (course.getCurrentEnrollment() >= course.getMaxCapacity()) {
            throw new BusinessException("课程已满");
        }

        // 验证会员是否已有该课程的预约
        LambdaQueryWrapper<CourseBooking> existsQuery = new LambdaQueryWrapper<>();
        existsQuery.eq(CourseBooking::getMemberId, memberId)
                .eq(CourseBooking::getCourseId, createDTO.getCourseId())
                .in(CourseBooking::getBookingStatus, 0, 1); // 待上课或已签到
        Long existsCount = courseBookingMapper.selectCount(existsQuery);
        if (existsCount > 0) {
            throw new BusinessException("您已预约该课程");
        }

        // 验证会员是否有足够的课时
        checkMemberSessions(memberId, course.getCourseType());

        // 创建预约记录
        CourseBooking booking = new CourseBooking();
        booking.setMemberId(memberId);
        booking.setCourseId(course.getId());
        booking.setBookingTime(LocalDateTime.now());
        booking.setBookingStatus(0); // 待上课

        int result = courseBookingMapper.insert(booking);
        if (result <= 0) {
            throw new BusinessException("创建预约失败");
        }

        // 更新课程报名人数
        course.setCurrentEnrollment(course.getCurrentEnrollment() + 1);
        courseMapper.updateById(course);

        // 生成签到码
        generateCheckinCode(booking.getId(), courseDateTime);

        // 扣减会员课时
        deductMemberSessions(memberId, course.getCourseType());

        log.info("预约创建成功，预约ID：{}", booking.getId());
        return booking.getId();
    }

    @Override
    public List<MiniBookingDTO> getMyBookings(Long memberId, Integer status) {
        LambdaQueryWrapper<CourseBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseBooking::getMemberId, memberId);

        if (status != null) {
            queryWrapper.eq(CourseBooking::getBookingStatus, status);
        }

        queryWrapper.orderByDesc(CourseBooking::getBookingTime);

        List<CourseBooking> bookings = courseBookingMapper.selectList(queryWrapper);
        List<MiniBookingDTO> bookingDTOs = new ArrayList<>();

        for (CourseBooking booking : bookings) {
            Course course = courseMapper.selectById(booking.getCourseId());
            if (course == null) continue;

            MiniBookingDTO dto = convertToMiniBookingDTO(booking, course);

            // 检查是否可取消
            dto.setCanCancel(checkCanCancel(booking, course));

            // 检查是否可签到
            dto.setCanCheckin(checkCanCheckin(booking, course));

            bookingDTOs.add(dto);
        }

        return bookingDTOs;
    }

    @Override
    public MiniBookingDetailDTO getBookingDetail(Long userId, Long bookingId) {
        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预约不存在");
        }

        Course course = courseMapper.selectById(booking.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        Member member = memberMapper.selectById(booking.getMemberId());
        Coach coach = coachMapper.selectById(course.getCoachId());

        MiniBookingDetailDTO dto = new MiniBookingDetailDTO();
        dto.setBookingId(booking.getId());
        dto.setCourseId(course.getId());
        dto.setCourseName(course.getCourseName());
        dto.setCourseType(course.getCourseType());
        dto.setCourseDescription(course.getDescription());

        if (coach != null) {
            dto.setCoachId(coach.getId());
            dto.setCoachName(coach.getRealName());
            dto.setCoachPhone(coach.getPhone());
        }

        dto.setCourseDate(course.getCourseDate().toString());
        dto.setStartTime(course.getStartTime().format(TIME_FORMATTER));
        dto.setEndTime(course.getEndTime().format(TIME_FORMATTER));
        dto.setLocation(course.getLocation());
        dto.setPrice(course.getPrice());
        dto.setBookingTime(booking.getBookingTime());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setCheckinTime(booking.getCheckinTime());
        dto.setCancellationReason(booking.getCancellationReason());
        dto.setCancellationTime(booking.getCancellationTime());

        if (member != null) {
            dto.setMemberName(member.getRealName());
            dto.setMemberPhone(member.getPhone());
            dto.setMemberNo(member.getMemberNo());
        }

        // 获取签到码
        MiniCheckinCode checkinCode = miniCheckinCodeMapper.selectByBookingId(bookingId);
        if (checkinCode != null) {
            MiniCheckinCodeDTO codeDTO = new MiniCheckinCodeDTO();
            BeanUtils.copyProperties(checkinCode, codeDTO);
            codeDTO.setCourseName(course.getCourseName());
            codeDTO.setCourseStartTime(LocalDateTime.of(course.getCourseDate(), course.getStartTime()));
            if (coach != null) {
                codeDTO.setCoachName(coach.getRealName());
            }
            dto.setCheckinCode(codeDTO);
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long memberId, Long bookingId, String reason) {
        log.info("取消预约，预约ID：{}，原因：{}", bookingId, reason);

        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预约不存在");
        }

        // 验证是否是本人的预约
        if (!booking.getMemberId().equals(memberId)) {
            throw new BusinessException("无权取消他人的预约");
        }

        // 验证预约状态
        if (booking.getBookingStatus() != 0) {
            throw new BusinessException("当前状态不能取消");
        }

        Course course = courseMapper.selectById(booking.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 验证取消时间
        LocalDateTime courseDateTime = LocalDateTime.of(course.getCourseDate(), course.getStartTime());
        configValidator.validateCourseCancellation(courseDateTime);

        // 更新预约状态
        booking.setBookingStatus(3); // 已取消
        booking.setCancellationReason(reason);
        booking.setCancellationTime(LocalDateTime.now());
        courseBookingMapper.updateById(booking);

        // 更新课程报名人数
        course.setCurrentEnrollment(Math.max(0, course.getCurrentEnrollment() - 1));
        courseMapper.updateById(course);

        // 更新签到码状态
        MiniCheckinCode checkinCode = miniCheckinCodeMapper.selectByBookingId(bookingId);
        if (checkinCode != null) {
            checkinCode.setStatus(2); // 已过期
            miniCheckinCodeMapper.updateById(checkinCode);
        }

        // 返还会员课时
        returnMemberSessions(memberId, course.getCourseType());

        log.info("预约取消成功，预约ID：{}", bookingId);
    }

    /**
     * 转换为可预约课程DTO
     */
    private MiniAvailableCourseDTO convertToAvailableCourseDTO(Course course) {
        MiniAvailableCourseDTO dto = new MiniAvailableCourseDTO();
        dto.setCourseId(course.getId());
        dto.setCourseName(course.getCourseName());
        dto.setCourseType(course.getCourseType());
        dto.setCourseDate(course.getCourseDate());
        dto.setStartTime(course.getStartTime());
        dto.setEndTime(course.getEndTime());
        dto.setPrice(course.getPrice());
        dto.setOriginalPrice(course.getPrice()); // 如果没有原价，就用现价
        dto.setDiscount(BigDecimal.valueOf(10)); // 默认10折
        dto.setMaxCapacity(course.getMaxCapacity());
        dto.setCurrentEnrollment(course.getCurrentEnrollment());
        dto.setRemainingSlots(course.getMaxCapacity() - course.getCurrentEnrollment());
        dto.setLocation(course.getLocation());
        dto.setDescription(course.getDescription());

        // 获取教练信息
        Coach coach = coachMapper.selectById(course.getCoachId());
        if (coach != null) {
            dto.setCoachId(coach.getId());
            dto.setCoachName(coach.getRealName());
        }

        return dto;
    }

    /**
     * 转换为预约DTO
     */
    private MiniBookingDTO convertToMiniBookingDTO(CourseBooking booking, Course course) {
        MiniBookingDTO dto = new MiniBookingDTO();
        dto.setBookingId(booking.getId());
        dto.setCourseId(course.getId());
        dto.setCourseName(course.getCourseName());
        dto.setCourseType(course.getCourseType());
        dto.setCourseDate(course.getCourseDate().toString());
        dto.setStartTime(course.getStartTime().format(TIME_FORMATTER));
        dto.setEndTime(course.getEndTime().format(TIME_FORMATTER));
        dto.setLocation(course.getLocation());
        dto.setBookingTime(booking.getBookingTime());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setCheckinTime(booking.getCheckinTime());

        Coach coach = coachMapper.selectById(course.getCoachId());
        if (coach != null) {
            dto.setCoachName(coach.getRealName());
        }

        return dto;
    }

    /**
     * 检查是否可以预约
     */
    private boolean checkCanBook(Long memberId, Course course) {
        // 检查会员是否有可用课时
        if (!hasAvailableSessions(memberId, course.getCourseType())) {
            return false;
        }

        // 检查是否已经预约
        LambdaQueryWrapper<CourseBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseBooking::getMemberId, memberId)
                .eq(CourseBooking::getCourseId, course.getId())
                .in(CourseBooking::getBookingStatus, 0, 1);
        Long count = courseBookingMapper.selectCount(queryWrapper);

        return count == 0;
    }

    /**
     * 获取不可预约原因
     */
    private String getCannotBookReason(Long memberId, Course course) {
        if (!hasAvailableSessions(memberId, course.getCourseType())) {
            return "可用课时不足";
        }

        LambdaQueryWrapper<CourseBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseBooking::getMemberId, memberId)
                .eq(CourseBooking::getCourseId, course.getId())
                .in(CourseBooking::getBookingStatus, 0, 1);
        Long count = courseBookingMapper.selectCount(queryWrapper);

        if (count > 0) {
            return "已预约该课程";
        }

        return "未知原因";
    }

    /**
     * 检查是否有可用课时
     */
    private boolean hasAvailableSessions(Long memberId, Integer courseType) {
        // 查询会员的有效订单项
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getProductType, courseType == 0 ? 1 : 2) // 私教课或团课
                .gt(OrderItem::getRemainingSessions, 0)
                .ge(OrderItem::getValidityEndDate, LocalDate.now())
                .eq(OrderItem::getStatus, "ACTIVE");

        Long count = orderItemMapper.selectCount(queryWrapper);
        return count > 0;
    }

    /**
     * 检查会员是否有足够的课时
     */
    private void checkMemberSessions(Long memberId, Integer courseType) {
        if (!hasAvailableSessions(memberId, courseType)) {
            throw new BusinessException("可用课时不足，请先购买课程");
        }
    }

    /**
     * 扣减会员课时
     */
    private void deductMemberSessions(Long memberId, Integer courseType) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getProductType, courseType == 0 ? 1 : 2)
                .gt(OrderItem::getRemainingSessions, 0)
                .ge(OrderItem::getValidityEndDate, LocalDate.now())
                .eq(OrderItem::getStatus, "ACTIVE")
                .orderByAsc(OrderItem::getValidityEndDate); // 先用快过期的

        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(orderItems)) {
            OrderItem orderItem = orderItems.get(0);
            orderItem.setRemainingSessions(orderItem.getRemainingSessions() - 1);

            if (orderItem.getRemainingSessions() <= 0) {
                orderItem.setStatus("USED_UP");
            }

            orderItemMapper.updateById(orderItem);
        }
    }

    /**
     * 返还会员课时
     */
    private void returnMemberSessions(Long memberId, Integer courseType) {
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getProductType, courseType == 0 ? 1 : 2)
                .ge(OrderItem::getValidityEndDate, LocalDate.now())
                .orderByDesc(OrderItem::getRemainingSessions);

        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(orderItems)) {
            OrderItem orderItem = orderItems.get(0);
            orderItem.setRemainingSessions(orderItem.getRemainingSessions() + 1);
            orderItem.setStatus("ACTIVE");
            orderItemMapper.updateById(orderItem);
        }
    }

    /**
     * 生成签到码
     */
    private void generateCheckinCode(Long bookingId, LocalDateTime courseDateTime) {
        // 生成6位随机数字码
        String digitalCode = String.format("%06d", new Random().nextInt(1000000));

        // 计算有效期（课前30分钟到课程开始）
        int startMinutes = configValidator.getCheckinStartMinutes();
        int endMinutes = configValidator.getCheckinEndMinutes();

        LocalDateTime validStart = courseDateTime.minusMinutes(startMinutes);
        LocalDateTime validEnd = endMinutes == 0 ?
                courseDateTime : courseDateTime.plusMinutes(endMinutes);

        MiniCheckinCode checkinCode = new MiniCheckinCode();
        checkinCode.setBookingId(bookingId);
        checkinCode.setDigitalCode(digitalCode);
        checkinCode.setStatus(0); // 未使用
        checkinCode.setValidStartTime(validStart);
        checkinCode.setValidEndTime(validEnd);

        miniCheckinCodeMapper.insert(checkinCode);
    }

    /**
     * 检查是否可以取消
     */
    private boolean checkCanCancel(CourseBooking booking, Course course) {
        if (booking.getBookingStatus() != 0) {
            return false;
        }

        LocalDateTime courseDateTime = LocalDateTime.of(course.getCourseDate(), course.getStartTime());
        return configValidator.canCancelCourse(courseDateTime);
    }

    /**
     * 检查是否可以签到
     */
    private boolean checkCanCheckin(CourseBooking booking, Course course) {
        if (booking.getBookingStatus() != 0) {
            return false;
        }

        LocalDateTime courseDateTime = LocalDateTime.of(course.getCourseDate(), course.getStartTime());
        return configValidator.canCheckIn(courseDateTime);
    }
}