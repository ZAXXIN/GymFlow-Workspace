package com.gymflow.service.impl.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymflow.dto.mini.MiniCheckinCodeDTO;
import com.gymflow.dto.mini.MiniReminderDTO;
import com.gymflow.dto.mini.MiniScanResultDTO;
import com.gymflow.entity.*;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.gymflow.service.mini.MiniCheckInService;
import com.gymflow.utils.SystemConfigValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiniCheckInServiceImpl implements MiniCheckInService {
    private final CourseBookingMapper courseBookingMapper;
    private final CourseMapper courseMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final MemberMapper memberMapper;
    private final CoachMapper coachMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final SystemConfigValidator configValidator;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public MiniCheckinCodeDTO getCheckinCode(Long memberId, Long bookingId) {
        // 验证预约是否存在且属于该会员
        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预约不存在");
        }

        if (!booking.getMemberId().equals(memberId)) {
            throw new BusinessException("无权查看他人的签到码");
        }

        // 验证预约状态
        if (booking.getBookingStatus() != 0) {
            throw new BusinessException("当前状态无法获取签到码");
        }

        // 获取排课信息
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("排课不存在");
        }

        // 获取课程信息
        Course course = courseMapper.selectById(schedule.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 获取教练信息
        Coach coach = coachMapper.selectById(schedule.getCoachId());

        // 计算签到码有效期（使用系统配置）
        LocalDateTime scheduleDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
        int startMinutes = configValidator.getCheckinStartMinutes();
        int endMinutes = configValidator.getCheckinEndMinutes();

        LocalDateTime validStartTime = scheduleDateTime.minusMinutes(startMinutes);
        LocalDateTime validEndTime = endMinutes == 0 ? scheduleDateTime : scheduleDateTime.plusMinutes(endMinutes);

        // 转换为DTO
        MiniCheckinCodeDTO dto = new MiniCheckinCodeDTO();
        dto.setBookingId(booking.getId());
        dto.setDigitalCode(booking.getSignCode());
        dto.setStatus(booking.getBookingStatus() == 0 ? 0 : 1); // 0-未使用，1-已使用
        dto.setValidStartTime(validStartTime);
        dto.setValidEndTime(validEndTime);
        dto.setCourseName(course.getCourseName());
        dto.setCourseStartTime(scheduleDateTime);
        if (coach != null) {
            dto.setCoachName(coach.getRealName());
        }

        // 获取会员信息
        Member member = memberMapper.selectById(memberId);
        if (member != null) {
            dto.setMemberName(member.getRealName());
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MiniScanResultDTO scanCheckin(Long coachId, String qrCode) {
        log.info("教练扫码核销，教练ID：{}，二维码：{}", coachId, qrCode);

        // 解析二维码（这里简化处理，实际可能需要解析JSON）
        // 假设二维码内容是预约ID
        Long bookingId;
        try {
            bookingId = Long.parseLong(qrCode);
        } catch (NumberFormatException e) {
            throw new BusinessException("无效的二维码");
        }

        // 直接从 course_booking 表查询预约
        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("无效的签到码");
        }

        return executeCheckin(booking, 0, "教练扫码核销");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MiniScanResultDTO verifyCode(String digitalCode, Integer checkinMethod, String notes) {
        log.info("数字码核销，数字码：{}", digitalCode);

        // 直接从 course_booking 表查询签到码
        LambdaQueryWrapper<CourseBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseBooking::getSignCode, digitalCode)
                .eq(CourseBooking::getBookingStatus, 0); // 待上课

        CourseBooking booking = courseBookingMapper.selectOne(queryWrapper);
        if (booking == null) {
            throw new BusinessException("无效的数字码");
        }

        return executeCheckin(booking, checkinMethod, notes);
    }

    @Override
    public Boolean checkCodeValid(Long userId, Long bookingId) {
        // 验证预约是否存在
        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            return false;
        }

        // 检查预约状态
        if (booking.getBookingStatus() != 0) {
            return false;
        }

        // 获取排课信息
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        if (schedule == null) {
            return false;
        }

        // 检查有效期（使用系统配置）
        LocalDateTime scheduleDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
        int startMinutes = configValidator.getCheckinStartMinutes();
        int endMinutes = configValidator.getCheckinEndMinutes();

        LocalDateTime validStartTime = scheduleDateTime.minusMinutes(startMinutes);
        LocalDateTime validEndTime = endMinutes == 0 ? scheduleDateTime : scheduleDateTime.plusMinutes(endMinutes);

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(validStartTime) || now.isAfter(validEndTime)) {
            return false;
        }

        return true;
    }

    /**
     * 执行核销
     */
    @Transactional(rollbackFor = Exception.class)
    protected MiniScanResultDTO executeCheckin(CourseBooking booking,
                                               Integer checkinMethod, String notes) {
        // 检查预约状态
        if (booking.getBookingStatus() != 0) {
            throw new BusinessException("预约状态不正确，无法签到");
        }

        // 获取排课信息验证签到时间
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("排课不存在");
        }

        LocalDateTime scheduleDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
        configValidator.validateCheckInTime(scheduleDateTime);

        // 检查是否已签到
        LambdaQueryWrapper<CheckinRecord> checkinQuery = new LambdaQueryWrapper<>();
        checkinQuery.eq(CheckinRecord::getCourseBookingId, booking.getId());
        Long checkInCount = checkInRecordMapper.selectCount(checkinQuery);
        if (checkInCount > 0) {
            throw new BusinessException("该预约已签到");
        }

        LocalDateTime now = LocalDateTime.now();

        // 创建签到记录
        CheckinRecord checkinRecord = new CheckinRecord();
        checkinRecord.setMemberId(booking.getMemberId());
        checkinRecord.setCourseBookingId(booking.getId());
        checkinRecord.setCheckinTime(now);
        checkinRecord.setCheckinMethod(checkinMethod);
        checkinRecord.setNotes(notes != null ? notes : "扫码核销");

        int result = checkInRecordMapper.insert(checkinRecord);
        if (result <= 0) {
            throw new BusinessException("签到失败");
        }

        // 更新预约状态
        booking.setBookingStatus(1); // 已签到
        booking.setCheckinTime(now);

        // 设置自动完成时间
        int autoCompleteHours = configValidator.getAutoCompleteHours();
        if (autoCompleteHours > 0) {
            booking.setAutoCompleteTime(now.plusHours(autoCompleteHours));
        }

        courseBookingMapper.updateById(booking);

        // 获取会员信息
        Member member = memberMapper.selectById(booking.getMemberId());
        Course course = courseMapper.selectById(schedule.getCourseId());

        // 构建结果
        MiniScanResultDTO resultDTO = new MiniScanResultDTO();
        resultDTO.setSuccess(true);
        resultDTO.setMessage("核销成功");
        resultDTO.setBookingId(booking.getId());
        resultDTO.setCourseName(course != null ? course.getCourseName() : "");
        if (member != null) {
            resultDTO.setMemberName(member.getRealName());
            resultDTO.setMemberPhone(member.getPhone());
        }
        resultDTO.setBookingTime(booking.getBookingTime().format(TIME_FORMATTER));
        resultDTO.setCheckinTime(now.format(TIME_FORMATTER));

        log.info("核销成功，预约ID：{}", booking.getId());
        return resultDTO;
    }

    @Override
    public MiniReminderDTO getCurrentReminder(Long userId, Integer userType) {
        log.info("获取当前时段提醒，用户ID：{}，用户类型：{}", userId, userType);

        MiniReminderDTO reminder = new MiniReminderDTO();
        reminder.setHasReminder(false);

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        if (userType == 0) {
            // 会员端提醒：显示当前时段的课程
            LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
            bookingQuery.eq(CourseBooking::getMemberId, userId)
                    .eq(CourseBooking::getBookingStatus, 0); // 待上课

            List<CourseBooking> bookings = courseBookingMapper.selectList(bookingQuery);

            for (CourseBooking booking : bookings) {
                CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
                if (schedule == null || !schedule.getScheduleDate().equals(today)) {
                    continue;
                }

                LocalDateTime courseStart = LocalDateTime.of(today, schedule.getStartTime());
                LocalDateTime courseEnd = LocalDateTime.of(today, schedule.getEndTime());

                // 课程开始前30分钟到课程结束
                if (now.isAfter(courseStart.minusMinutes(30)) && now.isBefore(courseEnd)) {
                    reminder.setHasReminder(true);
                    reminder.setType("MEMBER");

                    Course course = courseMapper.selectById(schedule.getCourseId());
                    if (course != null) {
                        reminder.setCourseName(course.getCourseName());
                    }

                    reminder.setBookingTime(courseStart);
                    reminder.setBookingId(booking.getId());

                    // 获取教练姓名
                    Coach coach = coachMapper.selectById(schedule.getCoachId());
                    if (coach != null) {
                        reminder.setCoachName(coach.getRealName());
                    }

                    // 计算签到码有效期
                    int startMinutes = configValidator.getCheckinStartMinutes();
                    int endMinutes = configValidator.getCheckinEndMinutes();
                    LocalDateTime validStartTime = courseStart.minusMinutes(startMinutes);
                    LocalDateTime validEndTime = endMinutes == 0 ? courseStart : courseStart.plusMinutes(endMinutes);

                    // 构建签到码DTO
                    MiniCheckinCodeDTO codeDTO = new MiniCheckinCodeDTO();
                    codeDTO.setBookingId(booking.getId());
                    codeDTO.setDigitalCode(booking.getSignCode());
                    codeDTO.setValidStartTime(validStartTime);
                    codeDTO.setValidEndTime(validEndTime);
                    if (course != null) {
                        codeDTO.setCourseName(course.getCourseName());
                    }
                    codeDTO.setCourseStartTime(courseStart);
                    if (coach != null) {
                        codeDTO.setCoachName(coach.getRealName());
                    }
                    reminder.setCheckinCode(codeDTO);
                    break;
                }
            }

        } else if (userType == 1) {
            // 教练端提醒：显示当前时段的学员
            LambdaQueryWrapper<CourseSchedule> scheduleQuery = new LambdaQueryWrapper<>();
            scheduleQuery.eq(CourseSchedule::getCoachId, userId)
                    .eq(CourseSchedule::getScheduleDate, today)
                    .le(CourseSchedule::getStartTime, currentTime.plusMinutes(30))
                    .ge(CourseSchedule::getEndTime, currentTime);

            List<CourseSchedule> schedules = courseScheduleMapper.selectList(scheduleQuery);

            for (CourseSchedule schedule : schedules) {
                // 查询该排课的预约学员
                LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
                bookingQuery.eq(CourseBooking::getScheduleId, schedule.getScheduleId())
                        .eq(CourseBooking::getBookingStatus, 0);

                List<CourseBooking> bookings = courseBookingMapper.selectList(bookingQuery);
                if (!bookings.isEmpty()) {
                    CourseBooking booking = bookings.get(0);
                    Member member = memberMapper.selectById(booking.getMemberId());
                    Course course = courseMapper.selectById(schedule.getCourseId());

                    if (member != null && course != null) {
                        reminder.setHasReminder(true);
                        reminder.setType("COACH");
                        reminder.setMemberId(member.getId());
                        reminder.setMemberName(member.getRealName());
                        reminder.setBookedCourseName(course.getCourseName());
                        reminder.setBookedStartTime(LocalDateTime.of(today, schedule.getStartTime()));
                        reminder.setBookedEndTime(LocalDateTime.of(today, schedule.getEndTime()));
                    }
                    break;
                }
            }
        }

        return reminder;
    }
}