package com.gymflow.service.impl.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymflow.dto.coach.CoachBasicDTO;
import com.gymflow.dto.course.CourseFullDTO;
import com.gymflow.dto.mini.*;
import com.gymflow.entity.*;
import com.gymflow.exception.BusinessException;
import com.gymflow.exception.MiniBusinessException;
import com.gymflow.mapper.*;
import com.gymflow.service.mini.MiniBookingService;
import com.gymflow.service.mini.MiniMessageService;
import com.gymflow.utils.SystemConfigValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiniBookingServiceImpl implements MiniBookingService {

    private final CourseMapper courseMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final CourseBookingMapper courseBookingMapper;
    private final CoachMapper coachMapper;
    private final MemberMapper memberMapper;
    private final CourseCoachMapper courseCoachMapper;
    private final OrderItemMapper orderItemMapper;
    private final MiniMessageService miniMessageService;
    private final SystemConfigValidator configValidator;

    @Override
    public List<MiniAvailableCourseDTO> getAvailableCourses(Long memberId, Integer courseType, String keyword) {
        // 查询所有可预约的排课（团课）
        LambdaQueryWrapper<CourseSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(CourseSchedule::getScheduleDate, LocalDate.now())
                .eq(CourseSchedule::getStatus, 1) // 正常状态
                .orderByAsc(CourseSchedule::getScheduleDate)
                .orderByAsc(CourseSchedule::getStartTime);

        List<CourseSchedule> schedules = courseScheduleMapper.selectList(wrapper);

        return schedules.stream()
                .filter(schedule -> schedule.getCurrentEnrollment() < schedule.getMaxCapacity())
                .map(schedule -> {
                    Course course = courseMapper.selectById(schedule.getCourseId());
                    Coach coach = coachMapper.selectById(schedule.getCoachId());

                    if (course == null || coach == null) {
                        return null;
                    }

                    // 根据查询条件过滤
                    if (courseType != null && !course.getCourseType().equals(courseType)) {
                        return null;
                    }
                    if (keyword != null && !keyword.isEmpty()) {
                        if (!course.getCourseName().contains(keyword) &&
                                !coach.getRealName().contains(keyword)) {
                            return null;
                        }
                    }

                    MiniAvailableCourseDTO dto = new MiniAvailableCourseDTO();
                    dto.setScheduleId(schedule.getId());
                    dto.setCourseId(course.getId());
                    dto.setCourseName(course.getCourseName());
                    dto.setCourseType(course.getCourseType());
                    if (course.getCourseType() == 0) {
                        dto.setCourseTypeDesc("私教课");
                    } else {
                        dto.setCourseTypeDesc("团课");
                    }
                    dto.setCoachId(coach.getId());
                    dto.setCoachName(coach.getRealName());
                    dto.setCourseDate(schedule.getScheduleDate());
                    dto.setStartTime(schedule.getStartTime());
                    dto.setEndTime(schedule.getEndTime());
                    dto.setPrice(course.getPrice());
                    dto.setOriginalPrice(course.getPrice());
                    dto.setDiscount(BigDecimal.valueOf(100));
                    dto.setMaxCapacity(schedule.getMaxCapacity());
                    dto.setCurrentEnrollment(schedule.getCurrentEnrollment());
                    dto.setRemainingSlots(schedule.getMaxCapacity() - schedule.getCurrentEnrollment());
                    dto.setDescription(course.getDescription());

                    // 检查会员是否已预约
                    LambdaQueryWrapper<CourseBooking> bookingWrapper = new LambdaQueryWrapper<>();
                    bookingWrapper.eq(CourseBooking::getMemberId, memberId)
                            .eq(CourseBooking::getScheduleId, schedule.getId())
                            .ne(CourseBooking::getBookingStatus, 3); // 排除已取消

                    boolean alreadyBooked = courseBookingMapper.selectCount(bookingWrapper) > 0;
                    dto.setCanBook(!alreadyBooked);

                    if (alreadyBooked) {
                        dto.setCannotBookReason("您已预约该课程");
                    } else if (schedule.getCurrentEnrollment() >= schedule.getMaxCapacity()) {
                        dto.setCannotBookReason("课程已满员");
                    }

                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public CourseFullDTO getCourseDetailForBooking(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new MiniBusinessException("课程不存在");
        }

        CourseFullDTO dto = new CourseFullDTO();
        dto.setId(course.getId());
        dto.setCourseType(course.getCourseType());
        if (course.getCourseType() == 0) {
            dto.setCourseTypeDesc("私教课");
        } else {
            dto.setCourseTypeDesc("团课");
        }
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        dto.setDuration(course.getDuration());
        dto.setPrice(course.getPrice());
        dto.setStatus(course.getStatus());
        dto.setNotice(course.getNotice());
        dto.setCreateTime(course.getCreateTime());
        dto.setUpdateTime(course.getUpdateTime());

        // 查询可授课教练
        List<Long> coachIds = courseCoachMapper.selectCoachIdsByCourseId(courseId);
        if (!coachIds.isEmpty()) {
            List<Coach> coaches = coachMapper.selectBatchIds(coachIds);
            dto.setCoaches(convertToCoachBasicDTO(coaches));
        }

        // 查询未来可预约的排课
        LambdaQueryWrapper<CourseSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(CourseSchedule::getCourseId, courseId)
                .ge(CourseSchedule::getScheduleDate, LocalDate.now())
                .eq(CourseSchedule::getStatus, 1)
                .orderByAsc(CourseSchedule::getScheduleDate)
                .orderByAsc(CourseSchedule::getStartTime);

        List<CourseSchedule> schedules = courseScheduleMapper.selectList(scheduleWrapper);

        // 转换为排课VO（简化版，只返回基本信息）
        List<com.gymflow.vo.CourseScheduleVO> scheduleVOs = schedules.stream()
                .filter(s -> s.getCurrentEnrollment() < s.getMaxCapacity())
                .map(schedule -> {
                    com.gymflow.vo.CourseScheduleVO vo = new com.gymflow.vo.CourseScheduleVO();
                    vo.setScheduleId(schedule.getId());
                    vo.setScheduleDate(schedule.getScheduleDate());
                    vo.setStartTime(schedule.getStartTime());
                    vo.setEndTime(schedule.getEndTime());
                    vo.setMaxCapacity(schedule.getMaxCapacity());
                    vo.setCurrentEnrollment(schedule.getCurrentEnrollment());
                    vo.setRemainingSlots(schedule.getMaxCapacity() - schedule.getCurrentEnrollment());

                    Coach coach = coachMapper.selectById(schedule.getCoachId());
                    if (coach != null) {
                        vo.setCoachName(coach.getRealName());
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        dto.setSchedules(scheduleVOs);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBooking(Long memberId, MiniCreateBookingDTO createDTO) {
        // 检查排课是否存在
        CourseSchedule schedule = courseScheduleMapper.selectById(createDTO.getScheduleId());
        if (schedule == null) {
            throw new MiniBusinessException("排课不存在");
        }

        // 检查排课状态
        if (schedule.getStatus() != 1) {
            throw new MiniBusinessException("该排课已取消");
        }

        // 检查是否还有名额
        if (schedule.getCurrentEnrollment() >= schedule.getMaxCapacity()) {
            throw new MiniBusinessException("该课程已满员");
        }

        // 检查会员是否已预约过
        LambdaQueryWrapper<CourseBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseBooking::getMemberId, memberId)
                .eq(CourseBooking::getScheduleId, createDTO.getScheduleId())
                .ne(CourseBooking::getBookingStatus, 3); // 排除已取消

        if (courseBookingMapper.selectCount(wrapper) > 0) {
            throw new MiniBusinessException("您已预约过该课程");
        }

        // 检查课程开始时间是否有效
        LocalDateTime courseDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
        if (courseDateTime.isBefore(LocalDateTime.now())) {
            throw new MiniBusinessException("不能预约已开始的课程");
        }

        // 获取课程信息
        Course course = courseMapper.selectById(schedule.getCourseId());

        // 检查会员是否有可用课时（如果是私教课）
        if (course.getCourseType() == 0) { // 私教课
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getProductType, 1) // 私教课
                    .eq(OrderItem::getStatus, "ACTIVE")
                    .gt(OrderItem::getRemainingSessions, 0)
                    .orderByDesc(OrderItem::getValidityEndDate);

            List<OrderItem> availableItems = orderItemMapper.selectList(itemWrapper);
            if (availableItems.isEmpty()) {
                throw new MiniBusinessException("您没有可用的私教课时");
            }

            // 使用最早过期的课时
            OrderItem item = availableItems.get(0);
            item.setRemainingSessions(item.getRemainingSessions() - 1);
            orderItemMapper.updateById(item);
        }

        // 创建预约
        CourseBooking booking = new CourseBooking();
        booking.setMemberId(memberId);
        booking.setScheduleId(createDTO.getScheduleId());
        booking.setCourseId(schedule.getCourseId());
        booking.setBookingStatus(0); // 待上课
        booking.setBookingTime(LocalDateTime.now());

        // 生成6位随机签到码
        String signCode = generateSignCode();
        booking.setSignCode(signCode);

        // 设置签到码过期时间（课前30分钟到课后15分钟）
        booking.setSignCodeExpireTime(courseDateTime.plusMinutes(15));

        // 设置自动完成时间（课程结束后1小时）
        LocalDateTime courseEndTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getEndTime());
        booking.setAutoCompleteTime(courseEndTime.plusHours(configValidator.getAutoCompleteHours()));

        courseBookingMapper.insert(booking);

        // 更新排课当前人数
        schedule.setCurrentEnrollment(schedule.getCurrentEnrollment() + 1);
        courseScheduleMapper.updateById(schedule);

        // 发送预约成功消息
        Member member = memberMapper.selectById(memberId);
        Coach coach = coachMapper.selectById(schedule.getCoachId());
        String messageContent = String.format("您已成功预约 %s - %s，教练：%s",
                course.getCourseName(),
                schedule.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                coach != null ? coach.getRealName() : "");

        miniMessageService.sendMessage(
                memberId, 0, "BOOKING_SUCCESS",
                "预约成功",
                messageContent,
                booking.getId()
        );

        return booking.getId();
    }

    @Override
    public List<MiniBookingDTO> getMyBookings(Long memberId, Integer status) {
        LambdaQueryWrapper<CourseBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseBooking::getMemberId, memberId);

        if (status != null) {
            wrapper.eq(CourseBooking::getBookingStatus, status);
        } else {
            // 默认不显示已取消的
            wrapper.ne(CourseBooking::getBookingStatus, 3);
        }

        wrapper.orderByDesc(CourseBooking::getBookingTime);

        List<CourseBooking> bookings = courseBookingMapper.selectList(wrapper);

        return bookings.stream().map(booking -> {
                    CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
                    if (schedule == null) {
                        return null;
                    }

                    Course course = courseMapper.selectById(booking.getCourseId());
                    Coach coach = coachMapper.selectById(schedule.getCoachId());

                    MiniBookingDTO dto = new MiniBookingDTO();
                    dto.setBookingId(booking.getId());
                    dto.setScheduleId(schedule.getId());
                    dto.setCourseId(course.getId());
                    dto.setCourseName(course.getCourseName());
                    dto.setCourseType(course.getCourseType());
                    dto.setCoachName(coach != null ? coach.getRealName() : "");
                    dto.setCourseDate(schedule.getScheduleDate().toString());
                    dto.setStartTime(schedule.getStartTime().toString());
                    dto.setEndTime(schedule.getEndTime().toString());
                    dto.setBookingTime(booking.getBookingTime());
                    dto.setBookingStatus(booking.getBookingStatus());
                    dto.setCheckinTime(booking.getCheckinTime());

                    // 判断是否可取消
                    if (booking.getBookingStatus() == 0) {
                        LocalDateTime courseDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
                        dto.setCanCancel(configValidator.canCancelCourse(courseDateTime));
                    } else {
                        dto.setCanCancel(false);
                    }

                    // 判断是否可签到
                    if (booking.getBookingStatus() == 0) {
                        LocalDateTime courseDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
                        dto.setCanCheckin(configValidator.canCheckIn(courseDateTime));
                    } else {
                        dto.setCanCheckin(false);
                    }

                    return dto;
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public MiniBookingDetailDTO getBookingDetail(Long userId, Long bookingId) {
        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new MiniBusinessException("预约记录不存在");
        }

        // 验证权限（只能查看自己的预约）
        if (!booking.getMemberId().equals(userId)) {
            throw new MiniBusinessException("无权查看此预约");
        }

        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        if (schedule == null) {
            throw new MiniBusinessException("排课信息不存在");
        }

        Course course = courseMapper.selectById(booking.getCourseId());
        Coach coach = coachMapper.selectById(schedule.getCoachId());
        Member member = memberMapper.selectById(booking.getMemberId());

        MiniBookingDetailDTO dto = new MiniBookingDetailDTO();
        dto.setBookingId(booking.getId());
        dto.setScheduleId(schedule.getId());
        dto.setCourseId(course.getId());
        dto.setCourseName(course.getCourseName());
        dto.setCourseType(course.getCourseType());
        if (course.getCourseType() == 0) {
            dto.setCourseTypeDesc("私教课");
        } else {
            dto.setCourseTypeDesc("团课");
        }
        dto.setCourseDescription(course.getDescription());
        dto.setCoachId(coach.getId());
        dto.setCoachName(coach.getRealName());
        dto.setCoachPhone(coach.getPhone());
        dto.setCourseDate(schedule.getScheduleDate().toString());
        dto.setStartTime(schedule.getStartTime().toString());
        dto.setEndTime(schedule.getEndTime().toString());
        dto.setPrice(course.getPrice());
        dto.setBookingTime(booking.getBookingTime());
        dto.setBookingStatus(booking.getBookingStatus());
        if (booking.getBookingStatus() == 0) {
            dto.setBookingStatusDesc("待上课");
        } else if (booking.getBookingStatus() == 1) {
            dto.setBookingStatusDesc("已签到");
        } else if (booking.getBookingStatus() == 2) {
            dto.setBookingStatusDesc("已完成");
        } else if (booking.getBookingStatus() == 3) {
            dto.setBookingStatusDesc("已取消");
        }
        dto.setCheckinTime(booking.getCheckinTime());
        dto.setCancellationReason(booking.getCancellationReason());
        dto.setCancellationTime(booking.getCancellationTime());
        dto.setMemberName(member.getRealName());
        dto.setMemberPhone(member.getPhone());
        dto.setMemberNo(member.getMemberNo());

        // 获取签到码信息
        if (booking.getBookingStatus() == 0) {
            LocalDateTime courseDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
            if (configValidator.canCheckIn(courseDateTime)) {
                MiniCheckinCodeDTO codeDTO = new MiniCheckinCodeDTO();
                codeDTO.setBookingId(booking.getId());
                codeDTO.setDigitalCode(booking.getSignCode());
                codeDTO.setStatus(0);
                codeDTO.setValidStartTime(courseDateTime.minusMinutes(configValidator.getCheckinStartMinutes()));
                codeDTO.setValidEndTime(booking.getSignCodeExpireTime());
                codeDTO.setCourseName(course.getCourseName());
                codeDTO.setCourseStartTime(courseDateTime);
                codeDTO.setMemberName(member.getRealName());
                codeDTO.setCoachName(coach.getRealName());

                dto.setCheckinCode(codeDTO);
            }
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long memberId, Long bookingId, String reason) {
        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new MiniBusinessException("预约记录不存在");
        }

        // 验证权限
        if (!booking.getMemberId().equals(memberId)) {
            throw new MiniBusinessException("无权取消此预约");
        }

        if (booking.getBookingStatus() != 0) {
            throw new MiniBusinessException("只有待上课的预约可以取消");
        }

        // 验证取消时间
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        if (schedule == null) {
            throw new MiniBusinessException("排课信息不存在");
        }

        LocalDateTime courseDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
        configValidator.validateCourseCancellation(courseDateTime);

        // 更新预约状态
        booking.setBookingStatus(3); // 已取消
        booking.setCancellationReason(reason);
        booking.setCancellationTime(LocalDateTime.now());
        courseBookingMapper.updateById(booking);

        // 更新排课当前人数
        schedule.setCurrentEnrollment(schedule.getCurrentEnrollment() - 1);
        courseScheduleMapper.updateById(schedule);

        // 如果是私教课，需要恢复课时
        Course course = courseMapper.selectById(booking.getCourseId());
        if (course.getCourseType() == 0) { // 私教课
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getProductType, 1)
                    .eq(OrderItem::getStatus, "ACTIVE")
                    .orderByDesc(OrderItem::getCreateTime)
                    .last("LIMIT 1");

            OrderItem item = orderItemMapper.selectOne(itemWrapper);
            if (item != null) {
                item.setRemainingSessions(item.getRemainingSessions() + 1);
                orderItemMapper.updateById(item);
            }
        }

        // 发送取消消息
        Member member = memberMapper.selectById(memberId);
        miniMessageService.sendMessage(
                memberId, 0, "BOOKING_CANCEL",
                "预约已取消",
                String.format("您已取消 %s 的预约，取消原因：%s", course.getCourseName(), reason),
                booking.getId()
        );
    }

    /**
     * 生成6位随机签到码
     */
    private String generateSignCode() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }

    /**
     * 转换为教练基础DTO
     */
    private List<CoachBasicDTO> convertToCoachBasicDTO(List<Coach> coaches) {
        return coaches.stream().map(coach -> {
            CoachBasicDTO dto = new CoachBasicDTO();
            dto.setId(coach.getId());
            dto.setRealName(coach.getRealName());
            dto.setPhone(coach.getPhone());
            dto.setGender(coach.getGender());
            dto.setSpecialty(coach.getSpecialty());

            // 将JSON字符串转换为List<String>
            if (coach.getCertifications() != null) {
                String certStr = coach.getCertifications();
                List<String> certs = List.of(certStr.replace("[", "").replace("]", "").replace("\"", "").split(","));
                dto.setCertificationList(certs);
            }

            dto.setYearsOfExperience(coach.getYearsOfExperience());
            dto.setHourlyRate(coach.getHourlyRate());
            dto.setCommissionRate(coach.getCommissionRate());
            dto.setStatus(coach.getStatus());
            dto.setRating(coach.getRating());
            dto.setIntroduction(coach.getIntroduction());
            dto.setCreateTime(coach.getCreateTime());
            dto.setUpdateTime(coach.getUpdateTime());

            return dto;
        }).collect(Collectors.toList());
    }
}